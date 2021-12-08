package moe.zcstaff.tinyessentials;

public final class DimPos {
  public final int x, y, z, dim;

  public DimPos(int x, int y, int z, int dim) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.dim = dim;
  }

  public DimPos(int[] pos) {
    x = pos[0];
    y = pos[1];
    z = pos[2];
    dim = pos[3];
  }

  public int[] toIntArray() {
    return new int[] { x, y, z, dim };
  }

  @Override
  public String toString() {
    return "[" + DimUtil.getDimName(dim) + ":" + x + "," + y + "," + z + "]";
  }
}
