package pfister.quickercrafting.common.util

import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.item._
import net.minecraft.item.crafting.{IRecipe, Ingredient}
import net.minecraftforge.fml.common.registry.ForgeRegistries

import scala.collection.JavaConversions._
import scala.collection.mutable

object RecipeCalculator {

  // Sorts the recipe registry for convenient display
  // Very verbose and ugly :(
  val SortedRecipes: Array[IRecipe] = ForgeRegistries.RECIPES.getValuesCollection.toSeq
    .filterNot(_.isDynamic)
    .sorted(new Ordering[IRecipe] {
      override def compare(x: IRecipe, y: IRecipe): Int = {
        val itemX = x.getRecipeOutput.getItem
        val itemY = y.getRecipeOutput.getItem

        // Blocks are first
        val xIsBlock = itemX.isInstanceOf[ItemBlock] || itemX.isInstanceOf[ItemDoor]
        val yIsBlock = itemY.isInstanceOf[ItemBlock] || itemY.isInstanceOf[ItemDoor]

        if (xIsBlock && !yIsBlock) return -1
        else if (yIsBlock && !xIsBlock) return 1
        else if (xIsBlock && yIsBlock) {
          // Special check for doors
          if (itemX.isInstanceOf[ItemDoor] && !itemY.isInstanceOf[ItemDoor]) return 1
          else if (!itemX.isInstanceOf[ItemDoor] && itemY.isInstanceOf[ItemDoor]) return -1
          else if (itemX.isInstanceOf[ItemDoor] && itemY.isInstanceOf[ItemDoor]) return 0

          val blockX = itemX.asInstanceOf[ItemBlock].getBlock
          val blockY = itemY.asInstanceOf[ItemBlock].getBlock
          // Full blocks should be put first
          val isFullBlockX = blockX.isFullBlock(blockX.getDefaultState)
          val isFullBlockY = blockY.isFullBlock(blockY.getDefaultState)
          if (isFullBlockX && !isFullBlockY)
            return -1
          else if (!isFullBlockX && isFullBlockY)
            return 1
          // Otherwise just do a default comparison
          var comparison = blockX.getClass.getName.compareTo(blockY.getClass.getName)
          if (comparison == 0)
            comparison = blockX.getUnlocalizedName.compareTo(blockY.getUnlocalizedName)
          return comparison
        }
        // Next is damagable items, typically tools, swords, hoes, armor, etc...
        val xIsDamagable = itemX.isDamageable
        val yIsDamagable = itemY.isDamageable

        if (xIsDamagable && !yIsDamagable) return -1
        else if (yIsDamagable && !xIsDamagable) return 1

        // Lastly a default check between class names and, failing that, their unlocalized names
        var comparison = itemX.getClass.getName.compareTo(itemY.getClass.getName)
        if (comparison == 0)
          comparison = itemX.getUnlocalizedName.compareTo(itemY.getUnlocalizedName)

        comparison
      }
    }).toArray
}
class RecipeCalculator(playerInv: InventoryPlayer) {

  // A copy of the player's inventory to avoid modifying itemstacks in the players inventory
  private val recipeWorkingInv: Array[ItemStack] = playerInv.mainInventory.map(_.copy).toArray
  def updateWorkingInv(index: Int, stack: ItemStack): Unit = {
    recipeWorkingInv(index) = stack.copy()
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
    RecipeCalculator.SortedRecipes.toIterator.filter(recipe => {
      canCraft(recipe)
    })
  }
}
