package com.example.handbraintoktok.util

import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

import kotlin.math.acos
import kotlin.math.sqrt
import kotlin.math.pow

import kotlin.math.acos
import kotlin.math.sqrt

fun calculateAngles(joint: Array<FloatArray>): FloatArray {
    // Original angle calculation
    val v1 = arrayOf(
        joint[0], joint[1], joint[2], joint[3], joint[0], joint[5], joint[6], joint[7], joint[0],
        joint[9], joint[10], joint[11], joint[0], joint[13], joint[14], joint[15], joint[0], joint[17], joint[18], joint[19]
    )
    val v2 = arrayOf(
        joint[1], joint[2], joint[3], joint[4], joint[5], joint[6], joint[7], joint[8], joint[9],
        joint[10], joint[11], joint[12], joint[13], joint[14], joint[15], joint[16], joint[17], joint[18], joint[19], joint[20]
    )

    val v = Array(v1.size) { FloatArray(3) }
    for (i in v1.indices) {
        for (j in 0..2) {
            v[i][j] = v2[i][j] - v1[i][j]
        }
        val norm = sqrt(v[i].map { it * it }.sum())
        for (j in 0..2) {
            v[i][j] /= norm
        }
    }

    val angleIndices1 = arrayOf(0, 1, 2, 4, 5, 6, 8, 9, 10, 12, 13, 14, 16, 17, 18)
    val angleIndices2 = arrayOf(1, 2, 3, 5, 6, 7, 9, 10, 11, 13, 14, 15, 17, 18, 19)
    val angles = FloatArray(angleIndices1.size)

    for (i in angleIndices1.indices) {
        val dotProduct = (0..2).sumOf { j -> v[angleIndices1[i]][j] * v[angleIndices2[i]][j].toDouble() }
        angles[i] = Math.toDegrees(acos(dotProduct)).toFloat()
    }

    // Palm-fingertip angle calculation
    val palmCenter = arrayOf(joint[2], joint[6], joint[10], joint[14], joint[18])
    val fingerTips = arrayOf(joint[4], joint[8], joint[12], joint[16], joint[20])

    val pv = Array(5) { FloatArray(3) }
    for (i in fingerTips.indices) {
        for (j in 0..2) {
            pv[i][j] = fingerTips[i][j] - palmCenter[i][j]
        }
        val norm = sqrt(pv[i].map { it * it }.sum())
        for (j in 0..2) {
            pv[i][j] /= norm
        }
    }

    val fingerAngleIndices1 = arrayOf(0, 0, 0, 0, 1, 1, 1, 2, 2, 3)
    val fingerAngleIndices2 = arrayOf(1, 2, 3, 4, 2, 3, 4, 3, 4, 4)
    val fingerAngles = FloatArray(fingerAngleIndices1.size)

    for (i in fingerAngleIndices1.indices) {
        val dotProduct = (0..2).sumOf { j ->
            pv[fingerAngleIndices1[i]][j] * pv[fingerAngleIndices2[i]][j].toDouble()
        }
        fingerAngles[i] = Math.toDegrees(acos(dotProduct)).toFloat()
    }

    // Combine all angles into a single array
    return angles + fingerAngles
}

// Helper function to calculate dot product
fun dotProduct(a: FloatArray, b: FloatArray): Double {
    return (0..2).sumOf { i -> a[i] * b[i].toDouble() }
}

// Helper function to calculate vector norm
fun norm(v: FloatArray): Double {
    return sqrt(v.map { it * it }.sum().toDouble())
}

fun calculateDistances(joint: Array<FloatArray>): FloatArray {
    val distances = mutableListOf<Float>()

    // 1. Distance between palm center and finger tips
    val palmCenter = joint[0]
    val fingerTips = arrayOf(joint[4], joint[8], joint[12], joint[16], joint[20])

    val palmToTipDistances = fingerTips.map { tip ->
        sqrt((0..2).map { i -> (tip[i] - palmCenter[i]).pow(2) }.sum()).toFloat()
    }
    distances.addAll(palmToTipDistances)

    // 2. Distance between adjacent finger tips
    for (i in fingerTips.indices) {
        for (j in i + 1 until fingerTips.size) {
            val dist = sqrt((0..2).map { k -> (fingerTips[i][k] - fingerTips[j][k]).pow(2) }.sum()).toFloat()
            distances.add(dist)
        }
    }

    return distances.toFloatArray().map { it * 200 }.toFloatArray()
}