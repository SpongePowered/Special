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

import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.util.weighted.VariableAmount;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

class PotionItemArchetype extends BasicItemArchetype {

    private final VariableAmount power;
    private final VariableAmount duration;
    private final PotionEffectType effect;

    PotionItemArchetype(final Type type, final VariableAmount quantity, final PotionEffectType effect, final VariableAmount power,
            final VariableAmount duration) {
        super(type.getItemType(), quantity);
        this.effect = Objects.requireNonNull(effect);
        this.power = Objects.requireNonNull(power);
        this.duration = Objects.requireNonNull(duration);
    }

    @Override
    public ItemStack create(final Random rand) {
        final int amount = this.getQuantity().flooredAmount(rand);
        final List<PotionEffect> itemEffects = new ArrayList<>();
        itemEffects.add(PotionEffect.of(this.effect, this.power.flooredAmount(rand), Ticks.of(this.duration.flooredAmount(rand))));
        return ItemStack.builder()
                .itemType(this.getType())
                .quantity(amount)
                .add(Keys.POTION_EFFECTS, itemEffects)
                .build();
    }

    public enum Type {
        NORMAL(ItemTypes.POTION.get()),
        SPLASH(ItemTypes.SPLASH_POTION.get()),
        LINGERING(ItemTypes.LINGERING_POTION.get());

        private final ItemType itemType;

        Type(final ItemType type) {
            this.itemType = type;
        }

        public ItemType getItemType() {
            return this.itemType;
        }
    }
}
