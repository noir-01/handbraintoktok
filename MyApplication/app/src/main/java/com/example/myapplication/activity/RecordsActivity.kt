package com.example.myapplication.activity

import android.widget.Button
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.CustomGraphView
import com.example.myapplication.R

class RecordsActivity : AppCompatActivity() {

    private lateinit var customGraphView: CustomGraphView
    private lateinit var originalDataPoints: List<Pair<String, Int>>  // 전체 데이터를 저장할 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_records)

        customGraphView = findViewById(R.id.customGraphView)

        // 전체 데이터를 originalDataPoints에 저장
        originalDataPoints = customGraphView.dataPoints

        val dailyButton: Button = findViewById(R.id.dailyButton)
        val weeklyButton: Button = findViewById(R.id.weeklyButton)
        val monthlyButton: Button = findViewById(R.id.monthlyButton)


        // 일간 버튼 클릭 이벤트 처리 (최신 10개 데이터)
        dailyButton.setOnClickListener {
            val dailyData = filterDailyData(originalDataPoints)
            customGraphView.updateData(dailyData)
            customGraphView.invalidate()  // 그래프 다시 그리기
        }

        // 주간 버튼 클릭 이벤트 처리 (6~8일 간격 데이터)
        weeklyButton.setOnClickListener {
            val weeklyData = filterWeeklyData(originalDataPoints)
            customGraphView.updateData(weeklyData)
            customGraphView.invalidate()  // 그래프 다시 그리기
        }
        // 월간 버튼 클릭 이벤트 처리
        monthlyButton.setOnClickListener {
            val monthlyData = filterMonthlyData(originalDataPoints)
            customGraphView.updateData(monthlyData)
            customGraphView.invalidate()  // 그래프 다시 그리기
        }

    }

    // 최신 10개 데이터를 필터링하는 함수
    private fun filterDailyData(dataPoints: List<Pair<String, Int>>): List<Pair<String, Int>> {
        return if (dataPoints.size > 10) {
            dataPoints.takeLast(10)  // 가장 최근 10개 데이터를 반환
        } else {
            dataPoints  // 데이터가 10개 이하인 경우 모든 데이터를 반환
        }
    }

    // 6~8일 간격으로 데이터를 필터링하는 함수
    private fun filterWeeklyData(dataPoints: List<Pair<String, Int>>): List<Pair<String, Int>> {
        if (dataPoints.size <= 10) {
            return dataPoints  // 데이터가 10개 이하인 경우 모든 데이터를 반환
        }
        val weeklyData = mutableListOf<Pair<String, Int>>()
        var index = 0
        while (index < dataPoints.size) {
            weeklyData.add(dataPoints[index])
            index += 6 + (0..2).random()  // 6~8일 간격으로 데이터 선택
        }
        return weeklyData
    }

    // 30일 간격으로 데이터를 필터링하는 함수
    private fun filterMonthlyData(dataPoints: List<Pair<String, Int>>): List<Pair<String, Int>> {
        if (dataPoints.size <= 5) {
            return dataPoints  // 데이터가 5개 이하인 경우 모든 데이터를 반환
        }
        val monthlyData = mutableListOf<Pair<String, Int>>()
        var index = 0
        while (index < dataPoints.size) {
            monthlyData.add(dataPoints[index])
            index += 30  // 30일 간격으로 데이터 선택
        }
        return monthlyData
    }
}
