package pfister.quickercrafting.client.gui

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.{GlStateManager, RenderHelper}
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.RecipeItemHelper
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory._
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipe
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.MathHelper
import net.minecraftforge.client.event.RenderTooltipEvent
import net.minecraftforge.fml.client.config.GuiUtils
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import org.lwjgl.input.Mouse
import pfister.quickercrafting.QuickerCrafting
import pfister.quickercrafting.common.gui.ContainerQuickerCrafting
import pfister.quickercrafting.common.network.{MessageCraftItem, PacketHandler}
import pfister.quickercrafting.common.util.CraftHandler

import scala.collection.JavaConversions._


// Companion object for GuiQuickerCrafting
// The name is slightly different because forge throws a fit if we try to register an object as an event handler with the same name as a non-static class
@EventBusSubscriber(Array(Side.CLIENT))
object GuiQuickCrafting {
  final val TEXTURE: ResourceLocation = new ResourceLocation(QuickerCrafting.MOD_ID, "textures/gui/quickercrafting_new.png")

  def canStack(itemStack1: ItemStack, itemStack2: ItemStack): Boolean = !itemStack1.isEmpty && !itemStack2.isEmpty && itemStack1.isItemEqual(itemStack2) && itemStack1.isStackable && (!itemStack1.getHasSubtypes || (itemStack1.getItemDamage == itemStack2.getItemDamage)) && ItemStack.areItemStackTagsEqual(itemStack1, itemStack2)

  @SubscribeEvent
  def onToolTipRender(event: RenderTooltipEvent.PostText): Unit = {
    if (!Minecraft.getMinecraft.currentScreen.isInstanceOf[GuiQuickerCrafting]) return

    val gui = Minecraft.getMinecraft.currentScreen.asInstanceOf[GuiQuickerCrafting]
    val recipe = gui.getHoveredRecipe
    if (recipe.isEmpty) return

    val itemMap = gui.inventorySlots.asInstanceOf[ClientContainerQuickerCrafting].RecipeCalculator.doCraft(recipe.get)

    if (itemMap.isEmpty) return

    val packedItemsAndCounts = itemMap.get.foldLeft(Map[Int, Int]())((acc, pair) => {
      val stackPacked = RecipeItemHelper.pack(Minecraft.getMinecraft.player.openContainer.inventoryItemStacks.get(pair._1))
      acc.updated(stackPacked, acc.getOrDefault(stackPacked, 0) + pair._2)
    })

    val tooltipX = event.getX + event.getWidth + 7
    val tooltipY = event.getY


    val tooltipTextWidth = if (packedItemsAndCounts.size >= 3) 54 else packedItemsAndCounts.size * 18
    val tooltipHeight = (1 + packedItemsAndCounts.size / 3) * 18
    val backgroundColor = 0xF0100010
    val borderColorStart = 0x505000FF
    val borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000

    GuiUtils.drawGradientRect(300, tooltipX - 3, tooltipY - 4, tooltipX + tooltipTextWidth + 3, tooltipY - 3, backgroundColor, backgroundColor)
    GuiUtils.drawGradientRect(300, tooltipX - 3, tooltipY + tooltipHeight + 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 4, backgroundColor, backgroundColor)
    GuiUtils.drawGradientRect(300, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor)
    GuiUtils.drawGradientRect(300, tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor)
    GuiUtils.drawGradientRect(300, tooltipX + tooltipTextWidth + 3, tooltipY - 3, tooltipX + tooltipTextWidth + 4, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor)
    GuiUtils.drawGradientRect(300, tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd)
    GuiUtils.drawGradientRect(300, tooltipX + tooltipTextWidth + 2, tooltipY - 3 + 1, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd)
    GuiUtils.drawGradientRect(300, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY - 3 + 1, borderColorStart, borderColorStart)
    GuiUtils.drawGradientRect(300, tooltipX - 3, tooltipY + tooltipHeight + 2, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, borderColorEnd, borderColorEnd)

    var x = 0
    var y = 0
    packedItemsAndCounts.foreach(pair => {
      GlStateManager.pushMatrix()
      RenderHelper.disableStandardItemLighting()
      RenderHelper.enableGUIStandardItemLighting()
      GlStateManager.translate(0, 0, 301)
      val stack = RecipeItemHelper.unpack(pair._1)
      gui.drawItemStack(stack, tooltipX + x * 18, tooltipY + y * 18, "" + pair._2)
      GlStateManager.popMatrix()
      y = if (x == 2) y + 1 else y
      x = (x + 1) % 3
    })

  }
}

