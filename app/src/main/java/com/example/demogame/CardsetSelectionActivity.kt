package com.example.demogame

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demogame.databinding.ActivityCardsetselectionBinding


class CardsetSelectionActivity : AppCompatActivity() {
    //gives access to xml elements
    private lateinit var binding: ActivityCardsetselectionBinding
    private lateinit var newRecyclerview : RecyclerView

    private lateinit var itemList: ArrayList<Item>
    private lateinit var itemAdapter: ItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //initialize the binding and set content view
        binding = ActivityCardsetselectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        newRecyclerview = binding.recyclerViewKinks
        newRecyclerview.layoutManager = LinearLayoutManager(this)
        newRecyclerview.setHasFixedSize(true)


        itemList = arrayListOf<Item>()
        loadCardSets()

        //init the item adapter and set it as the adapter for the recycler view
        itemAdapter = ItemAdapter(itemList)
        newRecyclerview.adapter=itemAdapter
        setupRecyclerAdapter()

        //set click listener for next step button

        binding.buttonStart.setOnClickListener {

            val intent = Intent(this, GameActivity::class.java)

            //get positions that were clicked (selected items) to pass along
            val itemSelectedArray= BooleanArray(itemList.size)
            for (i in itemList.indices){
                itemSelectedArray[i] = itemList[i].activated
            }
            startActivity(intent)
        }
    }

    private fun loadCardSets(){

        val images = arrayOf(
            R.drawable.sports,
            R.drawable.heart,
            R.drawable.handcuffs,
            R.drawable.spanking,
            R.drawable.feet,
            R.drawable.buttplug,
        )
        val identifiers = arrayOf(
            "sports",
            "cute",
            "bdsm",
            "spanking",
            "feet",
            "anal_play"
        )
        val descs = arrayOf(
            "The spicy workout routine for you and your partner \uD83D\uDE0F",
            "Collection of cards containing complete cuteness overload! \uD83D\uDC96",
            "Collection of dominant-submissive dares and more rough stuff.",
            "Cards for some good old-fashioned spankings, slaps, and more. \uD83D\uDC4B",
            "Cards for those who are toe-tally in love with feet! \uD83E\uDDB6",
            "Collection with extra cards for backdoor fun.",
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
            // Toggle if item is activated
            item.activated = !item.activated
        }
    }


}