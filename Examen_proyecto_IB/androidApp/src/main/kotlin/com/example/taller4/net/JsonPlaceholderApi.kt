package com.example.taller4.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Cliente simple para JSONPlaceholder usando HttpURLConnection.
 * - GET: lee un post por id.
 * - PUT: envía un JSON actualizado.
 */
object JsonPlaceholderApi {
    private const val BASE_URL = "https://jsonplaceholder.typicode.com"

    suspend fun getPost(id: Int): Result<ApiPost> = withContext(Dispatchers.IO) {
        try {
            // Aquí se hace la petición GET.
            val url = URL("$BASE_URL/posts/$id")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 15000
            }

            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = stream.bufferedReader().use(BufferedReader::readText)

            if (code in 200..299) {
                // Aquí se parsea el JSON.
                val json = JSONObject(body)
                Result.success(
                    ApiPost(
                        id = json.optInt("id"),
                        title = json.optString("title"),
                        body = json.optString("body"),
                    )
                )
            } else {
                Result.failure(IllegalStateException("HTTP $code"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePost(post: ApiPost): Result<ApiPost> = withContext(Dispatchers.IO) {
        try {
            // Aquí se hace la petición PUT.
            val url = URL("$BASE_URL/posts/${post.id}")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "PUT"
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                connectTimeout = 15000
                readTimeout = 15000
            }

            val payload = JSONObject().apply {
                put("id", post.id)
                put("title", post.title)
                put("body", post.body)
                put("userId", 1)
            }

            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(payload.toString())
                writer.flush()
            }

            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val response = stream.bufferedReader().use(BufferedReader::readText)

            if (code in 200..299) {
                val json = JSONObject(response)
                Result.success(
                    ApiPost(
                        id = json.optInt("id", post.id),
                        title = json.optString("title", post.title),
                        body = json.optString("body", post.body),
                    )
                )
            } else {
                Result.failure(IllegalStateException("HTTP $code"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class ApiPost(
    val id: Int,
    val title: String,
    val body: String,
)

