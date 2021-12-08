package moe.zcstaff.tinyessentials;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.Constants;

import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

public class Profile {
  private static Profile inst = null;
  private static final String configFile = "essentials.cfg";
  private static final String profile = "profile.dat";
  
  private File configDir;

  public int homeLimit;
  public boolean enableHome;
  public boolean enableBack;
  public boolean enableTP;
  public Map<UUID, PlayerProfile> players = new HashMap<UUID, PlayerProfile>();

  private Profile(File configDir) {
    this.configDir = configDir;
    if (!configDir.exists()) {
      configDir.mkdirs();
    }
    loadConfig();
  }

  public static Profile instance() {
    return inst;
  }

  public static void preInit(File configDir) {
    inst = new Profile(configDir);
  }

  public void loadConfig() {
    Configuration config = new Configuration(new File(configDir, configFile));
    config.load();
    homeLimit = config.get(Configuration.CATEGORY_GENERAL, "home_limit", 1).getInt();
    enableHome = config.get(Configuration.CATEGORY_GENERAL, "enable_home", true).getBoolean();
    enableBack = config.get(Configuration.CATEGORY_GENERAL, "enable_back", true).getBoolean();
    enableTP = config.get(Configuration.CATEGORY_GENERAL, "enable_tp", true).getBoolean();
    config.save();
  }

  public void loadProfile() {
    File f = new File(configDir, profile);
    if (f != null && f.exists()) {
      NBTTagCompound tag = null;
      try {
        tag = CompressedStreamTools.read(f);
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (tag != null) {
        readFromNBT(tag);
      }
    }
  }

  public void saveProfile() {
    File f = new File(configDir, profile);
    NBTTagCompound tag = new NBTTagCompound();
    saveToNBT(tag);
    try {
      CompressedStreamTools.write(tag, f);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void readFromNBT(NBTTagCompound tag) {
    players.clear();
    if (tag.hasKey("players") && tag.func_150299_b("players") == Constants.NBT.TAG_LIST) {
      NBTTagList list = tag.getTagList("players", Constants.NBT.TAG_COMPOUND);
      for (int i = 0; i < list.tagCount(); i++) {
        NBTTagCompound playerTag = list.getCompoundTagAt(i);
        PlayerProfile player = new PlayerProfile(playerTag);
        if (player.uuid != null) {
          players.put(player.uuid, player);
        }
      }
    }
  }

  public void saveToNBT(NBTTagCompound tag) {
    NBTTagList list = new NBTTagList();
    for (Map.Entry<UUID, PlayerProfile> e : players.entrySet()) {
      PlayerProfile player = e.getValue();
      NBTTagCompound playerTag = new NBTTagCompound();
      player.saveToNBT(playerTag);
      list.appendTag(playerTag);
    }
    tag.setTag("players", list);
  }

  public PlayerProfile getPlayer(EntityPlayerMP player) {
    GameProfile p = player.getGameProfile();
    UUID uuid = p.getId();
    if (!players.containsKey(uuid)) {
      players.put(uuid, new PlayerProfile(p));
    }
    return players.get(uuid);
  }
}
