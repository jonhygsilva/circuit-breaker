package com.example.circuit.breaker.example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CircuitBreakerApplication

fun main(args: Array<String>) {
	runApplication<CircuitBreakerApplication>(*args)
}
