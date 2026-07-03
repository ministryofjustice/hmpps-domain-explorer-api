package uk.gov.justice.digital.hmpps.domainexplorerapi.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

@Entity
@Table(
  name = "use_case_digital_service",
  uniqueConstraints = [UniqueConstraint(columnNames = ["use_case_id", "digital_service_id"], name = "uk_ucds")],
  indexes = [
    Index(name = "idx_ucds_use_case_id", columnList = "use_case_id"),
    Index(name = "idx_ucds_digital_service_id", columnList = "digital_service_id"),
  ],
)
data class UseCaseDigitalService(
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Int? = null,
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "use_case_id", nullable = false)
  val useCase: UseCase,
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "digital_service_id", nullable = false)
  val digitalService: DigitalService,
  @Column(columnDefinition = "text") val mappingJustification: String? = null,
  @Column(name = "created_at", nullable = false, updatable = false) val createdAt: LocalDateTime = LocalDateTime.now(),
)
