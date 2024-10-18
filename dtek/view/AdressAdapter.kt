package com.dtec.dtek.view

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dtec.dtek.Adress
import com.dtec.dtek.DtecClient
import com.dtec.dtek.ItemActivity
import com.dtec.dtek.R
import com.dtec.dtek.Utils
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

class AdressAdapter(val c: Context, val adressList: ArrayList<Adress>):RecyclerView.Adapter<AdressAdapter.AdressViewHolder>(){

    private lateinit var adreses : String

    inner class AdressViewHolder(val view:View): RecyclerView.ViewHolder(view) {
        val city = view.findViewById<TextView>(R.id.city)
        val street = view.findViewById<TextView>(R.id.street)
        val num = view.findViewById<TextView>(R.id.num)
        val remove = view.findViewById<ImageView>(R.id.remove).setOnClickListener() {
            AlertDialog.Builder(c).setTitle("Видалити?").setMessage("Ви впевнені?")
                .setPositiveButton("Так" ) {dialog, _->
                    Utils.removeFile(c, adressList.get(adapterPosition).hashCode().toString() + ".json")
                    adressList.removeAt(adapterPosition)
                    notifyDataSetChanged()
                    dialog.dismiss()
                }.setNegativeButton("Ні" ) {dialog, _->
                    dialog.dismiss()
                }.create().show()
        }
        val v = view.setOnClickListener( {
            Toast.makeText(c, "очікуємо відповідь", Toast.LENGTH_LONG).show()
            var adress = adressList.get(adapterPosition)
            Thread {
                parseNum(adress)
                createItemActivity(adress)
            }.start()
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdressViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.adress_item, parent, false)
        return AdressViewHolder(v)
    }

    override fun getItemCount(): Int {
        return adressList.size
    }

    override fun onBindViewHolder(holder: AdressViewHolder, position: Int) {
        val newList = adressList[position]
        holder.city.text = newList.city
        holder.street.text = newList.street
        holder.num.text = newList.num
    }

    private fun sendRequest(adress: Adress):String {
        val dtec = DtecClient()
        return dtec.request(dtec.requestCSRF(), adress)
    }

    private fun parseNum(adress: Adress) {
        val res = sendRequest(adress)
        if(res.isNotEmpty()) {
            val json = Json.parseToJsonElement(res)
            val jData = json.jsonObject.get("data")
            android.util.Log.d(DtecClient.tag, jData.toString())
            if(jData != null) {
                val jNum = jData.jsonObject.get(adress.num)
                if (jNum != null) {
                    val start_date = jNum.jsonObject.get("start_date").toString()
                    val end_date = jNum.jsonObject.get("end_date").toString()
                    adress.startDate = if(start_date.isEmpty()) "" else "початок: " + start_date
                    adress.endDate = if(end_date.isEmpty()) "" else "кінець: " + end_date
                }
            }
        }
    }

    private fun createItemActivity(adress: Adress) {
        val intent = Intent(c, ItemActivity::class.java)
        intent.putExtra("Item", adress.toString())
        ContextCompat.startActivity(c,intent, null)
    }

    fun setAdreses(adr : String) {
        adreses = adr
    }

    fun getAdresses():String {
        return adreses
    }
}