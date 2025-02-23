package com.example.demogame

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demogame.databinding.ActivityKinkselectionBinding


class KinkSelectionActivity : AppCompatActivity() {
    //gives access to xml elements
    private lateinit var binding: ActivityKinkselectionBinding
    private lateinit var newRecyclerview : RecyclerView

    private lateinit var images: Array<Int>
    private lateinit var descs: Array<String>
    private lateinit var itemList: ArrayList<Item>
    private lateinit var tmpList: ArrayList<Item>
    private lateinit var itemAdapter: ItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //initialize the binding and set content view
        binding = ActivityKinkselectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        images = arrayOf(
            R.drawable.spanking,
            R.drawable.feet,
            R.drawable.neck,
            R.drawable.sports,
            R.drawable.dirty_talk,
            R.drawable.buttplug,
            R.drawable.breath_play,
            R.drawable.face_slap
            )

        descs= arrayOf(
            "A good old-fashioned spanking",
            "Feet",
            "The neck is an erogenous zone too!",
            "Some sexy exercise never killed nobody!",
            "Talk dirty to me!",
            "Anal play",
            "Breath play (please be careful, we don't want to get sued!)",
            "Ouch! That's my face!",
        )

        newRecyclerview = binding.recyclerViewKinks
        newRecyclerview.layoutManager = LinearLayoutManager(this)
        newRecyclerview.setHasFixedSize(true)


        itemList = arrayListOf<Item>()
        tmpList = arrayListOf<Item>()
        addItems()

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

            intent.putExtra("selectedKinks", itemSelectedArray)
            startActivity(intent)
        }
    }

    private fun addItems(){
        for(i in images.indices){
            val item = Item(images[i],descs[i])
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