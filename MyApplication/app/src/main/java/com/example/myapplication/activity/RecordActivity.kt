package com.example.myapplication

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapplication.util.dataClass.DummyData
import android.util.Log
import androidx.core.content.res.ResourcesCompat


import com.example.myapplication.util.SettingUtil
import com.example.myapplication.util.dataClass.RandomGameHistoryDto
import com.example.myapplication.util.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.renderer.LineChartRenderer
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale


class RecordActivity : AppCompatActivity(){
    private lateinit var dailyList: List<RandomGameHistoryDto>
    private lateinit var weeklyList: List<RandomGameHistoryDto>
    private lateinit var monthlyList: List<RandomGameHistoryDto>
    private lateinit var chart: LineChart
    val apiService = RetrofitClient.apiService
    val averageEntries = mutableListOf<Entry>()
    var userDecadeAverage = 0f

    private fun generateWeeklyData(dailyList: List<RandomGameHistoryDto>): List<RandomGameHistoryDto> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val today = LocalDate.now()

        return (0 until 4).map { weekOffset ->
            val weekStart = today.minusWeeks(weekOffset.toLong()).with(java.time.DayOfWeek.MONDAY)
            val weekEnd = weekStart.plusDays(6)

            val records = dailyList.filter {
                val date = LocalDate.parse(it.startDate, formatter)
                date in weekStart..weekEnd
            }

            RandomGameHistoryDto(
                startDate = weekStart.format(formatter),
                averageReactionTime = if (records.isNotEmpty()) {
                    records.map { it.averageReactionTime }.average().toFloat()
                } else {
                    0f
                }
            )
        }
    }

    private fun generateMonthlyData(dailyList: List<RandomGameHistoryDto>): List<RandomGameHistoryDto> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val today = LocalDate.now()

        return (0 until 3).map { monthOffset ->
            val monthStart = today.minusMonths(monthOffset.toLong()).withDayOfMonth(1)
            val monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth())

            val records = dailyList.filter {
                val date = LocalDate.parse(it.startDate, formatter)
                date in monthStart..monthEnd
            }

            RandomGameHistoryDto(
                startDate = monthStart.format(formatter),
                averageReactionTime = if (records.isNotEmpty()) {
                    records.map { it.averageReactionTime }.average().toFloat()
                } else {
                    0f
                }
            )
        }
    }
    private fun generateAverageReactionByDecade(gameType: String): Map<String, Float> {
        val groupedByDecade = DummyData.dummyGameRecords.mapValues { (_, games) ->
            games[gameType] ?: emptyList()
        }
        Log.d("RecordActivity", "Grouped By Decade for $gameType: $groupedByDecade")

        return groupedByDecade.mapValues { (decade, records) ->
            if (records.isNotEmpty()) {
                val average = records.map { it.averageReactionTime }.average().toFloat()
                Log.d("RecordActivity", "Decade: $decade, Average Reaction Time: $average")
                average
            } else {
                0f
            }
        }
    }








    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.record)

        chart = findViewById(R.id.chart)


        // 버튼 초기화
        val backButton: ImageButton = findViewById(R.id.backButton)
        val dailyButton: ImageButton = findViewById(R.id.dailyButton)
        val weeklyButton: ImageButton = findViewById(R.id.weeklyButton)
        val monthlyButton: ImageButton = findViewById(R.id.monthlyButton)
        val recordRandomButton: ImageButton = findViewById(R.id.recordRandom)
        val recordMimicButton: ImageButton = findViewById(R.id.recordMimic)
        val recordRspButton: ImageButton = findViewById(R.id.recordRsp)
        val recordCalculatorButton: ImageButton = findViewById(R.id.recordCalculator)

        recordRandomButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {

                val gameType="RANDOM"
                Log.d("RecordActivity", "Game Type: $gameType")
                Log.d("RecordActivity", "Dummy Game Records: ${DummyData.dummyGameRecords}")

                //내 연령대 평균 가져오기
                val response = apiService.getRandomHistoryAverageByGameType("RANDOM","me")
                if(response.isSuccessful){
                    userDecadeAverage = response.body()?.get("result").toString().toFloat()/1000f
                }


                Log.d("DummyData", "Dummy Game Records: ${DummyData.dummyGameRecords}")


                dailyList = apiService.getRandomHistory(gameType,"DAILY")
                weeklyList = apiService.getRandomHistory(gameType,"WEEKLY")
                monthlyList = apiService.getRandomHistory(gameType,"MONTHLY")
                //내 평균

                val today = LocalDate.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
//                dailyList = (0 until 14).map { daysAgo ->
//                    RandomGameHistoryDto(
//                        startDate = today.minusDays(daysAgo.toLong()).format(formatter),
//                        averageReactionTime = (1..2).random() + (0..99).random() / 100f // 1.00 ~ 2.99 사이의 랜덤 값
//                    )
//                }
//                //Weekly List 생성
//                weeklyList = generateWeeklyData(dailyList)
//
//                // Monthly List 생성
//                monthlyList = generateMonthlyData(dailyList)

                withContext(Dispatchers.Main) {
                    Log.d("RecordActivity", "Setup chart completed with userDecadeAverage")
                    setupChart(chart, dailyList, "내 반응속도", userDecadeAverage)
                    Log.d("RecordActivity", "Chart setup completed")
                }

            }
        }

        recordRspButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val gameType="RSP"
                //내 연령대 평균 가져오기
                val response = apiService.getRandomHistoryAverageByGameType("RSP","me")
                if(response.isSuccessful){
                    userDecadeAverage = response.body()?.get("result").toString().toFloat()/1000f
                    Log.d("recordActivity","$userDecadeAverage")
                }
