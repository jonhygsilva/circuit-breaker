package com.example.circuit.breaker.example

import com.example.circuit.breaker.example.exception.BusinessException
import com.example.circuit.breaker.example.exception.ServiceUnavailableException
import com.example.circuit.breaker.example.configuration.Resilience4jConfig
import com.example.circuit.breaker.example.service.BookRemoteService
import com.example.circuit.breaker.example.service.BookService
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.retry.Retry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import java.time.Duration

class BookServiceTest {

    private lateinit var remoteService: BookRemoteService
    private lateinit var bookService: BookService
    private lateinit var bookCircuitBreaker: CircuitBreaker
    private lateinit var bookRetry: Retry

    @BeforeEach
    fun setUp() {
        remoteService = mock(BookRemoteService::class.java)
        val resilienceConfig = Resilience4jConfig()
        bookCircuitBreaker = resilienceConfig.bookCircuitBreaker()
        bookRetry = resilienceConfig.bookRetry()
        bookService = BookService(
            remoteService = remoteService,
            bookCircuitBreaker = bookCircuitBreaker,
            bookRetry = bookRetry
        )
    }

    @Test
    @DisplayName("Success Case: Should return the book list on first attempt")
    fun `should return recommendations on first attempt`() {
        val expectedBooks = listOf("Clean Code", "Effective Java")
        `when`(remoteService.getRecommendations()).thenReturn(expectedBooks)

        val result = bookService.getRecommendations()

        assertEquals(expectedBooks, result)
        verify(remoteService, times(1)).getRecommendations()
    }

    @Test
    @DisplayName("Retry Case: Should fail once and succeed on retry")
    fun `should fail and succeed on retry`() {
        `when`(remoteService.getRecommendations())
            .thenThrow(ServiceUnavailableException("Service temporarily unavailable"))
            .thenReturn(listOf("Domain-Driven Design", "Kotlin in Action"))

        val result = bookService.getRecommendations()
        val expectedBooks = listOf("Domain-Driven Design", "Kotlin in Action")

        assertEquals(expectedBooks, result)
        verify(remoteService, times(2)).getRecommendations()
    }

    @Test
    @DisplayName("Fallback Case: Should fail all retry attempts and trigger fallback")
    fun `should fail all retries and trigger fallback`() {
        `when`(remoteService.getRecommendations()).thenThrow(ServiceUnavailableException("Service unavailable"))

        val result = bookService.getRecommendations()
        val expectedFallbackBooks = listOf("Default Book 1", "Default Book 2")

        assertEquals(expectedFallbackBooks, result)
        verify(remoteService, times(3)).getRecommendations()
    }

    @Test
    @DisplayName("Business Exception Case: Should not retry and throw original exception")
    fun `should not retry on business exception`() {
        `when`(remoteService.getRecommendations()).thenThrow(BusinessException("Business rule violated"))

        assertThrows(BusinessException::class.java) {
            bookService.getRecommendations()
        }

        verify(remoteService, times(1)).getRecommendations()
    }

    @Test
    @DisplayName("Circuit Breaker OPEN Case: Should open the circuit and go straight to fallback")
    fun `should transition to open state and trigger fallback`() {
        `when`(remoteService.getRecommendations()).thenThrow(ServiceUnavailableException("Service unavailable"))

        repeat(5) {
            bookService.getRecommendations()
        }

        val result = bookService.getRecommendations()
        val expectedFallbackBooks = listOf("Default Book 1", "Default Book 2")

        assertEquals(expectedFallbackBooks, result)
        verify(remoteService, times(15)).getRecommendations()
    }

    @Test
    @DisplayName("Circuit Breaker HALF_OPEN Case: Should close the circuit after some successful attempts")
    fun `should transition to half-open and then close on success`() {
        `when`(remoteService.getRecommendations()).thenThrow(ServiceUnavailableException("Service unavailable"))

        repeat(5) {
            bookService.getRecommendations()
        }

        reset(remoteService)

        val shortWaitCircuitBreaker = CircuitBreaker.of(
            "testCircuitBreaker",
            CircuitBreakerConfig.custom()
                .failureRateThreshold(50f)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .waitDurationInOpenState(Duration.ofMillis(100))
                .permittedNumberOfCallsInHalfOpenState(3)
                .recordException { e -> e is ServiceUnavailableException }
                .build()
        )

        val shortWaitService = BookService(remoteService, shortWaitCircuitBreaker, bookRetry)

        Thread.sleep(150)

        `when`(remoteService.getRecommendations())
            .thenReturn(listOf("Success 1"))
            .thenReturn(listOf("Success 2"))
            .thenReturn(listOf("Success 3"))
            .thenReturn(listOf("Success 4"))

        shortWaitService.getRecommendations()
        shortWaitService.getRecommendations()
        shortWaitService.getRecommendations()
        val result = shortWaitService.getRecommendations()

        assertEquals(listOf("Success 4"), result)
    }
}
