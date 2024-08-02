import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.testapp.R
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        // Define a custom exception handler
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.e("MainActivity", "Caught exception: $throwable")
        }

        // Launch a coroutine on the Main dispatcher (UI thread)
        lifecycleScope.launch(Dispatchers.Main + exceptionHandler) {
            // Perform UI updates here
            updateUI()
        }

        // Launch a coroutine on the IO dispatcher for background work
        lifecycleScope.launch(Dispatchers.IO + exceptionHandler) {
            // Perform network call or database operation
            val result = fetchDataFromNetwork()

            // Switch to Main dispatcher to update UI
            withContext(Dispatchers.Main) {
                displayResult(result)
            }
        }

        // Start an asynchronous operation
        val deferred = lifecycleScope.async(Dispatchers.IO + exceptionHandler) {
            // Perform some computation or I/O operation
            computeResult()
        }

        // Launch another coroutine to wait for the async result
        lifecycleScope.launch {
            try {
                val result = deferred.await()
                // Use the result
            } catch (e: Exception) {
                // Handle any exceptions
            }
        }

        // Coroutine with timeout
        lifecycleScope.launch {
            try {
                withTimeout(5000L) {
                    // This block will be cancelled if it takes more than 5 seconds
                    val result = fetchDataWithPossibleDelay()
                    processResult(result)
                }
            } catch (e: TimeoutCancellationException) {
                // Handle timeout
            }
        }

        // Retry an operation with exponential backoff
        lifecycleScope.launch {
            val result = retry(times = 3, initialDelay = 100, maxDelay = 1000, factor = 2.0) {
                // Attempt the operation that might fail
                fetchDataThatMightFail()
            }
            // Use the result
        }
    }

    // Example of a suspend function that might be called from a coroutine
    suspend fun fetchDataFromNetwork(): String {
        delay(1000) // Simulate network delay
        return "Data from network"
    }

    // Implementation of retry logic
    suspend fun <T> retry(
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
                // You might want to log the exception here
            }
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
        }
        return block() // last attempt
    }

    // Other suspend functions...
    suspend fun updateUI() { /* ... */ }
    suspend fun computeResult(): Int { /* ... */ return 0}
    suspend fun fetchDataWithPossibleDelay(): String { /* ... */ return ""}
    suspend fun processResult(result: String) { /* ... */ }
    suspend fun fetchDataThatMightFail(): String { /* ... */ return ""}
    fun displayResult(result: String) { /* ... */ }
}