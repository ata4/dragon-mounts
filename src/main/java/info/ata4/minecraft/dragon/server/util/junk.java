//package info.ata4.minecraft.dragon.server.util;
//
//import net.minecraftforge.fml.relauncher.Side;
//import net.minecraftforge.fml.relauncher.SideOnly;
//
///**
// * Created by Richard on 24/07/2015.
// */
//public class junk
//{
//  @SideOnly(Side.CLIENT)
//  @Override
//  public IIcon getIcon(int side, int metadata){
//
//    int blockDirection = metadata & 7;
//
//    // from Facing (or alternatively EnumFacing)
//    final int DOWN = 0;
//    final int UP = 1;
//    final int NORTH = 2;
//    final int SOUTH = 3;
//    final int WEST = 4;
//    final int EAST = 5;
//
//    if (side == blockDirection) {
//      return (blockDirection != UP && blockDirection != DOWN ? this.faceIcon : this.faceIcon);
//    } else {
//      if (blockDirection != UP && blockDirection != DOWN) {
//        if (side != UP && side != DOWN) {
//          if (blockDirection != SOUTH && blockDirection != NORTH) {
//            return (side == EAST || side == WEST ? this.backIcon : this.blockIcon);
//          } else {
//            if (blockDirection != EAST && blockDirection != WEST) {
//              return (side == SOUTH || side == NORTH ? this.backIcon : this.blockIcon);
//            } else {
//              return this.blockIcon;
//            }
//          }
//        } else {
//          return this.blockIcon;
//        }
//      } else {
//        return (side == UP || side == DOWN ? this.backIcon : this.blockIcon);
//      }
//    }
//  }
//
//}
