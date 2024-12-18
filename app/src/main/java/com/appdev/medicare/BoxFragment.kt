package com.appdev.medicare


import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.Data
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appdev.medicare.databinding.FragmentBoxBinding
import com.appdev.medicare.model.MedicationData
import com.appdev.medicare.model.BoxData
import com.appdev.medicare.room.AppDatabase
import com.appdev.medicare.room.DatabaseBuilder
import com.appdev.medicare.room.dao.MedicineBoxDao
import com.appdev.medicare.room.entity.MedicineBox


class BoxFragment : Fragment() {

    private var _binding: FragmentBoxBinding? = null
    private val binding get() = _binding!!

    private lateinit var buttonAddBox: ImageButton
    private lateinit var recyclerViewBox: RecyclerView
    private lateinit var medicineBoxAdapter: MedicineBoxAdapter
    private lateinit var defaultBoxData: BoxData
    private lateinit var defaultMedicineSet: MutableList<MedicationData>

    private lateinit var addBoxActivityLauncher: ActivityResultLauncher<Intent>
    private lateinit var dataBase: AppDatabase

    private var cachedBoxList = mutableMapOf<String, BoxData>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBoxBinding.inflate(inflater, container, false)

        val root: View = binding.root
        dataBase = DatabaseBuilder.getInstance(requireContext())

        defaultMedicineSet = mutableListOf()

        defaultBoxData = BoxData(
            1,
            this.getString(R.string.childCold),
            this.getString(R.string.family),
            "",
            defaultMedicineSet,
            "",
            ""
        )

        buttonAddBox = binding.buttonAddBox
        recyclerViewBox = binding.recyclerViewBox
        medicineBoxAdapter = MedicineBoxAdapter(listOf(defaultBoxData))
        medicineBoxAdapter.setOnAddButtonClickListener(object :
            MedicineBoxAdapter.OnAddButtonClickListener {
            override fun onAddButtonClick(box: BoxData) {
                val boxList = box
                val intent = Intent(requireContext(), AddMedActivity::class.java)
                intent.putExtra("flag", false)
                startActivity(intent)
            }
        })
        recyclerViewBox.setAdapter(medicineBoxAdapter)

        addBoxActivityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val boxData = result.data?.getParcelableExtra<BoxData>("box_data")
                if (boxData != null) {
                    cachedBoxList[boxData.name] = boxData
                }
            }
        }
        buttonAddBox.setOnClickListener {
            val intent = Intent(requireContext(), AddBoxActivity::class.java)
            addBoxActivityLauncher.launch(intent)
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadDataAndUpdateUI()
    }

    // 从数据库获取数据，用于加载已有数据
    private fun loadDataAndUpdateUI() {

//        dataBase.medicineBoxDao().getAll()

        val recordList = getMedicineBoxFromDB()
        medicineBoxAdapter = MedicineBoxAdapter(recordList)
        medicineBoxAdapter.setOnAddButtonClickListener(object :
            MedicineBoxAdapter.OnAddButtonClickListener {
            override fun onAddButtonClick(box: BoxData) {
                val boxList = box
                val intent = Intent(requireContext(), AddMedActivity::class.java)
                intent.putExtra("flag", false)
                startActivity(intent)
            }
        })
        recyclerViewBox.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerViewBox.setAdapter(medicineBoxAdapter)
    }

    private fun getMedicineBoxFromDB(): List<BoxData> {
        val record1 = BoxData(
            1,
            this.getString(R.string.medicineBoxIndex, 1),
            this.getString(R.string.patientIndex, 1),
            "",
            defaultMedicineSet,
            "",
            "")
        val record2 = BoxData(
            2,
            this.getString(R.string.medicineBoxIndex, 2),
            this.getString(R.string.patientIndex, 2),
            "",
            defaultMedicineSet,
            "",
            "")
        val record3 = BoxData(
            3,
            this.getString(R.string.medicineBoxIndex, 3),
            this.getString(R.string.patientIndex, 3),
            "",
            defaultMedicineSet,
            "",
            "")
        val record4 = BoxData(
            4,
            this.getString(R.string.medicineBoxIndex, 4),
            this.getString(R.string.patientIndex, 4),
            "",
            defaultMedicineSet,
            "",
            "")
        return listOf(record1, record2, record3, record4)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}