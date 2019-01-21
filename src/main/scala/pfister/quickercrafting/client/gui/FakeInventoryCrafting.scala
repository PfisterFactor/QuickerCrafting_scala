package pfister.quickercrafting.client.gui

import net.minecraft.client.util.RecipeItemHelper
import net.minecraft.inventory.{Container, InventoryCrafting}
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipe

import scala.util.Try

class FakeInventoryCrafting(container: Container) extends InventoryCrafting(container, 3, 3) {
  private var fakedRecipe: IRecipe = _

  def setFakedRecipe(recipe: IRecipe) = {
    fakedRecipe = recipe
  }

  override def getStackInSlot(index: Int): ItemStack = {
    Try(fakedRecipe.getIngredients.get(index).getMatchingStacks.head).getOrElse(ItemStack.EMPTY)
  }

  override def removeStackFromSlot(index: Int): ItemStack = ItemStack.EMPTY

  override def setInventorySlotContents(index: Int, stack: ItemStack): Unit = {}

  override def decrStackSize(index: Int, count: Int): ItemStack = ItemStack.EMPTY

  override def fillStackedContents(helper: RecipeItemHelper): Unit = {}

}
