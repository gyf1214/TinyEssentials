package moe.zcstaff.tinyessentials.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import moe.zcstaff.tinyessentials.Lang;
import moe.zcstaff.tinyessentials.Profile;
import moe.zcstaff.tinyessentials.PlayerProfile;
import moe.zcstaff.tinyessentials.BackupUtil;

public class Admin extends BasicCommand {
  public Admin() {
    super("admin", Lang.usageAdmin, true);
  }

  @Override
  public void process(EntityPlayerMP entity, PlayerProfile player, String[] argString) {
    processConsole(entity, argString);
  }

  @Override
  public void processConsole(ICommandSender sender, String[] argString) {
    if (argString.length <= 0) {
      Lang.errMissArg.sendToChat(sender);
      return;
    }
    if (argString[0].equals("reload")) {
      Profile.instance().loadConfig();
      Lang.msgReload.sendToChat(sender);
    } else if (argString[0].equals("backup") || argString[0].equals("incBackup")) {
      long newBackup = BackupUtil.getNewBackup();
      if (!BackupUtil.instance().startBackup(newBackup, argString[0].equals("incBackup"))) {
        Lang.errHasBackup.sendToChat(sender);
      }
    } else {
      Lang.errMissArg.sendToChat(sender);
    }
  }

  @Override
  public boolean canUseCommand(PlayerProfile player) {
    return player.isOp();
  }
}
