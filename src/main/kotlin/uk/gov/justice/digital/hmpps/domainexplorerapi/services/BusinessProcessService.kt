package uk.gov.justice.digital.hmpps.domainexplorerapi.services

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domainexplorerapi.domain.BusinessProcess
import uk.gov.justice.digital.hmpps.domainexplorerapi.repositories.BusinessProcessRepository
import java.time.LocalDateTime

@Service
class BusinessProcessService(private val repository: BusinessProcessRepository) {
  private val logger = LoggerFactory.getLogger(javaClass)

  fun findAll(): List<BusinessProcess> {
    logger.debug("Fetching all business processes")
    return repository.findAll()
  }

  fun findById(id: Int): BusinessProcess? {
    logger.debug("Fetching business process with id: $id")
    return repository.findById(id).orElse(null)
  }

  fun findByName(name: String): BusinessProcess? {
    logger.debug("Fetching business process with name: $name")
    return repository.findByNameIgnoreCase(name)
  }

  fun search(searchTerm: String): List<BusinessProcess> {
    logger.debug("Searching business processes with term: $searchTerm")
    return if (searchTerm.isBlank()) {
      findAll()
    } else {
      repository.search(searchTerm)
    }
  }

  fun findByServiceAreaId(serviceAreaId: Int): List<BusinessProcess> {
    logger.debug("Fetching business processes for service area: $serviceAreaId")
    return repository.findByServiceAreaId(serviceAreaId)
  }

  fun create(businessProcess: BusinessProcess): BusinessProcess {
    logger.info("Creating new business process: ${businessProcess.name}")
    val existing = repository.findByNameIgnoreCase(businessProcess.name)
    if (existing != null) {
      throw IllegalArgumentException("Business process with name '${businessProcess.name}' already exists")
    }
    return repository.save(businessProcess)
  }

  fun upsert(businessProcess: BusinessProcess): BusinessProcess {
    logger.info("Upserting business process: ${businessProcess.name}")
    val existing = repository.findByNameIgnoreCase(businessProcess.name)
    return if (existing != null) {
      logger.info("Updating existing business process: ${businessProcess.name}")
      val updated = existing.copy(
        serviceArea = businessProcess.serviceArea,
        mappingJustification = businessProcess.mappingJustification,
        attributes = businessProcess.attributes,
        updatedAt = LocalDateTime.now(),
      )
      repository.save(updated)
    } else {
      logger.info("Creating new business process: ${businessProcess.name}")
      repository.save(businessProcess)
    }
  }

  fun update(id: Int, businessProcess: BusinessProcess): BusinessProcess {
    logger.info("Updating business process with id: $id")
    val existing = repository.findById(id).orElseThrow { IllegalArgumentException("Business process not found") }

    val updated = existing.copy(
      name = businessProcess.name,
      serviceArea = businessProcess.serviceArea,
      mappingJustification = businessProcess.mappingJustification,
      attributes = businessProcess.attributes,
      updatedAt = LocalDateTime.now(),
    )
    return repository.save(updated)
  }

  fun delete(id: Int) {
    logger.info("Deleting business process with id: $id")
    repository.deleteById(id)
  }
}
