package com.toxayah.summonerdata.include

import java.util.*

enum class RankedQueueType {
    SOLO,
    FLEX_5V5,
    FLEX_3V3,
    NORMAL
}
enum class TierType {
    CHALLENGER,
    MASTER,
    DIAMOND,
    PLATINUM,
    GOLD,
    SILVER,
    BRONZE,
    UNRANKED
}

enum class RankType {
    I,
    II,
    III,
    IV,
    V
}
data class Summoner(val summonerID: Int, val username: String, val region: String, val level: Int, var id: Int = -1)
data class Champion(val userId: Int, val champId: Int, val champName: String, val games: Int, val winRatio: Int, val kda: String, var id: Int = -1)
data class Ranked(val userId: Int, val type: RankedQueueType, val tier: TierType, val rank: RankType, val points: Int, var id: Int = -1)

class FullUser(val summoner:Summoner, val champions:Vector<Champion>, val rankedQueues: Vector<Ranked>)