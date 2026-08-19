package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

object SceneJsonHelper {
    fun toJson(scenes: List<SceneItem>): String {
        val array = JSONArray()
        for (scene in scenes) {
            val obj = JSONObject()
            obj.put("sceneNumber", scene.sceneNumber)
            obj.put("narrationText", scene.narrationText)
            obj.put("visualPrompt", scene.visualPrompt)
            obj.put("durationSec", scene.durationSec.toDouble())
            obj.put("visualTheme", scene.visualTheme)
            obj.put("cameraMotion", scene.cameraMotion)
            val kwArray = JSONArray()
            scene.keywordsToHighlight.forEach { kwArray.put(it) }
            obj.put("keywords", kwArray)
            array.put(obj)
        }
        return array.toString()
    }

    fun fromJson(jsonStr: String): List<SceneItem> {
        val result = mutableListOf<SceneItem>()
        if (jsonStr.isBlank()) return result
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val kws = mutableListOf<String>()
                if (obj.has("keywords")) {
                    val kwArray = obj.getJSONArray("keywords")
                    for (j in 0 until kwArray.length()) {
                        kws.add(kwArray.getString(j))
                    }
                }
                result.add(
                    SceneItem(
                        sceneNumber = obj.optInt("sceneNumber", i + 1),
                        narrationText = obj.optString("narrationText", ""),
                        visualPrompt = obj.optString("visualPrompt", ""),
                        durationSec = obj.optDouble("durationSec", 5.0).toFloat(),
                        visualTheme = obj.optString("visualTheme", "cyber_grid"),
                        cameraMotion = obj.optString("cameraMotion", "Zoom In"),
                        keywordsToHighlight = kws
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }
}
