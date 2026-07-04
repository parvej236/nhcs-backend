# NHCS Backend — National Health Commission System API

A robust Spring Boot REST API serving as the backend core of the **National Health Commission System (NHCS)** for Bangladesh. This system provides secure role-based portals (Patient, Doctor, Hospital), real-time AI risk analysis, clinical records, doctor recommendations, and a donor matchmaking service.

---

## 🗺️ How the System Serves Citizens (End-to-End Process Flow)

The system operates as a unified, connected healthcare network designed to serve citizens, clinics, and medical staff efficiently:

### 1. Public Health Entry & AI Symptom Analysis
* **Bangla AI Symptom Analyzer:** Citizens describe their health condition in Bangla (e.g., chest tightness, elevated pulse). The system analyzes the text along with vitals (Blood Pressure, Blood Glucose, Heart Rate) to evaluate risk.
* **Smart Specialty Suggestions:** The system recommends a clinical category (e.g., Cardiology, Endocrinology) and dynamically matches the best specialists from the active database.

### 2. Connected Appointment Booking & Medical Vault
* **Doctor Booking:** Registered patients book appointments.
* **Unified History Logging:** Successful clinical consultations feed directly into the patient's secure **Medical Vault**, creating integrated prescriptions (medicine lists, follow-up dates), lab reports (categorized biochem indicators like Troponin-I), and imaging reports (cardiac scans).

### 3. Public Emergency Blood Requests
* **No Sign-In Required:** Anyone can submit emergency blood requests from the dashboard, detailing patient names, locations, urgency, and past disease histories.
* **Hospital Matching CommandCenter:** Hospitals see requests on their dashboard as `Pending`.

### 4. AI Donor Matching & Fulfillment
* **Location & Compatibility Check:** When the hospital runs the **AI Donor Matcher**, the algorithm queries the active blood registry. It ranks donors based on:
  * Blood group compatibility (e.g. O+ compatible donors).
  * Geodistances to the hospital facility.
  * Donation eligibility status (last donation date > 3 months).
* **Fulfillment:** Hospitals send notifications directly to selected donors, exposing contact numbers for immediate coordination.

---

## 🛠️ Technology Stack

* **Language:** Java 21
* **Framework:** Spring Boot 3.x, Spring Data JPA, Spring Security (JWT-based role protection)
* **Build Tool:** Maven (Wrapper included)
* **Database:** PostgreSQL / H2 database
* **Libraries:** Lombok, Jackson

---

## 🔮 Future Enhancements

* **📍 Live Geolocation Routing:** Integrate real-time GIS mapping (e.g., Google Maps APIs) to calculate actual live road distances rather than coordinate approximation.
* **📊 Big Data Disease Surveillance:** Implement regional health diagnostic heatmaps to help the government predict outbreak hotspots (e.g., Dengue or Hypertension clusters) based on anonymous vitals analyzer queries.
* **📱 SMS/Push Donor Alerts:** Automate SMS alerts to compatible blood donors via telco API gateways immediately when an emergency request is placed.
