package com.example.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.IBinder
import com.example.MainActivity
import com.example.model.Song

/**
 * Servicio en primer plano (Foreground Service) para gestionar la reproducción de música en segundo plano.
 * Proporciona controles interactivos en la barra de notificaciones y la pantalla de bloqueo mediante MediaStyle.
 */
class MusicService : Service() {

    companion object {
        const val CHANNEL_ID = "kaku_next_playback_channel"
        const val NOTIFICATION_ID = 4529

        const val ACTION_PLAY = "com.example.action.PLAY"
        const val ACTION_PAUSE = "com.example.action.PAUSE"
        const val ACTION_NEXT = "com.example.action.NEXT"
        const val ACTION_PREVIOUS = "com.example.action.PREVIOUS"
        const val ACTION_STOP = "com.example.action.STOP"
    }

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        setupMediaSession()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun getAttributedContext(): Context {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            createAttributionContext("default")
        } else {
            this
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val context = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            applicationContext.createAttributionContext("default")
        } else {
            applicationContext
        }

        // Sincronizar el contexto en la instancia compartida de PlaybackManager
        PlaybackManager.setSandboxContext(context)

        when (action) {
            ACTION_PLAY -> {
                buildAndShowNotification(true)
            }
            ACTION_PAUSE -> {
                buildAndShowNotification(false)
            }
            ACTION_NEXT -> {
                PlaybackManager.playNext(context)
            }
            ACTION_PREVIOUS -> {
                PlaybackManager.playPrevious(context)
            }
            ACTION_STOP -> {
                stopForegroundService()
            }
        }
        return START_NOT_STICKY
    }

    private fun setupMediaSession() {
        val attrContext = getAttributedContext()
        mediaSession = MediaSession(attrContext, "KakuNextSession").apply {
            isActive = true
            setCallback(object : MediaSession.Callback() {
                override fun onPlay() {
                    PlaybackManager.togglePlayPause(attrContext)
                }

                override fun onPause() {
                    PlaybackManager.togglePlayPause(attrContext)
                }

                override fun onSkipToNext() {
                    PlaybackManager.playNext(attrContext)
                }

                override fun onSkipToPrevious() {
                    PlaybackManager.playPrevious(attrContext)
                }

                override fun onStop() {
                    stopForegroundService()
                }
            })
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Reproductor Kaku Next",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controles de reproducción de audio sobre la barra de estado y sistema"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildAndShowNotification(isPlaying: Boolean) {
        val song = PlaybackManager.currentSong.value ?: return

        // Actualizar el estado de sesión multimedia de Android
        updateMediaSessionState(isPlaying, song)

        // Acción al hacer click en la notificación (abrir MainActivity)
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val contentPendingIntent = PendingIntent.getActivity(this, 10, openIntent, pendingIntentFlags)

        // Intents para controles multimedia
        val playPauseAction = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
        val playPauseIntent = Intent(this, MusicService::class.java).apply { action = playPauseAction }
        val playPausePendingIntent = PendingIntent.getService(this, 20, playPauseIntent, pendingIntentFlags)

        val prevIntent = Intent(this, MusicService::class.java).apply { action = ACTION_PREVIOUS }
        val prevPendingIntent = PendingIntent.getService(this, 30, prevIntent, pendingIntentFlags)

        val nextIntent = Intent(this, MusicService::class.java).apply { action = ACTION_NEXT }
        val nextPendingIntent = PendingIntent.getService(this, 40, nextIntent, pendingIntentFlags)

        val stopIntent = Intent(this, MusicService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(this, 50, stopIntent, pendingIntentFlags)

        // Construir notificación usando Notification.Builder nativo que soporta MediaStyle sin dependencias externas complejas
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        // Crear una miniatura decorativa basada en el color dinámico asignado a la canción
        val coverBitmap = createCoverBitmap(song.coverColor)

        builder.setContentTitle(song.title)
            .setContentText(song.artist)
            .setSubText(song.album)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setLargeIcon(coverBitmap)
            .setContentIntent(contentPendingIntent)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setOngoing(isPlaying)

        // Asignar el estilo de reproducción con tokens de la sesión
        val mediaStyle = Notification.MediaStyle()
            .setMediaSession(mediaSession?.sessionToken)
            .setShowActionsInCompactView(0, 1, 2) // Botones prev, play/pause, next en vista compacta

        builder.setStyle(mediaStyle)

        // Acción 0: Anterior
        builder.addAction(
            Notification.Action.Builder(
                android.R.drawable.ic_media_previous,
                "Anterior",
                prevPendingIntent
            ).build()
        )

        // Acción 1: Reproducir / Pausar
        builder.addAction(
            Notification.Action.Builder(
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isPlaying) "Pausar" else "Reproducir",
                playPausePendingIntent
            ).build()
        )

        // Acción 2: Siguiente
        builder.addAction(
            Notification.Action.Builder(
                android.R.drawable.ic_media_next,
                "Siguiente",
                nextPendingIntent
            ).build()
        )

        // Acción 3: Detener/Cerrar
        builder.addAction(
            Notification.Action.Builder(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Cerrar",
                stopPendingIntent
            ).build()
        )

        val notification = builder.build()

        // Ajustar el inicio de Foreground Service según el SDK correspondiente
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        // Si pausó la canción, convertimos la notificación en "descartable" deslizando
        if (!isPlaying) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_DETACH)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(false)
            }
        }
    }

    private fun updateMediaSessionState(isPlaying: Boolean, song: Song) {
        val stateStatus = if (isPlaying) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED
        val positionMs = PlaybackManager.currentPosition.value.toLong() * 1000

        val playbackState = PlaybackState.Builder()
            .setState(stateStatus, positionMs, 1.0f)
            .setActions(
                PlaybackState.ACTION_PLAY or
                PlaybackState.ACTION_PAUSE or
                PlaybackState.ACTION_PLAY_PAUSE or
                PlaybackState.ACTION_SKIP_TO_NEXT or
                PlaybackState.ACTION_SKIP_TO_PREVIOUS or
                PlaybackState.ACTION_STOP
            )
            .build()
        mediaSession?.setPlaybackState(playbackState)

        val metadata = MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_TITLE, song.title)
            .putString(MediaMetadata.METADATA_KEY_ARTIST, song.artist)
            .putString(MediaMetadata.METADATA_KEY_ALBUM, song.album)
            .putLong(MediaMetadata.METADATA_KEY_DURATION, song.durationSeconds.toLong() * 1000)
            .build()
        mediaSession?.setMetadata(metadata)
    }

    private fun createCoverBitmap(colorVal: Long): Bitmap {
        val width = 120
        val height = 120
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = colorVal.toInt() or 0xFF000000.toInt()
            isAntiAlias = true
        }
        canvas.drawCircle(width / 2f, height / 2f, width / 2f, paint)
        return bitmap
    }

    private fun stopForegroundService() {
        PlaybackManager.release()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Si cierran la app, retenemos el servicio reproduciendo, pero si está pausado liberamos todo
        if (!PlaybackManager.isPlaying.value) {
            stopForegroundService()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession?.release()
        mediaSession = null
    }
}
