package org.frice.game.utils.message

import java.awt.Frame
import javax.swing.JOptionPane

/**
 * Created by ice1000 on 2016/8/14.
 * @author ice1000
 * @since v0.2
 */
class FDialog(val game: Frame?) {
	fun show(msg: String) = JOptionPane.showMessageDialog(game, msg)
	fun input() = input("")
	fun input(msg: String) = JOptionPane.showInputDialog(game, msg)!!
	fun confirm(msg: String) = confirm(msg, "")
	fun confirm(msg: String, title: String) = confirm(msg, title, JOptionPane.YES_NO_CANCEL_OPTION)
	fun confirm(msg: String, title: String, option: Int) = JOptionPane.showConfirmDialog(game, msg, title, option)
}