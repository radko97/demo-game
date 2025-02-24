package com.example.demogame

import android.util.Log
import kotlin.random.Random
import com.opencsv.CSVReader
import java.io.InputStreamReader

class CardManager(
    var player1name: String = "Player 1",
    var player2name: String = "Player 2",
    var itemList: ArrayList<String> = ArrayList(),
    var cardsetList: ArrayList<String> = ArrayList(),
    var player1Choices: BooleanArray = booleanArrayOf(false, false, false, false, false, false),
    var player2Choices: BooleanArray = booleanArrayOf(false, false, false, false, false, false),
    var player1HasDick: Boolean = false,
    var player2HasDick: Boolean = false
) {

    private var actHasDick = player1HasDick
    private var pasHasDick = player2HasDick
    private var cards: List<Card>  = listOf()
    private var idGiver: Int = 1

    private var defaultCard = Card(
        id=0,
        reversible = false,
        text = "Out of fitting cards",
        time = 0,
        level = -1,
        cardSet = "",
        item= "",
        pasNeedsDick = false,
        pasNeedsPussy = false,
        actNeedsPussy = false,
        actNeedsDick = false,
        domination = false,
        oral =false,
        anal=false,
        rng=0,
        jokerCode = 0
    )

    fun addCardsFromCsv(fileName: String){

        val cardInputStream = this::class.java.getResourceAsStream(fileName)

        fun toBooleanHelper(string: String): Boolean {
            if (string == "1" || string == "true") {
                return true
            }
            return false
        }

        fun toIntHelper(string: String): Int {
            if (string == "") return 0
            return string.toInt()
        }

        val reader = CSVReader(InputStreamReader(cardInputStream!!))

        var dominant = player1Choices[0] && player2Choices[1]  // Act ok with dom, pas ok with sub
        var receiveOral = player1Choices[3] && player2Choices[2]  // ActPlayer is the one RECEIVING oral
        var anal = player1Choices[4] && player2Choices[5]  // Player1 receives, Player2 gives


        // Read header
        val header = reader.readNext()
        val csvData = reader.readAll()

        val normalCards: List<Card> = csvData
            .asSequence()
            .filter { it.isNotEmpty() }
            .map { v ->
                idGiver += 1
                Card(
                    id = idGiver,
                    text = v[0].replace("ActPlayer", player1name).replace("PasPlayer", player2name).replace("XX", "🎲"),
                    reversible = toBooleanHelper(v.getOrNull(1) ?: ""),
                    time = toIntHelper(v.getOrNull(2) ?: ""),
                    level = toIntHelper(v.getOrNull(3) ?: ""),
                    item = v.getOrNull(4) ?: "",
                    cardSet = v.getOrNull(5) ?: "",
                    actNeedsDick = toBooleanHelper(v.getOrNull(6) ?: ""),
                    actNeedsPussy = toBooleanHelper(v.getOrNull(7) ?: ""),
                    pasNeedsDick = toBooleanHelper(v.getOrNull(8) ?: ""),
                    pasNeedsPussy = toBooleanHelper(v.getOrNull(9) ?: ""),
                    oral = toBooleanHelper(v.getOrNull(10) ?: ""),
                    anal = toBooleanHelper(v.getOrNull(11) ?: ""),
                    domination = toBooleanHelper(v.getOrNull(12) ?: ""),
                    rng = toIntHelper(v.getOrNull(13) ?: ""),
                    jokerCode = toIntHelper(v.getOrNull(14) ?: "")
                )
            }
            // Now filter according to player preferences
            .filter { it.cardSet in cardsetList || it.cardSet!!.isEmpty() }
            .filter { it.item in itemList || it.item!!.isEmpty() }
            .filter { !it.actNeedsDick || actHasDick }
            .filter { !it.pasNeedsDick || pasHasDick }
            .filter { !it.actNeedsPussy || !actHasDick } // If act doesn’t have a dick, they have a pussy
            .filter { !it.pasNeedsPussy || !pasHasDick }
            .filter { !it.domination || dominant }  // Card doesn’t include domination or domination is okay
            .filter { !it.oral || receiveOral }
            .filter { !it.anal || anal }
            .toList()

        //now act and pas reverse roles
        dominant = player1Choices[1] && player2Choices[0]  //act ok with dom, pas ok with sub
        receiveOral = player1Choices[2] && player2Choices[3]//for oral the actPlayer is the one RECEIVING oral, to allow for comb with dom
        anal = player1Choices[5] && player2Choices[4]//player1 receives, player2gives

        //for convenience of card creation, reversible cards are generated also for player 2 as ActPlayer
        val reversedCards: List<Card> = csvData
            .asSequence()
            .filter { it.isNotEmpty() }
            .map {v ->
                idGiver += 1
                Card(id = idGiver,
                    text = v[0].replace("ActPlayer", player2name).replace("PasPlayer", player1name).replace("XX", "🎲" ),
                    reversible = toBooleanHelper(v[1]),
                    time = toIntHelper(v[2]),
                    level = toIntHelper(v[3]),
                    item = v[4],
                    cardSet = v[5],
                    pasNeedsDick = toBooleanHelper(v[6]),
                    pasNeedsPussy = toBooleanHelper(v[7]),
                    actNeedsDick = toBooleanHelper(v[8]),
                    actNeedsPussy = toBooleanHelper(v[9]),
                    oral = toBooleanHelper(v[10]),
                    anal = toBooleanHelper(v[11]),
                    domination = toBooleanHelper(v[12]),
                    rng = toIntHelper(v[13]),
                    jokerCode= -toIntHelper(v[14]))
            }
            .filter { it.cardSet in cardsetList || it.cardSet!!.isEmpty() }
            .filter { it.item in itemList || it.item!!.isEmpty() }
            .filter { !it.actNeedsDick  || pasHasDick}  //doesnt need a dick or has one...
            .filter { !it.pasNeedsDick  || actHasDick}
            .filter { !it.actNeedsPussy || !pasHasDick} //if act doesnt have a dick, they have a pussy
            .filter { !it.pasNeedsPussy || !actHasDick}
            .filter { !it.domination || dominant}  //card doesnt include domination or domination is ok
            .filter { !it.oral || receiveOral}
            .filter { !it.anal || anal}
            .filter { it.reversible }.toList() //filter out non-reversible cards

        // Finally add the cards from the tsv
        cards = cards + normalCards + reversedCards

    }


    private fun printCard(i: Int){
        Log.d("TAG Id / Text", cards[i].id.toString()+"/ "+ cards[i].text)
    }

    fun printCards(){
        for (i in cards.indices){
            printCard(i)
        }
    }

    fun pullCard(level: Int): Card {

        val ids = cards.asSequence()
            .filter{ it.level == level }
            .map { it.id }.toList()

        //out of fitting cards
        if (ids.isEmpty()){
            return defaultCard
        }

        val rindex = Random(System.currentTimeMillis()).nextInt(ids.size)
        val pickedId = ids[rindex]
        val pickedCard = cards.find { it.id==pickedId}!!

        // Remove the pulled card from deck
        cards = cards.asSequence().filter { it.id != pickedId }.toList()

        //return it
        return pickedCard
    }

    fun getNumberOfCards(): Int{
        return cards.size
    }
}
