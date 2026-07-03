package uk.gov.justice.digital.hmpps.domainexplorerapi.services

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domainexplorerapi.domain.DataDomain
import uk.gov.justice.digital.hmpps.domainexplorerapi.repositories.DataDomainRepository
import java.time.LocalDateTime

@Service
class DataDomainService(private val repository: DataDomainRepository) {
  private val logger = LoggerFactory.getLogger(javaClass)

  fun findAll(): List<DataDomain> {
    logger.debug("Fetching all data domains")
    return repository.findAll()
  }

  fun findById(id: Int): DataDomain? {
    logger.debug("Fetching data domain with id: $id")
    return repository.findById(id).orElse(null)
  }

  fun findByName(name: String): DataDomain? {
    logger.debug("Fetching data domain with name: $name")
    return repository.findByNameIgnoreCase(name)
  }

  fun search(searchTerm: String): List<DataDomain> {
    logger.debug("Searching data domains with term: $searchTerm")
    return if (searchTerm.isBlank()) {
      findAll()
    } else {
      repository.searchByNameOrDescription(searchTerm)
    }
  }

  fun create(dataDomain: DataDomain): DataDomain {
    logger.info("Creating new data domain: ${dataDomain.name}")
    val existing = repository.findByNameIgnoreCase(dataDomain.name)
    if (existing != null) {
      throw IllegalArgumentException("Data domain with name '${dataDomain.name}' already exists")
    }
    return repository.save(dataDomain)
  }

  fun upsert(dataDomain: DataDomain): DataDomain {
    logger.info("Upserting data domain: ${dataDomain.name}")
    val existing = repository.findByNameIgnoreCase(dataDomain.name)
    return if (existing != null) {
      logger.info("Updating existing data domain: ${dataDomain.name}")
      val updated = existing.copy(
        description = dataDomain.description,
        attributes = dataDomain.attributes,
        updatedAt = LocalDateTime.now(),
      )
      repository.save(updated)
    } else {
      logger.info("Creating new data domain: ${dataDomain.name}")
      repository.save(dataDomain)
    }
  }

  fun update(id: Int, dataDomain: DataDomain): DataDomain {
    logger.info("Updating data domain with id: $id")
    val existing = repository.findById(id).orElseThrow { IllegalArgumentException("Data domain not found") }

    val updated = existing.copy(
      name = dataDomain.name,
      description = dataDomain.description,
      attributes = dataDomain.attributes,
      updatedAt = LocalDateTime.now(),
    )
    return repository.save(updated)
  }

  fun delete(id: Int) {
    logger.info("Deleting data domain with id: $id")
    repository.deleteById(id)
  }
}
