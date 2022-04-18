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
        val response: HttpResponse = client.get("https://api.jokes.one/jod")
        println(response.bodyAsText().fromJson<JokeBase>())
        client.close()
    }
}

data class Success(val total: Number?)

data class JokeBase(val success: Success?, val contents: Contents?)

data class Contents(val jokes: List<Jokes>?, val copyright: String?)

data class Joke(
    val title: String?,
    val lang: String?,
    val length: String?,
    val clean: String?,
    val racial: String?,
    val id: String?,
    val text: String?
)

data class Jokes(
    val description: String?,
    val language: String?,
    val background: String?,
    val category: String?,
    val date: String?,
    val joke: Joke?
)