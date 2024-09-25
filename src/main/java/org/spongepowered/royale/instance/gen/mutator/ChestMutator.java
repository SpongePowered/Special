/*
 * This file is part of Royale, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <http://github.com/SpongePowered>
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

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.Sign;
import org.spongepowered.api.block.entity.carrier.chest.Chest;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.weighted.LootTable;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.volume.stream.VolumeFlatMapper;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;
import org.spongepowered.royale.Constants;
import org.spongepowered.royale.Royale;
import org.spongepowered.royale.instance.InstanceImpl;
import org.spongepowered.royale.instance.gen.loot.ItemArchetype;
import org.spongepowered.royale.instance.gen.loot.Loots;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public final class ChestMutator extends SignMutator {

    private final Random random = new Random();

    public ChestMutator() {
        super(ResourceKey.of(Constants.Plugin.ID, "chest"), "chest");
    }

    @Override
    public VolumeFlatMapper<ServerWorld, BlockEntity> getBlockEntityMapper(final InstanceImpl instance) {
        return (world, blockentitySupplier, x, y, z) -> {
            final Sign sign = (Sign) blockentitySupplier.get();
            final Direction facingDirection = sign.get(Keys.DIRECTION).orElse(null);
            final String lootTableId = PlainTextComponentSerializer.plainText().serialize(sign.lines().get(1));
            final LootTable<ItemArchetype> lootTable = Loots.getTable(lootTableId.toLowerCase());
            final List<ItemArchetype> items = lootTable.get(this.random);

            Royale.getInstance().getPlugin().logger().info("Generating loot chest via table '{}' at {}x, {}y, {}z", lootTableId.toLowerCase(), x, y, z);
            final BlockState defaultChestState = BlockTypes.CHEST.get().defaultState();
            final BlockState newChestState = defaultChestState.with(Keys.DIRECTION, facingDirection)
                .orElse(defaultChestState);
            final Vector3i pos = new Vector3d(x, y, z).toInt();
            world.setBlock(pos, newChestState, BlockChangeFlags.ALL);

            final BlockEntity blockEntity = world.blockEntity(pos)
                .orElseThrow(() -> new IllegalStateException("Something is quite wrong...we set a Chest down yet found no block entity. This is a serious issue likely due to server misconfiguration!"));
            if (!(blockEntity instanceof Chest)) {
                throw new IllegalStateException(String.format("Something is quite wrong...we set a Chest down yet found a [%s] instead. This is a "
                    + "serious issue likely due to server misconfiguration!", blockEntity.getClass().getSimpleName()));
            }
            final Chest chest = (Chest) blockEntity;
            for (final ItemArchetype item : items) {
                chest.inventory().offer(item.create(this.random));
            }

            return Optional.of(chest);
        };
    }
}
