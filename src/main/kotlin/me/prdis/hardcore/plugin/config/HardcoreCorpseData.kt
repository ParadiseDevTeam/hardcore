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

package me.prdis.hardcore.plugin.config

import me.prdis.hardcore.plugin.objects.HardcoreConsts.server
import me.prdis.hardcore.plugin.objects.HardcoreImpl
import net.kyori.adventure.text.Component.text
import org.bukkit.Location
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.entity.Mannequin
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.io.encoding.Base64

/**
 * @author Medlar Wreak
 */

data class HardcoreCorpseData(
    val location: Location,
    val uniqueId: UUID,
    val inventory: Inventory,
    val name: String,
) : ConfigurationSerializable {
    companion object {
        fun from(mannequin: Mannequin, uuid: UUID, name: String): HardcoreCorpseData {
            val location = mannequin.location.clone()
            return HardcoreCorpseData(
                location,
                uuid,
                HardcoreImpl.linkedInventory[mannequin.uniqueId]!!,
                name,
            )
        }

        @Suppress("UNUSED")
        @JvmStatic
        fun deserialize(args: Map<String, Any>): HardcoreCorpseData {
            val location = args["location"] as Location
            val uuid = UUID.fromString(args["uniqueId"] as String)
            val name = args["name"] as String

            val inventoryContents = server.createInventory(null, 45, text(name)).apply {
                val contents =
                    args["inventory"] as? String ?: throw IllegalArgumentException("Inventory data is missing")
                val items = ItemStack.deserializeItemsFromBytes(Base64.decode(contents))
                this.contents = items
            }

            return HardcoreCorpseData(location, uuid, inventoryContents, name)
        }
    }

    override fun serialize(): MutableMap<String, Any> {
        val out = mutableMapOf<String, Any>()
        out["location"] = location
        out["uniqueId"] = uniqueId.toString()
        out["inventory"] = Base64.encode(ItemStack.serializeItemsAsBytes(inventory.contents))
        out["name"] = name

        return out
    }
}