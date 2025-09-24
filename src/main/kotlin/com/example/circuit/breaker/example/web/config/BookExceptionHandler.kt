package com.example.circuit.breaker.example.web.config

import com.example.circuit.breaker.example.exception.BusinessException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.lang.IllegalArgumentException

@ControllerAdvice
class BookExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBeanValidation(ex: IllegalArgumentException): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.message)
    }

    @ExceptionHandler(BusinessException::class)
    fun handleBeanValidation(ex: BusinessException): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.message)
    }
}
