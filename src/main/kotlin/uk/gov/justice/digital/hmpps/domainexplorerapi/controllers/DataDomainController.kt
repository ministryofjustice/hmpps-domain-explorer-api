package uk.gov.justice.digital.hmpps.domainexplorerapi.controllers

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.domainexplorerapi.domain.DataDomain
import uk.gov.justice.digital.hmpps.domainexplorerapi.dto.DataDomainDTO
import uk.gov.justice.digital.hmpps.domainexplorerapi.dto.SearchResultDTO
import uk.gov.justice.digital.hmpps.domainexplorerapi.services.DataDomainService

@RestController
@RequestMapping("/data-domains")
@CrossOrigin(origins = ["*"], maxAge = 3600)
class DataDomainController(private val service: DataDomainService) {
  private val logger = LoggerFactory.getLogger(javaClass)

  @GetMapping
  @PreAuthorize("hasRole('PROBATION_ROLE')")
  fun getAll(): ResponseEntity<List<DataDomainDTO>> {
    logger.info("GET /data-domains - Fetching all data domains")
    val domains = service.findAll()
    return ResponseEntity.ok(domains.map { it.toDTO() })
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('PROBATION_ROLE')")
  fun getById(@PathVariable id: Int): ResponseEntity<DataDomainDTO> {
    logger.info("GET /data-domains/$id")
    val domain = service.findById(id)
      ?: return ResponseEntity.notFound().build()
    return ResponseEntity.ok(domain.toDTO())
  }

  @GetMapping("/search")
  @PreAuthorize("hasRole('PROBATION_ROLE')")
  fun search(@RequestParam(required = false, defaultValue = "") searchTerm: String): ResponseEntity<SearchResultDTO<DataDomainDTO>> {
    logger.info("GET /data-domains/search?searchTerm=$searchTerm")
    val results = service.search(searchTerm)
    val dto = SearchResultDTO(
      results = results.map { it.toDTO() },
      count = results.size,
    )
    return ResponseEntity.ok(dto)
  }

  @PostMapping
  @PreAuthorize("hasRole('PROBATION_ROLE')")
  fun create(@RequestBody dto: DataDomainDTO): ResponseEntity<DataDomainDTO> {
    logger.info("POST /data-domains - Creating data domain: ${dto.name}")
    return try {
      val domain = DataDomain(
        name = dto.name,
        description = dto.description,
      )
      val created = service.create(domain)
      ResponseEntity.status(HttpStatus.CREATED).body(created.toDTO())
    } catch (e: Exception) {
      logger.error("Error creating data domain: ${e.message}")
      ResponseEntity.badRequest().build()
    }
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('PROBATION_ROLE')")
  fun update(@PathVariable id: Int, @RequestBody dto: DataDomainDTO): ResponseEntity<DataDomainDTO> {
    logger.info("PUT /data-domains/$id - Updating data domain")
    return try {
      val domain = DataDomain(
        name = dto.name,
        description = dto.description,
      )
      val updated = service.update(id, domain)
      ResponseEntity.ok(updated.toDTO())
    } catch (e: Exception) {
      logger.error("Error updating data domain: ${e.message}")
      ResponseEntity.notFound().build()
    }
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('PROBATION_ROLE')")
  fun delete(@PathVariable id: Int): ResponseEntity<Unit> {
    logger.info("DELETE /data-domains/$id")
    return try {
      service.delete(id)
      ResponseEntity.noContent().build()
    } catch (e: Exception) {
      logger.error("Error deleting data domain: ${e.message}")
      ResponseEntity.notFound().build()
    }
  }

  private fun DataDomain.toDTO() = DataDomainDTO(
    id = this.id,
    name = this.name,
    description = this.description,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt,
  )
}
