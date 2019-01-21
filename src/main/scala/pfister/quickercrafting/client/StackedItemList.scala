package pfister.quickercrafting.client

import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.{IRecipe, Ingredient}

import scala.collection.JavaConversions._
import scala.collection.mutable

private[client] class StackedItemList(items: List[ItemStack]) {

  private val _backingBuffer: mutable.ListBuffer[ItemStack] = {
    val listbuffer: mutable.ListBuffer[ItemStack] = mutable.ListBuffer()
    items.foreach(itemstack => {
      if (itemstack != ItemStack.EMPTY) {
        val existingItem = listbuffer.find(canStack(itemstack, _))
        if (existingItem.isDefined) {
          existingItem.get.setCount(existingItem.get.getCount + itemstack.getCount)
        }
        else
          listbuffer.append(itemstack)
      }
    })
    listbuffer
  }

  def StackedItems: List[ItemStack] = _backingBuffer.toList

  def canCreate(recipe: IRecipe): Boolean = {
    val ingredients = recipe.getIngredients
    val mergedIngredients = mutable.ListBuffer[(Int, Ingredient)]()
    ingredients.foreach(ingr => {
      if (ingr != Ingredient.EMPTY) {
        val matching = mergedIngredients.indexWhere(ingr2 => ingr2._2.getMatchingStacks.forall(item2 => ingr.getMatchingStacks.exists(item1 => item1.getItem == item2.getItem && item1.getMetadata == item2.getMetadata)))
        if (matching != -1) {
          val size = mergedIngredients.get(matching)._1 + 1
          mergedIngredients.remove(matching)
          mergedIngredients.add((size, ingr))
        }
        else
          mergedIngredients.add((1, ingr))
      }
    })

    mergedIngredients.forall(ingr => {
      StackedItems.exists(itemstack => itemstack.getCount >= ingr._1 && ingr._2.apply(itemstack))
    })
  }

  def hasContentsOf(other: StackedItemList): Boolean =
    other.StackedItems.forall(otheritemstack => {
      StackedItems.exists(itemstack => {
        itemstack.isItemEqual(otheritemstack) && itemstack.getCount >= otheritemstack.getCount
      })
    })


  private def canStack(from: ItemStack, to: ItemStack): Boolean = !from.isEmpty && !to.isEmpty && from.isItemEqual(to) && from.isStackable && (!from.getHasSubtypes || from.getItemDamage == to.getItemDamage) && ItemStack.areItemStackTagsEqual(from, to)


}
