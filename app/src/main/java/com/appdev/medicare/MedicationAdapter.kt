package com.appdev.medicare

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.appdev.medicare.model.MedicationData
import java.util.Locale


class MedicationAdapter(
    private var medicationList: MutableList<MedicationData>,
    private val onDeleteClick: (MedicationData) -> Unit
) : RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder>() {

    inner class MedicationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textMedicationName: TextView = itemView.findViewById(R.id.textMedicationName)
        val textDosageInfo: TextView = itemView.findViewById(R.id.textDosageInfo)
        val buttonDelete: ImageButton = itemView.findViewById(R.id.buttonDelete)
        val timeCheckBoxLayout: LinearLayout = itemView.findViewById(R.id.timeCheckBoxLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_medication, parent, false)
        return MedicationViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicationViewHolder, position: Int) {
        val medication = medicationList[position]
        holder.textMedicationName.text = medication.medicationName
        holder.textDosageInfo.text = "剂量: ${medication.dosage}"

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
            checkBox.text = "$time"
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
        onDeleteClick(medicationData)
    }

    private fun showDetailsDialog(context: Context, medicationData: MedicationData) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.activity_show_medication_details, null)
        val dialogTextMedicationName = dialogView.findViewById<TextView>(R.id.dialog_textMedicationName)
        val dialogTextPatientName = dialogView.findViewById<TextView>(R.id.dialog_textPatientName)
        val dialogTextDosageInfo = dialogView.findViewById<TextView>(R.id.dialog_textDosageInfo)
        val dialogTextRemainingAmount = dialogView.findViewById<TextView>(R.id.dialog_textRemainingAmount)
        val dialogTextDailyIntakeFrequency = dialogView.findViewById<TextView>(R.id.dialog_textDailyIntakeFrequency)
        val dialogTextIntakeIntervalDays = dialogView.findViewById<TextView>(R.id.dialog_textIntakeIntervalDays)
        val dialogTextExpiryDate = dialogView.findViewById<TextView>(R.id.dialog_textExpiryDate)
        val dialogButtonClose = dialogView.findViewById<ImageButton>(R.id.dialog_buttonClose)

        dialogTextMedicationName.text = medicationData.medicationName
        dialogTextPatientName.text = medicationData.patientName
        dialogTextDosageInfo.text = "剂量: ${medicationData.dosage}"
        dialogTextRemainingAmount.text = "剩余数量: ${medicationData.remainingAmount}"
        dialogTextDailyIntakeFrequency.text = "每日摄入频率: ${medicationData.dailyIntakeFrequency}"
        dialogTextIntakeIntervalDays.text = "摄入间隔天数: ${medicationData.intakeIntervalDays}"
        dialogTextExpiryDate.text = "过期日期: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(medicationData.expiryDate)}"

        val dialog = android.app.AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        dialogButtonClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
