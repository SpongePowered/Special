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
package org.spongepowered.royale.template;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.placeholder.PlaceholderContext;
import org.spongepowered.api.placeholder.PlaceholderParser;
import org.spongepowered.api.registry.RegistryTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ComponentTemplate {

    private final static Pattern PLACEHOLDER_TAG = Pattern.compile("<(?<token>pl_(?<placeholder>.+:.+)(_(?<arg>.+))?)>");
    private final static ParserContextPair NULL_PLACEHOLDER = new ParserContextPair(null, null);

    private final String templatedString;
    private final Map<String, ParserContextPair> detectedPlaceholders;

    public ComponentTemplate(final String templatedString) {
        this.templatedString = templatedString;
        this.detectedPlaceholders = ComponentTemplate.determinePlaceholders(templatedString);
    }

    // Determines the placeholders available in the
    private static Map<String, ParserContextPair> determinePlaceholders(final String templatedString) {
        final Map<String, ParserContextPair> map = new HashMap<>();
        // scan the string for the token `<pl_.+:.+(_.+)?>`
        final Matcher matcher = ComponentTemplate.PLACEHOLDER_TAG.matcher(templatedString);
        while (matcher.find()) {
            final String entry = matcher.group("token"); // entire thing needed for template matching
            final String placeholder = matcher.group("placeholder");
            try {
                final Optional<PlaceholderParser> parser = Sponge.game().registry(RegistryTypes.PLACEHOLDER_PARSER).findValue(ResourceKey.resolve(placeholder));
                if (parser.isPresent()) {
                    map.put(entry, new ParserContextPair(parser.get(), matcher.group("arg")));
                } else {
                    map.put(entry, ComponentTemplate.NULL_PLACEHOLDER);
                }
            } catch (final RuntimeException ex) {
                map.put(entry, ComponentTemplate.NULL_PLACEHOLDER);
            }
        }

        return map;
    }

    public Component parse(@Nullable final Object associatedObject, final Map<String, ComponentLike> arbitraryTokens) {
        final TagResolver.Builder builder = TagResolver.builder();
        this.detectedPlaceholders.forEach((key, component) -> builder.resolver(Placeholder.component(key, component.createComponent(associatedObject))));
        arbitraryTokens.forEach((key, component) -> builder.resolver(Placeholder.component(key, component)));
        return MiniMessage.miniMessage().deserialize(this.templatedString, builder.build());
    }

    public String getTemplatedString() {
        return this.templatedString;
    }

    static final class ParserContextPair {

        @Nullable private final PlaceholderParser parser;
        @Nullable private final String args;

        ParserContextPair(@Nullable final PlaceholderParser parser, @Nullable final String args) {
            this.parser = parser;
            this.args = args;
        }

        Component createComponent(@Nullable final Object associatedObject) {
            if (this.parser == null) {
                return Component.empty();
            }

            return this.parser.parse(PlaceholderContext.builder().argumentString(this.args).associatedObject(associatedObject).build()).asComponent();
        }

    }

}
