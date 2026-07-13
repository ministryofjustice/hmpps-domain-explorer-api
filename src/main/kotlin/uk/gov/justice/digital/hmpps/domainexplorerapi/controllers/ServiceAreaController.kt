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
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.domainexplorerapi.domain.ServiceArea
import uk.gov.justice.digital.hmpps.domainexplorerapi.dto.ServiceAreaDTO
import uk.gov.justice.digital.hmpps.domainexplorerapi.services.ServiceAreaService

@RestController
@RequestMapping("/service-areas")
@CrossOrigin(origins = ["*"], maxAge = 3600)
class ServiceAreaController(private val service: ServiceAreaService) {
  private val logger = LoggerFactory.getLogger(javaClass)

  @GetMapping
  @PreAuthorize("hasRole('PROBATION_ROLE')")
  fun getAll(): ResponseEntity<List<ServiceAreaDTO>> {
    logger.info("GET /service-areas - Fetching all service areas")
    val areas = service.findAll()
    return ResponseEntity.ok(areas.map { it.toDTO() })
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('PROBATION_ROLE')")
  fun getById(@PathVariable id: Int): ResponseEntity<ServiceAreaDTO> {
    logger.info("GET /service-areas/$id")
    val area = service.findById(id)
      ?: return ResponseEntity.notFound().build()
    return ResponseEntity.ok(area.toDTO())
  }

  @PostMapping
  @PreAuthorize("hasRole('PROBATION_ROLE')")
  fun create(@RequestBody dto: ServiceAreaDTO): ResponseEntity<ServiceAreaDTO> {
    logger.info("POST /service-areas - Creating service area: ${dto.name}")
    return try {
      val area = ServiceArea(name = dto.name)
      val created = service.create(area)
      ResponseEntity.status(HttpStatus.CREATED).body(created.toDTO())
    } catch (e: Exception) {
      logger.error("Error creating service area: ${e.message}")
      ResponseEntity.badRequest().build()
    }
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('PROBATION_ROLE')")
  fun update(@PathVariable id: Int, @RequestBody dto: ServiceAreaDTO): ResponseEntity<ServiceAreaDTO> {
    logger.info("PUT /service-areas/$id - Updating service area")
    return try {
      val area = ServiceArea(name = dto.name)
      val updated = service.update(id, area)
      ResponseEntity.ok(updated.toDTO())
    } catch (e: Exception) {
      logger.error("Error updating service area: ${e.message}")
      ResponseEntity.notFound().build()
    }
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('PROBATION_ROLE')")
  fun delete(@PathVariable id: Int): ResponseEntity<Unit> {
    logger.info("DELETE /service-areas/$id")
    return try {
      service.delete(id)
      ResponseEntity.noContent().build()
    } catch (e: Exception) {
      logger.error("Error deleting service area: ${e.message}")
      ResponseEntity.notFound().build()
    }
  }

  private fun ServiceArea.toDTO() = ServiceAreaDTO(
    id = this.id,
    name = this.name,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt,
  )
}
