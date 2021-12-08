package moe.zcstaff.tinyessentials;

import cpw.mods.fml.common.FMLCommonHandler;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

public class PlayerProfile {
  public UUID uuid;
  public String name;
  public Map<String, DimPos> homes = new HashMap<String, DimPos>();
  public DimPos lastPos = null;

  public PlayerProfile(GameProfile p) {
    uuid = p.getId();
    name = p.getName();
  }

  public PlayerProfile(NBTTagCompound tag) {
    readFromNBT(tag);
  }

  public boolean isOp() {
    return FMLCommonHandler.instance().getMinecraftServerInstance()
      .getConfigurationManager().func_152603_m().func_152700_a(name) != null;
  }

  public boolean canBack() {
    return isOp() || Profile.instance().enableBack;
  }

  public boolean canHome() {
    return isOp() || Profile.instance().enableHome;
  }

  public boolean canTP() {
    return isOp() || Profile.instance().enableTP;
  }

  public boolean canSetHome(String name) {
    int size = homes.containsKey(name) ? homes.size() : homes.size() + 1;
    return isOp() || (Profile.instance().enableHome && size <= Profile.instance().homeLimit);
  }

  public void readFromNBT(NBTTagCompound tag) {
    if (tag.hasKey("uuid") && tag.func_150299_b("uuid") == Constants.NBT.TAG_STRING) {
      uuid = UUID.fromString(tag.getString("uuid"));
    }
    if (tag.hasKey("name") && tag.func_150299_b("name") == Constants.NBT.TAG_STRING) {
      name = tag.getString("name");
    }

    if (tag.hasKey("lastPos") && tag.func_150299_b("lastPos") == Constants.NBT.TAG_INT_ARRAY) {
      lastPos = new DimPos(tag.getIntArray("lastPos"));
    }

    homes.clear();
    if (tag.hasKey("homes") && tag.func_150299_b("homes") == Constants.NBT.TAG_COMPOUND) {
      NBTTagCompound tagHomes = tag.getCompoundTag("homes");
      if (tagHomes != null && !tagHomes.hasNoTags()) {
        for (Object s : tagHomes.func_150296_c()) {
          String name = s.toString();
          if (tagHomes.func_150299_b(name) == Constants.NBT.TAG_INT_ARRAY) {
            homes.put(name, new DimPos(tagHomes.getIntArray(name)));
          }
        }
      }
    }
  }

  public void saveToNBT(NBTTagCompound tag) {
    tag.setString("uuid", uuid.toString());
    tag.setString("name", name);
    if (lastPos != null) {
      tag.setIntArray("lastPos", lastPos.toIntArray());
    }

    NBTTagCompound tagHomes = new NBTTagCompound();
    for (Map.Entry<String, DimPos> e : homes.entrySet()) {      
      tagHomes.setIntArray(e.getKey(), e.getValue().toIntArray());
    }
    tag.setTag("homes", tagHomes);
  }
}
