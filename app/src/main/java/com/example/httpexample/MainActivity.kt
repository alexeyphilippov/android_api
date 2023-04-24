package com.example.httpexample

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

private const val ENDPOINT = "http://10.0.2.2:8011"  // Im using json-server running on my localhost and emulator
private const val BOOKS_URI = "/add_caller/"
private const val TITLE = "title"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button.setOnClickListener {
            val token = editText.text.toString()
            val ip = editText3.text.toString()
            val phone_number = editText4.text.toString()

            Thread {
                addBook(token=token, ip=ip, phone_number=phone_number)
            }.start()
        }
        Thread {
            getBooksAndShowIt()
        }.start()
    }

    @WorkerThread
    fun getBooksAndShowIt() {
        val httpUrlConnection = URL(ENDPOINT + "/").openConnection() as HttpURLConnection
        httpUrlConnection.apply {
            connectTimeout = 100 // 10 seconds
            requestMethod = "GET"
            doInput = true
        }
        if (httpUrlConnection.responseCode != HttpURLConnection.HTTP_OK) {
            // show error toast
            return
        }
        val streamReader = InputStreamReader(httpUrlConnection.inputStream)
        var text: String = ""
        streamReader.use {
            text = it.readText()
        }

//        val books = mutableListOf<String>()
//        val json = JSONArray(text)
//        for (i in 0 until json.length()) {
//            val jsonBook = json.getJSONObject(i)
//            val title = jsonBook.getString(TITLE)
//            books.add(title)
//        }
        httpUrlConnection.disconnect()
        println(text)
        Handler(Looper.getMainLooper()).post {
            textView.text = text
        }
    }

    @WorkerThread
    fun addBook(token: String, ip: String, phone_number: String) {
        val httpUrlConnection = URL(ENDPOINT + BOOKS_URI).openConnection() as HttpURLConnection
        val content = JSONObject()
        content.put("push_token",token)
        content.put("ip",ip)
        content.put("phone_number",phone_number)
        println(content)
        httpUrlConnection.apply {
            connectTimeout = 1000 // 10 seconds
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
        }
        OutputStreamWriter(httpUrlConnection.outputStream).use {
            it.write(content.toString())
        }
        println(httpUrlConnection.responseCode)
        httpUrlConnection.disconnect()
        getBooksAndShowIt()
    }
}