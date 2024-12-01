package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class CustomGraphView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 선을 그릴 때 사용할 Paint 객체
    private val paintLine = Paint().apply {
        color = Color.BLACK
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    // 점을 그릴 때 사용할 Paint 객체
    private val paintPoint = Paint().apply {
        color = Color.RED
        strokeWidth = 10f
        style = Paint.Style.FILL
    }

    // 배경을 그릴 Paint 객체
    private val paintBackgroundRed = Paint().apply { color = Color.rgb(255, 179, 186) }   // 파스텔 빨간색
    private val paintBackgroundYellow = Paint().apply { color = Color.rgb(255, 253, 178) } // 파스텔 노란색
    private val paintBackgroundGreen = Paint().apply { color = Color.rgb(186, 255, 201) }   // 파스텔 초록색


    // 샘플 데이터 (날짜와 값 쌍의 리스트)
    var dataPoints: List<Pair<String, Int>> = listOf(
        Pair("2024/08/01", 33),
        Pair("2024/08/03", 31),
        Pair("2024/08/05", 31),
        Pair("2024/08/06", 30),
        Pair("2024/08/07", 30),
        Pair("2024/08/10", 31),
        Pair("2024/08/11", 30),
        Pair("2024/08/12", 29),
        Pair("2024/08/13", 28),
        Pair("2024/08/15", 28),
        Pair("2024/08/16", 27),
        Pair("2024/08/18", 25),
        Pair("2024/08/20", 25),
        Pair("2024/08/22", 23),
        Pair("2024/08/23", 23),
        Pair("2024/08/25", 22),
        Pair("2024/08/26", 22),
        Pair("2024/08/27", 21),
        Pair("2024/08/28", 20),
        Pair("2024/09/01", 23),
        Pair("2024/09/03", 21),
        Pair("2024/09/05", 21),
        Pair("2024/09/06", 20),
        Pair("2024/09/07", 20),
        Pair("2024/09/10", 21),
        Pair("2024/09/11", 20),
        Pair("2024/09/12", 19),
        Pair("2024/09/13", 18),
        Pair("2024/09/15", 18),
        Pair("2024/09/16", 17),
        Pair("2024/09/18", 15),
        Pair("2024/09/20", 15),
        Pair("2024/09/22", 13),
        Pair("2024/09/23", 13),
        Pair("2024/09/25", 12),
        Pair("2024/09/26", 12),
        Pair("2024/09/27", 11),
        Pair("2024/09/28", 10)

    )
    fun updateData(newDataPoints: List<Pair<String, Int>>) {
        dataPoints = newDataPoints
        invalidate() // View를 다시 그리기
    }



    override fun onDraw(canvas: Canvas) {

        // 캔버스 크기 가져오기
        val width = width
        val height = height

        // 패딩 설정
        val leftPadding = 100
        val rightPadding = 100
        val bottomPadding = 100
        val topPadding = 100

        // 배경 색상 영역 비율 계산
        val thirdHeight = height / 3


        // 붉은색 배경 (상단 1/3)
        canvas.drawRect(0f, 0f, width.toFloat(), thirdHeight.toFloat(), paintBackgroundRed)

        // 노란색 배경 (중간 1/3)
        canvas.drawRect(0f, thirdHeight.toFloat(), width.toFloat(), (2 * thirdHeight).toFloat(), paintBackgroundYellow)

        // 초록색 배경 (하단 1/3)
        canvas.drawRect(0f, (2 * thirdHeight).toFloat(), width.toFloat(), height.toFloat(), paintBackgroundGreen)


        // 그래프 그리기
        val xInterval = (width - leftPadding - rightPadding) / (dataPoints.size - 1)
        val maxY = dataPoints.maxOf { it.second }  // y축의 최대값
        val minY = dataPoints.minOf { it.second }  // y축의 최소값
        val yRange = maxY - minY  // y축의 값 범위

        // 각 데이터 포인트 사이에 선을 그리기
        for (i in 0 until dataPoints.size - 1) {
            val startX = leftPadding + i * xInterval
            val startY = height - bottomPadding - ((dataPoints[i].second - minY) * (height - topPadding - bottomPadding) / yRange)
            val endX = leftPadding + (i + 1) * xInterval
            val endY = height - bottomPadding - ((dataPoints[i + 1].second - minY) * (height - topPadding - bottomPadding) / yRange)

            // 선 그리기
            canvas.drawLine(startX.toFloat(), startY.toFloat(), endX.toFloat(), endY.toFloat(), paintLine)

            // 시작점에 점 그리기
            canvas.drawCircle(startX.toFloat(), startY.toFloat(), 10f, paintPoint)
        }

        // 마지막 점 그리기
        val lastX = leftPadding + (dataPoints.size - 1) * xInterval
        val lastY = height - bottomPadding - ((dataPoints.last().second - minY) * (height - topPadding - bottomPadding) / yRange)
        canvas.drawCircle(lastX.toFloat(), lastY.toFloat(), 10f, paintPoint)
    }

}
