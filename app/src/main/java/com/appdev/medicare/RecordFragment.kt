package com.appdev.medicare


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appdev.medicare.databinding.FragmentRecordBinding
import com.appdev.medicare.model.MedicineRecord


class RecordFragment : Fragment() {

    private var _binding: FragmentRecordBinding? = null
    private val binding get() = _binding!!

    private lateinit var buttonAddRecord: ImageButton
    private lateinit var recyclerViewRecord: RecyclerView
    private lateinit var medicineRecordAdapter: MedicineRecordAdapter
    private lateinit var defaultMedicineRecord: MedicineRecord


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordBinding.inflate(inflater, container, false)

        val root: View = binding.root
        defaultMedicineRecord = MedicineRecord(5,"展示", "病号1", "2024-10-16")

        buttonAddRecord = binding.buttonAddRecord
        recyclerViewRecord = binding.recyclerViewRecord
        medicineRecordAdapter = MedicineRecordAdapter(listOf(defaultMedicineRecord))
        recyclerViewRecord.setAdapter(medicineRecordAdapter)


        buttonAddRecord.setOnClickListener {
            val intent = Intent(requireContext(), AddMedActivity::class.java)
            startActivity(intent)
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadDataAndUpdateUI()
    }

    // 从数据库获取数据，用于加载已有数据
    private fun loadDataAndUpdateUI() {
        val recordList = getMedicineRecordsFromDB()
        medicineRecordAdapter = MedicineRecordAdapter(recordList)
        recyclerViewRecord.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerViewRecord.setAdapter(medicineRecordAdapter)
    }

    private fun getMedicineRecordsFromDB(): List<MedicineRecord> {
        val record1 = MedicineRecord(1,"病历1", "病号1", "2024-12-01")
        val record2 = MedicineRecord(2,"病历2", "病号2", "2024-12-05")
        val record3 = MedicineRecord(3,"病历1", "病号1", "2024-12-01")
        val record4 = MedicineRecord(4,"病历2", "病号2", "2024-12-05")
        return listOf(record1, record2, record3, record4)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}