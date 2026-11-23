package com.lexi.christmastown

import android.animation.ValueAnimator
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool

/** Sound effects: short, played instantly through a SoundPool. */
enum class Sfx(val res: Int, val volume: Float) {
    Tap(R.raw.sfx_tap, 0.35f),
    Coins(R.raw.sfx_coins, 0.9f),
    LevelUp(R.raw.sfx_level_up, 0.9f),
    Ding(R.raw.sfx_cookie_ding, 0.8f),
    SleighBells(R.raw.sfx_sleigh_bells, 0.8f),
    Pop(R.raw.sfx_toy_pop, 0.7f),
    Message(R.raw.sfx_chat_message, 0.6f),
    PresentDrop(R.raw.sfx_present_drop, 0.8f),
    Munch(R.raw.sfx_reindeer_munch, 0.8f),
    Oops(R.raw.sfx_oops, 0.6f),
}

/** Background music: one song loops at a time and fades into the next. */
enum class Music(val res: Int) {
    JingleBells(R.raw.music_jingle_bells),
    DeckTheHalls(R.raw.music_deck_the_halls),
    WeWish(R.raw.music_we_wish),
    SilentNight(R.raw.music_silent_night),
    SleighRide(R.raw.music_sleigh_ride),
    JoyToTheWorld(R.raw.music_joy_to_the_world),
    OChristmasTree(R.raw.music_o_christmas_tree),          // Snow Park (the Town Tree)
    UpOnTheHousetop(R.raw.music_up_on_the_housetop),       // sleigh flight
    JollyOldStNicholas(R.raw.music_jolly_old_st_nicholas), // Pets
    GoodKingWenceslas(R.raw.music_good_king_wenceslas),    // snowball fight
    TwelveDays(R.raw.music_twelve_days),                   // wrapping presents
    HappyBirthday(R.raw.music_happy_birthday),             // your birthday party
}

object Audio {
    private const val MUSIC_VOLUME = 0.45f

    private lateinit var app: Context
    private var pool: SoundPool? = null
    private val loaded = HashMap<Sfx, Int>()
    private var player: MediaPlayer? = null
    private var current: Music? = null
    private var wanted: Music? = null
    private var fade: ValueAnimator? = null
    private var inForeground = true

    var musicOn = true
        set(v) { field = v; if (v) wanted?.let { current = null; music(it) } else stopMusic() }
    var soundOn = true

    fun init(context: Context) {
        if (pool != null) return
        app = context.applicationContext
        val attrs = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
        pool = SoundPool.Builder().setMaxStreams(6).setAudioAttributes(attrs).build().also { p ->
            Sfx.entries.forEach { loaded[it] = p.load(app, it.res, 1) }
        }
    }

    fun play(s: Sfx) {
        if (!soundOn || !inForeground) return
        val id = loaded[s] ?: return
        pool?.play(id, s.volume, s.volume, 1, 0, 1f)
    }

    /** Switch to [m]: the old song fades out, the new one fades in. */
    fun music(m: Music) {
        wanted = m
        if (!musicOn || !inForeground || m == current) return
        current = m
        fadeTo(0f, 350) {
            player?.release()
            player = MediaPlayer.create(app, m.res)?.apply {
                isLooping = true
                setVolume(0f, 0f)
                start()
            }
            fadeTo(MUSIC_VOLUME, 700) {}
        }
    }

    private fun fadeTo(target: Float, millis: Long, then: () -> Unit) {
        fade?.cancel()
        val p = player
        if (p == null) { then(); return }
        val start = runCatching { if (target == 0f) MUSIC_VOLUME else 0f }.getOrDefault(0f)
        fade = ValueAnimator.ofFloat(start, target).apply {
            duration = millis
            addUpdateListener { a -> val v = a.animatedValue as Float; runCatching { p.setVolume(v, v) } }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                private var cancelled = false
                override fun onAnimationCancel(animation: android.animation.Animator) { cancelled = true }
                override fun onAnimationEnd(animation: android.animation.Animator) { if (!cancelled) then() }
            })
            start()
        }
    }

    private fun stopMusic() {
        fade?.cancel()
        player?.release()
        player = null
        current = null
    }

    /** The app went to the background (home button, phone call...): go quiet. */
    fun onPause() {
        inForeground = false
        fade?.cancel()
        player?.pause()
    }

    fun onResume() {
        inForeground = true
        if (!musicOn) return
        val p = player
        if (p != null) { p.setVolume(MUSIC_VOLUME, MUSIC_VOLUME); p.start() } else wanted?.let { current = null; music(it) }
    }
}
