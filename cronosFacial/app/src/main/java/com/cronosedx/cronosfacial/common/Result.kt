package com.cronosedx.cronosfacial.common

/**
 * A sealed class representing the result of an operation.
 * 
 * This provides type-safe error handling throughout the application.
 * 
 * @param T The type of data returned on success
 */
sealed class Result<out T> {
    /**
     * Represents a successful operation with data
     */
    data class Success<T>(val data: T) : Result<T>()
    
    /**
     * Represents a failed operation with an error
     */
    data class Error(val exception: Exception, val message: String? = null) : Result<Nothing>()
    
    /**
     * Represents an operation in progress
     */
    data object Loading : Result<Nothing>()
    
    /**
     * Returns true if this is a Success result
     */
    val isSuccess: Boolean
        get() = this is Success
    
    /**
     * Returns true if this is an Error result
     */
    val isError: Boolean
        get() = this is Error
    
    /**
     * Returns true if this is a Loading result
     */
    val isLoading: Boolean
        get() = this is Loading
    
    /**
     * Returns the data if this is a Success, or null otherwise
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
    
    /**
     * Returns the data if this is a Success, or throws the exception if Error
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
        is Loading -> throw IllegalStateException("Cannot get data from Loading state")
    }
}

/**
 * Extension function to map a Result's success value
 */
inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> {
    return when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> this
        is Result.Loading -> this
    }
}

/**
 * Extension function to perform an action on success
 */
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) {
        action(data)
    }
    return this
}

/**
 * Extension function to perform an action on error
 */
inline fun <T> Result<T>.onError(action: (Exception) -> Unit): Result<T> {
    if (this is Result.Error) {
        action(exception)
    }
    return this
}
