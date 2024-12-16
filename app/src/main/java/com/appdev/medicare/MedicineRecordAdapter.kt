package com.appdev.medicare

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.appdev.medicare.model.MedicineRecord

class MedicineRecordAdapter(private val recordList: List<MedicineRecord>) : RecyclerView.Adapter<MedicineRecordAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_medicine_record, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int): Unit {
        val record = recordList[position]
        holder.diseaseName.text = record.name
        holder.patientName.text = record.patientName
        holder.recordTime.text = record.recordTime
    }

    override fun getItemCount(): Int {
        return recordList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val diseaseName: TextView = itemView.findViewById(R.id.textDiseaseName)
        val patientName: TextView = itemView.findViewById(R.id.textPatientName)
        val recordTime: TextView = itemView.findViewById(R.id.textTime)
    }
}