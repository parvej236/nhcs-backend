CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE doctors (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    full_name VARCHAR(255),
    specialization VARCHAR(255),
    license_number VARCHAR(255),
    contact_number VARCHAR(255),
    hospital_affiliation VARCHAR(255),
    rating NUMERIC(3, 2) DEFAULT 5.0,
    experience_years INTEGER DEFAULT 0,
    consultation_fee INTEGER DEFAULT 0,
    image_url VARCHAR(500),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE hospitals (
    id BIGSERIAL PRIMARY KEY,
    facility_id VARCHAR(255) UNIQUE,
    name VARCHAR(255),
    division VARCHAR(255),
    classification VARCHAR(255),
    total_beds INTEGER,
    occupied_beds INTEGER,
    compliance_score INTEGER,
    status VARCHAR(255)
);

CREATE TABLE patients (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    full_name VARCHAR(255),
    date_of_birth DATE,
    gender VARCHAR(255),
    blood_group VARCHAR(255),
    contact_number VARCHAR(255),
    address VARCHAR(255),
    occupation VARCHAR(255),
    marital_status VARCHAR(255),
    present_address VARCHAR(255),
    permanent_address VARCHAR(255),
    emergency_contact_name VARCHAR(255),
    emergency_contact_relation VARCHAR(255),
    emergency_contact_phone VARCHAR(255),
    national_id VARCHAR(255),
    bp_systolic VARCHAR(50),
    bp_diastolic VARCHAR(50),
    blood_glucose VARCHAR(50),
    heart_rate VARCHAR(50),
    weight VARCHAR(50),
    vitals_last_updated TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE role_applications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    requested_role VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    application_date TIMESTAMP NOT NULL,
    notes VARCHAR(1000),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE patient_allergies (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    allergen VARCHAR(255) NOT NULL,
    severity VARCHAR(50) NOT NULL,
    reaction VARCHAR(255),
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);

CREATE TABLE patient_chronic_diseases (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    disease_name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    diagnosed_date DATE,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);

CREATE TABLE appointments (
    id VARCHAR(50) PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    date DATE NOT NULL,
    time_slot VARCHAR(50) NOT NULL,
    queue_number VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    hospital_name VARCHAR(255) NOT NULL,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE
);

CREATE TABLE prescriptions (
    id VARCHAR(50) PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    date TIMESTAMP NOT NULL,
    doctor_name VARCHAR(255) NOT NULL,
    doctor_specialization VARCHAR(255),
    hospital_name VARCHAR(255),
    diagnosis VARCHAR(1000),
    clinical_notes VARCHAR(2000),
    follow_up_date VARCHAR(50),
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);

CREATE TABLE prescription_medicines (
    id BIGSERIAL PRIMARY KEY,
    prescription_id VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    dosage VARCHAR(255),
    instruction VARCHAR(1000),
    duration VARCHAR(255),
    FOREIGN KEY (prescription_id) REFERENCES prescriptions(id) ON DELETE CASCADE
);

CREATE TABLE lab_reports (
    id VARCHAR(50) PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    test_name VARCHAR(255) NOT NULL,
    category VARCHAR(255),
    date TIMESTAMP NOT NULL,
    hospital_name VARCHAR(255),
    doctor_name VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    ai_interpretation VARCHAR(2000),
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);

CREATE TABLE lab_test_results (
    id BIGSERIAL PRIMARY KEY,
    lab_report_id VARCHAR(50) NOT NULL,
    parameter VARCHAR(255) NOT NULL,
    value VARCHAR(255) NOT NULL,
    unit VARCHAR(50),
    reference_range VARCHAR(100),
    status VARCHAR(50),
    FOREIGN KEY (lab_report_id) REFERENCES lab_reports(id) ON DELETE CASCADE
);

CREATE TABLE imaging_reports (
    id VARCHAR(50) PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    type VARCHAR(100) NOT NULL,
    body_part VARCHAR(255) NOT NULL,
    date TIMESTAMP NOT NULL,
    hospital_name VARCHAR(255),
    doctor_name VARCHAR(255),
    image_url VARCHAR(500),
    findings VARCHAR(2000),
    impression VARCHAR(2000),
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);

