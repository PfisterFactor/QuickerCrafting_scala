package pfister.quickercrafting.common.network

import io.netty.buffer.ByteBuf
import net.minecraft.item.crafting.IRecipe
import net.minecraft.network.PacketBuffer
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.network.simpleimpl.{IMessage, IMessageHandler, MessageContext}
import net.minecraftforge.fml.common.registry.ForgeRegistries
import pfister.quickercrafting.QuickerCrafting
import pfister.quickercrafting.client.RecipeCalculator

import scala.util.Try

class MessageCraftItem(private var recipe: IRecipe) extends IMessage {
  def this() = this(null)

  private var recipeString = if (recipe != null) recipe.getRegistryName.toString else ""

  def RecipeString: String = recipeString

  def Recipe: IRecipe = recipe

  override def toBytes(buf: ByteBuf): Unit = {
    new PacketBuffer(buf).writeString(recipeString)

  }

  override def fromBytes(buf: ByteBuf): Unit = {
    val recipeName = new PacketBuffer(buf).readString(50)
    recipeString = recipeName
    val recipe = Try(ForgeRegistries.RECIPES.getValue(new ResourceLocation(recipeString)))
    if (recipe.isSuccess)
      this.recipe = recipe.get
    else
      this.recipe = null

  }


}

class MessageCraftItemHandler extends IMessageHandler[MessageCraftItem, IMessage] {

  override def onMessage(message: MessageCraftItem, ctx: MessageContext): IMessage = {
    if (message.Recipe == null) {
      QuickerCrafting.Log.warn(s"MessageCraftItemHandler: Recipe '${message.RecipeString}' cannot be found.")
      return null
    }

    val player = ctx.getServerHandler.player
    val recipeCalculator = new RecipeCalculator(player.inventory)

    val itemsToRemove = recipeCalculator.tryCraftRecipe(message.Recipe)

    if (itemsToRemove.isEmpty) {
      QuickerCrafting.Log.warn(s"MessageCraftItemHandler: Recipe '${message.RecipeString}' cannot be crafted from ${player.getName}'s inventory on server.")
      return null
    }
    itemsToRemove.get.foreach(pair => {
      player.inventory.decrStackSize(pair._1, pair._2)
    })
    val recipeOutput = message.Recipe.getRecipeOutput.copy()
    if (!player.inventory.addItemStackToInventory(recipeOutput)) {
      player.dropItem(recipeOutput, false)
    }

    null
  }
}
