package uk.gov.justice.digital.hmpps.domainexplorerapi.config

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import uk.gov.justice.digital.hmpps.domainexplorerapi.dto.ApiErrorDTO

@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {
  private val logger = LoggerFactory.getLogger(javaClass)

  @ExceptionHandler(IllegalArgumentException::class)
  fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ApiErrorDTO> {
    logger.warn("Illegal argument exception: ${ex.message}")
    return ResponseEntity.badRequest().body(
      ApiErrorDTO(
        error = "INVALID_ARGUMENT",
        message = ex.message,
      ),
    )
  }

  @ExceptionHandler(Exception::class)
  fun handleGenericException(ex: Exception): ResponseEntity<ApiErrorDTO> {
    logger.error("Unexpected error occurred", ex)
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
      ApiErrorDTO(
        error = "INTERNAL_ERROR",
        message = "An unexpected error occurred. Please contact support.",
      ),
    )
  }
}
