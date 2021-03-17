
package adris.altoclef.trackers;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.WorldUtil;
import adris.altoclef.util.baritone.BaritoneHelper;
import adris.altoclef.util.csharpisbetter.Timer;
import adris.altoclef.util.csharpisbetter.Util;
import baritone.api.utils.BlockOptionalMetaLookup;
import baritone.pathing.movement.CalculationContext;
import baritone.process.MineProcess;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 * Blocks tracks the way I want it, When I want it.
 *
 */
public class BlockTracker extends Tracker {

    private static final int DEFAULT_REACH_ATTEMPTS_ALLOWED = 4;

    private final HashMap<Dimension, PosCache> _caches = new HashMap<>();

    //private final PosCache _cache = new PosCache(100, 64*1.5);

    private final Timer _timer = new Timer(7.0);

    private Map<Block, Integer> _trackingBlocks = new HashMap<>();

    //private Block _currentlyTracking = null;
    private AltoClef _mod;

    public BlockTracker(AltoClef mod, TrackerManager manager) {
        super(manager); _mod = mod;
    }

    @Override
    protected void updateState() {
        if (shouldUpdate()) {
            update();
        }
    }

    @Override
    protected void reset() {
        _trackingBlocks.clear();
        for (PosCache cache : _caches.values()) {
            cache.clear();
        }
    }

    public boolean isTracking(Block block) {
        return _trackingBlocks.containsKey(block) && _trackingBlocks.get(block) > 0;
    }

    public void trackBlock(Block ...blocks) {
        for (Block block : blocks) {
            if (!_trackingBlocks.containsKey(block)) {
                // We're tracking a new block, so we're not updated.
                setDirty();
                _trackingBlocks.put(block, 0);
            }
            _trackingBlocks.put(block, _trackingBlocks.get(block) + 1);
        }
    }

    public void stopTracking(Block ...blocks) {
        for (Block block : blocks) {
            if (_trackingBlocks.containsKey(block)) {
                int current = _trackingBlocks.get(block);
                if (current == 0) {
                    Debug.logWarning("Untracked block " + block + " more times than necessary. BlockTracker stack is unreliable from this point on.");
                } else {
                    _trackingBlocks.put(block, current - 1);
                    if (_trackingBlocks.get(block) <= 0) {
                        _trackingBlocks.remove(block);
                    }
                }
            }
        }
    }

    public void addBlock(Block block, BlockPos pos) {
        if (blockIsValid(pos, block)) {
            currentCache().addBlock(block, pos);
        } else {
            Debug.logInternal("INVALID SET: " + block + " " + pos);
        }
    }
    public boolean anyFound(Block ...blocks) {
        updateState();
        return currentCache().anyFound(blocks);
    }

    public BlockPos getNearestTracking(Vec3d pos, Block ...blocks) {
        return getNearestTracking(pos, (p) -> false, blocks);
    }
    public BlockPos getNearestTracking(Vec3d pos, Predicate<BlockPos> isInvalidTest, Block... blocks) {
        for (Block block : blocks) {
            if (!_trackingBlocks.containsKey(block)) {
                Debug.logWarning("BlockTracker: Not tracking block " + block + " right now.");
                return null;
            }
        }
        // Make sure we've scanned the first time if we need to.
        updateState();
        return currentCache().getNearest(_mod, pos, isInvalidTest, blocks);
    }

    public List<BlockPos> getKnownLocations(Block ...blocks) {
        updateState();
        return currentCache().getKnownLocations(blocks);
    }

