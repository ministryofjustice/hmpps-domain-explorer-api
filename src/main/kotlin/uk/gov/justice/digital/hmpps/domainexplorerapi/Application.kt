package uk.gov.justice.digital.hmpps.domainexplorerapi

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class DomainExplorerApi

private val logger = LoggerFactory.getLogger("Application")

fun main(args: Array<String>) {
  val isStandalone = System.getProperty("spring.profiles.active", "").contains("standalone")
  val isMac = System.getProperty("os.name", "").lowercase().contains("mac")

  if (isStandalone && isMac) {
    // Must be set before any AWT class is loaded.
    // This creates a persistent NSApplication so the Dock icon stays visible
    // for the lifetime of the process and macOS treats it as a foreground app.
    System.setProperty("java.awt.headless", "false")
  }

  if (isStandalone) {
    handleMacOsFirewall()
  }
  runApplication<DomainExplorerApi>(*args)
}

/**
 * On macOS, the Application Firewall may show a permission dialog when the
 * embedded server calls listen().  Two problems occur with an unsigned app:
 *
 *  1. The dialog appears behind other windows and is hard to find.
 *  2. The approval is not remembered between launches because the app has no
 *     stable code identity.
 *
 * This function runs a background daemon thread that:
 *  a) Attempts to pre-authorise the executable via socketfilterfw (no sudo
 *     required for the currently-running process on modern macOS – if it works
 *     the dialog is skipped entirely).
 *  b) Repeatedly activates the process via osascript for the first 20 seconds
 *     so any pending system dialog is forced to the foreground.
 */
private fun handleMacOsFirewall() {
  if (!System.getProperty("os.name", "").lowercase().contains("mac")) return

  Thread {
    // (a) Best-effort pre-authorisation – suppresses the dialog if successful
    try {
      val exe = ProcessHandle.current().info().command().orElse(null)
      if (exe != null) {
        val fw = "/usr/libexec/ApplicationFirewall/socketfilterfw"
        Runtime.getRuntime().exec(arrayOf(fw, "--add", exe)).waitFor()
        Runtime.getRuntime().exec(arrayOf(fw, "--unblockapp", exe)).waitFor()
      }
    } catch (_: Exception) {}

    // (b) Keep bringing the process to the front so any dialog is visible.
    // Runs for 20 s – long enough to cover Tomcat's bind/listen sequence.
    val deadline = System.currentTimeMillis() + 20_000
    while (System.currentTimeMillis() < deadline) {
      try {
        Runtime.getRuntime().exec(
          arrayOf(
            "osascript",
            "-e",
            """tell application "System Events" to set frontmost of process "ProbationDataExplorer" to true""",
          ),
        )
      } catch (_: Exception) {}
      Thread.sleep(1_500)
    }
  }.also { it.isDaemon = true }.start()
}

@Bean
fun init(): CommandLineRunner = CommandLineRunner {
  logger.info("=".repeat(80))
  logger.info("Data Explorer Application has started successfully")
  logger.info("REST API available at http://localhost:8080/api")
  logger.info("Swagger documentation at http://localhost:8080/api/swagger-ui.html")
  logger.info("=".repeat(80))
}
