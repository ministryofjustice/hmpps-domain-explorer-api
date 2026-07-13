package uk.gov.justice.digital.hmpps.domainexplorerapi.repositories

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.domainexplorerapi.domain.UseCaseServiceArea

@Repository
interface UseCaseServiceAreaRepository : JpaRepository<UseCaseServiceArea, Int> {
  @EntityGraph(attributePaths = ["serviceArea"])
  fun findByUseCaseId(useCaseid: Int): List<UseCaseServiceArea>
  fun findByServiceAreaId(serviceAreaid: Int): List<UseCaseServiceArea>
  fun findByUseCaseIdAndServiceAreaId(useCaseid: Int, serviceAreaid: Int): UseCaseServiceArea?
}