//                val userDecadeAverage = generateAverageReactionByDecade(gameType)["1970~1979년생"] ?: 0f
                dailyList = apiService.getRandomHistory(gameType,"DAILY")
                weeklyList = apiService.getRandomHistory(gameType,"WEEKLY")
                monthlyList = apiService.getRandomHistory(gameType,"MONTHLY")
                val today = LocalDate.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
//                dailyList = (0 until 14).map { daysAgo ->
//                    RandomGameHistoryDto(
//                        startDate = today.minusDays(daysAgo.toLong()).format(formatter),
//                        averageReactionTime = (1..2).random() + (0..99).random() / 100f // 1.00 ~ 2.99 사이의 랜덤 값
//                    )
//                }
//                //Weekly List 생성
//                weeklyList = generateWeeklyData(dailyList)
//
//                // Monthly List 생성
//                monthlyList = generateMonthlyData(dailyList)

                withContext(Dispatchers.Main) {
                    setupChart(chart, dailyList, "내 반응속도", userDecadeAverage)
                }
            }
        }


        recordCalculatorButton.setOnClickListener{
            CoroutineScope(Dispatchers.IO).launch {
                val gameType="CALC"
//                val userDecadeAverage = generateAverageReactionByDecade(gameType)["1970~1979년생"] ?: 0f
                //내 연령대 평균 가져오기
                val response = apiService.getRandomHistoryAverageByGameType("CALC","me")
                if(response.isSuccessful){
                    userDecadeAverage = response.body()?.get("result").toString().toFloat()/1000f
                    Log.d("recordActivity","$userDecadeAverage")
                }

                dailyList = apiService.getRandomHistory(gameType,"DAILY")
                weeklyList = apiService.getRandomHistory(gameType,"WEEKLY")
                monthlyList = apiService.getRandomHistory(gameType,"MONTHLY")
                val today = LocalDate.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
//                dailyList = (0 until 14).map { daysAgo ->
//                    RandomGameHistoryDto(
//                        startDate = today.minusDays(daysAgo.toLong()).format(formatter),
//                        averageReactionTime = (1..2).random() + (0..99).random() / 100f // 1.00 ~ 2.99 사이의 랜덤 값
//                    )
//                }
//                //Weekly List 생성
//                weeklyList = generateWeeklyData(dailyList)
//
//                // Monthly List 생성
//                monthlyList = generateMonthlyData(dailyList)

                withContext(Dispatchers.Main) {

                    setupChart(chart, dailyList, "내 반응속도", userDecadeAverage)
                }
            }
        }

        recordMimicButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val gameType="COPY"
//                val userDecadeAverage = generateAverageReactionByDecade(gameType)["1970~1979년생"] ?: 0f
                //내 연령대 평균 가져오기
                val response = apiService.getRandomHistoryAverageByGameType("COPY","me")
                if(response.isSuccessful){
                    userDecadeAverage = response.body()?.get("result").toString().toFloat()/1000f
                    Log.d("recordActivity","$userDecadeAverage")
                }
                dailyList = apiService.getRandomHistory(gameType,"DAILY")
                weeklyList = apiService.getRandomHistory(gameType,"WEEKLY")
                monthlyList = apiService.getRandomHistory(gameType,"MONTHLY")
                val today = LocalDate.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
