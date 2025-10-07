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
            .failureRateThreshold(50f) //Sets error rate to open the circuit (50% failures).
            .slidingWindowSize(10) //Analyzes the last X calls to decide if the circuit opens.
            .minimumNumberOfCalls(5) //Sets minimum calls before the circuit starts evaluating.
            .waitDurationInOpenState(Duration.ofSeconds(10)) //Sets how long the circuit stays OPEN.
            .permittedNumberOfCallsInHalfOpenState(3) //Max calls allowed in Half-Open state.
            .recordException { e -> e is ServiceUnavailableException } //Specifies which exceptions to record.
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
        ) //Function with exponential backoff, setting wait time and multiplier.

        val config = RetryConfig.custom<Any>()
            .maxAttempts(3) //Sets the maximum number of attempts (3 in example).
            .intervalFunction(intervalFn) //Defines the wait interval between retries.
            .retryExceptions(ServiceUnavailableException::class.java) //Specifies which exceptions trigger a retry.
            .build()

        val retry = Retry.of("bookServiceRetry", config)

        retry.eventPublisher
            .onRetry { e -> println("### Call failed, retry #${e.numberOfRetryAttempts}") }
            .onSuccess { e -> println("### Retry succeeded after ${e.numberOfRetryAttempts} attempts") }

        return retry
    }
}
