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
package org.spongepowered.royale.instance;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.adventure.SpongeComponents;
import org.spongepowered.api.block.entity.Sign;
import org.spongepowered.api.block.transaction.Operations;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.entity.ChangeEntityWorldEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.royale.Constants;
import org.spongepowered.royale.Royale;
import org.spongepowered.royale.api.RoyaleKeys;

import java.util.List;
import java.util.Optional;

public final class EventHandler {

    @Listener(order = Order.LAST)
    public void onJoin(final ServerSideConnectionEvent.Join event, @Getter("player") final ServerPlayer player) {
        final ServerWorld world = player.world();
        final Optional<InstanceImpl> instanceOpt = Royale.getInstance().getInstanceManager().getInstance(world.key());
        if (!instanceOpt.isPresent()) {
            return;
        }

        if (instanceOpt.get().isPlayerRegistered(player.uniqueId())) {
            instanceOpt.get().spectate(player);
        }
    }

    @Listener(order = Order.LAST)
    public void onDisconnect(final ServerSideConnectionEvent.Disconnect event, @Getter("player") final ServerPlayer player) {
        final ServerWorld world = player.world();
        final Optional<InstanceImpl> instanceOpt = Royale.getInstance().getInstanceManager().getInstance(world.key());
        if (!instanceOpt.isPresent()) {
            return;
        }

        if (instanceOpt.get().isPlayerRegistered(player.uniqueId())) {
            instanceOpt.get().disqualifyPlayer(player);
            if (instanceOpt.get().isRoundOver()) {
                if (world.players().isEmpty()) {
                    instanceOpt.get().advanceTo(InstanceImpl.State.FORCE_STOP);
                } else {
                    instanceOpt.get().advanceTo(InstanceImpl.State.PRE_END);
                }
            }
        }
    }

    @Listener(order = Order.LAST)
    public void onMoveEntity(final MoveEntityEvent event, @Getter("entity") final ServerPlayer player) {
        final ServerWorld world = player.world();
        final Optional<InstanceImpl> instance = Royale.getInstance().getInstanceManager().getInstance(world.key());

        // We only care about inner-instance movement
        if (!instance.isPresent()) {
            return;
        }

        // We only care about registered players
        if (!instance.get().isPlayerRegistered(player.uniqueId())) {
            return;
        }

        if (event instanceof ChangeEntityWorldEvent && !((ChangeEntityWorldEvent) event).originalWorld().equals(((ChangeEntityWorldEvent) event).destinationWorld())) {
            instance.get().disqualifyPlayer(player);
            return;
        }

        // If a Player has already spawned, this means they are playing. See if the instance allows movement
        if (!instance.get().getState().canAnyoneMove()) {
            event.setCancelled(true);
        }
    }

    @Listener(order = Order.LAST)
    public void onDestructEntity(final DestructEntityEvent.Death event, @Getter("entity") final ServerPlayer player) {
        final ServerWorld world = player.world();
        final Optional<InstanceImpl> instance = Royale.getInstance().getInstanceManager().getInstance(world.key());

        if (instance.isPresent()) {
            if (instance.get().isPlayerRegistered(player.uniqueId())) {
                instance.get().disqualifyPlayer(player);
                event.setCancelled(true);
                player.offer(Keys.HEALTH, player.maxHealth().get());
                player.offer(Keys.GAME_MODE, GameModes.SPECTATOR.get());
                player.transform(Keys.POTION_EFFECTS, list -> {
                    list.add(PotionEffect.of(PotionEffectTypes.NIGHT_VISION, 1, 1000000000));
                    return list;
                });
                if (instance.get().isRoundOver()) {
                    instance.get().advanceTo(InstanceImpl.State.PRE_END);
                }
            }
        }
    }

    @Listener
    public void onDamagePlayer(final DamageEntityEvent event, @First DamageSource source, @Getter("entity") final ServerPlayer player) {
        if (player.world().key().equals(Constants.Map.Lobby.LOBBY_WORLD_KEY) && source.type() == DamageTypes.VOID.get()) {
            event.setCancelled(true);
        }
    }

    @Listener
    public void onBreak(final ChangeBlockEvent.All event, @Root final ServerPlayer player) {
        final ServerWorld world = player.world();
        if (world.key().equals(Constants.Map.Lobby.LOBBY_WORLD_KEY) && !player.hasPermission(Constants.Permissions.ADMIN + ".lobby.edit")) {
            event.transactions(Operations.BREAK.get()).forEach(Transaction::invalidate);
            return;
        }

        final Optional<InstanceImpl> instance = Royale.getInstance().getInstanceManager().getInstance(world.key());
        if (instance.isPresent() && !instance.get().getState().canAnyoneInteract() && instance.get().isPlayerRegistered(player.uniqueId())) {
            event.transactions(Operations.BREAK.get()).forEach(Transaction::invalidate);
        }
    }

