package pfister.quickercrafting.common.util

import net.minecraft.client.util.RecipeItemHelper
import net.minecraft.inventory.InventoryBasic
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipe
import net.minecraftforge.fml.common.registry.ForgeRegistries

import scala.collection.JavaConversions._

object ExtensionClasses {

  implicit class ExtItemStack(itemStack: ItemStack) {
    def canStack(other: ItemStack): Boolean = !itemStack.isEmpty && !other.isEmpty && itemStack.isItemEqual(other) && itemStack.isStackable && (!itemStack.getHasSubtypes || (itemStack.getItemDamage == other.getItemDamage)) && ItemStack.areItemStackTagsEqual(itemStack, other) && other.getCount + itemStack.getCount <= other.getMaxStackSize
  }

  implicit class ExtRecipeItemHelper(recipeItemHelper: RecipeItemHelper) {
    def craftableRecipesIterator(): Iterator[IRecipe] = ForgeRegistries.RECIPES.filterNot(_.isDynamic).filter(recipe => recipeItemHelper.canCraft(recipe, null)).iterator
  }

  implicit class ExtInventoryBasic(invBasic: InventoryBasic) {
    // Tries to add an itemstack to an inventory by trying to stack it first, and if that fails put it in the first empty slot
    def condensedAdd(itemstackToAdd: ItemStack): ItemStack = {
      val copied = itemstackToAdd.copy()
      val size = invBasic.getSizeInventory
      var emptySlotIndex = -1
      var condensedSlotIndex = -1
      (0 until size).foreach(i => {
        val stack = invBasic.getStackInSlot(i)
        if (stack.isEmpty && emptySlotIndex == -1)
          emptySlotIndex = i
        else if (ItemStack.areItemsEqual(copied, stack) && copied.getCount + stack.getCount < copied.getMaxStackSize && condensedSlotIndex == -1)
          condensedSlotIndex = i
      })
      if (condensedSlotIndex != -1) {
        val stack = invBasic.getStackInSlot(condensedSlotIndex)
        val j = Math.min(invBasic.getInventoryStackLimit, stack.getMaxStackSize)
        val k = Math.min(copied.getCount, j - stack.getCount)

        if (k > 0) {
          stack.grow(k)
          copied.shrink(k)
          if (copied.isEmpty) {
            invBasic.markDirty()
            return ItemStack.EMPTY
          }
        }
      }
      else if (emptySlotIndex != -1) {
        invBasic.setInventorySlotContents(emptySlotIndex, copied)
        invBasic.markDirty()
        return ItemStack.EMPTY
      }
      if (copied.getCount != itemstackToAdd.getCount)
        invBasic.markDirty()

      copied
    }
  }


}
