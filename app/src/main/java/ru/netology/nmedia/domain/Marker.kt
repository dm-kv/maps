package ru.netology.nmedia.domain

data class Marker(
    val id: Long = 0,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
)