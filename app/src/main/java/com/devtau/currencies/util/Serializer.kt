package com.devtau.currencies.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object Serializer {

    fun serializeList(list: List<*>): String = Gson().toJson(list)

    fun deserializeListOfStrings(string: String?): List<String>? =
        if (string == null || string.isEmpty()) null
        else {
            val listType = object: TypeToken<List<String>>() {}.type
            Gson().fromJson<List<String>>(string, listType)
        }
}