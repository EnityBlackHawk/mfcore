package org.utfpr.mf.llm

import com.google.gson.Gson
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.Capability.RESPONSE_FORMAT_JSON_SCHEMA
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.request.ResponseFormat
import dev.langchain4j.model.chat.request.ResponseFormatType.JSON
import dev.langchain4j.model.chat.request.json.JsonArraySchema
import dev.langchain4j.model.chat.request.json.JsonObjectSchema
import dev.langchain4j.model.chat.request.json.JsonSchema
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.output.Response
import dev.langchain4j.model.output.structured.Description
import dev.langchain4j.service.AiServices
import dev.langchain4j.service.output.JsonSchemas
import org.utfpr.mf.descriptor.CachePolicy
import org.utfpr.mf.descriptor.LLMServiceDesc
import org.utfpr.mf.json.JsonSchemaList
import org.utfpr.mf.tools.CodeSession
import org.utfpr.mf.tools.MfCacheController
import java.io.IOException
import java.time.Duration
import java.util.function.Function

class LLMService(desc: LLMServiceDesc) : CodeSession("LLMService", LastSet), ChatAssistant {

    private val chatLanguageModel: ChatLanguageModel
    private val chatAssistant: ChatAssistant
    private val cachePolicy: CachePolicy = desc.cachePolicy
    private val cacheController: MfCacheController
    private val description : LLMServiceDesc

    init {

        cacheController = MfCacheController(desc.cacheDir)
        chatLanguageModel = OpenAiChatModel.OpenAiChatModelBuilder()
            .apiKey(desc.llm_key)
            .modelName(desc.model)
            .maxRetries(1)
            .logRequests(desc.logRequest)
            .timeout(Duration.ofDays(1))
            .logResponses(desc.logResponses)
            .temperature(desc.temp)
            .build()
        chatAssistant = AiServices.builder(ChatAssistant::class.java).chatLanguageModel(chatLanguageModel).build()
        description = desc
    }

    fun getDescription() : LLMServiceDesc {
        return description
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
        var count = 0;

        result = try {
            func.apply(prompt)
        } catch (e : Exception) {
            ERROR("Unable to parse LLM response: " + e.message)
            count++;
            null
        }

        if(result == null) {
            cacheController.save("LastError", result)
            throw RuntimeException("Unable do parse LLM response. Tried $count times")
        }

        try {
            cacheController.save(md5, result)
        } catch (e: IOException) {
            ERROR("Unable to save this request as cache: " + e.message)
        }

        return result
    }

    private fun generateJsonSchemaFrom(clazz : Class<*>, description : String? = null) : JsonObjectSchema {
        val jsonSchemaBuilder = JsonObjectSchema.builder()
            .description(description ?: "")


        for (field in clazz.declaredFields) {

            val ann = field.getAnnotation(Description::class.java) ?: continue
            when (field.type.simpleName) {
                "String" -> jsonSchemaBuilder.addStringProperty(field.name, ann.value.first())
                "Int", "Long", "Double", "Float"   -> jsonSchemaBuilder.addIntegerProperty(field.name, ann.value.first())
                "Boolean" -> jsonSchemaBuilder.addBooleanProperty(field.name, ann.value.first())
                "HashMap" -> jsonSchemaBuilder.addProperty(field.name, JsonObjectSchema.builder()
                    .description(ann.value.first())
                    .additionalProperties(true)
                    .build())
                clazz.simpleName -> continue
                else -> {
                    val nestedJsonSchema = generateJsonSchemaFrom(field.type, ann.value.first())
                    jsonSchemaBuilder.addProperty(field.name, nestedJsonSchema)
                }
            }

        }

        return jsonSchemaBuilder.build()
    }

    private fun internalGetJsonSchemaList(text: String): LLMResponseJsonSchema {
        val responseFormat = ResponseFormat.builder()
            .type(JSON)
            .jsonSchema(JsonSchema.builder()
                .name("JsonSchema")
                .rootElement(
                    JsonObjectSchema.builder()
                    .description("Root element")
                    .addStringProperty("name", "Name of the collection")
                        .addProperty("schemas", JsonArraySchema.builder()
                            .description("List of different collections")
                            .items(
                                generateJsonSchemaFrom(org.utfpr.mf.json.JsonSchema::class.java, "Collection")
                            )
                            .build())
                    .build()
                )
                .build()
            )
            .build()
        val chatRequest = ChatRequest.builder()
            .messages(UserMessage(text))
            .responseFormat(responseFormat)
            .build()

        val chatResponse = chatLanguageModel.chat(chatRequest)
        val gson = Gson()
        return gson.fromJson(chatResponse.aiMessage().text(), LLMResponseJsonSchema::class.java)
    }

    override fun getJsonSchemaList(text: String): LLMResponseJsonSchema {
        return process(text, LLMResponseJsonSchema::class.java, chatAssistant::getJsonSchemaList)
    }

    override fun chat(text: String): String {
        return process(text, String::class.java, chatAssistant::chat)
    }


    override fun getRelations(text: String): String {
        return process(text, String::class.java, chatAssistant::getRelations)
    }

    override fun getJson(text: String): String {
        return process(text, String::class.java, chatAssistant::getJson)
    }
}