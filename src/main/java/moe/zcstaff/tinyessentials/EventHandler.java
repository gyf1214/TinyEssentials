package moe.zcstaff.tinyessentials;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.world.WorldServer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class EventHandler {
  private int tick = 0;

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

  @SubscribeEvent
	public void playerDeath(LivingDeathEvent e) {
    if (e.entity instanceof EntityPlayerMP) {
      PlayerProfile p = Profile.instance().getPlayer((EntityPlayerMP) e.entity);
      p.lastPos = DimUtil.getEntityDimPos(e.entity);
    }
  }

  @SubscribeEvent
  public void serverTick(TickEvent.ServerTickEvent e) {
    if (tick == 0) {
      BackupUtil.instance().checkBackup();
    }
    tick = (tick + 1) % 20;
  }
}