class GuiQuickerCrafting(playerInv: InventoryPlayer) extends GuiContainer(new ClientContainerQuickerCrafting(playerInv)) {


  //
  var Scrollbar: GuiScrollBar = _

  // Set size of window
  this.xSize = 207
  this.ySize = 172
  // If the mouse was down the last frame
  private var wasClicking = false

  private var hoveredRecipe: Option[IRecipe] = None
  def getHoveredRecipe: Option[IRecipe] = hoveredRecipe

  override def initGui(): Unit = {
    super.initGui()
    Scrollbar = new GuiScrollBar(guiLeft, guiTop)
  }

  override def updateScreen(): Unit = {
    super.updateScreen()

    inventorySlots.detectAndSendChanges()
  }

  override def handleMouseClick(slotIn: Slot, slotId: Int, mouseButton: Int, `type`: ClickType): Unit = {
    if (slotId < inventorySlots.asInstanceOf[ClientContainerQuickerCrafting].clientSlotsStart)
      super.handleMouseClick(slotIn, slotId, mouseButton, `type`)
    else
    if (hoveredRecipe.isDefined) {
      if (CraftHandler.tryCraftRecipe(this.inventorySlots.asInstanceOf[ContainerQuickerCrafting], hoveredRecipe.get))
        PacketHandler.INSTANCE.sendToServer(new MessageCraftItem(hoveredRecipe.get))
    }
  }

  override def handleMouseInput(): Unit = {
    super.handleMouseInput()
    var i = Mouse.getEventDWheel
    if (i != 0 && Scrollbar.isEnabled) {
      val rows = (this.inventorySlots.asInstanceOf[ClientContainerQuickerCrafting].recipeStream.length + 8) / 9
      if (i > 0) i = 1
      else if (i < 0) i = -1
      Scrollbar.isScrolling = true
      Scrollbar.currentScroll = Scrollbar.currentScroll - i / rows.toFloat
      Scrollbar.currentScroll = MathHelper.clamp(Scrollbar.currentScroll, 0.0F, 1.0F)
    }
  }

