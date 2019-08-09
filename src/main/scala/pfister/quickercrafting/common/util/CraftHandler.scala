package pfister.quickercrafting.common.util

import net.minecraft.item.crafting.IRecipe
import pfister.quickercrafting.QuickerCrafting
import pfister.quickercrafting.common.gui.ContainerQuickerCrafting
import pfister.quickercrafting.common.util.ExtensionClasses.ExtInventoryBasic

// Handles the functionality for crafting something
object CraftHandler {
  // Attempts to craft a recipe given the items within the container
  def tryCraftRecipe(container: ContainerQuickerCrafting, recipe: IRecipe): Boolean = {
    val recipeCalculator = new RecipeCalculator(container)
    val isServer = !container.PlayerInv.player.world.isRemote
    // Get a map of items used and how much are used
    val itemsToRemove = recipeCalculator.doCraft(recipe)

    if (itemsToRemove.isEmpty) {
      if (isServer)
        QuickerCrafting.Log.warn(s"MessageCraftItemHandler: Recipe '${recipe.getRegistryName.toString}' cannot be crafted from ${container.PlayerInv.player.getDisplayNameString}'s inventory on server.")
      return false
    }

    if (!container.canFitStackInCraftResult(recipe.getRecipeOutput)) {
      // Detect to see if after crafting there will be an open slot in the craft result slots, if there is we can put the item there
      val willCraftResultItemBeConsumed = itemsToRemove.get.exists(pair => {
        container.isCraftResultIndex(pair._1) && container.getSlot(pair._1).getStack.getCount <= pair._2
      })
      if (!willCraftResultItemBeConsumed) {
        if (isServer)
          QuickerCrafting.Log.warn(s"MessageCraftItemHandler: Cannot stack '${recipe.getRegistryName.toString}' into item slot on server.")
        return false
      }
    }
    // Remove all items used during crafting
    itemsToRemove.get.foreach(pair => {
      val slot = container.getSlot(pair._1)
      slot.decrStackSize(pair._2)
    })

    // Get the recipe output itemstack
    // Todo: This will not work on special recipes (Repairing, Cloning Books, Fireworks, etc...)
    val recipeOutput = recipe.getRecipeOutput.copy()
    val size = container.craftResult.getSizeInventory
    val leftOver = container.craftResult.condensedAdd(recipeOutput)
    if (!leftOver.isEmpty) {
      container.PlayerInv.player.dropItem(recipeOutput, false)
    }
    true
  }
}
