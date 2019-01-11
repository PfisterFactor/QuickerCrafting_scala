package pfister.quickercrafting.common

import net.minecraft.item.Item
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.{EventBusSubscriber, EventHandler}
import net.minecraftforge.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import pfister.quickercrafting.common.item.ItemGuiTester


class CommonProxy {

  @EventHandler
  def preInit(event:FMLPreInitializationEvent): Unit = {

  }
  @EventHandler
  def init(event:FMLInitializationEvent): Unit = {

  }
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
