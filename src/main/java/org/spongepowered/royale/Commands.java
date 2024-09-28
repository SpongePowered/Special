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
package org.spongepowered.royale;

import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.adventure.SpongeComponents;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueParameter;
import org.spongepowered.api.command.parameter.managed.clientcompletion.ClientCompletionType;
import org.spongepowered.api.command.parameter.managed.clientcompletion.ClientCompletionTypes;
import org.spongepowered.api.command.parameter.managed.standard.ResourceKeyedValueParameters;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.DefaultWorldKeys;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.WorldManager;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;
import org.spongepowered.royale.api.Instance;
import org.spongepowered.royale.instance.InstanceType;
import org.spongepowered.royale.api.RoyaleKeys;
import org.spongepowered.royale.instance.exception.UnknownInstanceException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

final class Commands {

    private static final Parameter.Value<InstanceType> INSTANCE_TYPE_PARAMETER =
            Parameter.registryElement(TypeToken.get(InstanceType.class),
                    Constants.Plugin.INSTANCE_TYPE.asDefaultedType(Sponge::server)).key("instanceType").build();
    private static final Parameter.Value<Boolean> FORCE_PARAMETER = Parameter.bool().key("force").optional().build();
    private static final Parameter.Value<ResourceKey> WORLD_KEY_PARAMETER = Parameter.builder(ResourceKey.class)
            .key("world key")
            .addParser(new WorldKeyParameter())
            .build();
    private static final Parameter.Value<ResourceKey> INSTANCE_KEY_PARAMETER = Parameter.builder(ResourceKey.class)
            .key("instance key")
            .addParser(new InstanceKeyParameter())
            .build();
    private static final Parameter.Value<ResourceKey> NEW_WORLD_KEY_PARAMETER = Parameter.builder(ResourceKey.class)
            .key("new world")
            .addParser(ResourceKeyedValueParameters.RESOURCE_KEY)
            .build();

    private static Command.Parameterized createCommand() {
        return Command.builder()
                .permission(Constants.Plugin.ID + ".command.create")
                .shortDescription(Component.text("Creates an instance."))
                .extendedDescription(Component.text("Creates an instance from a ")
                        .append(Component.text("world", NamedTextColor.GREEN))
                        .append(Component.text(" with the specified instance "))
                        .append(Component.text("type", NamedTextColor.LIGHT_PURPLE))
                        .append(Component.text(".")))
                .addParameter(Commands.WORLD_KEY_PARAMETER)
                .addParameter(Commands.INSTANCE_TYPE_PARAMETER)
                .addParameter(Commands.FORCE_PARAMETER)
                .executor(context -> {
                    final InstanceType instanceType = context.requireOne(Commands.INSTANCE_TYPE_PARAMETER);
                    final ResourceKey targetWorldKey = context.requireOne(Commands.WORLD_KEY_PARAMETER);
                    final boolean force = context.one(Commands.FORCE_PARAMETER).orElse(false);

                    context.sendMessage(Identity.nil(), Component.text().content("Creating an instance from [")
                            .append(Component.text(targetWorldKey.formatted(), NamedTextColor.GREEN))
                            .append(Component.text("] using instance type "))
                            .append(Component.text(instanceType.name(), NamedTextColor.LIGHT_PURPLE))
                            .append(Component.text("."))
                            .build());

                    Royale.getInstance().getInstanceManager().createInstance(targetWorldKey, instanceType, force)
                            .whenCompleteAsync((ignored, throwable) -> {
                                if (throwable != null) {
                                    context.sendMessage(Identity.nil(), Component.text(throwable.getMessage(), NamedTextColor.RED));
                                    Royale.getInstance().getPlugin().logger().error(throwable);
                                    return;
                                }
                                context.sendMessage(Identity.nil(),
                                        Component.text().content("Created instance for [")
                                                .append(Component.text(targetWorldKey.formatted(), NamedTextColor.GREEN))
                                                .append(Component.text("]"))
                                                .build()
                                );
                                Commands.broadcastJoinMessage(targetWorldKey);
                            }, Royale.getInstance().getTaskExecutorService());
                    return CommandResult.success();
                })
                .build();
    }

