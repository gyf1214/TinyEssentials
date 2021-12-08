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
  private static final String backupFolder = "backup";
  
  public File rootDir;
  public File configDir;
  public File backupDir;
  public int homeLimit;
  public boolean enableHome;
  public boolean enableBack;
  public boolean enableTP;
  public int maxBackup;
  public int backupInterval;
  public long lastBackup = -1;
  public Map<UUID, PlayerProfile> players = new HashMap<UUID, PlayerProfile>();

  private Profile(File dir) {
    rootDir = dir.getParentFile();
    configDir = new File(dir, TinyEssentials.MOD_ID);
    if (!configDir.exists()) {
      configDir.mkdirs();
    }
    backupDir = new File(configDir, backupFolder);
    if (!backupDir.exists()) {
      backupDir.mkdirs();
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
    maxBackup = config.get(Configuration.CATEGORY_GENERAL, "max_backup", 5).getInt();
    backupInterval = config.get(Configuration.CATEGORY_GENERAL, "backup_interval", 0).getInt();
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
    if (tag.hasKey("lastBackup") && tag.func_150299_b("lastBackup") == Constants.NBT.TAG_LONG) {
      lastBackup = tag.getLong("lastBackup");
    }

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
    tag.setLong("lastBackup", lastBackup);

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
