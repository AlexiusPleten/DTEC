package com.dtec.dtek

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dtec.dtek.view.AdressAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import java.net.CookieHandler
import java.net.CookieManager


class MainActivity : AppCompatActivity() {
    private lateinit var addBtn: FloatingActionButton
    private lateinit var recv: RecyclerView
    private lateinit var adressList: ArrayList<Adress>
    private lateinit var adressAdapter: AdressAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adressList = ArrayList()
        recv = findViewById(R.id.recv)
        addBtn = findViewById(R.id.addAdress)
        adressAdapter = AdressAdapter(this, adressList)

        recv.layoutManager = LinearLayoutManager(this)
        recv.adapter = adressAdapter
        addBtn.setOnClickListener() {
            addInfo()
        }
        Thread{
            readFromFiles()
            val cookieManager = CookieManager()
            CookieHandler.setDefault(cookieManager)
            adressAdapter.setAdreses(DtecClient.getAdressList())
        }.start()
    }

    fun readFromFiles() {
        val files = Utils.getFiles(applicationContext)
        if (files!=null) {
            for (file in files) {
                val f = applicationContext.filesDir.absolutePath + "/" + file
                if(Utils.checkFileExtensionForJson(f)) {
                    val json = Utils.readFromFile(applicationContext, file)
                    adressList.add(Utils.fromJsonToObject(json))
                    adressAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun addInfo() {
        val json = Json.parseToJsonElement(
            adressAdapter.getAdresses()).jsonObject

        val infilter = LayoutInflater.from(this)
        val v = infilter.inflate(R.layout.add_item, null)
        val city = v.findViewById<TextView>(R.id.addedCity)
        val street = v.findViewById<TextView>(R.id.addedStreet)
        val num = v.findViewById<TextView>(R.id.addedNum)
        val addDialog = AlertDialog.Builder(this)

        city.setOnClickListener() { addCity(infilter, json, city) }
        street.setOnClickListener() { addStreet(infilter, json, street, city.text.toString()) }
        num.setOnClickListener() { addNum(infilter, num, street.text.toString()
            , city.text.toString()) }

        addDialog.setView(v)
        addDialog.setPositiveButton("Зберегти") { dialog, _->
            val a = Adress(city.text.toString(),
                street.text.toString(),
                num.text.toString())
            adressList.add(a)
            Thread {
                Utils.toJsonFile(applicationContext, a.hashCode().toString(), a)
            }.start()
            adressAdapter.notifyDataSetChanged()
            dialog.dismiss()
        }
        addDialog.setNegativeButton("Відміна", { dialog, _->
            dialog.dismiss()
        })
        addDialog.create().show()
    }

    private fun addCity(infilter : LayoutInflater, json: JsonObject, parent : TextView) {
        val v = infilter.inflate(R.layout.add_city, null)
        val city = v.findViewById<AutoCompleteTextView>(R.id.addedCity)
        val addDialog = AlertDialog.Builder(this)

        city.setAdapter(ArrayAdapter(v.context, android.R.layout.simple_list_item_1
            , json.keys.toList()))

        addDialog.setView(v)
        addDialog.setPositiveButton("Зберегти") { dialog, _->
            parent.text = city.text
            dialog.dismiss()
        }
        addDialog.setNegativeButton("Відміна", { dialog, _->
            dialog.dismiss()
        })
        addDialog.create().show()
    }

    private fun addStreet(infilter : LayoutInflater, json: JsonObject, parent: TextView, city: String) {
        val v = infilter.inflate(R.layout.add_street, null)
        val street = v.findViewById<AutoCompleteTextView>(R.id.addedStreet)
        val addDialog = AlertDialog.Builder(this)
        val list = ArrayList<String>()

        for (item in json.get(city)!!.jsonArray!!.toList()) {
            list.add(item.toString().replace("\"*".toRegex(), ""))
        }

        street.setAdapter(ArrayAdapter(v.context, android.R.layout.simple_list_item_1
            , list))

        addDialog.setView(v)
        addDialog.setPositiveButton("Зберегти") { dialog, _->
            parent.text = street.text
            dialog.dismiss()
        }
        addDialog.setNegativeButton("Відміна", { dialog, _->
            dialog.dismiss()
        })
        addDialog.create().show()
    }

    private fun addNum(infilter: LayoutInflater, parent: TextView, city: String, street: String) {
        val v = infilter.inflate(R.layout.add_num, null)
        val num = v.findViewById<AutoCompleteTextView>(R.id.addedNum)
        val addDialog = AlertDialog.Builder(this)
        val nums = getDataFromDtek(city, street)
        num.setAdapter(ArrayAdapter(v.context, android.R.layout.simple_list_item_1
            , nums))

        addDialog.setView(v)
        addDialog.setPositiveButton("Зберегти") { dialog, _->
            parent.text = num.text
            dialog.dismiss()
        }
        addDialog.setNegativeButton("Відміна", { dialog, _->
            dialog.dismiss()
        })
        addDialog.create().show()
    }

    private fun getDataFromDtek(city: String, street: String): List<String> {
        val httpResult = sendRequest(Adress(city, street, "2"))
        try {
            return Json.parseToJsonElement(httpResult)
                .jsonObject.keys.toList()
        } catch (e : Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    private fun sendRequest(adress: Adress):String {
        val dtec = DtecClient()
        val res = ""
        val t = Thread {
            dtec.request(dtec.requestCSRF(), adress)
        }
        t.start()
        t.join()
        return res
    }
}