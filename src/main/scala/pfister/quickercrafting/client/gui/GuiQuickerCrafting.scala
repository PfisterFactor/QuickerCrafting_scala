package pfister.quickercrafting.client.gui

import java.util.concurrent.ThreadLocalRandom

import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.ResourceLocation
import pfister.quickercrafting.QuickerCrafting
import pfister.quickercrafting.common.gui.{ContainerQuickerCrafting, GuiButtonQuickRecipe}

import scala.collection.JavaConversions._


private[gui] object GuiQuickCrafting {
  final val BACKGROUND: ResourceLocation = new ResourceLocation(QuickerCrafting.MOD_ID, "textures/gui/quickercrafting.png")
}

class GuiQuickerCrafting(playerInv: InventoryPlayer) extends GuiContainer(new ContainerQuickerCrafting(playerInv)) {

  // Create a new array of recipe items the size of the amount of crafting buttons there are
  private val displayedRecipeItems: Array[ItemStack] = new Array(32)
  this.xSize = 196
  this.ySize = 175

  override def initGui(): Unit = {
    super.initGui()
    for (y <- 0 until 4; x <- 0 until 8)
      buttonList.add(new GuiButtonQuickRecipe(y * 4 + x, this.guiLeft + 7 + x * 20, this.guiTop + 7 + y * 20, 20, 20, 236, 15, 20, GuiQuickCrafting.BACKGROUND))

    for (i <- 0 until 32)
      displayedRecipeItems(i) = new ItemStack(Item.REGISTRY.getObjectById(ThreadLocalRandom.current().nextInt(256, 300)), 1)
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
    drawRecipeItems(partialTicks)

  }

  // Draws all the recipe items over the crafting buttons
  def drawRecipeItems(partialTicks: Float): Unit = {
    buttonList.foreach(button => {
      GlStateManager.pushMatrix()
      //      GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)
      //      GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
      GlStateManager.translate(2, 2, 0)
      //      GlStateManager.scale(0.80,0.80,1)

      drawItemStack(displayedRecipeItems(button.id), button.x, button.y, "" + ThreadLocalRandom.current().nextInt(1, 65))
      GlStateManager.popMatrix()
    })
    val button = buttonList.get(ThreadLocalRandom.current().nextInt(0, 32))
    button.enabled = !button.enabled
  }


}
