package com.example 

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

// WADAH DATA (MODELS / DATA CLASS)

// 1. Wadah untuk Login
data class LoginRequest(val email: String, val password: String)
data class UserData(val idUser: Int, val namaLengkap: String, val email: String, val role: String)
data class BaseResponse(val success: Boolean, val message: String, val data: UserData? = null)

// 2. Wadah untuk Laporan Gangguan
data class GangguanData(val idLaporan: Int, val idUser: Int, val deskripsi: String, val lokasi: String, val status: String)
data class ListGangguanResponse(val success: Boolean, val message: String, val data: List<GangguanData>)
data class LaporRequest(val idUser: Int, val deskripsi: String, val lokasi: String)

// 3. Wadah baru untuk Pasang Baru
data class PasangBaruData(
    val idPengajuan: Int,
    val idUser: Int,
    val alamatPasang: String,
    val status: String
)
data class ListPasangBaruResponse(val success: Boolean, val message: String, val data: List<PasangBaruData>)
data class PasangBaruRequest(val idUser: Int, val alamatPasang: String)

// JALUR API (ROUTING)
fun Application.configureRouting() {

    // Paksa konek ke database MySQL
    Database.connect(
        url = "jdbc:mysql://localhost:3306/db_pln",
        driver = "com.mysql.cj.jdbc.Driver",
        user = "root",
        password = ""
    )

    routing {
        // FITUR 1: LOGIN
        post("/api/login") {
            try {
                val request = call.receive<LoginRequest>()
                val user = transaction {
                    Users.select {
                        (Users.email eq request.email) and (Users.password eq request.password)
                    }.singleOrNull()
                }

                if (user != null) {
                    val userData = UserData(
                        idUser = user[Users.idUser],
                        namaLengkap = user[Users.namaLengkap],
                        email = user[Users.email],
                        role = user[Users.role]
                    )
                    call.respond(BaseResponse(success = true, message = "Login Berhasil!", data = userData))
                } else {
                    call.respond(BaseResponse(success = false, message = "Email atau Password salah!", data = null))
                }
            } catch (e: Exception) {
                call.respond(BaseResponse(success = false, message = "Error: ${e.message}"))
            }
        }

        // FITUR 2: LAPOR GANGGUAN

        // A. Ambil Daftar Gangguan (GET)
        get("/api/gangguan") {
            try {
                val daftarGangguan = transaction {
                    LaporanGangguan.selectAll().map {
                        GangguanData(
                            idLaporan = it[LaporanGangguan.idLaporan],
                            idUser = it[LaporanGangguan.idUser],
                            deskripsi = it[LaporanGangguan.deskripsiGangguan],
                            lokasi = it[LaporanGangguan.lokasi],
                            status = it[LaporanGangguan.statusLaporan]
                        )
                    }
                }
                call.respond(ListGangguanResponse(success = true, message = "Data berhasil diambil!", data = daftarGangguan))
            } catch (e: Exception) {
                call.respond(BaseResponse(success = false, message = "Error: ${e.message}"))
            }
        }

        // B. Kirim Laporan Baru (POST)
        post("/api/gangguan") {
            try {
                val request = call.receive<LaporRequest>()
                transaction {
                    LaporanGangguan.insert {
                        it[idUser] = request.idUser
                        it[deskripsiGangguan] = request.deskripsi
                        it[lokasi] = request.lokasi
                    }
                }
                call.respond(BaseResponse(success = true, message = "Laporan berhasil dikirim!"))
            } catch (e: Exception) {
                call.respond(BaseResponse(success = false, message = "Error: ${e.message}"))
            }
        }

        // ==========================================
        // FITUR 3: PASANG BARU
        // ==========================================

        // A. Ambil Daftar Pengajuan Pasang Baru (GET)
        get("/api/pasang-baru") {
            try {
                val daftarPasangBaru = transaction {
                    PasangBaru.selectAll().map {
                        PasangBaruData(
                            idPengajuan = it[PasangBaru.idPengajuan],
                            idUser = it[PasangBaru.idUser],
                            alamatPasang = it[PasangBaru.alamatPasang],
                            status = it[PasangBaru.statusPengajuan]
                        )
                    }
                }
                call.respond(ListPasangBaruResponse(success = true, message = "Data pengajuan berhasil diambil!", data = daftarPasangBaru))
            } catch (e: Exception) {
                call.respond(BaseResponse(success = false, message = "Error: ${e.message}"))
            }
        }

        // B. Kirim Pengajuan Pasang Baru (POST)
        post("/api/pasang-baru") {
            try {
                val request = call.receive<PasangBaruRequest>()
                transaction {
                    PasangBaru.insert {
                        it[idUser] = request.idUser
                        it[alamatPasang] = request.alamatPasang
                        // status_pengajuan otomatis diisi 'Ditinjau' oleh default database
                    }
                }
                call.respond(BaseResponse(success = true, message = "Pengajuan pasang baru berhasil dikirim!"))
            } catch (e: Exception) {
                call.respond(BaseResponse(success = false, message = "Error: ${e.message}"))
            }
        }

    }
}