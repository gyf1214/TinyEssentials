package moe.zcstaff.tinyessentials;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraft.world.WorldServer;
import net.minecraft.entity.player.EntityPlayerMP;

public class EventHandler {
  @SubscribeEvent
  public void worldSaved(WorldEvent.Save e) {
    if(e.world.provider.dimensionId == 0 && e.world instanceof WorldServer) {
      System.out.println("world saved");
      Profile.instance().saveProfile();
    }
  }

  @SubscribeEvent
  public void playerLogged(PlayerEvent.PlayerLoggedInEvent e) {
    if (e.player instanceof EntityPlayerMP) {
      System.out.println(e.player + " joined");
      Profile.instance().getPlayer((EntityPlayerMP) e.player);
    }
  }
}