package moe.zcstaff.tinyessentials;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.MinecraftException;

import java.lang.Runnable;
import java.lang.Thread;
import java.lang.Long;
import java.lang.NumberFormatException;
import java.lang.Comparable;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

public class BackupUtil implements Runnable {
  private AtomicBoolean isBackuping = new AtomicBoolean(false);
  private long curBackup;
  private int maxBackup;
  private File zipFile;
  private File rootDir;
  private File backupDir;

  private static BackupUtil inst = new BackupUtil();

  private static final String[] ignoreList = new String[] { "logs" };

  private BackupUtil() {}

  public static BackupUtil instance() {
    return inst;
  }

  public void setWorldSave(boolean disable) {
    MinecraftServer server = MinecraftServer.getServer();
    for (int i = 0; i < server.worldServers.length; i++) {
      WorldServer world = server.worldServers[i];
      if (world != null) {
        world.levelSaving = disable;
      }
    }
  }
  
  public void forceWorldSave() throws MinecraftException {
    MinecraftServer server = MinecraftServer.getServer();
    server.getConfigurationManager().saveAllPlayerData();
    for (int i = 0; i < server.worldServers.length; i++) {
      WorldServer world = server.worldServers[i];
      if (world != null) {
        world.saveAllChunks(true, null);
      }
    }
    for (int i = 0; i < server.worldServers.length; i++) {
      WorldServer world = server.worldServers[i];
      if (world != null) {
        world.saveChunkData();
      }
    }
  }

  public ZipEntry getEntry(File file, boolean isFolder) {
    String name = rootDir.toPath().relativize(file.toPath()).toString();
    name = isFolder ? name + "/" : name;
    return new ZipEntry(name);
  }

  public void addTree(File file, ZipOutputStream zos) throws IOException {
    if (file.equals(backupDir)) {
      return;
    }
    for (int i = 0; i < ignoreList.length; i++) {
      if (file.equals(new File(rootDir, ignoreList[i]))) {
        return;
      }
    }
    try {
      if (file.isDirectory()) {
        if (!file.equals(rootDir)) {
          zos.putNextEntry(getEntry(file, true));
        }

        File[] subs = file.listFiles();
        for (int i = 0; i < subs.length; i++) {
          addTree(subs[i], zos);
        }
      } else if (file.isFile()) {
        zos.putNextEntry(getEntry(file, false));

        BufferedInputStream fin = new BufferedInputStream(new FileInputStream(file));
        try {
          byte[] buf = new byte[1024];
          int length = fin.read(buf);
          while (length >= 0) {
            zos.write(buf, 0, length);
            length = fin.read(buf);
          }
        } finally {
          fin.close();
        }
        
      }
    } catch (ZipException e) {
      System.out.println("ignore zip error " + e);
    }
  }

  private static class BackupEntry {
    public final long backup;
    public final File file;

    public BackupEntry(long backup, File file) {
      this.backup = backup;
      this.file = file;
    }

    @Override
    public String toString() {
      return "[backup:" + backup + "]";
    }
  }

  public void purgeOldBackups() {
    File[] files = backupDir.listFiles();
    List<BackupEntry> backups = new ArrayList<BackupEntry>();
    for (int i = 0; i < files.length; i++) {
      File f = files[i];
      String name = f.getName();
      if (name.startsWith("backup-") && name.endsWith(".zip")) {
        name = name.substring(7, name.length() - 4);
      } else {
        continue;
      }
      long backup = 0;
      try {
        backup = Long.parseLong(name);
      } catch (NumberFormatException e) {
        continue;
      }
      backups.add(new BackupEntry(backup, f));
    }
    backups.sort(new Comparator<BackupEntry>() {
      @Override
      public int compare(BackupEntry b1, BackupEntry b2) {
        return (int)(b2.backup - b1.backup);
      }
    });
    int i = 0;
    for (BackupEntry e : backups) {
      if (i >= maxBackup) {
        e.file.delete();
      }
      i++;
    }
  }

  @Override
  public void run() {
    System.out.println("start backup " + curBackup);

    try {
      setWorldSave(false);
      forceWorldSave();
      setWorldSave(true);
      ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
      try {
        addTree(rootDir, zos);
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        zos.close();
      }
      purgeOldBackups();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      setWorldSave(false);
    }
    
    Profile.instance().lastBackup = curBackup;
    isBackuping.set(false);
    System.out.println("finish backup");
  }

  public boolean startBackup(long backup) {
    if (!isBackuping.compareAndSet(false, true)) {
      return false;
    }
    curBackup = backup;
    rootDir = Profile.instance().rootDir;
    backupDir = Profile.instance().backupDir;
    maxBackup = Profile.instance().maxBackup;
    zipFile = new File(backupDir, "backup-" + curBackup + ".zip");
    Thread th = new Thread(this, "Essential Backup");
    th.start();
    return true;
  }

  public static long getNewBackup() {
    return System.currentTimeMillis() / 1000;
  }

  public void checkBackup() {
    long newBackup = getNewBackup();
    long lastBackup = Profile.instance().lastBackup;
    int interval = Profile.instance().backupInterval;
    if (lastBackup > 0 && interval > 0 && newBackup > lastBackup + interval) {
      startBackup(newBackup);
    }
  }
}
