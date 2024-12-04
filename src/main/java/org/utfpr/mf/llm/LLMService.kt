package org.utfpr.mf.llm

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.output.Response
import dev.langchain4j.service.AiServices
import org.utfpr.mf.descriptor.CachePolicy
import org.utfpr.mf.descriptor.LLMServiceDesc
import org.utfpr.mf.json.JsonSchemaList
import org.utfpr.mf.tools.CodeSession
import org.utfpr.mf.tools.MfCacheController
import java.io.IOException
import java.util.function.Function

class LLMService(desc: LLMServiceDesc) : CodeSession("LLMService", LastSet), ChatAssistant {

    private val chatLanguageModel: ChatLanguageModel
    private val chatAssistant: ChatAssistant
    private val cachePolicy: CachePolicy = desc.cachePolicy
    private val cacheController: MfCacheController

    init {

        cacheController = MfCacheController(desc.cacheDir)
        chatLanguageModel = OpenAiChatModel.OpenAiChatModelBuilder()
            .apiKey(desc.llm_key)
            .modelName(desc.model)
            .maxRetries(1)
            .logRequests(desc.logRequest)
            .logResponses(desc.logResponses)
            .temperature(desc.temp)
            .build()
        chatAssistant = AiServices.builder(ChatAssistant::class.java).chatLanguageModel(chatLanguageModel).build()
    }

    private fun <T> process(prompt: String, clazz : Class<T>, func: Function<String, T>): T {
        if (cachePolicy == CachePolicy.NO_CACHE) {
            return func.apply(prompt)
        }

        val md5 = MfCacheController.generateMD5(prompt)
        var result : T?
        try {
            result = cacheController.load(clazz, md5)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        if (result != null) {
            INFO("Found an usable cache for this request!")
            return result
        }

        if (cachePolicy == CachePolicy.CACHE_ONLY) {
            throw RuntimeException("No cache found, but CACHE_ONLY")
        }

        result = func.apply(prompt)

        try {
            cacheController.save(md5, result)
        } catch (e: IOException) {
            ERROR("Unable to save this request as cache: " + e.message)
        }

        return result
    }

    override fun getJsonSchemaList(text: String): LLMResponseJsonSchema {
        return process(text, LLMResponseJsonSchema::class.java, chatAssistant::getJsonSchemaList)
    }

    override fun chat(text: String): Response<AiMessage> {
        return process(text, Response::class.java, chatAssistant::chat) as Response<AiMessage>
    }


    override fun getRelations(text: String): Response<AiMessage> {
        return process(text, Response::class.java, chatAssistant::getRelations) as Response<AiMessage>
    }

    override fun getJson(text: String): Response<AiMessage> {
        return process(text, Response::class.java, chatAssistant::getJson) as Response<AiMessage>
    }
}