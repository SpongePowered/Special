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
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.Sign;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.volume.stream.VolumePredicate;
import org.spongepowered.royale.Royale;
import org.spongepowered.royale.instance.InstanceImpl;
import org.spongepowered.royale.instance.gen.InstanceMutator;

import java.util.StringJoiner;

abstract class SignMutator extends InstanceMutator {

    private final String signId;

    SignMutator(final ResourceKey key, final String signId) {
        super(key);
        this.signId = signId;
    }

    @Override
    public VolumePredicate<ServerWorld, BlockEntity> getBlockEntityPredicate(final InstanceImpl instance) {
        return (world, blockEntitySupplier, x, y, z) -> {
            final BlockEntity blockEntity = blockEntitySupplier.get();
            if (!(blockEntity instanceof Sign)) {
                return false;
            }
            final Sign sign = (Sign) blockEntity;

            if (!PlainTextComponentSerializer.plainText().serialize(sign.lines().get(0)).equalsIgnoreCase(this.signId)) {
                if (PlainTextComponentSerializer.plainText().serialize(sign.lines().get(1)).equalsIgnoreCase(this.signId)) {
                    Royale.getInstance().getPlugin().logger().error("Found mismatched sign at {}x {}y {}z!", x, y, z);
                } else {
                    return false;
                }
            }
            return true;
        };
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]")
                .add("key=" + this.key())
                .add("signId=" + this.signId)
                .toString();
    }
}
