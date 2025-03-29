package org.utfpr.mf.tools

import org.utfpr.mf.annotation.FromRDB
import org.utfpr.mf.annotation.ListOf
import org.utfpr.mf.metadata.DbMetadata
import java.lang.reflect.ParameterizedType
import java.sql.ResultSet

class QueryResult2 : QueryResult {


    constructor(resultSet : ResultSet, metadata: DbMetadata?) : super(resultSet, metadata)
    constructor(vararg columnNames : String) : super(*columnNames)

    fun filter(columnName : String, condition : (Any) -> Boolean) : QueryResult2? {
       val offset = columns.indexOf(columnName)
        if(offset == -1) {
            return null;
        }

        val qr = QueryResult2(*columns.toTypedArray())
        qr.metadata = this.metadata
        for(row in rows) {
            if(condition.invoke(row[offset]!!)) {
                qr.addRow(*row.map { it.toString() }.toTypedArray())
            }
        }
        return qr

    }

    private fun populateListObject(ptype : ParameterizedType) : List<Any> {
        if(metadata == null)
            throw Exception("Metadata required")

        val obj = ArrayList<Any>()
        val parClass = ptype.actualTypeArguments[0] as Class<*>
        val values = asObject(parClass)
        obj.addAll(values)
        return obj
    }

    override fun <T> asObject(clazz : Class<T>) : List<T> {
        if(metadata == null)
            throw Exception("Metadata required")

        val res = mutableListOf<T>()


        for (row in rows) {

            if(clazz.packageName == "java.lang") {

                // TODO Try to get ID first
                val v = cast(row[0], clazz)
                v?.let { res.add(it) }
                continue
            }


            val obj = ( if (clazz.isInterface) getConcreteTypeClass(clazz) else clazz).getDeclaredConstructor().newInstance() as T


            val fields = clazz.declaredFields

            for(field in fields) {
                field.isAccessible = true
                val ann = field.getAnnotation(FromRDB::class.java) ?: continue

                if(ann.typeClass.javaObjectType != field.type) {
                    throw RuntimeException("Type mismatch")
                }

                val annList = field.getAnnotation(ListOf::class.java)

                if(annList != null) {

                    var table : String = annList.targetTable
                    if(table == "\$auto") {
                        //TODO Convert to snake case
                        table = annList.value.simpleName!!.lowercase()
                    }
                    //TODO Support multiple PKs (column as List<String>)
                    var column = annList.targetColumn;
                    if(column == "\$auto") {
                        metadata!!.tables.filter { it.name.lowercase() == table }.forEach { it ->
                            it.columns.filter { it.isFk }.filter {
                                //TODO Test error cases (error if is abstract)
                                it.fkInfo?.pk_tableName == ann.table
                            }.forEach { column = it.name }
                        }
                    }
                    // TODO if the value is not in this current table (if it is in another table, make a SQL query)
                    var offset = columns.indexOf(annList.column)
                    if(offset == -1) {
                        // Gets the PK
                        metadata!!.tables.filter { it.name == ann.table }.first().columns.filter { it.isPk }.first().let {
                            offset = columns.indexOf(it.name)
                        }
                    }
                    val isString = columnTypes[offset] == SqlDataType.VARCHAR
                    val query = "SELECT * FROM $table WHERE $column = ${if (isString) "'${row[offset]}'" else row[offset]}"
                    val qr = DataImporter.runQuery(query, metadata!!, QueryResult2::class.java)
                    val result = qr.asObject(annList.value.java)
                    field.set(obj, result)
                    continue
                }

                if(ann.isAbstract) {

                    val qr = filter(columns.first()) {
                        it == row[0]
                    }

                    val result = qr!!.asObject(ann.typeClass.java)
                    println(result)
                    field.set(obj, result.firstOrNull())
                    continue
                }

                val offset = columns.indexOf(ann.column)
                var targetColumn = ann.targetColumn

                if(offset == -1) {
                    throw RuntimeException("Column ${ann.column} not found")
                }

                if(ann.targetColumn == "\$auto") {
                    metadata!!.tables.first { it.name == ann.targetTable }.columns.first { it.isPk }.let {
                        targetColumn = it.name
                    }
                }

                // TODO Tratar Json Loop
                if(ann.isReference || targetColumn.isNotBlank() && ann.targetTable.isNotBlank()) {
                    val isColumnString = columnTypes[offset] == SqlDataType.VARCHAR
                    val query = "SELECT ${ann.projection} FROM ${ann.targetTable} " +
                            "WHERE $targetColumn = ${ if(isColumnString) "'${row[offset]}'" else row[offset]}"

                    val res = DataImporter.runQuery(query, metadata!!, QueryResult2::class.java)
                    val newObj = if (ann.type == "array") res.populateListObject(field.genericType as ParameterizedType) else  res.asObject( field.type )
                    field.set(obj, if (ann.type == "array") newObj else newObj.firstOrNull())
                    continue
                }

                val v = cast(row[offset], ann.typeClass.java)
                field.set(obj, v)

            }

            res.add(obj)
        }
        return res;
    }

    private fun getConcreteTypeClass(clazz : Class<*>) : Class<*> {
        if(clazz == java.util.List::class.java) {
            return java.util.ArrayList::class.java
        }

        return clazz
    }

    private fun <T> cast(value: String?, clazz: Class<T>): T? {
    value?.let {
        val cName = clazz.simpleName
        return when (cName) {
            "Integer" -> it.toInt() as T
            "Long" -> it.toLong() as T
            "Float" -> it.toFloat() as T
            "Double" -> it.toDouble() as T
            "LocalDateTime" -> java.time.LocalDateTime.parse(it.replace(" ", "T")) as T
            "ZonedDateTime" -> java.time.ZonedDateTime.parse(it.replace(" ", "T")) as T
            "LocalDate" -> java.time.LocalDate.parse(it) as T
            "LocalTime" -> java.time.LocalTime.parse(it) as T
            "Instant" -> java.time.Instant.parse(it) as T
            "String" -> it as T
            else -> null
        }
    }
    return null
}

}