    public BlockPos getNearestWithinRange(BlockPos pos, double range, Block ...blocks) {
        return getNearestWithinRange(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), range, blocks);
    }
    public BlockPos getNearestWithinRange(Vec3d pos, double range, Block ...blocks) {
        int minX = (int)Math.round(pos.x - range),
            maxX = (int)Math.round(pos.x + range),
            minY = (int)Math.round(pos.y - range),
            maxY = (int)Math.round(pos.y + range),
            minZ = (int)Math.round(pos.z - range),
            maxZ = (int)Math.round(pos.z + range);
        double closestDistance = Float.POSITIVE_INFINITY;
        BlockPos nearest = null;
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    BlockPos check = new BlockPos(x, y, z);
                    if (currentCache().blockUnreachable(check)) continue;

                    assert MinecraftClient.getInstance().world != null;
                    Block b = MinecraftClient.getInstance().world.getBlockState(check).getBlock();
                    boolean valid = false;
                    for (Block type : blocks) {
                        if (type.is(b)) {
                            valid = true;
                            break;
                        }
                    }
                    if (!valid) continue;
                    if (check.isWithinDistance(pos, range)) {
                        double sq = check.getSquaredDistance(pos, false);
                        if (sq < closestDistance) {
                            closestDistance = sq;
                            nearest = check;
                        }
                    }
                }
            }
        }
        return nearest;
    }

    private boolean shouldUpdate() {
        return _timer.elapsed();
    }
    private void update() {
        _timer.reset();
        // Perform a baritone scan.
        rescanWorld();
    }

    private void rescanWorld() {
        Debug.logInternal("Rescanning world for " + _trackingBlocks.size() + " blocks... Hopefully not dummy slow.");
        CalculationContext ctx = new CalculationContext(_mod.getClientBaritone());
        Block[] blocksToScan = new Block[_trackingBlocks.size()];
        _trackingBlocks.keySet().toArray(blocksToScan);
        List<BlockPos> knownBlocks = currentCache().getKnownLocations(blocksToScan);

        // Clear invalid block pos before rescan
        for (BlockPos check : knownBlocks) {
            if (!blockIsValid(check, blocksToScan)) {
                //Debug.logInternal("Removed at " + check);
                currentCache().removeBlock(check, blocksToScan);
            }
        }

        BlockOptionalMetaLookup boml = new BlockOptionalMetaLookup(blocksToScan);
        List<BlockPos> found = MineProcess.searchWorld(ctx, boml, 64, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        if (MinecraftClient.getInstance().world != null) {
            for (BlockPos pos : found) {
                Block block = MinecraftClient.getInstance().world.getBlockState(pos).getBlock();

                if (_trackingBlocks.containsKey(block)) {
                    //Debug.logInternal("Good: " + block + " at " + pos);
                    currentCache().addBlock(block, pos);
                } else {
                    //Debug.logInternal("INVALID??? FOUND: " + block + " at " + pos);
                }
            }

            // Purge if we have too many blocks tracked at once.
            currentCache().smartPurge(_mod.getPlayer().getPos());
        }
    }

    // Checks whether it would be WRONG to say "at pos the block is block"
    // Returns true if wrong, false if correct OR undetermined/unsure.
    public boolean blockIsValid(BlockPos pos, Block ...blocks) {
        // We can't reach it, don't even try.
        if (currentCache().blockUnreachable(pos)) {
            return false;
        }
        // It might be OK to remove this. Will have to test.
        if (!_mod.getChunkTracker().isChunkLoaded(pos)) {
            Debug.logInternal("(failed chunkcheck: " + new ChunkPos(pos) + ")");
            return true;
        }
        // I'm bored
        ClientWorld zaWarudo = MinecraftClient.getInstance().world;
        // No world, therefore we don't assume block is invalid.
        if (zaWarudo == null) {
            return true;
        }
        try {
            for (Block block : blocks) {
                if (zaWarudo.isAir(pos) && WorldUtil.isAir(block)) {
                    return true;
                }
                BlockState state = zaWarudo.getBlockState(pos);
                if (state.getBlock().is(block)) {
                    return true;
                }
            }
            return false;
        } catch (NullPointerException e) {
            // Probably out of chunk. This means we can't judge its state.
            return true;
        }
    }


    public void requestBlockUnreachable(BlockPos pos, int allowedFailures) {
        currentCache().blacklistBlockUnreachable(_mod, pos, allowedFailures);
    }

    public void requestBlockUnreachable(BlockPos pos) {
        requestBlockUnreachable(pos, DEFAULT_REACH_ATTEMPTS_ALLOWED);
    }

        private PosCache currentCache() {
        Dimension dimension = _mod.getCurrentDimension();
        if (!_caches.containsKey(dimension)) {
            _caches.put(dimension, new PosCache(100, 64*1.5));
        }
        return _caches.get(dimension);
    }


    static class PosCache {
        private final HashMap<Block, List<BlockPos>> _cachedBlocks = new HashMap<>();

        private final HashMap<BlockPos, Block> _cachedByPosition = new HashMap<>();

        private final WorldLocateBlacklist _blacklist = new WorldLocateBlacklist();

        // Once we have too many blocks, start cutting them off. First only the ones that are far enough.
        private final double _cutoffRadius;
        private final int _cutoffSize;

        public PosCache(int cutoffSize, double cutoffRadius) {
            _cutoffSize = cutoffSize;
            _cutoffRadius = cutoffRadius;
        }

        public boolean anyFound(Block ...blocks) {
            for (Block block : blocks) {
                if (_cachedBlocks.containsKey(block)) return true;
            }
            return false;
        }

        public List<BlockPos> getKnownLocations(Block ...blocks) {
            List<BlockPos> result = new ArrayList<>();
            for (Block block : blocks) {
                List<BlockPos> found = _cachedBlocks.get(block);
                if (found != null) {
                    result.addAll(found);
                }
            }
            return result;
        }

        public void removeBlock(BlockPos pos, Block ...blocks) {
            for (Block block : blocks) {
                if (_cachedBlocks.containsKey(block)) {
                    _cachedBlocks.get(block).remove(pos);
                    _cachedByPosition.remove(pos);
                    if (_cachedBlocks.get(block).size() == 0) {
                        _cachedBlocks.remove(block);
                    }
                }
            }
        }

        public void addBlock(Block block, BlockPos pos) {
            if (blockUnreachable(pos)) return;
            if (_cachedByPosition.containsKey(pos)) {
                if (_cachedByPosition.get(pos) == block) {
                    // We're already tracked
                    return;
                } else {
                    // We're tracked incorrectly, fix
                    removeBlock(pos, block);
                }
            }
            if (!anyFound(block)) {
                _cachedBlocks.put(block, new ArrayList<>());
            }
            _cachedBlocks.get(block).add(pos);
            _cachedByPosition.put(pos, block);
        }


        public void clear() {
            Debug.logInternal("CLEARED BLOCK CACHE");
            _cachedBlocks.clear();
            _cachedByPosition.clear();
            _blacklist.clear();
        }

        public int getBlockTrackCount() {
            int count = 0;
            for (List<BlockPos> list : _cachedBlocks.values()) {
                count += list.size();
            }
            return count;
        }

        public void blacklistBlockUnreachable(AltoClef mod, BlockPos pos, int allowedFailures) {
            _blacklist.blackListBlock(mod, pos, allowedFailures);
        }
        public boolean blockUnreachable(BlockPos pos) {
            return _blacklist.unreachable(pos);
        }

        // Gets nearest block. For now does linear search. In the future might optimize this a bit
        public BlockPos getNearest(AltoClef mod, Vec3d position, Predicate<BlockPos> isInvalid, Block ...blocks) {
            if (!anyFound(blocks)) {
                //Debug.logInternal("(failed cataloguecheck for " + block.getTranslationKey() + ")");
                return null;
            }

            BlockPos closest = null;
            double minScore = Double.POSITIVE_INFINITY;

            List<BlockPos> blockList = getKnownLocations(blocks);

            int toPurge = blockList.size() - _cutoffSize;

            boolean closestPurged = false;

            for (BlockPos pos : blockList) {
                if (isInvalid.test(pos)) continue;

                // If our current block isn't valid, fix it up. This cleans while we're iterating.
                if (!mod.getBlockTracker().blockIsValid(pos, blocks)) {
                    removeBlock(pos, blocks);
                    continue;
                }

                double score = BaritoneHelper.calculateGenericHeuristic(position, Util.toVec3d(pos));

                boolean currentlyClosest = false;
                boolean purged = false;

                if (score < minScore) {
                    minScore = score;
                    closest = pos;
                    currentlyClosest = true;
                }

                if (toPurge > 0) {
                    double sqDist = position.squaredDistanceTo(Util.toVec3d(pos));
                    if (sqDist > _cutoffRadius*_cutoffRadius) {
                        // cut this one off.
                        for (Block block : blocks) {
                            if (_cachedBlocks.containsKey(block)) {
                                removeBlock(pos, block);
                            }
                        }
                        toPurge --;
                        purged = true;
                    }
                }

                if (currentlyClosest) {
                    closestPurged = purged;
                }
            }

            while (toPurge > 0) {
                if (blockList.size() == 0) {
                    //noinspection UnusedAssignment
                    toPurge = 0;
                    break;
                }
                blockList.remove(blockList.size() - 1);
                toPurge --;
            }

            // Special case: Our closest was purged. Add us back.
            if (closestPurged) {
                Debug.logInternal("Rare edge case: Closest block was purged cause it was real far away, it will now be added back.");
                blockList.add(closest);
            }

            return closest;
        }

        /**
         * Purge enough blocks so our size is small enough
         */
        public void smartPurge(Vec3d playerPos) {
            for (Block block : _cachedBlocks.keySet()) {
                List<BlockPos> tracking = _cachedBlocks.get(block);

                // Clear blacklisted blocks
                try {
                    tracking = tracking.stream()
                            .filter(pos -> !_blacklist.unreachable(pos))
                            .distinct()
                            .sorted((BlockPos left, BlockPos right) -> {
                                double leftDist = left.getSquaredDistance(playerPos, false);
                                double rightDist = right.getSquaredDistance(playerPos, false);
                                // 1 if left is further
                                // -1 if left is closer
                                if (leftDist > rightDist) {
                                    return 1;
                                } else if (leftDist < rightDist) {
                                    return -1;
                                }
                                return 0;
                            })
                            .limit(_cutoffSize)
                            .collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    // Comparison method violates its general contract: Sometimes transitivity breaks.
                    // In which case, ignore it.
                    Debug.logWarning("Failed to purge/reduce block search count for " + block + ": It remains at " + tracking.size());
                }
            }
        }
    }
}
