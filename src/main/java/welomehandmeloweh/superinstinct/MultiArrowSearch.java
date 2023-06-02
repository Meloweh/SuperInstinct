package welomehandmeloweh.superinstinct;

import adris.altoclef.AltoClef;
import net.minecraft.entity.projectile.ArrowEntity;

import java.util.*;

/**
 * SuperInstinct is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SuperInstinct is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SuperInstinct.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2023 MelowehAndWelomeh
 */
public class MultiArrowSearch {
    final Map<ArrowEntity, SingleArrowEscapeRouteSearch> asyncResultMap = new HashMap<>();

    public MultiArrowSearch(final List<ArrowEntity> arrows, final AltoClef mod) {
        arrows.forEach(arrow -> {
            asyncResultMap.put(arrow, new SingleArrowEscapeRouteSearch(arrow, mod));
        });
    }
}
