@file:Suppress("unused")

package org.frice

import org.frice.event.*
import org.frice.obj.AbstractObject
import org.frice.obj.button.FText
import org.frice.platform.*
import org.frice.platform.adapter.JvmDrawer
import org.frice.platform.adapter.JvmImage
import org.frice.resource.graphics.ColorResource
import org.frice.util.EventManager
import org.frice.util.cast
import org.frice.util.message.*
import org.frice.util.shape.FRectangle
import org.frice.util.shape.FShapeQuad
import org.frice.util.time.*
import java.awt.*
import java.awt.event.*
import java.util.function.Consumer
import javax.imageio.ImageIO.read
import javax.swing.*

/**
 * The base game class using Swing as renderer.
 * This class does rendering jobs, and something which are
 * invisible to game developers.
 *
 * Feel free to override the constructor, but please remember to
 * run super().
 *
 * Created by ice1000 on 2016/8/15.
 * @author ice1000
 * @since v0.2.3
 */
@Suppress("LeakingThis")
open class Game @JvmOverloads constructor(layerCount: Int = 1) : JFrame(), FriceGame {
	override val layers = Array(layerCount) { Layer() }
	override val eventManager = EventManager()
	override var activeArea: FShapeQuad? = null

	override var paused = false
		set(value) {
			if (value) FClock.pause() else FClock.resume()
			field = value
		}

	override var stopped = false
		set(value) {
			if (value) FClock.pause() else FClock.resume()
			field = value
		}

	override fun dialogConfirmYesNo(msg: String, title: String) =
		JOptionPane.showConfirmDialog(this, msg, title, YES_NO_OPTION) == YES_OPTION

	override fun dialogShow(msg: String, title: String) =
		JOptionPane.showMessageDialog(this, msg, title, OK_OPTION)

	override fun dialogInput(msg: String, title: String): String =
		JOptionPane.showInputDialog(this, msg, title)

	override var debug = true
	override var showFPS = true

	final override var loseFocus = false

	override var loseFocusChangeColor = true

	override var isFullScreen: Boolean
		get() = extendedState == Frame.MAXIMIZED_BOTH
		set(value) {
			if (value) extendedState = Frame.MAXIMIZED_BOTH
		}

	override var isAlwaysTop: Boolean
		get() = isAlwaysOnTop
		set(value) {
			if (isAlwaysOnTopSupported) isAlwaysOnTop = value
		}

	@get:JvmName(" refresh")
	internal val refresh = FTimer(4)
	override var millisToRefresh: Int
		get () = refresh.time
		set (value) {
			refresh.time = value
		}

	internal val panel: SwingGamePanel = SwingGamePanel()

	private val drawer: JvmDrawer

	val fpsCounter = FpsCounter()

	/** represent the mouse as an object */
	@JvmField
	val mouse = object : AbstractObject {
		override var x: Double
			get() = panel.mousePosition?.getX() ?: -1.0
			set(value) {
				panel.mousePosition?.setLocation(value, y)
			}

		override var y: Double
			get() = panel.mousePosition?.getY() ?: -1.0
			set(value) {
				panel.mousePosition?.setLocation(x, value)
			}

		override var rotate = 0.0
		override var died = false
		override var isVisible = false
		override fun toString() = "($x, $y)"
	}

	init {
		isResizable = false
		layout = BorderLayout()
		// set icon
		iconImage = read(javaClass.getResourceAsStream("/icon.png"))

		/// to prevent this engine from the call#cking NPE!!
		add(panel, BorderLayout.CENTER)
		bounds = BIG_SQUARE
		onInit()
		drawer = JvmDrawer(this).also(JvmDrawer::init)
		isVisible = true
		FLog.v("If the window doesn't appear, please call `launch(YourGameClass.class)` instead of the constructor.")
	}

	open fun onExit() {
		if (dialogConfirmYesNo("Are you sure to exit?")) {
			dispose()
			System.exit(0)
		}
	}

	fun putBottom(component: JComponent) = add(component, BorderLayout.SOUTH)
	fun putTop(component: JComponent) = add(component, BorderLayout.NORTH)

	override fun measureText(text: FText): FRectangle {
		drawer.useFont(text)
		val g = drawer.g
		val font = text.`font tmp obj` as? Font ?: g.font
		return FRectangle(font.getStringBounds(text.text, g.fontRenderContext))
	}

