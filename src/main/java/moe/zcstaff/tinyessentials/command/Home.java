package moe.zcstaff.tinyessentials.command;

import net.minecraft.entity.player.EntityPlayerMP;
import moe.zcstaff.tinyessentials.Lang;
import moe.zcstaff.tinyessentials.PlayerProfile;

public class Home extends BasicCommand {
  public Home() {
    super("home", Lang.usageHome);
  }

  @Override
  public void process(EntityPlayerMP entity, PlayerProfile player, String[] argString) {
    if (checkListHome(entity, player, argString)) {
      return;
    }
    String name = argString.length > 0 ? argString[0] : "home";
    if (player.homes.containsKey(name)) {
      teleportPlayer(entity, player, player.homes.get(name));
      Lang.msgHome.sendToChat(entity, name);
    } else {
      Lang.errNoHome.sendToChat(entity, name);
    }
  }

  @Override
  public boolean canUseCommand(PlayerProfile player) {
    return player.canHome();
  }
}