    private static Command.Parameterized broadcastCommand() {
        return Command.builder()
                .permission(Constants.Plugin.ID + ".command.broadcast")
                .shortDescription(Component.text("Broadcasts message to join an instance."))
                .extendedDescription(Component.text("Broadcasts message to join an instance in a ")
                        .append(Component.text("world", NamedTextColor.GREEN))
                        .append(Component.text(".")))
                .addParameter(Commands.INSTANCE_KEY_PARAMETER)
                .executor(context -> {
                    final ResourceKey worldKey = context.requireOne(Commands.INSTANCE_KEY_PARAMETER);
                    Commands.broadcastJoinMessage(worldKey);
                    return CommandResult.success();
                })
                .build();
    }

    private static void broadcastJoinMessage(final ResourceKey worldKey) {
        for (final ServerPlayer player : Sponge.server().onlinePlayers()) {
            if (player.world().key().equals(Constants.Map.Lobby.LOBBY_WORLD_KEY)) {
                player.sendMessage(Component.text().clickEvent(SpongeComponents.executeCallback(commandCause -> {
                    final Optional<Instance> instance = Royale.getInstance().getInstanceManager().getInstance(worldKey);
                    if (instance.isPresent()) {
                        final ServerPlayer serverPlayer = (ServerPlayer) commandCause.root();
                        if (instance.get().isFull()) {
                            serverPlayer.sendMessage(Component.text("Instance is full!"));
                        } else {
                            instance.get().addPlayer(serverPlayer);
                        }
                    }
                })).append(LinearComponents
                        .linear(Component.text("["), Component.text(worldKey.formatted(), NamedTextColor.RED),
                                Component.text("] is ready! Click this message or right-click a teleportation sign to join!"))).build());
            }
        }
    }

    private static Command.Parameterized statusCommand() {
        return Command.builder()
                .permission(Constants.Plugin.ID + ".command.status")
                .executor(context -> {
                    Component msg = Component.text("Lobby: ").append(worldStatus(Constants.Map.Lobby.LOBBY_WORLD_KEY))
                            .append(Component.text(" [ TP ] ").clickEvent(SpongeComponents.executeCallback(commandCause -> {
                                final ServerPlayer serverPlayer = (ServerPlayer) commandCause.root();
                                final Optional<ServerWorld> lobby = Sponge.server().worldManager().world(Constants.Map.Lobby.LOBBY_WORLD_KEY);
                                if (lobby.isPresent()) {
                                    serverPlayer.setLocation(ServerLocation.of(Constants.Map.Lobby.LOBBY_WORLD_KEY, lobby.get().properties().spawnPosition()));
                                    serverPlayer.offer(Keys.GAME_MODE, GameModes.SURVIVAL.get());
                                }
                            })));
                    for (Instance instance : Royale.getInstance().getInstanceManager().getAll()) {
                        final ResourceKey key = instance.getWorldKey();
                        Component component = Component.newline()
                                .append(Component.text("Instance " + key + ": "))
                                .append(worldStatus(key))
                                .append(Component.text(" (" + instance.getState() + ") "));

                        if (context.cause().root() instanceof ServerPlayer) {
                            component = component.append(Component.text(" [ TP ] ").clickEvent(SpongeComponents.executeCallback(commandCause -> {
                                final Optional<Instance> inst = Royale.getInstance().getInstanceManager().getInstance(key);
                                if (inst.isPresent()) {
                                    final ServerPlayer serverPlayer = (ServerPlayer) commandCause.root();
                                    inst.get().addSpectator(serverPlayer);
                                }
                            })));
                        }
                        msg = msg.append(component);
                    }
                    context.sendMessage(Identity.nil(), msg);
                    return CommandResult.success();
                })
                .build();
    }

    private static Command.Parameterized copyCommand() {
        return Command.builder()
                .addParameter(Commands.WORLD_KEY_PARAMETER)
                .addParameter(Commands.NEW_WORLD_KEY_PARAMETER)
                .permission(Constants.Plugin.ID + ".command.copy")
                .executor(context -> {
                    final ResourceKey sourceWorldKey = context.requireOne(Commands.WORLD_KEY_PARAMETER);
                    final ResourceKey targetWorldKey = context.requireOne(Commands.NEW_WORLD_KEY_PARAMETER);
                    final WorldManager wm = Sponge.server().worldManager();
                    if (wm.worldExists(targetWorldKey)) {
                        return CommandResult.error(Component.text("World already exists: " + targetWorldKey));
                    }
                    if (wm.worldExists(sourceWorldKey)) {
                        wm.copyWorld(sourceWorldKey, targetWorldKey)
                                .thenComposeAsync(b -> wm.loadProperties(targetWorldKey))
                                .thenAcceptAsync(opt -> opt.ifPresent(prop -> {
                            prop.setSerializationBehavior(SerializationBehavior.NONE);
                            wm.saveProperties(prop);
                            context.sendMessage(Identity.nil(), Component.text("World copied and set to readonly!"));
                        }));
                    } else {
                        return CommandResult.error(Component.text("World does not exists: " + sourceWorldKey));
                    }
                    return CommandResult.success();
                })
                .build();
    }


