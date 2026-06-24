package com.example.service

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.File
import java.io.InputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

object LyricsExtractor {
    private const val TAG = "LyricsExtractor"

    /**
     * Extracts embedded lyrics from an audio file (supporting SYLT, USLT, ©lyr).
     */
    fun extractLyrics(context: Context, filePath: String?): String? {
        if (filePath.isNullOrEmpty()) return null
        
        var inputStream: InputStream? = null
        try {
            inputStream = if (filePath.startsWith("content://")) {
                context.contentResolver.openInputStream(Uri.parse(filePath))
            } else {
                val file = File(filePath)
                if (file.exists()) file.inputStream() else null
            }
            
            if (inputStream == null) return null
            
            val bufferedStream = BufferedInputStream(inputStream)
            
            // Mark stream to read first 4 bytes to check format
            bufferedStream.mark(12)
            val headerBytes = ByteArray(4)
            val read = bufferedStream.read(headerBytes, 0, 4)
            bufferedStream.reset()
            
            if (read < 4) return null
            
            // Check if it's MP3/ID3
            if (headerBytes[0] == 'I'.toByte() && headerBytes[1] == 'D'.toByte() && headerBytes[2] == '3'.toByte()) {
                Log.d(TAG, "Detectado formato ID3v2 para archivo: $filePath")
                return parseId3Lyrics(bufferedStream)
            }
            
            // Check if it's MP4/M4A/AAC (box format, search for moov/ftyp/etc.)
            // Let's use the MP4 parser
            Log.d(TAG, "Detectado formato MP4/Box para archivo: $filePath")
            return parseMp4Lyrics(bufferedStream)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error extrayendo letras de $filePath: ${e.message}", e)
        } finally {
            try {
                inputStream?.close()
            } catch (e: Exception) {}
        }
        return null
    }

    /**
     * Parses ID3v2 tags from an InputStream to extract SYLT and USLT frames.
     */
    private fun parseId3Lyrics(inputStream: InputStream): String? {
        val dis = DataInputStream(inputStream)
        try {
            // Read ID3v2 header (10 bytes)
            val header = ByteArray(10)
            dis.readFully(header)
            
            if (header[0] != 'I'.toByte() || header[1] != 'D'.toByte() || header[2] != '3'.toByte()) {
                return null
            }
            
            val majorVersion = header[3].toInt()
            if (majorVersion < 2 || majorVersion > 4) {
                return null // only support ID3v2.2, v2.3, v2.4
            }
            
            // ID3v2 size is synchsafe (7 bits per byte)
            val size = ((header[6].toInt() and 0x7F) shl 21) or
                       ((header[7].toInt() and 0x7F) shl 14) or
                       ((header[8].toInt() and 0x7F) shl 7) or
                       (header[9].toInt() and 0x7F)
            
            if (size <= 0 || size > 15 * 1024 * 1024) { // safety limit 15MB
                return null
            }
            
            // Read the entire ID3 tag payload
            val id3Bytes = ByteArray(size)
            dis.readFully(id3Bytes)
            
            return extractLyricsFromId3Bytes(id3Bytes, majorVersion)
        } catch (e: Exception) {
            Log.e(TAG, "Error al parsear ID3v2: ${e.message}")
        }
        return null
    }

