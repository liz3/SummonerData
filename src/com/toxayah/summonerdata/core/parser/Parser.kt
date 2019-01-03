package com.toxayah.summonerdata.core.parser

import com.toxayah.summonerdata.include.*
import com.toxayah.summonerdata.utils.ChampionUtils
import com.toxayah.summonerdata.utils.toRomNum
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.*

interface Parser {
    fun parse(doc: Document, region: String): Summoner
    fun parseChampions(doc: Document, summoner: Summoner): Vector<Champion>
    fun parseRanked(doc: Document, user: Summoner): Vector<Ranked>
    fun extractUsers(doc: Document, own: String): Vector<String>
}

class OpggParser : Parser {
    override fun extractUsers(doc: Document, own: String): Vector<String> {
        val list = Vector<String>()
        val filter = doc.getElementsByClass("Team")
        for (teamEntry in filter) {
            if (teamEntry.parent().classNames().contains("Information")) continue
            val name = teamEntry.child(0).child(1).text()
            if (name == own) continue
           if(!list.contains(name) && !name.isEmpty() && !name.isBlank()) list.add(name)
        }
        return list
    }

    override fun parseRanked(doc: Document, user: Summoner): Vector<Ranked> {
        val list = Vector<Ranked>()
        getRankedInfo(doc, user.id, list)
        return list
    }

    override fun parseChampions(doc: Document, summoner: Summoner): Vector<Champion> {
        val list = Vector<Champion>()

        val filter = doc.getElementsByClass("MostChampionContent")
        if (filter.size == 0) return list
        val entries = filter[1].children()

        entries.forEach { entry ->
            if (!entry.classNames().contains("MoreButton")) {
                val champName = entry.child(1).text().split(" CS").first()
                val kda = entry.child(2).text().split(" KDA").first()
                val ratio = entry.child(3).child(0).text().replace("%", "").toInt()
                val games = entry.child(3).child(1).text().split(" ").first().toInt()
                list.add(Champion(summoner.id, ChampionUtils.champions[champName]!!, champName, games, ratio, kda))
            }
        }
        return list
    }


    private fun getSummonerId(doc: Document): Int {
        val filter = doc.getElementsByClass("GameListContainer")
        val id = filter.first().attributes()["data-summoner-id"]
        return id.toInt()
    }

    private fun getSummonerLevel(doc: Document): Int {
        val filter = doc.getElementsByClass("ProfileIcon")[0]
        return if (filter.children().size == 3)
            filter.child(2).text().toInt()
        else
            filter.child(1).text().toInt()
    }

    private fun getSummonerName(doc: Document): String {
        var filter = doc.getElementsByClass("Name")[0]
        if (filter.text().contains("[")) {
            filter = doc.getElementsByClass("Name")[1]
        }

        return filter.text()
    }

    private fun getRankedInfo(doc: Document, id: Int, list: Vector<Ranked>) {
        val filter = doc.getElementsByClass("TierBox")[0]
        val len = filter.children().size
        val firstEntry = filter.child(0)
        if (firstEntry.child(1).text() == "Unranked")
            return
        run {
            val tier = TierType.valueOf(firstEntry.child(1).child(0).child(0).text().split(" ")[0].toUpperCase())
            val rank = if (tier == TierType.CHALLENGER || tier == TierType.MASTER || tier == TierType.GRANDMASTER)
                RankType.I
            else
                RankType.valueOf(toRomNum(firstEntry.child(1).child(0).child(0).text().split(" ")[1].toInt()))
            val points = firstEntry.child(1).child(1).child(0).text().split(" ")[0].replace(",", "").toInt()
            list.addElement(Ranked(id, RankedQueueType.SOLO, tier, rank, points))
        }
        if (len == 2) {
            val secEntry = filter.child(1)

            if (secEntry.child(1).text().contains("Unranked", true)) return
            val tier = TierType.valueOf(secEntry.child(1).text().split(" ")[0].toUpperCase())
            val rank = if (tier == TierType.CHALLENGER || tier == TierType.MASTER || tier == TierType.GRANDMASTER)
                RankType.I
            else
                RankType.valueOf(toRomNum(secEntry.child(1).text().split(" ")[1].toInt()))

            val points = if (tier == TierType.CHALLENGER || tier == TierType.MASTER || tier == TierType.GRANDMASTER)
                secEntry.child(1).text().split(" ")[1].replace(",", "").toInt()
            else
                secEntry.child(1).text().split(" ")[2].toInt()

            val queue = secEntry.child(2).child(0).text()
            list.add(Ranked(id, if (queue == "Flex 5:5 Rank")
                RankedQueueType.FLEX_5V5
            else
                RankedQueueType.FLEX_3V3
                    , tier, rank, points))
        }
        if (len == 3) {
            for (x in 0..1) {
                val secEntry = filter.child(1 + x)
                val tier = TierType.valueOf(secEntry.child(1).text().split(" ")[0].toUpperCase())

                val rank = if (tier == TierType.CHALLENGER || tier == TierType.MASTER || tier == TierType.GRANDMASTER)
                    RankType.I
                else
                    RankType.valueOf(toRomNum(secEntry.child(1).text().split(" ")[1].toInt()))


                val points = if (tier == TierType.CHALLENGER || tier == TierType.MASTER || tier == TierType.GRANDMASTER)
                    secEntry.child(1).text().split(" ")[1].replace(",", "").toInt()
                else
                    secEntry.child(1).text().split(" ")[2].replace(",", "").toInt()
                val queue = secEntry.child(2).child(0).text()

                list.add(Ranked(id, if (queue == "Flex 5:5 Rank")
                    RankedQueueType.FLEX_5V5
                else
                    RankedQueueType.FLEX_3V3
                        , tier, rank, points))
            }
        }
    }

    override fun parse(doc: Document, region: String): Summoner {
        val summonerName = getSummonerName(doc)
        val summonerId = getSummonerId(doc)
        val summonerLevel = getSummonerLevel(doc)

        return Summoner(summonerId, summonerName, region, summonerLevel)
    }

}