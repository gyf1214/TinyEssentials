package moe.zcstaff.tinyessentials.command;

import cpw.mods.fml.common.event.FMLServerStartingEvent;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayerMP;
import moe.zcstaff.tinyessentials.DimUtil;
import moe.zcstaff.tinyessentials.DimPos;
import moe.zcstaff.tinyessentials.Lang;
import moe.zcstaff.tinyessentials.Profile;
import moe.zcstaff.tinyessentials.PlayerProfile;
import moe.zcstaff.tinyessentials.Lang;

import java.util.List;
import java.util.ArrayList;

public abstract class BasicCommand extends CommandBase {
  protected final String name, usage;
  protected final List<String> aliases = new ArrayList<String>();
  protected final boolean allowConsole;

  public BasicCommand(String name, Lang usage) {
    this(name, usage, false);
  }

  public BasicCommand(String name, Lang usage, boolean allowConsole) {
    this.name = name;
    this.usage = usage.text();
    this.allowConsole = allowConsole;
  }
  
  @Override
  public String getCommandName() {
    return name;
  }

  @Override
  public String getCommandUsage(ICommandSender var1) {
    return usage;
  }

  @Override
  public List<String> getCommandAliases() {
    return aliases;
  }

  @Override
  public void processCommand(ICommandSender sender, String[] argString) {
    World world = sender.getEntityWorld();
    if (world.isRemote) {
      System.out.println("Not processing on Client side");
    } else if (sender instanceof EntityPlayerMP) {
      EntityPlayerMP entity = (EntityPlayerMP)sender;
      PlayerProfile player = Profile.instance().getPlayer(entity);
      process(entity, player, argString);
    } else {
      process(null, null, argString);
    }
  }

  public abstract void process(EntityPlayerMP entity, PlayerProfile player, String[] argString);

  @Override
  public boolean canCommandSenderUseCommand(ICommandSender sender) {
    if (sender instanceof EntityPlayerMP) {
      return canUseCommand(Profile.instance().getPlayer((EntityPlayerMP)sender));
    } else {
      return allowConsole;
    }
  }

  public abstract boolean canUseCommand(PlayerProfile player);

  protected static final String defaultHome = "home";

  protected boolean checkListHome(EntityPlayerMP entity, PlayerProfile player, String[] argString) {
    if (argString.length <= 0 && player.homes.size() > 0 && 
       (player.homes.size() > 1 || !player.homes.containsKey(defaultHome))) {
      Lang.msgListHome.sendToChat(entity, String.join(", ", player.homes.keySet().toArray(new String[0])));
      return true;
    }
    return false;
  }

  protected void teleportPlayer(EntityPlayerMP entity, PlayerProfile player, DimPos pos) {
    player.lastPos = DimUtil.getEntityDimPos(entity);
    DimUtil.teleportEntity(entity, pos);
  }

  public static void registerCommands(FMLServerStartingEvent e) {
    e.registerServerCommand(new Home());
    e.registerServerCommand(new SetHome());
    e.registerServerCommand(new DelHome());
    e.registerServerCommand(new Back());
  }
}
