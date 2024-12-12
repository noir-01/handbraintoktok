package com.example.myapplication.util.mediapipe

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import smile.classification.KNN
import smile.classification.RandomForest
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

object GestureRecogObj {

    private lateinit var appContext: Context
    private var numNeighbors: Int = 3

    private lateinit var trainingData: Array<DoubleArray>
    private lateinit var labels: IntArray
    private var knnModel: KNN<DoubleArray>? = null

    // Context와 numNeighbors 설정 메서드
    fun initialize(context: Context, numNeighbors: Int = 3) {
        this.numNeighbors = numNeighbors
        this.appContext = context.applicationContext
        loadTrainingData()
        val start = System.currentTimeMillis()
        knnModel = initializeKNNModel()
        Log.d("grs time", "model init time: ${"%.2f".format((System.currentTimeMillis() - start) / 1000.0)}s")
    }

    // Load training data from the CSV file
    private fun loadTrainingData() {
        val features = ArrayList<DoubleArray>()
        val labelsList = ArrayList<Int>()
        runBlocking {
            withContext(Dispatchers.Default) {
                // Read the CSV file from assets
                appContext.assets.open("gesture_25.csv").use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { br ->
                        val header = br.readLine() // Read header if present
                        var line: String?

                        while (br.readLine().also { line = it } != null) {
                            val values = line!!.split(",").map { it.trim() }
                            val feature = DoubleArray(values.size - 1) // Last column is the label

                            for (i in 0 until values.size - 1) {
                                feature[i] =
                                    values[i].toDouble() // Convert feature strings to double
                            }

                            // Parse the label safely as a double and then convert to int
                            val label = values.last().toDouble().toInt()
                            features.add(feature)
                            labelsList.add(label)
                        }
                    }
                }

                trainingData = features.toTypedArray()
                labels = labelsList.toIntArray()
            }
        }
    }

    // Initialize the KNN model
    private fun initializeKNNModel(): KNN<DoubleArray> {
        val numFeatures = trainingData[0].size
        val numSamples = trainingData.size
        val trainingData2D = Array(numSamples) { DoubleArray(numFeatures) }

        for (i in trainingData.indices) {
            trainingData2D[i] = trainingData[i]
        }
        // Fit the KNN model
        return KNN.fit(trainingData2D, labels, numNeighbors)
    }

    // Predict the gesture index based on input angles
    fun predict(angles: FloatArray): Int {
        val inputData = arrayOf(angles.map { it.toDouble() }.toDoubleArray())
        val predictions = knnModel?.predict(inputData)
        return predictions?.get(0) ?: -1
    }

    fun predictByResult(result: HandLandmarkerResult, handIndex: Int = 0): Int {
        // 손 랜드마크의 개수 확인
        if (result.landmarks().isEmpty() || handIndex >= result.landmarks().size) {
            return -1
        }
        // 특정 손의 랜드마크 선택
        val selectedHandLandmarks = result.landmarks()[handIndex]
        val joint = Array(21) { FloatArray(3) }

        for (i in selectedHandLandmarks.indices) {
            joint[i][0] = selectedHandLandmarks[i].x()
            joint[i][1] = selectedHandLandmarks[i].y()
            joint[i][2] = selectedHandLandmarks[i].z()
        }

        val angles = calculateAngles(joint)

        // KNN 모델을 사용하여 예측
        val predictions = knnModel?.predict(arrayOf(angles.map { it.toDouble() }.toDoubleArray()))
        return predictions?.get(0) ?: -1 // 예측된 인덱스를 반환
    }
}
