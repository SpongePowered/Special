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
package org.spongepowered.royale.instance.scoreboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.scoreboard.criteria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.royale.instance.InstanceImpl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Scoreboard format is as follows:
 *
 *          Instance_Name (Bastian)
 *          Instance_Type_Name (Deathmatch, Free-For-All, etc)
 *      --------------------
 *
 *          PlayerA
 *          PlayerB
 *          PlayerC
 *          PlayerD (imagine this one is got a strikethrough as they lost)
 */
public final class InstanceScoreboard {

    private final Scoreboard scoreboard;
    private final Objective objective;
    private final Score instanceTypeScore, dashesScore, emptyLineScore;
    private final Map<UUID, PlayerData> playerData = new HashMap<>();

    public InstanceScoreboard(final InstanceImpl instance) {
        this.scoreboard = Scoreboard.builder().build();
        this.objective =
                Objective.builder().name("main").displayName(Component.text(instance.getWorldKey().formatted(), NamedTextColor.GREEN))
                        .criterion(Criteria.DUMMY).build();

        // Instance type
        this.instanceTypeScore = this.objective.findOrCreateScore("instanceType");
        this.instanceTypeScore.setDisplay(Component.text(instance.getType().name(), NamedTextColor.RED));
        this.instanceTypeScore.setScore(0);

        // Dashes
        this.dashesScore = this.objective.findOrCreateScore("dashes");
        this.dashesScore.setDisplay(Component.text("----------------"));
        this.dashesScore.setScore(0);

        // Empty line
        this.emptyLineScore = this.objective.findOrCreateScore("emptyLine");
        this.emptyLineScore.setDisplay(Component.empty());
        this.emptyLineScore.setScore(0);

        this.scoreboard.addObjective(this.objective);
        this.scoreboard.updateDisplaySlot(this.objective, DisplaySlots.SIDEBAR);

        this.sortScoreboard();
    }

    public Scoreboard getHandle() {
        return this.scoreboard;
    }

    public void addPlayer(final ServerPlayer player) {
        final Score score = this.objective.findOrCreateScore(player);
        score.setDisplay(Component.text(player.name(), NamedTextColor.DARK_GREEN));
        score.setScore(1);

        final Team team = Team.builder().name(player.name()).build();
        team.addMember(player.teamRepresentation());
        this.scoreboard.registerTeam(team);

        this.playerData.put(player.uniqueId(), new PlayerData(score, team, player.name(), player.uniqueId()));
        player.setScoreboard(this.scoreboard);

        this.sortScoreboard();
    }

    public void killPlayer(final ServerPlayer player) {
        if (!this.playerData.containsKey(player.uniqueId())) {
            return;
        }

        final PlayerData data = this.playerData.get(player.uniqueId());
        this.objective.removeScore(data.score);
        final Score score = this.objective.findOrCreateScore(player);
        score.setDisplay(Component.text(player.name(), NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
        score.setScore(0);
        data.score = score;
        data.dead = true;

        this.sortScoreboard();
    }

    public void removePlayer(final ServerPlayer player) {
        final PlayerData data = this.playerData.remove(player.uniqueId());
        if (data != null) {
            data.team.unregister();
            this.objective.removeScore(data.score);
        }
        this.sortScoreboard();
        Sponge.server().serverScoreboard().ifPresent(player::setScoreboard);
    }

    private void sortScoreboard() {
        final List<PlayerData> alive = new ArrayList<>();
        final List<PlayerData> dead = new ArrayList<>();

        for (final Map.Entry<UUID, PlayerData> entry : this.playerData.entrySet()) {
            if (entry.getValue().dead) {
                dead.add(entry.getValue());
            } else {
                alive.add(entry.getValue());
            }
        }

        // Inverse alphabetical order
        final Comparator<PlayerData> comparator = (p1, p2) -> -p1.name.compareTo(p2.name);

        dead.sort(comparator);
        alive.sort(comparator);

        int position = 0;

        for (int i = 0; i < dead.size(); i++, position++) {
            this.playerData.get(dead.get(i).uuid).score.setScore(position);
        }

        for (int i = 0; i < alive.size(); i++, position++) {
            this.playerData.get(alive.get(i).uuid).score.setScore(position);
        }

        this.emptyLineScore.setScore(++position);
        this.dashesScore.setScore(++position);
        this.instanceTypeScore.setScore(++position);
    }

    private static class PlayerData {

        String name;
        Score score;
        Team team;
        boolean dead;
        UUID uuid;

        PlayerData(final Score score, final Team team, final String name, final UUID uuid) {
            this.name = name;
            this.score = score;
            this.team = team;
            this.uuid = uuid;
        }
    }
}
