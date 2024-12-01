package com.example.myapplication.util.network

import fi.iki.elonen.NanoHTTPD
import java.io.File

class MyHttpServer(context: android.content.Context, port: Int) : NanoHTTPD(port) {
    private val context: android.content.Context = context

    override fun serve(session: IHTTPSession?): Response {
        val uri = session?.uri
        return if (uri != null) {
            when {
                uri == "/index.html" -> {
                    // Serve the HTML file
                    val html = context.assets.open("index.html").bufferedReader().use { it.readText() }
                    newFixedLengthResponse(Response.Status.OK, "text/html", html)
                }
                uri.startsWith("/mp3/") -> {
                    // Serve MP3 file dynamically
                    val fileName = uri.substring(5)  // Extract file name after "/mp3/"
                    val musicFile = File(context.getExternalFilesDir(null), fileName)
                    if (musicFile.exists()) {
                        // Read the MP3 file into a byte array
                        val mp3Bytes = musicFile.readBytes()
                        // Convert the byte array to an InputStream
                        val mp3InputStream = mp3Bytes.inputStream()
                        // Return the InputStream as the response with the appropriate MIME type for MP3
                        val response = newFixedLengthResponse(
                            Response.Status.OK,
                            "audio/mpeg",
                            mp3InputStream, mp3Bytes.size.toLong()
                        )
                        response.addHeader("Access-Control-Allow-Origin", "null")
                        response.addHeader("Access-Control-Allow-Methods", "GET, OPTIONS")
                        response.addHeader("Access-Control-Allow-Headers", "Content-Type")
                        response
                    } else {
                        // Return 404 if the file doesn't exist
                        newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "File not found")
                    }
                }
                else -> {
                    // Return 404 for any other URI that doesn't match
                    newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found")
                }
            }
        } else {
            // Return a 400 if the URI is null
            newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Bad Request")
        }
    }


}
