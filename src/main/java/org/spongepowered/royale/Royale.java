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

import com.google.inject.Inject;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.entity.Sign;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.persistence.DataStore;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterBuilderEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.RegisterDataEvent;
import org.spongepowered.api.event.lifecycle.RegisterRegistryEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.TaskExecutorService;
import org.spongepowered.api.world.server.WorldManager;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import org.spongepowered.royale.api.InstanceManager;
import org.spongepowered.royale.api.RoyaleKeys;
import org.spongepowered.royale.configuration.MappedConfigurationAdapter;
import org.spongepowered.royale.instance.EventHandler;
import org.spongepowered.royale.instance.InstanceManagerImpl;
import org.spongepowered.royale.instance.InstanceType;
import org.spongepowered.royale.instance.configuration.InstanceTypeConfiguration;
import org.spongepowered.royale.instance.gen.InstanceMutator;
import org.spongepowered.royale.template.ComponentTemplate;
import org.spongepowered.royale.template.ComponentTemplateTypeSerializer;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Plugin(Constants.Plugin.ID)
public final class Royale {

    private static Royale INSTANCE;

    private final PluginContainer plugin;
    private final Path configFile;
    private final ConfigurationOptions options;
    private final InstanceManager instanceManager;
    private TaskExecutorService taskExecutorService;

    @Inject
    public Royale(final PluginContainer plugin, @ConfigDir(sharedRoot = false) final Path configFile) {
        Royale.INSTANCE = this;

        this.instanceManager = new InstanceManagerImpl();
        this.plugin = plugin;
        this.configFile = configFile;
        this.options = ConfigurationOptions.defaults()
                .serializers(Sponge.configManager().serializers().childBuilder()
                            .register(ComponentTemplate.class, new ComponentTemplateTypeSerializer())
                                     .build());
    }

    public static Royale getInstance() {
        return Royale.INSTANCE;
    }

    public PluginContainer getPlugin() {
        return this.plugin;
    }

    public Path getConfigFile() {
        return this.configFile;
    }

    public ConfigurationOptions getConfigurationOptions() {
        return this.options;
    }

    public InstanceManager getInstanceManager() {
        return this.instanceManager;
    }

    public TaskExecutorService getTaskExecutorService() {
        Objects.requireNonNull(this.taskExecutorService);
        return this.taskExecutorService;
    }

    @Listener
    public void onRegisterData(RegisterDataEvent event) {
        final ResourceKey datastoreKey = ResourceKey.of(this.plugin, "datastore");

        final DataStore dataStore = DataStore.builder().pluginData(datastoreKey)
                .holder(Sign.class, ItemStack.class)
                .keys(RoyaleKeys.WORLD, RoyaleKeys.TYPE)
                .build();

        final DataRegistration registration = DataRegistration.builder()
                .dataKey(RoyaleKeys.WORLD, RoyaleKeys.TYPE)
                .store(dataStore)
                .build();
        event.register(registration);
    }

    @Listener
    public void onRegisterCatalogs(final RegisterRegistryEvent.EngineScoped<Server> event) {
        event.register(ResourceKey.of(Constants.Plugin.ID, "instance_mutator"), true, this::defaultMutators);
        event.register(ResourceKey.of(Constants.Plugin.ID, "instance_type"), true, this::defaultInstanceTypes);
    }

    @Listener
    public void onRegisterInstanceTypeBuilders(final RegisterBuilderEvent event) {
        event.register(InstanceType.Builder.class, InstanceType.Builder::new);
    }

    @Listener
    public void onRegisterCommands(final RegisterCommandEvent<Command.Parameterized> event) {
        event.register(this.plugin, Commands.rootCommand(), Constants.Plugin.ID);
    }

    @Listener
    public void onStartingServer(final StartingEngineEvent<Server> event) {
        Sponge.eventManager().registerListeners(this.plugin, new EventHandler());
        this.taskExecutorService = event.engine().scheduler().executor(this.plugin);
    }

    @Listener
    public void onServerStarted(final StartedEngineEvent<Server> event) {
        final WorldManager worldManager = Sponge.server().worldManager();
        worldManager.loadWorld(Constants.Map.Lobby.LOBBY_TEMPLATE)
                .thenAccept(world -> {
                    if (world == null) {
                        throw new RuntimeException("UNABLE TO LOAD LOBBY WORLD. Shutting down the server.");
                    }
                })
                .whenCompleteAsync((result, exception) -> {
                    if (exception != null) {
                        this.plugin.logger().fatal(exception);
                        Sponge.server().shutdown();
                    }
                }, Royale.getInstance().getTaskExecutorService());
    }

    private Map<ResourceKey, InstanceMutator> defaultMutators() {
        return Constants.Map.DEFAULT_MAP_MUTATORS;
    }

    private Map<ResourceKey, InstanceType> defaultInstanceTypes() {
        final Map<ResourceKey, InstanceType> defaultTypes = new HashMap<>();
        boolean createDefaults = true;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Constants.Map.INSTANCE_TYPES_FOLDER, entry -> entry.getFileName().toString()
                .endsWith(".conf"))) {
            for (final Path path : stream) {
                final MappedConfigurationAdapter<InstanceTypeConfiguration> adapter = new MappedConfigurationAdapter<>(
                        InstanceTypeConfiguration.class, this.options, path);

                try {
                    adapter.load();
                } catch (final IOException e) {
                    this.plugin.logger().error("Failed to load configuration for path [{}]!", path, e);
                    continue;
                }

                final String instanceId = path.getFileName().toString().split("\\.")[0].toLowerCase();
                final InstanceType newType = InstanceType.builder()
                        .key(ResourceKey.of(this.plugin, instanceId))
                        .from(adapter.getConfig())
                        .build();

                this.plugin.logger().info("Registered instance type '{}'", newType.key());
                defaultTypes.put(ResourceKey.of(this.plugin, instanceId), newType);
                createDefaults = false;
            }

        } catch (final IOException e) {
            throw new RuntimeException("Failed to iterate over the instance type configuration files!");
        }

        if (createDefaults) {
            final InstanceType defaultType = InstanceType.builder()
                    .key(ResourceKey.of(this.plugin, "last_man_standin"))
                    .name("Last Man Standing")
                    .build();
            defaultTypes.put(ResourceKey.of(this.plugin, "last_man_standin"), defaultType);
            this.plugin.logger().info("Registered instance type '{}'", defaultType.key());

            final MappedConfigurationAdapter<InstanceTypeConfiguration> adapter = new MappedConfigurationAdapter<>(
                    InstanceTypeConfiguration.class, this.options, Constants.Map.INSTANCE_TYPES_FOLDER.resolve(defaultType.key().value() +
                ".conf"));
            defaultType.injectIntoConfig(adapter.getConfig());
            try {
                adapter.save();
            } catch (final ConfigurateException e) {
                throw new RuntimeException(e);
            }
        }
        if (defaultTypes.isEmpty()) {
            throw new RuntimeException("Default InstanceTypes map is empty. Create defaults: " + createDefaults);
        }
        return defaultTypes;
    }


}
