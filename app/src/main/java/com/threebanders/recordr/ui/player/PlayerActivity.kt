package com.threebanders.recordr.ui.player

import android.content.res.Configuration
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.chibde.visualizer.LineBarVisualizer
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.sdsmdg.harjot.crollerTest.Croller
import com.threebanders.recordr.R
import com.threebanders.recordr.ui.BaseActivity
import com.threebanders.recordr.viewmodels.contact_details.ContactDetailsExtra.RECORDING_EXTRA
import core.threebanders.recordr.CoreUtil
import core.threebanders.recordr.data.Recording
import core.threebanders.recordr.player.AudioPlayer
import core.threebanders.recordr.player.PlaybackListenerInterface
import core.threebanders.recordr.player.PlayerAdapter

class PlayerActivity : BaseActivity() {
    var player: AudioPlayer? = null
    var recording: Recording? = null
    var playPause: ImageButton? = null
    var resetPlaying: ImageButton? = null
    private var happy: ImageButton? = null
    private var sad: ImageButton? = null
    lateinit var recordingInfo: TextView
    var playSeekBar: SeekBar? = null
    var playedTime: TextView? = null
    var totalTime: TextView? = null
    var userIsSeeking = false
    private var visualizer: LineBarVisualizer? = null
    private var audioManager: AudioManager? = null
    private var phoneVolume = 0
    lateinit var gainControl: Croller
    lateinit var volumeControl: Croller

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    public override fun createFragment(): Fragment? {
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.player_activity)

        firebaseAnalytics = Firebase.analytics

