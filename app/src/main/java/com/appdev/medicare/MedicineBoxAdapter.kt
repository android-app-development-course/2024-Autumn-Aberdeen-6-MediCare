package com.appdev.medicare

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.appdev.medicare.model.BoxData

class MedicineBoxAdapter(private val boxList: List<BoxData>) : RecyclerView.Adapter<MedicineBoxAdapter.ViewHolder>() {

    private var onAddMedicineButtonClickListener: OnAddButtonClickListener? = null

    interface OnAddButtonClickListener {
        fun onAddButtonClick(box: BoxData)
    }

    fun setOnAddButtonClickListener(listener: OnAddButtonClickListener) {
        onAddMedicineButtonClickListener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_medicine_box, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int): Unit {
        val record = boxList[position]
        holder.boxName.text = record.name

        holder.addMedicineButton.setOnClickListener {
            // 定义接口
            Log.d("Click Now","step1")
            onAddMedicineButtonClickListener?.onAddButtonClick(record)
        }
    }

    override fun getItemCount(): Int {
        return boxList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val boxName: TextView = itemView.findViewById(R.id.textBoxName)
        val addMedicineButton: ImageButton = itemView.findViewById(R.id.buttonAddMedicine)
    }
}