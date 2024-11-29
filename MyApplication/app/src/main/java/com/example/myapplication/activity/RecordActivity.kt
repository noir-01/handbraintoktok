package com.example.myapplication

import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.renderer.LineChartRenderer
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter



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
            val today = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            dailyList = (0 until 14).map { daysAgo ->
                RandomGameHistoryDto(
                    startDate = today.minusDays(daysAgo.toLong()).format(formatter),
                    averageReactionTime = (1..2).random() + (0..99).random() / 100f // 1.00 ~ 2.99 사이의 랜덤 값
                )
            }
            withContext(Dispatchers.Main){
                setupChart(chart, dailyList, "Daily Average Reaction Time")
            }
        }
    }

    // backgroundColors 함수
    fun setBackgroundColors(chart: LineChart) {
        chart.setDrawGridBackground(false)
        chart.renderer = object : LineChartRenderer(chart, chart.animator, chart.viewPortHandler) {
            override fun drawExtras(c: Canvas?) {
                super.drawExtras(c)
                if (c != null) {
                    val viewPort = chart.viewPortHandler.contentRect

                    // Define the color regions based on Y values
                    val totalHeight = viewPort.height()
                    val redHeight = totalHeight / 3
                    val yellowHeight = totalHeight / 3 * 2

                    val paint = Paint()

                    // Draw red region
                    paint.color = resources.getColor(R.color.red)
                    c.drawRect(
                        viewPort.left,
                        viewPort.bottom - redHeight,
                        viewPort.right,
                        viewPort.bottom,
                        paint
                    )

                    // Draw yellow region
                    paint.color = resources.getColor(R.color.yellow)
                    c.drawRect(
                        viewPort.left,
                        viewPort.bottom - yellowHeight,
                        viewPort.right,
                        viewPort.bottom - redHeight,
                        paint
                    )

                    // Draw green region
                    paint.color = resources.getColor(R.color.green)
                    c.drawRect(
                        viewPort.left,
                        viewPort.top,
                        viewPort.right,
                        viewPort.bottom - yellowHeight,
                        paint
                    )
                }
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
            lineWidth=4f // 선 두께

            // Customize the circle (data points)
            setDrawCircles(true) // 점 표시
            setCircleColor(ContextCompat.getColor(this@RecordActivity, R.color.black)) // 점 색상
            setCircleHoleColor(ContextCompat.getColor(this@RecordActivity, R.color.black)) // 점 내부 색상
            circleRadius = 5f // 점의 반지름
            setDrawCircleHole(false) // 점 내부에 빈 공간 없음
            valueTextSize = 10f // 텍스트 크기

            // Customize value labels (optional)
            valueTextColor = resources.getColor(R.color.black) // 텍스트 색상
            valueTextSize = 10f // 텍스트 크기
        }





        // Set up the data for the chart
        val lineData = LineData(dataSet)
        chart.data = lineData

        // Customize the chart appearance
        chart.apply {
//            description.isEnabled = false
//            legend.isEnabled = true
            isDragEnabled = false // 드래그 비활성화
            setScaleEnabled(false) // 확대/축소 비활성화
            setPinchZoom(false) // 핀치 줌 비활성화
            isDoubleTapToZoomEnabled = false // 더블 탭 줌 비활성화
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
                axisMaximum = 3f // Max value for the Y-axis (assuming your Y values are in 0-3 range)
            }
            axisRight.isEnabled = false
            // Add background color regions
            setBackgroundColors(chart)
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