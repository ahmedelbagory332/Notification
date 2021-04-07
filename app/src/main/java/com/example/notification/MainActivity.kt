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
import android.media.AudioAttributes
import android.media.MediaMetadata
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
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
    companion object {
        var MESSAGES: MutableList<Message> = ArrayList()
        fun sendMessage(context: Context){
            var replyIntent: Intent? = null
            var replyPendingIntent: PendingIntent? = null
            val remoteInput: RemoteInput = RemoteInput.Builder("key_text_reply")
                    .setLabel("Your answer...")
                    .build()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                replyIntent = Intent(context, DirectReplyReceiver::class.java)
                replyPendingIntent = PendingIntent.getBroadcast(context,
                        0, replyIntent, 0)
            } else {
                //start chat activity instead (PendingIntent.getActivity)
                //cancel notification with notificationManagerCompat.cancel(id)
            }

            val replyAction: NotificationCompat.Action = NotificationCompat.Action.Builder(
                    R.drawable.ic_reply,
                    "Reply",
                    replyPendingIntent
            ).addRemoteInput(remoteInput).build()

            val me: Person = Person.Builder().setName("you").build()
         //   val conversationTitle: Person = Person.Builder().setName("Chat").build()

            val messagingStyle = NotificationCompat.MessagingStyle(me)
             messagingStyle.conversationTitle = "chat"
            for (chatMessage in MESSAGES) {
               val sender: Person = Person.Builder().setName("${chatMessage.sender}").build()

                val notificationMessage = NotificationCompat.MessagingStyle.Message(
                        chatMessage.text,
                        chatMessage.timestamp,
                        sender
                )
                messagingStyle.addMessage(notificationMessage)
            }

            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationManager =  context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel = NotificationChannel(
                        "Channel_id_sendMessage", "Channel_name_sendMessage", NotificationManager.IMPORTANCE_HIGH
                )
                val attributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                notificationChannel.description = "Channel_description_sendMessage"
                notificationChannel.enableLights(true)
                notificationChannel.enableVibration(true)
                notificationChannel.setSound(soundUri, attributes)
                notificationManager.createNotificationChannel(notificationChannel)
            }
            val notificationBuilder = NotificationCompat.Builder(context, "Channel_id_sendMessage")


            notificationBuilder.setAutoCancel(true)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.ic_baseline_message_24)
                     .setStyle(messagingStyle)
                    .addAction(replyAction)
                    .setColor(Color.BLUE)
                     .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setOnlyAlertOnce(true)

            notificationManager.notify(100, notificationBuilder.build())
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mediaSessionCompat = MediaSessionCompat(this, "tag")
        tvTrackName = findViewById(R.id.trackName)
        play = findViewById(R.id.play)
        tracks = mutableListOf()
        populateTracks()
        registerReceiver(broadcastReceiver, IntentFilter("TRACKS_TRACKS"))
        val me: Person = Person.Builder().setName("you").build()
        MESSAGES.add(Message("Hello", me.name))
        MESSAGES.add(Message("Good morning!", "ahmed"))
        MESSAGES.add(Message("Hello", me.name))
        MESSAGES.add(Message("Hi!", "ahmed"))
        MESSAGES.add(Message("where are u", me.name))

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
                    "Channel_id_music", "Channel_name_music", NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.description = "Channel_description_music"
            notificationChannel.setSound(null, null)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notificationBuilder = NotificationCompat.Builder(this, "Channel_id_music")

        pendingIntentPrevious = if (pos != 0) {
            val intentPrevious: Intent = Intent(this, NotificationMediaReceiver::class.java)
                    .setAction(actionPrevious)
            PendingIntent.getBroadcast(
                    this, 0,
                    intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT
            )

        }else{

            null
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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            mediaSessionCompat.setMetadata(
                    MediaMetadataCompat.Builder()
                            .putString(MediaMetadata.METADATA_KEY_TITLE, track.title)
                            .putString(MediaMetadata.METADATA_KEY_ARTIST, track.artist)
                            .build()
            )
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
            .setOnlyAlertOnce(true)
            .setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(1, 2, 3)
                            .setMediaSession(mediaSessionCompat.sessionToken)
            )
            .setSubText("Sub Text")
                .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .priority = NotificationCompat.PRIORITY_HIGH


        notificationManager.notify(1, notificationBuilder.build())
    }

    fun default(view: View) {
        val rand = Random()
        val idNotification = rand.nextInt(1000000000)

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationManager =  getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                    "Channel_id_default", "Channel_name_default", NotificationManager.IMPORTANCE_HIGH
            )
            val attributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()

            notificationChannel.description = "Channel_description_default"
            notificationChannel.enableLights(true)
            notificationChannel.enableVibration(true)
            notificationChannel.setSound(soundUri, attributes)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notificationBuilder = NotificationCompat.Builder(this, "Channel_id_default")


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
                    "Channel_id_action", "Channel_name_action", NotificationManager.IMPORTANCE_HIGH
            )
            val attributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
            notificationChannel.description = "Channel_description_action"
            notificationChannel.enableLights(true)
            notificationChannel.enableVibration(true)
            notificationChannel.setSound(soundUri, attributes)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notificationBuilder = NotificationCompat.Builder(this, "Channel_id_action")

    val openActivityIntent: Intent = Intent(this, MainActivity::class.java)
        val openActivityPendingIntent : PendingIntent  = PendingIntent.getActivity(
                this,
                0,
                openActivityIntent,
                0) // we put flag to 0 cause we don't need to any flag in this action

        val toastIntent: Intent = Intent(this, NotificationReceiver::class.java)
        toastIntent.putExtra("toastMessage", "message from notification")
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
            .addAction(R.drawable.ic_baseline_message_24, "Show message", toastPendingIntent)
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
                    "Channel_id_textStyle", "Channel_name_textStyle", NotificationManager.IMPORTANCE_HIGH
            )
            val attributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
            notificationChannel.description = "Channel_description_textStyle"
            notificationChannel.enableLights(true)
            notificationChannel.enableVibration(true)
            notificationChannel.setSound(soundUri, attributes)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notificationBuilder = NotificationCompat.Builder(this, "Channel_id_textStyle")

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
                    "Channel_id_textStyleInbox", "Channel_name_textStyleInbox"
                    , NotificationManager.IMPORTANCE_HIGH
            )
            val attributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
            notificationChannel.description = "Channel_description_textStyleInbox"
            notificationChannel.enableLights(true)
            notificationChannel.enableVibration(true)
            notificationChannel.setSound(soundUri, attributes)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notificationBuilder = NotificationCompat.Builder(this, "Channel_id_textStyleInbox")

        val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.push_notifications)

        notificationBuilder
            .setAutoCancel(true)
            .setContentTitle("Title")
            .setContentText("message")
            .setLargeIcon(largeIcon)
            .setStyle(NotificationCompat.InboxStyle()
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
                    "Channel_id_pictureStyle", "Channel_name_pictureStyle", NotificationManager.IMPORTANCE_HIGH
            )
            val attributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
            notificationChannel.description = "Channel_description_pictureStyle"
            notificationChannel.enableLights(true)
            notificationChannel.enableVibration(true)
            notificationChannel.setSound(soundUri, attributes)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notificationBuilder = NotificationCompat.Builder(this, "Channel_id_pictureStyle")

        val picture  = BitmapFactory.decodeResource(resources, R.drawable.push_notifications)

         notificationBuilder
             .setAutoCancel(true)
             .setContentTitle("Title")
             .setContentText("message")
             .setLargeIcon(picture)
             .setStyle(NotificationCompat.BigPictureStyle()
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

    fun message(view: View) {

        sendMessage(this)

    }

    fun progress(view: View) {
        val rand = Random()
        val  progressMax = 100

        val notificationManager =  getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "Channel_id_progress", "Channel_name_progress", NotificationManager.IMPORTANCE_LOW
            )


            notificationChannel.description = "Channel_description_progress"
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notificationBuilder = NotificationCompat.Builder(this, "Channel_id_progress")


        notificationBuilder.setAutoCancel(true)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.ic_baseline_notifications_active_24)
            .setTicker(resources.getString(R.string.app_name))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(false)
            .setContentTitle("Download")
            .setContentText("Download in progress")
            .setOnlyAlertOnce(true)
            .setProgress(progressMax, 0, true)

        notificationManager.notify(101, notificationBuilder.build())

        Thread(Runnable {
            SystemClock.sleep(2000)
            var progress = 0
            while (progress <= progressMax) {

                notificationBuilder.setProgress(progressMax, progress, false)
                    .setAutoCancel(false)
                notificationManager.notify(101, notificationBuilder.build())
                SystemClock.sleep(1000)
                progress += 20
            }
            notificationBuilder.setContentText("Download finished")
                .setProgress(0, 0, false)
                .setAutoCancel(true)
            notificationManager.notify(101, notificationBuilder.build())
        }).start()

    }

    fun check(view: View) {
        val rand = Random()
        val idNotification = rand.nextInt(1000000000)

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationManager =  getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        @RequiresApi(Build.VERSION_CODES.N)
        if (!notificationManager.areNotificationsEnabled()) {
            openNotificationSettings()
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            isChannelBlocked("Channel_id_check")) {
            openChannelSettings("Channel_id_check")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "Channel_id_check", "Channel_name_check", NotificationManager.IMPORTANCE_HIGH
            )
            val attributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            notificationChannel.description = "Channel_description_check"
            notificationChannel.enableLights(true)
            notificationChannel.enableVibration(true)
            notificationChannel.setSound(soundUri, attributes)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notificationBuilder = NotificationCompat.Builder(this, "Channel_id_check")


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





    fun play(view: View) {
        if (isPlaying){
            onTrackPause()
        } else {
            onTrackPlay()
        }
    }

    fun next(view: View) {
        position++
        if ( position < tracks.size ){
            showMediaNotification(
                    tracks[position],
                    R.drawable.ic_pause, position, tracks.size - 1
            )
            tvTrackName.text = tracks[position].title
        }

    }

    fun previous(view: View) {
        position--
        if (position!=0 || position>0){
            showMediaNotification(
                    tracks[position],
                    R.drawable.ic_pause, position, tracks.size - 1
            )
            tvTrackName.text = tracks[position].title
        }else{
            // play the first track again
        }

    }


    private fun openNotificationSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            startActivity(intent)
        } else {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }

    @RequiresApi(26)
    private fun isChannelBlocked(channelId: String): Boolean {
        val manager =
            getSystemService(NotificationManager::class.java)
        val channel = manager.getNotificationChannel(channelId)
        return channel != null &&
                channel.importance == NotificationManager.IMPORTANCE_NONE
    }

    @RequiresApi(26)
    private fun openChannelSettings(channelId: String) {
        val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        intent.putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
        startActivity(intent)
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