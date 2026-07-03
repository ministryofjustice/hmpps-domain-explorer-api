package uk.gov.justice.digital.hmpps.domainexplorerapi.controllers

import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.domainexplorerapi.etl.ExcelIngestor
import java.nio.file.Files
import java.nio.file.Path

@RestController
@RequestMapping("/upload")
@CrossOrigin(origins = ["*"], maxAge = 3600)
class UploadController(private val excelIngestor: ExcelIngestor) {
  private val logger = LoggerFactory.getLogger(javaClass)

  @PostMapping("/data-domains", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
  @PreAuthorize("hasRole('PROBATION_ROLE')")
  fun uploadDataDomains(@RequestParam("file") file: MultipartFile): ResponseEntity<String> {
    logger.info("POST /upload/data-domains - Uploading data domains file")
    return try {
      validateFile(file)
      val tempFile = saveTempFile(file)
      excelIngestor.ingestDataDomainsFromFile(tempFile.toString())
      Files.delete(tempFile)
      ResponseEntity.ok("Data domains uploaded and ingested successfully")
    } catch (e: Exception) {
      logger.error("Error uploading data domains: ${e.message}", e)
      ResponseEntity.badRequest().body("Failed to upload data domains: ${e.message}")
    }
  }

  @PostMapping("/workflows-use-cases", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
  @PreAuthorize("hasRole('PROBATION_ROLE')")
  fun uploadWorkflowsUseCases(@RequestParam("file") file: MultipartFile): ResponseEntity<String> {
    logger.info("POST /upload/workflows-use-cases - Uploading workflows and use cases file")
    return try {
      validateFile(file)
      val tempFile = saveTempFile(file)
      excelIngestor.ingestUseCasesAndBusinessProcessesFromFile(tempFile.toString())
      Files.delete(tempFile)
      ResponseEntity.ok("Workflows and use cases uploaded and ingested successfully")
    } catch (e: Exception) {
      logger.error("Error uploading workflows and use cases: ${e.message}", e)
      ResponseEntity.badRequest().body("Failed to upload workflows and use cases: ${e.message}")
    }
  }

  private fun validateFile(file: MultipartFile) {
    if (file.isEmpty) {
      throw IllegalArgumentException("File is empty")
    }
    if (!file.originalFilename?.endsWith(".xlsx", ignoreCase = true)!!) {
      throw IllegalArgumentException("File must be an XLSX file")
    }
  }

  private fun saveTempFile(file: MultipartFile): Path {
    val tempFile = Files.createTempFile("upload-", ".xlsx")
    file.inputStream.use { input ->
      Files.newOutputStream(tempFile).use { output ->
        input.copyTo(output)
      }
    }
    return tempFile
  }
}
