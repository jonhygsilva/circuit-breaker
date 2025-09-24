package com.example.circuit.breaker.example.service

import com.example.circuit.breaker.example.exception.ServiceUnavailableException
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.retry.Retry
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Service responsible for retrieving book recommendations with resilience patterns (Circuit Breaker and Retry).
 */
@Service
class BookService(
    private val remoteService: BookRemoteService,
    private val bookCircuitBreaker: CircuitBreaker,
    private val bookRetry: Retry
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    companion object {
        private val DEFAULT_BOOKS = listOf("Default Book 1", "Default Book 2")
    }

    /**
     * Gets book recommendations from the remote service, using circuit breaker and retry.
     * If the service is unavailable, returns default recommendations.
     */
    fun getRecommendations(): List<String> {
        val decoratedSupplier = CircuitBreaker.decorateSupplier(
            bookCircuitBreaker,
            Retry.decorateSupplier(
                bookRetry
            ) { remoteService.getRecommendations() }
        )

        return try {
            decoratedSupplier.get()
        } catch (ex: Exception) {
            when (ex) {
                is ServiceUnavailableException,
                is CallNotPermittedException -> fallback()
                else -> throw ex
            }
        }
    }

    /**
     * Fallback method for book recommendations when remote service is unavailable.
     */
    private fun fallback(): List<String> {
        logger.warn("Fallback triggered!")
        return DEFAULT_BOOKS
    }
}
