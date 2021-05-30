package adris.altoclef;


import baritone.altoclef.AltoClefSettings;
import baritone.api.Settings;
import baritone.api.utils.RayTraceUtils;
import baritone.process.MineProcess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.function.Predicate;


/**
 * Represents a state of global config. It can be copied and reset so that behaviour across tasks is consistent.
 */
public class ConfigState {

    private final AltoClef mod;
    // TODO: 2021-05-22 remove stack
    Stack<State> states = new Stack<>();

    public ConfigState(AltoClef mod) {
        this.mod = mod;

        // Start with one state.
        push();
    }

    /// Parameters

    public void setFollowDistance(double distance) {
        current().followOffsetDistance = distance;
        current().applyState();
    }

    public void setMineScanDroppedItems(boolean value) {
        current().mineScanDroppedItems = value;
        current().applyState();
    }

    public void addThrowawayItems(Item... items) {
        Collections.addAll(current().throwawayItems, items);
        current().applyState();
    }

    /*
    public void removeThrowawayItems(Item ...items) {
        // No removeAll huh. Nice one Java.
        for (Item item : items) {
            current().throwawayItems.remove(item);
        }
        current().applyState();
    }
    public void removeThrowawayItems(ItemTarget...targets) {
        // Just to be safe we remove ALL items that we may want to use.
        for (ItemTarget target : targets) {
            removeThrowawayItems(target.getMatches());
        }
        current().applyState();
    }
    */

    public boolean exclusivelyMineLogs() {
        return current().exclusivelyMineLogs;
    }

    public void setExclusivelyMineLogs(boolean value) {
        current().exclusivelyMineLogs = value;
        current().applyState();
    }

    public boolean shouldExcludeFromForcefield(Entity entity) {
        for (Predicate<Entity> pred : current().excludeFromForceField) {
            if (pred.test(entity))
                return true;
        }
        return false;
    }

    public void addForceFieldExclusion(Predicate<Entity> pred) {
        current().excludeFromForceField.add(pred);
        // Not needed, as excludeFromForceField isn't applied anywhere else.
        // current.applyState();
    }

    public void avoidBlockBreaking(BlockPos pos) {
        current().blocksToAvoidBreaking.add(pos);
        current().applyState();
    }

    public void avoidBlockBreaking(Predicate<BlockPos> pred) {
        current().toAvoidBreaking.add(pred);
        current().applyState();
    }

    public void avoidBlockPlacing(Predicate<BlockPos> pred) {
        current().toAvoidPlacing.add(pred);
        current().applyState();
    }

    public void allowWalkingOn(Predicate<BlockPos> pred) {
        current().allowWalking.add(pred);
        current().applyState();
    }

    public void setRayTracingFluidHandling(RaycastContext.FluidHandling fluidHandling) {
        current().rayFluidHandling = fluidHandling;
        //Debug.logMessage("OOF: " + fluidHandling);
        current().applyState();
    }

    public void setSearchAnywhereFlag(boolean value) {
        current().mineProcSearchAnyFlag = value;
        current().applyState();
    }

    public void setAllowWalkThroughFlowingWater(boolean value) {
        current().allowWalkThroughFlowingWater = value;
        current().applyState();
    }

    public void setPauseOnLostFocus(boolean pauseOnLostFocus) {
        current().pauseOnLostFocus = pauseOnLostFocus;
        current().applyState();
    }

    public void addProtectedItems(Item... items) {
        Collections.addAll(current().protectedItems, items);
        current().applyState();
    }

    public void removeProtectedItems(Item... items) {
        current().protectedItems.removeAll(Arrays.asList(items));
        current().applyState();
    }

    public boolean isProtected(Item item) {
        // For now nothing is protected.
        return current().protectedItems.contains(item);
    }

    public boolean shouldForceFieldPlayers() {
        return current().forceFieldPlayers;
    }

    public void setForceFieldPlayers(boolean forceFieldPlayers) {
        current().forceFieldPlayers = forceFieldPlayers;
        // Not needed, nothing changes.
        // current.applyState()
    }

    public void allowWalkThroughLava(boolean allow) {
        current().walkThroughLava = allow;
        current().applyState();
    }

    public void setPreferredStairs(boolean allow) {
        current().preferredStairs = allow;
        current().applyState();
    }

    public void setAllowDiagonalAscend(boolean allow) {
        current().allowDiagonalAscend = allow;
        current().applyState();
    }

    public void avoidDodgingProjectile(Predicate<Entity> whenToDodge) {
        current().avoidDodgingProjectile.add(whenToDodge);
        // Not needed, nothing changes.
        // current().applyState();
    }

    public boolean shouldAvoidDodgingProjectile(Entity entity) {
        for (Predicate<Entity> test : current().avoidDodgingProjectile) {
            if (test.test(entity))
                return true;
        }
        return false;
    }

    public void allowShears(boolean allow) {
        current().allowShears = allow;
        current().applyState();
    }

    /// Stack management
    public void push() {
        if (states.empty()) {
            states.push(new State());
        } else {
            // Make copy and push that
            states.push(new State(current()));
        }
    }

    public void pop() {
        if (states.empty()) {
            Debug.logError("State stack is empty. This shouldn't be happening.");
            return;
        }
        State s = states.pop();
        s.applyState();
    }

    private State current() {
        if (states.empty()) {
            Debug.logError("STATE EMPTY, UNEMPTIED!");
            push();
        }
        return states.peek();
    }

