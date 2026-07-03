package uk.gov.justice.digital.hmpps.domainexplorerapi.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.domainexplorerapi.domain.DataDomain

@Repository
interface DataDomainRepository : JpaRepository<DataDomain, Int> {
  fun findByNameIgnoreCase(name: String): DataDomain?

  @Query("SELECT d FROM DataDomain d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(d.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
  fun searchByNameOrDescription(searchTerm: String): List<DataDomain>

  fun findByNameIgnoreCaseContaining(name: String): List<DataDomain>
}
