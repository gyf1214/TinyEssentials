package moe.zcstaff.tinyessentials.command;

import net.minecraft.entity.player.EntityPlayerMP;
import moe.zcstaff.tinyessentials.Lang;
import moe.zcstaff.tinyessentials.PlayerProfile;

public class DelHome extends BasicCommand {
  public DelHome() {
    super("delhome", Lang.usageDelhome);
  }

  @Override
  public void process(EntityPlayerMP entity, PlayerProfile player, String[] argString) {
    if (checkListHome(entity, player, argString)) {
      return;
    }
    String name = argString.length > 0 ? argString[0] : "home";
    if (player.homes.containsKey(name)) {
      player.homes.remove(name);
      Lang.msgDelhome.sendToChat(entity, name);
    } else {
      Lang.errNoHome.sendToChat(entity, name);
    }
  }

  @Override
  public boolean canUseCommand(PlayerProfile player) {
    return player.canHome();
  }
}