package pfister.quickercrafting.common.gui

import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.inventory.{Container, Slot}


class ContainerQuickerCrafting(playerInv: InventoryPlayer) extends Container {
  // Handle the player's inventory rendering
  for (
    yIndex <- 0 until 3;
    xIndex <- 0 until 9
  ) addSlotToContainer(new Slot(playerInv, yIndex * 9 + xIndex + 9, 8 + xIndex * 18, 101 + yIndex * 18))

  for (hotbarIndex <- 0 until 9)
    addSlotToContainer(new Slot(playerInv, hotbarIndex, 8 + hotbarIndex * 18, 159))
  //

  override def canInteractWith(playerIn: EntityPlayer): Boolean = {
    true
  }


}
