package com.dtec.dtek

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class ItemActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.item_activity)

        val data = intent.getStringExtra("Item").toString()

        val city = findViewById<TextView>(R.id.cityText)
        val street = findViewById<TextView>(R.id.streetText)
        val num = findViewById<TextView>(R.id.numText)
        val startDate = findViewById<TextView>(R.id.startDate)
        val endDate = findViewById<TextView>(R.id.endDate)

        val str = data?.split("///")

        city.text = str?.get(0)
        street.text = str?.get(1)
        num.text = str?.get(2)
        if(!str?.get(3).equals("") && !str?.get(4).equals("")) {
            startDate.text = str?.get(3)
            endDate.text = str?.get(4)
        } else {
            startDate.text = "Світло є!!!"
        }
    }
}