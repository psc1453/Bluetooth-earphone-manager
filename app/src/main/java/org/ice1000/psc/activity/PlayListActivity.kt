package org.ice1000.psc.activity

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.media.session.MediaSessionCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import kotlinx.android.synthetic.main.activity_play_list.*
import org.ice1000.psc.R
import org.ice1000.psc.songsFile
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onLongClick
import java.io.File
import java.util.*

/**
 * @author ice1000
 * @property songs MutableList<File>
 * @property playList File
 * @property allLists ArrayList<String>
 * @property mostRecentSong File
 * @property currentSong File
 * @property nextList String
 * @property lastList String
 * @property mostRecentClick Boolean
 * @property mostRecentClickTime Long
 * @property mediaPlayers MutableList<MediaPlayer>
 * @property mediaSession MediaSessionCompat
 * @property defaultValueHolder Button
 */
class PlayListActivity : AppCompatActivity() {
	companion object {
		const val circle1 = 127
		const val circle2 = 126
		const val upLong = 87
		const val downLong = 88
		val all = listOf(circle1, circle2, upLong, downLong)
	}

	private lateinit var songs: MutableList<File>
	private lateinit var playList: File
	private lateinit var allLists: ArrayList<String>
	private lateinit var mostRecentSong: File
	private lateinit var currentSong: File
	private lateinit var nextList: String
	private lateinit var lastList: String
	private var mostRecentClick: Boolean = false
	private var mostRecentClickTime: Long = System.currentTimeMillis()
	private val mediaPlayers = mutableListOf<MediaPlayer>()
	private lateinit var mediaSession: MediaSessionCompat
	private lateinit var defaultValueHolder: Button
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_play_list)
		defaultValueHolder = Button(this)
		val list = intent.getStringExtra("list")
		allLists = intent.getStringArrayListExtra("all_song")
		title = list
		val indexOf = allLists.indexOf(list)
		val nextIndex = (indexOf + 1 + allLists.size) % allLists.size
		val lastIndex = (indexOf - 1 + allLists.size) % allLists.size
		nextList = allLists[nextIndex]
		lastList = allLists[lastIndex]
		playList = getExternalFilesDir(null).resolve(list)
		songs = playList.readLines().filter { it.isNotBlank() }.map(songsFile::resolve).toMutableList()
		songs.forEach { song -> current_play_list.addView(initButton(song)) }
		mediaSession = MediaSessionCompat(this, "WTF").apply {
			isActive = true
			setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS)
			setMediaButtonReceiver(null)
			setCallback(object : MediaSessionCompat.Callback() {
				private var boolean = false
				override fun onMediaButtonEvent(mediaButtonEvent: Intent): Boolean {
					boolean = !boolean
					if (boolean) return true
					val currentTimeMillis = System.currentTimeMillis()
					val delta = currentTimeMillis - mostRecentClickTime
					val hasDouble = delta < 8000 && mostRecentClick
//		toast("(delta < 8000).toString() = ${(delta < 8000)}, mostRecentClick = $mostRecentClick")
					val ret: Boolean
					val keyCode = mediaButtonEvent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT).keyCode
					when (keyCode) {
						circle1, circle2 -> {
							if (hasDouble) mediaPlayers.lastOrNull()?.run { if (isPlaying) pause() else start() }
							ret = true
							mostRecentClick = true
						}
						upLong -> {
							fuck()
							if (hasDouble) startActivity(intentFor<PlayListActivity>(
									"list" to lastList, "all_song" to allLists, "media_switched" to true))
							else play(firstOrNull = mostRecentSong)
							ret = true
							mostRecentClick = false
						}
						downLong -> {
							fuck()
							if (hasDouble) startActivity(intentFor<PlayListActivity>(
									"list" to nextList, "all_song" to allLists, "media_switched" to true))
							else {
								val random = Random()
								var newSong = songs[random.nextInt(songs.size)]
								while (newSong == currentSong) newSong = songs[random.nextInt(songs.size)]
								play(firstOrNull = newSong)
							}
							ret = true
							mostRecentClick = false
						}
						else -> {
							mostRecentClick = false
							ret = super.onMediaButtonEvent(mediaButtonEvent)
						}
					}
					mostRecentClickTime = currentTimeMillis
					return ret
				}
			})
		}
		if (intent.getBooleanExtra("media_switched", false)) play()
	}

	private fun play(files: List<File> = songs,
									 random: Random = Random(),
									 firstOrNull: File = files[random.nextInt(files.size)]) {
		for (i in 0 until current_play_list.childCount) {
			val childAt = current_play_list.getChildAt(i) as? Button ?: continue
			if (childAt.text == firstOrNull.name) childAt.height = 200
		}
		if (!::mostRecentSong.isInitialized) mostRecentSong = firstOrNull
		currentSong = firstOrNull
		MediaPlayer.create(this, Uri.fromFile(firstOrNull)).apply {
			isLooping = false
			start()
			mediaPlayers.add(this)
			setOnCompletionListener {
				for (i in 0 until current_play_list.childCount) {
					val childAt = current_play_list.getChildAt(i) as? Button ?: continue
					if (childAt.text == firstOrNull.name) childAt.height = defaultValueHolder.height
				}
				mostRecentSong = firstOrNull
				var newSong = files[random.nextInt(files.size)]
				while (newSong == firstOrNull) newSong = files[random.nextInt(files.size)]
				play(files, random, newSong)
			}
		}
	}

	private fun initButton(song: File): View {
		val button = Button(this)
		button.text = song.name
		button.onClick {
			play(firstOrNull = songsFile.resolve(song))
		}
		button.onLongClick {
			Snackbar.make(button, "Delete?", Snackbar.LENGTH_LONG).setAction("Sure!") {
				val index = songs.indexOf(song)
				songs.removeAt(index)
				current_play_list.removeViewAt(index)
			}.show()
		}
		return button
	}

	override fun onKeyDown(keyCode: Int, event: KeyEvent?) =
			(keyCode in all) || super.onKeyDown(keyCode, event)

	override fun onDestroy() {
		super.onDestroy()
		fuck()
	}

	private fun fuck() {
		mediaPlayers.forEach {
			it.stop()
			it.release()
		}
		mediaPlayers.clear()
		for (i in 0 until current_play_list.childCount) {
			val childAt = current_play_list.getChildAt(i) as? Button ?: continue
			childAt.height = defaultValueHolder.height
		}
	}

	override fun onPause() {
		super.onPause()
		playList.writeText(songs.joinToString(System.lineSeparator()))
	}
}
