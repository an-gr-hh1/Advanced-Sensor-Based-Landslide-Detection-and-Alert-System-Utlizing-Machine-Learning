# Advanced-Sensor-Based-Landslide-Detection-and-Alert-System-Utlizing-Machine-Learning
 In this proposed system, we present a precise and timely warning system to reduce the effects of landslides. This system strategically partitions its ML and IoT components, improving both risk prediction and simultaneous supervision. The predictive model LightGBM, is trained with an extensive dataset obtained from DEM, which enabled mapping out high-risk areas with great accuracy. The system utilizes IoT nodes dedicated entirely to areas of high risk pinpointed by the ML model, consisting of a rain sensor, a soil moisture sensor, an ADXL 345 sensor, and a GPS module that sends real-time updates to a centralized cloud framework. All real-time sensed data are processed and demonstrated on an android dashboard and features an interactive map that shows hazard zones, allowing swift action and emergency response. It has been programmed to trigger an alert when abnormalities are detected. The combination of machine learning and the IoT allows for increased accuracy of prediction and responsiveness, making it a strong and scalable solution to protect people in regions prone to landslides. The proposed system significantlyimproves the efficiency of technology in disaster management.

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

<ul>
  <li>
    <strong>SHAP Analysis</strong>: Shows feature contribution for better explainability.<br/>
    <img src="images/shap.png" alt="SHAP Summary Plot" width="45%"/>
  </li>
  <li>
    <strong>Confusion Matrix</strong>: Highlights class prediction distribution.<br/>
    <img src="images/confusion.png" alt="Confusion Matrix" width="45%"/>
  </li>
  <li>
    <strong>ROC Curve</strong>: Compares true positive rate vs. false positive rate.<br/>
    <img src="images/roc.png" alt="ROC Curve" width="45%"/>
  </li>
  <li>
    <strong>Precision-Recall Curve</strong>: Compares precision vs. recall rate.<br/>
    <img src="images/pr.png" alt="Precision-Recall Curve" width="45%"/>
  </li>
</ul>


---

## ⚠️ Model Comparison

Various models were tested including:
- LightGBM
- Random Forest
- Support Vector Machines
- Logistic Regression

<img src="images/output.png" alt="Comparison of Models" width="45%" style="margin-bottom: 10px;"/>

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

<img src="images/Heatmap_satellite.jpg" alt="Heatmap" width="45%" style="margin-right: 10px;"/>

---

## 🔧 IoT System Details

### 🖼️ Circuit Diagram and Setup

<p align="center">
  <img src="images/circuit.png" alt="Circuit Diagram" width="50%" height="350px"/>
  <img src="images/iot.png" alt="Final Setup" width="40%" height="350px" style="margin-right: 10px;"/>
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

### ☁️ Firebase Integration

- Realtime sensor updates under `/sensor_readings`
- Alerts pushed to `/alerts` node with cooldown mechanism

---

## 🔧 Android Application

- Real-time Alert and Informative Dashboard
- Interactive Maps
- Reporting incidents and Community Forum

<p align="center">
  <img src="images/dashboard.jpg" alt="Dashboard" width="250px" height="450px"/>
  <img src="images/alert.jpg" alt="Alert Modal" width="250px" height="450px"/>
  <img src="images/map.jpg" alt="Interactive Map" width="250px" height="450px"/>
  <img src="images/forum.jpg" alt="Community Forum" width="250px" height="450px"/>
</p>

---

## 📌 Project Highlights

- 🔁 Real-time sensor data + prediction-based alert system
- 📍 GPS-linked location tagging for hazardous zones
- 📉 Machine learning improves prediction beyond threshold-only logic
- 🛰️ Combines physical sensing with cloud-based data logging

---

## 📬 Contact

For any queries, reach out at [your-email@example.com].
