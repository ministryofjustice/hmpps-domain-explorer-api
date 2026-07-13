-- Data Explorer Application Database Schema

-- Drop existing tables (be careful in production!)
DROP TABLE IF EXISTS use_case_data_domain CASCADE;
DROP TABLE IF EXISTS use_case_service_area CASCADE;
DROP TABLE IF EXISTS use_case_digital_service CASCADE;
DROP TABLE IF EXISTS business_process_service_area CASCADE;
DROP TABLE IF EXISTS business_process_digital_service CASCADE;
DROP TABLE IF EXISTS use_case CASCADE;
DROP TABLE IF EXISTS business_process CASCADE;
DROP TABLE IF EXISTS digital_service CASCADE;
DROP TABLE IF EXISTS service_area CASCADE;
DROP TABLE IF EXISTS data_domain CASCADE;

-- Core Entities

CREATE TABLE data_domain (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    attributes JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_data_domain_name ON data_domain(name);

CREATE TABLE service_area (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_service_area_name ON service_area(name);

CREATE TABLE digital_service (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_digital_service_name ON digital_service(name);

CREATE TABLE business_process (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    service_area_id INT REFERENCES service_area(id) ON DELETE SET NULL,
    mapping_justification TEXT,
    attributes JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_business_process_name ON business_process(name);
CREATE INDEX idx_business_process_service_area_id ON business_process(service_area_id);

CREATE TABLE use_case (
    id SERIAL PRIMARY KEY,
    uc_id VARCHAR(100) NOT NULL UNIQUE,
    title VARCHAR(255),
    description TEXT,
    mapping_justification TEXT,
    attributes JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_use_case_uc_id ON use_case(uc_id);
CREATE INDEX idx_use_case_title ON use_case(title);

-- Many-to-Many Relationship Tables

CREATE TABLE business_process_digital_service (
    id SERIAL PRIMARY KEY,
    business_process_id INT NOT NULL REFERENCES business_process(id) ON DELETE CASCADE,
    digital_service_id INT NOT NULL REFERENCES digital_service(id) ON DELETE CASCADE,
    mapping_justification TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_bp_ds UNIQUE(business_process_id, digital_service_id)
);

CREATE INDEX idx_bpds_business_process_id ON business_process_digital_service(business_process_id);
CREATE INDEX idx_bpds_digital_service_id ON business_process_digital_service(digital_service_id);

CREATE TABLE use_case_digital_service (
    id SERIAL PRIMARY KEY,
    use_case_id INT NOT NULL REFERENCES use_case(id) ON DELETE CASCADE,
    digital_service_id INT NOT NULL REFERENCES digital_service(id) ON DELETE CASCADE,
    mapping_justification TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_ucds UNIQUE(use_case_id, digital_service_id)
);

CREATE INDEX idx_ucds_use_case_id ON use_case_digital_service(use_case_id);
CREATE INDEX idx_ucds_digital_service_id ON use_case_digital_service(digital_service_id);

CREATE TABLE business_process_service_area (
    id SERIAL PRIMARY KEY,
    business_process_id INT NOT NULL REFERENCES business_process(id) ON DELETE CASCADE,
    service_area_id INT NOT NULL REFERENCES service_area(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_bpsa UNIQUE(business_process_id, service_area_id)
);

CREATE INDEX idx_bpsa_business_process_id ON business_process_service_area(business_process_id);
CREATE INDEX idx_bpsa_service_area_id ON business_process_service_area(service_area_id);

CREATE TABLE use_case_service_area (
    id SERIAL PRIMARY KEY,
    use_case_id INT NOT NULL REFERENCES use_case(id) ON DELETE CASCADE,
    service_area_id INT NOT NULL REFERENCES service_area(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_ucsa UNIQUE(use_case_id, service_area_id)
);

CREATE INDEX idx_ucsa_use_case_id ON use_case_service_area(use_case_id);
CREATE INDEX idx_ucsa_service_area_id ON use_case_service_area(service_area_id);

CREATE TABLE use_case_data_domain (
    id SERIAL PRIMARY KEY,
    use_case_id INT NOT NULL REFERENCES use_case(id) ON DELETE CASCADE,
    data_domain_id INT NOT NULL REFERENCES data_domain(id) ON DELETE CASCADE,
    mapping_justification TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_ucdd UNIQUE(use_case_id, data_domain_id)
);

CREATE INDEX idx_ucdd_use_case_id ON use_case_data_domain(use_case_id);
CREATE INDEX idx_ucdd_data_domain_id ON use_case_data_domain(data_domain_id);

