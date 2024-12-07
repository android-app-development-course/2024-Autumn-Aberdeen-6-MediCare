package com.appdev.medicare

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import java.util.ArrayList

// 在构建好每个Fragment页面后替换这些类即可
class Fragment2 : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_box, container, false)
    }
}

class Fragment3 : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_record, container, false)
    }
}
class Fragment4 : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_me, container, false)
    }
}

class MainActivity : AppCompatActivity() {
    // 使用更符合Kotlin命名习惯的变量名
    private lateinit var viewPager: ViewPager2
    private lateinit var radioGroup: RadioGroup
    private lateinit var radioButton1: RadioButton
    private lateinit var radioButton2: RadioButton
    private lateinit var radioButton3: RadioButton
    private lateinit var radioButton4: RadioButton
    private val fragmentList = ArrayList<Fragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()

        // 为RadioGroup设置选中状态改变的监听器
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_calendar -> viewPager.currentItem = 0
                R.id.rb_box -> viewPager.currentItem = 1
                R.id.rb_record -> viewPager.currentItem = 2
                R.id.rb_me -> viewPager.currentItem = 3
            }
        }

        // 为ViewPager2注册页面变化监听器
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateRadioButtonCheckedState(position)
            }
        })
    }

    private fun initViews() {
        viewPager = findViewById(R.id.viewpager)
        radioGroup = findViewById(R.id.rg_tab)
        radioButton1 = findViewById(R.id.rb_calendar)
        radioButton2 = findViewById(R.id.rb_box)
        radioButton3 = findViewById(R.id.rb_record)
        radioButton4 = findViewById(R.id.rb_me)

        fragmentList.add(CalendarActivity())
        fragmentList.add(Fragment2())
        fragmentList.add(Fragment3())
        fragmentList.add(Fragment4())

        val adapter = ViewPager2Adapter(this, fragmentList)
        viewPager.adapter = adapter
    }

    // 根据ViewPager2的当前页面位置更新RadioButton的选中状态
    private fun updateRadioButtonCheckedState(position: Int) {
        radioButton1.isChecked = position == 0
        radioButton2.isChecked = position == 1
        radioButton3.isChecked = position == 2
        radioButton4.isChecked = position == 3
    }
}


class ViewPager2Adapter(fragmentActivity: FragmentActivity, private val fragmentList: List<Fragment>) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return fragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }
}