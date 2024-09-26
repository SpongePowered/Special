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

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.ResourceKeyed;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.volume.stream.VolumeFlatMapper;
import org.spongepowered.api.world.volume.stream.VolumePredicate;
import org.spongepowered.royale.instance.InstanceImpl;

import java.util.Objects;
import java.util.StringJoiner;

public abstract class InstanceMutator implements ResourceKeyed {

    private final ResourceKey key;

    protected InstanceMutator(final ResourceKey key) {
        this.key = key;
    }

    @Override
    public final ResourceKey key() {
        return this.key;
    }

    public void prepare(final InstanceImpl instance) {

    }

    public abstract VolumePredicate<ServerWorld, BlockEntity> getBlockEntityPredicate(final InstanceImpl instance);

    public abstract VolumeFlatMapper<ServerWorld, BlockEntity> getBlockEntityMapper(final InstanceImpl instance);

    @Override
    public int hashCode() {
        return this.key.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final InstanceMutator that = (InstanceMutator) o;
        return Objects.equals(this.key, that.key);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", InstanceMutator.class.getSimpleName() + "[", "]")
                .add("key=" + this.key)
                .toString();
    }
}
