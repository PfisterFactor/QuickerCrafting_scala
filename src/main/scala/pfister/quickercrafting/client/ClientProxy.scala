package pfister.quickercrafting.client

import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import pfister.quickercrafting.common.CommonProxy
import pfister.quickercrafting.common.item.ModItems

class ClientProxy extends CommonProxy {
  // Pre Init is the place set anything up that is required by your own or other mods.
  // This stage’s event is the FMLPreInitializationEvent. Common actions to preform in preInit are:
  // * Creating and reading the config file
  // * Registering Capabilities
  override def preInit(event: FMLPreInitializationEvent): Unit = {
    super.preInit(event)
  }
  // Init is where to accomplish any game related tasks that rely upon the items and blocks set up in preInit.
  // This stage’s event is the FMLInitializationEvent. Common actions to preform in init are:
  // * Registering world generators
  // * Registering event handlers
  // * Sending IMC messages
  override def init(event: FMLInitializationEvent): Unit = {
    super.init(event)
  }
  // Post Init is where your mod usually does things which rely upon other mods.
  // This stage’s event is the FMLPostInitializationEvent. Common actions to preform in postInit are:
  // * Mod compatibility, or anything which depends on other mods’ init phases being finished.
  override def postInit(event: FMLPostInitializationEvent): Unit = {
    super.postInit(event)
  }
}

@EventBusSubscriber(Array(Side.CLIENT))
object ClientEventListener {
  @SubscribeEvent
  def registerItemModels(event: ModelRegistryEvent): Unit = {
    ModItems.initModels()
  }
}