    public class State {
        // TODO: 2021-05-22 public bad
        /// Baritone Params
        public double followOffsetDistance;
        public List<Item> throwawayItems = new ArrayList<>();
        public List<Item> protectedItems = new ArrayList<>();
        public boolean mineScanDroppedItems;
        public boolean walkThroughLava;
        public boolean allowDiagonalAscend;
        public boolean preferredStairs;

        // Alto Clef params
        public boolean exclusivelyMineLogs;
        public boolean forceFieldPlayers;
        public List<Predicate<Entity>> avoidDodgingProjectile = new ArrayList<>();

        public List<Predicate<Entity>> excludeFromForceField = new ArrayList<>();

        // Extra Baritone Settings
        public HashSet<BlockPos> blocksToAvoidBreaking = new HashSet<>();
        public List<Predicate<BlockPos>> toAvoidBreaking = new ArrayList<>();
        public List<Predicate<BlockPos>> toAvoidPlacing = new ArrayList<>();
        public List<Predicate<BlockPos>> allowWalking = new ArrayList<>();
        public boolean allowWalkThroughFlowingWater;
        public boolean allowShears;

        // Minecraft config
        public boolean pauseOnLostFocus = true;

        // Hard coded stuff
        public RaycastContext.FluidHandling rayFluidHandling;
        public boolean mineProcSearchAnyFlag;

        public State() {
            this(null);
        }

        public State(State toCopy) {
            // Read in current state
            readState(mod.getClientBaritoneSettings());

            readExtraState(mod.getExtraBaritoneSettings());

            readMinecraftState();

            if (toCopy != null) {
                // Copy over stuff from old one
                exclusivelyMineLogs = toCopy.exclusivelyMineLogs;
                avoidDodgingProjectile.addAll(toCopy.avoidDodgingProjectile);
                excludeFromForceField.addAll(toCopy.excludeFromForceField);
                forceFieldPlayers = toCopy.forceFieldPlayers;
            }
        }

        /**
         * Make the current state match our copy
         */
        public void applyState() {
            applyState(mod.getClientBaritoneSettings(), mod.getExtraBaritoneSettings());
        }

        /**
         * Read in a copy of the current state
         */
        private void readState(Settings s) {
            throwawayItems.clear();
            throwawayItems.addAll(s.acceptableThrowawayItems.value);
            followOffsetDistance = s.followOffsetDistance.value;
            mineScanDroppedItems = s.mineScanDroppedItems.value;
            walkThroughLava = s.assumeWalkOnLava.value;
            allowDiagonalAscend = s.allowDiagonalAscend.value;
            //preferredStairs = s.allowDownward.value;
        }

        private void readExtraState(AltoClefSettings settings) {
            synchronized (settings.getBreakMutex()) { // TODO: 2021-05-22 why is there synchronized nesting here?
                synchronized (settings.getPlaceMutex()) {
                    blocksToAvoidBreaking = new HashSet<>(settings.getBlocksToAvoidBreaking());
                    toAvoidBreaking = new ArrayList<>(settings.getBreakAvoiders());
                    toAvoidPlacing = new ArrayList<>(settings.getPlaceAvoiders());
                    protectedItems = new ArrayList<>(settings.getProtectedItems());
                    synchronized (settings.getPropertiesMutex()) {
                        allowWalking = new ArrayList<>(settings.getForceWalkOnPredicates());
                    }
                }
            }
            allowWalkThroughFlowingWater = settings.isFlowingWaterPassAllowed();
            allowShears = settings.areShearsAllowed();

            rayFluidHandling = RayTraceUtils.fluidHandling;
            mineProcSearchAnyFlag = MineProcess.searchAnyFlag;
        }

        private void readMinecraftState() {
            pauseOnLostFocus = MinecraftClient.getInstance().options.pauseOnLostFocus;
        }

        /**
         * Make the current state match our copy
         */
        private void applyState(Settings s, AltoClefSettings sa) {
            s.acceptableThrowawayItems.value.clear();
            s.acceptableThrowawayItems.value.addAll(throwawayItems);
            s.followOffsetDistance.value = followOffsetDistance;
            s.mineScanDroppedItems.value = mineScanDroppedItems;
            s.assumeWalkOnLava.value = walkThroughLava;
            s.allowDiagonalAscend.value = allowDiagonalAscend;

            // We need an alternrative method to handle this, this method makes navigation much less reliable.
            //s.allowDownward.value = preferredStairs;


            // Kinda jank but it works.
            synchronized (sa.getBreakMutex()) {
                synchronized (sa.getPlaceMutex()) { // TODO: 2021-05-22 why is there synchronized nesting here?
                    sa.getBreakAvoiders().clear();
                    sa.getBreakAvoiders().addAll(toAvoidBreaking);
                    sa.getBlocksToAvoidBreaking().clear();
                    sa.getBlocksToAvoidBreaking().addAll(blocksToAvoidBreaking);
                    sa.getPlaceAvoiders().clear();
                    sa.getPlaceAvoiders().addAll(toAvoidPlacing);
                    sa.getProtectedItems().clear();
                    sa.getProtectedItems().addAll(protectedItems);
                    synchronized (sa.getPropertiesMutex()) {
                        sa.getForceWalkOnPredicates().clear();
                        sa.getForceWalkOnPredicates().addAll(allowWalking);
                    }
                }
            }

            sa.setFlowingWaterPass(allowWalkThroughFlowingWater);
            sa.allowShears(allowShears);

            // Extra / hard coded
            RayTraceUtils.fluidHandling = rayFluidHandling; // ?
            MineProcess.searchAnyFlag = mineProcSearchAnyFlag;

            // Minecraft
            MinecraftClient.getInstance().options.pauseOnLostFocus = pauseOnLostFocus;
        }
    }
}
