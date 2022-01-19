package adris.altoclef;

import adris.altoclef.butler.ButlerConfig;
import baritone.api.event.events.ChatEvent;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

/**
 * Connects client mixins to our AltoClef instance
 *
 * Mixins have no way (currently) to access our mod.
 * <p>
 * As a result I'll do this statically.
 * <p>
 * However, I want to avoid grabbing AltoClef in a static context, so this class
 * serves at the "static dumpster" which is the only spot where
 * I allow the bad practice of singletons to flourish
 */
public class StaticMixinHookups {

    private static AltoClef _mod;
    // for SOME REASON baritone triggers a block cancel breaking every other frame, so we have a 2 frame requirement for that?
    private static int _breakCancelFrames;

    public static void hookupMod(AltoClef mod) {
        _mod = mod;
    }

    public static void onInitializeLoad() {
        _mod.onInitializeLoad();
    }

    public static void onClientTick() {
        _mod.onClientTick();
    }

    public static void onClientRenderOverlay(MatrixStack stack) {
        _mod.onClientRenderOverlay(stack);
    }

    // Every chat message can be interrupted by us
    public static void onChat(ChatEvent e) {
        String line = e.getMessage();
        if (AltoClef.getCommandExecutor().isClientCommand(line)) {
            e.cancel();
            AltoClef.getCommandExecutor().execute(line);
        }
    }

    public static void onBlockBreaking(BlockPos pos, double progress) {
        _mod.getControllerExtras().onBlockBreak(pos, progress);
        _breakCancelFrames = 2;
    }

    public static void onBlockCancelBreaking() {
        if (_breakCancelFrames-- == 0) {
            _mod.getControllerExtras().onBlockStopBreaking();
        }
    }

    public static void onBlockBroken(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        //Debug.logMessage("BLOCK BROKEN: " + (world == _mod.getWorld()) + " : " + pos + " " + state.getBlock().getTranslationKey() + " " + player.getName().getString());
        _mod.getControllerExtras().onBlockBroken(world, pos, state, player);
    }

    public static void onBlockPlaced(BlockPos pos, BlockState state) {
        _mod.getControllerExtras().onBlockPlaced(pos, state);
    }

    public static void onScreenOpenBegin(Screen screen) {
        if (screen == null) {
            _mod.getContainerSubTracker().onScreenClose();
        }
    }

    public static void onScreenOpenEnd(Screen screen) {
        _mod.getContainerSubTracker().onScreenOpenFirstTick(screen);
    }

    public static void onBlockInteract(BlockHitResult hitResult, BlockState blockState) {
        _mod.getContainerSubTracker().onBlockInteract(hitResult.getBlockPos(), blockState.getBlock());
    }

    public static void onChunkLoad(WorldChunk chunk) {
        _mod.onChunkLoad(chunk);
    }

    public static void onChunkUnload(int x, int z) {
        _mod.onChunkUnload(new ChunkPos(x, z));
    }

    public static void onWhisperReceive(String user, String message) {
        _mod.getButler().receiveWhisper(user, message);
    }

    public static void onGameMessage(String message, boolean nonChat) {
        _mod.getOnGameMessage().invoke(message);
        boolean debug = ButlerConfig.getInstance().whisperFormatDebug;
        if (debug) {
            Debug.logMessage("RECEIVED WHISPER: \"" + message + "\".");
        }
        if (nonChat) {
            _mod.getButler().receiveMessage(message);
        } else if (debug) {
            Debug.logMessage("    Not Parsing: Not a chat message.");
        }
    }

    public static void onGameOverlayMessage(String message) {
        _mod.getOnGameOverlayMessage().invoke(message);
    }

    public static void onPlayerCollidedWithEntity(PlayerEntity player, Entity entity) {
        _mod.getEntityTracker().registerPlayerCollision(player, entity);
    }
}
