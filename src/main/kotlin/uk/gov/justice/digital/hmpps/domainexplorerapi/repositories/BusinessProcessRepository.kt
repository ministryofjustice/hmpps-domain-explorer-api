package uk.gov.justice.digital.hmpps.domainexplorerapi.repositories

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.domainexplorerapi.domain.BusinessProcess
import java.util.Optional

@Repository
interface BusinessProcessRepository : JpaRepository<BusinessProcess, Int> {
  @EntityGraph(attributePaths = ["serviceArea"])
  override fun findAll(): List<BusinessProcess>

  @EntityGraph(attributePaths = ["serviceArea"])
  override fun findById(id: Int): Optional<BusinessProcess>

  fun findByNameIgnoreCase(name: String): BusinessProcess?

  @Query("SELECT b FROM BusinessProcess b LEFT JOIN FETCH b.serviceArea WHERE LOWER(b.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
  fun search(searchTerm: String): List<BusinessProcess>

  fun findByNameIgnoreCaseContaining(name: String): List<BusinessProcess>

  fun findByServiceAreaId(serviceAreaId: Int): List<BusinessProcess>
}
