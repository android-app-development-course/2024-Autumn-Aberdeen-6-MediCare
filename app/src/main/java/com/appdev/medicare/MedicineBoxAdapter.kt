package com.appdev.medicare

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.appdev.medicare.model.MedicineBox

class MedicineBoxAdapter(private val boxList: List<MedicineBox>) : RecyclerView.Adapter<MedicineBoxAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_medicine_box, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int): Unit {
        val record = boxList[position]
        holder.boxName.text = record.name
    }

    override fun getItemCount(): Int {
        return boxList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val boxName: TextView = itemView.findViewById(R.id.textBoxName)
    }
}