//                dailyList = (0 until 14).map { daysAgo ->
//                    RandomGameHistoryDto(
//                        startDate = today.minusDays(daysAgo.toLong()).format(formatter),
//                        averageReactionTime = (1..2).random() + (0..99).random() / 100f // 1.00 ~ 2.99 사이의 랜덤 값
//                    )
//                }
//                //Weekly List 생성
//                weeklyList = generateWeeklyData(dailyList)
//
//                // Monthly List 생성
//                monthlyList = generateMonthlyData(dailyList)

                withContext(Dispatchers.Main) {

                    setupChart(chart, dailyList, "내 반응속도", userDecadeAverage)
                }
            }
        }

        // 뒤로가기 버튼 클릭 이벤트
        backButton.setOnClickListener {
            finish() // 현재 액티비티 종료
        }
        // 버튼 클릭 이벤트 설정
        dailyButton.setOnClickListener {
            showDailyData()
        }

        weeklyButton.setOnClickListener {
            showWeeklyData()
        }

        monthlyButton.setOnClickListener {
            showMonthlyData()
        }
        // RPS,CALC,RANDOM
        CoroutineScope(Dispatchers.IO).launch{
            dailyList = apiService.getRandomHistory("RANDOM","DAILY")
            weeklyList = apiService.getRandomHistory("RANDOM","WEEKLY")
            monthlyList = apiService.getRandomHistory("RANDOM","MONTHLY")
            val today = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            dailyList = (0 until 14).map { daysAgo ->
                RandomGameHistoryDto(
                    startDate = today.minusDays(daysAgo.toLong()).format(formatter),
                    averageReactionTime = (1..2).random() + (0..99).random() / 100f // 1.00 ~ 2.99 사이의 랜덤 값
                )
            }

            weeklyList = generateWeeklyData(dailyList)
            monthlyList = generateMonthlyData(dailyList)

            withContext(Dispatchers.Main){
                setupChart(chart, dailyList, "Daily Average Reaction Time")
            }
        }
        //첫 진입 시 랜덤게임 기록 보여주기
        recordRandomButton.post {
            recordRandomButton.performClick()
        }
        chart.notifyDataSetChanged()
        chart.invalidate()
    }

    fun setBackgroundColors(chart: LineChart,threshold: Float) {
        chart.setDrawGridBackground(false)
        chart.renderer = object : LineChartRenderer(chart, chart.animator, chart.viewPortHandler) {
            override fun drawData(c: Canvas?) {
                drawBackgroundColors(c,threshold)
                super.drawData(c)
            }

//            private fun drawBackgroundColors(c: Canvas?) {
//                if (c != null) {
//                    val viewPort = chart.viewPortHandler.contentRect
//
//                    // Define the color regions based on Y values
//                    val totalHeight = viewPort.height()
//                    val redHeight = totalHeight / 3
//                    val yellowHeight = totalHeight / 3 * 2
//
//                    val paint = Paint()
//
//                    // Draw red region
//                    paint.color = resources.getColor(R.color.red)
//                    c.drawRect(
//                        viewPort.left,
//                        viewPort.bottom - redHeight,
//                        viewPort.right,
//                        viewPort.bottom,
//                        paint
//                    )
//
//                    // Draw yellow region
//                    paint.color = resources.getColor(R.color.yellow)
//                    c.drawRect(
//                        viewPort.left,
//                        viewPort.bottom - yellowHeight,
//                        viewPort.right,
//                        viewPort.bottom - redHeight,
//                        paint
//                    )
//
//                    // Draw green region
//                    paint.color = resources.getColor(R.color.green)
//                    c.drawRect(
//                        viewPort.left,
//                        viewPort.top,
//                        viewPort.right,
//                        viewPort.bottom - yellowHeight,
//                        paint
//                    )
//                }
//            }
fun drawBackgroundColors(c: Canvas?, threshold: Float) {
    if (c != null) {
        val viewPort = chart.viewPortHandler.contentRect
        val yAxis = chart.axisLeft

        val paint = Paint()
        val transformer = chart.getTransformer(YAxis.AxisDependency.LEFT)
        // Y축 값을 화면의 픽셀 좌표로 변환
        val thresholdY = transformer.getPixelForValues(0f, threshold).y.toFloat()

        // Draw red region (above threshold)
        paint.color = resources.getColor(R.color.yellow)
        c.drawRect(
            viewPort.left,
            viewPort.top, // 차트 위쪽
            viewPort.right,
            thresholdY, // threshold 값 위치
            paint
        )

        // Draw green region (below threshold)
        paint.color = resources.getColor(R.color.green)
        c.drawRect(
            viewPort.left,
            thresholdY, // threshold 값 위치
            viewPort.right,
            viewPort.bottom, // 차트 아래쪽
            paint
        )
    }
}
        }
    }


    fun setupChart(chart: LineChart,
                   dataList: List<RandomGameHistoryDto>,
                   label: String,
                   userDecadeAverage: Float? = null
    ) {
        if (dataList.isEmpty()) {
            chart.clear()
            chart.data = null
            chart.invalidate()
            return
        }
        val customTypeface = ResourcesCompat.getFont(this, R.font.sb_aggro)
        // 게임기록 데이터셋
        val entries = mutableListOf<Entry>()
        // Prepare entries for the chart (x-axis: day/week/month, y-axis: averageReactionTime)
        dataList.forEachIndexed { index, history ->
            entries.add(Entry(index.toFloat(), history.averageReactionTime/1000))
        }

        // Create a dataset
        val dataSet = LineDataSet(entries, label).apply {
            valueTextSize = 12f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return String.format("%.2f", value) // 소수점 둘째 자리까지 포맷
                }
            }
            valueTypeface=customTypeface
            color = ContextCompat.getColor(this@RecordActivity, R.color.origin_red)
            valueTextColor = resources.getColor(R.color.black) // Customize the text color
            lineWidth = 4f // 선 두께
            // Customize the circle (data points)
            setDrawCircles(true) // 점 표시
            setCircleColor(ContextCompat.getColor(this@RecordActivity, R.color.black)) // 점 색상
            setCircleHoleColor(
                ContextCompat.getColor(
                    this@RecordActivity,
                    R.color.black
                )
            ) // 점 내부 색상
            circleRadius = 5f // 점의 반지름
            setDrawCircleHole(false) // 점 내부에 빈 공간 없음

            // Customize value labels (optional)
            valueTextColor = resources.getColor(R.color.black) // 텍스트 색상
            valueTextSize = 15f // 텍스트 크기
        }


        val averageEntries = mutableListOf<Entry>()
        if (userDecadeAverage != null) {
            dataList.forEachIndexed { index, _ ->
                averageEntries.add(Entry(index.toFloat(), userDecadeAverage))
            }
        }
        Log.d("RecordActivity", "Average Entries: $averageEntries")

        // 연령대 평균 데이터셋 생성
        val averageDataSet = LineDataSet(averageEntries, "내 연령대 평균 반응속도").apply {
            color = ContextCompat.getColor(this@RecordActivity, R.color.blue) // 선 색상을 파란색으로 변경
            lineWidth = 3f // 선 두께 조정
            setDrawCircles(true) // 데이터 포인트를 표시
            setCircleColor(ContextCompat.getColor(this@RecordActivity, R.color.blue)) // 점 색상 파란색
            setDrawCircleHole(false) // 원 내부를 채움
            valueTextSize = 0f // 데이터 값(초) 표시하지 않음
        }


        // Set up the data for the chart
        val lineData = LineData(dataSet,averageDataSet)
        chart.data = lineData
        chart.legend.apply {
            textSize = 20f // 범례 텍스트 크기 변경
            typeface = customTypeface
            textColor = ContextCompat.getColor(this@RecordActivity, R.color.black) // 텍스트 색상 (옵션)
        }
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
                textSize=13f
                typeface = customTypeface
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        return if (index in dataList.indices){
                            dataList[index].getStartDateAsLocalDate().format(DateTimeFormatter.ofPattern("MM-dd"))
                        }
                        else{
                            " " // 유효하지 않을 경우 빈 문자열 반환
                        }
                    }
                }
            }
            axisLeft.apply {
                textSize=13f
                typeface = customTypeface
                axisMinimum = 0f
                axisMaximum = 5f // Max value for the Y-axis (assuming your Y values are in 0-3 range)
            }
            axisRight.isEnabled = false
            chart.invalidate()
            // Add background color regions
            setBackgroundColors(chart,userDecadeAverage?:0f)
            invalidate() // Refresh the chart
        }
    }
    //
    // Use this function for daily, weekly, and monthly buttons
    fun showDailyData() {
        setupChart(chart, dailyList, "내 반응속도",userDecadeAverage)
    }

    fun showWeeklyData() {
        setupChart(chart, weeklyList, "내 반응속도",userDecadeAverage)
    }

    fun showMonthlyData() {
        setupChart(chart, monthlyList, "내 반응속도",userDecadeAverage)
    }
}
