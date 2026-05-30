package com.example

import org.jetbrains.exposed.sql.Table

object Users : Table("users") {
    val idUser = integer("id_user").autoIncrement()
    val namaLengkap = varchar("nama_lengkap", 100)
    val email = varchar("email", 100)
    val password = varchar("password", 255)
    val role = varchar("role", 20)

    override val primaryKey = PrimaryKey(idUser)
}

object LaporanGangguan : Table("laporan_gangguan") {
    val idLaporan = integer("id_laporan").autoIncrement()
    val idUser = integer("id_user")
    val deskripsiGangguan = text("deskripsi_gangguan")
    val lokasi = text("lokasi")
    val fotoGangguan = varchar("foto_gangguan", 255).nullable()
    val statusLaporan = varchar("status_laporan", 20).default("Menunggu")

    override val primaryKey = PrimaryKey(idLaporan)
}

object PasangBaru : Table("pasang_baru") {
    val idPengajuan = integer("id_pengajuan").autoIncrement()
    val idUser = integer("id_user")
    val alamatPasang = text("alamat_pasang")
    val dokumenPersyaratan = varchar("dokumen_persyaratan", 255).nullable()
    val statusPengajuan = varchar("status_pengajuan", 20).default("Ditinjau")

    override val primaryKey = PrimaryKey(idPengajuan)
}