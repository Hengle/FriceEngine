package org.frice.game

import org.frice.game.event.OnFrameClickEvent
import org.frice.game.event.OnFrameMouseEvent
import org.frice.game.resource.FileImageResource
import org.frice.game.spirit.ImageObject
import org.frice.utils.log.Log
import org.frice.utils.time.Timer
import java.awt.Color
import java.awt.Rectangle
import java.io.File
import java.util.*

/**
 * Created by ice1000 on 2016/8/13.
 * @author ice1000
 * @since v0.1
 */
class Demo : Game() {

	val dickTimer = Timer(1000)
	var fuck = 0
	val objList = ArrayList<ImageObject>()
	var mode = 0

	override fun onInit() {
		bounds = Rectangle(100, 100, 640, 480)
		title = "Demo of Frice"
		backgroundColor = Color.BLUE
	}

	override fun onMouse(e: OnFrameMouseEvent) {
	}

	override fun onRefresh() {
		if (dickTimer.ended()) {
			val texture = FileImageResource("tres" + File.separator + "display.png")
			val obj: ImageObject
			if (fuck > 300) mode = 1 else if (fuck < 1) mode = 0
			when (mode) {
				0 -> {
					obj = ImageObject(texture, -1, fuck, fuck)
					objList.add(obj)
					addObject(obj)
					fuck += 100
				}
				1 -> {
					obj = objList[objList.size - 1]
					objList.remove(obj)
					removeObject(obj)
					fuck -= 100
				}
			}
			Log.v("objList.size = ${objList.size}")
		}
	}

	override fun onExit() {
	}

	override fun onClick(e: OnFrameClickEvent) {
	}

}