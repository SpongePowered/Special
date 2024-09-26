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

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.royale.instance.InstanceImpl;

public final class ProgressTask extends InstanceTask {

    private final BossBar bossBar;
    private final long roundLengthTotal;
    private final boolean infinite;
    private long roundLengthRemaining;

    public ProgressTask(final InstanceImpl instance, BossBar bossBar) {
        super(instance);
        this.bossBar = bossBar;
        this.roundLengthTotal = instance.getType().getRoundLength();
        this.infinite = this.roundLengthTotal == -1;
        this.roundLengthRemaining = this.infinite ? Long.MAX_VALUE : this.roundLengthTotal;
    }

    @Override
    public void accept(final ScheduledTask task) {
        final ServerWorld world = this.instance.world();

        if (this.infinite) {
            this.bossBar.progress(1);
            this.bossBar.color(BossBar.Color.GREEN);
            this.bossBar.name(Component.text("Time remaining: --")); //TODO
            world.showBossBar(this.bossBar);
            return;
        }

        if (this.roundLengthRemaining < 0) {
            throw new IllegalStateException("Round should be over but the progress task is still running");
        }

        final float percent = (float) this.roundLengthRemaining / this.roundLengthTotal;
        this.bossBar.progress(percent);

        if (percent < 0.33) {
            this.bossBar.color(BossBar.Color.RED);
        } else if (percent < 0.66) {
            this.bossBar.color(BossBar.Color.YELLOW);
        } else {
            this.bossBar.color(BossBar.Color.GREEN);
        }

        final int seconds = (int) this.roundLengthRemaining % 60;
        if (this.roundLengthRemaining >= 60) {
            final int minutes = (int) this.roundLengthRemaining / 60;
            this.bossBar.name(Component.text(String.format("Time remaining: %02d:%02d", minutes, seconds)));
        } else {
            this.bossBar.name(Component.text(String.format("Time remaining: %02d", seconds)));
        }

        world.showBossBar(this.bossBar);

        if (this.roundLengthRemaining-- == 0) {
            this.instance.advance();
        }
    }

    @Override
    public void cleanup() {
        final ServerWorld world = this.instance.world();
        world.hideBossBar(this.bossBar);
    }
}
