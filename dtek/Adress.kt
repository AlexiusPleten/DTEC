package com.dtec.dtek

import kotlinx.serialization.Serializable

@Serializable
data class Adress(val c: String = "", val s: String = "", val n: String = "") {
    var city:String = c
    var street:String = s
    var num:String = n

    var startDate:String = ""
    var endDate:String = ""

    override fun toString(): String {
        return "$city///$street///$num///$startDate///$endDate"
    }
}