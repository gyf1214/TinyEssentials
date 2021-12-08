package moe.zcstaff.tinyessentials.command;

import net.minecraft.entity.player.EntityPlayerMP;
import moe.zcstaff.tinyessentials.Lang;
import moe.zcstaff.tinyessentials.PlayerProfile;

public class Back extends BasicCommand {
  public Back() {
    super("back", Lang.usageBack);
  }

  @Override
  public void process(EntityPlayerMP entity, PlayerProfile player, String[] argString) {
    if (player.lastPos != null) {
      teleportPlayer(entity, player, player.lastPos);
      Lang.msgBack.sendToChat(entity);
    } else {
      Lang.errNoBack.sendToChat(entity);
    }
  }

  @Override
  public boolean canUseCommand(PlayerProfile player) {
    return player.canBack();
  }
}
