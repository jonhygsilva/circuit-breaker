package com.example.circuit.breaker.example.service

import com.example.circuit.breaker.example.exception.BusinessException
import com.example.circuit.breaker.example.exception.ServiceUnavailableException
import org.springframework.stereotype.Service

@Service
class BookRemoteService {

    fun getRecommendations(): List<String> {
        return listOf("Domain-Driven Design", "Kotlin in Action")
    }
}
