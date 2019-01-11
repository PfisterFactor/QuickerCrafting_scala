package pfister.quickercrafting.common.item

import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.{ActionResult, EnumActionResult, EnumHand}
import net.minecraft.world.World
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import pfister.quickercrafting.QuickerCrafting

object ItemGuiTester {
  final val REGISTRY_NAME = "gui_tester"
}
class ItemGuiTester extends Item {
  setRegistryName(ItemGuiTester.REGISTRY_NAME)
  setUnlocalizedName(s"${QuickerCrafting.MOD_ID}.${ItemGuiTester.REGISTRY_NAME}")
  setCreativeTab(CreativeTabs.MISC)

  @SideOnly(Side.CLIENT)
  def initModel(): Unit = {
    ModelLoader.setCustomModelResourceLocation(this,0,new ModelResourceLocation(Items.STICK.getRegistryName,"inventory"))
  }

  override def onItemRightClick(world: World, playerIn: EntityPlayer, handIn: EnumHand): ActionResult[ItemStack] = {
    playerIn.openGui(QuickerCrafting, 0, world, playerIn.posX.toInt, playerIn.posY.toInt, playerIn.posZ.toInt)
    new ActionResult[ItemStack](EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn))
  }

}
