package adris.altoclef.trackers;


import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.mixins.PersistentProjectileEntityAccessor;
import adris.altoclef.trackers.blacklisting.EntityLocateBlacklist;
import adris.altoclef.util.CachedProjectile;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.ProjectileUtil;
import adris.altoclef.util.baritone.BaritoneHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;


@SuppressWarnings("rawtypes")
public class EntityTracker extends Tracker {
    private final HashMap<Item, List<ItemEntity>> itemDropLocations = new HashMap<>();
    private final HashMap<Class, List<Entity>> entityMap = new HashMap<>();
    private final List<Entity> closeEntities = new ArrayList<>();
    private final List<HostileEntity> hostiles = new ArrayList<>();
    private final List<CachedProjectile> projectiles = new ArrayList<>();
    private final HashMap<String, PlayerEntity> playerMap = new HashMap<>();
    private final HashMap<String, Vec3d> playerLastCoordinates = new HashMap<>();
    private final EntityLocateBlacklist entityBlacklist = new EntityLocateBlacklist();

    public EntityTracker(TrackerManager manager) {
        super(manager);
    }

    /**
     * Squash a class that may have sub classes into one distinguishable class type. For ease of use.
     *
     * @param type: An entity class that may have a 'simpler' class to squash to
     *
     * @return what the given entity class should be read as/catalogued as.
     */
    private static Class squashType(Class type) {
        // Squash types for ease of use
        if (PlayerEntity.class.isAssignableFrom(type)) {
            return PlayerEntity.class;
        }
        return type;
    }

