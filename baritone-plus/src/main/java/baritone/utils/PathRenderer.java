/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package baritone.utils;

import baritone.api.BaritoneAPI;
import baritone.api.event.events.RenderEvent;
import baritone.api.pathing.calc.IPath;
import baritone.api.pathing.goals.*;
import baritone.api.utils.BetterBlockPos;
import baritone.api.utils.Helper;
import baritone.api.utils.interfaces.IGoalRenderPos;
import baritone.behavior.PathingBehavior;
import baritone.pathing.path.PathExecutor;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.tileentity.BeaconTileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.world.DimensionType;

import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Brady
 * @since 8/9/2018
 */
public final class PathRenderer implements IRenderer, Helper {

    private static final ResourceLocation TEXTURE_BEACON_BEAM = new ResourceLocation("textures/entity/beacon_beam.png");


    private PathRenderer() {}

    public static double posX() {
        return renderManager.renderPosX();
    }

    public static double posY() {
        return renderManager.renderPosY();
    }

    public static double posZ() {
        return renderManager.renderPosZ();
    }

    public static void render(RenderEvent event, PathingBehavior behavior) {
        float partialTicks = event.getPartialTicks();
        Goal goal = behavior.getGoal();
        if (Helper.mc.currentScreen instanceof GuiClick) {
            ((GuiClick) Helper.mc.currentScreen).onRender(event.getModelViewStack(), event.getProjectionMatrix());
        }

        DimensionType thisPlayerDimension = behavior.baritone.getPlayerContext().world().getDimensionType();
        DimensionType currentRenderViewDimension = BaritoneAPI.getProvider().getPrimaryBaritone().getPlayerContext().world().getDimensionType();

        if (thisPlayerDimension != currentRenderViewDimension) {
            // this is a path for a bot in a different dimension, don't render it
            return;
        }

        Entity renderView = Helper.mc.getRenderViewEntity();

        if (renderView.world != BaritoneAPI.getProvider().getPrimaryBaritone().getPlayerContext().world()) {
            System.out.println("I have no idea what's going on");
            System.out.println("The primary baritone is in a different world than the render view entity");
            System.out.println("Not rendering the path");
            return;
        }

        if (goal != null && settings.renderGoal.value) {
            drawDankLitGoalBox(event.getModelViewStack(), renderView, goal, partialTicks, settings.colorGoalBox.value);
        }

        if (!settings.renderPath.value) {
            return;
        }

        PathExecutor current = behavior.getCurrent(); // this should prevent most race conditions?
        PathExecutor next = behavior.getNext(); // like, now it's not possible for current!=null to be true, then suddenly false because of another thread
        if (current != null && settings.renderSelectionBoxes.value) {
            drawManySelectionBoxes(event.getModelViewStack(), renderView, current.toBreak(), settings.colorBlocksToBreak.value);
            drawManySelectionBoxes(event.getModelViewStack(), renderView, current.toPlace(), settings.colorBlocksToPlace.value);
            drawManySelectionBoxes(event.getModelViewStack(), renderView, current.toWalkInto(), settings.colorBlocksToWalkInto.value);
        }

        //drawManySelectionBoxes(player, Collections.singletonList(behavior.pathStart()), partialTicks, Color.WHITE);

        // Render the current path, if there is one
        if (current != null && current.getPath() != null) {
            int renderBegin = Math.max(current.getPosition() - 3, 0);
            drawPath(event.getModelViewStack(), current.getPath(), renderBegin, settings.colorCurrentPath.value, settings.fadePath.value, 10, 20);
        }

        if (next != null && next.getPath() != null) {
            drawPath(event.getModelViewStack(), next.getPath(), 0, settings.colorNextPath.value, settings.fadePath.value, 10, 20);
        }

        // If there is a path calculation currently running, render the path calculation process
        behavior.getInProgress().ifPresent(currentlyRunning -> {
            currentlyRunning.bestPathSoFar().ifPresent(p -> {
                drawPath(event.getModelViewStack(), p, 0, settings.colorBestPathSoFar.value, settings.fadePath.value, 10, 20);
            });

            currentlyRunning.pathToMostRecentNodeConsidered().ifPresent(mr -> {
                drawPath(event.getModelViewStack(), mr, 0, settings.colorMostRecentConsidered.value, settings.fadePath.value, 10, 20);
                drawManySelectionBoxes(event.getModelViewStack(), renderView, Collections.singletonList(mr.getDest()), settings.colorMostRecentConsidered.value);
            });
        });
    }

