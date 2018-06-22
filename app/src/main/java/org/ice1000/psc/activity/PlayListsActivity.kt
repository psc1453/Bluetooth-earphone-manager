package org.ice1000.psc.activity

import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout.VERTICAL
import kotlinx.android.synthetic.main.activity_play_lists.*
import kotlinx.android.synthetic.main.content_play_lists.*
import org.ice1000.psc.R
import org.ice1000.psc.swap
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onLongClick
import java.io.File

class PlayListsActivity : AppCompatActivity() {
	companion object {
		const val FILE_NAME = "play_lists.txt"
	}

	private lateinit var playListsFile: File
	lateinit var playLists: ArrayList<String>

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_play_lists)
		setSupportActionBar(toolbar)
		playListsFile = getExternalFilesDir(null).resolve(FILE_NAME)
		playLists = if (!playListsFile.exists()) {
			playListsFile.createNewFile()
			arrayListOf()
		} else ArrayList(playListsFile.readLines())
		refreshPlayLists()

		add_fab.setOnClickListener {
			alert("Play list name?") {
				var edit: EditText? = null
				customView {
					edit = editText()
				}
				yesButton {
					val text = (edit ?: return@yesButton).text.toString()
					toast("Created play list: $text")
					if (checkDuplicate(text)) return@yesButton
					playLists.add(text)
					play_list_list.addView(initPlayList(text))
				}
				noButton { }
			}.show()
		}
		supportActionBar?.setDisplayHomeAsUpEnabled(true)
	}

	private fun initPlayList(text: String): Button {
		val button = Button(this)
		button.text = text
		button.onClick {
			startActivity(intentFor<PlayListActivity>(
					"list" to text, "all_song" to playLists, "media_switched" to false))
		}
		button.onLongClick {
			lateinit var dialogInterface: DialogInterface
			dialogInterface = alert {
				title = "What to do?"
				customView {
					linearLayout {
						orientation = VERTICAL
						button("Move Up").onClick {
							val index = playLists.indexOf(text)
							playLists.swap(index, index - 1)
							refreshPlayLists()
							dialogInterface.cancel()
						}
						button("Move Down").onClick {
							val index = playLists.indexOf(text)
							playLists.swap(index, index + 1)
							refreshPlayLists()
							dialogInterface.cancel()
						}
						button("Delete").onClick {
							val index = playLists.indexOf(text)
							playLists.removeAt(index)
							play_list_list.removeViewAt(index)
							dialogInterface.cancel()
						}
						val rename = button("Rename to:")
						val renameText = editText()
						rename.onClick {
							val indexOf = playLists.indexOf(text)
							val newText = renameText.text.toString()
							if (checkDuplicate(newText)) return@onClick
							playLists[indexOf] = newText
							(play_list_list.getChildAt(indexOf) as? Button)?.text = newText
							dialogInterface.cancel()
						}
					}
				}
			}.show()
		}
		return button
	}

	private fun refreshPlayLists() {
		play_list_list.removeAllViews()
		playLists.forEach { playList ->
			play_list_list.addView(initPlayList(playList))
		}
	}

	private fun checkDuplicate(newText: String): Boolean {
		if (playLists.any { it == newText }) {
			toast("Duplicate name $newText")
			return true
		}
		return false
	}

	override fun onPause() {
		super.onPause()
		playListsFile.writeText(playLists.joinToString(System.lineSeparator()))
		val externalFilesDir = getExternalFilesDir(null)
		playLists.forEach {
			externalFilesDir.resolve(it).run {
				if (!exists()) createNewFile()
			}
		}
	}
}