        val toolbar = findViewById<Toolbar>(R.id.toolbar_player)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true)
            actionBar.setTitle(R.string.player_title)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        recording = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra(RECORDING_EXTRA, Recording::class.java)
        } else {
            intent.getParcelableExtra(RECORDING_EXTRA)
        }

        visualizer = findViewById(R.id.visualizer)
        visualizer?.setColor(ContextCompat.getColor(this, R.color.colorAccentLighter))
        visualizer?.setDensity(
            if (resources.configuration.orientation ==
                Configuration.ORIENTATION_PORTRAIT
            ) DENSITY_PORTRAIT else DENSITY_LANDSCAPE.toFloat()
        )

        try {
            visualizer?.setPlayer(AUDIO_SESSION_ID)
        } catch (exc: Exception) {
            visualizer = null
        }
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        playPause = findViewById(R.id.test_player_play_pause)
        resetPlaying = findViewById(R.id.test_player_reset)
        playSeekBar = findViewById(R.id.play_seekbar)
        playedTime = findViewById(R.id.test_play_time_played)
        totalTime = findViewById(R.id.test_play_total_time)
        playPause?.setOnClickListener {
            if (player!!.playerState == PlayerAdapter.State.PLAYING) {
                player!!.pause()
                playPause?.background = ContextCompat.getDrawable(this, R.drawable.player_play)
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else if (player!!.playerState == PlayerAdapter.State.PAUSED ||
                player!!.playerState == PlayerAdapter.State.INITIALIZED
            ) {
                player!!.play()
                playPause?.background = ContextCompat.getDrawable(this, R.drawable.player_pause)
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
        resetPlaying?.setOnClickListener {
            if (player!!.playerState == PlayerAdapter.State.PLAYING) playPause?.background =
                ContextCompat.getDrawable(this, R.drawable.player_play)
            player!!.reset()
        }
        playSeekBar?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            var userSelectedPosition = 0
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) userSelectedPosition = progress
                playedTime?.text = CoreUtil.getDurationHuman(progress.toLong(), false)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                userIsSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                userIsSeeking = false
                player!!.seekTo(userSelectedPosition)
            }
        })
        gainControl = findViewById(R.id.gain_control)
        player?.setGain(25.0F)
        gainControl.progress = 25
        gainControl.setOnProgressChangedListener { progress: Int -> player!!.setGain(progress.toFloat()) }
        volumeControl = findViewById(R.id.volume_control)
        if (audioManager != null) {
            volumeControl.setMax(audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
            phoneVolume = audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
            volumeControl.progress = phoneVolume
        }
        volumeControl.setOnProgressChangedListener { progress: Int ->
            audioManager!!.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                progress,
                0
            )
        }
        recordingInfo = findViewById(R.id.recording_info)
        recordingInfo.text = String.format(
            resources.getString(R.string.recording_info),
            recording!!.name, recording!!.humanReadingFormat
        )

        happy = findViewById(R.id.happy)
        happy!!.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.SCORE, "1")

            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.POST_SCORE, bundle)
        }

        sad = findViewById(R.id.sad)
        sad!!.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.SCORE, "-1")

            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.POST_SCORE, bundle)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (visualizer != null) {
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) visualizer!!.setDensity(
                DENSITY_LANDSCAPE.toFloat()
            ) else visualizer!!.setDensity(DENSITY_PORTRAIT)
        }
    }

    override fun onStart() {
        super.onStart()

        player = AudioPlayer(PlaybackListener())
        playedTime!!.text = getString(R.string.time_zero)
        if (!player!!.loadMedia(recording!!.path)) return
        totalTime!!.text = CoreUtil.getDurationHuman(player!!.totalDuration.toLong(), false)
        player!!.setGain(gainControl.progress.toFloat())

        val pref = prefs
        val currentPosition = pref.getInt(CURRENT_POS, 0)
        val isPlaying = pref.getBoolean(IS_PLAYING, true)
        if (!player!!.setMediaPosition(currentPosition)) {
            return
        }
        if (isPlaying) {
            playPause!!.background = ContextCompat.getDrawable(this, R.drawable.player_pause)
            player!!.play()
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            playPause!!.background = ContextCompat.getDrawable(this, R.drawable.player_play)
            player!!.playerState = PlayerAdapter.State.PAUSED
        }
    }

    override fun onStop() {
        super.onStop()
        val pref = prefs
        val editor = pref.edit()
        editor.putInt(CURRENT_POS, player!!.currentPosition)
        editor.putBoolean(IS_PLAYING, player!!.playerState == PlayerAdapter.State.PLAYING)
        editor.apply()
        player!!.stopPlayer()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onDestroy() {
        super.onDestroy()
        val pref = prefs
        val editor = pref.edit()
        editor.remove(IS_PLAYING)
        editor.remove(CURRENT_POS)
        editor.apply()
        if (visualizer != null) visualizer!!.release()
        if (audioManager != null) audioManager!!.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            phoneVolume,
            0
        )
    }

    internal inner class PlaybackListener : PlaybackListenerInterface {
        override fun onDurationChanged(duration: Int) {
            playSeekBar!!.max = duration
        }

        override fun onPositionChanged(position: Int) {
            if (!userIsSeeking) {
                if (Build.VERSION.SDK_INT >= 24) playSeekBar!!.setProgress(
                    position,
                    true
                ) else playSeekBar!!.progress = position
            }
        }

        override fun onPlaybackCompleted() {
            playPause!!.post {
                playPause!!.background =
                    ContextCompat.getDrawable(this@PlayerActivity, R.drawable.player_play)
            }
            player!!.reset()
        }

        override fun onError() {
            playPause!!.background =
                ContextCompat.getDrawable(this@PlayerActivity, R.drawable.player_play)
            playPause!!.isEnabled = false
            resetPlaying!!.isEnabled = false
            totalTime!!.text = getString(R.string.time_zero)
            playSeekBar!!.isEnabled = false
            recordingInfo.text = resources.getString(R.string.player_error)
            recordingInfo.setTextColor(ContextCompat.getColor(this@PlayerActivity, R.color.red))
            volumeControl.isEnabled = false
            gainControl.isEnabled = false
        }

        override fun onReset() {
            player = AudioPlayer(PlaybackListener())
            if (player!!.loadMedia(recording!!.path)) player!!.setGain(gainControl.progress.toFloat())
        }
    }

    companion object {
        const val AUDIO_SESSION_ID = 0
        const val IS_PLAYING = "is_playing"
        const val CURRENT_POS = "current_pos"
        const val DENSITY_PORTRAIT = 70f
        const val DENSITY_LANDSCAPE = 150
    }
}