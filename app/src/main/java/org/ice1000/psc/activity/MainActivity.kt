package org.ice1000.psc.activity

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.ice1000.psc.R
import org.jetbrains.anko.intentFor

class MainActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		play_list.setOnClickListener { startActivity(intentFor<PlayListsActivity>()) }
		all_songs.setOnClickListener { startActivity(intentFor<AllSongsActivity>()) }

		navigation.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
			when (item.itemId) {
				R.id.navigation_home -> {
					return@OnNavigationItemSelectedListener true
				}
				R.id.navigation_notifications -> {
					return@OnNavigationItemSelectedListener true
				}
			}
			false
		})
	}
}
