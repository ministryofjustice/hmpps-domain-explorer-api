-- Truncate all tables and reset sequences.
-- Junction/child tables are listed first to satisfy FK constraints,
-- but RESTART IDENTITY CASCADE handles everything safely.
TRUNCATE TABLE
    use_case_data_domain,
    use_case_service_area,
    use_case_digital_service,
    business_process_service_area,
    business_process_digital_service,
    use_case,
    business_process,
    digital_service,
    service_area,
    data_domain
RESTART IDENTITY CASCADE;

