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

import me.prdis.hardcore.plugin.HardcorePlugin
import org.bukkit.NamespacedKey
import org.bukkit.advancement.Advancement
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper
import org.incendo.cloud.paper.util.sender.Source

/**
 * @author Medlar Wreak
 */

object HardcoreConsts {
    val plugin = HardcorePlugin.instance
    val server = plugin.server

    private val executionCoordinator = ExecutionCoordinator.simpleCoordinator<Source>()
    val commandManager = PaperCommandManager.builder(PaperSimpleSenderMapper.simpleSenderMapper())
        .executionCoordinator(executionCoordinator)
        .buildOnEnable(plugin)


    val Advancement.isRecipe
        get() = this.key().value().startsWith("recipes")

    val Advancement.isRootItself
        get() = this.root == this

    val HARDCORE_NPC_KEY = NamespacedKey(plugin, "hardcore_npc")
}