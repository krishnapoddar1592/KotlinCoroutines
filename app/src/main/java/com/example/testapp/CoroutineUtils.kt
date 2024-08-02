package com.example.testapp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CoroutineUtils {
    companion object {
        private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        /**
         * Launches a new coroutine on the Main dispatcher without blocking the current thread.
         * Use this for UI-related tasks.
         *
         * @param block The suspend function to execute
         * @return A Job that can be used to cancel the coroutine
         */
        fun launchOnMain(block: suspend CoroutineScope.() -> Unit): Job {
            return mainScope.launch { block() }
        }

        /**
         * Launches a new coroutine on the IO dispatcher without blocking the current thread.
         * Use this for network calls, database operations, or other I/O-intensive tasks.
         *
         * @param block The suspend function to execute
         * @return A Job that can be used to cancel the coroutine
         */
        fun launchOnIO(block: suspend CoroutineScope.() -> Unit): Job {
            return ioScope.launch { block() }
        }

        /**
         * Switches the context to the IO dispatcher and executes the given block.
         * This function is suspend and will block the calling coroutine until completion.
         * Use this when you need to perform I/O operations within an existing coroutine.
         *
         * @param block The suspend function to execute on the IO dispatcher
         * @return The result of the block execution
         */
        suspend fun <T> withIOContext(block: suspend CoroutineScope.() -> T): T {
            return withContext(Dispatchers.IO) { block() }
        }

        /**
         * Switches the context to the Main dispatcher and executes the given block.
         * This function is suspend and will block the calling coroutine until completion.
         * Use this when you need to perform UI updates from a background coroutine.
         *
         * @param block The suspend function to execute on the Main dispatcher
         * @return The result of the block execution
         */
        suspend fun <T> withMainContext(block: suspend CoroutineScope.() -> T): T {
            return withContext(Dispatchers.Main) { block() }
        }

        /**
         * Starts an asynchronous operation on the IO dispatcher without blocking.
         * The operation can be awaited later.
         * Use this when you want to start a task but don't need the result immediately.
         *
         * @param block The suspend function to execute asynchronously
         * @return A Deferred object that can be awaited for the result
         */
        fun <T> asyncOnIO(block: suspend CoroutineScope.() -> T): Deferred<T> {
            return ioScope.async { block() }
        }

        /**
         * Retries an IO operation with exponential backoff.
         * This function is suspend and will block the calling coroutine until all retries are exhausted or successful.
         * Use this for network calls or other operations that might fail temporarily.
         *
         * @param times Number of retry attempts
         * @param initialDelay Initial delay before the first retry (in milliseconds)
         * @param maxDelay Maximum delay between retries (in milliseconds)
         * @param factor Multiplication factor for exponential backoff
         * @param block The suspend function to retry
         * @return The result of the successful execution
         * @throws Exception if all retry attempts fail
         */
        suspend fun <T> retryIO(
            times: Int = 3,
            initialDelay: Long = 100,
            maxDelay: Long = 1000,
            factor: Double = 2.0,
            block: suspend () -> T
        ): T {
            var currentDelay = initialDelay
            repeat(times - 1) {
                try {
                    return block()
                } catch (e: Exception) {
                    // You can add logging here
                }
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
            }
            return block() // last attempt
        }

        /**
         * Executes a given suspend function with a specified timeout.
         * This function is suspend and will block the calling coroutine until completion or timeout.
         * Use this when you want to limit the execution time of an operation.
         *
         * @param timeMillis Timeout duration in milliseconds
         * @param block The suspend function to execute with a timeout
         * @return The result of the block execution
         * @throws TimeoutCancellationException if the timeout is exceeded
         */
        suspend fun <T> withTimeout(timeMillis: Long, block: suspend CoroutineScope.() -> T): T {
            return kotlinx.coroutines.withTimeout(timeMillis) { block() }
        }

        /**
         * Cancels all running coroutines in both main and IO scopes.
         * This does not block and returns immediately.
         * Use this for cleanup, like in onDestroy() of an Activity or ViewModel.
         */
        fun cancelAllCoroutines() {
            mainScope.coroutineContext.cancelChildren()
            ioScope.coroutineContext.cancelChildren()
        }
    }
}