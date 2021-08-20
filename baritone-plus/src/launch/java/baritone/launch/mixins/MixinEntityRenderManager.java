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

package baritone.launch.mixins;

import baritone.utils.accessor.IEntityRenderManager;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityRendererManager.class)
public class MixinEntityRenderManager implements IEntityRenderManager {


    @Override
    public double renderPosX() {
        return ((EntityRendererManager) (Object) this).info.getProjectedView().x;
    }

    @Override
    public double renderPosY() {
        return ((EntityRendererManager) (Object) this).info.getProjectedView().y;
    }

    @Override
    public double renderPosZ() {
        return ((EntityRendererManager) (Object) this).info.getProjectedView().z;
    }
}
