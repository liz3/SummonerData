package com.toxayah.summonerdata.utils

import org.json.JSONArray
import org.json.JSONObject

object ChampionUtils {

    val champions = HashMap<String, Int>()
    private fun getNewestVersion(): String {
        val req = httpRequest("https://ddragon.leagueoflegends.com/api/versions.json").getBodyStr()
        val parsed = JSONArray(req)
        return parsed.get(0) as String
    }
    fun init(leagueVersion:String = getNewestVersion()) {

        println("League version $leagueVersion is been used for champion information")
        val response = httpRequest("https://ddragon.leagueoflegends.com/cdn/$leagueVersion/data/en_US/champion.json").getBodyStr()
        val parsed = JSONObject(response)
        val all = parsed["data"] as JSONObject
        all.keys().forEach {
            val champ = (all[it]) as JSONObject
            champions[champ.getString("name")] = champ.getInt("key")
        }
        println("Added ${champions.size} champions")
    }
}