package uk.gov.justice.digital.hmpps.domainexplorerapi.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.domainexplorerapi.domain.BusinessProcessDigitalService

@Repository
interface BusinessProcessDigitalServiceRepository : JpaRepository<BusinessProcessDigitalService, Int> {
  fun findByBusinessProcessId(businessProcessid: Int): List<BusinessProcessDigitalService>
  fun findByDigitalServiceId(digitalServiceid: Int): List<BusinessProcessDigitalService>
  fun findByBusinessProcessIdAndDigitalServiceId(businessProcessid: Int, digitalServiceid: Int): BusinessProcessDigitalService?
}
