package com.example.demogame

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demogame.databinding.ActivityMainBinding


private const val s =
    "Level 0 - Includes non-sexual dares and some kissing and touching, but clothes stay on!"

class MainActivity : AppCompatActivity() {

    // Gives access to xml elements
    private lateinit var binding: ActivityMainBinding

    private lateinit var newRecyclerview : RecyclerView
    private lateinit var images: Array<Int>
    private lateinit var descs: Array<String>
    private lateinit var levelList: ArrayList<Item>
    private lateinit var itemAdapter: ItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        //init layout
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tinyDB = TinyDB(applicationContext)

        // Load or create the DB with a cardManager
        loadOrCreateDB(tinyDB)

        val cardManager: CardManager = tinyDB.getObject("manager", CardManager::class.java)

        // Init the names and gender switches from DB
        initPlayerNamesAndGenders(cardManager)

        //Level recycler view
        images = arrayOf(
            R.drawable.l0,
            R.drawable.l1,
            R.drawable.l2,
            R.drawable.l3,
        )
        descs = arrayOf(
            getString(R.string.level0desc),
            getString(R.string.level1desc),
            getString(R.string.level2desc),
            getString(R.string.level3desc)
        )

        newRecyclerview = binding.recyclerViewLevels
        newRecyclerview.layoutManager = LinearLayoutManager(this)
        newRecyclerview.setHasFixedSize(true)

        levelList = arrayListOf<Item>()
        loadLevels()

        // Init the item adapter and set it as the adapter for the recycler view for the levels
        itemAdapter = ItemAdapter(levelList)
        newRecyclerview.adapter=itemAdapter
        setupRecyclerAdapter()

        // The button to move to the next screen
        binding.mainActivityButton.setOnClickListener {
            goToPlayerSelectionMenu(cardManager, tinyDB)
        }
    }

    private fun initPlayerNamesAndGenders(cardManager: CardManager) {
        binding.player1Text.hint = cardManager.player1name
        binding.player2Text.hint = cardManager.player2name
        binding.femaleSwitch1.isChecked = !cardManager.player1HasDick
        binding.femaleSwitch2.isChecked = !cardManager.player2HasDick
    }

    private fun loadOrCreateDB(tinyDB: TinyDB) {
        val wasOpenedBefore = tinyDB.getBoolean("NOT_FIRST_TIME")
        if (wasOpenedBefore) {
            Log.d("TAG", "Not first time.")
        } else {
            tinyDB.putBoolean("NOT_FIRST_TIME", true)
            tinyDB.putObject("manager", CardManager())
            tinyDB.putListInt("levels", ArrayList<Int>())
            Log.d("TAG", "Fist time. Initialized manager and levels.")
        }
    }

    private fun goToPlayerSelectionMenu(
        cardManager: CardManager,
        tinyDB: TinyDB
    ) {
        // Get the selected levels (indices which are activated)
        val levelsSelected: ArrayList<Int> = levelList
            .mapIndexed { index, item -> if (item.activated) index else null }
            .filterNotNull()
            .toCollection(ArrayList())

        if (levelsSelected.isEmpty()){
            Toast.makeText(this, getString(R.string.selectOneLevel), Toast.LENGTH_LONG).show()
            return
        }

        val intent = Intent(this, PlayerChoiceActivity::class.java)
        val player1name: String = binding.player1Text.text.toString()
        val player2name: String = binding.player2Text.text.toString()

        val player1female: Boolean = binding.femaleSwitch1.isChecked
        val player2female: Boolean = binding.femaleSwitch2.isChecked

        if (player1name != "") cardManager.player1name = player1name
        if (player2name != "") cardManager.player2name = player2name
        cardManager.player1HasDick = !player1female
        cardManager.player2HasDick = !player2female



        // Update the tinyDB manager and levels
        tinyDB.putObject("manager", cardManager)
        tinyDB.putListInt("levels", levelsSelected)

        // Move to the player choice activity
        startActivity(intent)
    }

    private fun loadLevels(){
        for(i in images.indices){
            val item = Item(images[i],descs[i])
            levelList.add(item)
        }
    }

    private fun setupRecyclerAdapter(){
        newRecyclerview.adapter = itemAdapter
        newRecyclerview.layoutManager = LinearLayoutManager(this)

        itemAdapter.onItemClick = { item ->

            // Toggle the level.
            item.activated = !item.activated

        }
    }

}

