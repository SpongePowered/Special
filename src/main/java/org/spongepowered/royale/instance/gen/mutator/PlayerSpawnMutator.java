/*
 * This file is part of Royale, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://github.com/SpongePowered>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.royale.instance.gen.mutator;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.volume.stream.VolumeFlatMapper;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;
import org.spongepowered.royale.Constants;
import org.spongepowered.royale.Royale;
import org.spongepowered.royale.instance.InstanceImpl;

import java.util.Optional;

public final class PlayerSpawnMutator extends SignMutator {

    public PlayerSpawnMutator() {
        super(ResourceKey.of(Constants.Plugin.ID, "player_spawn"), "player_spawn");
    }

    @Override
    public VolumeFlatMapper<ServerWorld, BlockEntity> getBlockEntityMapper(final InstanceImpl instance) {
        return (world, blockEntitySupplier, x, y, z) -> {
            final BlockState air = BlockTypes.AIR.get().defaultState();
            final Vector3i pos = new Vector3d(x, y, z).toInt();
            world.setBlock(pos, air, BlockChangeFlags.ALL);
            // Always remove the block entity
            world.removeBlockEntity(pos);

            instance.addSpawnpoint(new Vector3d(x + 0.5, y + 0.0125, z + 0.5));
            Royale.getInstance().getPlugin().logger().info("Found player spawn at {}x, {}y, {}z.", x, y, z);
            return Optional.empty();
        };
    }
}
