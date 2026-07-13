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
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime

@Entity
@Table(
  name = "business_process",
  indexes = [
    Index(name = "idx_business_process_name", columnList = "name"),
    Index(name = "idx_business_process_service_area_id", columnList = "service_area_id"),
  ],
)
data class BusinessProcess(
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Int? = null,
  @Column(nullable = false, length = 255) val name: String,
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "service_area_id", nullable = true)
  val serviceArea: ServiceArea? = null,
  @Column(columnDefinition = "text") val mappingJustification: String? = null,
  @Column(name = "attributes", columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  val attributes: String? = null,
  @Column(name = "created_at", nullable = false, updatable = false) val createdAt: LocalDateTime = LocalDateTime.now(),
  @Column(name = "updated_at", nullable = false) val updatedAt: LocalDateTime = LocalDateTime.now(),
)
