package moe.zcstaff.tinyessentials;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraftforge.common.MinecraftForge;
import moe.zcstaff.tinyessentials.command.BasicCommand;

import java.io.File;

@Mod(modid = TinyEssentials.MOD_ID, name = "TinyEssentials", version = TinyEssentials.VERSION,
     acceptedMinecraftVersions = "1.7.10", acceptableRemoteVersions = "*",
     dependencies = "required-after:Forge")
public class TinyEssentials {
  public static final String MOD_ID = "TinyEssentials";
  public static final String VERSION = "@VERSION@";
  
  @Mod.EventHandler
  public void preInit(FMLPreInitializationEvent e) {
    System.out.println("hello tinyessentials");
    Profile.preInit(new File(e.getModConfigurationDirectory(), MOD_ID));

    MinecraftForge.EVENT_BUS.register(new EventHandler());
    FMLCommonHandler.instance().bus().register(new EventHandler());
  }

  @Mod.EventHandler
  public void serverStart(FMLServerStartingEvent e) {
    System.out.println("server start");
    BasicCommand.registerCommands(e);
  }

  @Mod.EventHandler
  public void onServerAboutToStart(FMLServerAboutToStartEvent e) {
    System.out.println("server about to start");
    Profile.instance().loadProfile();
  }
}