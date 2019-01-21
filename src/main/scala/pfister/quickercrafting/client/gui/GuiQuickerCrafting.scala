package pfister.quickercrafting.client.gui

import java.util.Comparator

import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.{GuiButton, GuiButtonImage}
import net.minecraft.client.renderer.{GlStateManager, RenderHelper}
import net.minecraft.client.resources.I18n
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.{Container, IContainerListener, IInventory}
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipe
import net.minecraft.util.{NonNullList, ResourceLocation}
import pfister.quickercrafting.QuickerCrafting
import pfister.quickercrafting.client.RecipeCalculator
import pfister.quickercrafting.common.gui.{ContainerQuickerCrafting, GuiButtonQuickRecipe}

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.util.Try


private[gui] object GuiQuickCrafting {
  final val TEXTURE: ResourceLocation = new ResourceLocation(QuickerCrafting.MOD_ID, "textures/gui/quickercrafting.png")
}

class GuiQuickerCrafting(playerInv: InventoryPlayer) extends GuiContainer(new ContainerQuickerCrafting(playerInv)) with IContainerListener {

  private val cachedRecipes: mutable.ListBuffer[IRecipe] = mutable.ListBuffer()

  private var recipeCalculator: RecipeCalculator = new RecipeCalculator(playerInv)

  private var recipeIterator: Iterator[IRecipe] = recipeCalculator.getRecipeIterator()

  private val fakeCraftingInventory: FakeInventoryCrafting = new FakeInventoryCrafting(this.inventorySlots)

  var currentPage: Int = 1
  var maxPage: Int = currentPage

  // Set size of window
  this.xSize = 234
  this.ySize = 183
  //
  inventorySlots.addListener(this)

  def updateRecipes(container: Container, slotInd: Int, stack: ItemStack): Unit = {
    cachedRecipes.clear()
    recipeCalculator.updateWorkingInv(container, slotInd, stack)
    recipeIterator = recipeCalculator.getRecipeIterator()
    val time = QuickerCrafting.time {
      recipeIterator.foreach(r => cachedRecipes.append(r))
      cachedRecipes.sort(new Comparator[IRecipe] {
        override def compare(o1: IRecipe, o2: IRecipe): Int =
          o1.getRecipeOutput.getItem.getCreativeTab.getTabIndex - o2.getRecipeOutput.getItem.getCreativeTab.getTabIndex
      })
    }
    QuickerCrafting.Log.info(s"Recipe generation took ${time._1} ms.")
    maxPage = cachedRecipes.size / 32
    if (cachedRecipes.size % 32 != 0) maxPage += 1
    if (currentPage > maxPage)
      currentPage = maxPage
    if (currentPage <= 0)
      currentPage = 1

  }

  override def initGui(): Unit = {
    super.initGui()
    // Populate the recipe list with buttons
    for (y <- 0 until 4; x <- 0 until 8)
      buttonList.add(new GuiButtonQuickRecipe(y * 8 + x, this.guiLeft + 7 + x * 20, this.guiTop + 7 + y * 20, 20, 20, 236, 15, 20, GuiQuickCrafting.TEXTURE))

    buttonList.add(new GuiButtonImage(33, this.guiLeft + 194, this.guiTop + 7, 11, 7, 234, 0, 7, GuiQuickCrafting.TEXTURE))
    buttonList.add(new GuiButtonImage(34, this.guiLeft + 194, this.guiTop + 27, 11, 7, 245, 0, 7, GuiQuickCrafting.TEXTURE))




  }

  // Fired on change in inventory
  override def sendSlotContents(containerToSend: Container, slotInd: Int, stack: ItemStack): Unit = {
    updateRecipes(containerToSend, slotInd, stack)
  }

  override def updateScreen(): Unit = {
    super.updateScreen()
    inventorySlots.detectAndSendChanges()
  }
  override def actionPerformed(button: GuiButton): Unit = {
    button.id match {
      case 33 => currentPage = Math.max(currentPage - 1, 1)
      case 34 => currentPage = Math.min(currentPage + 1, maxPage)
      case id if id < 33 => {
        val recipe = Try(cachedRecipes(currentPage * 32 + id))
        if (recipe.isSuccess) {
          fakeCraftingInventory.setFakedRecipe(recipe.get)
          val itemCrafted = recipe.get.getCraftingResult(fakeCraftingInventory)
          net.minecraftforge.fml.common.FMLCommonHandler.instance.firePlayerCraftingEvent(playerInv.player, itemCrafted, fakeCraftingInventory)

        }

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

  override def drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int): Unit = {
    val string = s"$currentPage/$maxPage"
    this.fontRenderer.drawString(string, 187 - (string.length - 4) * 3, 17, 0)

    this.fontRenderer.drawString(I18n.format("container.crafting"), 172, 36, 4210752)
    this.fontRenderer.drawString(I18n.format("container.inventory"), 8, 89, 4210752)
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
      if (recipe.isDefined)
        renderToolTip(recipe.get.getRecipeOutput, mouseX, mouseY)
    }

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