    private static Command.Parameterized linkCommand() {
        return Command.builder()
                .addParameter(Commands.WORLD_KEY_PARAMETER)
                .addParameter(Commands.INSTANCE_TYPE_PARAMETER)
                .permission(Constants.Plugin.ID + ".command.link")
                .executor(context -> {
                    if (!(context.cause().root() instanceof ServerPlayer)) {
                        throw new CommandException(Component.text("A player needs to be provided if you are not a player!", NamedTextColor.RED));
                    }

                    final ServerPlayer player = (ServerPlayer) context.cause().root();
                    final ResourceKey targetWorldKey = context.requireOne(Commands.WORLD_KEY_PARAMETER);
                    final InstanceType type = context.requireOne(Commands.INSTANCE_TYPE_PARAMETER);
                    final ItemStack stack = player.itemInHand(HandTypes.MAIN_HAND);
                    if (stack.type().isAnyOf(ItemTypes.ACACIA_SIGN, ItemTypes.BIRCH_SIGN, ItemTypes.DARK_OAK_SIGN, ItemTypes.JUNGLE_SIGN, ItemTypes.OAK_SIGN, ItemTypes.SPRUCE_SIGN, ItemTypes.CRIMSON_SIGN, ItemTypes.WARPED_SIGN)) {
                        stack.offer(RoyaleKeys.WORLD, targetWorldKey);
                        stack.offer(RoyaleKeys.TYPE, type.key());
                        player.setItemInHand(HandTypes.MAIN_HAND, stack);
                        context.sendMessage(Identity.nil(), Component.text("Place down your sign to link it to the instance", NamedTextColor.YELLOW));
                    } else {
                        context.sendMessage(Identity.nil(), Component.text("You need to hold a sign in your hand", NamedTextColor.RED));
                    }
                    return CommandResult.success();
                })
                .build();
    }

    private static Component worldStatus(ResourceKey key) {
        boolean exists = Sponge.server().worldManager().worldExists(key);
        boolean loaded = Sponge.server().worldManager().world(key).isPresent();
        return exists ? (loaded ? Component.text("Loaded") : Component.text("Unloaded")) : Component.text("Missing");
    }

    private static Command.Parameterized startCommand() {
        return Command.builder()
                .permission(Constants.Plugin.ID + ".command.start")
                .shortDescription(Component.text("Starts an ")
                        .append(Component.text("instance", NamedTextColor.LIGHT_PURPLE))
                        .append(Component.text(".")))
                .extendedDescription(Component.text("Starts an ")
                        .append(Component.text("instance", NamedTextColor.LIGHT_PURPLE))
                        .append(Component.text(".")))
                .addParameter(Commands.INSTANCE_KEY_PARAMETER)
                .executor(context -> {
                    final ResourceKey worldKey =  context.requireOne(Commands.INSTANCE_KEY_PARAMETER);
                    try {
                        context.sendMessage(Identity.nil(),
                                Component.text().content("Starting round countdown in [")
                                        .append(Component.text(worldKey.formatted(), NamedTextColor.GREEN))
                                        .append(Component.text("]."))
                                        .build());
                        Royale.getInstance().getInstanceManager().startInstance(worldKey);
                        return CommandResult.success();
                    } catch (final UnknownInstanceException e) {
                        throw new CommandException(Component.text().content("Unable to start round in [")
                                .append(Component.text(worldKey.formatted(), NamedTextColor.GREEN))
                                .append(Component.text("], was it created?"))
                                .build());
                    }
                })
                .build();
    }

