package com.example.demogame

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demogame.databinding.ActivityItemselectionBinding

class ItemSelectionActivity : AppCompatActivity() {
    //gives access to xml elements
    private lateinit var binding: ActivityItemselectionBinding
    private lateinit var newRecyclerview : RecyclerView
    private lateinit var itemList: ArrayList<Item>
    private lateinit var itemAdapter: ItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //initialize the binding and set content view
        binding = ActivityItemselectionBinding.inflate(layoutInflater)
        setContentView(binding.root)



        newRecyclerview = binding.recyclerView
        newRecyclerview.layoutManager = LinearLayoutManager(this)
        newRecyclerview.setHasFixedSize(true)

        // Init and Load the list of items
        itemList = arrayListOf<Item>()
        loadItems()

        //init the item adapter and set it as the adapter for the recycler view
        itemAdapter = ItemAdapter(itemList)
        newRecyclerview.adapter=itemAdapter
        setupRecyclerAdapter()

        //set click listener for next step button

        binding.button.setOnClickListener {

            goToCardSetSelection()
        }
    }

    private fun goToCardSetSelection() {
        val intent = Intent(this, CardsetSelectionActivity::class.java)

        // Restructure the items for use by DB
        val activatedItems = ArrayList<String>()
        for (item in itemList) {
            if (item.identifier != null && item.activated) {
                activatedItems.add(item.identifier)
            }
        }
        val tinyDB =  TinyDB(applicationContext)
        val cardManager: CardManager = tinyDB.getObject("manager", CardManager::class.java)
        cardManager.itemList = activatedItems
        tinyDB.putObject("manager", cardManager)

        startActivity(intent)
    }

    private fun loadItems(){

        val identifiers = arrayOf(
            "dildo",
            "whip",
            "ice_cubes",
            "vibrator",
            "buttplug",
            "blindfold",
            "camera",
            "collar",
            "handcuffs",
            "nipple_clamps",
            "water"
        )
        val images = arrayOf(
            R.drawable.dildo,
            R.drawable.whip,
            R.drawable.ice_cubes,
            R.drawable.vibrator,
            R.drawable.buttplug,
            R.drawable.blindfold,
            R.drawable.camera,
            R.drawable.collar,
            R.drawable.handcuffs,
            R.drawable.nipple_clamps,
            R.drawable.water,
        )
        val descs = arrayOf(
            "A dildo",
            "A whip or similar spanking tool",
            "Some ice cubes in the freezer",
            "Your favorite orgasm dispenser",
            "An anal plug",
            "A blindfold",
            "An old camera with no internet connection \uD83D\uDE09",
            "A collar",
            "Some handcuffs",
            "Nipple clamps or clothespins (if you dare)",
            "A glass of water"
        )

        for(i in images.indices){
            val item = Item(images[i],descs[i], identifiers[i])
            itemList.add(item)
        }
    }

    private fun setupRecyclerAdapter(){
        newRecyclerview.adapter = itemAdapter
        newRecyclerview.layoutManager = LinearLayoutManager(this)

        itemAdapter.onItemClick = { item ->
            // Toggle the item activated
            item.activated = !item.activated
        }
    }


}