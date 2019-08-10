package pfister.quickercrafting.client.gui

import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory._
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipe
import net.minecraft.util.NonNullList
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import pfister.quickercrafting.common.gui.{ContainerQuickerCrafting, NoDragSlot}
import pfister.quickercrafting.common.util.RecipeCalculator

import scala.collection.JavaConversions._
import scala.util.Try

object SlotState extends Enumeration {
  // Enabled means the slot has a recipe, disabled means the slot has no recipe, empty means the slot has a recipe, but can't be crafted
  val ENABLED, DISABLED, EMPTY = Value
}

@SideOnly(Side.CLIENT)
class ClientSlot(inv: IInventory, index: Int, xPos: Int, yPos: Int) extends NoDragSlot(inv, index, xPos, yPos) {

  var State: SlotState.Value = SlotState.EMPTY
  var Recipe: Option[IRecipe] = None

  override def isEnabled: Boolean = State == SlotState.ENABLED || State == SlotState.EMPTY
}

@SideOnly(Side.CLIENT)
class ClientContainerQuickerCrafting(playerInv: InventoryPlayer) extends ContainerQuickerCrafting(playerInv) {
  val RecipeCalculator: RecipeCalculator = new RecipeCalculator(this)
  val clientSlotsStart = inventorySlots.size()

  // Stores all the recipes
  val recipeInventory = new InventoryBasic("", false, 27)
  var shouldDisplayScrollbar = false
  protected var slotRowYOffset = 0
  protected var recipeStream: Stream[IRecipe] = RecipeCalculator.getRecipeIterator.toStream

  for (y <- 0 until 3; x <- 0 until 9) {
    addSlotToContainer(new ClientSlot(recipeInventory, y * 9 + x, 8 + x * 18, 20 + y * 18))
  }

  def updateDisplay(currentScroll: Double, exemptSlotIndex: Int): Unit = {
    recipeStream = RecipeCalculator.getRecipeIterator.toStream
    val length = recipeStream.length
    val i = (length + 8) / 9 - 3
    slotRowYOffset = ((currentScroll * i.toDouble) + 0.5D).toInt
    shouldDisplayScrollbar = length > inventorySlots.size() - clientSlotsStart
    inventorySlots.drop(clientSlotsStart).filterNot(_.slotNumber == exemptSlotIndex).map(_.asInstanceOf[ClientSlot]).foreach(slot => {
      val recipe = Try(recipeStream(slotRowYOffset * 9 + slot.slotNumber - clientSlotsStart))
      if (recipe.isSuccess) {
        slot.putStack(recipe.get.getRecipeOutput)
        slot.State = SlotState.ENABLED
        slot.Recipe = Some(recipe.get)
      }
      else {
        slot.putStack(ItemStack.EMPTY)
        slot.State = SlotState.DISABLED
        slot.Recipe = None
      }
    })
    val exemptSlot: Option[ClientSlot] = if (exemptSlotIndex != -1 && exemptSlotIndex > clientSlotsStart)
      Some(getSlot(exemptSlotIndex).asInstanceOf[ClientSlot])
    else
      None

    if (exemptSlot.isDefined && exemptSlot.get.State != SlotState.DISABLED && exemptSlot.get.Recipe.isDefined && !recipeStream.contains(exemptSlot.get.Recipe.get))
      exemptSlot.get.State = SlotState.EMPTY

  }

  def getRecipeForSlot(slotNum: Int): Option[IRecipe] = {
    if (slotNum < clientSlotsStart || slotNum >= inventorySlots.size())
      None
    else {
      getSlot(slotNum).asInstanceOf[ClientSlot].Recipe
    }
  }

  // Only sends changes for the slots shared between server and client
  override def detectAndSendChanges(): Unit = {
    for (i <- 0 until clientSlotsStart) {
      val itemstack = this.inventorySlots.get(i).getStack
      var itemstack1 = this.inventoryItemStacks.get(i)

      if (!ItemStack.areItemStacksEqual(itemstack, itemstack1)) {
        val clientStackChanged = !ItemStack.areItemStacksEqualUsingNBTShareTag(itemstack1, itemstack)
        itemstack1 = if (itemstack.isEmpty)
          ItemStack.EMPTY
        else
          itemstack.copy

        this.inventoryItemStacks.set(i, itemstack1)

        if (clientStackChanged) {
          listeners.foreach(_.sendSlotContents(this, i, itemstack1))
        }
      }

    }
  }

  override def getInventory: NonNullList[ItemStack] = {
    val list = NonNullList.create[ItemStack]()
    list.addAll(super.getInventory.take(clientSlotsStart))
    list
  }


}
