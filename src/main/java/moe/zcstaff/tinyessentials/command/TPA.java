package moe.zcstaff.tinyessentials.command;

import net.minecraft.entity.player.EntityPlayerMP;
import moe.zcstaff.tinyessentials.Lang;
import moe.zcstaff.tinyessentials.PlayerProfile;
import moe.zcstaff.tinyessentials.DimUtil;

public class TPA extends BasicCommand {
  public TPA() {
    super("tpa", Lang.usageTP);
  }

  @Override
  public void process(EntityPlayerMP entity, PlayerProfile player, String[] argString) {
    if (argString.length <= 0) {
      Lang.errMissArg.sendToChat(entity);
      return;
    }
    String name = argString[0];
    EntityPlayerMP target = DimUtil.findPlayer(name);
    if (target == null) {
      Lang.errNoPlayer.sendToChat(entity, name);
      return;
    }
    DimUtil.teleportEntity(entity, DimUtil.getEntityDimPos(target));
    Lang.msgTP.sendToChat(entity, name);
    Lang.msgTPNotify.sendToChat(target, player.name);
  }

  @Override
  public boolean canUseCommand(PlayerProfile player) {
    return player.canTP();
  }
}
