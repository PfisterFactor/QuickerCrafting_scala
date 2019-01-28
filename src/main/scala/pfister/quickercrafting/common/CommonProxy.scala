package pfister.quickercrafting.common

import net.minecraft.item.Item
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.{EventBusSubscriber, EventHandler}
import net.minecraftforge.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.NetworkRegistry
import pfister.quickercrafting.QuickerCrafting
import pfister.quickercrafting.common.gui.GuiHandler
import pfister.quickercrafting.common.item.ItemGuiTester
import pfister.quickercrafting.common.network.PacketHandler
import pfister.quickercrafting.common.util.RecipeCalculator


class CommonProxy {
  // Pre Init is the place set anything up that is required by your own or other mods.
  // This stage’s event is the FMLPreInitializationEvent. Common actions to preform in preInit are:
  // * Creating and reading the config file
  // * Registering Capabilities
  @EventHandler
  def preInit(event:FMLPreInitializationEvent): Unit = {
    PacketHandler.registerMessages()
  }

  // Init is where to accomplish any game related tasks that rely upon the items and blocks set up in preInit.
  // This stage’s event is the FMLInitializationEvent. Common actions to preform in init are:
  // * Registering world generators
  // * Registering event handlers
  // * Sending IMC messages
  @EventHandler
  def init(event:FMLInitializationEvent): Unit = {
    NetworkRegistry.INSTANCE.registerGuiHandler(QuickerCrafting, GuiHandler)
    // Force load RecipeCalculator to sort recipes prematurely
    RecipeCalculator.SortedRecipes
  }

  // Post Init is where your mod usually does things which rely upon other mods.
  // This stage’s event is the FMLPostInitializationEvent. Common actions to preform in postInit are:
  // * Mod compatibility, or anything which depends on other mods’ init phases being finished.
  @EventHandler
  def postInit(event: FMLPostInitializationEvent): Unit = {

  }



}
@EventBusSubscriber
object CommonEventListener {
  @SubscribeEvent
  def registerItems(event: RegistryEvent.Register[Item]): Unit = {
    event.getRegistry.register(new ItemGuiTester)
  }
}
