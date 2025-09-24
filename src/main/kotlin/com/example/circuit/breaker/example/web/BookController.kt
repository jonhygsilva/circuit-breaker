package com.example.circuit.breaker.example.web

import com.example.circuit.breaker.example.service.BookService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class BookController(private val bookService: BookService) {

    @GetMapping("/books/recommendations")
    fun getRecommendations(): List<String> {
        return bookService.getRecommendations()
    }
}