  // Draws the background to the GUI
  override def drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int): Unit = {
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F)
    // Bind the GUI texture
    this.mc.getTextureManager.bindTexture(GuiQuickCrafting.TEXTURE)
    this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize)

  }

  // Draws the buttons and stuff on top of the background
  override def drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float): Unit = {
    var hoveredSlotIndex = Option(getSlotUnderMouse).find(_.isInstanceOf[ClientSlot]).map(_.slotNumber).getOrElse(-1)
    if (Scrollbar.isScrolling)
      hoveredSlotIndex = -1
    inventorySlots.asInstanceOf[ClientContainerQuickerCrafting].updateDisplay(Scrollbar.currentScroll, hoveredSlotIndex)
    Scrollbar.isEnabled = inventorySlots.asInstanceOf[ClientContainerQuickerCrafting].shouldDisplayScrollbar
    drawDefaultBackground()
    val isClicking: Boolean = Mouse.isButtonDown(0)
    if (!wasClicking && isClicking && Scrollbar.isInScrollBarBounds(mouseX, mouseY))
      Scrollbar.isScrolling = true
    if (!isClicking)
      Scrollbar.isScrolling = false

    wasClicking = isClicking

    if (Scrollbar.isScrolling) {
      Scrollbar.currentScroll = (mouseY - (guiTop + GuiScrollBar.GUI_POS_Y)) / GuiScrollBar.SCROLLBAR_HEIGHT.toFloat
      Scrollbar.currentScroll = MathHelper.clamp(Scrollbar.currentScroll, 0.0F, 1.0F)
    }
    super.drawScreen(mouseX, mouseY, partialTicks)

    if (getSlotUnderMouse != null && getSlotUnderMouse.isInstanceOf[ClientSlot]) {
      hoveredRecipe = getSlotUnderMouse.asInstanceOf[ClientSlot].Recipe
    }
    else
      hoveredRecipe = None

    renderHoveredToolTip(mouseX, mouseY)
  }

  override def drawSlot(slotIn: Slot): Unit = {
    super.drawSlot(slotIn)

  }

  override def renderHoveredToolTip(mouseX: Int, mouseY: Int): Unit = {
    super.renderHoveredToolTip(mouseX, mouseY)
    if (hoveredRecipe.isDefined)
      renderToolTip(hoveredRecipe.get.getRecipeOutput, mouseX, mouseY)
  }

  override def drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int): Unit = {
    this.fontRenderer.drawString(I18n.format("container.crafting"), 8, 6, 4210752)
    this.fontRenderer.drawString(I18n.format("container.inventory"), 8, 78, 4210752)
    GlStateManager.colorMask(true, true, true, false)
    GlStateManager.pushMatrix()
    GlStateManager.disableLighting()
    GlStateManager.disableDepth()
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F)
    this.mc.getTextureManager.bindTexture(GuiQuickCrafting.TEXTURE)
    if (Scrollbar.isEnabled)
      this.drawTexturedModalRect(GuiScrollBar.GUI_POS_X, MathHelper.clamp(GuiScrollBar.GUI_POS_Y - GuiScrollBar.TEX_HEIGHT / 2 + (GuiScrollBar.SCROLLBAR_HEIGHT * Scrollbar.currentScroll).toInt, GuiScrollBar.GUI_POS_Y, GuiScrollBar.GUI_POS_Y + GuiScrollBar.SCROLLBAR_HEIGHT - GuiScrollBar.TEX_HEIGHT - 1), GuiScrollBar.TEX_OFFSET_X, GuiScrollBar.TEX_OFFSET_Y, GuiScrollBar.TEX_WIDTH, GuiScrollBar.TEX_HEIGHT)
    else {
      this.drawTexturedModalRect(GuiScrollBar.GUI_POS_X, GuiScrollBar.GUI_POS_Y, GuiScrollBar.TEX_OFFSET_X + GuiScrollBar.TEX_WIDTH, GuiScrollBar.TEX_OFFSET_Y, GuiScrollBar.TEX_WIDTH, GuiScrollBar.TEX_HEIGHT)
      Scrollbar.currentScroll = 0
    }
    GlStateManager.popMatrix()

    inventorySlots.inventorySlots.filter(s => s.isInstanceOf[ClientSlot] && (s.asInstanceOf[ClientSlot].State == SlotState.DISABLED || s.asInstanceOf[ClientSlot].State == SlotState.EMPTY)).foreach(slot =>
      drawGradientRect(slot.xPos, slot.yPos, slot.xPos + 16, slot.yPos + 16, 0x55000000, 0x55000000)
    )

  }

  def renderRecipeBoxTooltip(mouseX: Int, mouseY: Int): Unit = {
    GlStateManager.pushMatrix()
    GuiUtils.drawGradientRect(0, mouseX + 12, mouseY - 12, mouseX + 54 + 12, mouseY + 54 - 12, -267386864, -267386864)
    GlStateManager.popMatrix()
  }

  override def hasClickedOutside(mouseX: Int, mouseY: Int, left: Int, top: Int): Boolean = {
    if (mouseY > top + 107) {
      mouseX < left || mouseX > left + 175 || mouseY < top || mouseY > top + ySize
    }
    else
      super.hasClickedOutside(mouseX, mouseY, left, top)
  }

}

