package org.frice.game.resource

import java.io.File
import javax.imageio.ImageIO

/**
 * Created by ice1000 on 2016/8/13.
 * @author ice1000
 * @since v0.1
 */
class FileImageResource(file: File) : ImageResource {
	constructor(path: String) : this(File(path))

	override val image = ImageIO.read(file)!!

	override fun getResource() = image
}