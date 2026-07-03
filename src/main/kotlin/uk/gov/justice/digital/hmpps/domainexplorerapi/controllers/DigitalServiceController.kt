package uk.gov.justice.digital.hmpps.domainexplorerapi.controllers

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.security.access.prepost.PreAuthorize
import uk.gov.justice.digital.hmpps.domainexplorerapi.domain.DigitalService
import uk.gov.justice.digital.hmpps.domainexplorerapi.dto.DigitalServiceDTO
import uk.gov.justice.digital.hmpps.domainexplorerapi.services.DigitalServiceService

@RestController
@RequestMapping("/digital-services")
@CrossOrigin(origins = ["*"], maxAge = 3600)
class DigitalServiceController(private val service: DigitalServiceService) {
  private val logger = LoggerFactory.getLogger(javaClass)

  @GetMapping
  @PreAuthorize("hasRole('PROBATION_ROLE')")
  fun getAll(): ResponseEntity<List<DigitalServiceDTO>> {
    logger.info("GET /digital-services - Fetching all digital services")
    val services = service.findAll()
    return ResponseEntity.ok(services.map { it.toDTO() })
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('PROBATION_ROLE')")
  fun getById(@PathVariable id: Int): ResponseEntity<DigitalServiceDTO> {
    logger.info("GET /digital-services/$id")
    val service = service.findById(id)
      ?: return ResponseEntity.notFound().build()
    return ResponseEntity.ok(service.toDTO())
  }

  @PostMapping
  @PreAuthorize("hasRole('PROBATION_ROLE')")
  fun create(@RequestBody dto: DigitalServiceDTO): ResponseEntity<DigitalServiceDTO> {
    logger.info("POST /digital-services - Creating digital service: ${dto.name}")
    return try {
      val ds = DigitalService(name = dto.name)
      val created = service.create(ds)
      ResponseEntity.status(HttpStatus.CREATED).body(created.toDTO())
    } catch (e: Exception) {
      logger.error("Error creating digital service: ${e.message}")
      ResponseEntity.badRequest().build()
    }
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('PROBATION_ROLE')")
  fun update(@PathVariable id: Int, @RequestBody dto: DigitalServiceDTO): ResponseEntity<DigitalServiceDTO> {
    logger.info("PUT /digital-services/$id - Updating digital service")
    return try {
      val ds = DigitalService(name = dto.name)
      val updated = service.update(id, ds)
      ResponseEntity.ok(updated.toDTO())
    } catch (e: Exception) {
      logger.error("Error updating digital service: ${e.message}")
      ResponseEntity.notFound().build()
    }
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('PROBATION_ROLE')")
  fun delete(@PathVariable id: Int): ResponseEntity<Unit> {
    logger.info("DELETE /digital-services/$id")
    return try {
      service.delete(id)
      ResponseEntity.noContent().build()
    } catch (e: Exception) {
      logger.error("Error deleting digital service: ${e.message}")
      ResponseEntity.notFound().build()
    }
  }

  private fun DigitalService.toDTO() = DigitalServiceDTO(
    id = this.id,
    name = this.name,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt,
  )
}
