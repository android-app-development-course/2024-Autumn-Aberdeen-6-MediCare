package com.appdev.medicare

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.appdev.calendarpage.model.DateItem

class CalendarAdapter(private val daysOfMonth: List<Int>, private val deMode : Boolean, private val dateItems: List<DateItem>) :
    RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    private lateinit var medicationAdapter: MedicationAdapter
    private lateinit var recyclerViewMedication: RecyclerView

    private var allDateTextViews: MutableList<TextView> = mutableListOf()
    private var selectedView : View? = null
    private var startSelectView : Pair<Int,View>? = null
    private var endSelectView : Pair<Int,View>? = null
    private var selectedViews : MutableList<View> = mutableListOf()
    private var selectedDateItem : DateItem? = null
    private var selectedDateItems : MutableList<DateItem> = mutableListOf()
    private var onDateSelectedListener: OnDateSelectedListener? = null
    var setMultiSelectMode = deMode

    interface OnDateSelectedListener {
        fun onDateSelected(dateItem: DateItem, flag: Boolean)
    }
    fun setOnDateSelectedListener(listener: OnDateSelectedListener) {
        onDateSelectedListener = listener
    }

    inner class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textDay: TextView = itemView.findViewById(R.id.textDay)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_day, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val day = daysOfMonth[position]

        if (day != 0) {
            // 设置当前day的 唯一标识符 "yyyyMMDD"
            val currentDateItem = dateItems[day-1]
            holder.textDay.tag = currentDateItem.dateIdentifier

            holder.textDay.text = day.toString()
            holder.textDay.setOnClickListener {
                if (setMultiSelectMode) {
                    if (startSelectView == null) {
                        startSelectView = Pair(day, holder.textDay)
                        holder.textDay.setBackgroundResource(R.drawable.range_points)
                    }
                    else if (endSelectView == null && day == startSelectView!!.first) {
                        startSelectView = null
                        holder.textDay.setBackgroundResource(R.drawable.default_shape)
                    }
                    else if (endSelectView == null) {
                        endSelectView = Pair(day, holder.textDay)
                        holder.textDay.setBackgroundResource(R.drawable.range_points)
                        if (endSelectView!!.first > startSelectView!!.first) {
                            for (d in startSelectView!!.first..endSelectView!!.first) {
                                val betweenView = allDateTextViews[d-1]
                                selectedDateItems.add(dateItems[d-1])
                                selectedViews.add(betweenView)
                            }
                        } else {
                            startSelectView = endSelectView.also{ endSelectView = startSelectView}
                            for (d in startSelectView!!.first..endSelectView!!.first) {
                                val betweenView = allDateTextViews[d-1]
                                selectedDateItems.add(dateItems[d-1])
                                selectedViews.add(betweenView)
                            }
                        }
                        selectedViews.forEach{view ->
                            if (view != startSelectView!!.second && view != endSelectView!!.second)
                                view.setBackgroundResource(R.drawable.pressed_shape)
                        }
                    }
                    else if (day == startSelectView!!.first || day == endSelectView!!.first) {
                        selectedViews.forEach{view ->
                            view.setBackgroundResource(R.drawable.default_shape)
                        }
                        startSelectView = null
                        endSelectView = null
                        selectedViews.clear()
                        selectedDateItems.clear()
                    }
                }
                else {
                    if (selectedView == null) {
                        holder.textDay.setBackgroundResource(R.drawable.pressed_shape)
                        selectedView = holder.textDay
                        selectedDateItem = currentDateItem
//                      Toast.makeText(holder.itemView.context, "Selected date: $day", Toast.LENGTH_SHORT).show()

                        // 在Main中处理当天药品信息的展示操作
                        onDateSelectedListener?.onDateSelected(currentDateItem, true)
                    }
                    else if (holder.textDay == selectedView) {
                        // 如果有先前选中的视图，恢复其默认状态
                        selectedDateItem = null
                        selectedView!!.setBackgroundResource(R.drawable.default_shape)
                        selectedView = null
                        onDateSelectedListener?.onDateSelected(currentDateItem, false)
                    }
                    else if (holder.textDay != selectedView) {
                        // 如果有先前选中的视图，恢复其默认状态
                        selectedView!!.setBackgroundResource(R.drawable.default_shape)
                        // 设置当前点击的视图为选中状态
                        holder.textDay.setBackgroundResource(R.drawable.pressed_shape)
                        selectedDateItem = currentDateItem
                        selectedView = holder.textDay
//                        Toast.makeText(holder.itemView.context, "Selected date: $day", Toast.LENGTH_SHORT).show()

                        // 在Main中处理当天药品信息的展示操作
                        onDateSelectedListener?.onDateSelected(currentDateItem, true)
                    }
                }
            }
            allDateTextViews.add(holder.textDay)
        } else {
            holder.itemView.visibility = View.INVISIBLE
        }
    }
    fun clearStates() {
        if (setMultiSelectMode) {
            selectedViews.forEach{view ->
                view.setBackgroundResource(R.drawable.default_shape)
            }
            startSelectView = null
            endSelectView = null
            selectedViews.clear()
            selectedDateItems.clear()
        } else {
            selectedDateItem = null
            selectedView!!.setBackgroundResource(R.drawable.default_shape)
            selectedView = null
        }
    }

    override fun getItemCount(): Int = daysOfMonth.size

    fun getAllTextViews(): MutableList<TextView> {
        return allDateTextViews
    }

    fun getSelectedDateItem(): DateItem? {
        return selectedDateItem
    }

    fun getSelectedDateItems(): MutableList<DateItem> {
        return selectedDateItems
    }
}

