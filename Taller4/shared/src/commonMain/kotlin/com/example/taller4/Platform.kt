package com.example.taller4

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform