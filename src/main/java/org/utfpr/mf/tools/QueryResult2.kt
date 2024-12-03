package org.utfpr.mf.tools

import org.springframework.data.annotation.Id
import org.utfpr.mf.annotation.FromRDB
import org.utfpr.mf.metadata.DbMetadata
import kotlin.reflect.cast
import java.sql.ResultSet
import kotlin.reflect.KClass
import kotlin.reflect.full.memberFunctions

class QueryResult2(resultSet : ResultSet, metadata: DbMetadata?) : QueryResult(resultSet, metadata) {


    override fun <T> asObject(clazz : Class<T>) : List<T> {
        if(metadata == null)
            throw Exception("Metadata required")

        val res = mutableListOf<T>()


        for (row in rows) {

            if(clazz.packageName == "java.lang") {
                val v = cast(row[0], clazz)
                v?.let { res.add(it) }
                continue
            }

            val obj = clazz.getDeclaredConstructor().newInstance()
            val fields = clazz.declaredFields

            for(field in fields) {
                field.isAccessible = true
                val ann = field.getAnnotation(FromRDB::class.java)

                if(ann == null) {
                    continue
                }

                if(ann.typeClass.equals(field.type)) {
                    throw RuntimeException("Type mismatch")
                }

                val offset = columns.indexOf(ann.column)

                if(offset == -1) {
                    //TODO Report
                    continue
                }

                if(ann.isReference || ann.targetColumn.isNotBlank() && ann.targetTable.isNotBlank()) {
                    val isColumnString = columnTypes[offset] == SqlDataType.VARCHAR
                    val query = "SELECT ${ann.projection} FROM ${ann.targetTable} " +
                            "WHERE ${ann.targetColumn} = ${ if(isColumnString) "'${row[offset]}'" else row[offset]}"
                    val res = DataImporter.runQuery(query, metadata, QueryResult2::class.java)
                    val newObj = res.asObject( field.type )
                    field.set(obj, newObj.firstOrNull())
                    continue
                }




                val v = cast(row[offset], ann.typeClass.java)
                field.set(obj, v)


            }

            res.add(obj)
        }
        return res;
    }

    private fun <T> cast(value: String?, clazz: Class<T>): T? {
    value?.let {
        val cName = clazz.simpleName
        // TODO: Ajust PromptData4 to only use one type of date format ask chatGPT what type is better
        return when (cName) {
            "Integer" -> it.toInt() as T
            "Long" -> it.toLong() as T
            "Float" -> it.toFloat() as T
            "Double" -> it.toDouble() as T
            "LocalDateTime" -> java.time.LocalDateTime.parse(it.replace(" ", "T")) as T
            "String" -> it as T
            else -> null
        }
    }
    return null
}

}