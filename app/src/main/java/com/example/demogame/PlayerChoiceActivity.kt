package com.example.demogame

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.demogame.databinding.ActivityPlayerchoiceBinding

class PlayerChoiceActivity(
) : AppCompatActivity(){

    // Gives access to XML elements
    private lateinit var binding: ActivityPlayerchoiceBinding

    private var currPlayer = 1

    private lateinit var player1choices: BooleanArray
    private lateinit var player2choices: BooleanArray
    private var player1name = ""
    private var player2name = ""

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        //initialize the binding and set content view
        binding = ActivityPlayerchoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init choices arrays (to empty / false for everything
        player1choices= BooleanArray(6)
        player2choices=BooleanArray(6)

        //get player1 and 2 names to display them on the top
        val tinyDB =  TinyDB(applicationContext)
        val cardManager: CardManager = tinyDB.getObject("manager", CardManager::class.java)
        player1name = cardManager.player1name
        player2name = cardManager.player2name

        Log.d("TAG Levels activated", tinyDB.getListInt("levels").toString())

        val playerNameTextView: TextView = binding.displayPlayerNameChoice
        if (currPlayer==1){
            playerNameTextView.text = player1name
        }
        else if (currPlayer==2){
            playerNameTextView.text=player2name
        }

        // Move to the second player's selection or to the item selection screen
        binding.buttonNextPlayer.setOnClickListener {
            goToNextPlayerOrItemSelection()
        }

    }

    private fun goToNextPlayerOrItemSelection() {
        savePlayerChoices()

        if (currPlayer == 1) {
            togglePlayerName()
            currPlayer = 2
            resetCheckboxes()
        } else {
            navigateToItemSelection()
        }
    }

    private fun savePlayerChoices() {
        val choices = if (currPlayer == 1) player1choices else player2choices
        choices[0] = binding.checkBoxDominant.isChecked
        choices[1] = binding.checkBoxSubmissive.isChecked
        choices[2] = binding.checkboxGiveOral.isChecked
        choices[3] = binding.checkBoxReceiveOral.isChecked
        choices[4] = binding.checkboxGiveAnal.isChecked
        choices[5] = binding.checkboxReceiveAnal.isChecked
    }

    private fun navigateToItemSelection() {
        // Update the player choices in the DB's card manager and move on
        val intent = Intent(this, ItemSelectionActivity::class.java)
        val tinyDB =  TinyDB(applicationContext)
        val cardManager: CardManager = tinyDB.getObject("manager", CardManager::class.java)
        cardManager.player1Choices = player1choices
        cardManager.player2Choices = player2choices
        tinyDB.putObject("manager", cardManager)
        startActivity(intent)
    }

    private fun togglePlayerName(){
        val playerNameView: TextView = binding.displayPlayerNameChoice
        if (currPlayer==1){
            if (player2name != "") {
                playerNameView.text = player2name
            } else {
                playerNameView.text = getString(R.string.player2)
            }
        }
        else if (currPlayer==2){
            if (player1name != "") {
                playerNameView.text = player1name
            } else {
                playerNameView.text = getString(R.string.player1)
            }
        }
    }


    private fun resetCheckboxes(){
        binding.checkBoxDominant.isChecked = false
        binding.checkboxGiveOral.isChecked = false
        binding.checkboxGiveAnal.isChecked = false
        binding.checkboxReceiveAnal.isChecked = false
        binding.checkBoxSubmissive.isChecked = false
        binding.checkBoxReceiveOral.isChecked = false

    }

    override fun onBackPressed() {

        //revert to player1 if player2 is picking
        if (currPlayer == 1) {
            super.onBackPressed()
        } else {
            togglePlayerName()
            currPlayer = 1
            resetCheckboxes()
        }

    }


}