package adris.altoclef.tasks.ArrowMapTests;

import adris.altoclef.AltoClef;
import baritone.api.utils.Rotation;
import baritone.api.utils.RotationUtils;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;

public class MultiArrowSearch {
    final Map<ArrowEntity, SingleArrowEscapeRouteSearch> asyncResultMap = new HashMap<>();

    public MultiArrowSearch(final List<ArrowEntity> arrows, final AltoClef mod) {
        arrows.forEach(arrow -> {
            asyncResultMap.put(arrow, new SingleArrowEscapeRouteSearch(arrow, mod));
        });
    }
}
