package com.toxayah.summonerdata

import com.toxayah.summonerdata.core.DatabaseQueue
import com.toxayah.summonerdata.core.Job
import com.toxayah.summonerdata.include.FetcherType
import com.toxayah.summonerdata.include.getDefault
import com.toxayah.summonerdata.utils.ChampionUtils

var limit = 100000 * 10

fun main(args: Array<String>) {


    var lVersion = ""
    val databaseOpts = getDefault()
    val targets = HashMap<String, String>()
    for ((index, arg) in args.withIndex()) {
        if (arg == "-limit") {
            limit = args[index + 1].toInt()
            println("Set max Queue size to $limit")
        }
        if (arg == "-version") lVersion = args[index + 1]
        if (arg == "-db-host") databaseOpts.host = args[index + 1]
        if (arg == "-db-user") databaseOpts.user = args[index + 1]
        if (arg == "-db-pass") databaseOpts.password = args[index + 1]
        if (arg == "-db-target") databaseOpts.database = args[index + 1]
        if (arg == "-db-port") databaseOpts.port = args[index + 1].toInt()
        if (arg == "-target") {
            val region = args[index + 1].toUpperCase()
            val name = args[index + 2].replace("-", " ")
            targets[name] = region
        }
    }
    if (lVersion == "")
        ChampionUtils.init()
    else
        ChampionUtils.init(lVersion)
    println("Starting the following Jobs:")
    for ((name, region) in targets) {
        println("[$region] $name")
        val thread = Thread {
            Job(FetcherType.OPGG, region, name, databaseOpts)
        }
        thread.name = "Job runner [$region] $name"
        thread.start()
        println("Thread deployed")
    }

}