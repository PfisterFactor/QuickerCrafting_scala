package pfister.quickercrafting.common.gui

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.World
import net.minecraftforge.fml.common.network.IGuiHandler
import pfister.quickercrafting.client.gui.GuiQuickerCrafting

object GuiHandler extends IGuiHandler {
  override def getServerGuiElement(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): Object = {
    ID match {
      case 0 => new ContainerQuickerCrafting(player.inventory)
    }
  }

  override def getClientGuiElement(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): Object = {
    ID match {
      case 0 => new GuiQuickerCrafting(player.inventory)
    }

  }
}
