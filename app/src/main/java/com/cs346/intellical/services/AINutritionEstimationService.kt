package com.cs346.intellical.services

import android.graphics.BitmapFactory
import android.util.Base64
import com.cs346.intellical.BuildConfig
import com.cs346.intellical.domain.MealEstimate
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONArray
import org.json.JSONObject

class AINutritionEstimationService : NutritionEstimationService {

    private val gson = Gson()
    private val apiKey = BuildConfig.GEMINI_API_KEY

    private val prompt = "You are an expert nutritionist. Analyze this food image and return a JSON object with the nutritional estimate. " +
        "Return ONLY a valid, parseable JSON object without any markdown formatting. " +
        "The JSON object must have the following exact keys: " +
        "name (String): A short accurate name of the meal, " +
        "description (String): A brief 1-2 sentence caption describing the meal and its key nutritional highlights, " +
        "calories (Integer): Estimated total calories between 50 and 3000, " +
        "proteinGrams (Integer): Estimated total protein in grams between 0 and 500, " +
        "carbsGrams (Integer): Estimated total carbohydrates in grams between 0 and 500, " +
        "fatGrams (Integer): Estimated total fat in grams between 0 and 500, " +
        "healthScore (Integer): A score from 1 to 10 based on how generally healthy it is, " +
        "ingredients (Array of Strings): 3 to 6 main visible ingredients, " +
        "confidence (Float): A value from 0.0 to 1.0 representing your confidence in this breakdown."

    override suspend fun estimateFromImage(imageBytes: ByteArray): MealEstimate = withContext(Dispatchers.IO) {
        if (imageBytes.isEmpty()) {
            throw Exception("No image provided to the AI Service.")
        }

        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            ?: throw Exception("Failed to decode image bytes into a Bitmap.")

        val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
        val requestBody = buildRequestBody(base64Image)

        val rawJson = executeRequest(requestBody)

        try {
            parseResponse(rawJson)
        } catch (firstError: Exception) {
            // Retry once on parse failure
            val retryJson = executeRequest(requestBody)
            try {
                parseResponse(retryJson)
            } catch (retryError: Exception) {
                throw Exception("Failed to parse AI response after retry: ${retryError.message}\nRaw: $retryJson")
            }
        }
    }

    private fun buildRequestBody(base64Image: String): String {
        val textPart = JSONObject().put("text", prompt + " Final response must be a single JSON object.")
        val inlineData = JSONObject()
            .put("mime_type", "image/jpeg")
            .put("data", base64Image)
        val imagePart = JSONObject().put("inline_data", inlineData)

        val parts = JSONArray().put(textPart).put(imagePart)
        val content = JSONObject().put("parts", parts)
        val contents = JSONArray().put(content)

        val generationConfig = JSONObject()
            .put("temperature", 0.0)
            .put("topP", 1.0)
            .put("topK", 1)
            .put("seed", 42)
            .put("response_mime_type", "application/json")

        return JSONObject()
            .put("generationConfig", generationConfig)
            .put("contents", contents)
            .toString()
    }

    private fun executeRequest(requestBody: String): String {
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        connection.outputStream.use { os ->
            os.write(requestBody.toByteArray())
        }

        val responseCode = connection.responseCode
        val responseStream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
        val responseText = responseStream.bufferedReader().readText()

        if (responseCode !in 200..299) {
            throw Exception("Gemini API error ($responseCode): $responseText")
        }

        return responseText
    }

    private fun parseResponse(responseText: String): MealEstimate {
        val jsonResponse = JsonParser.parseString(responseText).asJsonObject
        val candidateText = jsonResponse
            .getAsJsonArray("candidates")
            .get(0).asJsonObject
            .getAsJsonObject("content")
            .getAsJsonArray("parts")
            .get(0).asJsonObject
            .get("text").asString

        val cleanedJson = candidateText.replace("```json", "").replace("```", "").trim()
        return gson.fromJson(cleanedJson, MealEstimate::class.java)
    }
}
