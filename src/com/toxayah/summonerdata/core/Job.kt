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

    private lateinit var parser: Parser
    var queue:DatabaseQueue
    private var database = DbApi(DatabaseConnection(dbOpts))
    private val requestFactory = RequestFactory()
    init {
        if (type == FetcherType.OPGG)
            parser = OpggParser()
        queue = DatabaseQueue(dbOpts)
        queue.setup("queue_${Thread.currentThread().id}")
     //   queue.setup("queue_15")
        queue.add(entryName)
        cycle()
    }
    private fun cycle() {

        while (true) {
            val target = queue.poll()
            if (target == "") {
                println("$region: Queue is empty, exiting")
                return
            }
            val doc = Jsoup.parse(requestFactory.request(region, target))
            println("[WORKER ${Thread.currentThread().id}]: Walking on $target")
            try {
                val user = parser.parse(doc, region)
                val users = parser.extractUsers(doc, user.username)
                println("$region: Found ${users.size} other users in ${user.username}´s Profile")
                for (n in users) {
                    if (!queue.contains(n) && !database.checkSummonerExists(n, region)) {
                        queue.add(n)
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