    private static Command.Parameterized endCommand() {
        return Command.builder()
                .permission(Constants.Plugin.ID + ".command.end")
                .shortDescription(Component.text().content("Ends an ")
                        .append(Component.text("instance", NamedTextColor.LIGHT_PURPLE))
                        .append(Component.text("."))
                        .build())
                .addParameter(Commands.INSTANCE_KEY_PARAMETER)
                .executor(context -> {
                    final ResourceKey worldKey = context.requireOne(Commands.INSTANCE_KEY_PARAMETER);

                    context.sendMessage(Identity.nil(), Component.text()
                            .content("Ending round in [")
                            .append(Component.text(worldKey.formatted(), NamedTextColor.GREEN))
                            .append(Component.text("]."))
                            .build());
                    try {
                        Royale.getInstance().getInstanceManager().endInstance(worldKey);
                        return CommandResult.success();
                    } catch (final UnknownInstanceException e) {
                        throw new CommandException(Component.text().content("Unable to end round in [")
                                .append(Component.text(worldKey.formatted(), NamedTextColor.GREEN))
                                .append(Component.text("], was it created?"))
                                .build());
                    }
                })
                .build();
    }

    private static Command.Parameterized unloadCommand() {
        return Command.builder()
                .permission(Constants.Plugin.ID + ".command.unload")
                .shortDescription(Component.text().content("Unloading an ")
                        .append(Component.text("instance", NamedTextColor.LIGHT_PURPLE))
                        .append(Component.text("."))
                        .build())
                .addParameter(Commands.INSTANCE_KEY_PARAMETER)
                .executor(context -> {
                    final ResourceKey worldKey = context.requireOne(Commands.INSTANCE_KEY_PARAMETER);

                    context.sendMessage(Identity.nil(), Component.text()
                            .content("Unloading instance in [")
                            .append(Component.text(worldKey.formatted(), NamedTextColor.GREEN))
                            .append(Component.text("]."))
                            .build());

                    Royale.getInstance().getInstanceManager().unloadInstance(worldKey)
                            .thenAcceptAsync(success -> {
                                if (success) {
                                    Royale.getInstance().getPlugin().logger().info("Instance {} has been unloaded!", worldKey);
                                } else {

                                    Royale.getInstance().getPlugin().logger().info("Unable to unload instance in {}!", worldKey);
                                }
                            }, Royale.getInstance().getTaskExecutorService());
                    return CommandResult.success();
                })
                .build();
    }

    private static Command.Parameterized joinCommand() {
        return Command.builder()
                .permission(Constants.Plugin.ID + ".command.join")
                .shortDescription(Component.text()
                        .content("Joins an ")
                        .append(Component.text("instance", NamedTextColor.LIGHT_PURPLE))
                        .append(Component.text("."))
                        .build())
                .addParameter(Commands.INSTANCE_KEY_PARAMETER)
                .executor(context -> {
                    if (!(context.cause().root() instanceof ServerPlayer)) {
                        throw new CommandException(Component.text("A player needs to be provided if you are not a player!", NamedTextColor.RED));
                    }

                    final ServerPlayer player = (ServerPlayer) context.cause().root();
                    final ResourceKey worldKey = context.requireOne(Commands.INSTANCE_KEY_PARAMETER);

                    final Optional<Instance> instance = Royale.getInstance().getInstanceManager().getInstance(worldKey);
                    if (instance.isEmpty()) {
                        throw new CommandException(
                                Component.text().content("Instance [")
                                    .append(Component.text(worldKey.formatted(), NamedTextColor.GREEN))
                                    .append(Component.text("] is not a valid instance, is it running?"))
                                    .build());
                    }
                    if (instance.get().isFull()) {
                        player.sendMessage(Component.text("Instance is full!"));
                        return CommandResult.success();
                    }

                    player.sendMessage(Component.text().content("Joining [")
                            .append(Component.text(worldKey.formatted(), NamedTextColor.GREEN))
                            .append(Component.text("]."))
                            .build());
                    instance.get().addPlayer(player);

                    return CommandResult.success();
                })
                .build();
    }

    private static Command.Parameterized spectateCommand() {
        return Command.builder()
                .permission(Constants.Plugin.ID + ".command.spectate")
                .shortDescription(Component.text()
                        .content("Spectate an ")
                        .append(Component.text("instance", NamedTextColor.LIGHT_PURPLE))
                        .append(Component.text("."))
                        .build())
                .addParameter(Commands.INSTANCE_KEY_PARAMETER)
                .executor(context -> {
                    if (!(context.cause().root() instanceof ServerPlayer)) {
                        throw new CommandException(Component.text("A player needs to be provided if you are not a player!", NamedTextColor.RED));
                    }

                    final ServerPlayer player = (ServerPlayer) context.cause().root();
                    final ResourceKey worldKey = context.requireOne(Commands.INSTANCE_KEY_PARAMETER);

                    final Optional<Instance> instance = Royale.getInstance().getInstanceManager().getInstance(worldKey);
                    if (instance.isEmpty()) {
                        throw new CommandException(
                                Component.text().content("Instance [")
                                        .append(Component.text(worldKey.formatted(), NamedTextColor.GREEN))
                                        .append(Component.text("] is not a valid instance, is it running?"))
                                        .build());
                    }

                    instance.get().addSpectator(player);

                    return CommandResult.success();
                })
                .build();
    }

