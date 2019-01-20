package pfister.quickercrafting.common.gui

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButtonImage
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation

// Simple extension class to implemented rendering disabled buttons on GuiButtonImage.
// I don't know why it's not there in the first place.
class GuiButtonQuickRecipe(id: Int, x: Int, y: Int, width: Int, height: Int, xtext: Int, ytext: Int, ydiff: Int, res: ResourceLocation) extends GuiButtonImage(id, x, y, width, height, xtext, ytext, ydiff, res) {


  // If the button is disabled just draw the disabled texture (yDiff * 2) over the button.
  // Little bit of overdraw, but it's much prettier than just reimplementing the method.
  override def drawButton(mc: Minecraft, mouseX: Int, mouseY: Int, partialTicks: Float): Unit = {
    super.drawButton(mc, mouseX, mouseY, partialTicks)
    if (!this.visible || this.enabled) return

    GlStateManager.disableDepth()
    // The resource is already bound from the super method.
    this.drawTexturedModalRect(this.x, this.y, xtext, ytext + ydiff * 2, this.width, this.height)
    GlStateManager.enableDepth()

  }


}
