/*
 ** 2013 October 29
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.client.gui;

import info.ata4.minecraft.dragon.DragonMounts;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.entity.breeds.DragonBreed;
import info.ata4.minecraft.dragon.server.entity.breeds.EnumDragonBreed;
import info.ata4.minecraft.dragon.server.entity.helper.DragonBreedHelper;
import info.ata4.minecraft.dragon.server.entity.helper.DragonLifeStageHelper;
import info.ata4.minecraft.dragon.server.entity.helper.DragonReproductionHelper;
import info.ata4.minecraft.dragon.util.reflection.PrivateAccessor;
import java.text.DecimalFormat;
import java.util.Collection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.lwjgl.input.Keyboard;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class GuiDragonDebug extends Gui implements PrivateAccessor {
    
    private static final int WHITE = 0xFFFFFF;
    private static final int GREY = 0xAAAAAA;
    private static final int YELLOW = 0xFFFF00;
    private static final int RED = 0xFF8888;
    
    public static Object probe;
    public static boolean enabled = true;
    
    private final Minecraft mc = Minecraft.getMinecraft();
    private final FontRenderer fr;
    private final GuiTextPrinter text;
    private final DecimalFormat dfShort = new DecimalFormat("0.00");
    private final DecimalFormat dfLong = new DecimalFormat("0.0000");
    private ScaledResolution res;
    private EntityTameableDragon dragonClient;
    private EntityTameableDragon dragonServer;
    
    public GuiDragonDebug() {
        fr = mc.fontRendererObj;
        text = new GuiTextPrinter(fr);
    }
    
    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent event) {
        if (!enabled || event.isCancelable() || event.getType() != ElementType.TEXT) {
            return;
        }

        getClientDragon();
        getServerDragon();

        if (dragonClient != null) {
            GuiIngameForge ingameGUI = (GuiIngameForge) mc.ingameGUI;
            res = ingameGUI.getResolution();

            renderTitle();

            try {
                if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                    renderNavigation();
                    renderAttributes();
                    renderBreedPoints();
                } else {
                    renderEntityInfo();
                    renderAITasks();
//                    renderWatchedObjects();
                }

                renderProbe();
            } catch (Exception ex) {
                renderException(ex);
            }

            if (dragonClient.isDead) {
                dragonClient = null;
                dragonServer = null;
            }
        }

    }
    
    private void getClientDragon() {
        // always return currently ridden dragon first
        if (mc.thePlayer.getRidingEntity() instanceof EntityTameableDragon) {
            dragonClient = (EntityTameableDragon) mc.thePlayer.getRidingEntity();
            return;
        }
        
        if (mc.objectMouseOver == null) {
            return;
        }
        
        if (mc.objectMouseOver.entityHit == null) {
            return;
        }
        
        if (!(mc.objectMouseOver.entityHit instanceof EntityTameableDragon)) {
            return;
        }
        
        dragonClient = (EntityTameableDragon) mc.objectMouseOver.entityHit;
    }

    private void getServerDragon() {
        if (!mc.isSingleplayer()) {
            // not possible on dedicated
            return;
        }
        
        if (dragonClient == null) {
            // client dragon required
            dragonServer = null;
            return;
        }
        
        if (dragonServer != null && dragonServer.getEntityId() == dragonClient.getEntityId()) {
            // done before
            return;
        }
        
        MinecraftServer mcs = mc.getIntegratedServer();
        
        for (WorldServer ws : mcs.worldServers) {
            Entity ent = ws.getEntityByID(dragonClient.getEntityId());
            if (ent != null && ent instanceof EntityTameableDragon) {
                dragonServer = (EntityTameableDragon) ent;
                return;
            }
        }
    }
    
    private EntityTameableDragon getSelectedDragon() {
        return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? dragonClient : dragonServer;
    }

    private void renderTitle() {
        String title = String.format("%s %s Debug", DragonMounts.NAME,
                DragonMounts.instance.getMetadata().version);
        
        text.setOrigin(16, 8);
        text.setColor(GREY);
        text.println(title);
        text.setColor(WHITE);
    }
    
    private void renderEntityInfo() {
        EntityTameableDragon dragon = getSelectedDragon();
        if (dragon == null) {
            return;
        }
        
        text.setOrigin(16, 32);
        
        text.setColor(YELLOW);
        text.println("Entity");
        text.setColor(WHITE);
        
        text.println("Side: " + (dragon.isServer() ? "server" : "client"));
        
        text.println("ID: " + dragon.getEntityId());
        text.println("UUID: " + StringUtils.abbreviate(dragon.getUniqueID().toString(), 22));
        text.println("Name: " + dragon.getName());
        
        // position
        String px = dfShort.format(dragon.posX);
        String py = dfShort.format(dragon.posY);
        String pz = dfShort.format(dragon.posZ);
        text.printf("x: %s y: %s z: %s\n", px, py, pz);
        
        // rotation
        String pitch = dfShort.format(dragon.rotationPitch);
        String yaw = dfShort.format(dragon.rotationYaw);
        String yawHead = dfShort.format(dragon.rotationYawHead);
        text.printf("p: %s y: %s yh: %s\n", pitch, yaw, yawHead);
        
        // health
        String health = dfShort.format(dragon.getHealth());
        String healthMax = dfShort.format(dragon.getMaxHealth());
        String healthRel = dfShort.format(dragon.getHealthRelative() * 100);
        text.printf("Health: %s/%s (%s%%)\n", health, healthMax, healthRel);
        
        // breed
        text.print("Breed: ");
        EnumDragonBreed breedType = dragon.getBreedType();
        text.setColor(breedType.getBreed().getColor());
        text.println(breedType.getName());
        text.setColor(WHITE);
        
        // life stage
        DragonLifeStageHelper lifeStage = dragon.getLifeStageHelper();
        String lifeStageName = lifeStage.getLifeStage().name().toLowerCase();
        int ticksSinceCreation = dragon.getLifeStageHelper().getTicksSinceCreation();
        text.printf("Life stage: %s (%d)\n", lifeStageName, ticksSinceCreation);
        
        // size
        String scale = dfShort.format(lifeStage.getScale());
        String width = dfShort.format(dragon.width);
        String height = dfShort.format(dragon.height);
        text.printf("Size: %s (w:%s h:%s)\n", scale, width, height);
        
        // tamed flag/owner name
        //String tamedString = dragon.getOwnerName();
        String tamedString;
        if (dragon.isTamed()) {
            Entity player = dragon.getOwner();
            if (player != null) {
                tamedString = "yes (" + player.getName()+ ")";
            } else {
                tamedString = "yes (" + StringUtils.abbreviate(dragon.getOwnerId().toString(), 22) + ")";
            }
        } else {
            tamedString = "no";
        }
        text.println("Tamed: " + tamedString);
        
        // breeder name
        DragonReproductionHelper reproduction = dragon.getReproductionHelper();
        EntityPlayer breeder = reproduction.getBreeder();
        String breederName;
        if (breeder == null) {
            breederName = "none";
        } else {
            breederName = breeder.getName();
        }
        text.println("Breeder: " + breederName);
        text.println("Reproduced: " + reproduction.getReproCount());
        text.println("Saddled: " + dragon.isSaddled());
    }
    
    private void renderAttributes() {
        EntityTameableDragon dragon = getSelectedDragon();
        if (dragon == null) {
            return;
        }
        
        text.setOrigin(text.getX() + 180, 8);
        
        text.setColor(YELLOW);
        text.println("Attributes");
        text.setColor(WHITE);
        
        Collection<IAttributeInstance> attribs = dragon.getAttributeMap().getAllAttributes();
        
        attribs.forEach(attrib -> {
            String attribName = I18n.translateToLocal("attribute.name." + attrib.getAttribute().getAttributeUnlocalizedName());
            String attribValue = dfShort.format(attrib.getAttributeValue());
            String attribBase = dfShort.format(attrib.getBaseValue());
            text.println(attribName + " = " + attribValue + " (" + attribBase + ")");
        });
        
        text.println();
    }
    
    private void renderBreedPoints() {
        if (dragonServer == null) {
            return;
        }
        
        text.setColor(YELLOW);
        text.println("Breed points");
        text.setColor(WHITE);
        
        DragonBreedHelper breedHelper = dragonServer.getBreedHelper();
        breedHelper.getBreedPoints().forEach((breedType, points) -> {
            text.setColor(breedType.getBreed().getColor());
            text.printf("%s: %d\n", breedType, points.get());
        });
    }

    private void renderNavigation() {
        text.setOrigin(16, 32);
        
        text.setColor(YELLOW);
        text.println("Navigation (Ground)");
        text.setColor(WHITE);
        
        PathNavigate nav = dragonServer.getNavigator();
        PathNavigateGround pathNavigateGround = null;
        if (nav instanceof PathNavigateGround) {
            pathNavigateGround = (PathNavigateGround) nav;
        }
        
        text.println("Search range: " + nav.getPathSearchRange());
        text.println("Can swim: " + (pathNavigateGround == null ? "N/A" : pathNavigateGround.getCanSwim()));
        text.println("Break doors: " + (pathNavigateGround == null ? "N/A" : pathNavigateGround.getEnterDoors()));
        text.println("No path: " + nav.noPath());

        Path path = nav.getPath();
        
        if (path != null) {
            text.println("Length: " + path.getCurrentPathLength());
            text.println("Index: " + path.getCurrentPathIndex());
            
            PathPoint finalPoint = path.getFinalPathPoint();
            text.println("Final point: " + finalPoint);
        }
        
        text.println();

        text.setColor(YELLOW);
        text.println("Navigation (Air)");
        text.setColor(WHITE);
        
        text.println("Can fly: " + dragonClient.canFly());
        text.println("Flying: " + dragonClient.isFlying());
        text.println("Altitude: " + dfLong.format(dragonClient.getAltitude()));
    }
  
    private void renderAITasks() {
        if (dragonServer == null) {
            return;
        }

        text.setOrigin(text.getX() + 180, 8);
        
        text.setColor(YELLOW);
        text.println("AI tasks");
        text.setColor(WHITE);
    }
    
    private void renderProbe() {
        if (probe == null) {
            return;
        }
        
        text.setOrigin(16, res.getScaledHeight() - text.getLineSpace() * 2);
        
        text.println(probe.getClass().getSimpleName() + ":" + String.valueOf(probe));
    }

    private void renderException(Exception ex) {
        text.setOrigin(16, 32);

        text.setColor(RED);
        text.println("GUI exception:");
        text.printf(ExceptionUtils.getStackTrace(ex));
        text.setColor(WHITE);
    }
}
