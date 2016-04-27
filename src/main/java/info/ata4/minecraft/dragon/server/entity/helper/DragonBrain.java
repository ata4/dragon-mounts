/*
** 2016 March 12
**
** The author disclaims copyright to this source code. In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.helper;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.entity.ai.EntityAIDragonCatchOwner;
import info.ata4.minecraft.dragon.server.entity.ai.EntityAIDragonRide;
import info.ata4.minecraft.dragon.server.entity.ai.air.EntityAIDragonFollowOwner;
import info.ata4.minecraft.dragon.server.entity.ai.air.EntityAIDragonLand;
import info.ata4.minecraft.dragon.server.entity.ai.ground.EntityAIDragonHunt;
import info.ata4.minecraft.dragon.server.entity.ai.ground.EntityAIDragonMate;
import info.ata4.minecraft.dragon.server.entity.ai.ground.EntityAIDragonWatchIdle;
import info.ata4.minecraft.dragon.server.entity.ai.ground.EntityAIDragonWatchLiving;
import info.ata4.minecraft.dragon.server.util.EntityClassPredicate;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtTarget;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateGround;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonBrain extends DragonHelper {

    // mutex 1: movement
    // mutex 2: looking
    // mutex 4: special state
    private final EntityAITasks tasks;
    
    // mutex 1: waypointing
    // mutex 2: continuous waypointing
    
    // mutex 1: generic targeting
    private final EntityAITasks targetTasks;
    
    public DragonBrain(EntityTameableDragon dragon) {
        super(dragon);
        tasks = dragon.tasks;
        targetTasks = dragon.targetTasks;
    }
    
    public void setAvoidsWater(boolean avoidWater) {
        PathNavigate pathNavigate = dragon.getNavigator();
        if (pathNavigate instanceof PathNavigateGround) {
            PathNavigateGround pathNavigateGround = (PathNavigateGround) pathNavigate;
            pathNavigateGround.setCanSwim(!avoidWater); // originally setAvoidsWater()
        }
    }
    
    public void clearTasks() {
        clearTasks(tasks);
        clearTasks(targetTasks);
    }
    
    public void clearTasks(EntityAITasks tasks) {
        List<EntityAITaskEntry> taskEntries = new ArrayList<>(tasks.taskEntries);
        taskEntries.forEach(entry -> tasks.removeTask(entry.action));
    }
    
    public void updateAITasks() {
        // only hatchlings are small enough for doors
        // (eggs don't move on their own anyway and are ignored)
        // guessed, based on EntityAIRestrictOpenDoor - break the door down, don't open it
        if (dragon.getNavigator() instanceof PathNavigateGround) {
            PathNavigateGround pathNavigateGround = (PathNavigateGround) dragon.getNavigator();
            pathNavigateGround.setEnterDoors(dragon.isHatchling());
        }
        
        // clear current navigation target
        dragon.getNavigator().clearPathEntity();
        
        // clear existing tasks
        clearTasks();
        
        // eggs don't have any tasks
        if (dragon.isEgg()) {
            return;
        }
        
        tasks.addTask(0, new EntityAIDragonCatchOwner(dragon)); // mutex all
        tasks.addTask(1, new EntityAIDragonRide(dragon)); // mutex all
        tasks.addTask(2, new EntityAIDragonFollowOwner(dragon)); // mutex all
        tasks.addTask(3, new EntityAIMoveTowardsRestriction(dragon, 1)); // mutex 1
        
        if (dragon.isFlying()) {
            tasks.addTask(3, new EntityAIDragonLand(dragon, 1)); // mutex 1
        } else {
            tasks.addTask(2, new EntityAISwimming(dragon)); // mutex 4
            tasks.addTask(4, dragon.getAISit()); // mutex 4+1

            tasks.addTask(6, new EntityAITempt(dragon, 0.75, dragon.getBreed().getBreedingItem(), false)); // mutex 2+1
            tasks.addTask(7, new EntityAIAttackMelee(dragon, 1, true)); // mutex 2+1

//            tasks.addTask(9, new EntityAIDragonFollowOwner(dragon, 1, 12, 128)); // mutex 2+1
            tasks.addTask(10, new EntityAIWander(dragon, 1)); // mutex 1
            tasks.addTask(11, new EntityAIDragonWatchIdle(dragon)); // mutex 2
            tasks.addTask(11, new EntityAIDragonWatchLiving(dragon, 16, 0.05f)); // mutex 2

            targetTasks.addTask(5, new EntityAIDragonHunt(dragon, EntityAnimal.class, false,
                new EntityClassPredicate(
                        EntitySheep.class,
                        EntityPig.class,
                        EntityChicken.class,
                        EntityRabbit.class
                )
            )); // mutex 1

            if (dragon.isHatchling()) {
                tasks.addTask(8, new EntityAIFollowParent(dragon, 0.8)); // mutex 2+1
                tasks.addTask(9, new EntityAIPanic(dragon, 1)); // mutex 1
            } else {
                targetTasks.addTask(2, new EntityAIOwnerHurtByTarget(dragon)); // mutex 1
                targetTasks.addTask(3, new EntityAIOwnerHurtTarget(dragon)); // mutex 1
                targetTasks.addTask(4, new EntityAIHurtByTarget(dragon, false)); // mutex 1
            }

            if (dragon.isAdult()) {
                tasks.addTask(5, new EntityAIDragonMate(dragon, 0.6)); // mutex 2+1
            }
        }
    }
}
