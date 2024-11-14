package com.example.myapplication.util

val gestureLabels: Map<Int, String> = mapOf(
    0 to "middle_finger",
    1 to "heart",
    2 to "heart_twohands",
    3 to "thumb_up",
    4 to "v",
    5 to "ok",
    6 to "call",
    7 to "alien",
    8 to "baby",
    9 to "four",
    10 to "mandoo",
    11 to "one",
    12 to "rabbit",
    13 to "rock",
    14 to "three",
    15 to "two",
    16 to "eight",
    17 to "five",
    18 to "lucky_finger",
    19 to "seven",
    20 to "six",
    21 to "wolf"
)
val reversedGestureLabels: Map<String, Int> = gestureLabels.entries.associate { (key, value) -> value to key }

val questionType: Map<String, Int> = mapOf(
    "copy" to 0,
    "win" to 1,
    "lose" to 2,
    "calc" to 3
)

val difficulty: Map<String,Int> = mapOf(
    "EASY" to 0,
    "NORMAL" to 1,
    "HARD" to 2
)