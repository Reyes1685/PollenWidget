package com.tuapp.pollenwidget

import org.json.JSONObject
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object PollenRepository {

    suspend fun fetchPollenData(
        lat: Double = 40.4168,
        lon: Double = -3.7038
    ): PollenData? {
        return try {
            val url = "https://air-quality-api.open-meteo.com/v1/air-quality" +
                "?latitude=$lat" +
                "&longitude=$lon" +
                "&hourly=grass_pollen,birch_pollen,alder_pollen" +
                "&timezone=Europe%2FMadrid" +
                "&forecast_days=1"
            val json = URL(url).readText()
            parseResponse(json)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun parseResponse(json: String): PollenData? {
        val root   = JSONObject(json)
        val hourly = root.getJSONObject("hourly")
        val times  = hourly.getJSONArray("time")
        val grass  = hourly.getJSONArray("grass_pollen")
        val birch  = hourly.getJSONArray("birch_pollen")
        val alder  = hourly.getJSONArray("alder_pollen")

        val now = LocalDateTime.now()
        val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

        var bestIndex = 0
        for (i in 0 until times.length()) {
            val t = LocalDateTime.parse(times.getString(i), fmt)
            if (!t.isAfter(now)) bestIndex = i
        }

        fun getVal(arr: org.json.JSONArray, idx: Int): Double =
            if (arr.isNull(idx)) 0.0 else arr.getDouble(idx)

        val hourLabel = times.getString(bestIndex).substring(11, 16)

        return PollenData(
            grass     = getVal(grass, bestIndex),
            birch     = getVal(birch, bestIndex),
            alder     = getVal(alder, bestIndex),
            timestamp = "Hoy $hourLabel"
        )
    }
}
