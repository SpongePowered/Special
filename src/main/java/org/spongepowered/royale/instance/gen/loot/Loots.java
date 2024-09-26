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

import static org.spongepowered.api.util.weighted.VariableAmount.fixed;
import static org.spongepowered.api.util.weighted.VariableAmount.range;
import static org.spongepowered.royale.instance.gen.loot.PotionItemArchetype.Type.LINGERING;
import static org.spongepowered.royale.instance.gen.loot.PotionItemArchetype.Type.NORMAL;
import static org.spongepowered.royale.instance.gen.loot.PotionItemArchetype.Type.SPLASH;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.enchantment.EnchantmentTypes;
import org.spongepowered.api.util.weighted.ChanceTable;
import org.spongepowered.api.util.weighted.LootTable;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.util.weighted.WeightedTable;

import java.util.HashMap;
import java.util.Map;

public final class Loots {

    // @formatter:off

    private static final LootTable<ItemArchetype> LOW_LEVEL = new LootTable<>();
    private static final LootTable<ItemArchetype> MID_LEVEL = new LootTable<>();
    private static final LootTable<ItemArchetype> HIGH_LEVEL = new LootTable<>();
    private static final LootTable<ItemArchetype> RARE_LEVEL = new LootTable<>();

    private static final WeightedTable<ItemArchetype> BASIC_FOOD = new WeightedTable<>();
    private static final WeightedTable<ItemArchetype> HIGH_FOOD = new WeightedTable<>();

    private static final WeightedTable<ItemArchetype> BASIC_COMBAT = new WeightedTable<>();
    private static final WeightedTable<ItemArchetype> BASIC_RANGED = new WeightedTable<>();

    private static final WeightedTable<ItemArchetype> MID_COMBAT = new WeightedTable<>();
    private static final WeightedTable<ItemArchetype> MID_RANGED = new WeightedTable<>();

    private static final ChanceTable<ItemArchetype> HIGH_COMBAT = new ChanceTable<>();

    private static final WeightedTable<ItemArchetype> RARE_ITEMS = new WeightedTable<>();

    private static final ChanceTable<ItemArchetype> WEIRD_ITEMS = new ChanceTable<>();

    private static final WeightedTable<ItemArchetype> BASIC_POTIONS = new WeightedTable<>();
    private static final WeightedTable<ItemArchetype> HIGH_POTIONS = new WeightedTable<>();

    private static final Map<String, LootTable<ItemArchetype>> LOOT_TABLES = new HashMap<>();

    // @formatter:on

