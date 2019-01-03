package com.toxayah.summonerdata.core

import com.toxayah.summonerdata.include.DatabaseInfo
import java.sql.Connection
import java.sql.DriverManager

class DatabaseQueue(private val opts: DatabaseInfo) {

    private var ready = false

    private var tableName = ""
    private lateinit var connection: Connection

    private fun executeQuery(query: String): Boolean {
        if (!ready) return false
        val st = connection.createStatement()
        return st.execute(query)
    }

    private fun createTable() {
        val statement = connection.createStatement()
        statement.execute("CREATE TABLE IF NOT EXISTS `$tableName` (`ID` INT(11) NOT NULL AUTO_INCREMENT, `VALUE` VARCHAR(60), PRIMARY KEY (`ID`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;")
    }

    fun clear() {
        if (ready) flushTable()
    }

    private fun flushTable() {
        val statement = connection.createStatement()
        statement.execute("TRUNCATE TABLE `$tableName`;")
    }

    fun deserialize() {
        val statement = connection.createStatement()
        statement.execute("DROP TABLE `$tableName`;")
        tableName = ""
        ready = false
    }

    fun setup(name: String) {
        if (ready) return
        tableName = name
        connection = {
            DriverManager.getConnection(
                    "jdbc:mysql://${opts.host}:${opts.port}/${opts.database}", opts.user, opts.password)
        }.invoke()!!
        createTable()
        flushTable()
        ready = true
    }

    fun add(entry: String) {
        if (!ready) return
        val statement = connection.prepareStatement("INSERT INTO `$tableName` (`VALUE`) VALUES (?);")
        statement.setString(1, entry)
        statement.execute()
    }

    fun poll(): String {
        val statement = connection.createStatement()
        val result = statement.executeQuery("SELECT * FROM `$tableName` LIMIT 1;")
        if (!result.first()) {
            return ""
        }
        val id = result.getInt("ID")
        val value = result.getString("VALUE")
        connection.createStatement().execute("DELETE FROM `$tableName` WHERE `ID` = $id;")
        return value
    }

    fun contains(entry: String): Boolean {
        if (!ready) return false

        val statement = connection.prepareStatement("SELECT `ID` FROM `$tableName` WHERE `VALUE` = (?);")
        statement.setString(1, entry)

        return statement.executeQuery().next()
    }
}