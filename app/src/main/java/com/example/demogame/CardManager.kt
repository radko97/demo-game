package com.example.demogame

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Timer
import kotlin.concurrent.timerTask
import kotlin.random.Random

class CardManager(
    var player1name: String = "Player 1",
    var player2name: String = "Player 2",
    var itemList: ArrayList<String> = ArrayList(),
    var cardsetList: ArrayList<String> = ArrayList(),
    var player1Choices: BooleanArray = booleanArrayOf(false, false, false, false, false, false),
    var player2Choices: BooleanArray = booleanArrayOf(false, false, false, false, false, false),
    var player1HasDick: Boolean = false,
    var player2HasDick: Boolean = false,
) {
    private var cardDeck: ArrayList<Card> = ArrayList()

    fun loadCards(levelList: ArrayList<Int>): Boolean {
        val baseUrl = "https://prominent-daisi-detectiveai-0befacd3.koyeb.app"
        // Build the filters
        val filters = JSONObject().apply {
            put("itemList", JSONArray(itemList))  // Convert itemList to JSONArray
            put("cardsetList", JSONArray(cardsetList))  // Convert cardsetList to JSONArray
            put("player1Choices", JSONArray(player1Choices.map { it }))  // Convert player1Choices to JSONArray
            put("player2Choices", JSONArray(player2Choices.map { it }))  // Convert player2Choices to JSONArray
            put("player1HasDick", player1HasDick)
            put("player2HasDick", player2HasDick)
        }

        // Prepare the JSON payload
        val payload = JSONObject().apply {
            put("filters", filters)  // filters is already a JSONObject
            put("level_list", JSONArray(levelList.map { it }))  // Convert levelList (List<Int>) to JSONArray
        }


        return try {
            val url = URL("$baseUrl/cards")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.outputStream.use { os ->
                os.write(payload.toString().toByteArray(Charsets.UTF_8))
            }

            // Perform the request
            connection.connect()

            Log.d("TAG", payload.toString())

            // Read the response
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = InputStreamReader(connection.inputStream)
                val responseBody = reader.readText()
                val cardsJsonArray = JSONArray(responseBody)

                cardDeck = ArrayList()

                for (i in 0 until cardsJsonArray.length()) {
                    val cardJson = cardsJsonArray.getJSONObject(i)
                    val cardResponse = Card(
                        id = cardJson.getInt("id"),
                        text = cardJson.getString("text"),
                        level = cardJson.getInt("level"),
                        rng = cardJson.getInt("rng"),
                        jokerCode = cardJson.getInt("jokerCode"),
                        time = cardJson.getInt("time")
                    )
                    cardDeck.add(cardResponse)
                }

                true
            } else {
                Log.d("TAG", connection.toString())
                Log.d("TAG", connection.responseMessage)
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("TAG", e.toString())
            false
        }
    }

    fun pullCard(level: Int): Card {
        val ids = cardDeck.asSequence()
            .filter { it.level == level }
            .map { it.id }.toList()

        val defaultCard = Card(
            id = 0,
            text = "Out of fitting cards",
            time = 0,
            level = -1,
            rng = 0,
            jokerCode = 0
        )
        // Out of fitting cards
        if (ids.isEmpty()) {
            return defaultCard
        }

        val rindex = Random(System.currentTimeMillis()).nextInt(ids.size)
        val pickedId = ids[rindex]
        val pickedCard = cardDeck.find { it.id == pickedId }!!

        pickedCard.text = pickedCard.text.replace("ActPlayer", player1name).replace("PasPlayer", player2name)

        // Remove the pulled card from deck
        cardDeck = cardDeck.asSequence().filter { it.id != pickedId }.toList() as ArrayList<Card>

        // Return it
        return pickedCard
    }
}
