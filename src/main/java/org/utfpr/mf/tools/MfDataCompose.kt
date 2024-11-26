package org.utfpr.mf.tools

import org.utfpr.mf.annotation.FromRDB
import org.utfpr.mf.metadata.DbMetadata

class MfDataCompose(
    private val metadata: DbMetadata
) {

    fun <T> composeData(clazz : Class<T>) : List<T>  {

        val obj = clazz.getDeclaredConstructor().newInstance()
        val fields = clazz.declaredFields

        for (field in fields) {
            val ann = field.getAnnotation(FromRDB::class.java)



        }

        throw NotImplementedError("Not implemented yet")
    }

}