	override fun measureTextWidth(text: FText): Int {
		drawer.useFont(text)
		val g = drawer.g
		val font = text.`font tmp obj` as? Font ?: g.font
		return g.getFontMetrics(font).stringWidth(text.text)
	}

	/**
	 * add keyboard listeners with lambda
	 */
	fun addKeyListener(
		typed: Consumer<KeyEvent>? = null,
		pressed: Consumer<KeyEvent>? = null,
		released: Consumer<KeyEvent>? = null) {
		addKeyListener(object : KeyListener {
			override fun keyPressed(e: KeyEvent) {
				pressed?.accept(e)
			}

			override fun keyReleased(e: KeyEvent) {
				released?.accept(e)
			}

			override fun keyTyped(e: KeyEvent) {
				typed?.accept(e)
			}
		})
	}

	override val screenCut get() = drawer.friceImage

	fun addKeyTypedEvent(
		keyCode: Int,
		key: Consumer<KeyEvent>) = addKeyListener(typed = Consumer { e -> if (e.keyCode == keyCode) key.accept(e) })

	fun addKeyPressedEvent(
		keyCode: Int,
		key: Consumer<KeyEvent>) = addKeyListener(pressed = Consumer { e -> if (e.keyCode == keyCode) key.accept(e) })

	fun addKeyReleasedEvent(
		keyCode: Int,
		key: Consumer<KeyEvent>) = addKeyListener(released = Consumer { e -> if (e.keyCode == keyCode) key.accept(e) })

	override fun setCursor(o: FriceImage) {
		cursor = toolkit.createCustomCursor(cast<JvmImage>(o).image, Point(0, 0), "cursor")
	}

	/**
	 * all rendering work and game object calculating are here.
	 *
	 * Created by ice1000 on 2016/8/13.
	 * @author ice1000
	 * @since v0.1
	 */
	inner class SwingGamePanel : JComponent() {

		init {
			addMouseListener(object : MouseListener {
				override fun mouseClicked(event: MouseEvent) = mouse(swingMouse(event, MOUSE_CLICKED))
				override fun mouseEntered(event: MouseEvent?) = Unit
				override fun mouseExited(event: MouseEvent?) = Unit
				override fun mousePressed(event: MouseEvent) = mouse(swingMouse(event, MOUSE_PRESSED))
				override fun mouseReleased(event: MouseEvent) = mouse(swingMouse(event, MOUSE_RELEASED))
			})

			addMouseMotionListener(object : MouseMotionListener {
				override fun mouseDragged(event: MouseEvent?) = Unit
				override fun mouseMoved(event: MouseEvent) = mouse(swingMouse(event, MOUSE_MOVED))
			})

			addWindowListener(object : WindowListener {
				override fun windowDeiconified(e: WindowEvent?) = Unit
				override fun windowActivated(e: WindowEvent) {
					loseFocus = false
					FClock.resume()
					onFocus()
				}

				override fun windowDeactivated(e: WindowEvent) {
					loseFocus = true
					FClock.pause()
					onLoseFocus()
				}

				override fun windowIconified(e: WindowEvent?) = Unit
				override fun windowClosing(e: WindowEvent?) = onExit()
				override fun windowClosed(e: WindowEvent?) = Unit
				override fun windowOpened(e: WindowEvent?) = Unit
			})
		}

		override fun update(g: Graphics?) = paint(g)
		override fun paintComponent(g: Graphics) {
			clearScreen(drawer)
			dealWithObjects(drawer)

			if (loseFocus && loseFocusChangeColor) {
				repeat(drawer.friceImage.width) { x: Int ->
					repeat(drawer.friceImage.height) { y: Int ->
						drawer.friceImage[x, y] = drawer.friceImage[x, y].darker()
					}
				}
			}

			drawer.restore()
			drawer.init()
			drawer.color = ColorResource.DARK_GRAY
			if (showFPS) drawer.drawString("fps: ${fpsCounter.display}", 30.0, height - 30.0)

			/*
			 * 厚颜无耻
			 * drawer.drawString("Powered by FriceEngine. ice1000", 5, 20)
			 */
			g.drawImage(drawer.friceImage.image, 0, 0, this)
		}
	}

}
