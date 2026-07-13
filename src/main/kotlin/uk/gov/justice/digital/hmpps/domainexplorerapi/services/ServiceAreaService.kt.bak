package uk.gov.justice.digital.hmpps.domainexplorerapi.services

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domainexplorerapi.domain.ServiceArea
import uk.gov.justice.digital.hmpps.domainexplorerapi.repositories.ServiceAreaRepository
import java.time.LocalDateTime

@Service
class ServiceAreaService(private val repository: ServiceAreaRepository) {
  private val logger = LoggerFactory.getLogger(javaClass)

  fun findAll(): List<ServiceArea> {
    logger.debug("Fetching all service areas")
    return repository.findAll()
  }

  fun findById(id: Int): ServiceArea? {
    logger.debug("Fetching service area with id: $id")
    return repository.findById(id).orElse(null)
  }

  fun findByName(name: String): ServiceArea? {
    logger.debug("Fetching service area with name: $name")
    return repository.findByNameIgnoreCase(name)
  }

  fun create(serviceArea: ServiceArea): ServiceArea {
    logger.info("Creating new service area: ${serviceArea.name}")
    val existing = repository.findByNameIgnoreCase(serviceArea.name)
    if (existing != null) {
      throw IllegalArgumentException("Service area with name '${serviceArea.name}' already exists")
    }
    return repository.save(serviceArea)
  }

  fun upsert(serviceArea: ServiceArea): ServiceArea {
    logger.info("Upserting service area: ${serviceArea.name}")
    val existing = repository.findByNameIgnoreCase(serviceArea.name)
    return if (existing != null) {
      logger.info("Service area already exists: ${serviceArea.name}")
      existing // Service areas don't have additional fields to update
    } else {
      logger.info("Creating new service area: ${serviceArea.name}")
      repository.save(serviceArea)
    }
  }

  fun update(id: Int, serviceArea: ServiceArea): ServiceArea {
    logger.info("Updating service area with id: $id")
    val existing = repository.findById(id).orElseThrow { IllegalArgumentException("Service area not found") }

    val updated = existing.copy(
      name = serviceArea.name,
      updatedAt = LocalDateTime.now(),
    )
    return repository.save(updated)
  }

  fun delete(id: Int) {
    logger.info("Deleting service area with id: $id")
    repository.deleteById(id)
  }
}
