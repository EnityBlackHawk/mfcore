package org.utfpr.mf.tools

import org.utfpr.mf.annotation.FromRDB
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
                val ann = field.getAnnotation(FromRDB::class.java)

                if(ann == null) {
                    continue
                }

                if(ann.typeClass.javaObjectType != field.type) {
                    throw RuntimeException("Type mismatch")
                }

                if(ann.isAbstract) {

//                    val cols = ann.typeClass.java.declaredFields.map {
//                        it.getAnnotation(FromRDB::class.java)?.column ?: throw RuntimeException("All props of a composite prop must be annotated ")
//                    }
//
//                    for(col in cols) {
//                        val offset = columns.indexOf(col)
//
//                    }


                    val qr = filter(columns.first()) {
                        it == row[0]
                    }

                    val result = qr!!.asObject(ann.typeClass.java)
                    field.set(obj, result.firstOrNull())
                    continue
                }

                val offset = columns.indexOf(ann.column)

                if(offset == -1) {
                    throw RuntimeException("Column ${ann.column} not found")
                }
                // TODO Tratar Json Loop
                if(ann.isReference || ann.targetColumn.isNotBlank() && ann.targetTable.isNotBlank()) {
                    val isColumnString = columnTypes[offset] == SqlDataType.VARCHAR
                    val query = "SELECT ${ann.projection} FROM ${ann.targetTable} " +
                            "WHERE ${ann.targetColumn} = ${ if(isColumnString) "'${row[offset]}'" else row[offset]}"

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