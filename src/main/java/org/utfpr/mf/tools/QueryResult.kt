package org.utfpr.mf.tools

import com.mongodb.DBRef
import org.utfpr.mf.metadata.DbMetadata
import org.springframework.data.annotation.Id
import org.utfpr.mf.metadata.GenericRegistry
import java.sql.ResultSet
import java.util.*
import java.sql.Connection
import java.sql.Types

open class QueryResult {
    protected val columns = mutableListOf<String>()
    protected val columnTypes = mutableListOf<SqlDataType>()
    protected open val rows = mutableListOf<List<String?>>()
    protected lateinit var resultSet : ResultSet
    protected var metadata : DbMetadata? = null

    constructor(metadata: DbMetadata?) {
        this.metadata = metadata
    }

    constructor(resultSet: ResultSet, metadata: DbMetadata? = null) {
        this.metadata = metadata
        val rsMetadata = resultSet.metaData
        this.resultSet = resultSet

        for (i in 0 until rsMetadata.columnCount) {
            columns.add(resultSet.metaData.getColumnName(i + 1))
            columnTypes.add(SqlDataType.getByValue(resultSet.metaData.getColumnType(i + 1)))
        }
        while (resultSet.next()) {
            val row = mutableListOf<String?>()
            for (i in columns.indices) {

                val result = resultSet.getString(i + 1)
                row.add(result)
            }
            require(row.size == columns.size) { "Row length does not match column length" }
            rows.add(row)
        }

    }

    constructor(vararg columnNames : String) : this(null) {
        this.columns.addAll(columnNames)
    }

    
    fun addRow(vararg row: String): QueryResult {
        require(row.size == columns.size) { "Row length does not match column length" }
        val r = mutableListOf<String>()
        r.addAll(row.asList())
        rows.add(r)
        return this
    }

    fun <T> getAllFromColumn(columnName: String, outputClass : Class<T>): List<T> {
        val columnIndex = columns.indexOf(columnName)
        require(columnIndex != -1) { "Column $columnName not found" }
        val strings = rows.map { it[columnIndex].toString() }
        return when(outputClass) {
            Integer::class.java -> strings.map { it.toInt() } as List<T>
            Long::class.java -> strings.map { it.toLong() } as List<T>
            Float::class.java -> strings.map { it.toFloat() } as List<T>
            Double::class.java -> strings.map { it.toDouble() } as List<T>
            else -> strings as List<T>
        }
    }

    fun asString() : String{
        val sb = StringBuilder();
        var formatSting = ""
        for(i in 0 until  columns.size) {
            formatSting += ("%${
                (rows.map { it[i].toString() }.maxByOrNull { it: String? -> it?.length ?: 0  }?.length ?: 0).coerceAtLeast(
                    columns[i].length
                )
            }s" + if (i + 1 == columns.size) "" else " | ")
        }
        formatSting += "\n"

        sb.append(String.format(formatSting, *columns.toTypedArray()))
        for (i in 0 until rows.size) {
            sb.append(String.format(formatSting, *rows[i].toTypedArray()))
        }

        return sb.toString()
    }

    fun asMarkdown() : String {
        val sb = StringBuilder();
        var formatSting = "| "
        for(i in 0 until  columns.size) {
            formatSting += ("%${
                (rows.map { it[i].toString() }.maxByOrNull { it: String? -> it?.length ?: 0  }?.length ?: 0).coerceAtLeast(
                    columns[i].length
                )
            }s" + if(i + 1 == columns.size) " |" else " | ")
        }
        formatSting += "\n"

        sb.append(String.format(formatSting, *columns.toTypedArray()))
        val vars = ":--:".repeatList(columns.size);
        sb.append(String.format(formatSting, *vars.toTypedArray()))

        for (i in 0 until rows.size) {
            sb.append(String.format(formatSting, *rows[i].toTypedArray()))
        }

        return sb.toString()
    }


