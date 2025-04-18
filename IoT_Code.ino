#include <WiFi.h>
#include <Firebase_ESP_Client.h>
#include <Wire.h>
#include <Adafruit_ADXL345_U.h>
#include <Adafruit_Sensor.h>
#include <TinyGPS++.h>
#include <HardwareSerial.h>

#include "addons/TokenHelper.h"
#include "addons/RTDBHelper.h"

// WiFi credentials
const char* ssid = "";
const char* password = "";

// Firebase configuration
#define FIREBASE_HOST ""
#define API_KEY ""
#define USER_EMAIL ""
#define USER_PASSWORD ""

// Define Firebase objects
FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

// Pin definitions
const int soilMoisturePin = 34;
const int rainSensorPin = 35;
HardwareSerial gpsSerial(1);
TinyGPSPlus gps;

// ADXL345 Object
Adafruit_ADXL345_Unified accel = Adafruit_ADXL345_Unified(123);

// Sensor Calibration Values
const int dryValue = 3500;
const int wetValue = 1500;
const int rainDryValue = 4095;
const int rainWetValue = 0;

// Individual Sensor Thresholds
const int moistureThresholdPercentage = 80;
const int rainThresholdPercentage = 40;
const float vibrationThreshold = 20.0;

// Combined Threshold Parameters based on Research Data
// For Soil Moisture + Rainfall
const float moistureRainMoistureWeight = 0.6;
const float moistureRainRainWeight = 0.4;
const float moistureRainThreshold = 58.0;

// For Rainfall + Vibration (Multiplicative model)
const float rainVibrationMultiThreshold = 0.35;

// Triple Combination Critical Values from Research
const int tripleMoistureThreshold = 50;
const int tripleRainThreshold = 20;
const float tripleVibrationThreshold = 12.0;

// Alert timing: persistent time tracking for alert cooldown
unsigned long lastAlertTime = 0;
const unsigned long alertCooldown = 60000;
const int numReadings = 5;
int soilReadings[numReadings] = {0};
int rainReadings[numReadings] = {0};
int readIndex = 0;

// Modified GPS handling
double previousLat = NAN;
double previousLon = NAN;


void setup() {
  Serial.begin(115200);
  Wire.begin();

  pinMode(soilMoisturePin, INPUT);
  pinMode(rainSensorPin, INPUT);

  // Connect to WiFi
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Connecting to WiFi...");
  }
  Serial.println("Connected to WiFi");

  // Firebase setup
  config.api_key = API_KEY;
  config.database_url = FIREBASE_HOST;
  auth.user.email = USER_EMAIL;
  auth.user.password = USER_PASSWORD;

  Firebase.begin(&config, &auth);
  Firebase.reconnectNetwork(true);
  Serial.println("Firebase Initialized");

  // ADXL345 Initialization with Error Handling
  if (!accel.begin()) {
    Serial.println("Failed to find ADXL345 sensor! Vibration will be set to 0.");
  } else {
    Serial.println("ADXL345 Found!");
    accel.setRange(ADXL345_RANGE_16_G);
  }

  // GPS Initialization (RX=16, TX=17)
  gpsSerial.begin(9600, SERIAL_8N1, 16, 17);
}

