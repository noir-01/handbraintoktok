package com.example.myapplication

import android.widget.Button
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class RecordActivity : AppCompatActivity() {

    private lateinit var customGraphView: CustomGraphView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_records)

        customGraphView = findViewById(R.id.customGraphView)

        val dailyButton: Button = findViewById(R.id.dailyButton)
        val weeklyButton: Button = findViewById(R.id.weeklyButton)
        val monthlyButton: Button = findViewById(R.id.monthlyButton)

        // 일간 버튼 클릭 이벤트 처리 (전체 데이터를 표시)
        dailyButton.setOnClickListener {
            customGraphView.updateData(customGraphView.dataPoints)  // 모든 데이터 표시
            customGraphView.invalidate()  // 그래프 다시 그리기
        }

        // 주간 버튼 클릭 이벤트 처리
        weeklyButton.setOnClickListener {
            val weeklyData = filterWeeklyData(customGraphView.dataPoints)
            customGraphView.updateData(weeklyData)
            customGraphView.invalidate()  // 그래프 다시 그리기
        }

        // 월간 버튼 클릭 이벤트 처리
        monthlyButton.setOnClickListener {
            val monthlyData = filterMonthlyData(customGraphView.dataPoints)
            customGraphView.updateData(monthlyData)
            customGraphView.invalidate()  // 그래프 다시 그리기
        }
    }

    // 6~8일 간격으로 데이터를 필터링하는 함수
    private fun filterWeeklyData(dataPoints: List<Pair<String, Int>>): List<Pair<String, Int>> {
        if (dataPoints.size <= 8) {
            return dataPoints  // 데이터가 8개 이하인 경우 모든 데이터를 반환
        }
        return dataPoints.filterIndexed { index, _ -> index % 6 == 0 || index % 8 == 0 }
    }

    // 한 달 간격으로 데이터를 필터링하는 함수
    private fun filterMonthlyData(dataPoints: List<Pair<String, Int>>): List<Pair<String, Int>> {
        if (dataPoints.size <= 30) {
            return dataPoints  // 데이터가 30개 이하인 경우 모든 데이터를 반환
        }
        return dataPoints.filterIndexed { index, _ -> index % 30 == 0 }
    }
}

