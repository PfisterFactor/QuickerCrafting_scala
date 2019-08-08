package pfister.quickercrafting.client.gui

object GuiScrollBar {
  val TEX_OFFSET_X = 232
  val TEX_OFFSET_Y = 0
  val TEX_WIDTH = 12
  val TEX_HEIGHT = 15
  val GUI_POS_X = 174
  val GUI_POS_Y = 20
  val SCROLLBAR_HEIGHT = 53
}

class GuiScrollBar(val guiLeft: Int, val guiTop: Int) {
  var isScrolling = false
  var currentScroll = 0.0d
  var isEnabled = true

  def isInScrollBarBounds(mouseX: Int, mouseY: Int): Boolean = mouseX >= guiLeft + GuiScrollBar.GUI_POS_X && mouseX < guiLeft + GuiScrollBar.GUI_POS_X + GuiScrollBar.TEX_WIDTH && mouseY >= guiTop + GuiScrollBar.GUI_POS_Y && mouseY < guiTop + GuiScrollBar.GUI_POS_Y + GuiScrollBar.SCROLLBAR_HEIGHT


}
