package moe.zcstaff.tinyessentials;

import net.minecraftforge.common.DimensionManager;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;

import java.lang.Math;
import java.util.List;

public class DimUtil {
  public static String getDimName(int dim) {
    World w = DimensionManager.getWorld(dim);
    return w == null ? "DIM" + dim : w.provider.getDimensionName();
  }

  public static DimPos getEntityDimPos(Entity e) {
    return new DimPos((int)Math.floor(e.posX), (int)Math.floor(e.posY), (int)Math.floor(e.posZ), e.dimension);
  }

  public static boolean teleportEntity(EntityPlayerMP entity, DimPos pos) {
    double x = (double)pos.x + 0.5;
    double y = (double)pos.y + 0.5;
    double z = (double)pos.z + 0.5;
    int dim = pos.dim;

    if (dim == entity.dimension) {
      entity.fallDistance = 0;
      entity.playerNetServerHandler.setPlayerLocation(x, y, z, entity.rotationYaw, entity.rotationPitch);
      return true;
    }
    
    int from = entity.dimension;
    float rotationYaw = entity.rotationYaw;
    float rotationPitch = entity.rotationPitch;

    MinecraftServer server = MinecraftServer.getServer();
    WorldServer toDim = server.worldServerForDimension(dim);
    server.getConfigurationManager().transferPlayerToDimension(entity, dim, new TeleporterStub(toDim));
    if(from == 1 && entity.isEntityAlive()) {
      toDim.spawnEntityInWorld(entity);
      toDim.updateEntityWithOptionalForce(entity, false);
    }
    
    entity.fallDistance = 0;
    entity.rotationYaw = rotationYaw;
    entity.rotationPitch = rotationPitch;
    entity.setPositionAndUpdate(x, y, z);

    return true;
  }
  
  public static EntityPlayerMP findPlayer(String name) {
    List list = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
    for (Object o : list) {
      if (o instanceof EntityPlayerMP) {
        EntityPlayerMP player = (EntityPlayerMP)o;
        if (player.getGameProfile().getName().equals(name)) {
          return player;
        }
      }
    }
    return null;
  }

  private static class TeleporterStub extends Teleporter {
    public TeleporterStub(WorldServer w) {
      super(w);
    }
    
    @Override
    public boolean makePortal(Entity e) {
      return true;
    }
    
    @Override
    public boolean placeInExistingPortal(Entity e, double x, double y, double z, float f) {
      return true;
    }
    
    @Override
    public void placeInPortal(Entity entity, double x, double y, double z, float f) {
      entity.setLocationAndAngles(x, y, z, entity.rotationPitch, entity.rotationYaw);
      entity.motionX = 0D;
      entity.motionY = 0D;
      entity.motionZ = 0D;
      entity.fallDistance = 0F;
    }
    
    @Override
    public void removeStalePortalLocations(long l) {
    }
  }
}
