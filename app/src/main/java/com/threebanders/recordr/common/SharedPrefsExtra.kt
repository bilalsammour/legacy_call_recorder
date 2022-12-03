package com.threebanders.recordr.common

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import core.threebanders.recordr.data.Recording
import java.lang.reflect.Type

object SharedPrefsExtra {

    fun getDataFromSharedPreferences(context: Context): List<Recording?>? {
        val gson = Gson()
        val productFromShared: List<Recording?>?
        val sharedPref: SharedPreferences? =
            context.getSharedPreferences("PREFS_TAG", Context.MODE_PRIVATE)
        val jsonPreferences = sharedPref?.getString("PRODUCT_TAG", "")
        val type: Type = object : TypeToken<List<Recording?>?>() {}.type
        productFromShared = gson.fromJson<List<Recording?>>(jsonPreferences, type)
        return productFromShared
    }

    fun setDataFromSharedPreferences(context: Context, curProduct: List<Recording?>) {
        val gson = Gson()
        val jsonCurProduct = gson.toJson(curProduct)
        val sharedPref: SharedPreferences? =
            context.getSharedPreferences("PREFS_TAG", Context.MODE_PRIVATE)
        val editor = sharedPref?.edit()
        editor?.putString("PRODUCT_TAG", jsonCurProduct)
        editor?.apply()
    }
}