void loop() {
  // Sensor reading with moving average
  soilReadings[readIndex] = analogRead(soilMoisturePin);
  rainReadings[readIndex] = analogRead(rainSensorPin);
  readIndex = (readIndex + 1) % numReadings;

  // Calculate smoothed values
  int soilSmoothed = 0, rainSmoothed = 0;
  for (int i = 0; i < numReadings; i++) {
    soilSmoothed += soilReadings[i];
    rainSmoothed += rainReadings[i];
  }
  soilSmoothed /= numReadings;
  rainSmoothed /= numReadings;

  // Convert soil moisture to percentage using calibration values
  int soilMoisturePercentage = map(soilSmoothed, dryValue, wetValue, 0, 100);
  soilMoisturePercentage = constrain(soilMoisturePercentage, 0, 100);

  // Convert rain sensor value to percentage using calibration values
  int rainPercentage = map(rainSmoothed, rainDryValue, rainWetValue, 0, 100);
  rainPercentage = constrain(rainPercentage, 0, 100);

  // ADXL345 Vibration Reading with error handling
  // Improved vibration reading with error recovery
  static bool accelConnected = true;
  static unsigned long lastSensorCheck = 0;
  float vibration = 0.0;
  
  if (millis() - lastSensorCheck > 60000) {
    accelConnected = accel.begin();
    lastSensorCheck = millis();
  }

  if (accelConnected) {
    sensors_event_t event;
    if (accel.getEvent(&event)) {
      vibration = (abs(event.acceleration.x) + abs(event.acceleration.y) + abs(event.acceleration.z));
    } else {
    Serial.println("ADXL345 Not Detected! Using default vibration = 0.");
    }
  }

  // GPS Data processing
  while (gpsSerial.available() > 0) {
    gps.encode(gpsSerial.read());
  }
  double latitude = gps.location.isValid() ? gps.location.lat() : NAN;
  double longitude = gps.location.isValid() ? gps.location.lng() : NAN;

  // Geospatial validation (prevent sudden jumps)
  if (!isnan(latitude) && !isnan(previousLat)) {
    if (abs(latitude - previousLat) > 0.01 || abs(longitude - previousLon) > 0.01) {
      // Flag as potential GPS error
      Serial.println("GPS position jump detected!");
      latitude = NAN;
      longitude = NAN;
    }
  }
  previousLat = latitude;
  previousLon = longitude;

  // Debug output for sensor readings
  Serial.printf("Soil Moisture: %d%%\n", soilMoisturePercentage);
  Serial.printf("Rain Sensor: %d%%\n", rainPercentage);
  Serial.printf("Vibration: %.2f\n", vibration);
  Serial.printf("GPS: %f, %f\n", latitude, longitude);

  // Individual Alerts based on sensor thresholds
  bool moistureAlert = (soilMoisturePercentage > moistureThresholdPercentage);
  bool rainAlert = (rainPercentage > rainThresholdPercentage);
  bool vibrationAlert = (vibration > vibrationThreshold);

  // Combined threshold calculations:
  // 1. Soil Moisture + Rainfall (Risk Index formula)
  float moistureRainRiskIndex = (moistureRainMoistureWeight * soilMoisturePercentage) +
                                (moistureRainRainWeight * rainPercentage);
  bool moistureRainAlert = (moistureRainRiskIndex > moistureRainThreshold);

  // 2. Rainfall + Vibration (Multiplicative model)
  float rainVibrationMulti = (rainPercentage / 40.0) * (vibration / 20.0);
  bool rainVibrationAlert = (rainVibrationMulti > rainVibrationMultiThreshold);

  // 3. Triple Combination condition
  bool tripleAlert = (soilMoisturePercentage >= tripleMoistureThreshold) &&
                     (rainPercentage >= tripleRainThreshold) &&
                     (vibration >= tripleVibrationThreshold);

  // Flag to indicate if any alert condition is met
  bool alertCondition = moistureAlert || rainAlert || vibrationAlert ||
                        moistureRainAlert || rainVibrationAlert || tripleAlert;

  // --- Risk Calculation ---
  // Option: Combine normalized sensor readings (0-100%) using a simple average
  // Normalize each sensor: (actual / threshold) capped at 100%
  float moistureRiskPct   = (soilMoisturePercentage / (float)moistureThresholdPercentage) * 100.0;
  float rainRiskPct       = (rainPercentage / (float)rainThresholdPercentage) * 100.0;
  float vibrationRiskPct  = (vibration / vibrationThreshold) * 100.0;

  // Cap values at 100%
  moistureRiskPct = (moistureRiskPct > 100.0) ? 100.0 : moistureRiskPct;
  rainRiskPct = (rainRiskPct > 100.0) ? 100.0 : rainRiskPct;
  vibrationRiskPct = (vibrationRiskPct > 100.0) ? 100.0 : vibrationRiskPct;

  // Assign weights
  float individualRisk = 0.4 * moistureRiskPct + 0.4 * rainRiskPct + 0.2 * vibrationRiskPct;
  individualRisk = (individualRisk > 100.0) ? 100.0 : individualRisk;

  // Risk level categorization based on overall risk
  String riskLevel;
  if (individualRisk >= 75)
    riskLevel = "EXTREME";
  else if (individualRisk >= 50)
    riskLevel = "HIGH";
  else if (individualRisk >= 25)
    riskLevel = "MODERATE";
  else
    riskLevel = "LOW";

  // Build alert message based on prioritized conditions
  String alertMessage = "";
  String alertType = "";

  if (tripleAlert) {
    alertMessage = "IMMINENT LANDSLIDE DANGER! All critical factors exceeded safety thresholds.";
    alertType = "triple_critical";
  }
  else if (rainVibrationAlert && moistureRainAlert) {
    alertMessage = "SEVERE LANDSLIDE RISK! Multiple combined factors indicate critical soil instability.";
    alertType = "multiple_combined";
  }
  else if (rainVibrationAlert) {
    alertMessage = "Surface Instability Detected! Rain and vibration combination indicating potential slope failure.";
    alertType = "rain_vibration";
  }
  else if (moistureRainAlert) {
    alertMessage = "Soil Saturation Critical! Moisture and rainfall combination reaching dangerous levels.";
    alertType = "moisture_rain";
  }
  else if (vibrationAlert && (rainPercentage > (rainThresholdPercentage / 2) || soilMoisturePercentage > (moistureThresholdPercentage / 2))) {
    alertMessage = "Concerning Ground Movement! High vibration with elevated moisture/rainfall detected.";
    alertType = "vibration_plus";
  }
  else if (vibrationAlert) {
    alertMessage = "High Vibration Detected! Monitoring for potential ground movement.";
    alertType = "vibration_only";
  }
  else if (rainAlert && soilMoisturePercentage > (moistureThresholdPercentage / 2)) {
    alertMessage = "Heavy Rainfall on Semi-Saturated Soil! Monitoring for developing instability.";
    alertType = "rain_with_moisture";
  }
  else if (rainAlert) {
    alertMessage = "Heavy Rainfall Detected! Monitoring for water infiltration effects.";
    alertType = "rain_only";
  }
  else if (moistureAlert) {
    alertMessage = "High Soil Moisture! Soil approaching saturation threshold.";
    alertType = "moisture_only";
  }

  // Prefix alert with risk level if an alert condition is met
  if (!alertMessage.isEmpty()) {
    alertMessage = "Risk Level: " + riskLevel + "." + alertMessage;
  }

  // Debug outputs for combined calculations
  Serial.printf("Moisture+Rain Risk Index: %.1f (Alert: %s)\n", moistureRainRiskIndex, moistureRainAlert ? "YES" : "NO");
  Serial.printf("Rain+Vibration Multi: %.2f (Alert: %s)\n", rainVibrationMulti, rainVibrationAlert ? "YES" : "NO");
  Serial.printf("Triple Condition: %s\n", tripleAlert ? "YES" : "NO");
  Serial.printf("Overall Risk: %.1f%% - %s\n", individualRisk, riskLevel.c_str());
  if (!alertMessage.isEmpty()) {
    Serial.printf("Alert Type: %s\n", alertType.c_str());
    Serial.printf("Alert Message: %s\n", alertMessage.c_str());
  }

  // Update Firebase with sensor readings and risk
  if (Firebase.ready()) {
    Firebase.RTDB.setInt(&fbdo, "/sensor_readings/soil_moisture", soilMoisturePercentage);
    Firebase.RTDB.setInt(&fbdo, "/sensor_readings/rain_sensor", rainPercentage);
    Firebase.RTDB.setFloat(&fbdo, "/sensor_readings/vibration", vibration);
    Firebase.RTDB.setFloat(&fbdo, "/sensor_readings/gps_latitude", latitude);
    Firebase.RTDB.setFloat(&fbdo, "/sensor_readings/gps_longitude", longitude);
    Firebase.RTDB.setBool(&fbdo, "/sensor_readings/alert", alertCondition);

    // Cooldown-protected alert sending
    unsigned long currentTime = millis();
    if (alertCondition && !alertMessage.isEmpty()) {
      if (currentTime - lastAlertTime >= alertCooldown) {
        Firebase.RTDB.setString(&fbdo, "/alerts", alertMessage);
        Serial.println("Alert sent to Firebase: " + alertMessage);
        lastAlertTime = currentTime;

      }
      else {
        Serial.printf("Alert suppressed (cooldown active). Next available in %d seconds\n", 
                     (alertCooldown - (currentTime - lastAlertTime)) / 1000);
      }
    }
  }
  else {
    Serial.println("Firebase not ready! Data not sent.");
  }

  delay(5000);
}
