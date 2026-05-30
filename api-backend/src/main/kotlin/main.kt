package com.example

import io.ktor.server.application.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.sql.Database

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    // 1. KONEKSI KE DATABASE
    Database.connect(
        url = "jdbc:mysql://localhost:3306/db_pln",
        driver = "com.mysql.cj.jdbc.Driver",
        user = "root",
        password = ""
    )

    // 2. ROUTING
    configureSerialization()
    configureRouting()
}