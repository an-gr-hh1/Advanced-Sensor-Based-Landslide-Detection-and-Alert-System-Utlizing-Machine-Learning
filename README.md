# Advanced-Sensor-Based-Landslide-Detection-and-Alert-System-Utlizing-Machine-Learning
 In this proposed system, we present a precise and timely warning system to reduce the effects of landslides. This system strategically partitions its ML and IoT components, improving both risk prediction and simultaneous supervision. The predictive model LightGBM, is trained with an extensive dataset obtained from DEM, which enabled mapping out high-risk areas with great accuracy. The system utilizes IoT nodes dedicated entirely to areas of high risk pinpointed by the ML model, consisting of a rain sensor, a soil moisture sensor, an ADXL 345 sensor, and a GPS module that sends real-time updates to a centralized cloud framework. All real-time sensed data are processed and demonstrated on an android dashboard and features an interactive map that shows hazard zones, allowing swift action and emergency response. It has been programmed to trigger an alert when abnormalities are detected. The combination of machine learning and the IoT allows for increased accuracy of prediction and responsiveness, making it a strong and scalable solution to protect people in regions prone to landslides. The proposed system significantlyimproves the efficiency of technology in disaster management.
<p align="center"><img src="images/pipeline.png" alt="Machine Learning Pipeline" width="800" class="center" style="margin-bottom: 10px;margin-top: 10px;"/></p>

## 🛠️ How to Run the Project

### 🔍 Machine Learning Part

1. **Install dependencies**
   ```bash
   pip install tensorflow keras
   ```

2. **Run the Jupyter notebooks**
   - Open `LightGBM.ipynb` to train the landslide detection model.
   - Use `Model_Comparison.ipynb` to compare different ML models.

3. **Required files**
   - Make sure all dataset files are in the appropriate `data/` folder.

### 🌐 IoT Part

1. **Hardware Setup**
   - ESP32 microcontroller
   - Sensors:
     - Soil Moisture Sensor (Analog)
     - Rainfall Sensor
     - ADXL345 Accelerometer (I2C)
     - NEO-6M GPS Module
   - Connect as per the provided circuit diagram.

2. **Software Setup**
   - Use the Arduino IDE.
   - Install the following libraries:
     - `Firebase_ESP_Client`
     - `TinyGPS++`
     - `Adafruit_Sensor`
     - `Adafruit_ADXL345_U`
   - Upload the `IoT code.txt` to your ESP32.

3. **Connectivity**
   - Set Wi-Fi credentials and Firebase configuration in the code.

---

## 📊 Results

### ✅ Evaluation Metrics


- <strong>SHAP Analysis</strong> & <strong>Confusion Matrix</strong><br/>
   <p align="center"><img src="images/matrix plot.jpg" alt="SHAP Summary Plot and Confusion Matrix" width="800" class="center" style="margin-bottom: 10px;"/></p>
- <strong>ROC Curve</strong> & <strong>Precision-Recall Curve</strong><br/>
   <p align="center"><img src="images/curve.jpg" alt="ROC and PR Curve" width="800" class="center" style="margin-bottom: 10px;"/></p>
- <strong>Evaluation Metrics</strong>
   
   | **Metric**  | **AUC** | **Precision** | **F1-Score** | **Recall** | **Specificity** |
   |-------------|---------|---------------|--------------|------------|-----------------|
   |   Training  | 0.9782  | 0.9637        | 0.8266       | 0.7238     | 0.9908          |
   |   Testing   | 0.9774  | 0.9590        | 0.8250       | 0.7200     | 0.9914          |

<p align="center"><img src="images/metrics_heatmap.png" alt="Heatmap of Metrics" width="800" class="center" style="margin-top: 10px;"/></p>

---

## ⚠️ Model Comparison

Various models were tested including:
- LightGBM
- Random Forest
- Support Vector Machines
- Logistic Regression

<p align="center"><img src="images/output.png" alt="Comparison of Models" width="800" class="center" style="margin-bottom: 10px;"/></p>

| Model                   | Accuracy | Recall  | F1-Score | Specificity |
|-------------------------|----------|---------|----------|-------------|
| LightGBM                | 83.99%   | 88.17%  | 84.64%   | 79.82%      |
| RandomForest            | 83.55%   | 87.78%  | 84.22%   | 79.31%      |
| Gradient Boosting       | 83.23%   | 87.53%  | 83.92%   | 78.92%      |
| SVM                     | 82.97%   | 89.72%  | 84.05%   | 76.22%      |
| MultiLayer Perceptron   | 82.52%   | 84.58%  | 82.87%   | 80.46%      |
| CNN                     | 82.13%   | 85.86%  | 82.78%   | 78.41%      |
| Logistic Classifier     | 80.78%   | 84.19%  | 81.42%   | 77.38%      |
| AdaBoost                | 79.43%   | 80.98%  | 79.75%   | 77.89%      |
| NaiveBayes Classifier   | 71.98%   | 67.87%  | 70.78%   | 76.09%      |


LightGBM outperformed other models in terms of both accuracy and generalization.

---

## 🔥 Heatmap and Hazard Point Generation

- Landslide risk zones are visualized on a **heatmap** using GPS-coordinates.
- Risk levels:
  - 🟢 Low
  - 🟡 Moderate
  - 🔴 High

<p align="center"><img src="images/landslide_risk.jpg" alt="Heatmap" width="800" class="center" style="margin-bottom: 10px;"/></p>

- For better visual understanding refer to [`Heatmap`](landslide_heatmap.html)
---

## 🔧 IoT System Details

### 🖼️ Circuit Diagram

<p align="center">
  <img src="images/circuit.png" alt="Circuit Diagram" width="50%" height="350px"/>
</p>


1. ESP32 Microcontroller  
2. Raindrop Sensor  
3. Soil Moisture Sensor 
4. 3-axis ADXL 345 Accelerometer Sensor 
5. Neo 6M GPS Module 

### 📏 Equations and Logic Used

1. **Moisture + Rainfall Risk Index**

   - `RiskIndex = (0.6 × Moisture%) + (0.4 × Rain%)`  
   > Alert if RiskIndex > 58%

3. **Rainfall + Vibration Multiplicative Risk**

   - `RiskMulti = (Rain% / 40) × (Vibration / 20)`  
   > Alert if RiskMulti > 0.35

4. **Triple Alert Criteria**

   - `Moisture% >= 50 AND Rain% >= 20 AND Vibration >= 12`

5. **Risk Level Calculation**

   - `Overall Risk (%) = 0.4 × MoistureRisk + 0.4 × RainRisk + 0.2 × VibrationRisk`

7. **Categorization**
   - `0–25`: LOW
   - `25–50`: MODERATE
   - `50–75`: HIGH
   - `75–100`: EXTREME

### ❄️ Setup

<p align="center"><img src="images/iot.png" alt="Final Setup" width="40%" height="380px" class="center" style="margin-bottom: 10px;"/></p>


### ☁️ Firebase Integration

- Realtime sensor updates under `/sensor_readings`
- Alerts pushed to `/alerts` node with cooldown mechanism

---

## 🔧 Android Application

- Real-time Alert and Informative Dashboard
- Interactive Maps
- Reporting incidents and Community Forum

<p align="center">
  <img src="images/dashboard.jpg" alt="Dashboard" width="250px" height="500px"/>
  <img src="images/alert.jpg" alt="Alert Modal" width="250px" height="500px"/>
  <img src="images/map.jpg" alt="Interactive Map" width="250px" height="500px"/>
  <img src="images/report.jpg" alt="Incident Report" width="250px" height="500px"/>
  <img src="images/forum.jpg" alt="Community Forum" width="250px" height="500px"/>
</p>

---

## 🪄 Android Application Setup Guide

This guide provides a step-by-step process to clone an Android application project from GitHub and run it in Android Studio.

### 📋 Prerequisites

Before you begin, ensure you have the following installed:

- [Android Studio](https://developer.android.com/studio) (latest version recommended)
- [Java Development Kit (JDK) 8 or higher](https://adoptopenjdk.net/)
- [Git](https://git-scm.com/)

---

### 🚀 Steps to Clone and Run the Android Project

#### 1. Clone the Repository

   1.1. Open your terminal/command prompt.
    
   1.2. Run the following command to clone the repository to your local machine:
    
   ```bash
   git clone https://github.com/an-gr-hh1/Advanced-Sensor-Based-Landslide-Detection-and-Alert-System-Utlizing-Machine-Learning.git
   ```
#### 2. Open the Project in Android Studio

#### 3. Sync Gradle

#### 4. Check the SDK Version and Dependencies

   4.1. Setup and add necessary API keys for Google Map and OpenWeatherMap
  
   4.2. Get google-services JSON file from firebase project and add it to the app folder
   ```bash
    ├── app/
    │   ├── google-services.json
   ```

#### 5. Connect a Device or Launch an Emulator

#### 6. Build and Run the Application

#### 7. Project Structure Overview
```bash
your-project/
├── app/                         # Main Android app module
│   ├── src/                     # Source code files
│   ├── res/                     # Resources (layouts, strings, images, etc.)
│   ├── build.gradle             # App-level build configuration
├── build.gradle                 # Top-level build file for all modules
├── settings.gradle              # Gradle settings file
├── proguard-rules.pro           # ProGuard configuration (if applicable)
```

### 📁 Important Files

Here are some key configuration files in the project that you might want to check:

- [`build.gradle`](build.gradle): Top-level Gradle build script for project-wide settings and dependency repositories.
- [`app/build.gradle`](app/build.gradle): Module-level Gradle script where dependencies and Android-specific configurations are declared.
- [`gradle/libs.versions.toml`](gradle/libs.versions.toml): Centralized version catalog used for managing plugin and library versions (if the project uses version catalogs).

These files help define how the project is built and what dependencies are used.
---

## 📌 Project Highlights

- 🔁 Real-time sensor data + prediction-based alert system
- 📍 GPS-linked location tagging for hazardous zones
- 📉 Machine learning improves prediction beyond threshold-only logic
- 🛰️ Combines physical sensing with cloud-based data logging

---

## 📬 Contact

For any queries, reach out at [your-email@example.com].
