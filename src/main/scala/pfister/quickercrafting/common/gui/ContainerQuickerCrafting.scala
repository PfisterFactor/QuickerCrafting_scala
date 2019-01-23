package pfister.quickercrafting.common.gui

import net.minecraft.client.util.RecipeItemHelper
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.inventory._
import net.minecraft.item.ItemStack


class ContainerQuickerCrafting(val PlayerInv: InventoryPlayer) extends Container {
  val craftResult = new InventoryBasic("", false, 1)
  val recipeHelper = new RecipeItemHelper()


  // Handle the player's inventory rendering
  for (
    yIndex <- 0 until 3;
    xIndex <- 0 until 9
  ) addSlotToContainer(new Slot(PlayerInv, yIndex * 9 + xIndex + 9, 8 + xIndex * 18, 110 + yIndex * 18))

  for (hotbarIndex <- 0 until 9)
    addSlotToContainer(new Slot(PlayerInv, hotbarIndex, 8 + hotbarIndex * 18, 168))
  //

  addSlotToContainer(new Slot(craftResult, 0, 176, 78))


  override def canInteractWith(playerIn: EntityPlayer): Boolean = {
    true
  }

  override def onContainerClosed(playerIn: EntityPlayer): Unit = {
    super.onContainerClosed(playerIn)
    playerIn.dropItem(craftResult.removeStackFromSlot(0), false)
  }

  override def transferStackInSlot(playerIn: EntityPlayer, index: Int): ItemStack = {
    if (index != 36) return ItemStack.EMPTY
    val slot = this.inventorySlots.get(index)
    playerIn.addItemStackToInventory(slot.onTake(playerIn, slot.getStack))
    slot.putStack(ItemStack.EMPTY)
    return ItemStack.EMPTY
  }
}
