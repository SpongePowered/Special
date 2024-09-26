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
package org.spongepowered.royale.instance.gen;

import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeCollectors;
import org.spongepowered.math.vector.Vector2i;
import org.spongepowered.math.vector.Vector3i;
import org.spongepowered.royale.Royale;
import org.spongepowered.royale.instance.InstanceImpl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Mutates the map templates after loading into the final form.
 */
public final class InstanceMutatorPipeline {

    private final Set<InstanceMutator> mutators;

    public InstanceMutatorPipeline() {
        this.mutators = new HashSet<>();
    }

    public Collection<InstanceMutator> getMutators() {
        return this.mutators;
    }

    public void mutate(final InstanceImpl instance) {

        final ServerWorld world = instance.world();

        final double r = world.border().diameter() / 2;

        final Vector2i center = world.border().center().toInt();
        final Vector3i min = new Vector3i(center.x() - r, world.min().y(), center.y() - r);
        final Vector3i max = new Vector3i(center.x() + r, world.max().y(), center.y() + r);

        for (final InstanceMutator mutator : this.mutators) {
            Royale.getInstance().getPlugin().logger().info("Mutating instance [{}] with mutator [{}]...", instance.getWorldKey(), mutator.key());
        }

        Royale.getInstance().getPlugin().logger().info("[Mutator] Performing pass for instance {} - {} blocks total.", instance.getWorldKey(),
                world.border().diameter() * world.border().diameter());

        for (InstanceMutator instanceMutator : this.mutators) {
            instanceMutator.prepare(instance);
        }

        for (InstanceMutator mutator : this.mutators) {
            world.blockEntityStream(min, max, StreamOptions.lazily())
                    .filter(mutator.getBlockEntityPredicate(instance))
                    .flatMap(mutator.getBlockEntityMapper(instance))
                    .apply(VolumeCollectors.applyBlockEntitiesOrRemove(world));
        }

        Royale.getInstance().getPlugin().logger().info("[Mutator] Done.");
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", InstanceMutatorPipeline.class.getSimpleName() + "[", "]")
                .add("mutators=" + this.mutators)
                .toString();
    }
}
