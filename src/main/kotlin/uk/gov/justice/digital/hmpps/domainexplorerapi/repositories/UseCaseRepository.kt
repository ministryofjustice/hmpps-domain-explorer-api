package uk.gov.justice.digital.hmpps.domainexplorerapi.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.domainexplorerapi.domain.UseCase

@Repository
interface UseCaseRepository : JpaRepository<UseCase, Int> {
  fun findByUcIdIgnoreCase(ucId: String): UseCase?

  @Query("SELECT u FROM UseCase u WHERE LOWER(u.ucId) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
  fun search(searchTerm: String): List<UseCase>

  fun findByTitleIgnoreCaseContaining(title: String): List<UseCase>
}
