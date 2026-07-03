package uk.gov.justice.digital.hmpps.domainexplorerapi.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "digital_service", indexes = [Index(name = "idx_digital_service_name", columnList = "name")])
data class DigitalService(
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Int? = null,
  @Column(nullable = false, unique = true, length = 255) val name: String,
  @Column(name = "created_at", nullable = false, updatable = false) val createdAt: LocalDateTime = LocalDateTime.now(),
  @Column(name = "updated_at", nullable = false) val updatedAt: LocalDateTime = LocalDateTime.now(),
)
