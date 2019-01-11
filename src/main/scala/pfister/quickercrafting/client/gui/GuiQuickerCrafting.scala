package pfister.quickercrafting.client.gui

import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.util.ResourceLocation
import pfister.quickercrafting.QuickerCrafting
import pfister.quickercrafting.common.gui.{ContainerQuickerCrafting, GuiButtonImageExt}


private[gui] object GuiQuickCrafting {
  final val BACKGROUND: ResourceLocation = new ResourceLocation(QuickerCrafting.MOD_ID, "textures/gui/quickercrafting.png")
}

class GuiQuickerCrafting(playerInv: InventoryPlayer) extends GuiContainer(new ContainerQuickerCrafting(playerInv)) {


  override def initGui(): Unit = {
    super.initGui()
    for (y <- 0 until 4; x <- 0 until 8)
      buttonList.add(new GuiButtonImageExt(y * 4 + x, this.guiLeft + 7 + x * 18, this.guiTop + 7 + y * 18, 18, 18, 238, 15, 18, GuiQuickCrafting.BACKGROUND))
  }

  override def updateScreen(): Unit = {
    super.updateScreen()
  }

  // Draws the background to the GUI
  override def drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int): Unit = {
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F)
    this.mc.getTextureManager.bindTexture(GuiQuickCrafting.BACKGROUND)
    this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize)
  }

  // Draws the buttons and stuff on top of the background
  override def drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float): Unit = {
    drawDefaultBackground()
    super.drawScreen(mouseX, mouseY, partialTicks)
  }


}
