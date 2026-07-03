package uk.gov.justice.digital.hmpps.domainexplorerapi.repositories

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.domainexplorerapi.domain.UseCaseDigitalService

@Repository
interface UseCaseDigitalServiceRepository : JpaRepository<UseCaseDigitalService, Int> {
  @EntityGraph(attributePaths = ["digitalService"])
  fun findByUseCaseId(useCaseid: Int): List<UseCaseDigitalService>
  fun findByDigitalServiceId(digitalServiceid: Int): List<UseCaseDigitalService>
  fun findByUseCaseIdAndDigitalServiceId(useCaseid: Int, digitalServiceid: Int): UseCaseDigitalService?
}
