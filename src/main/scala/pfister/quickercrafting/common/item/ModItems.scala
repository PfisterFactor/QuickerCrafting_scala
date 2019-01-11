package pfister.quickercrafting.common.item

import net.minecraft.item.Item
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import pfister.quickercrafting.QuickerCrafting

object ModItems {
  lazy val gui_tester: ItemGuiTester = Item.REGISTRY.getObject(new ResourceLocation(QuickerCrafting.MOD_ID, ItemGuiTester.REGISTRY_NAME)).asInstanceOf[ItemGuiTester]

  @SideOnly(Side.CLIENT)
  def initModels(): Unit = {
    gui_tester.initModel()
  }
}
