package com.programmersbox.composefun

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() = runBlocking {
        val client = HttpClient()
        val response: HttpResponse = client.get("https://raw.githubusercontent.com/phunware-services/dev-interview-homework/master/feed.json")
        println(response.bodyAsText())
        client.close()
    }

    @Test
    fun jokeTest() = runBlocking {
        val client = HttpClient()
        val response: HttpResponse = client.get("https://icanhazdadjoke.com/") {
            headers { append("Accept", "application/json") }
        }
        println(response.bodyAsText().fromJson<DadJoke>())
        client.close()
    }
}
