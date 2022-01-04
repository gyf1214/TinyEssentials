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
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

public class BackupUtil implements Runnable {
  private AtomicBoolean isBackupFinished = new AtomicBoolean(false);
  private AtomicBoolean isBackuping = new AtomicBoolean(false);
  private long curBackup;
  private int maxBackup;
  private File zipFile;
  private File rootDir;
  private File backupDir;
  private Map<String, Long> backupFiles;
  private boolean incremental;

  private static BackupUtil inst = new BackupUtil();

  private static final String[] ignoreList = new String[] { "logs" };

  private BackupUtil() {}

  public static BackupUtil instance() {
    return inst;
  }

  public void setWorldSaveDisabled(boolean disable) {
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

  public String getEntryName(File file, boolean isFolder) {
    String name = rootDir.toPath().relativize(file.toPath()).toString();
    name = isFolder ? name + "/" : name;
    return name;
  }

  private interface ITreeProcessor {
    public boolean processDir(File file) throws IOException;
    public void processFile(File file) throws IOException;
  }

  private class ZipTreeProcesser implements ITreeProcessor {
    private ZipOutputStream zos;
    private Map<String, Long> fileMap;

    public ZipTreeProcesser(ZipOutputStream zos) {
      this(zos, new HashMap<String, Long>());
    }

    public ZipTreeProcesser(ZipOutputStream zos, Map<String, Long> files) {
      this.zos = zos;
      fileMap = files;
    }

    public void writeFile(File file) throws IOException, ZipException {
      BufferedInputStream fin = new BufferedInputStream(new FileInputStream(file));
      try {
        byte[] buf = new byte[4096];
        int length = fin.read(buf);
        while (length >= 0) {
          zos.write(buf, 0, length);
          length = fin.read(buf);
        }
      } finally {
        fin.close();
      }
    }

    @Override
    public boolean processDir(File file) throws IOException {
      try {
        zos.putNextEntry(new ZipEntry(getEntryName(file, true)));
        return true;
      } catch (ZipException e) {
        System.out.println("ignore zip error " + e);
      }
      return false;
    }

    @Override
    public void processFile(File file) throws IOException {
      String name = getEntryName(file, false);
      if (incremental && backupFiles.containsKey(name) && backupFiles.get(name) >= file.lastModified()) {
        return;
      }
      try {
        zos.putNextEntry(new ZipEntry(name));
        writeFile(file);
      } catch (ZipException e) {
        System.out.println("ignore zip error " + e);
      }
    }
  }

  private class TimeTreeProcessor implements ITreeProcessor {
    private Map<String, Long> fileMap;

    public TimeTreeProcessor(Map<String, Long> files) {
      fileMap = files;
    }

    @Override
    public boolean processDir(File file) {
      return true;
    }

    @Override
    public void processFile(File file) {
      String entry = getEntryName(file, false);
      fileMap.put(entry, file.lastModified());
    }
  }

  public void addTree(File file, ITreeProcessor processor) throws IOException {
    if (file.equals(backupDir)) {
      return;
    }
    for (int i = 0; i < ignoreList.length; i++) {
      if (file.equals(new File(rootDir, ignoreList[i]))) {
        return;
      }
    }
    if (file.isDirectory()) {
      boolean step = true;
      if (!file.equals(rootDir)) {
        step = processor.processDir(file);
      }

      if (step) {
        File[] subs = file.listFiles();
        for (int i = 0; i < subs.length; i++) {
          addTree(subs[i], processor);
        }
      }
    } else if (file.isFile()) {
      processor.processFile(file);
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
    String prefix = getBackupPrefix();
    File[] files = backupDir.listFiles();
    List<BackupEntry> backups = new ArrayList<BackupEntry>();
    for (int i = 0; i < files.length; i++) {
      File f = files[i];
      String name = f.getName();
      if (name.startsWith(prefix) && name.endsWith(".zip")) {
        name = name.substring(prefix.length(), name.length() - 4);
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
    System.out.println("start " + getBackupPrefix() + curBackup);

    long lastIncBackup = Profile.instance().lastIncBackup;
    long lastBackup = Profile.instance().lastBackup;
    Map<String, Long> backupFilesOld = incremental ? null : new HashMap<String, Long>(backupFiles);
    boolean success = false;

    try {
      Profile.instance().lastIncBackup = curBackup;
      if (!incremental) {
        Profile.instance().lastBackup = curBackup;
        backupFiles.clear();
        addTree(rootDir, new TimeTreeProcessor(backupFiles));
      }
      Profile.instance().saveProfile();

      ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
      try {
        addTree(rootDir, new ZipTreeProcesser(zos));
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        zos.close();
      }
      purgeOldBackups();
      success = true;
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (!success) {
      Profile.instance().lastIncBackup = lastIncBackup;
      if (!incremental) {
        Profile.instance().lastBackup = lastBackup;
        backupFiles.clear();
        backupFiles.putAll(backupFilesOld);
      }
      System.out.println("backup failed");
    } else {
      System.out.println("backup suceeded");
    }
    
    isBackuping.set(false);
    isBackupFinished.set(true);
  }

  public String getBackupPrefix() {
    return incremental ? "inc-backup-" : "backup-";
  }

  public boolean startBackup(long backup, boolean inc) {
    if (!isBackuping.compareAndSet(false, true)) {
      return false;
    }
    curBackup = backup;
    incremental = inc;
    rootDir = Profile.instance().rootDir;
    backupDir = Profile.instance().backupDir;
    maxBackup = incremental ? Profile.instance().maxIncBackup : Profile.instance().maxBackup;
    backupFiles = Profile.instance().backupFiles;
    setWorldSaveDisabled(true);
    zipFile = new File(backupDir, getBackupPrefix() + curBackup + ".zip");
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
    long lastIncBackup = Profile.instance().lastIncBackup;
    int interval = Profile.instance().backupInterval;
    int incInt = Profile.instance().incBackupInterval;
    if (lastBackup > 0 && interval > 0 && newBackup >= lastBackup + interval) {
      startBackup(newBackup, false);
    } else if (lastIncBackup > 0 && incInt > 0 && newBackup >= lastIncBackup + incInt) {
      startBackup(newBackup, true);
    } else if (isBackupFinished.compareAndSet(true, false)) {
      setWorldSaveDisabled(false);
    }
  }
}
