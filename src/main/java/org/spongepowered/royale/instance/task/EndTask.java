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
package org.spongepowered.royale.instance.task;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.royale.Royale;
import org.spongepowered.royale.instance.InstanceImpl;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public final class EndTask extends InstanceTask {

    private final boolean mustStopManually;
    private final long endLengthTotal;
    private long endLengthRemaining;

    public EndTask(final InstanceImpl instance) {
        super(instance);
        this.endLengthTotal = instance.getType().getRoundEndLength();
        this.mustStopManually = this.endLengthTotal == -1;
        this.endLengthRemaining = this.mustStopManually ? Long.MAX_VALUE : this.endLengthTotal;
    }

    @Override
    public void accept(final ScheduledTask task) {
        final ServerWorld world = this.instance.world();

        if (this.mustStopManually) {
            return;
        }

        if (this.endLengthRemaining < 0) {
            throw new IllegalStateException("End phase should be over but the end task is still running");
        }

        final Optional<UUID> winnerOpt = this.instance.getWinner();
        if (winnerOpt.isEmpty()) {
            Royale.getInstance().getPlugin().logger().warn("{} ended without a winner", this.instance.getWorldKey().formatted());
            this.instance.advance();
            return;
        }

        // First tick, kickoff end sequence
        if (this.endLengthTotal == this.endLengthRemaining) {

            // Find ServerPlayer -> cached GameProfile -> Unknown
            final Optional<ServerPlayer> winner = Sponge.server().player(winnerOpt.get());
            final Component name = winner.flatMap(p -> p.get(Keys.DISPLAY_NAME))
                    .orElse(Sponge.server().gameProfileManager().cache().findById(winnerOpt.get()).flatMap(GameProfile::name).map(Component::text)
                            .orElse(Component.text("Unknown")));

            winner.ifPresent(player -> player.spawnParticles(ParticleEffect.builder()
                            .type(ParticleTypes.FIREWORK)
                            .quantity(30)
                            .build(), player.location().position()));

            Sponge.server().broadcastAudience()
                    .sendMessage(LinearComponents.linear(NamedTextColor.GREEN, name,
                            NamedTextColor.WHITE, Component.text(" has won the game!")));

            final Title title = Title.title(LinearComponents.linear(NamedTextColor.GREEN, name,
                    NamedTextColor.WHITE, Component.text(" is the winner!")), Component.empty(),
                    Title.Times.times(Duration.ZERO, Duration.ofSeconds(this.endLengthTotal - 2), Duration.ofSeconds(1)));

            if (winner.isEmpty()) {
                world.showTitle(title);
            } else {
                final Title winnerTitle = Title.title(Component.text("You are the winner!", NamedTextColor.GREEN), Component.empty(),
                        Title.Times.times(Duration.ZERO, Duration.ofSeconds(this.endLengthTotal - 2), Duration.ofSeconds(1)));
                for (final ServerPlayer player : world.players()) {
                    if (player.uniqueId().equals(winner.get().uniqueId())) {
                        player.showTitle(winnerTitle);
                    } else {
                        player.showTitle(title);
                    }
                }
            }

            Royale.getInstance().getPlugin().logger().info("Round finished in {}!", this.instance.getWorldKey());
        }

        if (this.endLengthRemaining-- == 0) {
            this.instance.advance();
        }
    }
}
