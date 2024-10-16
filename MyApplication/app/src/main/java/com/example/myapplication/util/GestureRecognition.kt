package com.example.myapplication.util

import java.io.BufferedReader
import android.content.Context
import android.util.Log
import com.example.myapplication.R
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import java.io.FileReader
import smile.classification.KNN
import java.io.InputStreamReader
import kotlin.math.acos
import kotlin.math.sqrt
import com.example.myapplication.util.calculateAngles
import com.example.myapplication.util.calculateDistances

class GestureRecognition(private val context: Context, private val numNeighbors: Int = 3) {

    private lateinit var trainingData: Array<DoubleArray>
    private lateinit var labels: IntArray
    private lateinit var knnModel: KNN<DoubleArray>

    init{
        loadTrainingData()
        initialize()
    }

    // Load training data from the CSV file
    private fun loadTrainingData() {
        val features = ArrayList<DoubleArray>()
        val labelsList = ArrayList<Int>()

        // Read the CSV file from assets
        context.assets.open("gesture_25.csv").use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { br ->
                val header = br.readLine() // Read header if present
                var line: String?

                while (br.readLine().also { line = it } != null) {
                    val values = line!!.split(",").map { it.trim() }
                    val feature = DoubleArray(values.size - 1) // Last column is the label

                    for (i in 0 until values.size - 1) {
                        feature[i] = values[i].toDouble() // Convert feature strings to double
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

    // Initialize the KNN model
    fun initialize() {
        loadTrainingData()

        // Convert trainingData to a 2D Double array for the KNN model
        val numFeatures = trainingData[0].size
        val numSamples = trainingData.size
        val trainingData2D = Array(numSamples) { DoubleArray(numFeatures) }

        for (i in trainingData.indices) {
            trainingData2D[i] = trainingData[i]
        }

        // Fit the KNN model
        knnModel = KNN.fit(trainingData2D, labels, numNeighbors)
    }

    // Predict the gesture index based on input angles
    fun predict(angles: FloatArray): Int {
        val inputData = arrayOf(angles.map { it.toDouble() }.toDoubleArray())
        val predictions = knnModel.predict(inputData)
        return predictions[0]
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
        val predictions = knnModel.predict(arrayOf(angles.map { it.toDouble() }.toDoubleArray()))
        return predictions[0] // 예측된 인덱스를 반환
    }

    fun calcAngles(result: HandLandmarkerResult): FloatArray {
        val joint = Array(21) { FloatArray(3) }
        // Calculate angles using dot product and arccos
        val angleIndices1 = arrayOf(0, 1, 2, 4, 5, 6, 8, 9, 10, 12, 13, 14, 16, 17, 18)
        val angleIndices2 = arrayOf(1, 2, 3, 5, 6, 7, 9, 10, 11, 13, 14, 15, 17, 18, 19)
        val angles = FloatArray(angleIndices1.size)

        // Compute vectors between specific joints (v1 and v2)
        val indices1 = arrayOf(0, 1, 2, 3, 0, 5, 6, 7, 0, 9, 10, 11, 0, 13, 14, 15, 0, 17, 18, 19)
        val indices2 = arrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)

        for (res in result.landmarks()) {
            for (i in res.indices) {
                // Store x, y, z coordinates for each landmark in the joint array
                joint[i][0] = res[i].x()
                joint[i][1] = res[i].y()
                joint[i][2] = res[i].z()
            }

            val vectors = Array(indices1.size) { FloatArray(3) }

            for (i in indices1.indices) {
                // Calculate v = v2 - v1
                for (j in 0..2) {
                    vectors[i][j] = joint[indices2[i]][j] - joint[indices1[i]][j]
                }

                // Normalize the vectors
                val norm = sqrt(vectors[i].map { it * it }.sum())
                for (j in 0..2) {
                    vectors[i][j] /= norm
                }
            }

            for (i in angleIndices1.indices) {
                val dotProduct =
                    (0..2).map { j -> vectors[angleIndices1[i]][j] * vectors[angleIndices2[i]][j] }
                        .sum()
                angles[i] = Math.toDegrees(acos(dotProduct).toDouble()).toFloat()
            }
        }
        return angles
    }
}


