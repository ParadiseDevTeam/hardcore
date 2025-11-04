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

package me.prdis.hardcore.plugin.objects

import com.destroystokyo.paper.profile.PlayerProfile
import io.papermc.paper.ban.BanListType
import io.papermc.paper.datacomponent.item.ResolvableProfile
import me.prdis.hardcore.plugin.config.HardcoreCorpseData
import me.prdis.hardcore.plugin.events.HardcoreEvent
import me.prdis.hardcore.plugin.objects.HardcoreConsts.HARDCORE_NPC_KEY
import me.prdis.hardcore.plugin.objects.HardcoreConsts.plugin
import me.prdis.hardcore.plugin.objects.HardcoreConsts.server
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.BanEntry
import org.bukkit.GameRule
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Mannequin
import org.bukkit.entity.Player
import org.bukkit.entity.Pose
import org.bukkit.event.HandlerList
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

/**
 * @author Medlar Wreak
 */

@Suppress("UNCHECKED_CAST", "UnstableApiUsage")
object HardcoreImpl {
    var isRunning = false
    val linkedInventory = HashMap<UUID, Inventory>()
    val corpses = arrayListOf<HardcoreCorpseData>()
    var usableUnbans = 0
    val completedRootAdvancements = mutableListOf<String>()

    fun start(resumption: Boolean = false) {
        setupWorlds()

        server.pluginManager.registerEvents(HardcoreEvent, plugin)

        if (!resumption) {
            isRunning = true
            usableUnbans = 0
            corpses.clear()
            completedRootAdvancements.clear()

            plugin.config.set("isRunning", true)
            plugin.config.set("usableUnbans", 0)
            plugin.config.set("completedRootAdvancements", emptyList<String>())
            plugin.saveConfig()
        }
    }

    fun stop() {
        HandlerList.unregisterAll(HardcoreEvent)

        server.worlds.forEach {
            it.entities.filter { e ->
                e is Mannequin && e.persistentDataContainer.has(HARDCORE_NPC_KEY)
            }.forEach { e ->
                e.remove()
            }
        }

        server.getBanList(BanListType.PROFILE).getEntries<BanEntry<PlayerProfile>>().forEach {
            it.remove()
        }

        usableUnbans = 0
        corpses.clear()
        completedRootAdvancements.clear()

        plugin.config.set("isRunning", false)
        plugin.config.set("corpses", null)
        plugin.config.set("usableUnbans", 0)
        plugin.config.set("completedRootAdvancements", null)
        plugin.saveConfig()
    }

    private fun setupWorlds() {
        server.worlds.forEach {
            it.setGameRule(GameRule.KEEP_INVENTORY, true)
            it.setGameRule(GameRule.REDUCED_DEBUG_INFO, true)

            it.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
            it.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
        }
    }

    private fun createCorpseNpcFromData(
        corpseData: HardcoreCorpseData,
    ) {
        val world = corpseData.location.world
        val npc = world.spawn(corpseData.location, Mannequin::class.java).apply {
            @Suppress("UnstableApiUsage")
            this.profile =
                ResolvableProfile.resolvableProfile(server.createProfileExact(corpseData.uniqueId, corpseData.name))
            customName(text("${corpseData.name}의 시체"))
            description = null
            setPose(Pose.SLEEPING, true)
            this.isInvulnerable = true
            this.isImmovable = true

            this.persistentDataContainer.set(HARDCORE_NPC_KEY, PersistentDataType.BOOLEAN, true)
        }

        linkedInventory[npc.uniqueId] = corpseData.inventory

        corpses += HardcoreCorpseData.from(npc, corpseData.uniqueId, corpseData.name)
        plugin.config.set("corpses", corpses)
        plugin.saveConfig()
    }

    private fun createCorpseInventory(player: Player): Inventory {
        return server.createInventory(null, 45, text("${player.name}의 시체", NamedTextColor.DARK_GRAY)).apply {
            contents = Array(45) { if (it < 41) player.inventory.contents[it] else ItemStack(Material.AIR) }
        }
    }

    fun createCorpseNPC(player: Player, deathLocation: Location) {
        createCorpseNpcFromData(
            HardcoreCorpseData(
                deathLocation,
                player.uniqueId,
                createCorpseInventory(player),
                player.name
            )
        )
    }
}