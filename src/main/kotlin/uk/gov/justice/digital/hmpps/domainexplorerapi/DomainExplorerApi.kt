package uk.gov.justice.digital.hmpps.domainexplorerapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DomainExplorerApi

fun main(args: Array<String>) {
  runApplication<DomainExplorerApi>(*args)
}
