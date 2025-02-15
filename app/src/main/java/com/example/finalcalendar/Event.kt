package com.example.finalcalendar

import java.io.Serializable

data class Event(
    val title: String,
    val date: String,
    val time: String
) : Serializable
