package com.example.taller5

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform