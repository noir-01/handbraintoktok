package com.example.myapplication.util.mediapipe

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import smile.classification.RandomForest
import smile.data.formula.Formula
import smile.data.DataFrame
import smile.data.Tuple
import smile.data.type.DataTypes.DoubleType
import smile.data.type.StructField
import smile.data.type.StructType
import smile.data.vector.IntVector
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

object RandomForestsObj {
    // Application Context를 사용하여 메모리 누수 방지
    private lateinit var appContext: Context

    private lateinit var trainingData: Array<DoubleArray>
    private lateinit var labels: IntArray

    // Lazy initialization of RandomForest model
    private var randomForestModel: RandomForest? = null

    // Number of trees and maximum depth for RandomForest
    private const val NUM_TREES = 100
    private const val MAX_DEPTH = 20

    fun saveModel(model: RandomForest, filePath: String) {
        ObjectOutputStream(FileOutputStream(filePath)).use { it.writeObject(model) }
    }
    fun loadModel(filePath: String): RandomForest {
        return ObjectInputStream(FileInputStream(filePath)).use { it.readObject() as RandomForest }
    }
    // Initialize with application context
    fun initialize(context: Context) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val isModelCreated = sharedPreferences.getBoolean("isModelCreated", false)
        val editor = sharedPreferences.edit()
        val modelPath = context.filesDir.absolutePath + "/random_forest_model.bin"

        // Application Context 사용
        if (!::appContext.isInitialized && !isModelCreated) {
            this.appContext = context.applicationContext
            val start = System.currentTimeMillis()
            loadTrainingData()
            randomForestModel = trainRandomForestModel()
            saveModel(randomForestModel!!,modelPath)
            Log.d("grs time","model init time: ${(System.currentTimeMillis()-start)/1000}s")
            editor.putBoolean("isModelCreated",true)
            editor.apply()
        }else if(isModelCreated){
            runBlocking {
                withContext(Dispatchers.Default) {
                    val start = System.currentTimeMillis()
                    randomForestModel = loadModel(modelPath)
                    Log.d("grs time","model load time: ${(System.currentTimeMillis()-start)/1000}s")
                }
            }
        }
    }

    val schema = StructType(
        *Array(25) { StructField("feature${it+1}", DoubleType) } // Correct usage of DoubleType
    )

    // Load training data from the CSV file
    private fun loadTrainingData() {
        val features = ArrayList<DoubleArray>()
        val labelsList = ArrayList<Int>()
        runBlocking {
            withContext(Dispatchers.Default) {
                appContext.assets.open("gesture_25.csv").use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { br ->
                        br.readLine() // 헤더 스킵
                        var line: String?

                        while (br.readLine().also { line = it } != null) {
                            val values = line!!.split(",").map { it.trim() }
                            // 마지막 값이 label, 그 전까지가 feature
                            // 실제 CSV가 feature 15개 + label 1개 라고 가정
                            val featureCount = 25
                            val feature = DoubleArray(featureCount)

                            for (i in 0 until featureCount) {
                                feature[i] = values[i].toDouble()
                            }

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


     ////Train RandomForest model
     private fun trainRandomForestModel(): RandomForest {
         // 레이블을 Int 배열로 변환
         val intLabels = labels.map { it.toInt() }.toIntArray()

         // feature만 담은 2차원 배열 생성
         val featureCount = trainingData[0].size
         val combinedFeatures = Array(trainingData.size) { i ->
             DoubleArray(featureCount) { j -> trainingData[i][j] }
         }

         val featureColumns = (1..featureCount).map { "feature$it" }.toTypedArray()
         val featureDF = DataFrame.of(combinedFeatures, *featureColumns)
         val labelColumn = IntVector.of("label", intLabels)
         val dataFrame = featureDF.merge(labelColumn)
         val formula = Formula.lhs("label")

         // RandomForest 분류 모델 학습
         return RandomForest.fit(formula,dataFrame)
     }

    // Predict gesture using HandLandmarkerResult
    fun predictByResult(result: HandLandmarkerResult, handIndex: Int = 0): Int {
        // Check if landmarks are available
        if (result.landmarks().isEmpty() || handIndex >= result.landmarks().size) {
            return -1
        }

        // Select landmarks for specific hand
        val selectedHandLandmarks = result.landmarks()[handIndex]
        val joint = Array(21) { FloatArray(3) }

        for (i in selectedHandLandmarks.indices) {
            joint[i][0] = selectedHandLandmarks[i].x()
            joint[i][1] = selectedHandLandmarks[i].y()
            joint[i][2] = selectedHandLandmarks[i].z()
        }

        // Calculate angles
        val angles = calculateAngles(joint)

        // Predict using RandomForest
        val inputData = angles.map { it.toDouble() }.toDoubleArray()

// Create the Tuple with inputData and schema
        val inputTuple = Tuple.of(inputData, schema)

        return randomForestModel?.predict(inputTuple)?:-1  // Make prediction
    }
}