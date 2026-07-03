package uk.gov.justice.digital.hmpps.domainexplorerapi.config

import io.swagger.v3.oas.models.Operation
import org.springdoc.core.customizers.OperationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.HandlerMethod

@Configuration
class OpenApiOperationIdCustomizer {
  @Bean
  fun operationIdCustomizer(): OperationCustomizer = OperationCustomizer { operation: Operation, handlerMethod: HandlerMethod ->
    // Build a stable, unique operationId: <ControllerName><MethodName>
    val controller = handlerMethod.beanType.simpleName.removeSuffix("Controller")
    val methodName = handlerMethod.method.name
    // Capitalize first letter of method for readability
    val method = methodName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    val opId = controller + method
    operation.operationId = opId
    operation
  }
}
