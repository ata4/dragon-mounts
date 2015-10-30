//package info.ata4.minecraft.dragon.server.util;
//
//import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
//import net.minecraft.entity.player.EntityPlayer;
//import net.minecraftforge.event.entity.EntityJoinWorldEvent;
//import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
//
///**
// * Created by TGG on 18/07/2015.
// *  Used to suppress spawning of other entities to make debugging easier.
// */
//public class EntitySpawnSuppressor {
//
//  @SubscribeEvent
//  public void entityJoinWorld(EntityJoinWorldEvent event) {
//    if (event.entity instanceof EntityPlayer) return;
//    if (event.entity instanceof EntityTameableDragon) return;
//    event.setCanceled(true);
//  }
//
//}
//
//
