package pfister.quickercrafting.client.gui

import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory._
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipe
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import pfister.quickercrafting.common.gui.{ContainerQuickerCrafting, NoDragSlot}
import pfister.quickercrafting.common.util.RecipeCalculator

import scala.collection.JavaConversions._
import scala.util.Try

@SideOnly(Side.CLIENT)
class ClientSlot(inv: IInventory, index: Int, xPos: Int, yPos: Int) extends NoDragSlot(inv, index, xPos, yPos) {
  var enabled = true

  override def isEnabled: Boolean = enabled
}

@SideOnly(Side.CLIENT)
class ClientContainerQuickerCrafting(playerInv: InventoryPlayer) extends ContainerQuickerCrafting(playerInv) {
  val RecipeCalculator: RecipeCalculator = new RecipeCalculator(playerInv)
  val clientSlotsStart = inventorySlots.size()
  // Stores all the recipes
  val recipeInventory = new InventoryBasic("", false, 27)
  var slotRowYOffest: Int = 0
  private var recipeStream: Stream[IRecipe] = _

  for (y <- 0 until 3; x <- 0 until 9) {
    addSlotToContainer(new ClientSlot(recipeInventory, y * 9 + x, 8 + x * 18, 20 + y * 18))
  }

  def updateDisplay(currentScroll: Double): Unit = {
    slotRowYOffest = 0 // currentScroll trickery
    inventorySlots.drop(clientSlotsStart).foreach(slot => {
      val recipe = Try(recipeStream.get(slot.getSlotIndex + slotRowYOffest * 9))

      if (recipe.isSuccess) {
        slot.asInstanceOf[ClientSlot].enabled = true
        slot.putStack(recipe.get.getRecipeOutput)
      }
      else {
        slot.putStack(ItemStack.EMPTY)
        slot.asInstanceOf[ClientSlot].enabled = false
      }
    })
  }

  def getRecipeForSlot(slotNum: Int): Option[IRecipe] = {
    if (slotNum < clientSlotsStart || slotNum >= inventorySlots.size())
      None
    else
      Try(recipeStream.get(inventorySlots.get(slotNum).getSlotIndex + slotRowYOffest * 9)).toOption
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

        if (true) {
          listeners.foreach(_.sendSlotContents(this, i, itemstack1))
          val slot = this.getSlot(i)
          if (slot.inventory.isInstanceOf[InventoryPlayer])
            updateRecipes(slot.getSlotIndex, itemstack1)
        }
      }

    }
  }

  def updateRecipes(invIndex: Int, stack: ItemStack): Unit = {
    RecipeCalculator.updateWorkingInv(invIndex, stack)
    recipeStream = RecipeCalculator.getRecipeIterator().toStream
  }

}
