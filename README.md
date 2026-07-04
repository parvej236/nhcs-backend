# NHCS Backend — National Health Commission System API

A robust Spring Boot REST API serving as the backend core of the **National Health Commission System (NHCS)** for Bangladesh. This system provides secure role-based portals (Patient, Doctor, Hospital), real-time AI risk analysis, clinical records, doctor recommendations, and a donor matchmaking service.

---

## 🌟 Key Features

* **🛡️ Secure JWT Authentication:** Role-based access control supporting `PATIENT`, `DOCTOR`, and `HOSPITAL` roles.
* **🩸 AI Blood Matcher Engine:** Computes compatible blood donors in real-time based on blood groups, geography, and active donation status.
* **🩺 Vitals Risk Analyzer:** Analyzes patient symptoms and vitals (systolic/diastolic BP, heart rate, blood glucose) to categorize risk.
* **👨‍⚕️ Smart Doctor Recommendation:** Matches patient symptom reports to the most relevant doctor specializations using advanced database filters.
* **🗃️ Judge Check Data Seeding:** Automatic seeding of unified mock patient data, prescriptions, lab results, imaging scans, and appointments linked to default login profiles.

---

## 🛠️ Tech Stack

* **Language:** Java 21
* **Framework:** Spring Boot 3.x, Spring Data JPA, Spring Security
* **Build Tool:** Maven (Wrapper included)
* **Libraries:** Lombok, Jackson

---

## 🧪 Seeding & Default Credentials (Judge Check)

The backend auto-seeds the database with default profiles for evaluation purposes:

| Username | Password | Role | Description / Seeding Context |
| :--- | :--- | :--- | :--- |
| `patient_judge` | `password123` | Patient | Laila Khan (Dhanmondi, O+, seeds 2 appointments, labs, ECG, and O+ donor status) |
| `doctor_judge` | `password123` | Doctor | Dr. Rahim Chowdhury (Cardiology, Dhaka Medical College Hospital) |
| `hospital_judge` | `password123` | Hospital | Dhaka Medical College Hospital portal login |

---

## 🚀 Execution Instructions

### Prerequisites
* JDK 21
* Maven 3.8+ (optional, wrapper is included)

### Compile and Start
Run the following commands in the `nhcs-backend` root folder:

```bash
# Clean and compile the Java project
./mvnw clean compile

# Start the Spring Boot Application
./mvnw spring-boot:run
```

The application starts by default at `http://localhost:8080`.
