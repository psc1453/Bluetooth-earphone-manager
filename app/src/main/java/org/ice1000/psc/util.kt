package org.ice1000.psc

import java.io.File

fun <T> MutableList<T>.swap(index1: Int, index2: Int) {
	val tmp = this[index1]
	this[index1] = this[index2]
	this[index2] = tmp
}

val songsFile = File("/storage/emulated/0/cycling")
