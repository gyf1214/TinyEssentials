package moe.zcstaff.tinyessentials;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public final class Lang {
  private final String id;

  private Lang(String id) {
    this.id = id;
  }

  public String toText(Object... o) {
    ChatComponentTranslation msg = new ChatComponentTranslation(id, o);
    return msg.getUnformattedTextForChat();
  }

  public IChatComponent toChat(Object... o) {
    return new ChatComponentText(toText(o));
  }

  public void sendToChat(ICommandSender entity, Object... o) {
    entity.addChatMessage(toChat(o));
  }

  public static Lang get(String sub) {
    return new Lang("essentials." + sub);
  }

  public static Lang usageHome     = get("usage.home");
  public static Lang usageSethome  = get("usage.sethome");
  public static Lang usageDelhome  = get("usage.delhome");
  public static Lang usageBack     = get("usage.back");
  public static Lang usageTP       = get("usage.tp");
  public static Lang usageSuicide  = get("usage.suicide");
  public static Lang usageAdmin    = get("usage.admin");

  public static Lang msgHome       = get("msg.home");
  public static Lang msgSetHome    = get("msg.sethome");
  public static Lang msgListHome   = get("msg.listhome");
  public static Lang msgDelhome    = get("msg.delhome");
  public static Lang msgBack       = get("msg.back");
  public static Lang msgTP         = get("msg.tp");
  public static Lang msgTPNotify   = get("msg.tpNotify");
  public static Lang msgSuicide    = get("msg.suicide");
  public static Lang msgReload     = get("msg.reload");

  public static Lang errNoHome     = get("err.noHome");
  public static Lang errHomeExceed = get("err.homeExceed");
  public static Lang errNoBack     = get("err.noBack");
  public static Lang errMissArg    = get("err.missArg");
  public static Lang errNoPlayer   = get("err.noPlayer");
  public static Lang errHasBackup  = get("err.hasBackup");
}
