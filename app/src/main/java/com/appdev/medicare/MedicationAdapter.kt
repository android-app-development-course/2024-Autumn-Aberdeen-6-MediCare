package com.appdev.medicare

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.appdev.medicare.model.DateItem
import com.appdev.medicare.model.MedicationData


class MedicationAdapter(
    private val dateItem: DateItem,
    private val onDeleteClick: (MutableList<MedicationData>, DateItem, MedicationData) -> Unit
) : RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder>() {

    private var medicationList = dateItem.medicationData!!
    private lateinit var context: Context

    inner class MedicationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textMedicationName: TextView = itemView.findViewById(R.id.textMedicationName)
        val textDosageInfo: TextView = itemView.findViewById(R.id.textDosageInfo)
        val buttonDelete: ImageButton = itemView.findViewById(R.id.buttonDelete)
        val timeCheckBoxLayout: LinearLayout = itemView.findViewById(R.id.timeCheckBoxLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_medication, parent, false)
        context = parent.context
        return MedicationViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicationViewHolder, position: Int) {
        val medication = medicationList[position]
        holder.textMedicationName.text = medication.medicationName
        holder.textDosageInfo.text = context.getString(R.string.dosageWithData, medication.dosage)

        val dailyIntakeTimes = medication.dailyIntakeTimes

        holder.itemView.setOnClickListener {
            // 点击小卡片，进入详细页逻辑
            showDetailsDialog(holder.itemView.context, medication)
        }

        holder.timeCheckBoxLayout.removeAllViews()
        var currentRow: LinearLayout? = null
        for ((index, time) in dailyIntakeTimes.withIndex()) {
            if (index % 5 == 0) {
                currentRow = LinearLayout(holder.itemView.context)
                currentRow.orientation = LinearLayout.HORIZONTAL
                holder.timeCheckBoxLayout.addView(currentRow)
            }
            val checkBox = CheckBox(holder.itemView.context)
            checkBox.text = time
            currentRow?.addView(checkBox)
        }

        // 点击删除按钮时调用删除事件
        holder.buttonDelete.setOnClickListener {
            val deletedMedication = medicationList[position]
            removeMedication(deletedMedication)
        }
    }

    override fun getItemCount(): Int = medicationList.size

    private fun removeMedication(medicationData: MedicationData) {
        medicationList = medicationList.filter { it!= medicationData }.toMutableList()
        notifyDataSetChanged()
        onDeleteClick(medicationList, dateItem, medicationData)
    }

    private fun showDetailsDialog(context: Context, medicationData: MedicationData) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.activity_show_medication_details, null)
        val dialogTextMedicationName = dialogView.findViewById<TextView>(R.id.dialog_textMedicationName)
        val dialogTextPatientName = dialogView.findViewById<TextView>(R.id.dialog_textPatientName)
        val dialogTextDosageInfo = dialogView.findViewById<TextView>(R.id.dialog_textDosageInfo)
        val dialogTextRemainingAmount = dialogView.findViewById<TextView>(R.id.dialog_textRemainingAmount)
        val dialogTextDailyIntakeFrequency = dialogView.findViewById<TextView>(R.id.dialog_textDailyIntakeFrequency)
        val dialogTextExpiryDate = dialogView.findViewById<TextView>(R.id.dialog_textExpiryDate)
        val dialogButtonClose = dialogView.findViewById<ImageButton>(R.id.dialog_buttonClose)

        dialogTextMedicationName.text = medicationData.medicationName
        dialogTextPatientName.text = medicationData.patientName
        dialogTextDosageInfo.text = context.getString(R.string.dosageWithData, medicationData.dosage)
        dialogTextRemainingAmount.text = context.getString(R.string.remainingAmountWithData, medicationData.remainingAmount)
        dialogTextDailyIntakeFrequency.text = context.getString(R.string.dailyIntakeFrequencyWithData, medicationData.dailyIntakeFrequency)
        dialogTextExpiryDate.text = context.getString(R.string.expirationDateWithData, medicationData.expiryDate)

        val dialog = android.app.AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        dialogButtonClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}