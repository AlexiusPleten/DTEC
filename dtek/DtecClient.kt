package com.dtec.dtek

import androidx.annotation.WorkerThread
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class DtecClient {
    private val ENDPOINT = "https://www.dtek-oem.com.ua/ua/ajax"
    companion object{
        val tag = "----------DTEC----------"

        private fun getHtml():String {
            val httpURLConnection = URL("https://www.dtek-oem.com.ua/ua/shutdowns").openConnection() as HttpURLConnection
            httpURLConnection.apply {
                connectTimeout = 5000
                requestMethod = "GET"
                doInput = true
            }
            val streamReader = InputStreamReader(httpURLConnection.inputStream)
            return streamReader.readText()
        }

        public fun getAdressList(): String {
            val html = getHtml()
            val listMeta = "<script>DisconSchedule.streets = "
            val listPos = html.indexOf(listMeta) + listMeta.length
            val listEndPos = html.indexOf('\n', listPos)
            return html.substring(listPos, listEndPos)
        }
    }

    @WorkerThread
    fun request(csrf : String, adress: Adress): String{
        val httpURLConnection = URL(ENDPOINT).openConnection() as HttpURLConnection
        httpURLConnection.apply {
            connectTimeout = 5000
            requestMethod = "POST"
            doInput = true
            doOutput = true
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            setRequestProperty("X-CSRF-Token", csrf)
        }
        OutputStreamWriter(httpURLConnection.outputStream).use {
            it.write(Utils.adressToUrl(adress))
            it.flush()
            it.close()
        }
        android.util.Log.d(tag, httpURLConnection.responseCode.toString())
        if(httpURLConnection.responseCode != HttpURLConnection.HTTP_OK) {
            return ""
        }
        val streamReader = InputStreamReader(httpURLConnection.inputStream)
        var t = ""
        streamReader.use { t = it.readText() }
        httpURLConnection.disconnect()
        android.util.Log.d(tag, t)
        return t
    }

    @WorkerThread
    fun requestCSRF():String {
        return getCSRFToken(getHtml())
    }

    private fun getCSRFToken(html:String) : String {
        val csrfMeta = "<meta name=\"csrf-token\" content=\""
        val csrfPos = html.lastIndexOf(csrfMeta) + csrfMeta.length
        return html.substring(csrfPos, csrfPos + 88)
    }
}