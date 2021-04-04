package com.example.notification


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import java.util.*


class MainActivity : AppCompatActivity() ,Playable {
    private lateinit var  mediaSessionCompat:MediaSessionCompat
    private lateinit var tracks: MutableList<Track>
    private lateinit var tvTrackName: TextView
    private lateinit var play: ImageButton
    private var position = 0
    private var isPlaying = false
    private val actionPrevious = "actionprevious"
    private val actionPlay = "actionplay"
    private val actionNext = "actionnext"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mediaSessionCompat = MediaSessionCompat(this, "tag")
        tvTrackName = findViewById(R.id.trackName)
        play = findViewById(R.id.play)
        tracks = mutableListOf()
        populateTracks()
        registerReceiver(broadcastReceiver, IntentFilter("TRACKS_TRACKS"))

    }

    private var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.extras!!.getString("actionname")) {
                actionPrevious -> onTrackPrevious()
                actionPlay -> if (isPlaying) {
                    onTrackPause()
                } else {
                    onTrackPlay()
                }
                actionNext -> onTrackNext()
            }
        }
    }
    private fun populateTracks() {
         tracks.add(Track("Track 1", "Artist 1", R.drawable.t1))
        tracks.add(Track("Track 2", "Artist 2", R.drawable.t2))
        tracks.add(Track("Track 3", "Artist 3", R.drawable.t3))
        tracks.add(Track("Track 4", "Artist 4", R.drawable.t4))
    }

    private fun showMediaNotification(track: Track, playButton: Int, pos: Int, size: Int){
        var icPrevious: Int = 0
        var icNext: Int = 0
        var pendingIntentPrevious: PendingIntent? = null
        var pendingIntentNext: PendingIntent? = null

        val notificationManager =  getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "Channel_id_music", "Channel_name_music", NotificationManager.IMPORTANCE_LOW
            )
            notificationChannel.description = "Channel_description_music"
            notificationChannel.setSound(null, null)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notificationBuilder = NotificationCompat.Builder(this, "Channel_id_music")

        if (pos != 0) {
            val intentPrevious: Intent = Intent(this, NotificationMediaReceiver::class.java)
                .setAction(actionPrevious)
            pendingIntentPrevious = PendingIntent.getBroadcast(
                this, 0,
                intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT
            )

        }else{

            pendingIntentPrevious = null
            // if this first song play it from start
        }

        val intentPlay: Intent = Intent(this, NotificationMediaReceiver::class.java)
            .setAction(actionPlay)
        val pendingIntentPlay = PendingIntent.getBroadcast(
            this, 0,
            intentPlay, PendingIntent.FLAG_UPDATE_CURRENT
        )

        pendingIntentNext = if (pos != size) {
            val intentNext: Intent = Intent(this, NotificationMediaReceiver::class.java)
                .setAction(actionNext)
            PendingIntent.getBroadcast(
                this, 0,
                intentNext, PendingIntent.FLAG_UPDATE_CURRENT
            )

        } else{

            null
        }

        // notification with MEDIA STYLE
        val artwork:Bitmap =BitmapFactory.decodeResource(resources, track.image)
        notificationBuilder
            .setSmallIcon(R.drawable.ic_baseline_music_note_24)
            .setContentTitle(track.title)
            .setContentText(track.artist)
            .setLargeIcon(artwork)
            .addAction(R.drawable.ic_dislike, "Dislike", null)
            .addAction(R.drawable.ic_previous, "Previous", pendingIntentPrevious)
            .addAction(playButton, "Play", pendingIntentPlay)
            .addAction(R.drawable.ic_next, "Next", pendingIntentNext)
            .addAction(R.drawable.ic_like, "Like", null)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(1, 2, 3)
                    .setMediaSession(mediaSessionCompat.sessionToken)
            )
            .setSubText("Sub Text")
            .priority = NotificationCompat.PRIORITY_LOW

        notificationManager.notify(1, notificationBuilder.build())
    }

    fun default(view: View) {
        val rand = Random()
        val idNotification = rand.nextInt(1000000000)

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationManager =  getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "Channel id", "Channel name", NotificationManager.IMPORTANCE_LOW
            )
            notificationChannel.description = "Channel description"
            notificationChannel.setSound(null, null)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notificationBuilder = NotificationCompat.Builder(this, "Channel id")


    notificationBuilder.setAutoCancel(true)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.ic_baseline_notifications_active_24)
            .setTicker(resources.getString(R.string.app_name))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setSound(soundUri)
            .setContentTitle("Title")
            .setContentText("message")
        notificationManager.notify(idNotification, notificationBuilder.build())
    }

    fun action(view: View) {
        val rand = Random()
        val idNotification = rand.nextInt(1000000000)

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationManager =  getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "Channel id", "Channel name", NotificationManager.IMPORTANCE_LOW
            )
            notificationChannel.description = "Channel description"
            notificationChannel.setSound(null, null)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notificationBuilder = NotificationCompat.Builder(this, "Channel id")

    val openActivityIntent: Intent = Intent(this,MainActivity::class.java)
        val openActivityPendingIntent : PendingIntent  = PendingIntent.getActivity(
            this,
            0,
            openActivityIntent,
            0) // we put flag to 0 cause we don't need to any flag in this action

        val toastIntent: Intent = Intent(this,NotificationReceiver::class.java)
        toastIntent.putExtra("toastMessage","message from notification")
        val toastPendingIntent : PendingIntent  = PendingIntent.getBroadcast(
            this,
            1,
            toastIntent,
            PendingIntent.FLAG_UPDATE_CURRENT) // we put flag to FLAG_UPDATE_CURRENT cause when send notification with different message update to new message

        notificationBuilder
            .setAutoCancel(true)
            .setContentTitle("Title")
            .setContentText("message")
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.ic_baseline_notifications_active_24)
            .setTicker(resources.getString(R.string.app_name))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setSound(soundUri)
            .setContentIntent(openActivityPendingIntent)
            .addAction(R.drawable.ic_baseline_message_24,"Show message",toastPendingIntent)
            .color = Color.BLUE
        notificationManager.notify(idNotification, notificationBuilder.build())


    }

    fun textStyle(view: View) {
        val rand = Random()
        val idNotification = rand.nextInt(1000000000)

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationManager =  getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "Channel id", "Channel name", NotificationManager.IMPORTANCE_LOW
            )
            notificationChannel.description = "Channel description"
            notificationChannel.setSound(null, null)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notificationBuilder = NotificationCompat.Builder(this, "Channel id")

        val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.push_notifications)

        notificationBuilder
            .setAutoCancel(true)
            .setContentTitle("Title")
            .setContentText("message")
            .setLargeIcon(largeIcon)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(getString(R.string.long_dummy_text))
                    .setBigContentTitle("Big Content Title")
                    .setSummaryText("Summary Text") // in gmail notification this is be the email address
            )
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.ic_baseline_notifications_active_24)
            .setTicker(resources.getString(R.string.app_name))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setSound(soundUri)

        notificationManager.notify(idNotification, notificationBuilder.build())


    }

    fun textStyleInbox(view: View) {
        val rand = Random()
        val idNotification = rand.nextInt(1000000000)

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationManager =  getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "Channel id", "Channel name", NotificationManager.IMPORTANCE_LOW
            )
            notificationChannel.description = "Channel description"
            notificationChannel.setSound(null, null)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notificationBuilder = NotificationCompat.Builder(this, "Channel id")

        val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.push_notifications)

        notificationBuilder
            .setAutoCancel(true)
            .setContentTitle("Title")
            .setContentText("message")
            .setLargeIcon(largeIcon)
            .setStyle( NotificationCompat.InboxStyle()
                .addLine("This is line 1")
                .addLine("This is line 2")
                .addLine("This is line 3")
                .addLine("This is line 4")
                .addLine("This is line 5")
                .addLine("This is line 6")
                .addLine("This is line 7")
                .setBigContentTitle("Big Content Title")
                .setSummaryText("Summary Text"))
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.ic_baseline_notifications_active_24)
            .setTicker(resources.getString(R.string.app_name))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setSound(soundUri)

        notificationManager.notify(idNotification, notificationBuilder.build())


    }

    fun pictureStyle(view: View) {
        val rand = Random()
        val idNotification = rand.nextInt(1000000000)

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationManager =  getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "Channel id", "Channel name", NotificationManager.IMPORTANCE_LOW
            )
            notificationChannel.description = "Channel description"
            notificationChannel.setSound(null, null)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notificationBuilder = NotificationCompat.Builder(this, "Channel id")

        val picture  = BitmapFactory.decodeResource(resources, R.drawable.push_notifications)

         notificationBuilder
             .setAutoCancel(true)
             .setContentTitle("Title")
             .setContentText("message")
             .setLargeIcon(picture)
             .setStyle( NotificationCompat.BigPictureStyle()
                 .bigPicture(picture)
                 .bigLargeIcon(null)) // .bigLargeIcon(null) to hide pic when expand notification and all pic show
             .setWhen(System.currentTimeMillis())
             .setSmallIcon(R.drawable.ic_baseline_notifications_active_24)
             .setTicker(resources.getString(R.string.app_name))
             .setPriority(NotificationCompat.PRIORITY_HIGH)
             .setCategory(NotificationCompat.CATEGORY_MESSAGE)
             .setSound(soundUri)

         notificationManager.notify(idNotification, notificationBuilder.build())



    }

    fun play(view: View) {
        if (isPlaying){
            onTrackPause()
        } else {
            onTrackPlay()
        }
    }

    fun next(view: View) {
        position++
        showMediaNotification(
            tracks[position],
            R.drawable.ic_pause, position, tracks.size - 1
        )
        tvTrackName.text = tracks[position].title
    }

    fun previous(view: View) {
        position--
        showMediaNotification(
            tracks[position],
            R.drawable.ic_pause, position, tracks.size - 1
        )
        tvTrackName.text = tracks[position].title
    }

    override fun onTrackPrevious() {
        position--
        showMediaNotification(
            tracks[position],
            R.drawable.ic_pause, position, tracks.size - 1
        )
        tvTrackName.text = tracks[position].title

    }

    override fun onTrackPlay() {
        showMediaNotification(
            tracks[position],
            R.drawable.ic_pause, position, tracks.size - 1
        )
        tvTrackName.text = tracks[position].title
        play.setImageResource(R.drawable.ic_pause)
        isPlaying = true
    }

    override fun onTrackPause() {
        showMediaNotification(
            tracks[position],
            R.drawable.ic_play, position, tracks.size - 1
        )
        tvTrackName.text = tracks[position].title
        play.setImageResource(R.drawable.ic_play)
        isPlaying = false
    }

    override fun onTrackNext() {
        position++
        showMediaNotification(
            tracks[position],
            R.drawable.ic_pause, position, tracks.size - 1
        )
        tvTrackName.text = tracks[position].title
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(broadcastReceiver)
    }
}