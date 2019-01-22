package pfister.quickercrafting.common.network

import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper
import net.minecraftforge.fml.relauncher.Side

object PacketHandler {
  final val INSTANCE: SimpleNetworkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel("quickercrafting")

  def registerMessages(): Unit = {
    INSTANCE.registerMessage(classOf[MessageCraftItemHandler], classOf[MessageCraftItem], 0, Side.SERVER)
  }
}
