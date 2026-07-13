package uk.gov.justice.digital.hmpps.domainexplorerapi.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.domainexplorerapi.domain.BusinessProcessServiceArea

@Repository
interface BusinessProcessServiceAreaRepository : JpaRepository<BusinessProcessServiceArea, Int> {
  fun findByBusinessProcessId(businessProcessid: Int): List<BusinessProcessServiceArea>
  fun findByServiceAreaId(serviceAreaid: Int): List<BusinessProcessServiceArea>
  fun findByBusinessProcessIdAndServiceAreaId(businessProcessid: Int, serviceAreaid: Int): BusinessProcessServiceArea?
}
