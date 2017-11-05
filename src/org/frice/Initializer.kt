package org.frice

import javafx.application.Application
import org.frice.platform.FriceGame
import org.frice.utils.message.FLog
import org.frice.utils.misc.loop
import org.frice.utils.time.FTimeListener
import java.awt.Rectangle
import java.util.*
import javax.swing.UIManager
import javax.swing.WindowConstants
import kotlin.concurrent.thread

object Initializer {
	init {
		if ("Windows" in System.getProperty("os.name"))
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel")
		else UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel")
	}

	@JvmField
	val TO_X = 100
	@JvmField
	val TO_Y = 100

	@JvmField
	val SMALL_PHONE = Rectangle(TO_X, TO_Y, 480, 800)
	@JvmField
	val BIG_PHONE = Rectangle(TO_X, TO_Y, 720, 1200)
	@JvmField
	val HUGE_PHONE = Rectangle(TO_X, TO_Y, 1080, 1920)

	@JvmField
	val SMALL_SQUARE = Rectangle(TO_X, TO_Y, 400, 400)
	@JvmField
	val BIG_SQUARE = Rectangle(TO_X, TO_Y, 800, 800)

	@JvmStatic
	fun Rectangle.rotate() {
		width -= -height
		height -= width
		width += height
	}

	@JvmStatic
	fun launch(game: GameFX) = Application.launch(game.javaClass)

	@JvmStatic
	fun launch(game: Game) {
		game.defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
		FLog.v("Engine start!")
		game.run {
			onLastInit()
			thread {
				loop {
					try {
						onRefresh()
						timeListeners.forEach(FTimeListener::check)
						// only update per "refreshTime"
						if (!paused && !stopped && refresh.ended()) {
							panel.repaint()
							++fpsCounter
							if (fpsTimer.ended()) {
								fpsDisplay = fpsCounter
								fpsCounter = 0
							}
						}
					} catch (ignored: ConcurrentModificationException) {
					}
				}
			}
		}
		FLog.v("Engine thread successfully created.")
	}

	@JvmStatic
	fun launch(c: Class<out FriceGame>) = c.newInstance().let { instance ->
		when (instance) {
			is Game -> launch(instance)
			is GameFX -> launch(instance)
			else -> throw IllegalArgumentException("You should launch an instance of Game or GameFX!")
		}
	}
}