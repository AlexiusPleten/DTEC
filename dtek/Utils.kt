package com.dtec.dtek

import android.content.Context
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileWriter
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors
import java.util.stream.Stream

//Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
class Utils {
    companion object {
        fun toJsonFile(context: Context, name: String, adress: Adress) {
            val FILE_NAME = "$name.json"
            val json = Json.encodeToString(adress)
            val fileWriter = FileWriter(File(context.filesDir.absolutePath + "/" + FILE_NAME))
            fileWriter.append(json)
            fileWriter.flush()
            fileWriter.close()
        }

        fun getFiles(context: Context): Set<String>? {
            val dir = File(context.filesDir.absolutePath)
            if (isDirEmpty(dir)) {
                return Stream.of<File>(*File(dir.absolutePath).listFiles())
                    .filter { file: File -> !file.isDirectory }
                    .map<String> { obj: File -> obj.name }
                    .collect(Collectors.toSet<String>())
            } else return null
        }

        private fun isDirEmpty(dir:File): Boolean {
            if (dir.isDirectory) {
                val files = dir.listFiles()
                return (files != null && files.isNotEmpty())
            } else {
                return false
            }
        }

        fun readFromFile(context: Context, fileName: String): String {
            var str: String = ""
            File(context.filesDir.absolutePath + "/" +fileName).forEachLine { str += it }
            return str
        }

        fun fromJsonToObject(json: String): Adress {
            return Json.decodeFromString<Adress>(json)
        }

        fun checkFileExtensionForJson(fileName: String): Boolean {
            return File(fileName).extension.equals("json")
        }

        fun removeFile(context: Context, fileName: String) {
            val file = File(context.filesDir.absolutePath + "/" + fileName)
            if(file.exists()) { file.delete() }
        }

        fun adressToUrl(adress: Adress):String {
            val method = "method=getHomeNum"
            val city = "&data%5B0%5D%5Bname%5D=city&data%5B0%5D%5Bvalue%5D=" +
                    URLEncoder.encode(adress.city, StandardCharsets.UTF_8.toString())
            val street = "&data%5B1%5D%5Bname%5D=street&data%5B1%5D%5Bvalue%5D=" +
                    URLEncoder.encode(adress.street, StandardCharsets.UTF_8.toString())

            return method + city + street
        }
    }
}