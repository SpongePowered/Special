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
import org.spongepowered.royale.instance.InstanceType;
import org.spongepowered.royale.instance.exception.UnknownInstanceException;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface InstanceManager {

    //TODO type is impl
    CompletableFuture<Instance> createInstance(final ResourceKey key, final InstanceType type, final boolean force);

    CompletableFuture<Boolean> unloadInstance(final ResourceKey key);

    void startInstance(final ResourceKey key) throws UnknownInstanceException;

    void endInstance(final ResourceKey key) throws UnknownInstanceException;

    Optional<Instance> getInstance(final ResourceKey key);

    Collection<Instance> getAll();

}
