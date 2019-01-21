package pfister.quickercrafting.client

import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.Container
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.{IRecipe, Ingredient}
import net.minecraftforge.fml.common.registry.ForgeRegistries

import scala.collection.JavaConversions._
import scala.collection.mutable


class RecipeCalculator(playerInv: InventoryPlayer) {
  val RECIPES = ForgeRegistries.RECIPES

  // A copy of the player's inventory to avoid modifying itemstacks in the players inventory
  private val recipeWorkingInv: Array[ItemStack] = Array.fill(36)(ItemStack.EMPTY)

  def updateWorkingInv(container: Container, slotInd: Int, stack: ItemStack): Unit = {
    val index = container.inventorySlots.get(slotInd).slotNumber
    recipeWorkingInv(index) = new ItemStack(stack.getItem, stack.getCount, stack.getMetadata, stack.getTagCompound)
  }

  // Attempts to craft a recipe using the players inventory
  // If success, returns a list of indexes to the players inventory corresponding to ingredients used and amount used
  // If failure, returns None
  def tryCraftRecipe(recipe: IRecipe): Option[Map[Int, Int]] = {
    // A map of all the items and their amounts used in the recipe
    val usedItemMap: mutable.Map[Int, Int] = mutable.Map()

    val returnVal = recipe.getIngredients.filterNot(_ == Ingredient.EMPTY).forall(ingr => {
      // Find an itemstack index where the count is greater than 0 and the ingredient accepts the itemstack for crafting
      val index = recipeWorkingInv.indexWhere(itemstack => itemstack.getCount > 0 && ingr.apply(itemstack))
      if (index != -1) {
        val item = recipeWorkingInv(index)
        // Decrement the items count, it can be negative because we fix the item amounts in the end
        item.setCount(item.getCount - 1)
        // Mark the item needed and how much of it would be needed
        usedItemMap.update(index, usedItemMap.getOrDefault(index, 0) + 1)
        true
      }
      else
        false
    })
    // Fix the item amounts on our working inventory
    usedItemMap.foreach(pair => {
      val itemstack = recipeWorkingInv(pair._1)
      itemstack.setCount(itemstack.getCount + pair._2)
    })
    if (returnVal)
      Some(usedItemMap.toMap)
    else
      None
  }

  // Determines if the inventory can craft a recipe
  def canCraft(recipe: IRecipe): Boolean = tryCraftRecipe(recipe).isDefined

  def getRecipeIterator(): Iterator[IRecipe] = {
    RECIPES.filter(recipe => {
      !recipe.getRecipeOutput.isEmpty && canCraft(recipe)
    }).toIterator
  }
}