    public static void drawPath(MatrixStack stack, IPath path, int startIndex, Color color, boolean fadeOut, int fadeStart0, int fadeEnd0) {
        IRenderer.startLines(color, settings.pathRenderLineWidthPixels.value, settings.renderPathIgnoreDepth.value);

        int fadeStart = fadeStart0 + startIndex;
        int fadeEnd = fadeEnd0 + startIndex;

        List<BetterBlockPos> positions = path.positions();
        for (int i = startIndex, next; i < positions.size() - 1; i = next) {
            BetterBlockPos start = positions.get(i);
            BetterBlockPos end = positions.get(next = i + 1);

            int dirX = end.x - start.x;
            int dirY = end.y - start.y;
            int dirZ = end.z - start.z;

            while (next + 1 < positions.size() && (!fadeOut || next + 1 < fadeStart) &&
                    (dirX == positions.get(next + 1).x - end.x &&
                            dirY == positions.get(next + 1).y - end.y &&
                            dirZ == positions.get(next + 1).z - end.z)) {
                end = positions.get(++next);
            }

            if (fadeOut) {
                float alpha;

                if (i <= fadeStart) {
                    alpha = 0.4F;
                } else {
                    if (i > fadeEnd) {
                        break;
                    }
                    alpha = 0.4F * (1.0F - (float) (i - fadeStart) / (float) (fadeEnd - fadeStart));
                }
                IRenderer.glColor(color, alpha);
            }

            drawLine(stack, start.x, start.y, start.z, end.x, end.y, end.z);

            tessellator.draw();
        }

        IRenderer.endLines(settings.renderPathIgnoreDepth.value);
    }


    public static void drawLine(MatrixStack stack, double x1, double y1, double z1, double x2, double y2, double z2) {
        Matrix4f matrix4f = stack.getLast().getMatrix();

        double vpX = posX();
        double vpY = posY();
        double vpZ = posZ();
        boolean renderPathAsFrickinThingy = !settings.renderPathAsLine.value;

        buffer.begin(renderPathAsFrickinThingy ? GL_LINE_STRIP : GL_LINES, DefaultVertexFormats.POSITION);
        buffer.pos(matrix4f, (float) (x1 + 0.5D - vpX), (float) (y1 + 0.5D - vpY), (float) (z1 + 0.5D - vpZ)).endVertex();
        buffer.pos(matrix4f, (float) (x2 + 0.5D - vpX), (float) (y2 + 0.5D - vpY), (float) (z2 + 0.5D - vpZ)).endVertex();

        if (renderPathAsFrickinThingy) {
            buffer.pos(matrix4f, (float) (x2 + 0.5D - vpX), (float) (y2 + 0.53D - vpY), (float) (z2 + 0.5D - vpZ)).endVertex();
            buffer.pos(matrix4f, (float) (x1 + 0.5D - vpX), (float) (y1 + 0.53D - vpY), (float) (z1 + 0.5D - vpZ)).endVertex();
            buffer.pos(matrix4f, (float) (x1 + 0.5D - vpX), (float) (y1 + 0.5D - vpY), (float) (z1 + 0.5D - vpZ)).endVertex();
        }
    }

    public static void drawManySelectionBoxes(MatrixStack stack, Entity player, Collection<BlockPos> positions, Color color) {
        IRenderer.startLines(color, settings.pathRenderLineWidthPixels.value, settings.renderSelectionBoxesIgnoreDepth.value);

        //BlockPos blockpos = movingObjectPositionIn.getBlockPos();
        BlockStateInterface bsi = new BlockStateInterface(BaritoneAPI.getProvider().getPrimaryBaritone().getPlayerContext()); // TODO this assumes same dimension between primary baritone and render view? is this safe?

        positions.forEach(pos -> {
            BlockState state = bsi.get0(pos);
            VoxelShape shape = state.getShape(player.world, pos);
            AxisAlignedBB toDraw = shape.isEmpty() ? VoxelShapes.fullCube().getBoundingBox() : shape.getBoundingBox();
            toDraw = toDraw.offset(pos);
            IRenderer.drawAABB(stack, toDraw, .002D);
        });

        IRenderer.endLines(settings.renderSelectionBoxesIgnoreDepth.value);
    }

