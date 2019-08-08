package pfister.quickercrafting.common.gui

import net.minecraft.client.util.RecipeItemHelper
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.inventory._
import net.minecraft.item.ItemStack

class NoDragSlot(inv: IInventory, index: Int, xPos: Int, yPos: Int) extends Slot(inv, index, xPos, yPos) {
  override def isItemValid(stack: ItemStack): Boolean = false
}
class ContainerQuickerCrafting(val PlayerInv: InventoryPlayer) extends Container {
  val craftResult = new InventoryBasic("", false, 3)
  val recipeHelper = new RecipeItemHelper()

  // Handle the player's inventory rendering
  for (
    yIndex <- 0 until 3;
    xIndex <- 0 until 9
  ) addSlotToContainer(new Slot(PlayerInv, yIndex * 9 + xIndex + 9, 8 + xIndex * 18, 90 + yIndex * 18))

  for (hotbarIndex <- 0 until 9)
    addSlotToContainer(new Slot(PlayerInv, hotbarIndex, 8 + hotbarIndex * 18, 148))

  //
  for (i <- 0 until 3)
    addSlotToContainer(new NoDragSlot(craftResult, i, 184, 90 + i * 18))

  def canFitStackInCraftResult(stack: ItemStack): Boolean = {
    val workingStack = stack.copy()
    for (i <- 0 until craftResult.getSizeInventory) {
      val item = craftResult.getStackInSlot(i)
      if (item.isEmpty)
        return true
      else if (ItemStack.areItemsEqual(workingStack, item)) {
        if (item.getCount + workingStack.getCount <= item.getMaxStackSize)
          return true
        else
          workingStack.setCount(workingStack.getCount - (item.getMaxStackSize - item.getCount))
      }
    }
    false
  }

  def isCraftResultIndex(index: Int): Boolean = getSlot(index).isInstanceOf[NoDragSlot]

  override def canInteractWith(playerIn: EntityPlayer): Boolean = {
    true
  }

  override def onContainerClosed(playerIn: EntityPlayer): Unit = {
    super.onContainerClosed(playerIn)
    for (i <- 0 until craftResult.getSizeInventory) {
      val stack = craftResult.removeStackFromSlot(i)
      if (!playerIn.inventory.addItemStackToInventory(stack))
        playerIn.dropItem(stack, false)
    }


  }

  override def transferStackInSlot(playerIn: EntityPlayer, index: Int): ItemStack = {
    if (!this.getSlot(index).isInstanceOf[NoDragSlot]) return ItemStack.EMPTY
    val slot = this.inventorySlots.get(index)
    playerIn.addItemStackToInventory(slot.onTake(playerIn, slot.getStack))
    slot.putStack(ItemStack.EMPTY)
    ItemStack.EMPTY
  }

}
