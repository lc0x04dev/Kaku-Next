package com.example.model

/**
 * Representa una canción en el ecosistema Kaku Next.
 */
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: String, // e.g. "3:45"
    val durationSeconds: Int,
    val lyrics: String?,
    val coverColor: Long, // Color Hex (e.g. 0xFF00F0FF) for ambient styling
    val isFavorite: Boolean = false,
    val filePath: String? = null
)
