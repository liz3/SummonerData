package com.toxayah.summonerdata.core

import com.toxayah.summonerdata.include.Champion
import com.toxayah.summonerdata.include.DatabaseInfo
import com.toxayah.summonerdata.include.Ranked
import com.toxayah.summonerdata.include.Summoner
import java.sql.*


class DbApi(private val connection: DatabaseConnection) {

    fun checkSummonerExists(id: Int, region: String): Boolean {
        val statement = connection.connection.prepareStatement("SELECT `ID` FROM `Accounts` WHERE `SID` = (?) AND `REGION` = (?)")
        statement.setInt(1, id)
        statement.setString(2, region)
        val result = statement.executeQuery()
        return result.next()
    }
    fun checkSummonerExists(id: String, region: String): Boolean {
        val statement = connection.connection.prepareStatement("SELECT `ID` FROM `Accounts` WHERE `USERNAME` = (?) AND `REGION` = (?)")
        statement.setString(1, id)
        statement.setString(2, region)
        val result = statement.executeQuery()
        return result.next()
    }

    fun getSummoner(name: String, region: String = ""): Summoner? {
        val statement = if (region == "")
            connection.connection.prepareStatement("SELECT * FROM `Accounts` WHERE `USERNAME` = (?)")
        else
            connection.connection.prepareStatement("SELECT * FROM `Accounts` WHERE `USERNAME` = (?) AND `REGION` = (?)")

        statement.setString(1, name)
        if (region != "")
            statement.setString(2, region)
        val result = statement.executeQuery()
        if (!result.next()) return null;
        return Summoner(result.getInt("SID"), result.getString("USERNAME"), result.getString("REGION"), result.getInt("LEVEL"), result.getInt("ID"))
    }

    fun getSummoner(id: Int, region: String = ""): Summoner? {
        val statement = if (region == "")
            connection.connection.prepareStatement("SELECT * FROM `Accounts` WHERE `SID` = (?)")
        else
            connection.connection.prepareStatement("SELECT * FROM `Accounts` WHERE `SID` = (?) AND `REGION` = (?)")

        statement.setInt(1, id)
        if (region != "")
            statement.setString(2, region)
        val result = statement.executeQuery()
        if (!result.next()) return null
        return Summoner(result.getInt("SID"),
                result.getString("USERNAME"),
                result.getString("REGION"),
                result.getInt("LEVEL"),
                result.getInt("ID"))
    }

    fun insertUser(summoner: Summoner): Boolean {
        if (checkSummonerExists(summoner.summonerID, summoner.region)) return true
        val st = connection.connection.prepareStatement("insert into `Accounts` (SID, USERNAME, REGION, LEVEL) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS)
        st.setInt(1, summoner.summonerID)
        st.setString(2, summoner.username)
        st.setString(3, summoner.region)
        st.setInt(4, summoner.level)
        val result = st.executeUpdate()
        val rs = st.generatedKeys
        if (rs.next()) {
            summoner.id = rs.getInt(1)
        }
        return true
    }
    fun insertRank(userId:Int, rank:Ranked): Boolean {
        var st = connection.connection.prepareStatement("SELECT ID FROM ranks WHERE QUEUE = (?) AND UID = (?)")
        st.setString(1, rank.type.toString())
        st.setInt(2, userId)
        val result = st.executeQuery()
        if(result.next()) return true
        st = connection.connection.prepareStatement("insert into ranks (UID, QUEUE, TIER, RANK, POINTS) values (?,?,?,?,?);")
        st.setInt(1, userId)
        st.setString(2, rank.type.toString())
        st.setString(3, rank.tier.toString())
        st.setString(4, rank.rank.toString())
        st.setInt(5, rank.points)
        return st.execute()
    }
    fun insertChampion(userId:Int, champ:Champion): Boolean {
        var st = connection.connection.prepareStatement("SELECT ID from champions where CHAMPID = (?) and UID = (?)")
        st.setInt(1, champ.champId)
        st.setInt(2, userId)
        val result = st.executeQuery()
        if(result.next()) return true
        st = connection.connection.prepareStatement("insert into champions (UID, CHAMPID, NAME, GAMES, RATIO, KDA) values (?,?,?,?,?,?);")
        st.setInt(1, userId)
        st.setInt(2, champ.champId)
        st.setString(3, champ.champName)
        st.setInt(4, champ.games)
        st.setInt(5, champ.winRatio)
        st.setString(6, champ.kda)
        return st.execute()
    }
}

class DatabaseConnection(private val opts: DatabaseInfo) {

    val connection = {
        DriverManager.getConnection(
                "jdbc:mysql://${opts.host}:${opts.port}/${opts.database}", opts.user, opts.password)
    }.invoke()!!

    init {
        executeQuery("CREATE TABLE IF NOT EXISTS `Accounts` (`ID` INT(11) NOT NULL AUTO_INCREMENT, `SID` INT(11), `USERNAME` VARCHAR(120), `REGION` VARCHAR(5), `LEVEL` INT(11), PRIMARY KEY (`ID`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;")
        executeQuery("CREATE TABLE IF NOT EXISTS `Ranks` (`ID` INT(11) NOT NULL AUTO_INCREMENT, `UID` INT(11), `QUEUE` VARCHAR(120), `TIER` VARCHAR(120), `RANK` VARCHAR(120), `POINTS` INT(11), PRIMARY KEY (`ID`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;")
        executeQuery("CREATE TABLE IF NOT EXISTS `Champions` (`ID` INT(11) NOT NULL AUTO_INCREMENT, `UID` INT(11), `CHAMPID` INT(11), `NAME` VARCHAR(120), `GAMES` INT(11), `RATIO` INT(11), `KDA` VARCHAR(120), PRIMARY KEY (`ID`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;")
    }
    private fun executeQuery(query: String): Boolean {
        val st = connection.createStatement()
        return st.execute(query)
    }
}