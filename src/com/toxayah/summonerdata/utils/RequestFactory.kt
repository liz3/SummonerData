package com.toxayah.summonerdata.utils

import org.json.JSONObject
import java.net.URLEncoder

class RequestFactory {


    fun updateOpGG(region: String, id: Int): Boolean {
        httpRequest("http://$region.op.gg/summoner/ajax/renew.json/", method = "POST", bdy = "summonerId=$id".toByteArray(), headers = hashMapOf(
                Pair("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"),
                Pair("Accept", "application/json, text/javascript, */*; q=0.01"),
                Pair("X-Requested-With", "XMLHttpRequest"),
                Pair("Origin", "http://${region.toLowerCase()}.op.gg")))

        var counter = 0
        while (counter < 15) {
            val result = httpRequest("http://$region.op.gg/summoner/ajax/renewStatus.json/", method = "POST", bdy = "summonerId=$id".toByteArray(), headers = hashMapOf(
                    Pair("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"),
                    Pair("Accept", "application/json, text/javascript, */*; q=0.01"),
                    Pair("X-Requested-With", "XMLHttpRequest"),
                    Pair("Origin", "http://${region.toLowerCase()}.op.gg")
            ))
            var delay = 1500L
            try {
                val obj = JSONObject(result.getBodyStr())
                if (obj.getBoolean("finish")) return true
                delay = obj.getLong("delay")
            } catch (ignored: Exception) {
                return false
            }
            Thread.sleep(delay)
            counter++
        }

        return false
    }

    fun request(region: String, entry: String): String {
        val trg = "http://${region.toLowerCase().replace("kr", "www")}.op.gg/summoner/userName=${URLEncoder.encode(entry, "UTF-8")}"
        val request = httpRequest(trg, headers = hashMapOf(Pair("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")))
        return request.getBodyStr()
    }
}