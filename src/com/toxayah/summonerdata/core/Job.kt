package com.toxayah.summonerdata.core

import com.toxayah.summonerdata.core.parser.OpggParser
import com.toxayah.summonerdata.core.parser.Parser
import com.toxayah.summonerdata.include.DatabaseInfo
import com.toxayah.summonerdata.include.FetcherType
import com.toxayah.summonerdata.include.getDefault
import com.toxayah.summonerdata.limit
import com.toxayah.summonerdata.utils.RequestFactory
import org.jsoup.Jsoup
import java.sql.SQLNonTransientConnectionException
import java.util.*


class Job(type: FetcherType, private val region: String, entryName: String, private val dbOpts: DatabaseInfo = getDefault()) {

    private val queue = LinkedList<String>()
    private lateinit var parser: Parser
    private var database = DbApi(DatabaseConnection(dbOpts))
    private val requestFactory = RequestFactory()
    init {
        if (type == FetcherType.OPGG)
            parser = OpggParser()
        queue.add(entryName)
        cycle()
    }
    private fun cycle() {
        while (true) {
            if (queue.size == 0) {
                println("$region: Queue is empty, exiting")
                return
            }
            val target = queue.poll()
            val doc = Jsoup.parse(requestFactory.request(region, target))
            println("$region: Processing $target")
            try {
                val user = parser.parse(doc, region)
                val users = parser.extractUsers(doc, user.username)
                println("$region: Found ${users.size} other users in ${user.username}´s Profile")
                for (n in users) {
                    if (!queue.contains(n) && !database.checkSummonerExists(n, region)) {
                        if (queue.size <= limit)
                            queue.add(n)
                        else
                            println("$region WARNING Reached Buffer List Limit")
                    }
                }
                database.insertUser(user)
                if (user.id == -1) {
                    continue
                }
                parser.parseChampions(doc, user).forEach {
                    database.insertChampion(user.id, it)
                }
                parser.parseRanked(doc, user).forEach {
                    database.insertRank(user.id, it)
                }
            } catch (e: Exception) {
                if (e is SQLNonTransientConnectionException) {
                    if (!queue.contains(target)) queue.add(0, target)
                    while (true) {
                        try {
                            println("$region Database Connection dead, attempting reconnect")
                            database = DbApi(DatabaseConnection(dbOpts))
                            println("$region Reconnect successful continuing")
                            break
                        } catch (e: Exception) {
                            println("$region Reconnect failed, trying again in 0.5 seconds...")
                            Thread.sleep(500)
                        }
                    }
                }
            }
        }
    }
}