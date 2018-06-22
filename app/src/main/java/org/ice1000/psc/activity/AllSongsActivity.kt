package org.ice1000.psc.activity

import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.CheckBox
import android.widget.LinearLayout.VERTICAL
import kotlinx.android.synthetic.main.activity_all_songs.*
import kotlinx.android.synthetic.main.content_all_songs.*
import org.ice1000.psc.R
import org.ice1000.psc.songsFile
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.io.File

/**
 * @author ice1000
 * @property checkBoxes MutableList<CheckBox>
 */
class AllSongsActivity : AppCompatActivity() {
	private val checkBoxes = mutableListOf<Pair<CheckBox, String>>()
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_all_songs)
		setSupportActionBar(toolbar)
		for (song in songsFile.listFiles { f: File -> !f.isDirectory })
			all_songs.addView(initCheckBox(song.name))
		val root = getExternalFilesDir(null)
		val lines = root.resolve(PlayListsActivity.FILE_NAME)
				.readLines()
		add_song_fab.setOnClickListener {
			lateinit var dialogInterface: DialogInterface
			val selected = checkBoxes
					.filter { (it, _) -> it.isChecked }
			dialogInterface = alert {
				title = "Add to..."
				customView {
					scrollView {
						linearLayout {
							orientation = VERTICAL
							lines.forEach { playList ->
								button(playList) {
									onClick {
										val resolve = root.resolve(playList)
										val readText = resolve.readText()
										resolve.writeText(selected
												.joinToString(System.lineSeparator(), prefix = readText + System.lineSeparator()) { it.second })
										dialogInterface.cancel()
									}
								}
							}
						}
					}
				}
			}.show()
		}
	}

	private fun initCheckBox(text: String): CheckBox {
		val checkBox = CheckBox(this)
		checkBox.text = text
		checkBoxes.add(checkBox to text)
		return checkBox
	}
}
