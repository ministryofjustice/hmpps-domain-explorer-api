package uk.gov.justice.digital.hmpps.domainexplorerapi.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.domainexplorerapi.domain.ServiceArea

@Repository
interface ServiceAreaRepository : JpaRepository<ServiceArea, Int> {
  fun findByNameIgnoreCase(name: String): ServiceArea?
  fun findByNameIgnoreCaseContaining(name: String): List<ServiceArea>
}
