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
package org.spongepowered.royale.configuration;

import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.serialize.SerializationException;

import java.nio.file.Path;

public final class MappedConfigurationAdapter<T extends AbstractConfiguration> {

    private final Class<T> configClass;
    private final Path configFile;
    private final HoconConfigurationLoader loader;
    private final ObjectMapper<T> mapper;

    private ConfigurationNode root;
    private T config;

    public MappedConfigurationAdapter(final Class<T> configClass, final ConfigurationOptions options, final Path configFile) {
        this.configClass = configClass;
        this.configFile = configFile;
        this.loader = HoconConfigurationLoader.builder()
                .defaultOptions(options)
                .path(configFile)
                .build();
        try {
            this.mapper = ObjectMapper.factory().get(configClass);
        } catch (final SerializationException e) {
            throw new RuntimeException(String.format("Failed to construct mapper for config class '%s'!", this.configClass));
        }
        this.root = this.loader.createNode(options);
        try {
            this.config = this.mapper.load(this.root);
        } catch (final SerializationException e) {
            throw new RuntimeException(e);
        }
    }

    public Class<T> getConfigClass() {
        return this.configClass;
    }

    public Path getConfigFile() {
        return this.configFile;
    }

    public T getConfig() {
        return this.config;
    }

    public void load() throws ConfigurateException {
        this.root = this.loader.load();
        this.config = this.mapper.load(this.root);
    }

    public void save() throws ConfigurateException {
        this.mapper.save(this.config, this.root);
        this.loader.save(this.root);
    }
}
