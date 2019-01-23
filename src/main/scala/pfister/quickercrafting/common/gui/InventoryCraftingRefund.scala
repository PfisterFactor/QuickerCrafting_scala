package pfister.quickercrafting.common.gui

import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.{Container, InventoryCrafting}
import net.minecraft.item.ItemStack

class InventoryCraftingRefund(container: Container, width: Int, height: Int, refundInv: InventoryPlayer) extends InventoryCrafting(container, width, height) {
  override def setInventorySlotContents(index: Int, newStack: ItemStack): Unit = {
    val thisStack = getStackInSlot(index)
    if (newStack.getCount > 1) {
      val splitStack = newStack.splitStack(newStack.getCount - 1)
      if (!refundInv.addItemStackToInventory(splitStack))
        refundInv.player.dropItem(splitStack, false)
    }
    if (newStack.isEmpty || thisStack.getCount < 1 || thisStack.isEmpty) {
      super.setInventorySlotContents(index, newStack)
      return
    }

    if (!refundInv.addItemStackToInventory(newStack))
      refundInv.player.dropItem(newStack, false)


  }


  override def getInventoryStackLimit: Int = 1

}
