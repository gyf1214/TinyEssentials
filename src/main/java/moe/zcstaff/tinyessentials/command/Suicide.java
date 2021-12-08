package moe.zcstaff.tinyessentials.command;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.IChatComponent;
import moe.zcstaff.tinyessentials.PlayerProfile;
import moe.zcstaff.tinyessentials.Lang;

public class Suicide extends BasicCommand {
  public Suicide() {
    super("suicide", Lang.usageSuicide);
  }

  private static class SuicideDamageSource extends DamageSource {
    public SuicideDamageSource() {
			super("suicide");
			setDamageBypassesArmor();
			setDamageAllowedInCreativeMode();
      setDamageIsAbsolute();
		}

    @Override
		public IChatComponent func_151519_b(EntityLivingBase entity) {
      String name = "";
      if (entity instanceof EntityPlayerMP) {
        name = ((EntityPlayerMP)entity).getGameProfile().getName();
      }
      return Lang.msgSuicide.toChat(name);
		}
  }

  private final DamageSource suicideDamage = new SuicideDamageSource();

  @Override
  public void process(EntityPlayerMP entity, PlayerProfile player, String[] argString) {
    entity.attackEntityFrom(suicideDamage, 999999999);
  }

  @Override
  public boolean canUseCommand(PlayerProfile player) {
    return true;
  }
}
