package uk.gov.justice.digital.hmpps.domainexplorerapi.integration

import org.testcontainers.containers.PostgreSQLContainer

object PostgresContainer {
  val instance =
    PostgreSQLContainer("postgres:17").apply {
      start()
    }
}
