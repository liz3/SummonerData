package com.toxayah.summonerdata.include

data class DatabaseInfo(var host:String, var user:String, var password:String, var database:String, var port:Int = 3306)


fun getDefault() : DatabaseInfo {
    return DatabaseInfo("localhost", "root", "", "summoner")
}