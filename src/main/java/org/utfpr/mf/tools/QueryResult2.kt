package org.utfpr.mf.tools

import org.springframework.data.annotation.Id
import org.utfpr.mf.annotation.FromRDB
import org.utfpr.mf.metadata.DbMetadata
import kotlin.reflect.cast
import java.sql.ResultSet

class QueryResult2(resultSet : ResultSet, metadata: DbMetadata?) : QueryResult(resultSet, metadata) {



    override fun <T> asObject(clazz : Class<T>) : List<T> {
        if(metadata == null)
            throw Exception("Metadata required")
        val res = mutableListOf<T>()
        for (row in rows) {
            val obj = clazz.getDeclaredConstructor().newInstance()
            val fields = clazz.declaredFields

            for(field in fields) {
                field.isAccessible = true
                val ann = field.getAnnotation(FromRDB::class.java)
                val offset = columns.indexOf(ann.column)

                if(ann.isReference) {
                    val query = "SELECT * FROM ${ann.table} WHERE ${ann.column} = ${row[offset]}"
                    val res = DataImporter.runQuery(query, metadata, QueryResult2::class.java)
                    val newObj = res.asObject( field.type )
                    field.set(obj, newObj.first())
                    continue
                }

                if(offset == -1) {
                    throw NotImplementedError("Column not found")
                }
                val v = ann.typeClass.cast(row[offset]);
                field.set(obj, v)


            }

            res.add(obj)
        }
        return res;
    }

}