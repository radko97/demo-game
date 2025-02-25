package com.example.demogame

data class Card(
    val id: Int,
    var text: String,
    val time: Int,
    val level: Int,
    val rng: Int,
    val jokerCode: Int
)
