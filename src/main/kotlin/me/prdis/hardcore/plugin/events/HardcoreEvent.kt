/*
 * Copyright (C) 2025 Paradise Dev Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 */

package me.prdis.hardcore.plugin.events

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent
import com.destroystokyo.paper.event.server.PaperServerListPingEvent
import com.destroystokyo.paper.profile.PlayerProfile
import io.papermc.paper.event.player.AsyncChatEvent
import me.prdis.hardcore.plugin.objects.HardcoreConsts.HARDCORE_NPC_KEY
import me.prdis.hardcore.plugin.objects.HardcoreConsts.isRecipe
import me.prdis.hardcore.plugin.objects.HardcoreConsts.isRootItself
import me.prdis.hardcore.plugin.objects.HardcoreConsts.plugin
import me.prdis.hardcore.plugin.objects.HardcoreConsts.server
import me.prdis.hardcore.plugin.objects.HardcoreImpl
import me.prdis.hardcore.plugin.objects.HardcoreImpl.completedRootAdvancements
import me.prdis.hardcore.plugin.objects.HardcoreImpl.createCorpseNPC
import me.prdis.hardcore.plugin.objects.HardcoreImpl.usableUnbans
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.BanEntry
import org.bukkit.entity.EntityType
import org.bukkit.entity.Mannequin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*
import java.util.*

/**
 * @author Medlar Wreak
 */

object HardcoreEvent : Listener {
    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        joinMessage(null)

        // Sync advancement progress
        server.onlinePlayers.toTypedArray().forEach {
            if (player.uniqueId != it.uniqueId) {
                server.advancementIterator().asSequence().forEach { advancement ->
                    if (!advancement.isRecipe) {
                        it.getAdvancementProgress(advancement).awardedCriteria.forEach { criterion ->
                            player.getAdvancementProgress(advancement).awardCriteria(criterion)
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    fun PlayerQuitEvent.onQuit() {
        quitMessage(null)
    }

    @EventHandler
    fun PlayerAdvancementCriterionGrantEvent.onCriterionGrant() {
        if (advancement.isRecipe) return

        server.onlinePlayers.toTypedArray().forEach {
            it.getAdvancementProgress(advancement).awardCriteria(criterion)
        }

        val advancementKey = advancement.key().value().split("/").firstOrNull() ?: ""
        if (advancementKey.isEmpty()) return
        if (completedRootAdvancements.contains(advancementKey)) return

        val childAdvancements = server.advancementIterator().asSequence()
            .filter { advc -> !advc.isRecipe && !advc.isRootItself && advc.key().value().startsWith(advancementKey) }
            .toSet()

        val isRootAdvancementDone = server.onlinePlayers.toTypedArray().all { player ->
            childAdvancements.all { advc -> player.getAdvancementProgress(advc).isDone }
        }

        if (isRootAdvancementDone) {
            completedRootAdvancements.add(advancementKey)

            ++usableUnbans
            plugin.config.set("usableUnbans", usableUnbans)
            plugin.config.set("completedRootAdvancements", completedRootAdvancements)
            plugin.saveConfig()
        }
    }

    @EventHandler
    fun AsyncChatEvent.onChat() {
        if (!player.isOp) isCancelled = true
    }

    @EventHandler
    fun PlayerDeathEvent.onDeath() {
        createCorpseNPC(player, player.location.clone().apply {
            pitch = 0f
            yaw = 0f
        })
        player.inventory.clear()
        player.ban<BanEntry<PlayerProfile>>(" ", null as Date?, null, true)
        deathMessage(null)
    }

    @EventHandler
    fun PlayerInteractAtEntityEvent.onPlayerInteraectAtEntity() {
        if (rightClicked is Mannequin && rightClicked.persistentDataContainer.has(HARDCORE_NPC_KEY)) {
            val npc = rightClicked as Mannequin
            HardcoreImpl.linkedInventory[npc.uniqueId]?.let {
                player.openInventory(it)
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun PlayerToggleSneakEvent.onToggleSneak() {
        if (isSneaking) {
            val npc = player.getNearbyEntities(2.0, 2.0, 2.0)
                .find { it.type == EntityType.MANNEQUIN && it.persistentDataContainer.has(HARDCORE_NPC_KEY) } as? Mannequin

            if (npc != null) {
                HardcoreImpl.linkedInventory[npc.uniqueId]?.let {
                    player.openInventory(it)
                }
            }
        }
    }

    @EventHandler
    fun PlayerCommandPreprocessEvent.onCommandPreProcess() {
        if (!player.isOp) isCancelled = true
    }

    @EventHandler
    fun PaperServerListPingEvent.onServerListPing() {
        setHidePlayers(true)
        motd(text("HARDCORE", NamedTextColor.RED).decorate(TextDecoration.BOLD))
    }
}