package uk.gov.justice.digital.hmpps.domainexplorerapi.services

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domainexplorerapi.domain.DigitalService
import uk.gov.justice.digital.hmpps.domainexplorerapi.repositories.DigitalServiceRepository
import java.time.LocalDateTime

@Service
class DigitalServiceService(private val repository: DigitalServiceRepository) {
  private val logger = LoggerFactory.getLogger(javaClass)

  fun findAll(): List<DigitalService> {
    logger.debug("Fetching all digital services")
    return repository.findAll()
  }

  fun findById(id: Int): DigitalService? {
    logger.debug("Fetching digital service with id: $id")
    return repository.findById(id).orElse(null)
  }

  fun findByName(name: String): DigitalService? {
    logger.debug("Fetching digital service with name: $name")
    return repository.findByNameIgnoreCase(name)
  }

  fun create(digitalService: DigitalService): DigitalService {
    logger.info("Creating new digital service: ${digitalService.name}")
    val existing = repository.findByNameIgnoreCase(digitalService.name)
    if (existing != null) {
      throw IllegalArgumentException("Digital service with name '${digitalService.name}' already exists")
    }
    return repository.save(digitalService)
  }

  fun upsert(digitalService: DigitalService): DigitalService {
    logger.info("Upserting digital service: ${digitalService.name}")
    val existing = repository.findByNameIgnoreCase(digitalService.name)
    return if (existing != null) {
      logger.info("Digital service already exists: ${digitalService.name}")
      existing // Digital services don't have additional fields to update
    } else {
      logger.info("Creating new digital service: ${digitalService.name}")
      repository.save(digitalService)
    }
  }

  fun update(id: Int, digitalService: DigitalService): DigitalService {
    logger.info("Updating digital service with id: $id")
    val existing = repository.findById(id).orElseThrow { IllegalArgumentException("Digital service not found") }

    val updated = existing.copy(
      name = digitalService.name,
      updatedAt = LocalDateTime.now(),
    )
    return repository.save(updated)
  }

  fun delete(id: Int) {
    logger.info("Deleting digital service with id: $id")
    repository.deleteById(id)
  }
}