    static {
        Loots.LOOT_TABLES.put("lowlevel", Loots.LOW_LEVEL);
        Loots.LOOT_TABLES.put("midlevel", Loots.MID_LEVEL);
        Loots.LOOT_TABLES.put("highlevel", Loots.HIGH_LEVEL);
        Loots.LOOT_TABLES.put("rarelevel", Loots.RARE_LEVEL);

        Loots.LOW_LEVEL.addTable(Loots.BASIC_FOOD);
        Loots.LOW_LEVEL.addTable(Loots.BASIC_COMBAT);
        Loots.LOW_LEVEL.addTable(Loots.BASIC_RANGED);
        Loots.LOW_LEVEL.addTable(Loots.WEIRD_ITEMS);

        Loots.MID_LEVEL.addTable(Loots.BASIC_FOOD);
        Loots.MID_LEVEL.addTable(Loots.BASIC_POTIONS);
        Loots.MID_LEVEL.addTable(Loots.MID_COMBAT);
        Loots.MID_LEVEL.addTable(Loots.MID_RANGED);
        Loots.MID_LEVEL.addTable(Loots.HIGH_FOOD);
        Loots.MID_LEVEL.addTable(Loots.WEIRD_ITEMS);

        Loots.HIGH_LEVEL.addTable(Loots.MID_COMBAT);
        Loots.HIGH_LEVEL.addTable(Loots.HIGH_COMBAT);
        Loots.HIGH_LEVEL.addTable(Loots.HIGH_POTIONS);
        Loots.HIGH_LEVEL.addTable(Loots.HIGH_FOOD);
        Loots.HIGH_LEVEL.addTable(Loots.WEIRD_ITEMS);

        Loots.RARE_LEVEL.addTable(Loots.HIGH_COMBAT);
        Loots.RARE_LEVEL.addTable(Loots.HIGH_POTIONS);
        Loots.RARE_LEVEL.addTable(Loots.HIGH_FOOD);
        Loots.RARE_LEVEL.addTable(Loots.WEIRD_ITEMS);
        Loots.RARE_LEVEL.addTable(Loots.RARE_ITEMS);

        // @formatter:off

        // basic_food
        Loots.BASIC_FOOD.add(     item(   ItemTypes.APPLE.get(),                range(1, 3)),   5);
        Loots.BASIC_FOOD.add(     item(   ItemTypes.BAKED_POTATO.get(),         range(1, 3)),   3);
        Loots.BASIC_FOOD.add(     item(   ItemTypes.BREAD.get(),                range(1, 3)),   5);
        Loots.BASIC_FOOD.add(     item(   ItemTypes.CAKE.get(),                 fixed(1)),      1);
        Loots.BASIC_FOOD.add(     item(   ItemTypes.CARROT.get(),               range(1, 4)),   3);
        Loots.BASIC_FOOD.add(     item(   ItemTypes.COOKED_CHICKEN.get(),       range(1, 3)),   3);
        Loots.BASIC_FOOD.add(     item(   ItemTypes.COOKED_COD.get(),           range(1, 3)),   1);
        Loots.BASIC_FOOD.add(     item(   ItemTypes.COOKED_SALMON.get(),        range(1, 3)),   1);
        Loots.BASIC_FOOD.add(     item(   ItemTypes.COOKED_MUTTON.get(),        range(1, 3)),   3);
        Loots.BASIC_FOOD.add(     item(   ItemTypes.COOKED_RABBIT.get(),        range(1, 3)),   3);
        Loots.BASIC_FOOD.add(     item(   ItemTypes.COOKIE.get(),               range(2, 8)),   5);
        Loots.BASIC_FOOD.add(     item(   ItemTypes.MELON.get(),                range(1, 3)),   5);
        Loots.BASIC_FOOD.add(     item(   ItemTypes.BOWL.get(),                 range(1, 3)),   2);
        Loots.BASIC_FOOD.add(     item(   ItemTypes.RED_MUSHROOM.get(),         range(1, 3)),   3);
        Loots.BASIC_FOOD.add(     item(   ItemTypes.BROWN_MUSHROOM.get(),       range(1, 3)),   4);
        Loots.BASIC_FOOD.add(     item(   ItemTypes.HONEY_BOTTLE.get(),         range(1, 2)),   3);

        // high_food
        Loots.HIGH_FOOD.add(      item(   ItemTypes.COOKED_BEEF.get(),          range(1, 2)),   2);
        Loots.HIGH_FOOD.add(      item(   ItemTypes.COOKED_PORKCHOP.get(),      range(1, 2)),   3);
        Loots.HIGH_FOOD.add(      item(   ItemTypes.MUSHROOM_STEW.get(),        fixed(1)),      3);
        Loots.HIGH_FOOD.add(      item(   ItemTypes.RABBIT_STEW.get(),          range(1, 3)),   2);
        Loots.HIGH_FOOD.add(      item(   ItemTypes.PUMPKIN_PIE.get(),          fixed(1)),      3);
        Loots.HIGH_FOOD.add(      item(   ItemTypes.GOLDEN_APPLE.get(),         range(1, 3)),   1);
        Loots.HIGH_FOOD.add(      item(   ItemTypes.GOLDEN_CARROT.get(),        range(1, 3)),   1);

        // basic_combat
        Loots.BASIC_COMBAT.add(   item(   ItemTypes.WOODEN_AXE.get(),           fixed(1)),      5);
        Loots.BASIC_COMBAT.add(   item(   ItemTypes.WOODEN_SWORD.get(),         fixed(1)),      5);
        Loots.BASIC_COMBAT.add(   item(   ItemTypes.STONE_AXE.get(),            fixed(1)),      4);
        Loots.BASIC_COMBAT.add(   item(   ItemTypes.STONE_SWORD.get(),          fixed(1)),      4);
        Loots.BASIC_COMBAT.add(   item(   ItemTypes.FISHING_ROD.get(),          fixed(1)),      1);
        Loots.BASIC_COMBAT.add(   item(   ItemTypes.FLINT_AND_STEEL.get(),      fixed(1)),      2);
        Loots.BASIC_COMBAT.add(   item(   ItemTypes.SHIELD.get(),               fixed(1)),      1);
        Loots.BASIC_COMBAT.add(   item(   ItemTypes.LEATHER_HELMET.get(),       fixed(1)),      5);
        Loots.BASIC_COMBAT.add(   item(   ItemTypes.LEATHER_CHESTPLATE.get(),   fixed(1)),      1);
        Loots.BASIC_COMBAT.add(   item(   ItemTypes.LEATHER_LEGGINGS.get(),     fixed(1)),      2);
        Loots.BASIC_COMBAT.add(   item(   ItemTypes.LEATHER_BOOTS.get(),        fixed(1)),      3);
        Loots.BASIC_COMBAT.add(   item(   ItemTypes.TRIDENT.get(),              fixed(1)),      0.5);

        // basic_ranged
        Loots.BASIC_RANGED.add(   item(   ItemTypes.BOW.get(),                  fixed(1)),      5);
        Loots.BASIC_RANGED.add(   item(   ItemTypes.ARROW.get(),                range(2, 6)),   10);
        Loots.BASIC_RANGED.add(   item(   ItemTypes.LEATHER_HELMET.get(),       fixed(1)),      5);
        Loots.BASIC_RANGED.add(   item(   ItemTypes.LEATHER_CHESTPLATE.get(),   fixed(1)),      1);
        Loots.BASIC_RANGED.add(   item(   ItemTypes.LEATHER_LEGGINGS.get(),     fixed(1)),      2);
        Loots.BASIC_RANGED.add(   item(   ItemTypes.LEATHER_BOOTS.get(),        fixed(1)),      3);

        // mid_combat
        Loots.MID_COMBAT.add(     item(   ItemTypes.GOLDEN_AXE.get(),           fixed(1)),      12);
        Loots.MID_COMBAT.add(     item(   ItemTypes.GOLDEN_SWORD.get(),         fixed(1)),      12);
        Loots.MID_COMBAT.add(     item(   ItemTypes.IRON_AXE.get(),             fixed(1)),      8);
        Loots.MID_COMBAT.add(     item(   ItemTypes.IRON_SWORD.get(),           fixed(1)),      8);
        Loots.MID_COMBAT.add(     item(   ItemTypes.SHIELD.get(),               fixed(1)),      12.5);
        Loots.MID_COMBAT.add(     item(   ItemTypes.GOLDEN_HELMET.get(),        fixed(1)),      7);
        Loots.MID_COMBAT.add(     item(   ItemTypes.GOLDEN_CHESTPLATE.get(),    fixed(1)),      2);
        Loots.MID_COMBAT.add(     item(   ItemTypes.GOLDEN_LEGGINGS.get(),      fixed(1)),      6);
        Loots.MID_COMBAT.add(     item(   ItemTypes.GOLDEN_BOOTS.get(),         fixed(1)),      7);
        Loots.MID_COMBAT.add(     item(   ItemTypes.IRON_HELMET.get(),          fixed(1)),      4);
        Loots.MID_COMBAT.add(     item(   ItemTypes.IRON_CHESTPLATE.get(),      fixed(1)),      1);
        Loots.MID_COMBAT.add(     item(   ItemTypes.IRON_LEGGINGS.get(),        fixed(1)),      2);
        Loots.MID_COMBAT.add(     item(   ItemTypes.IRON_BOOTS.get(),           fixed(1)),      4);
        Loots.MID_COMBAT.add(     item(   ItemTypes.TURTLE_HELMET.get(),        fixed(1)),      8);

        // mid_ranged
        Loots.MID_RANGED.add(     item(   ItemTypes.BOW.get(),              fixed(1)),      5);
        Loots.MID_RANGED.add(     item(   ItemTypes.ARROW.get(),            range(5, 12)),  10);
        Loots.MID_RANGED.add(     item(   ItemTypes.TRIDENT.get(),          fixed(1)),      2);
        Loots.MID_RANGED.add(item(ItemTypes.LEATHER_CHESTPLATE.get(), fixed(1), false,
                Component.text("Tough", NamedTextColor.RED),
                new HashMap<EnchantmentType, VariableAmount>() {{
                    put(EnchantmentTypes.PROTECTION.get(), fixed(1));
                }}), 1);
        Loots.MID_RANGED.add(item(ItemTypes.LEATHER_LEGGINGS.get(), fixed(1), false,
                Component.text("Tough", NamedTextColor.RED),
                new HashMap<EnchantmentType, VariableAmount>() {{
                    put(EnchantmentTypes.PROTECTION.get(), fixed(1));
                }}), 1);

        // high_combat
        Loots.HIGH_COMBAT.add(    item(   ItemTypes.DIAMOND_AXE.get(),          fixed(1)),      0.004);
        Loots.HIGH_COMBAT.add(    item(   ItemTypes.DIAMOND_SWORD.get(),        fixed(1)),      0.0035);
        Loots.HIGH_COMBAT.add(    item(   ItemTypes.DIAMOND_HELMET.get(),       fixed(1)),      0.0028);
        Loots.HIGH_COMBAT.add(    item(   ItemTypes.DIAMOND_CHESTPLATE.get(),   fixed(1)),      0.0016);
        Loots.HIGH_COMBAT.add(    item(   ItemTypes.DIAMOND_LEGGINGS.get(),     fixed(1)),      0.002);
        Loots.HIGH_COMBAT.add(    item(   ItemTypes.DIAMOND_BOOTS.get(),        fixed(1)),      0.0024);

        // rare_items
        Loots.RARE_ITEMS.add(item(ItemTypes.DIAMOND_SWORD.get(), fixed(1), false,
                Component.text("Searing ", NamedTextColor.RED).append(Component.text("Sponge", NamedTextColor.YELLOW)),
                new HashMap<EnchantmentType, VariableAmount>() {{
                    put(EnchantmentTypes.FIRE_ASPECT.get(), range(1, 2));
                    put(EnchantmentTypes.KNOCKBACK.get(), range(1, 2));
                }}), 0.0018);

        Loots.RARE_ITEMS.add(item(ItemTypes.SHIELD.get(), fixed(1), true,
                Component.text("Valiant ", NamedTextColor.GRAY).append(Component.text("Sponge", NamedTextColor.YELLOW)),
                new HashMap<EnchantmentType, VariableAmount>() {{
                    put(EnchantmentTypes.UNBREAKING.get(), range(1, 3));
                }}), 0.0015);
        Loots.RARE_ITEMS.add(item(ItemTypes.TRIDENT.get(), fixed(1), true,
                Component.text("Surfing ", NamedTextColor.AQUA).append(Component.text("Sponge", NamedTextColor.YELLOW)),
                new HashMap<EnchantmentType, VariableAmount>() {{
                    put(EnchantmentTypes.RIPTIDE.get(), fixed(1));
                }}), 0.0018);
        Loots.RARE_ITEMS.add(item(ItemTypes.ELYTRA.get(), fixed(1)), 0.00125);

        // weird_items
        Loots.WEIRD_ITEMS.add(    item(   ItemTypes.SPONGE.get(),                 range(1, 6)),   15);
        Loots.WEIRD_ITEMS.add(    item(   ItemTypes.ROTTEN_FLESH.get(),           range(1, 9)),   6);
        Loots.WEIRD_ITEMS.add(    item(   ItemTypes.POISONOUS_POTATO.get(),       range(1, 3)),   2.5);
        Loots.WEIRD_ITEMS.add(    item(   ItemTypes.NAUTILUS_SHELL.get(),         fixed(1)),      4);
        Loots.WEIRD_ITEMS.add(    item(   ItemTypes.PUFFERFISH.get(),             fixed(1)),      2);

        // basic_potions

        Loots.BASIC_POTIONS.add(  potion(NORMAL,      fixed(1),       PotionEffectTypes.FIRE_RESISTANCE.get(),  fixed(1),   range(200, 400)),   1);
        Loots.BASIC_POTIONS.add(  potion(NORMAL,      fixed(1),       PotionEffectTypes.INSTANT_HEALTH.get(),   fixed(1),   fixed(1)),          3);
        Loots.BASIC_POTIONS.add(  potion(NORMAL,      fixed(1),       PotionEffectTypes.NIGHT_VISION.get(),     fixed(1),   range(200, 400)),   4);
        Loots.BASIC_POTIONS.add(  potion(NORMAL,      fixed(1),       PotionEffectTypes.REGENERATION.get(),     fixed(1),   range(20, 80)),     2);

        Loots.BASIC_POTIONS.add(  potion(SPLASH,      fixed(1),       PotionEffectTypes.HUNGER.get(),           fixed(1),   range(100, 200)),   5);
        Loots.BASIC_POTIONS.add(  potion(SPLASH,      fixed(1),       PotionEffectTypes.INSTANT_DAMAGE.get(),   fixed(1),   fixed(1)),          2);
        Loots.BASIC_POTIONS.add(  potion(SPLASH,      fixed(1),       PotionEffectTypes.SLOWNESS.get(),         fixed(1),   range(60, 200)),    6);

        // high_potions
        Loots.HIGH_POTIONS.add(   potion(NORMAL,      fixed(1),       PotionEffectTypes.FIRE_RESISTANCE.get(),  fixed(1),   range(200, 400)),   6   );
        Loots.HIGH_POTIONS.add(   potion(NORMAL,      fixed(1),       PotionEffectTypes.HEALTH_BOOST.get(),     fixed(1),   range(200, 400)),   4   );
        Loots.HIGH_POTIONS.add(   potion(NORMAL,      fixed(1),       PotionEffectTypes.INSTANT_HEALTH.get(),   fixed(1),   fixed(1)),          4   );
        Loots.HIGH_POTIONS.add(   potion(NORMAL,      fixed(1),       PotionEffectTypes.INVISIBILITY.get(),     fixed(1),   range(100, 200)),   1   );
        Loots.HIGH_POTIONS.add(   potion(NORMAL,      fixed(1),       PotionEffectTypes.JUMP_BOOST.get(),       fixed(1),   range(100, 200)),   4   );
        Loots.HIGH_POTIONS.add(   potion(NORMAL,      fixed(1),       PotionEffectTypes.NIGHT_VISION.get(),     fixed(1),   range(200, 400)),   6   );
        Loots.HIGH_POTIONS.add(   potion(NORMAL,      fixed(1),       PotionEffectTypes.REGENERATION.get(),     fixed(1),   range(40, 120)),    2   );
        Loots.HIGH_POTIONS.add(   potion(NORMAL,      fixed(1),       PotionEffectTypes.REGENERATION.get(),     fixed(2),   range(20, 100)),    0.1 );
        Loots.HIGH_POTIONS.add(   potion(NORMAL,      fixed(1),       PotionEffectTypes.SPEED.get(),            fixed(1),   range(100, 200)),   2   );
        Loots.HIGH_POTIONS.add(   potion(NORMAL,      fixed(1),       PotionEffectTypes.SPEED.get(),            fixed(2),   range(50, 100)),    0.3 );

        Loots.HIGH_POTIONS.add(   potion(SPLASH,      fixed(1),       PotionEffectTypes.BLINDNESS.get(),        fixed(1),   range(60, 120)),    2   );
        Loots.HIGH_POTIONS.add(   potion(SPLASH,      fixed(1),       PotionEffectTypes.HUNGER.get(),           fixed(1),   range(100, 200)),   6   );
        Loots.HIGH_POTIONS.add(   potion(SPLASH,      fixed(1),       PotionEffectTypes.INSTANT_DAMAGE.get(),   fixed(1),   fixed(1)),          3   );
        Loots.HIGH_POTIONS.add(   potion(SPLASH,      fixed(1),       PotionEffectTypes.NAUSEA.get(),           fixed(1),   range(60, 120)),    2   );
        Loots.HIGH_POTIONS.add(   potion(SPLASH,      fixed(1),       PotionEffectTypes.SLOWNESS.get(),         fixed(1),   range(100, 200)),   6   );
        Loots.HIGH_POTIONS.add(   potion(SPLASH,      fixed(1),       PotionEffectTypes.WEAKNESS.get(),         fixed(1),   range(60, 120)),    4   );

        Loots.HIGH_POTIONS.add(   potion(LINGERING,   fixed(1),       PotionEffectTypes.HUNGER.get(),           fixed(1),   range(60, 120)),    1   );

        // @formatter:on
    }

    private Loots() {
    }

    public static LootTable<ItemArchetype> getTable(final String id) {
        final LootTable<ItemArchetype> table = Loots.LOOT_TABLES.get(id);
        if (table == null) {
            throw new IllegalArgumentException(String.format("Loot table: %s not found!", id));
        }
        return table;
    }

    private static ItemArchetype item(final ItemType type, final VariableAmount quantity) {
        return new BasicItemArchetype(type, quantity);
    }

    private static ItemArchetype item(final ItemType type, final VariableAmount quantity, final boolean unbreakable, final Component name,
            final Map<EnchantmentType, VariableAmount> enchantments) {
        return new EnchantedItemArchetype(type, quantity, unbreakable, name, enchantments);
    }

    private static ItemArchetype potion(final PotionItemArchetype.Type type, final VariableAmount quantity, final PotionEffectType effect,
            final VariableAmount power, final VariableAmount duration) {
        return new PotionItemArchetype(type, quantity, effect, power, duration);
    }
}