    private static Command.Parameterized leaveCommand() {
        return Command.builder()
                .permission(Constants.Plugin.ID + ".command.leave")
                .shortDescription(Component.text()
                        .content("Leave an ")
                        .append(Component.text("instance", NamedTextColor.LIGHT_PURPLE))
                        .append(Component.text("."))
                        .build())
                .executor(context -> {
                    if (!(context.cause().root() instanceof ServerPlayer)) {
                        throw new CommandException(Component.text("A player needs to be provided if you are not a player!", NamedTextColor.RED));
                    }

                    final ServerPlayer player = (ServerPlayer) context.cause().root();
                    final ResourceKey worldKey = player.world().key();

                    final Optional<Instance> instance = Royale.getInstance().getInstanceManager().getInstance(worldKey);
                    if (instance.isEmpty()) {
                        throw new CommandException(
                                Component.text().content("Instance [")
                                        .append(Component.text(worldKey.formatted(), NamedTextColor.GREEN))
                                        .append(Component.text("] is not a valid instance, is it running?"))
                                        .build());
                    }

                    if (instance.get().isPlayerAlive(player)) {
                        instance.get().removePlayer(player);
                    } else {
                        instance.get().removeSpectator(player);
                    }

                    final ServerWorld lobby = Sponge.server().worldManager().world(Constants.Map.Lobby.LOBBY_WORLD_KEY)
                            .or(() -> Sponge.server().worldManager().world(DefaultWorldKeys.DEFAULT)).get();
                    Sponge.server().serverScoreboard().ifPresent(player::setScoreboard);
                    player.setLocation(ServerLocation.of(lobby, lobby.properties().spawnPosition()));
                    player.offer(Keys.GAME_MODE, GameModes.SURVIVAL.get());

                    return CommandResult.success();
                })
                .build();
    }

    private static Command.Parameterized editCommand() {
        return Command.builder()
                .permission(Constants.Plugin.ID + ".command.edit")
                .addParameter(Commands.WORLD_KEY_PARAMETER)
                .shortDescription(Component.text()
                        .content("Allows editing a royale world")
                        .build())
                .executor(context -> {
                    final ResourceKey targetWorldKey = context.requireOne(Commands.WORLD_KEY_PARAMETER);
                    final Optional<Instance> instance = Royale.getInstance().getInstanceManager().getInstance(targetWorldKey);
                    if (instance.isPresent()) {
                        throw new CommandException(
                                Component.text().content("Instance [")
                                        .append(Component.text(targetWorldKey.formatted(), NamedTextColor.GREEN))
                                        .append(Component.text("] is currently active instance."))
                                        .build());
                    }

                    final WorldManager wm = Sponge.server().worldManager();
                    final Optional<ServerWorldProperties> wp = wm.loadProperties(targetWorldKey).join();
                    final Optional<ServerWorld> worldToEdit = wm.world(targetWorldKey);
                    if (wp.isPresent() || worldToEdit.isPresent()) {
                        final ServerWorldProperties properties = wp.orElseGet(() -> worldToEdit.get().properties());
                        final SerializationBehavior serializationBehavior = properties.serializationBehavior();
                        switch (serializationBehavior) {
                            case AUTOMATIC:
                            case AUTOMATIC_METADATA_ONLY:
                            case MANUAL:
                            case MANUAL_METADATA_ONLY:
                                try {
                                    if (worldToEdit.isPresent()) {
                                        worldToEdit.get().save();
                                        context.sendMessage(Identity.nil(), Component.text("Saved world"));
                                    } else {
                                        wm.loadWorld(targetWorldKey);
                                        context.sendMessage(Identity.nil(), Component.text("Loading world"));
                                    }
                                } catch (IOException e) {
                                    context.sendMessage(Identity.nil(), Component.text("Exception while saving!"));
                                }
                                properties.setSerializationBehavior(SerializationBehavior.NONE);
                                wm.saveProperties(properties);
                                context.sendMessage(Identity.nil(), Component.text("World is now readonly!"));
                                break;
                            case NONE:
                                properties.setSerializationBehavior(SerializationBehavior.MANUAL);
                                wm.saveProperties(properties);
                                if (worldToEdit.isEmpty()) {
                                    wm.loadWorld(targetWorldKey);
                                }
                                context.sendMessage(Identity.nil(), Component.text("World can now be modified!"));
                                break;
                        }
                    } else {
                        context.sendMessage(Identity.nil(), Component.text("World properties not found!"));
                    }

                    return CommandResult.success();
                })
                .build();
    }

