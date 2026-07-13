package uk.gov.justice.digital.hmpps.domainexplorerapi.etl

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.poi.common.usermodel.HyperlinkType
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.util.CellReference
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domainexplorerapi.domain.BusinessProcess
import uk.gov.justice.digital.hmpps.domainexplorerapi.domain.BusinessProcessDigitalService
import uk.gov.justice.digital.hmpps.domainexplorerapi.domain.BusinessProcessServiceArea
import uk.gov.justice.digital.hmpps.domainexplorerapi.domain.DataDomain
import uk.gov.justice.digital.hmpps.domainexplorerapi.domain.DigitalService
import uk.gov.justice.digital.hmpps.domainexplorerapi.domain.ServiceArea
import uk.gov.justice.digital.hmpps.domainexplorerapi.domain.UseCase
import uk.gov.justice.digital.hmpps.domainexplorerapi.domain.UseCaseDataDomain
import uk.gov.justice.digital.hmpps.domainexplorerapi.domain.UseCaseDigitalService
import uk.gov.justice.digital.hmpps.domainexplorerapi.domain.UseCaseServiceArea
import uk.gov.justice.digital.hmpps.domainexplorerapi.repositories.BusinessProcessDigitalServiceRepository
import uk.gov.justice.digital.hmpps.domainexplorerapi.repositories.BusinessProcessServiceAreaRepository
import uk.gov.justice.digital.hmpps.domainexplorerapi.repositories.UseCaseDataDomainRepository
import uk.gov.justice.digital.hmpps.domainexplorerapi.repositories.UseCaseDigitalServiceRepository
import uk.gov.justice.digital.hmpps.domainexplorerapi.repositories.UseCaseServiceAreaRepository
import uk.gov.justice.digital.hmpps.domainexplorerapi.services.BusinessProcessService
import uk.gov.justice.digital.hmpps.domainexplorerapi.services.DataDomainService
import uk.gov.justice.digital.hmpps.domainexplorerapi.services.DigitalServiceService
import uk.gov.justice.digital.hmpps.domainexplorerapi.services.ServiceAreaService
import uk.gov.justice.digital.hmpps.domainexplorerapi.services.UseCaseService
import java.io.File
import java.time.LocalDateTime

