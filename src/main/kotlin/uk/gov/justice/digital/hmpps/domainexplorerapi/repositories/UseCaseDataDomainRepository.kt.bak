package uk.gov.justice.digital.hmpps.domainexplorerapi.repositories

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.domainexplorerapi.domain.UseCaseDataDomain

@Repository
interface UseCaseDataDomainRepository : JpaRepository<UseCaseDataDomain, Int> {
  @EntityGraph(attributePaths = ["dataDomain"])
  fun findByUseCaseId(useCaseid: Int): List<UseCaseDataDomain>
  fun findByDataDomainId(dataDomainid: Int): List<UseCaseDataDomain>
  fun findByUseCaseIdAndDataDomainId(useCaseid: Int, dataDomainid: Int): UseCaseDataDomain?
}
