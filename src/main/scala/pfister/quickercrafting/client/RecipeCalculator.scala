package pfister.quickercrafting.client

import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.init.Items
import net.minecraft.item.crafting.IRecipe
import net.minecraftforge.fml.common.registry.ForgeRegistries

import scala.collection.JavaConversions._


class RecipeCalculator(private var PlayerInv: InventoryPlayer) {
  val RECIPES = ForgeRegistries.RECIPES
  val cachedStackedInventory: StackedItemList = new StackedItemList(PlayerInv.mainInventory.toList)

  def updatePlayerInv(playerInv: InventoryPlayer): Unit = {
    PlayerInv = playerInv
  }

  def canCraft(recipe: IRecipe): Boolean = {
    cachedStackedInventory.canCreate(recipe)
  }

  def getRecipeIterator(): Iterator[IRecipe] = {
    RECIPES.filter(recipe => {
      recipe.getRecipeOutput.getItem != Items.AIR && canCraft(recipe)
    }).toIterator
  }
}