    private fun extractLyricsFromId3Bytes(id3Bytes: ByteArray, majorVersion: Int): String? {
        var offset = 0
        val size = id3Bytes.size
        
        var usltText: String? = null
        var syltText: String? = null
        
        // Loop through frames
        while (offset + 10 <= size) {
            val frameId: String
            val frameSize: Int
            
            if (majorVersion == 2) {
                // ID3v2.2 uses 3-character frame IDs and 3-byte size
                frameId = String(id3Bytes, offset, 3, StandardCharsets.US_ASCII)
                frameSize = ((id3Bytes[offset + 3].toInt() and 0xFF) shl 16) or
                            ((id3Bytes[offset + 4].toInt() and 0xFF) shl 8) or
                            (id3Bytes[offset + 5].toInt() and 0xFF)
                offset += 6
            } else {
                // ID3v2.3 & ID3v2.4 use 4-character frame IDs and 4-byte size
                frameId = String(id3Bytes, offset, 4, StandardCharsets.US_ASCII)
                
                if (majorVersion == 4) {
                    // ID3v2.4 frame size is synchsafe (7 bits per byte)
                    frameSize = ((id3Bytes[offset + 4].toInt() and 0x7F) shl 21) or
                                ((id3Bytes[offset + 5].toInt() and 0x7F) shl 14) or
                                ((id3Bytes[offset + 6].toInt() and 0x7F) shl 7) or
                                (id3Bytes[offset + 7].toInt() and 0x7F)
                } else {
                    // ID3v2.3 frame size is normal 32-bit int
                    frameSize = ((id3Bytes[offset + 4].toInt() and 0xFF) shl 24) or
                                ((id3Bytes[offset + 5].toInt() and 0xFF) shl 16) or
                                ((id3Bytes[offset + 6].toInt() and 0xFF) shl 8) or
                                (id3Bytes[offset + 7].toInt() and 0xFF)
                }
                offset += 10
            }
            
            // Check for padding (frame ID starting with null byte)
            if (frameId.isEmpty() || frameId[0] == '\u0000') {
                break
            }
            
            if (offset + frameSize > size) {
                break
            }
            
            // Check for USLT / ULT (Unsynchronized Lyrics)
            if (frameId == "USLT" || frameId == "ULT") {
                val parsed = parseUsltFrame(id3Bytes, offset, frameSize)
                if (!parsed.isNullOrBlank()) {
                    usltText = parsed
                }
            }
            // Check for SYLT / SLT (Synchronized Lyrics)
            else if (frameId == "SYLT" || frameId == "SLT") {
                val parsed = parseSyltFrame(id3Bytes, offset, frameSize)
                if (!parsed.isNullOrBlank()) {
                    syltText = parsed
                }
            }
            
            offset += frameSize
        }
        
        // Prefer SYLT (Synchronized) over USLT (Unsynchronized) if both exist!
        return syltText ?: usltText
    }

    private fun parseUsltFrame(bytes: ByteArray, offset: Int, frameSize: Int): String? {
        if (frameSize <= 4) return null
        
        val encoding = bytes[offset].toInt() and 0xFF
        val charset = getCharset(encoding)
        
        // Skip language code (3 bytes)
        var cursor = offset + 4
        val limit = offset + frameSize
        
        // Skip content descriptor (null-terminated string)
        cursor = skipNullTerminated(bytes, cursor, limit, encoding)
        
        if (cursor >= limit) return null
        
        return try {
            String(bytes, cursor, limit - cursor, charset).trim()
        } catch (e: Exception) {
            null
        }
    }

