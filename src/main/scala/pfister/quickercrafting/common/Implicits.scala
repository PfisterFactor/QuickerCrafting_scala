package pfister.quickercrafting.common

import net.minecraft.item.ItemStack

object Implicits {

  implicit class ExtItemStack(itemStack: ItemStack) {
    def canStack(other: ItemStack): Boolean = !itemStack.isEmpty && !other.isEmpty && itemStack.isItemEqual(other) && itemStack.isStackable && (!itemStack.getHasSubtypes || (itemStack.getItemDamage == other.getItemDamage)) && ItemStack.areItemStackTagsEqual(itemStack, other)

  }

}
