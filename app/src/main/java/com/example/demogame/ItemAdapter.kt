package com.example.demogame

import android.graphics.Color
import android.graphics.Color.WHITE
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView


class ItemAdapter(private val itemList : ArrayList<Item>) : RecyclerView.Adapter<ItemAdapter.MyViewHolder>(),Filterable {

    var onItemClick: ((Item) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.list_item,
            parent,false)

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val currentItem = itemList[position]
        holder.itemImage.setImageResource(currentItem.icon)
        holder.itemDesc.text = currentItem.desc
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val itemImage : ShapeableImageView = itemView.findViewById(R.id.itemImage)
        val itemDesc : TextView = itemView.findViewById(R.id.itemDescription)

        //define the item click listener to change colors depending on item state
        //TODO: Unhardcode colors?

        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(itemList[adapterPosition])
                if (itemList[adapterPosition].activated) {
                    this.itemView.setBackgroundColor(Color.parseColor("#117A16"))
                }
                else{
                    this.itemView.setBackgroundColor(Color.parseColor("#DA3813"))
                }
            }

        }
    }

    override fun getFilter(): Filter {
        TODO("Not yet implemented")
    }

}