    public static boolean isAngryAtPlayer(Entity hostile) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        // NOTE: These do not work.
        if (hostile instanceof EndermanEntity) {
            EndermanEntity enderman = (EndermanEntity) hostile;
            return enderman.isAngryAt(player) && enderman.isAngry();
        }
        if (hostile instanceof ZombifiedPiglinEntity) {
            ZombifiedPiglinEntity zombie = (ZombifiedPiglinEntity) hostile;
            // Will ALWAYS be false.
            return zombie.hasAngerTime() && zombie.isAngryAt(player);
        }
        return !isTradingPiglin(hostile);
        /*
        if (hostile instanceof SpiderEntity) {
            SpiderEntity sp = (SpiderEntity) hostile;
            float b = sp.getBrightnessAtEyes();
            // Will not consider spiders that stop attacking!
            return (b < 0.5f);
        }*/
    }

    public static boolean isTradingPiglin(Entity entity) {
        if (entity instanceof PiglinEntity) {
            PiglinEntity pig = (PiglinEntity) entity;
            for (ItemStack stack : pig.getItemsHand()) {
                if (stack.getItem().equals(Items.GOLD_INGOT)) {
                    // We're trading with this one, ignore it.
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isHostileToPlayer(AltoClef mod, HostileEntity mob) {
        return isAngryAtPlayer(mob) && mob.canSee(mod.getPlayer());
    }

    public ItemEntity getClosestItemDrop(Vec3d position, Item... items) {
        ensureUpdated();
        ItemTarget[] tempTargetList = new ItemTarget[items.length];
        for (int i = 0; i < items.length; ++i) {
            tempTargetList[i] = new ItemTarget(items[i], 9999999);
        }
        return getClosestItemDrop(position, tempTargetList);
        //return getClosestItemDrop(position, ItemTarget.getItemArray(_mod, targets));
    }

    public ItemEntity getClosestItemDrop(Vec3d position, ItemTarget... targets) {
        ensureUpdated();
        if (targets.length == 0) {
            Debug.logError("You asked for the drop position of zero items... Most likely a typo.");
            return null;
        }
        if (!itemDropped(targets)) {
            Debug.logError("You forgot to check for whether item (example): " + targets[0].getMatches()[0].getTranslationKey() +
                           " was dropped before finding its drop location.");
            return null;
        }

        ItemEntity closestEntity = null;
        float minCost = Float.POSITIVE_INFINITY;
        for (ItemTarget target : targets) {
            for (Item item : target.getMatches()) {
                if (!itemDropped(item)) continue;
                for (ItemEntity entity : itemDropLocations.get(item)) {
                    if (entityBlacklist.unreachable(entity)) continue;
                    if (!entity.getStack().getItem().equals(item)) continue;

                    float cost = (float) BaritoneHelper.calculateGenericHeuristic(position, entity.getPos());
                    if (cost < minCost) {
                        minCost = cost;
                        closestEntity = entity;
                    }
                }
            }
        }
        return closestEntity;
    }

    public Entity getClosestEntity(Vec3d position, Class... entityTypes) {
        return this.getClosestEntity(position, (entity) -> false, entityTypes);
    }

    public Entity getClosestEntity(Vec3d position, Predicate<? super Entity> ignore, Class... entityTypes) {
        Entity closestEntity = null;
        double minCost = Float.POSITIVE_INFINITY;
        for (Class toFind : entityTypes) {
            if (entityMap.containsKey(toFind)) {
                for (Entity entity : entityMap.get(toFind)) {
                    // Don't accept entities that no longer exist
                    if (!entity.isAlive()) continue;
                    if (ignore.test(entity)) continue;
                    double cost = entity.squaredDistanceTo(position);
                    if (cost < minCost) {
                        minCost = cost;
                        closestEntity = entity;
                    }
                }
            }
        }
        return closestEntity;
    }

    public boolean itemDropped(Item... items) {
        ensureUpdated();
        for (Item item : items) {
            if (itemDropLocations.containsKey(item)) {
                // Find a non-blacklisted item
                for (ItemEntity entity : itemDropLocations.get(item)) {
                    if (!entityBlacklist.unreachable(entity)) return true;
                }
            }
        }
        return false;
    }

    public boolean itemDropped(ItemTarget... targets) {
        ensureUpdated();
        for (ItemTarget target : targets) {
            if (itemDropped(target.getMatches())) return true;
        }
        return false;
    }

    public boolean entityFound(Class... types) {
        ensureUpdated();
        for (Class type : types) {
            if (entityMap.containsKey(type)) return true;
        }
        return false;
    }

    public <T extends Entity> List<T> getTrackedEntities(Class<T> type) {
        ensureUpdated();
        if (!entityFound(type)) {
            return Collections.emptyList();
        }
        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
            //noinspection unchecked
            return (List<T>) entityMap.get(type);
        }
    }

    public List<Entity> getCloseEntities() {
        ensureUpdated();
        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
            return closeEntities;
        }
    }

    public List<CachedProjectile> getProjectiles() {
        ensureUpdated();
        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
            return projectiles;
        }
    }

    public List<HostileEntity> getHostiles() {
        ensureUpdated();
        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
            return hostiles;
        }
    }

    public boolean isPlayerLoaded(String name) {
        ensureUpdated();
        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
            return playerMap.containsKey(name);
        }
    }

    public Vec3d getPlayerMostRecentPosition(String name) {
        ensureUpdated();
        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
            if (playerLastCoordinates.containsKey(name)) {
                return playerLastCoordinates.get(name);
            }
        }
        return null;
    }

    public PlayerEntity getPlayerEntity(String name) {
        if (isPlayerLoaded(name)) {
            synchronized (BaritoneHelper.MINECRAFT_LOCK) {
                return playerMap.get(name);
            }
        }
        return null;
    }

    public void requestEntityUnreachable(Entity entity) {
        entityBlacklist.blackListItem(mod, entity, 2);
    }

    public boolean isEntityReachable(Entity entity) {
        return !entityBlacklist.unreachable(entity);
    }

    @Override
    protected synchronized void updateState() {
        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
            itemDropLocations.clear();
            entityMap.clear();
            closeEntities.clear();
            projectiles.clear();
            hostiles.clear();
            playerMap.clear();
            if (MinecraftClient.getInstance().world == null) return;

            // Loop through all entities and track 'em
            for (Entity entity : MinecraftClient.getInstance().world.getEntities()) {

                Class type = entity.getClass();
                type = squashType(type);

                if (entity == null || !entity.isAlive()) continue;

                // Don't catalogue our own player.
                if (type == PlayerEntity.class && entity.equals(mod.getPlayer())) continue;
                if (!entityMap.containsKey(type)) {
                    //Debug.logInternal("NEW TYPE: " + type);
                    entityMap.put(type, new ArrayList<>());
                }
                entityMap.get(type).add(entity);

                if (mod.getControllerExtras().inRange(entity)) {
                    closeEntities.add(entity);
                }

                if (entity instanceof ItemEntity) {
                    ItemEntity ientity = (ItemEntity) entity;
                    Item droppedItem = ientity.getStack().getItem();

                    if (!itemDropLocations.containsKey(droppedItem)) {
                        itemDropLocations.put(droppedItem, new ArrayList<>());
                    }
                    itemDropLocations.get(droppedItem).add(ientity);
                } else if (entity instanceof MobEntity) {
                    //MobEntity mob = (MobEntity) entity;


                    if (entity instanceof HostileEntity) {

                        // Only run away if the hostile can see us.
                        HostileEntity hostile = (HostileEntity) entity;

                        if (isHostileToPlayer(mod, hostile)) {
                            // Check if the mob is facing us or is close enough
                            boolean closeEnough = hostile.isInRange(mod.getPlayer(), 26);

                            //Debug.logInternal("TARGET: " + hostile.is);
                            if (closeEnough) {
                                hostiles.add(hostile);
                            }
                        }
                    }

                /*
                if (mob instanceof HostileEntity) {
                    HostileEntity hostile = (HostileEntity) mob;
                }
                 */
                } else if (entity instanceof ProjectileEntity) {
                    if (!mod.getConfigState().shouldAvoidDodgingProjectile(entity)) {
                        CachedProjectile proj = new CachedProjectile();
                        ProjectileEntity projEntity = (ProjectileEntity) entity;

                        boolean inAir = true;
                        // Get projectile "inGround" variable
                        if (entity instanceof PersistentProjectileEntity) {
                            //noinspection CastConflictsWithInstanceof
                            inAir = !((PersistentProjectileEntityAccessor) entity).isInGround();
                        }

                        if (inAir) {
                            proj.position = projEntity.getPos();
                            proj.velocity = projEntity.getVelocity();
                            proj.gravity = ProjectileUtil.hasGravity(projEntity) ? ProjectileUtil.GRAVITY_ACCEL : 0;
                            proj.projectileType = projEntity.getClass();
                            projectiles.add(proj);
                        }
                    }
                } else if (entity instanceof PlayerEntity) {
                    PlayerEntity player = (PlayerEntity) entity;
                    String name = player.getName().getString();
                    playerMap.put(name, player);
                    playerLastCoordinates.put(name, player.getPos());
                }
            }
        }
    }

    @Override
    protected void reset() {
        // Dirty clears everything else.
        entityBlacklist.clear();
    }
}
