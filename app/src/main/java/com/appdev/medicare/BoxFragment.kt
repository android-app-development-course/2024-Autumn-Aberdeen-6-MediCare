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
import com.appdev.medicare.databinding.FragmentBoxBinding
import com.appdev.medicare.model.MedicationData
import com.appdev.medicare.model.MedicineBox


class BoxFragment : Fragment() {

    private var _binding: FragmentBoxBinding? = null
    private val binding get() = _binding!!

    private lateinit var buttonAddBox: ImageButton
    private lateinit var recyclerViewBox: RecyclerView
    private lateinit var medicineBoxAdapter: MedicineBoxAdapter
    private lateinit var defaultMedicineBox: MedicineBox
    private lateinit var defaultMedicineSet: MutableList<MedicationData>


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBoxBinding.inflate(inflater, container, false)

        val root: View = binding.root

        defaultMedicineSet = mutableListOf()

        defaultMedicineBox = MedicineBox(1,"小孩感冒", "家庭", defaultMedicineSet,"","")

        buttonAddBox = binding.buttonAddBox
        recyclerViewBox = binding.recyclerViewBox
        medicineBoxAdapter = MedicineBoxAdapter(listOf(defaultMedicineBox))
        medicineBoxAdapter.setOnAddButtonClickListener(object :
            MedicineBoxAdapter.OnAddButtonClickListener {
            override fun onAddButtonClick(box: MedicineBox) {
                val boxList = box
                val intent = Intent(requireContext(), AddMedActivity::class.java)
                intent.putExtra("flag", false)
                startActivity(intent)
            }
        })
        recyclerViewBox.setAdapter(medicineBoxAdapter)

        buttonAddBox.setOnClickListener {
            val intent = Intent(requireContext(), AddBoxActivity::class.java)
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
        val recordList = getMedicineBoxFromDB()
        medicineBoxAdapter = MedicineBoxAdapter(recordList)
        medicineBoxAdapter.setOnAddButtonClickListener(object :
            MedicineBoxAdapter.OnAddButtonClickListener {
            override fun onAddButtonClick(box: MedicineBox) {
                val boxList = box
                val intent = Intent(requireContext(), AddMedActivity::class.java)
                intent.putExtra("flag", false)
                startActivity(intent)
            }
        })
        recyclerViewBox.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerViewBox.setAdapter(medicineBoxAdapter)
    }

    private fun getMedicineBoxFromDB(): List<MedicineBox> {
        val record1 = MedicineBox(1,"药箱1", "病号1", defaultMedicineSet,"","")
        val record2 = MedicineBox(2,"药箱2", "病号2", defaultMedicineSet,"","")
        val record3 = MedicineBox(3,"药箱1", "病号1", defaultMedicineSet,"","")
        val record4 = MedicineBox(4,"药箱2", "病号2", defaultMedicineSet,"","")
        return listOf(record1, record2, record3, record4)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}