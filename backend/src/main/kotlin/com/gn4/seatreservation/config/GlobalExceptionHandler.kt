package com.gn4.seatreservation.config

import com.gn4.seatreservation.dto.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(NoSuchElementException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFound(ex: NoSuchElementException): ErrorResponse {
        log.info("404 Not Found: {}", ex.message)
        return ErrorResponse(ErrorCodes.NOT_FOUND, ex.message ?: "Not found")
    }

    @ExceptionHandler(SeatConflictException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleConflict(ex: SeatConflictException): ErrorResponse {
        log.info("409 Conflict [{}]: {}", ex.code, ex.message)
        return ErrorResponse(ex.code, ex.message)
    }

    @ExceptionHandler(
        MethodArgumentNotValidException::class,
        BindException::class,
        MissingRequestHeaderException::class,
        IllegalArgumentException::class,
    )
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleBadRequest(ex: Exception): ErrorResponse {
        val msg =
            when (ex) {
                is MethodArgumentNotValidException ->
                    ex.bindingResult.allErrors.firstOrNull()?.defaultMessage ?: "Validation failed"

                is BindException ->
                    ex.bindingResult.allErrors.firstOrNull()?.defaultMessage ?: "Validation failed"

                is MissingRequestHeaderException ->
                    "Missing header: ${ex.headerName}"

                else -> ex.message ?: "Bad request"
            }
        log.info("400 Bad Request: {}", msg)
        return ErrorResponse(ErrorCodes.BAD_REQUEST, msg)
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleUnknown(ex: Exception): ErrorResponse {
        log.error("500 Internal Server Error", ex)
        return ErrorResponse("INTERNAL_SERVER_ERROR", "Unexpected error")
    }
}
