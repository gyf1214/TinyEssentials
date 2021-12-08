package moe.zcstaff.tinyessentials.command;

import net.minecraft.entity.player.EntityPlayerMP;
import moe.zcstaff.tinyessentials.DimUtil;
import moe.zcstaff.tinyessentials.DimPos;
import moe.zcstaff.tinyessentials.Lang;
import moe.zcstaff.tinyessentials.Profile;
import moe.zcstaff.tinyessentials.PlayerProfile;

public class SetHome extends BasicCommand {
  public SetHome() {
    super("sethome", Lang.usageSethome);
  }

  @Override
  public void process(EntityPlayerMP entity, PlayerProfile player, String[] argString) {
    if (checkListHome(entity, player, argString)) {
      return;
    }
    String name = argString.length > 0 ? argString[0] : "home";
    if (player.canSetHome(name)) {
      DimPos pos = DimUtil.getEntityDimPos(entity);
      player.homes.put(name, pos);
      Lang.msgSetHome.sendToChat(entity, name, pos);
    } else {
      Lang.errHomeExceed.sendToChat(entity, Profile.instance().homeLimit);
    }
  }

  @Override
  public boolean canUseCommand(PlayerProfile player) {
    return player.canHome();
  }
}