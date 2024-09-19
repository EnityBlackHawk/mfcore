package org.mf.langchain

import org.mf.langchain.metadata.DbMetadata
import org.mf.langchain.util.QueryResult
import org.springframework.core.io.PathResource
import org.springframework.core.io.support.EncodedResource
import org.springframework.jdbc.datasource.init.ScriptUtils
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException

class DataImporter {

    companion object{

//        fun importAirlines(filePath: String, airlineRepository: AirlineRepository?) {
//            val jsonFile = File(filePath)
//            val jsonString = jsonFile.readText();
//            val gson = Gson();
//            val itemType = object : TypeToken<List<AirlineJson>>() {}.type
//            val airlineJson = gson.fromJson<List<AirlineJson>>(jsonString, itemType)
//
//            airlineRepository?.apply {
//                saveAll(
//                        airlineJson.map { it ->
//                            Airline.builder()
//                                    .id(it.id)
//                                    .iata(it.iata)
//                                    .icao(it.icao)
//                                    .name(it.name)
//                                    .build()
//                        }
//                )
//            }
//        }
//
//        fun importAirports(filePath : String, airportRepository: AirportRepository?) : String {
//            val jsonFile = File(filePath)
//            val jsonString = jsonFile.readText();
//            val gson = Gson();
//            val itemType = object  : TypeToken<List<AirportJson>>() {}.type
//            val airportJson = gson.fromJson<List<AirportJson>>(jsonString, itemType)
//
//           airportRepository?.apply {
//               saveAll(
//                       airportJson.map {it ->
//                           Airport.builder()
//                                   .id(it.iata_code)
//                                   .city(it.city)
//                                   .country(it.country)
//                                   .name(it.name)
//                                   .build()
//                       }
//               )
//           }
//           return airportJson[0].iata_code
//        }
        fun runSQLFromFile(path : String, connection : Connection) {
            ScriptUtils.executeSqlScript(
                connection,
                EncodedResource(PathResource(path)),
                false,
                false,
                ScriptUtils.DEFAULT_COMMENT_PREFIX,
                ScriptUtils.DEFAULT_STATEMENT_SEPARATOR,
                ScriptUtils.DEFAULT_BLOCK_COMMENT_START_DELIMITER,
                ScriptUtils.DEFAULT_BLOCK_COMMENT_END_DELIMITER
            );
        }

        fun runSQL(sql : String, connection : Connection) : String {
            var result: String;
            try {
                connection.createStatement().use { statement ->
                    val hasRS = statement.execute(sql)
                    result = if (hasRS) QueryResult(statement.resultSet).asString() else "OK"
                    statement.close()
                }
            } catch (e : SQLException){
                return e.message.toString()
            }
            return result
        }

        fun <T> runQuery(sql : String, connection: Connection, output : Class<T>) : T {

                connection.createStatement().use { statement ->
                    val rs = statement.executeQuery(sql)
                    val res = when(output) {
                        ResultSet::class.java -> rs as T
                        QueryResult::class.java -> QueryResult(rs) as T
                        else -> throw IllegalArgumentException("Unsupported output class")
                    }
                    statement.close()
                    return res
                }
        }

        fun <T> runQuery(sql : String, dbm: DbMetadata, output : Class<T>) : T {
            dbm.apply {
                connection.createStatement().use { statement ->
                    val rs = statement.executeQuery(sql)
                    val res = when(output) {
                        ResultSet::class.java -> rs as T
                        QueryResult::class.java -> QueryResult(rs, this) as T
                        else -> throw IllegalArgumentException("Unsupported output class")
                    }
                    statement.close()
                    return res
                }
            }
        }



        fun createDatabase(connection : Connection, databaseName : String) : String {
            return runSQL("CREATE DATABASE $databaseName", connection)
        }

        private fun resResultSet(rs : ResultSet) : String {
            val sb = StringBuilder()
            val md = rs.metaData
            val columns = md.columnCount
            val ss = mutableListOf<String>()
            for (i in 1..columns) {
                ss.add(md.getColumnName(i))
            }

            val values = mutableMapOf<String, MutableList<String>>()

            while (rs.next()) {
                val row = mutableListOf<String?>()
                for (i in 1..columns) {
                    row.add(rs.getString(i))
                }
                ss.forEachIndexed {index, it ->
                    if(values[it] == null) values[it] = mutableListOf()
                    values[it]!!.add(row[index] ?: "NULL")
                }

                // sb.append(String.format(formatString, *row.toTypedArray()))
            }

            var formatString = ""
            for (i in 1..columns)
                formatString += ("%${
                    (values[ss[i - 1]]!!.maxByOrNull { it: String -> it.length }?.length ?: 0).coerceAtLeast(
                        ss[i - 1].length
                    )
                }s" + if (i == columns) "" else " | ")
            formatString += "\n"

            sb.append(String.format(formatString, *ss.toTypedArray()))
            val rowSize = values[ss[0]]!!.size
            for (i in 0 until rowSize) {
                val row = mutableListOf<String?>()
                for (j in 0 until columns) {
                    row.add(values[ss[j]]!![i])
                }
                sb.append(String.format(formatString, *row.toTypedArray()))
            }

            return sb.toString()
        }

        fun getCardinality(connection: Connection) : String {
            runSQL("ANALYZE", connection)
            return runSQL("SELECT \n" +
                    "    relname AS table_name, \n" +
                    "    reltuples AS row_count\n" +
                    "FROM \n" +
                    "    pg_class C\n" +
                    "JOIN \n" +
                    "    pg_namespace N ON (N.oid = C.relnamespace)\n" +
                    "WHERE \n" +
                    "    nspname NOT IN ('pg_catalog', 'information_schema') \n" +
                    "    AND C.relkind = 'r';", connection)
        }


    }



}