    open fun <T> asObject(clazz : Class<T>) : List<T> {
        if(metadata == null)
            throw Exception("Metadata required")
        val res = mutableListOf<T>()
        for (row in rows) {
            val obj = clazz.getDeclaredConstructor().newInstance()

            for (i in 0 until columns.size) {
                val field = runCatching {
                    val fieldName = columns[i].snakeToCamelCase()
                    clazz.getDeclaredField(fieldName)
                }.getOrNull()
                if(field == null) continue
                field.isAccessible = true
                val rowString = row[i].toString()
                if(row[i] == null || rowString.isEmpty()) continue
                when (field.type.name) {
                    "int", "java.lang.Integer" -> field.set(obj, rowString.toInt())
                    "long", "java.lang.Long" -> field.set(obj, rowString.toLong())
                    "float", "java.lang.Float" -> field.set(obj, rowString.toFloat())
                    "double", "java.lang.Double" -> field.set(obj, rowString.toDouble())
                    "java.lang.String" -> field.set(obj, row[i])
                    "java.sql.Date" -> field.set(obj, java.sql.Date.valueOf(rowString))
                    "java.util.Date" -> {
                        val date = java.text.SimpleDateFormat("yyyy-MM-dd").parse(rowString)
                        field.set(obj, date)
                    }
                    "java.sql.Timestamp" -> field.set(obj, java.sql.Timestamp.valueOf(rowString))
                    "java.sql.Time" -> field.set(obj, java.sql.Time.valueOf(rowString))
                    "java.time.LocalDateTime" -> field.set(obj, java.time.LocalDateTime.parse(rowString.replace(" ", "T")))
                    else -> {
                        val isRef = field.isAnnotationPresent(org.springframework.data.mongodb.core.mapping.DBRef::class.java)
                        val _c = field.type
                        val isList = _c.name == "java.util.List"
                        val c = if (isList) _c.typeParameters[0].javaClass else _c
                        val constr = c.getDeclaredConstructor()
                        constr.isAccessible = true
                        val o = constr.newInstance()
                        val fs = c.declaredFields
                        val fId = fs.firstOrNull { it.isAnnotationPresent(Id::class.java) }


                        if(isRef) {
                            assert(fId != null) { "fId was null" }
                            fId!!.isAccessible = true
                            fId.set(o, row[i])
                            field.set(obj, o)
                            continue
                        }

                        var tableName = c.simpleName.lowercase()
                        if(tableName.contains("reference")) {
                            tableName = tableName.replace("reference", "")
                        }
                        val table = metadata!!.tables.find { it.name == tableName }
                        if (table == null)
                            throw Exception("Table $tableName not found")

                        val pkName = table.primaryKey.name

                        val query = "SELECT * FROM $tableName WHERE $pkName = '${row[i]}'"
                        val arr = DataImporter.runQuery(query, metadata!!, QueryResult::class.java).asObject(c)
                        assert(arr.size == 1) { "No rows resulted for: $query" }
                        arr.firstOrNull()?.let { field.set(obj, it)}
                    }
                }
            }
            res.add(obj)
        }
        return res;
    }

    fun asGenericRegistry() : List<GenericRegistry> {
        val res = mutableListOf<GenericRegistry>()
        for (row in rows) {
            val obj = GenericRegistry()
            for (i in 0 until columns.size) {
                obj[columns[i]] = row[i].toString()
            }
            res.add(obj)
        }
        return res
    }

    fun String.snakeToCamelCase(): String {
        return this.split('_')
            .mapIndexed { index, word ->
                if (index == 0) word.lowercase()
                else word.replaceFirstChar { it.uppercase() }
            }
            .joinToString("")
    }

    fun String.repeatList(n : Int) : List<String> {
        val value = this.toString()
        val result = mutableListOf<String>()
        for(i in 1..n) {
            result.add(value)
        }
        return result
    }


    override fun toString() : String {
        return asString()
    }
}