    public static void drawDankLitGoalBox(MatrixStack stack, Entity player, Goal goal, float partialTicks, Color color) {
        double renderPosX = posX();
        double renderPosY = posY();
        double renderPosZ = posZ();
        double minX, maxX;
        double minZ, maxZ;
        double minY, maxY;
        double y1, y2;
        double y = MathHelper.cos((float) (((float) ((System.nanoTime() / 100000L) % 20000L)) / 20000F * Math.PI * 2));
        if (goal instanceof IGoalRenderPos) {
            BlockPos goalPos = ((IGoalRenderPos) goal).getGoalPos();
            minX = goalPos.getX() + 0.002 - renderPosX;
            maxX = goalPos.getX() + 1 - 0.002 - renderPosX;
            minZ = goalPos.getZ() + 0.002 - renderPosZ;
            maxZ = goalPos.getZ() + 1 - 0.002 - renderPosZ;
            if (goal instanceof GoalGetToBlock || goal instanceof GoalTwoBlocks) {
                y /= 2;
            }
            y1 = 1 + y + goalPos.getY() - renderPosY;
            y2 = 1 - y + goalPos.getY() - renderPosY;
            minY = goalPos.getY() - renderPosY;
            maxY = minY + 2;
            if (goal instanceof GoalGetToBlock || goal instanceof GoalTwoBlocks) {
                y1 -= 0.5;
                y2 -= 0.5;
                maxY--;
            }
        } else if (goal instanceof GoalXZ) {
            GoalXZ goalPos = (GoalXZ) goal;

            if (settings.renderGoalXZBeacon.value) {
                glPushAttrib(GL_LIGHTING_BIT);

                Helper.mc.getTextureManager().bindTexture(TEXTURE_BEACON_BEAM);
                if (settings.renderGoalIgnoreDepth.value) {
                    RenderSystem.disableDepthTest();
                }

                stack.push(); // push
                stack.translate(goalPos.getX() - renderPosX, -renderPosY, goalPos.getZ() - renderPosZ); // translate

                BeaconTileEntityRenderer.renderBeamSegment(
                        stack,
                        mc.getRenderTypeBuffers().getBufferSource(),
                        TEXTURE_BEACON_BEAM,
                        partialTicks,
                        1.0F,
                        player.world.getGameTime(),
                        0,
                        256,
                        color.getColorComponents(null),

                        // Arguments filled by the private method lol
                        0.2F,
                        0.25F
                );

                stack.pop(); // pop

                if (settings.renderGoalIgnoreDepth.value) {
                    RenderSystem.enableDepthTest();
                }

                glPopAttrib();
                return;
            }

            minX = goalPos.getX() + 0.002 - renderPosX;
            maxX = goalPos.getX() + 1 - 0.002 - renderPosX;
            minZ = goalPos.getZ() + 0.002 - renderPosZ;
            maxZ = goalPos.getZ() + 1 - 0.002 - renderPosZ;

            y1 = 0;
            y2 = 0;
            minY = 0 - renderPosY;
            maxY = 256 - renderPosY;
        } else if (goal instanceof GoalComposite) {
            for (Goal g : ((GoalComposite) goal).goals()) {
                drawDankLitGoalBox(stack, player, g, partialTicks, color);
            }
            return;
        } else if (goal instanceof GoalInverted) {
            drawDankLitGoalBox(stack, player, ((GoalInverted) goal).origin, partialTicks, settings.colorInvertedGoalBox.value);
            return;
        } else if (goal instanceof GoalYLevel) {
            GoalYLevel goalpos = (GoalYLevel) goal;
            minX = player.getPositionVec().x - settings.yLevelBoxSize.value - renderPosX;
            minZ = player.getPositionVec().z - settings.yLevelBoxSize.value - renderPosZ;
            maxX = player.getPositionVec().x + settings.yLevelBoxSize.value - renderPosX;
            maxZ = player.getPositionVec().z + settings.yLevelBoxSize.value - renderPosZ;
            minY = ((GoalYLevel) goal).level - renderPosY;
            maxY = minY + 2;
            y1 = 1 + y + goalpos.level - renderPosY;
            y2 = 1 - y + goalpos.level - renderPosY;
        } else {
            return;
        }

        IRenderer.startLines(color, settings.goalRenderLineWidthPixels.value, settings.renderGoalIgnoreDepth.value);

        renderHorizontalQuad(stack, minX, maxX, minZ, maxZ, y1);
        renderHorizontalQuad(stack, minX, maxX, minZ, maxZ, y2);

        Matrix4f matrix4f = stack.getLast().getMatrix();
        buffer.begin(GL_LINES, DefaultVertexFormats.POSITION);
        buffer.pos(matrix4f, (float) minX, (float) minY, (float) minZ).endVertex();
        buffer.pos(matrix4f, (float) minX, (float) maxY, (float) minZ).endVertex();
        buffer.pos(matrix4f, (float) maxX, (float) minY, (float) minZ).endVertex();
        buffer.pos(matrix4f, (float) maxX, (float) maxY, (float) minZ).endVertex();
        buffer.pos(matrix4f, (float) maxX, (float) minY, (float) maxZ).endVertex();
        buffer.pos(matrix4f, (float) maxX, (float) maxY, (float) maxZ).endVertex();
        buffer.pos(matrix4f, (float) minX, (float) minY, (float) maxZ).endVertex();
        buffer.pos(matrix4f, (float) minX, (float) maxY, (float) maxZ).endVertex();
        tessellator.draw();

        IRenderer.endLines(settings.renderGoalIgnoreDepth.value);
    }

    private static void renderHorizontalQuad(MatrixStack stack, double minX, double maxX, double minZ, double maxZ, double y) {
        if (y != 0) {
            Matrix4f matrix4f = stack.getLast().getMatrix();
            buffer.begin(GL_LINE_LOOP, DefaultVertexFormats.POSITION);
            buffer.pos(matrix4f, (float) minX, (float) y, (float) minZ).endVertex();
            buffer.pos(matrix4f, (float) maxX, (float) y, (float) minZ).endVertex();
            buffer.pos(matrix4f, (float) maxX, (float) y, (float) maxZ).endVertex();
            buffer.pos(matrix4f, (float) minX, (float) y, (float) maxZ).endVertex();
            tessellator.draw();
        }
    }
}
