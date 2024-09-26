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
package org.spongepowered.royale.instance.configuration.category;

import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.royale.Constants;
import org.spongepowered.royale.configuration.AbstractConfigurationCategory;
import org.spongepowered.royale.template.ComponentTemplate;

import java.util.List;

@ConfigSerializable
public final class RoundCategory extends AbstractConfigurationCategory {

    @Setting
    @Comment("Default items to give players. Order of items will be inserted into the hotbar, left to right.")
    public List<ItemStackSnapshot> defaultItems = Constants.Map.Round.DEFAULT_ITEMS;

    @Setting
    @Comment("Countdown until round starts (in seconds). Default (" + Constants.Map.Round.DEFAULT_START_LENGTH + ").")
    public long start = Constants.Map.Round.DEFAULT_START_LENGTH;

    @Setting
    @Comment("Template used to display the round start.")
    public ComponentTemplate startTemplate = Constants.Map.Round.DEFAULT_TEXT_TEMPLATE_START;

    @Setting
    @Comment("Length of the round (in seconds). Specifying -1 means the instance goes until a winner is found. Default (" + Constants.Map
            .Round.DEFAULT_LENGTH + ").")
    public long length = Constants.Map.Round.DEFAULT_LENGTH;

    @Setting
    @Comment("Length of the time after round ends until everyone is kicked (in seconds). Specifying -1 means the instance must be "
            + "manually terminated. Default (" + Constants.Map.Round.DEFAULT_END_LENGTH + ").")
    public long end = Constants.Map.Round.DEFAULT_END_LENGTH;

    @Setting
    @Comment("Template used to display the winner.")
    public ComponentTemplate endTemplate = Constants.Map.Round.DEFAULT_TEXT_TEMPLATE_END;

    @Setting("players-to-start-round-automatically")
    @Comment("If specified, once the instance has this amount of players, it will "
            + "automatically start. Specifying -1 means the instance must be started manually. Default (" + Constants.Map.Round
            .DEFAULT_AUTOMATIC_START_PLAYER_COUNT + ").")
    public int automaticStartPlayerCount = Constants.Map.Round.DEFAULT_AUTOMATIC_START_PLAYER_COUNT;
}
