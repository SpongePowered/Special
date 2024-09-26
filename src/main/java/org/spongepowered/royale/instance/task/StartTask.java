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
package org.spongepowered.royale.instance.task;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.royale.instance.InstanceImpl;

import java.time.Duration;
import java.util.Collections;

public final class StartTask extends InstanceTask {

    private int seconds = 0;

    public StartTask(final InstanceImpl instance) {
        super(instance);
    }

    @Override
    public void accept(final ScheduledTask task) {
        final ServerWorld world = this.instance.world();

        long remaining = this.instance.getType().getRoundStartLength() - this.seconds;
        if (remaining < 0) {
            throw new IllegalStateException("Instance should have already started but the start task is still running");
        }

        final Title.Times times = Title.Times.times(Duration.ZERO, Duration.ofMillis(600), Duration.ofMillis(400));

        if (remaining == 0) {
            final Component template = this.instance.getType().getRoundStartTemplate().parse(null, Collections.emptyMap());
            world.showTitle(Title.title(template, Component.empty(), times));
            this.instance.advance();
        }

        Title title;
        if (remaining == 1) {
            title = Title.title(Component.text("1", NamedTextColor.GOLD), Component.empty(), times);
        } else if (remaining == 2) {
            title = Title.title(Component.text("2", NamedTextColor.RED), Component.empty(), times);
        } else {
            title = Title.title(Component.text(remaining, NamedTextColor.DARK_RED), Component.empty(), times);
        }
        world.showTitle(title);
        this.seconds++;
    }
}
