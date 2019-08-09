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

@SideOnly(Side.CLIENT)
class ClientSlot(inv: IInventory, index: Int, xPos: Int, yPos: Int) extends NoDragSlot(inv, index, xPos, yPos) {
  var enabled = true

  override def isEnabled: Boolean = enabled
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

  def updateDisplay(currentScroll: Double): Unit = {
    recipeStream = RecipeCalculator.getRecipeIterator.toStream
    val length = recipeStream.length
    val i = (length + 8) / 9 - 3
    slotRowYOffset = ((currentScroll * i.toDouble) + 0.5D).toInt
    shouldDisplayScrollbar = length > inventorySlots.size() - clientSlotsStart


    inventorySlots.drop(clientSlotsStart).foreach(slot => {
      val recipe = Try(recipeStream(slotRowYOffset * 9 + slot.slotNumber - clientSlotsStart))

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
    else {
      Try(recipeStream(slotRowYOffset * 9 + slotNum - clientSlotsStart)).toOption
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
