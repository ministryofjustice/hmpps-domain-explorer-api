package uk.gov.justice.digital.hmpps.domainexplorerapi.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.domainexplorerapi.domain.DigitalService

@Repository
interface DigitalServiceRepository : JpaRepository<DigitalService, Int> {
  fun findByNameIgnoreCase(name: String): DigitalService?
  fun findByNameIgnoreCaseContaining(name: String): List<DigitalService>
}
