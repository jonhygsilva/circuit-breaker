package com.example.circuit.breaker.example.configuration

import com.example.circuit.breaker.example.exception.ServiceUnavailableException
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.core.IntervalFunction
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class Resilience4jConfig {

    @Bean
    fun bookCircuitBreaker(): CircuitBreaker {
        val config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50f)
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .waitDurationInOpenState(Duration.ofSeconds(10))
            .permittedNumberOfCallsInHalfOpenState(3)
            .recordException { e -> e is ServiceUnavailableException }
            .build()

        val circuitBreaker = CircuitBreaker.of("bookServiceCircuitBreaker", config)

        circuitBreaker.eventPublisher
            .onStateTransition { e -> println("### CircuitBreaker: ${e.stateTransition}") }
            .onFailureRateExceeded { e -> println("### Failure rate exceeded: ${e.failureRate}%") }
            .onCallNotPermitted { println("### Call blocked by CircuitBreaker") }

        return circuitBreaker
    }

    @Bean
    fun bookRetry(): Retry {
        val intervalFn = IntervalFunction.ofExponentialBackoff(
            500,
            2.0
        )

        val config = RetryConfig.custom<Any>()
            .maxAttempts(3)
            .intervalFunction(intervalFn)
            .retryExceptions(ServiceUnavailableException::class.java)
            .build()

        val retry = Retry.of("bookServiceRetry", config)

        retry.eventPublisher
            .onRetry { e -> println("### Call failed, retry #${e.numberOfRetryAttempts}") }
            .onSuccess { e -> println("### Retry succeeded after ${e.numberOfRetryAttempts} attempts") }

        return retry
    }
}
