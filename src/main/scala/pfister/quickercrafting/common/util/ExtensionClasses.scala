package pfister.quickercrafting.common.util

import net.minecraft.client.util.RecipeItemHelper
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipe
import net.minecraftforge.fml.common.registry.ForgeRegistries

import scala.collection.JavaConversions._

object ExtensionClasses {

  implicit class ExtItemStack(itemStack: ItemStack) {
    def canStack(other: ItemStack): Boolean = !itemStack.isEmpty && !other.isEmpty && itemStack.isItemEqual(other) && itemStack.isStackable && (!itemStack.getHasSubtypes || (itemStack.getItemDamage == other.getItemDamage)) && ItemStack.areItemStackTagsEqual(itemStack, other)
  }

  implicit class ExtRecipeItemHelper(recipeItemHelper: RecipeItemHelper) {
    def craftableRecipesIterator(): Iterator[IRecipe] = ForgeRegistries.RECIPES.filterNot(_.isDynamic).filter(recipe => recipeItemHelper.canCraft(recipe, null)).iterator
  }


}
