package pfister.quickercrafting.common.util

import net.minecraft.item.crafting.IRecipe
import pfister.quickercrafting.QuickerCrafting
import pfister.quickercrafting.common.gui.ContainerQuickerCrafting

object CraftHandler {
  def tryCraftRecipe(container: ContainerQuickerCrafting, recipe: IRecipe): Boolean = {
    val recipeCalculator = new RecipeCalculator(container)
    val isServer = !container.PlayerInv.player.world.isRemote
    val itemsToRemove = recipeCalculator.doCraft(recipe)

    if (itemsToRemove.isEmpty) {
      if (isServer)
        QuickerCrafting.Log.warn(s"MessageCraftItemHandler: Recipe '${recipe.getRegistryName.toString}' cannot be crafted from ${container.PlayerInv.player.getDisplayNameString}'s inventory on server.")
      return false
    }

    if (!container.canFitStackInCraftResult(recipe.getRecipeOutput)) {
      val willCraftResultItemBeConsumed = itemsToRemove.get.exists(pair => {
        container.isCraftResultIndex(pair._1) && container.getSlot(pair._1).getStack.getCount <= pair._2
      })
      if (!willCraftResultItemBeConsumed) {
        if (isServer)
          QuickerCrafting.Log.warn(s"MessageCraftItemHandler: Cannot stack '${recipe.getRegistryName.toString}' into item slot on server.")
        return false
      }
    }

    itemsToRemove.get.foreach(pair => {
      val slot = container.getSlot(pair._1)
      slot.decrStackSize(pair._2)
    })
    val recipeOutput = recipe.getRecipeOutput.copy()
    val leftOver = container.craftResult.addItem(recipeOutput)
    if (!leftOver.isEmpty) {
      container.PlayerInv.player.dropItem(recipeOutput, false)
    }
    true
  }
}