@Service
class ExcelIngestor(
  private val dataDomainService: DataDomainService,
  private val useCaseService: UseCaseService,
  private val businessProcessService: BusinessProcessService,
  private val serviceAreaService: ServiceAreaService,
  private val digitalServiceService: DigitalServiceService,
  private val useCaseDataDomainRepository: UseCaseDataDomainRepository,
  private val businessProcessDigitalServiceRepository: BusinessProcessDigitalServiceRepository,
  private val useCaseDigitalServiceRepository: UseCaseDigitalServiceRepository,
  private val businessProcessServiceAreaRepository: BusinessProcessServiceAreaRepository,
  private val useCaseServiceAreaRepository: UseCaseServiceAreaRepository,
) {
  private val logger = LoggerFactory.getLogger(javaClass)
  private val objectMapper = ObjectMapper()

  fun ingestDataDomainsFromFile(filePath: String) {
    logger.info("Starting data domain ingestion from: $filePath")
    val file = File(filePath)

    if (!file.exists()) {
      logger.error("File not found: $filePath")
      throw IllegalArgumentException("File not found: $filePath")
    }

    var rowsExamined = 0
    var rowsSkipped = 0

    WorkbookFactory.create(file).use { workbook ->
      // Log all available sheets
      val sheetNames = mutableListOf<String>()
      for (i in 0 until workbook.numberOfSheets) {
        sheetNames.add(workbook.getSheetName(i))
      }
      logger.info("Available sheets: $sheetNames")

      // Look for "another view" sheet as specified in requirements
      val sheet = workbook.getSheet("another view")
        ?: throw IllegalArgumentException("Sheet 'another view' not found in workbook")

      logger.info("Found 'another view' sheet, starting to process data domains")
      logger.info("Sheet has ${sheet.lastRowNum + 1} rows (0-${sheet.lastRowNum})")

      // Log all rows to understand structure
      for (rowIndex in 0..sheet.lastRowNum) {
        val row = sheet.getRow(rowIndex)
        if (row != null) {
          val col0 = getCellStringValue(row, 0)
          val col1 = getCellStringValue(row, 1)
          val hasHyperlink = row.getCell(1)?.hyperlink != null
          logger.info("Row $rowIndex: col0='$col0' | col1='$col1' | hasHyperlink=$hasHyperlink")
        }
      }

      // Process data domain rows from row 2 to 32 (indices 1-31)
      for (rowIndex in 1..31) {
        val row = sheet.getRow(rowIndex) ?: continue
        rowsExamined++

        val name = getCellStringValue(row, 0) ?: run {
          rowsSkipped++
          continue
        } // Column B
        if (name.isBlank()) {
          rowsSkipped++
          continue
        }

        // Follow hyperlink in column 1 (B) to get description from "Definition" tab
        val description = getDescriptionFromHyperlink(workbook, row)

        // Collect additional attributes from columns C, D, E (indices 2, 3, 4)
        val attributesMap = mutableMapOf<String, Any?>()
        for (cellIndex in 2..4) { // Columns C, D, E
          val value = getCellValue(row.getCell(cellIndex))
          if (value != null && value.isNotBlank()) {
            val columnName = when (cellIndex) {
              2 -> "Column C"
              3 -> "Column D"
              4 -> "Column E"
              else -> "Column $cellIndex"
            }
            attributesMap[columnName] = value
          }
        }
        val attributes = if (attributesMap.isNotEmpty()) objectMapper.writeValueAsString(attributesMap) else null

        logger.debug("Row $rowIndex: Processing domain name='$name', description='$description', attributes='$attributes'")

        try {
          val dataDomain = DataDomain(
            name = name,
            description = description,
            attributes = attributes,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
          )
          dataDomainService.upsert(dataDomain)
          logger.info("Upserted data domain: $name")
        } catch (e: Exception) {
          logger.warn("Error processing data domain '$name': ${e.message}")
        }
      }
    }

    logger.info("Data domain ingestion completed - Examined $rowsExamined rows, skipped $rowsSkipped rows")
  }

  fun ingestUseCasesAndBusinessProcessesFromFile(filePath: String) {
    logger.info("Starting use cases and business processes ingestion from: $filePath")
    val file = File(filePath)

    if (!file.exists()) {
      logger.error("File not found: $filePath")
      throw IllegalArgumentException("File not found: $filePath")
    }

    WorkbookFactory.create(file).use { workbook ->
      // Log all available sheets
      val sheetNames = mutableListOf<String>()
      for (i in 0 until workbook.numberOfSheets) {
        sheetNames.add(workbook.getSheetName(i))
      }
      logger.info("Available sheets in workflows file: $sheetNames")

      // Ingest Service Areas first
      ingestServiceAreas(workbook)

      // Ingest Digital Services
      ingestDigitalServices(workbook)

      // Ingest Business Processes
      val workflowSheet = workbook.getSheet("Workflows")
      if (workflowSheet != null) {
        logger.info("Found 'Workflows' sheet, ingesting business processes")
        ingestBusinessProcesses(workflowSheet)
      } else {
        logger.error("Sheet 'Workflows' not found in workbook")
      }

      // Ingest Use Cases
      val ucSheet = workbook.getSheet("Use Cases")
      if (ucSheet != null) {
        logger.info("Found 'Use Cases' sheet, ingesting use cases")
        ingestUseCases(ucSheet)
      } else {
        logger.warn("Sheet 'Use Cases' not found in workbook")
      }

      // Process mappings
      val mappingSheet = workbook.getSheet("Mappings")
      if (mappingSheet != null) {
        processMappings(mappingSheet)
      }
    }

    logger.info("Use cases and business processes ingestion completed")
  }

  private fun ingestServiceAreas(workbook: Workbook) {
    logger.debug("Ingesting service areas")
    val sheet = workbook.getSheet("Data") ?: return

    for (rowIndex in 1..sheet.lastRowNum) {
      val row = sheet.getRow(rowIndex) ?: continue
      val name = getCellStringValue(row, 0) ?: continue

      if (name.isBlank()) continue
      if (name == "Total") break

      try {
        val serviceArea = ServiceArea(
          name = name,
          createdAt = LocalDateTime.now(),
          updatedAt = LocalDateTime.now(),
        )
        serviceAreaService.upsert(serviceArea)
        logger.info("Upserted service area: $name")
      } catch (e: Exception) {
        logger.warn("Error processing service area '$name': ${e.message}")
      }
    }
  }

  private fun ingestDigitalServices(workbook: Workbook) {
    logger.debug("Ingesting digital services")
    val sheet = workbook.getSheet("Data") ?: return

    for (rowIndex in 1..sheet.lastRowNum) {
      val row = sheet.getRow(rowIndex) ?: continue
      val name = getCellStringValue(row, 3) ?: continue

      if (name.isBlank()) continue
      if (name == "Total") break
      try {
        val digitalService = DigitalService(
          name = name,
          createdAt = LocalDateTime.now(),
          updatedAt = LocalDateTime.now(),
        )
        digitalServiceService.upsert(digitalService)
        logger.info("Upserted digital service: $name")
      } catch (e: Exception) {
        logger.warn("Error processing digital service '$name': ${e.message}")
      }
    }
  }

  private fun ingestBusinessProcesses(sheet: org.apache.poi.ss.usermodel.Sheet) {
    logger.debug("Ingesting business processes")

    val headerRow = sheet.getRow(0) ?: return
    val headers = mutableListOf<String>()
    for (cellIndex in 0..headerRow.lastCellNum) {
      val header = getCellValue(headerRow.getCell(cellIndex))?.trim() ?: "Column_$cellIndex"
      headers.add(header)
    }

    // Find column indices with alias handling for sheet variations
    val nameIndex = findHeaderIndex(headers, listOf("Name", "Business Process", "Business Process Name", "Workflow"))
    val serviceAreaIndex = findHeaderIndex(
      headers,
      listOf("Possible Service Area", "Service Area", "Service Areas", "Service Area(s)"),
    )
    val digitalServiceIndex = findHeaderIndex(
      headers,
      listOf("Possible Digital Service", "Digital Service", "Digital Services", "Service", "Services"),
    )
    val justificationIndex = findHeaderIndex(headers, listOf("Justification", "Mapping Justification"))

    logger.info(
      "Business Process column indices - Name: $nameIndex, Service Area: $serviceAreaIndex, " +
        "Digital Service: $digitalServiceIndex, Justification: $justificationIndex",
    )

    if (nameIndex == -1) {
      logger.warn("Header 'Name' not found in sheet")
      return
    }

    val reservedIndices = setOf(nameIndex, serviceAreaIndex, digitalServiceIndex, justificationIndex)

    for (rowIndex in 1..sheet.lastRowNum) {
      val row = sheet.getRow(rowIndex) ?: continue

      val name = getCellStringValue(row, nameIndex) ?: continue
      if (name.isBlank()) continue

      val serviceAreaNames = if (serviceAreaIndex >= 0) splitMultiValueCell(getCellStringValue(row, serviceAreaIndex)) else emptyList()
      val digitalServiceNames = if (digitalServiceIndex >= 0) splitMultiValueCell(getCellStringValue(row, digitalServiceIndex)) else emptyList()
      val justification = getCellStringValue(row, justificationIndex)

      logger.info(
        "Row $rowIndex: businessProcess='$name', serviceAreas=$serviceAreaNames, " +
          "digitalServices=$digitalServiceNames, justification='$justification'",
      )

      // Collect additional attributes
      val attributesMap = mutableMapOf<String, Any?>()
      for (cellIndex in 0..row.lastCellNum) {
        if (cellIndex >= headers.size) continue
        if (cellIndex in reservedIndices) continue
        val value = getCellValue(row.getCell(cellIndex))
        if (value != null) {
          attributesMap[headers[cellIndex]] = value
        }
      }
      val attributes = if (attributesMap.isNotEmpty()) objectMapper.writeValueAsString(attributesMap) else null

      var savedBusinessProcess: BusinessProcess? = null
      try {
        // Keep the direct FK populated (first service area) for backward compatibility
        val firstServiceArea = serviceAreaNames.firstOrNull()?.let { serviceAreaName ->
          serviceAreaService.findByName(serviceAreaName)
            ?: serviceAreaService.upsert(
              ServiceArea(
                name = serviceAreaName,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
              ),
            )
        }

        val businessProcess = BusinessProcess(
          name = name,
          serviceArea = firstServiceArea,
          mappingJustification = justification,
          attributes = attributes,
          createdAt = LocalDateTime.now(),
          updatedAt = LocalDateTime.now(),
        )
        savedBusinessProcess = businessProcessService.upsert(businessProcess)
        logger.info("Upserted business process: $name")
      } catch (e: Exception) {
        logger.warn("Error processing business process '$name': ${e.message}")
      }

      savedBusinessProcess?.let { bp ->
        try {
          serviceAreaNames.forEach { linkBusinessProcessToServiceArea(bp, it) }
          digitalServiceNames.forEach { linkBusinessProcessToDigitalService(bp, it, justification) }
        } catch (e: Exception) {
          logger.warn("Error linking mappings for business process '$name': ${e.message}")
        }
      }
    }
  }

  private fun ingestUseCases(sheet: org.apache.poi.ss.usermodel.Sheet) {
    logger.debug("Ingesting use cases")

    val headerRow = sheet.getRow(0) ?: return
    val headers = mutableListOf<String>()
    for (cellIndex in 0..headerRow.lastCellNum) {
      val header = getCellValue(headerRow.getCell(cellIndex))?.trim() ?: "Column_$cellIndex"
      headers.add(header)
    }
    logger.info("Use Cases sheet headers: $headers")

    // Find column indices
    val ucIdIndex = headers.indexOfFirst { it.equals("ID", ignoreCase = true) }
    val titleIndex = headers.indexOfFirst { it.equals("Title", ignoreCase = true) }
    val descriptionIndex = headers.indexOfFirst { it.equals("What", ignoreCase = true) }

    // Primary, secondary and third data domain columns
    val primaryDomainIndex = headers.indexOfFirst {
      it.equals("Primary Domain", ignoreCase = true) ||
        it.equals("Primary Data Domain", ignoreCase = true) ||
        it.equals("Primary", ignoreCase = true)
    }
    val secondaryDomainIndex = headers.indexOfFirst {
      it.equals("Secondary Domain", ignoreCase = true) ||
        it.equals("Secondary Data Domain", ignoreCase = true) ||
        it.equals("Secondary", ignoreCase = true)
    }
    val thirdDomainIndex = headers.indexOfFirst {
      it.equals("Third Domain", ignoreCase = true) ||
        it.equals("Third Data Domain", ignoreCase = true) ||
        it.equals("Third", ignoreCase = true)
    }
    val serviceAreaIndex = findHeaderIndex(
      headers,
      listOf("Service Area", "Possible Service Area", "Service Areas", "Service Area(s)"),
    )
    val digitalServiceIndex = findHeaderIndex(
      headers,
      listOf("Digital Service", "Digital Services", "Service", "Services", "Possible Service"),
    )
    val justificationIndex = findHeaderIndex(
      headers,
      listOf("Justification", "Mapping Justification"),
    )

    logger.info(
      "Column indices - ID: $ucIdIndex, Title: $titleIndex, Description: $descriptionIndex, " +
        "Primary Domain: $primaryDomainIndex, Secondary Domain: $secondaryDomainIndex, Third Domain: $thirdDomainIndex, " +
        "Service Area: $serviceAreaIndex, Digital Service: $digitalServiceIndex, Justification: $justificationIndex",
    )

    if (ucIdIndex == -1) {
      logger.warn("Header 'ID' not found. Available: $headers")
      return
    }
    if (titleIndex == -1) {
      logger.warn("Header 'Title' not found. Available: $headers")
      return
    }
    if (descriptionIndex == -1) {
      logger.warn("Header 'What' not found. Available: $headers")
      return
    }

    // Columns handled as proper relations – exclude from attributes map
    val reservedIndices = setOf(
      ucIdIndex,
      titleIndex,
      descriptionIndex,
      primaryDomainIndex,
      secondaryDomainIndex,
      thirdDomainIndex,
      serviceAreaIndex,
      digitalServiceIndex,
      justificationIndex,
    )

    for (rowIndex in 1..sheet.lastRowNum) {
      val row = sheet.getRow(rowIndex) ?: continue

      val ucId = getCellStringValue(row, ucIdIndex) ?: continue
      if (ucId.isBlank()) continue

      val title = getCellStringValue(row, titleIndex)
      val description = getCellStringValue(row, descriptionIndex)
      val primaryDomainName = if (primaryDomainIndex >= 0) getCellStringValue(row, primaryDomainIndex)?.takeIf { it.isNotBlank() } else null
      val secondaryDomainName = if (secondaryDomainIndex >= 0) getCellStringValue(row, secondaryDomainIndex)?.takeIf { it.isNotBlank() } else null
      val thirdDomainName = if (thirdDomainIndex >= 0) getCellStringValue(row, thirdDomainIndex)?.takeIf { it.isNotBlank() } else null
      val serviceAreaNames = if (serviceAreaIndex >= 0) splitMultiValueCell(getCellStringValue(row, serviceAreaIndex)) else emptyList()
      val digitalServiceNames = if (digitalServiceIndex >= 0) splitMultiValueCell(getCellStringValue(row, digitalServiceIndex)) else emptyList()
      val mappingJustification = if (justificationIndex >= 0) getCellStringValue(row, justificationIndex)?.takeIf { it.isNotBlank() } else null

      logger.info(
        "Row $rowIndex: ucId='$ucId', primaryDomain='$primaryDomainName', secondaryDomain='$secondaryDomainName', " +
          "thirdDomain='$thirdDomainName', serviceAreas=$serviceAreaNames, digitalServices=$digitalServiceNames, justification='${mappingJustification ?: ""}'",
      )

      // Collect remaining columns as JSONB attributes
      val attributesMap = mutableMapOf<String, Any?>()
      for (cellIndex in 0..row.lastCellNum) {
        if (cellIndex >= headers.size) continue
        if (cellIndex in reservedIndices) continue
        val value = getCellValue(row.getCell(cellIndex))
        if (value != null) attributesMap[headers[cellIndex]] = value
      }
      val attributes = if (attributesMap.isNotEmpty()) objectMapper.writeValueAsString(attributesMap) else null

      var savedUseCase: UseCase? = null
      try {
        val useCase = UseCase(
          ucId = ucId,
          title = title,
          description = description,
          mappingJustification = mappingJustification,
          attributes = attributes,
          createdAt = LocalDateTime.now(),
          updatedAt = LocalDateTime.now(),
        )
        savedUseCase = useCaseService.upsert(useCase)
        logger.info("Upserted use case: $ucId")
      } catch (e: Exception) {
        logger.warn("Error processing use case '$ucId': ${e.message}")
      }

      // Link data domains in a separate try/catch so upsert errors don't prevent linking
      savedUseCase?.let { uc ->
        try {
          primaryDomainName?.let { linkUseCaseToDataDomain(uc, it, "primary") }
          secondaryDomainName?.let { linkUseCaseToDataDomain(uc, it, "secondary") }
          thirdDomainName?.let { linkUseCaseToDataDomain(uc, it, "tertiary") }
          serviceAreaNames.forEach { linkUseCaseToServiceArea(uc, it) }
          digitalServiceNames.forEach { linkUseCaseToDigitalService(uc, it) }
        } catch (e: Exception) {
          logger.warn("Error linking data domains for use case '$ucId': ${e.message}")
        }
      }
    }
  }

  private fun linkUseCaseToDataDomain(useCase: UseCase, domainName: String, role: String) {
    val dataDomain = dataDomainService.findByName(domainName)
    if (dataDomain == null) {
      logger.warn("Data domain '$domainName' not found for use case '${useCase.ucId}' ($role) – skipping link")
      return
    }
    val useCaseId = useCase.id ?: return
    val domainId = dataDomain.id ?: return

    val existing = useCaseDataDomainRepository.findByUseCaseIdAndDataDomainId(useCaseId, domainId)
    if (existing == null) {
      useCaseDataDomainRepository.save(
        UseCaseDataDomain(
          useCase = useCase,
          dataDomain = dataDomain,
          mappingJustification = role,
          createdAt = LocalDateTime.now(),
        ),
      )
      logger.info("Linked use case '${useCase.ucId}' -> data domain '$domainName' ($role)")
    } else {
      logger.debug("Link already exists: use case '${useCase.ucId}' -> data domain '$domainName'")
    }
  }

  private fun linkUseCaseToServiceArea(useCase: UseCase, serviceAreaName: String) {
    val useCaseId = useCase.id ?: return
    val serviceArea = serviceAreaService.findByName(serviceAreaName)
      ?: serviceAreaService.upsert(
        ServiceArea(
          name = serviceAreaName,
          createdAt = LocalDateTime.now(),
          updatedAt = LocalDateTime.now(),
        ),
      )
    val serviceAreaId = serviceArea.id ?: return

    val existing = useCaseServiceAreaRepository.findByUseCaseIdAndServiceAreaId(useCaseId, serviceAreaId)
    if (existing == null) {
      useCaseServiceAreaRepository.save(
        UseCaseServiceArea(
          useCase = useCase,
          serviceArea = serviceArea,
          createdAt = LocalDateTime.now(),
        ),
      )
      logger.info("Linked use case '${useCase.ucId}' -> service area '$serviceAreaName'")
    }
  }

  private fun linkUseCaseToDigitalService(useCase: UseCase, digitalServiceName: String) {
    val useCaseId = useCase.id ?: return
    val digitalService = digitalServiceService.findByName(digitalServiceName)
      ?: digitalServiceService.upsert(
        DigitalService(
          name = digitalServiceName,
          createdAt = LocalDateTime.now(),
          updatedAt = LocalDateTime.now(),
        ),
      )
    val digitalServiceId = digitalService.id ?: return

    val existing = useCaseDigitalServiceRepository.findByUseCaseIdAndDigitalServiceId(useCaseId, digitalServiceId)
    if (existing == null) {
      useCaseDigitalServiceRepository.save(
        UseCaseDigitalService(
          useCase = useCase,
          digitalService = digitalService,
          mappingJustification = null,
          createdAt = LocalDateTime.now(),
        ),
      )
      logger.info("Linked use case '${useCase.ucId}' -> digital service '$digitalServiceName'")
    }
  }

  private fun linkBusinessProcessToServiceArea(businessProcess: BusinessProcess, serviceAreaName: String) {
    val businessProcessId = businessProcess.id ?: return
    val serviceArea = serviceAreaService.findByName(serviceAreaName)
      ?: serviceAreaService.upsert(
        ServiceArea(
          name = serviceAreaName,
          createdAt = LocalDateTime.now(),
          updatedAt = LocalDateTime.now(),
        ),
      )
    val serviceAreaId = serviceArea.id ?: return

    val existing = businessProcessServiceAreaRepository.findByBusinessProcessIdAndServiceAreaId(businessProcessId, serviceAreaId)
    if (existing == null) {
      businessProcessServiceAreaRepository.save(
        BusinessProcessServiceArea(
          businessProcess = businessProcess,
          serviceArea = serviceArea,
          createdAt = LocalDateTime.now(),
        ),
      )
      logger.info("Linked business process '${businessProcess.name}' -> service area '$serviceAreaName'")
    }
  }

  private fun linkBusinessProcessToDigitalService(
    businessProcess: BusinessProcess,
    digitalServiceName: String,
    mappingJustification: String?,
  ) {
    val businessProcessId = businessProcess.id ?: return
    val digitalService = digitalServiceService.findByName(digitalServiceName)
      ?: digitalServiceService.upsert(
        DigitalService(
          name = digitalServiceName,
          createdAt = LocalDateTime.now(),
          updatedAt = LocalDateTime.now(),
        ),
      )
    val digitalServiceId = digitalService.id ?: return

    val existing = businessProcessDigitalServiceRepository.findByBusinessProcessIdAndDigitalServiceId(businessProcessId, digitalServiceId)
    if (existing == null) {
      businessProcessDigitalServiceRepository.save(
        BusinessProcessDigitalService(
          businessProcess = businessProcess,
          digitalService = digitalService,
          mappingJustification = mappingJustification,
          createdAt = LocalDateTime.now(),
        ),
      )
      logger.info("Linked business process '${businessProcess.name}' -> digital service '$digitalServiceName'")
    }
  }

  private fun splitMultiValueCell(value: String?): List<String> {
    if (value.isNullOrBlank()) return emptyList()
    return value
      .split(Regex("[;,\\n\\r]+"))
      .map { it.trim() }
      .filter { it.isNotBlank() }
      .distinct()
  }

  private fun findHeaderIndex(headers: List<String>, aliases: List<String>): Int {
    val normalizedAliases = aliases.map { normalizeHeader(it) }.toSet()
    return headers.indexOfFirst { normalizeHeader(it) in normalizedAliases }
  }

  private fun normalizeHeader(value: String): String = value.lowercase().replace(Regex("[^a-z0-9]+"), "")

  private fun processMappings(sheet: org.apache.poi.ss.usermodel.Sheet) {
    logger.debug("Processing mappings")
    // Implementation depends on actual mapping structure in spreadsheet
  }

  private fun getCellValue(cell: Cell?): String? {
    if (cell == null) return null
    return when (cell.cellType) {
      org.apache.poi.ss.usermodel.CellType.STRING -> cell.stringCellValue.trim().ifBlank { null }
      org.apache.poi.ss.usermodel.CellType.NUMERIC -> cell.numericCellValue.toString()
      org.apache.poi.ss.usermodel.CellType.BOOLEAN -> cell.booleanCellValue.toString()
      org.apache.poi.ss.usermodel.CellType.FORMULA -> when (cell.cachedFormulaResultType) {
        org.apache.poi.ss.usermodel.CellType.STRING -> cell.stringCellValue.trim().ifBlank { null }
        org.apache.poi.ss.usermodel.CellType.NUMERIC -> cell.numericCellValue.toString()
        org.apache.poi.ss.usermodel.CellType.BOOLEAN -> cell.booleanCellValue.toString()
        else -> null
      }
      org.apache.poi.ss.usermodel.CellType.BLANK -> null
      else -> null
    }
  }

  private fun getCellStringValue(row: Row, index: Int): String? {
    val cell = row.getCell(index) ?: return null
    return when (cell.cellType) {
      org.apache.poi.ss.usermodel.CellType.STRING -> cell.stringCellValue.trim()
      org.apache.poi.ss.usermodel.CellType.NUMERIC -> cell.numericCellValue.toString()
      org.apache.poi.ss.usermodel.CellType.BOOLEAN -> cell.booleanCellValue.toString()
      org.apache.poi.ss.usermodel.CellType.FORMULA -> when (cell.cachedFormulaResultType) {
        org.apache.poi.ss.usermodel.CellType.STRING -> cell.stringCellValue.trim()
        org.apache.poi.ss.usermodel.CellType.NUMERIC -> cell.numericCellValue.toString()
        org.apache.poi.ss.usermodel.CellType.BOOLEAN -> cell.booleanCellValue.toString()
        else -> null
      }
      org.apache.poi.ss.usermodel.CellType.BLANK -> ""
      else -> null
    }
  }

  private fun getCellStringValue(cell: Cell?): String? {
    if (cell == null) return null
    return when (cell.cellType) {
      org.apache.poi.ss.usermodel.CellType.STRING -> cell.stringCellValue.trim()
      org.apache.poi.ss.usermodel.CellType.NUMERIC -> cell.numericCellValue.toString()
      org.apache.poi.ss.usermodel.CellType.BOOLEAN -> cell.booleanCellValue.toString()
      org.apache.poi.ss.usermodel.CellType.FORMULA -> when (cell.cachedFormulaResultType) {
        org.apache.poi.ss.usermodel.CellType.STRING -> cell.stringCellValue.trim()
        org.apache.poi.ss.usermodel.CellType.NUMERIC -> cell.numericCellValue.toString()
        org.apache.poi.ss.usermodel.CellType.BOOLEAN -> cell.booleanCellValue.toString()
        else -> null
      }
      else -> null
    }
  }

  private fun getDescriptionFromHyperlink(workbook: Workbook, row: Row): String? {
    val lookupText = getCellStringValue(row.getCell(0))?.trim()?.takeIf { it.isNotEmpty() } ?: return null // Column A on "another view"

    val sourceSheet = workbook.getSheet("ProbationDataDomains")
    if (sourceSheet != null) {
      val matchingCell = findCellByText(sourceSheet, lookupText, 1, 6) // Columns B-G
      if (matchingCell != null) {
        val hyperlink = matchingCell.hyperlink?.address
        if (!hyperlink.isNullOrBlank()) {
          logger.debug("Resolved hyperlink for '$lookupText' from ProbationDataDomains: $hyperlink")
          return resolveDescriptionFromHyperlink(workbook, hyperlink)
        }
        logger.warn("Matching cell for '$lookupText' found on ProbationDataDomains but none of the exact matches had a hyperlink")
      } else {
        logger.warn("No matching cell found on ProbationDataDomains for '$lookupText'")
      }
    } else {
      logger.warn("Sheet 'ProbationDataDomains' not found in workbook")
    }

    return null
  }

  private fun findCellByText(sheet: org.apache.poi.ss.usermodel.Sheet, text: String, startColumn: Int, endColumn: Int): Cell? {
    val normalizedLookup = text.trim()
    var firstExactMatch: Cell? = null

    for (rowIndex in sheet.firstRowNum..sheet.lastRowNum) {
      val candidateRow = sheet.getRow(rowIndex) ?: continue
      for (cellIndex in startColumn..endColumn) {
        val candidateCell = candidateRow.getCell(cellIndex) ?: continue
        val candidateText = getCellStringValue(candidateCell)?.trim() ?: continue
        if (candidateText == normalizedLookup) {
          if (firstExactMatch == null) {
            firstExactMatch = candidateCell
          }
          candidateCell.hyperlink?.let {
            if (it.address?.isNotBlank() == true && it.type == HyperlinkType.DOCUMENT) {
              logger.debug("Matched '$text' on sheet '${sheet.sheetName}' at row $rowIndex, col $cellIndex with hyperlink ${it.address}")
              return candidateCell
            }
          }
          logger.debug("Matched '$text' on sheet '${sheet.sheetName}' at row $rowIndex, col $cellIndex without valid hyperlink")
        }
      }
    }

    return firstExactMatch
  }

  private fun resolveDescriptionFromHyperlink(workbook: Workbook, hyperlink: String): String? {
    logger.debug("Hyperlink address: $hyperlink")

    val rawSheetName = hyperlink.substringBefore('!')
    val sheetName = rawSheetName.removeSurrounding("'")
    val cellReference = hyperlink.substringAfter('!')
    logger.debug("Parsed sheet name: '$sheetName' (raw: '$rawSheetName'), cell ref: '$cellReference'")

    val targetSheet = workbook.getSheet(sheetName)
    if (targetSheet == null) {
      logger.warn("Target sheet not found: '$sheetName'")
      return null
    }

    val ref = CellReference(cellReference)
    logger.debug("CellReference row: ${ref.row}, col: ${ref.col}")
    val targetRow = targetSheet.getRow(ref.row)
    if (targetRow == null) {
      logger.warn("Target row not found: ${ref.row} in sheet '$sheetName'")
      return null
    }
    val targetCell = targetRow.getCell(ref.col.toInt())
    if (targetCell == null) {
      logger.warn("Target cell not found: row ${ref.row}, col ${ref.col} in sheet '$sheetName'")
      return null
    }

    val description = getCellStringValue(targetCell)
    logger.debug("Retrieved description: '$description'")
    return description
  }
}
