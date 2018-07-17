package org.frice.anim.rotate

import org.frice.Game
import org.frice.launch
import org.frice.obj.sub.ShapeObject
import org.frice.resource.graphics.ColorResource
import org.frice.util.shape.FOval

class AccelerateRotateTest : Game() {
	override fun onInit() {
		setSize(200, 200)
		setLocation(550, 250)
		addObject(
			ShapeObject(ColorResource.八云蓝, FOval(20.0, 40.0), 80.0, 60.0).apply { addAnim(AccelerateRotate(3.0)) },
			ShapeObject(ColorResource(0x7F781895), FOval(20.0, 40.0), 80.0, 60.0).apply { addAnim(AccelerateRotate(-3.0)) })
	}
	companion object { @JvmStatic fun main(args: Array<String>) = launch(AccelerateRotateTest::class.java) }
}

