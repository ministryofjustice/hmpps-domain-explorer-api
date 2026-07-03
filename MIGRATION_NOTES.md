# Backend Code Migration

This backend template has been integrated with the Domain Explorer application code.

## What Changed

1. **Kotlin Source Code**: All backend application code from `probationMap/backend` has been copied to the template's package structure:
   - Original: `uk.gov.moj.hmpps.datamap`
   - New: `uk.gov.justice.digital.hmpps.domainexplorerapi`

2. **Database Resources**: The following files have been added:
   - `src/main/resources/schema.sql` - Database schema
   - `src/main/resources/truncate.sql` - Database truncation script
   - `src/main/resources/application.properties` - Application configuration

3. **Application Class**: The main application class has been integrated into `Application.kt` with the class name `DomainExplorerApi`

## Building

To build the backend:

```bash
cd hmpps-domain-explorer-api
./gradlew build
```

## Running

To run locally:

```bash
./gradlew bootRun
```

To run with a specific profile (e.g., standalone):

```bash
./gradlew bootRun --args='--spring.profiles.active=standalone'
```

## Database Setup

The application expects a PostgreSQL database. Configuration is in `application.properties`.

To truncate all tables (Maven goal available):

```bash
# This assumes Maven integration; you may need to use SQL directly or access via the API
```

## Docker

To build and run with Docker:

```bash
docker build -t hmpps-domain-explorer-api .
docker run -p 8080:8080 -e DATABASE_URL=postgres://... hmpps-domain-explorer-api
```

See `docker-compose.yml` for a complete setup example.

