package uk.gov.justice.digital.hmpps.domainexplorerapi.services

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domainexplorerapi.domain.UseCase
import uk.gov.justice.digital.hmpps.domainexplorerapi.repositories.UseCaseRepository
import java.time.LocalDateTime

@Service
class UseCaseService(private val repository: UseCaseRepository) {
  private val logger = LoggerFactory.getLogger(javaClass)

  fun findAll(): List<UseCase> {
    logger.debug("Fetching all use cases")
    return repository.findAll()
  }

  fun findById(id: Int): UseCase? {
    logger.debug("Fetching use case with id: $id")
    return repository.findById(id).orElse(null)
  }

  fun findByUcId(ucId: String): UseCase? {
    logger.debug("Fetching use case with ucId: $ucId")
    return repository.findByUcIdIgnoreCase(ucId)
  }

  fun search(searchTerm: String): List<UseCase> {
    logger.debug("Searching use cases with term: $searchTerm")
    return if (searchTerm.isBlank()) {
      findAll()
    } else {
      repository.search(searchTerm)
    }
  }

  fun create(useCase: UseCase): UseCase {
    logger.info("Creating new use case: ${useCase.ucId}")
    val existing = repository.findByUcIdIgnoreCase(useCase.ucId)
    if (existing != null) {
      throw IllegalArgumentException("Use case with ucId '${useCase.ucId}' already exists")
    }
    return repository.save(useCase)
  }

  fun upsert(useCase: UseCase): UseCase {
    logger.info("Upserting use case: ${useCase.ucId}")
    val existing = repository.findByUcIdIgnoreCase(useCase.ucId)
    return if (existing != null) {
      logger.info("Updating existing use case: ${useCase.ucId}")
      val updated = existing.copy(
        title = useCase.title,
        description = useCase.description,
        mappingJustification = useCase.mappingJustification,
        attributes = useCase.attributes,
        updatedAt = LocalDateTime.now(),
      )
      repository.save(updated)
    } else {
      logger.info("Creating new use case: ${useCase.ucId}")
      repository.save(useCase)
    }
  }

  fun update(id: Int, useCase: UseCase): UseCase {
    logger.info("Updating use case with id: $id")
    val existing = repository.findById(id).orElseThrow { IllegalArgumentException("Use case not found") }

    val updated = existing.copy(
      ucId = useCase.ucId,
      title = useCase.title,
      description = useCase.description,
      mappingJustification = useCase.mappingJustification,
      attributes = useCase.attributes,
      updatedAt = LocalDateTime.now(),
    )
    return repository.save(updated)
  }

  fun delete(id: Int) {
    logger.info("Deleting use case with id: $id")
    repository.deleteById(id)
  }
}
