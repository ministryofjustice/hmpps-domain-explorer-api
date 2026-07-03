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
  name = "use_case_service_area",
  uniqueConstraints = [UniqueConstraint(columnNames = ["use_case_id", "service_area_id"], name = "uk_ucsa")],
  indexes = [
    Index(name = "idx_ucsa_use_case_id", columnList = "use_case_id"),
    Index(name = "idx_ucsa_service_area_id", columnList = "service_area_id"),
  ],
)
data class UseCaseServiceArea(
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Int? = null,
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "use_case_id", nullable = false)
  val useCase: UseCase,
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "service_area_id", nullable = false)
  val serviceArea: ServiceArea,
  @Column(name = "created_at", nullable = false, updatable = false) val createdAt: LocalDateTime = LocalDateTime.now(),
)