    private fun parseSyltFrame(bytes: ByteArray, offset: Int, frameSize: Int): String? {
        if (frameSize <= 6) return null
        
        val encoding = bytes[offset].toInt() and 0xFF
        val charset = getCharset(encoding)
        
        // Skip language (3 bytes)
        val timestampFormat = bytes[offset + 4].toInt() and 0xFF // 1 = ms, 2 = frames
        val contentType = bytes[offset + 5].toInt() and 0xFF
        
        var cursor = offset + 6
        val limit = offset + frameSize
        
        // Skip content descriptor
        cursor = skipNullTerminated(bytes, cursor, limit, encoding)
        
        if (cursor >= limit) return null
        
        val sb = java.lang.StringBuilder()
        
        try {
            while (cursor + 4 < limit) {
                // Read lyric text (null-terminated string)
                val textStart = cursor
                cursor = skipNullTerminated(bytes, cursor, limit, encoding)
                val textLen = (cursor - textStart) - (if (encoding == 1 || encoding == 2) 2 else 1)
                val text = if (textLen > 0) {
                    String(bytes, textStart, textLen, charset).trim()
                } else {
                    ""
                }
                
                if (cursor + 4 > limit) break
                
                // Read 4 bytes of timestamp
                val rawTime = ((bytes[cursor].toInt() and 0xFF) shl 24) or
                              ((bytes[cursor + 1].toInt() and 0xFF) shl 16) or
                              ((bytes[cursor + 2].toInt() and 0xFF) shl 8) or
                              (bytes[cursor + 3].toInt() and 0xFF)
                cursor += 4
                
                // Convert timestamp to milliseconds if unit is frames (usually unit is milliseconds=1)
                val timeMs = if (timestampFormat == 1) rawTime.toLong() else (rawTime * 26L) // rough conversion for MPEG frames
                
                if (timeMs >= 0) {
                    val minutes = timeMs / 60000
                    val seconds = (timeMs % 60000) / 1000
                    val hundredths = (timeMs % 1000) / 10
                    
                    val timeTag = String.format("[%02d:%02d.%02d]", minutes, seconds, hundredths)
                    if (sb.isNotEmpty()) {
                        sb.append("\n")
                    }
                    sb.append(timeTag).append(" ").append(text)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parseando SYLT frame: ${e.message}")
        }
        
        return if (sb.isNotEmpty()) sb.toString() else null
    }

    private fun skipNullTerminated(bytes: ByteArray, startOffset: Int, limit: Int, encoding: Int): Int {
        var cursor = startOffset
        if (encoding == 1 || encoding == 2) {
            // UTF-16 null terminator is 0x00 0x00 (aligned)
            while (cursor + 1 < limit) {
                if (bytes[cursor] == 0.toByte() && bytes[cursor + 1] == 0.toByte()) {
                    return cursor + 2
                }
                cursor += 2
            }
            return limit
        } else {
            // UTF-8 or ISO-8859-1 null terminator is 0x00
            while (cursor < limit) {
                if (bytes[cursor] == 0.toByte()) {
                    return cursor + 1
                }
                cursor++
            }
            return limit
        }
    }

    private fun getCharset(encoding: Int): Charset {
        return when (encoding) {
            1 -> StandardCharsets.UTF_16
            2 -> StandardCharsets.UTF_16BE
            3 -> StandardCharsets.UTF_8
            else -> StandardCharsets.ISO_8859_1
        }
    }

    /**
     * Parses nested MP4 boxes from an InputStream to find ©lyr -> data.
     */
    private fun parseMp4Lyrics(inputStream: InputStream): String? {
        val dis = DataInputStream(inputStream)
        return try {
            parseMp4Container(dis, Long.MAX_VALUE)
        } catch (e: Exception) {
            Log.e(TAG, "Error parseando MP4: ${e.message}")
            null
        }
    }

    private fun parseMp4Container(dis: DataInputStream, containerSize: Long): String? {
        var bytesRead = 0L
        try {
            while (bytesRead + 8 <= containerSize) {
                val sizeVal = dis.readInt()
                val typeBytes = ByteArray(4)
                dis.readFully(typeBytes)
                bytesRead += 8
                
                val size = if (sizeVal == 1) {
                    val extendedSize = dis.readLong()
                    bytesRead += 8
                    extendedSize
                } else {
                    sizeVal.toLong() and 0xFFFFFFFFL
                }
                
                val type = String(typeBytes, StandardCharsets.US_ASCII)
                val payloadSize = size - 8 - (if (sizeVal == 1) 8 else 0)
                
                if (type == "moov" || type == "udta" || type == "ilst" || type == "\u00A9lyr") {
                    val result = parseMp4Container(dis, payloadSize)
                    if (result != null) return result
                    bytesRead += payloadSize
                } else if (type == "meta") {
                    // meta box has a 4-byte flags/version field before children
                    if (payloadSize >= 4) {
                        dis.readInt() // skip version/flags
                        val result = parseMp4Container(dis, payloadSize - 4)
                        if (result != null) return result
                        bytesRead += payloadSize
                    } else {
                        dis.skipBytes(payloadSize.toInt())
                        bytesRead += payloadSize
                    }
                } else if (type == "data") {
                    // data box under ©lyr has 8 bytes header (4 bytes type, 4 bytes locale)
                    if (payloadSize > 8) {
                        dis.readInt() // skip type indicator
                        dis.readInt() // skip locale
                        val textBytes = ByteArray((payloadSize - 8).toInt())
                        dis.readFully(textBytes)
                        bytesRead += payloadSize
                        return String(textBytes, StandardCharsets.UTF_8)
                    } else {
                        dis.skipBytes(payloadSize.toInt())
                        bytesRead += payloadSize
                    }
                } else {
                    // Skip unneeded boxes safely
                    var skipped = 0L
                    while (skipped < payloadSize) {
                        val skipCount = dis.skip(payloadSize - skipped)
                        if (skipCount <= 0L) break
                        skipped += skipCount
                    }
                    bytesRead += payloadSize
                }
            }
        } catch (e: Exception) {
            // reached end or error
        }
        return null
    }
}
