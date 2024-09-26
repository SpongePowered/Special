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
package org.spongepowered.royale.api;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.entity.Sign;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.royale.instance.InstanceType;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface Instance {

    boolean addSpawnpoint(Vector3d vector3d);

    boolean addPlayer(ServerPlayer player);

    boolean removePlayer(ServerPlayer player);

    boolean addSpectator(ServerPlayer player);

    boolean removeSpectator(ServerPlayer player);

    boolean isPlayerRegistered(ServerPlayer player);

    boolean isPlayerAlive(ServerPlayer player);

    boolean isFull();

    Optional<UUID> getWinner();

    Collection<UUID> getPlayers();

    InstanceType getType();

    InstanceState getState();

    ResourceKey getWorldKey();

    ServerWorld world();

    boolean link(Sign sign);

}