    static Command.Parameterized rootCommand() {
        return Command.builder()
                .permission(Constants.Plugin.ID + ".command.root")
                .shortDescription(Component.text("Displays available commands"))
                .extendedDescription(Component.text("Displays available commands"))
                .executor(context -> {
                    context.sendMessage(Identity.nil(), Component.text("Some help should go here..."));
                    return CommandResult.success();
                })
                .addChild(Commands.createCommand(), "create")
                .addChild(Commands.broadcastCommand(), "broadcast")
                .addChild(Commands.statusCommand(), "status")
                .addChild(Commands.linkCommand(), "link")
                .addChild(Commands.copyCommand(), "copy")
                .addChild(Commands.startCommand(), "start")
                .addChild(Commands.endCommand(), "end")
                .addChild(Commands.unloadCommand(), "unload")
                .addChild(Commands.joinCommand(), "join")
                .addChild(Commands.spectateCommand(), "spectate")
                .addChild(Commands.leaveCommand(), "leave")
                .addChild(Commands.editCommand(), "edit")
                .build();
    }

    private static class WorldKeyParameter implements ValueParameter<ResourceKey> {

        @Override
        public List<CommandCompletion> complete(final CommandContext context, final String currentInput) {
            return Sponge.server().worldManager().worldKeys().stream()
                    .map(ResourceKey::formatted)
                    .filter(x -> x.startsWith(currentInput.toLowerCase(Locale.ROOT)))
                    .map(CommandCompletion::of)
                    .collect(Collectors.toList());
        }

        @Override
        public Optional<? extends ResourceKey> parseValue(final Parameter.Key<? super ResourceKey> parameterKey,
                final ArgumentReader.Mutable reader,
                final CommandContext.Builder context) throws ArgumentParseException {
            final ResourceKey key = reader.parseResourceKey();
            if (Sponge.server().worldManager().worldExists(key)) {
                return Optional.of(key);
            }
            throw reader.createException(Component.text(String.format("The world %s does not exist.", key.formatted()), NamedTextColor.RED));
        }

        @Override
        public List<ClientCompletionType> clientCompletionType() {
            return Collections.singletonList(ClientCompletionTypes.RESOURCE_KEY.get());
        }
    }

    private static class InstanceKeyParameter implements ValueParameter<ResourceKey> {

        @Override
        public List<CommandCompletion> complete(final CommandContext context, final String currentInput) {
            return Royale.getInstance().getInstanceManager().getAll().stream().map(Instance::getWorldKey)
                    .map(ResourceKey::formatted)
                    .filter(x -> x.startsWith(currentInput.toLowerCase(Locale.ROOT)))
                    .map(CommandCompletion::of)
                    .collect(Collectors.toList());
        }

        @Override
        public Optional<? extends ResourceKey> parseValue(final Parameter.Key<? super ResourceKey> parameterKey,
                final ArgumentReader.Mutable reader,
                final CommandContext.Builder context) throws ArgumentParseException {
            final ResourceKey key = reader.parseResourceKey();
            if (Sponge.server().worldManager().worldExists(key)) {
                final Optional<Instance> instance = Royale.getInstance().getInstanceManager().getInstance(key);
                if (instance.isPresent()) {
                    return Optional.of(key);
                }
                throw reader.createException(Component.text(String.format("The world %s is not an instance.", key.formatted()), NamedTextColor.RED));
            }
            throw reader.createException(Component.text(String.format("The world %s does not exist.", key.formatted()), NamedTextColor.RED));
        }

        @Override
        public List<ClientCompletionType> clientCompletionType() {
            return Collections.singletonList(ClientCompletionTypes.RESOURCE_KEY.get());
        }
    }
}
