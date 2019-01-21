package pfister.quickercrafting

import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import net.minecraftforge.fml.common.{Mod, SidedProxy}
import org.apache.logging.log4j.Logger
import pfister.quickercrafting.common.CommonProxy


@Mod(modid = QuickerCrafting.MOD_ID,name = QuickerCrafting.NAME,version = QuickerCrafting.VERSION,modLanguage = "scala")
object QuickerCrafting {
  final val NAME = "Quicker Crafting"
  final val MOD_ID = "quickercrafting"
  final val VERSION = "0.1"

  var Log:Logger = _
  @SidedProxy(clientSide = "pfister.quickercrafting.client.ClientProxy",serverSide = "pfister.quickercrafting.common.CommonProxy")
  var proxy: CommonProxy = _

  @EventHandler def preInit(event:FMLPreInitializationEvent): Unit = {
    Log = event.getModLog
    proxy.preInit(event)
  }
  @EventHandler def init(event:FMLInitializationEvent): Unit = proxy.init(event)
  @EventHandler def postInit(event: FMLPostInitializationEvent): Unit = proxy.postInit(event)

  // Debug usage
  def time[R](block: => R): (Long, R) = {
    val t0 = System.currentTimeMillis()
    val result = block // call-by-name
    val t1 = System.currentTimeMillis()
    (t1 - t0, result)
  }

}
