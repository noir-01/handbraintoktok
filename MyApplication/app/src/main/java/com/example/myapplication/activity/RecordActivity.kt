package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.util.dataClass.RandomGameHistoryDto
import com.example.myapplication.util.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.withContext


class RecordActivity : AppCompatActivity(){
    private lateinit var dailyList: List<RandomGameHistoryDto>
    private lateinit var weeklyList: List<RandomGameHistoryDto>
    private lateinit var monthlyList: List<RandomGameHistoryDto>
    private lateinit var chart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.record)

        val apiService = RetrofitClient.apiService
        chart = findViewById(R.id.chart)
        CoroutineScope(Dispatchers.IO).launch{
            dailyList = apiService.getRandomHistory("COPY","DAILY")
            weeklyList = apiService.getRandomHistory("COPY","WEEKLY")
            monthlyList = apiService.getRandomHistory("COPY","MONTHLY")
            withContext(Dispatchers.Main){
                setupChart(chart, dailyList, "Daily Average Reaction Time")
            }
        }
    }
    fun setupChart(chart: LineChart, dataList: List<RandomGameHistoryDto>, label: String) {
        val entries = mutableListOf<Entry>()

        // Prepare entries for the chart (x-axis: day/week/month, y-axis: averageReactionTime)
        dataList.forEachIndexed { index, history ->
            entries.add(Entry(index.toFloat(), history.averageReactionTime))
        }

        // Create a dataset
        val dataSet = LineDataSet(entries, label).apply {
            color = resources.getColor(R.color.red) // You can customize the line color
            valueTextColor = resources.getColor(R.color.black) // Customize the text color
        }

        // Set up the data for the chart
        val lineData = LineData(dataSet)
        chart.data = lineData

        // Customize the chart appearance
        chart.apply {
            description.isEnabled = false
            legend.isEnabled = true
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return when (dataList.size) {
                            in 1..30 -> "Day ${dataList[value.toInt()].getStartDateAsLocalDate()}"
                            in 1..12 -> "Week ${dataList[value.toInt()].getStartDateAsLocalDate()}"
                            else -> "Month ${dataList[value.toInt()].getStartDateAsLocalDate()}"
                        }
                    }
                }
            }
            axisLeft.apply {
                axisMinimum = 0f
            }
            axisRight.isEnabled = false
            invalidate() // Refresh the chart
        }
    }

    // Use this function for daily, weekly, and monthly buttons
    fun showDailyData() {
        setupChart(chart, dailyList, "Daily Average Reaction Time")
    }

    fun showWeeklyData() {
        setupChart(chart, weeklyList, "Weekly Average Reaction Time")
    }

    fun showMonthlyData() {
        setupChart(chart, monthlyList, "Monthly Average Reaction Time")
    }

}