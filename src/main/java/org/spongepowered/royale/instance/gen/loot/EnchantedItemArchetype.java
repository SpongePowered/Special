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
package org.spongepowered.royale.instance.gen.loot;

import net.kyori.adventure.text.Component;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.weighted.VariableAmount;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

class EnchantedItemArchetype implements ItemArchetype {

    private final boolean unbreakable;
    private final ItemType type;
    private final Map<EnchantmentType, VariableAmount> enchantments;
    private final Component name;
    private final VariableAmount quantity;

    EnchantedItemArchetype(final ItemType type, final VariableAmount quantity, final boolean unbreakable, final Component name,
            final Map<EnchantmentType, VariableAmount> enchantments) {
        this.type = Objects.requireNonNull(type);
        this.quantity = Objects.requireNonNull(quantity);
        this.unbreakable = unbreakable;
        this.name = Objects.requireNonNull(name);
        this.enchantments = Objects.requireNonNull(enchantments);
    }

    @Override
    public ItemStack create(final Random rand) {
        final int amount = this.quantity.flooredAmount(rand);
        final ItemStack itemStack = ItemStack.builder()
                .itemType(this.type)
                .quantity(amount)
                .build();

        final List<Enchantment> enchantmentsToApply = new ArrayList<>();
        for (final Map.Entry<EnchantmentType, VariableAmount> entry : this.enchantments.entrySet()) {
            final int level = entry.getValue().flooredAmount(rand);
            if (level > 0) {
                enchantmentsToApply.add(Enchantment.builder().type(entry.getKey()).level(level).build());
            }
        }

        itemStack.offer(Keys.APPLIED_ENCHANTMENTS, enchantmentsToApply);
        itemStack.offer(Keys.CUSTOM_NAME, this.name);
        itemStack.offer(Keys.IS_UNBREAKABLE, this.unbreakable);

        return itemStack;
    }

    public final ItemType getType() {
        return this.type;
    }

    public final VariableAmount getQuantity() {
        return this.quantity;
    }

    public final boolean isUnbreakable() {
        return this.unbreakable;
    }

    public final Map<EnchantmentType, VariableAmount> getEnchantments() {
        return Collections.unmodifiableMap(this.enchantments);
    }
}
