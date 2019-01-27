package pfister.quickercrafting.client.gui

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.{GuiButton, GuiButtonImage}
import net.minecraft.client.renderer.{GlStateManager, RenderHelper}
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.RecipeItemHelper
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.{Container, IContainerListener, IInventory}
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipe
import net.minecraft.util.{NonNullList, ResourceLocation}
import net.minecraftforge.client.event.RenderTooltipEvent
import net.minecraftforge.fml.client.config.GuiUtils
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import pfister.quickercrafting.QuickerCrafting
import pfister.quickercrafting.common.gui.{ContainerQuickerCrafting, GuiButtonQuickRecipe}
import pfister.quickercrafting.common.network.{MessageCraftItem, PacketHandler}
import pfister.quickercrafting.common.util.ExtensionClasses.ExtItemStack
import pfister.quickercrafting.common.util.RecipeCalculator

import scala.collection.JavaConversions._
import scala.util.Try

@EventBusSubscriber
object GuiQuickCrafting {
  final val TEXTURE: ResourceLocation = new ResourceLocation(QuickerCrafting.MOD_ID, "textures/gui/quickercrafting.png")

  def canStack(itemStack1: ItemStack, itemStack2: ItemStack): Boolean = !itemStack1.isEmpty && !itemStack2.isEmpty && itemStack1.isItemEqual(itemStack2) && itemStack1.isStackable && (!itemStack1.getHasSubtypes || (itemStack1.getItemDamage == itemStack2.getItemDamage)) && ItemStack.areItemStackTagsEqual(itemStack1, itemStack2)

  @SubscribeEvent
  def onToolTipRender(event: RenderTooltipEvent.PostText): Unit = {
    if (!Minecraft.getMinecraft.currentScreen.isInstanceOf[GuiQuickerCrafting]) return

    val gui = Minecraft.getMinecraft.currentScreen.asInstanceOf[GuiQuickerCrafting]
    val recipe = gui.getHoveredRecipe
    if (recipe.isEmpty) return

    val itemMap = gui.RecipeCalculator.tryCraftRecipe(recipe.get)

    if (itemMap.isEmpty) return

    val packedItemsAndCounts = itemMap.get.foldLeft(Map[Int, Int]())((acc, pair) => {
      val stackPacked = RecipeItemHelper.pack(Minecraft.getMinecraft.player.inventory.mainInventory.get(pair._1))
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

class GuiQuickerCrafting(playerInv: InventoryPlayer) extends GuiContainer(new ContainerQuickerCrafting(playerInv)) with IContainerListener {

  val RecipeCalculator: RecipeCalculator = new RecipeCalculator(playerInv)
  private var cachedRecipes: Stream[IRecipe] = _

  private var currentPage: Int = 1

  // Set size of window
  this.xSize = 208
  this.ySize = 192
  //
  inventorySlots.addListener(this)

  private var hoveredRecipe: Option[IRecipe] = None

  def getHoveredRecipe: Option[IRecipe] = hoveredRecipe

  override def initGui(): Unit = {
    super.initGui()
    // Populate the recipe list with buttons
    for (y <- 0 until 4; x <- 0 until 8)
      buttonList.add(new GuiButtonQuickRecipe(y * 8 + x, this.guiLeft + 7 + x * 20, this.guiTop + 16 + y * 20, 20, 20, 236, 15, 20, GuiQuickCrafting.TEXTURE))
    buttonList.add(new GuiButtonImage(33, this.guiLeft + 178, this.guiTop + 16, 12, 7, 232, 0, 7, GuiQuickCrafting.TEXTURE))
    buttonList.add(new GuiButtonImage(34, this.guiLeft + 178, this.guiTop + 36, 12, 7, 244, 0, 7, GuiQuickCrafting.TEXTURE))
  }

  // Fired on change in inventory
  override def sendSlotContents(containerToSend: Container, slotInd: Int, stack: ItemStack): Unit = {
    val slot = this.inventorySlots.getSlot(slotInd)
    if (slot.inventory.isInstanceOf[InventoryPlayer])
      updateRecipes(containerToSend, slot.getSlotIndex, stack)
  }

  def updateRecipes(container: Container, invIndex: Int, stack: ItemStack): Unit = {
    RecipeCalculator.updateWorkingInv(invIndex, stack)
    cachedRecipes = RecipeCalculator.getRecipeIterator().toStream
  }

  override def updateScreen(): Unit = {
    super.updateScreen()
    inventorySlots.detectAndSendChanges()
  }


  override def actionPerformed(button: GuiButton): Unit = {
    button.id match {
      case 33 => currentPage = Math.max(currentPage - 1, 1)
      case 34 =>
        if (cachedRecipes.isDefinedAt(currentPage * 32))
          currentPage = currentPage + 1
      case id if id < 33 =>
        val recipe = Try(cachedRecipes((currentPage - 1) * 32 + id))
        if (recipe.isSuccess) {
          val craftSlot = this.inventorySlots.getSlot(36)
          if (craftSlot.getStack.isEmpty || recipe.get.getRecipeOutput.canStack(craftSlot.getStack))
            PacketHandler.INSTANCE.sendToServer(new MessageCraftItem(recipe.get))
        }
      case _ =>
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
    drawDefaultBackground()
    super.drawScreen(mouseX, mouseY, partialTicks)
    drawRecipeItems(partialTicks)
    renderHoveredToolTip(mouseX, mouseY)
  }

  // Draws all the recipe items over the crafting buttons
  def drawRecipeItems(partialTicks: Float): Unit = {
    buttonList.take(32).foreach(button => {
      val recipe = Try(cachedRecipes((currentPage - 1) * 32 + button.id)).toOption

      if (recipe.isDefined) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(2, 2, 0)
        RenderHelper.disableStandardItemLighting()
        RenderHelper.enableGUIStandardItemLighting()
        val itemstack = recipe.get.getRecipeOutput
        drawItemStack(itemstack, button.x, button.y, "" + itemstack.getCount)
        button.enabled = true
        GlStateManager.popMatrix()
      }
      else
        button.enabled = false
    })

  }

  override def renderHoveredToolTip(mouseX: Int, mouseY: Int) = {
    super.renderHoveredToolTip(mouseX, mouseY)
    val tooltipButton = buttonList.take(32).find(b => b.enabled && b.isMouseOver)
    if (tooltipButton.isDefined) {
      val recipe = Try(cachedRecipes.get((currentPage - 1) * 32 + tooltipButton.get.id)).toOption
      if (recipe.isDefined) {
        hoveredRecipe = recipe
        renderToolTip(recipe.get.getRecipeOutput, mouseX, mouseY)
      }
    }
    else
      hoveredRecipe = None

  }

  override def drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int): Unit = {
    val string = s"$currentPage"
    this.fontRenderer.drawString(string, 172 - (string.length - 4) * 3, 26, 0)

    this.fontRenderer.drawString(I18n.format("container.crafting"), 8, 6, 4210752)
    this.fontRenderer.drawString(I18n.format("container.inventory"), 8, 98, 4210752)

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

  override def sendWindowProperty(containerIn: Container, varToUpdate: Int, newValue: Int): Unit = {}

  override def sendAllWindowProperties(containerIn: Container, inventory: IInventory): Unit = {}

  override def sendAllContents(containerToSend: Container, itemsList: NonNullList[ItemStack]): Unit = {}
}

