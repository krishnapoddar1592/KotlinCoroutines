package com.example.testapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.testapp.ui.theme.TestAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TestAppTheme {
                Text(text = "abcd")
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }


        //Use cases of the util
        // Use Case 1: Updating UI after a network call
        CoroutineUtils.launchOnIO {
            // This runs on IO thread, doesn't block UI
//            val result = api.fetchData()
            CoroutineUtils.withMainContext {
                // This switches to Main thread, blocks only this coroutine
//                updateUI(result)
            }
        }

        // Use Case 2: Parallel network requests
        val deferredResult1 = CoroutineUtils.asyncOnIO {
//            api.fetchData1()
        }
        val deferredResult2 = CoroutineUtils.asyncOnIO {
//            api.fetchData2()
        }
        // Later, in a coroutine scope:
//        val result1 = deferredResult1.await() // This will block the coroutine until result is available
//        val result2 = deferredResult2.await() // This will block the coroutine until result is available

        // Use Case 3: Retry a network call
        CoroutineUtils.launchOnIO {
            try {
                val result = CoroutineUtils.retryIO {
                    // This will retry up to 3 times with exponential backoff
//                    api.fetchDataThatMightFail()
                }
                // Process result
            } catch (e: Exception) {
                // Handle final failure
            }
        }

        // Use Case 4: Perform a task with a timeout
        CoroutineUtils.launchOnIO {
            try {
                val result = CoroutineUtils.withTimeout(5000) {
                    // This will throw TimeoutCancellationException if it takes more than 5 seconds
//                    api.fetchDataWithPossibleDelay()
                }
                // Process result
            } catch (e: TimeoutCancellationException) {
                // Handle timeout
            }
        }

        // Use Case 5: Cleanup when leaving a screen
//        class MyViewModel : ViewModel() {
//            override fun onCleared() {
//                super.onCleared()
//                CoroutineUtils.cancelAllCoroutines()
//            }
//        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TestAppTheme {
        Greeting("Android")
    }
}