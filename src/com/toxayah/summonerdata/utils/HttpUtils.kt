package com.toxayah.summonerdata.utils

import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class HttpResponse(val code: Int, val responseMessage: String, val headers: Map<String, String>, val body: ByteArray) {

    fun getBodyStr(): String {
        return String(body)
    }
}

fun httpRequest(target: String, method: String = "GET", headers: Map<String, String> = HashMap(), bdy: ByteArray? = null): HttpResponse {
        val connection = if (target.startsWith("https"))
        URL(target).openConnection() as HttpsURLConnection
    else
        URL(target).openConnection() as HttpURLConnection

    connection.requestMethod = method
    connection.instanceFollowRedirects = true
    connection.doInput = true
    connection.doOutput = true
    connection.setRequestProperty("User-Agent", "21xayah.com / indexer")
    connection.setRequestProperty("Pragma", "no-cache")
    connection.setRequestProperty("Accept-Language", "en-GB,en;q=0.9,de-DE;q=0.8,de;q=0.7,en-US;q=0.6")
    connection.setRequestProperty("Cache-Control", "no-cache")
    for ((k, v) in headers) connection.setRequestProperty(k, v)
    if (method != "GET" && bdy != null) {
        connection.outputStream.write(bdy)
        connection.outputStream.flush()
    }
    val code = connection.responseCode
    val msg = connection.responseMessage
    val headers = connection.headerFields
    val inStream = if (code == 200)
        connection.inputStream
    else
        connection.errorStream
    val headersMap = HashMap<String, String>()
    val byteStream = ByteArrayOutputStream()
    inStream.copyTo(byteStream)

    headers.forEach { key ->
        key.value.forEach {

            headersMap[if (key.key == null) "Baseline_req" else key.key] = it
        }
    }
    return HttpResponse(code, msg, headersMap, byteStream.toByteArray())
}