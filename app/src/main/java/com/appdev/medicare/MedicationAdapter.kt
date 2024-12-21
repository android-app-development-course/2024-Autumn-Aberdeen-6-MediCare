package com.appdev.medicare

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.appdev.medicare.CalendarAdapter.OnDateSelectedListener
import com.appdev.medicare.model.DateItem
import com.appdev.medicare.model.MedicationData
import com.appdev.medicare.room.AppDatabase
import com.appdev.medicare.room.DatabaseBuilder
import com.appdev.medicare.utils.DatabaseSync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date


class MedicationAdapter(
    private val dateItem: DateItem,
    private val onDeleteClick: (MutableList<MedicationData>, DateItem, MedicationData) -> Unit
) : RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder>() {

    private var medicationList = dateItem.medicationData!!
    private lateinit var context: Context
    private var updateListener: MedicationDataUpdateListener? = null

    inner class MedicationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textMedicationName: TextView = itemView.findViewById(R.id.textMedicationName)
        val textDosageInfo: TextView = itemView.findViewById(R.id.textDosageInfo)
        val buttonDelete: ImageButton = itemView.findViewById(R.id.buttonDelete)
        val timeCheckBoxLayout: LinearLayout = itemView.findViewById(R.id.timeCheckBoxLayout)
    }

    interface MedicationDataUpdateListener {
        fun updateMedicationData(
            medicationId: Int,
            newPatientName: String,
            newDosageInfo: String,
            newRemainingAmount: String,
            newDailyIntakeFrequency: String,
            newExpiryDate: String
        ): Boolean
    }

    fun setUpdateListener(listener: MedicationDataUpdateListener) {
        updateListener = listener
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
//            Log.w("check id", "medicationID: ${medication.medicationID}")
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
        (context as? Activity)?.runOnUiThread {
            Toast.makeText(context, context.getString(R.string.deleteSuccess), Toast.LENGTH_SHORT).show()
        }
        notifyDataSetChanged()
        onDeleteClick(medicationList, dateItem, medicationData)
    }

    private fun showDetailsDialog(context: Context, medicationData: MedicationData) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.activity_show_medication_details, null)
        val dialogTextMedicationName = dialogView.findViewById<TextView>(R.id.dialog_textMedicationName)
        val dialogTextPatientName = dialogView.findViewById<EditText>(R.id.dialog_textPatientName)
        val dialogTextDosageInfo = dialogView.findViewById<EditText>(R.id.dialog_textDosageInfo)
        val dialogTextRemainingAmount = dialogView.findViewById<EditText>(R.id.dialog_textRemainingAmount)
        val dialogTextDailyIntakeFrequency = dialogView.findViewById<EditText>(R.id.dialog_textDailyIntakeFrequency)
        val dialogTextExpiryDate = dialogView.findViewById<EditText>(R.id.dialog_textExpiryDate)
        val dialogButtonClose = dialogView.findViewById<ImageButton>(R.id.dialog_buttonClose)
        val dialogButtonEdit = dialogView.findViewById<Button>(R.id.dialog_buttonEdit)
        val dialogButtonSubmit = dialogView.findViewById<Button>(R.id.dialog_buttonSubmit)

        dialogTextMedicationName.text = medicationData.medicationName
        dialogTextPatientName.setText(medicationData.patientName)
        dialogTextDosageInfo.setText(medicationData.dosage)
        dialogTextRemainingAmount.setText(medicationData.remainingAmount)
        dialogTextDailyIntakeFrequency.setText(medicationData.dailyIntakeFrequency)
        dialogTextExpiryDate.setText(medicationData.expiryDate)

        setEditModeEnabled(dialogView, false)

        val dialog = android.app.AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        dialogButtonClose.setOnClickListener {
            dialog.dismiss()
        }

        dialogButtonEdit.setOnClickListener {
            setEditModeEnabled(dialogView, true)
        }

        dialogButtonSubmit.setOnClickListener {
            val medicationId = medicationData.medicationID
            val newPatientName = dialogTextPatientName.text.toString()
            val newDosageInfo = dialogTextDosageInfo.text.toString()
            val newRemainingAmount = dialogTextRemainingAmount.text.toString()
            val newDailyIntakeFrequency = dialogTextDailyIntakeFrequency.text.toString()
            val newExpiryDate = dialogTextExpiryDate.text.toString()

            medicationData.patientName = newPatientName
            medicationData.dosage = newDosageInfo
            medicationData.remainingAmount = newRemainingAmount
            medicationData.dailyIntakeFrequency = newDailyIntakeFrequency
            medicationData.expiryDate = newExpiryDate

            // 数据库更新
            if (newPatientName.isNotEmpty() && newDosageInfo.isNotEmpty() && newRemainingAmount.isNotEmpty()
                && newDailyIntakeFrequency.isNotEmpty() && newExpiryDate.isNotEmpty()) {

                kotlinx.coroutines.runBlocking{
                    withContext(Dispatchers.IO) {
                        val dataBase = DatabaseBuilder.getInstance(context)
                        val rowAffected = dataBase.medicationDao().updateMedicationRecord(medicationId,
                            newPatientName,
                            newDosageInfo,
                            newRemainingAmount,
                            newDailyIntakeFrequency,
                            newExpiryDate)
//                        Log.w("check rowAffected", "medicationID: $medicationId, rowAffected: $rowAffected")
                        if (rowAffected == 1) {
                            (context as? Activity)?.runOnUiThread {
                                Toast.makeText(context, context.getString(R.string.modifySuccess), Toast.LENGTH_SHORT).show()
                                setEditModeEnabled(dialogView, false)
                                dialog.dismiss()
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(context, context.getString(R.string.inputAllMedicineInfo), Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun setEditModeEnabled(dialogView: View, enabled: Boolean) {
        val dialogTextPatientName = dialogView.findViewById<EditText>(R.id.dialog_textPatientName)
        val dialogTextDosageInfo = dialogView.findViewById<EditText>(R.id.dialog_textDosageInfo)
        val dialogTextRemainingAmount = dialogView.findViewById<EditText>(R.id.dialog_textRemainingAmount)
        val dialogTextDailyIntakeFrequency = dialogView.findViewById<EditText>(R.id.dialog_textDailyIntakeFrequency)
        val dialogTextExpiryDate = dialogView.findViewById<EditText>(R.id.dialog_textExpiryDate)
        val dialogButtonSubmit = dialogView.findViewById<Button>(R.id.dialog_buttonSubmit)

        dialogTextPatientName.isEnabled = enabled
        dialogTextDosageInfo.isEnabled = enabled
        dialogTextRemainingAmount.isEnabled = enabled
        dialogTextDailyIntakeFrequency.isEnabled = enabled
        dialogTextExpiryDate.isEnabled = enabled
        dialogButtonSubmit.isEnabled = enabled
    }
}