    @Listener
    public void onInteract(final InteractBlockEvent.Secondary event, @Root final ServerPlayer player) {
        final ServerWorld world = player.world();
        final Optional<InstanceImpl> instance = Royale.getInstance().getInstanceManager().getInstance(world.key());

        if (instance.isPresent() && !instance.get().getState().canAnyoneInteract() && instance.get().isPlayerRegistered(player.uniqueId())) {
            event.setCancelled(true);
        }
    }

    @Listener
    public void onInteractByPlayer(final InteractBlockEvent.Secondary event, @Root final ServerPlayer player) {
        event.block().location().flatMap(Location::blockEntity).filter(Sign.class::isInstance).ifPresent(sign -> {
            final Optional<ResourceKey> worldKey = sign.get(RoyaleKeys.WORLD);
            final Optional<ResourceKey> typeKey = sign.get(RoyaleKeys.TYPE);
            if (worldKey.isPresent() && typeKey.isPresent()) {
                final Optional<InstanceImpl> optInstance = Royale.getInstance().getInstanceManager().getInstance(worldKey.get());
                if (optInstance.isPresent()) {
                    final InstanceImpl instance = optInstance.get();
                    if (!instance.canRegisterMorePlayers()) {
                        player.sendActionBar(Component.text("World is full!", NamedTextColor.RED));
                    } else {
                        player.sendActionBar(Component.text(String.format("Joining world '%s'", worldKey.get()), NamedTextColor.GREEN));
                        if (instance.registerPlayer(player)) {
                            instance.spawnPlayer(player);
                        }
                    }
                    instance.updateSign();
                } else {
                    player.sendActionBar(Component.text(String.format("World '%s' isn't up yet! Creating instance...", worldKey.get()), NamedTextColor.RED));
                    sign.transform(Keys.SIGN_LINES, lines -> {
                        lines.set(2, Component.text("creating Instance", NamedTextColor.YELLOW));
                        return lines;
                    });

                    final InstanceType type = Constants.Plugin.INSTANCE_TYPE.get().value(typeKey.get());
                    Royale.getInstance().getInstanceManager().createInstance(worldKey.get(), type, false)
                            .thenAcceptAsync(InstanceImpl::updateSign, Royale.getInstance().getTaskExecutorService());
                }
            } else {
                final Optional<ResourceKey> wKey = player.get(RoyaleKeys.WORLD);
                final Optional<ResourceKey> tkey = player.get(RoyaleKeys.TYPE);
                if (wKey.isPresent() && tkey.isPresent() && player.hasPermission(Constants.Permissions.ADMIN + ".create.sign")) {
                    sign.offer(RoyaleKeys.WORLD, wKey.get());
                    sign.offer(RoyaleKeys.TYPE, tkey.get());
                    sign.transform(Keys.SIGN_LINES, lines -> {
                        lines.set(0, Component.text(wKey.get().namespace(), NamedTextColor.AQUA));
                        lines.set(1, Component.text(wKey.get().formatted()));
                        lines.set(2, Component.empty());
                        lines.set(3, Component.text(tkey.get().formatted()));
                        return lines;
                    });
                    final Optional<InstanceImpl> instOpt = Royale.getInstance().getInstanceManager().getInstance(wKey.get());
                    if (instOpt.isPresent()) {
                        instOpt.get().link((Sign) sign);
                    } else {
                        final InstanceType type = Constants.Plugin.INSTANCE_TYPE.get().findValue(tkey.get()).get();
                        Royale.getInstance().getInstanceManager().createInstance(wKey.get(), type, false)
                                .thenAcceptAsync(instance -> instance.link((Sign) sign), Royale.getInstance().getTaskExecutorService());
                    }
                }
            }
        });
    }

    private void convertToLobbyPlayer(final ServerPlayer player) {
        player.setScoreboard(Sponge.server().serverScoreboard().orElse(null));
        Utils.resetPlayer(player);
    }

    private boolean isTeleportSign(final List<Component> lines) {
        return lines.size() != 0 && SpongeComponents.plainSerializer().serialize(lines.get(0)).equalsIgnoreCase(Constants.Map.Lobby.JOIN_SIGN_HEADER);
    }
}
