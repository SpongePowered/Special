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

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.ResourceKeyed;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.Nameable;
import org.spongepowered.api.util.ResourceKeyedBuilder;
import org.spongepowered.royale.Constants;
import org.spongepowered.royale.instance.configuration.InstanceTypeConfiguration;
import org.spongepowered.royale.instance.gen.InstanceMutator;
import org.spongepowered.royale.instance.gen.InstanceMutatorPipeline;
import org.spongepowered.royale.template.ComponentTemplate;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public final class InstanceType implements ResourceKeyed, Nameable {

    private final ResourceKey key;
    private final InstanceMutatorPipeline mutatorPipeline;
    private String name;
    private ComponentTemplate nameTemplate, roundStartTemplate, roundEndTemplate;
    private final List<ItemStackSnapshot> defaultItems;
    private long roundStartLength, roundLength, roundEndLength;
    private int automaticStartPlayerCount;

    private InstanceType(final Builder builder) {
        this.key = builder.key;
        this.name = builder.name;
        this.nameTemplate = builder.nameTemplate;
        this.mutatorPipeline = new InstanceMutatorPipeline();
        this.mutatorPipeline.getMutators().addAll(builder.mutators);
        this.defaultItems = builder.defaultItems;
        this.roundStartTemplate = builder.roundStartTemplate;
        this.roundStartLength = builder.roundStartLength;
        this.roundLength = builder.roundLength;
        this.roundEndTemplate = builder.roundEndTemplate;
        this.roundEndLength = builder.roundEndLength;
        this.automaticStartPlayerCount = builder.automaticStartPlayerCount;
    }

    public static Builder builder() {
        return Sponge.game().builderProvider().provide(Builder.class);
    }

    private static Optional<InstanceMutator> findInstanceMutator(ResourceKey key) {
        return Optional.ofNullable(Constants.Map.DEFAULT_MAP_MUTATORS.get(key));
        // TODO fix registry
        // return Sponge.server().registry(Constants.Plugin.INSTANCE_MUTATOR).findValue(key);
    }

    @Override
    public ResourceKey key() {
        return this.key;
    }

    @Override
    public String name() {
        return this.name;
    }

    public long getRoundStartLength() {
        return this.roundStartLength;
    }

    public long getRoundLength() {
        return this.roundLength;
    }

    public long getRoundEndLength() {
        return this.roundEndLength;
    }

    public ComponentTemplate getNameTemplate() {
        return this.nameTemplate;
    }

    public ComponentTemplate getRoundStartTemplate() {
        return this.roundStartTemplate;
    }

    public ComponentTemplate getRoundEndTemplate() {
        return this.roundEndTemplate;
    }

    public int getAutomaticStartPlayerCount() {
        return this.automaticStartPlayerCount;
    }

    public List<ItemStackSnapshot> getDefaultItems() {
        return this.defaultItems;
    }

    public InstanceMutatorPipeline getMutatorPipeline() {
        return this.mutatorPipeline;
    }

    public void injectFromConfig(final InstanceTypeConfiguration value) {
        this.name = value.general.name;
        this.nameTemplate = value.general.nameTemplate;
        this.mutatorPipeline.getMutators().clear();
        value.general.mapMutators.stream().map(k -> findInstanceMutator(k).get()).forEach(this.mutatorPipeline.getMutators()::add);
        this.defaultItems.clear();
        this.defaultItems.addAll(value.round.defaultItems);
        this.roundStartTemplate = value.round.startTemplate;
        this.roundEndTemplate = value.round.endTemplate;
        this.roundStartLength = value.round.start;
        this.roundLength = value.round.length;
        this.roundEndLength = value.round.end;
        this.automaticStartPlayerCount = value.round.automaticStartPlayerCount;
    }

    public void injectIntoConfig(final InstanceTypeConfiguration config) {

        config.general.name = this.name;
        config.general.nameTemplate = this.nameTemplate;
        config.general.mapMutators.clear();
        config.general.mapMutators.addAll(this.mutatorPipeline.getMutators().stream().map(InstanceMutator::key).toList());

        config.round.defaultItems.clear();
        config.round.defaultItems.addAll(this.defaultItems);
        config.round.start = this.roundStartLength;
        config.round.startTemplate = this.roundStartTemplate;
        config.round.length = this.roundLength;
        config.round.end = this.roundEndLength;
        config.round.endTemplate = this.roundEndTemplate;
        config.round.automaticStartPlayerCount = this.automaticStartPlayerCount;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final InstanceType instanceType = (InstanceType) o;
        return Objects.equals(this.key, instanceType.key);
    }

    @Override
    public int hashCode() {
        return this.key.hashCode();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", InstanceType.class.getSimpleName() + "[", "]")
                .add("key=" + this.key)
                .add("name=" + this.name)
                .add("nameTemplate=" + this.nameTemplate)
                .add("roundStartTemplate=" + this.roundStartTemplate)
                .add("roundEndTemplate=" + this.roundEndTemplate)
                .add("defaultItems=" + this.defaultItems)
                .add("roundStartLength=" + this.roundStartLength)
                .add("roundLength=" + this.roundLength)
                .add("roundEndLength=" + this.roundEndLength)
                .add("automaticPlayerStartCount=" + this.automaticStartPlayerCount)
                .add("mutatorPipeline=" + this.mutatorPipeline)
                .toString();
    }

    public static final class Builder implements ResourceKeyedBuilder<InstanceType, Builder> {

        ResourceKey key;
        String name;
        ComponentTemplate nameTemplate, roundStartTemplate, roundEndTemplate;
        List<ItemStackSnapshot> defaultItems;
        long roundStartLength, roundLength, roundEndLength;
        int automaticStartPlayerCount;
        Set<InstanceMutator> mutators;

        public Builder() {
            this.reset();
        }

        public Builder from(final InstanceType value) {
            this.key = value.key;
            this.name = value.name;
            this.nameTemplate = value.nameTemplate;

            this.mutators = new HashSet<>(value.mutatorPipeline.getMutators());
            this.defaultItems = new LinkedList<>(value.defaultItems);
            this.roundStartTemplate = value.roundStartTemplate;
            this.roundEndTemplate = value.roundEndTemplate;
            this.roundStartLength = value.roundStartLength;
            this.roundLength = value.roundLength;
            this.roundEndLength = value.roundEndLength;
            this.automaticStartPlayerCount = value.automaticStartPlayerCount;
            return this;
        }

        public Builder from(final InstanceTypeConfiguration value) {
            this.name = value.general.name;
            this.nameTemplate = value.general.nameTemplate;
            this.mutators = value.general.mapMutators.stream()
                    .map(x -> findInstanceMutator(x).orElseThrow(() -> new IllegalArgumentException("Unknown mutator " + x)))
                    .collect(Collectors.toSet());
            this.defaultItems = new LinkedList<>(value.round.defaultItems);
            this.roundStartTemplate = value.round.startTemplate;
            this.roundEndTemplate = value.round.endTemplate;
            this.roundStartLength = value.round.start;
            this.roundLength = value.round.length;
            this.roundEndLength = value.round.end;
            this.automaticStartPlayerCount = value.round.automaticStartPlayerCount;
            return this;
        }

        @Override
        public Builder reset() {
            this.name = null;
            this.nameTemplate = Constants.Map.DEFAULT_TEXT_TEMPLATE_NAME;
            this.mutators = new HashSet<>(Constants.Map.DEFAULT_MAP_MUTATORS.values());
            this.defaultItems = new LinkedList<>(Constants.Map.Round.DEFAULT_ITEMS);
            this.roundStartTemplate = Constants.Map.Round.DEFAULT_TEXT_TEMPLATE_START;
            this.roundEndTemplate = Constants.Map.Round.DEFAULT_TEXT_TEMPLATE_END;
            this.roundStartLength = Constants.Map.Round.DEFAULT_START_LENGTH;
            this.roundLength = Constants.Map.Round.DEFAULT_LENGTH;
            this.roundEndLength = Constants.Map.Round.DEFAULT_END_LENGTH;
            this.automaticStartPlayerCount = Constants.Map.Round.DEFAULT_AUTOMATIC_START_PLAYER_COUNT;
            return this;
        }

        @Override
        public Builder key(final ResourceKey key) {
            this.key = key;
            return this;
        }

        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        public Builder nameTemplate(final ComponentTemplate template) {
            this.nameTemplate = template;
            return this;
        }

        public Builder roundStartTemplate(final ComponentTemplate template) {
            this.roundStartTemplate = template;
            return this;
        }

        public Builder roundStartLength(final long length) {
            this.roundStartLength = length;
            return this;
        }

        public Builder roundLength(final long length) {
            this.roundStartLength = length;
            return this;
        }

        public Builder roundEndTemplate(final ComponentTemplate template) {
            this.roundEndTemplate = template;
            return this;
        }

        public Builder roundEndLength(final long length) {
            this.roundEndLength = length;
            return this;
        }

        public Builder item(final ItemStackSnapshot item) {
            Objects.requireNonNull(item);
            this.defaultItems.add(item);
            return this;
        }

        public Builder items(final Collection<ItemStackSnapshot> items) {
            Objects.requireNonNull(items);
            this.defaultItems.addAll(items);
            return this;
        }

        public Builder mutator(final InstanceMutator mutator) {
            Objects.requireNonNull(mutator);
            this.mutators.add(mutator);
            return this;
        }

        public Builder automaticStartPlayerCount(final int playerCount) {
            this.automaticStartPlayerCount = playerCount;
            return this;
        }

        public Builder mutator(final ResourceKey key) {
            Objects.requireNonNull(key);
            findInstanceMutator(key).ifPresent(instanceMutator -> this.mutators.add(instanceMutator));
            return this;
        }

        @Override
        public InstanceType build() {
            Objects.requireNonNull(this.key);
            Objects.requireNonNull(this.nameTemplate);
            Objects.requireNonNull(this.roundStartTemplate);
            Objects.requireNonNull(this.roundEndTemplate);

            return new InstanceType(this);
        }
    }
}
