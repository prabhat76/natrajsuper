package com.example.natraj.util.error

import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

enum class ErrorType {
    NETWORK_ERROR,
    TIMEOUT_ERROR,
    SERVER_ERROR,
    AUTHENTICATION_ERROR,
    VALIDATION_ERROR,
    NOT_FOUND_ERROR,
    UNKNOWN_ERROR,
    ORDER_CREATION_FAILED,
    PAYMENT_FAILED,
    INVALID_RESPONSE,
    NO_INTERNET
}

data class ErrorMessage(
    val title: String,
    val message: String,
    val actionText: String = "Retry"
)

object AppErrorHandler {
    
    private val errorMessages = mapOf(
        ErrorType.NETWORK_ERROR to ErrorMessage(
            title = "Connection Problem",
            message = "Unable to connect to the server. Please check your internet connection and try again.",
            actionText = "Retry"
        ),
        ErrorType.TIMEOUT_ERROR to ErrorMessage(
            title = "Request Timed Out",
            message = "The server took too long to respond. Please try again.",
            actionText = "Retry"
        ),
        ErrorType.SERVER_ERROR to ErrorMessage(
            title = "Server Error",
            message = "Something went wrong on our end. Our team has been notified and we're working to fix it.",
            actionText = "Try Again"
        ),
        ErrorType.AUTHENTICATION_ERROR to ErrorMessage(
            title = "Authentication Failed",
            message = "Your session has expired. Please login again to continue.",
            actionText = "Login"
        ),
        ErrorType.VALIDATION_ERROR to ErrorMessage(
            title = "Invalid Information",
            message = "Please check your details and try again.",
            actionText = "Go Back"
        ),
        ErrorType.NOT_FOUND_ERROR to ErrorMessage(
            title = "Not Found",
            message = "The requested resource could not be found.",
            actionText = "Go Back"
        ),
        ErrorType.ORDER_CREATION_FAILED to ErrorMessage(
            title = "Order Failed",
            message = "We couldn't process your order. Please try again or contact support if the problem persists.",
            actionText = "Retry"
        ),
        ErrorType.PAYMENT_FAILED to ErrorMessage(
            title = "Payment Failed",
            message = "Your payment could not be processed. Please check your payment details and try again.",
            actionText = "Try Again"
        ),
        ErrorType.INVALID_RESPONSE to ErrorMessage(
            title = "Invalid Response",
            message = "We received an unexpected response from the server. Please try again.",
            actionText = "Retry"
        ),
        ErrorType.NO_INTERNET to ErrorMessage(
            title = "No Internet Connection",
            message = "Please check your internet connection and try again.",
            actionText = "Retry"
        ),
        ErrorType.UNKNOWN_ERROR to ErrorMessage(
            title = "Something Went Wrong",
            message = "An unexpected error occurred. Please try again later.",
            actionText = "Retry"
        )
    )
    
    fun getErrorMessage(errorType: ErrorType): ErrorMessage {
        return errorMessages[errorType] ?: errorMessages[ErrorType.UNKNOWN_ERROR]!!
    }
    
    fun fromException(exception: Exception): ErrorType {
        return when (exception) {
            is UnknownHostException -> ErrorType.NO_INTERNET
            is SocketTimeoutException -> ErrorType.TIMEOUT_ERROR
            is IOException -> ErrorType.NETWORK_ERROR
            is HttpException -> {
                when (exception.code()) {
                    401, 403 -> ErrorType.AUTHENTICATION_ERROR
                    404 -> ErrorType.NOT_FOUND_ERROR
                    400, 422 -> ErrorType.VALIDATION_ERROR
                    in 500..599 -> ErrorType.SERVER_ERROR
                    else -> ErrorType.UNKNOWN_ERROR
                }
            }
            else -> {
                // Check exception message for specific errors
                val message = exception.message?.lowercase() ?: ""
                when {
                    message.contains("timeout") -> ErrorType.TIMEOUT_ERROR
                    message.contains("network") -> ErrorType.NETWORK_ERROR
                    message.contains("auth") -> ErrorType.AUTHENTICATION_ERROR
                    message.contains("order") -> ErrorType.ORDER_CREATION_FAILED
                    message.contains("payment") -> ErrorType.PAYMENT_FAILED
                    else -> ErrorType.UNKNOWN_ERROR
                }
            }
        }
    }
    
    fun getHttpErrorType(statusCode: Int): ErrorType {
        return when (statusCode) {
            401, 403 -> ErrorType.AUTHENTICATION_ERROR
            404 -> ErrorType.NOT_FOUND_ERROR
            400, 422 -> ErrorType.VALIDATION_ERROR
            in 500..599 -> ErrorType.SERVER_ERROR
            408 -> ErrorType.TIMEOUT_ERROR
            else -> ErrorType.UNKNOWN_ERROR
        }
    }
}
