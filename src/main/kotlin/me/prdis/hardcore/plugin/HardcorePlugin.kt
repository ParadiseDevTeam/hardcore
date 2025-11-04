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

package me.prdis.hardcore.plugin

import me.prdis.hardcore.plugin.commands.HardcoreCommand
import me.prdis.hardcore.plugin.config.HardcoreCorpseData
import me.prdis.hardcore.plugin.objects.HardcoreConsts.plugin
import me.prdis.hardcore.plugin.objects.HardcoreImpl
import net.kyori.adventure.text.Component.text
import org.bukkit.configuration.serialization.ConfigurationSerialization.registerClass
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author Medlar Wreak
 */

class HardcorePlugin : JavaPlugin() {

    companion object {
        lateinit var instance: HardcorePlugin
            private set
    }

    override fun onEnable() {
        instance = this

        registerClass(HardcoreCorpseData::class.java)

        HardcoreImpl.isRunning = plugin.config.getBoolean("isRunning")

        if (HardcoreImpl.isRunning) {
            HardcoreImpl.usableUnbans = plugin.config.getInt("usableUnbans")

            val configCorpses = config.getList("corpses")?.mapNotNull { it as? HardcoreCorpseData } ?: run {
                if (HardcoreImpl.isRunning) {
                    componentLogger.warn(text("Configuration corpses cannot be found"))
                }
                emptyList()
            }

            HardcoreImpl.corpses.addAll(configCorpses)

            val configCompletedAdvcs = config.getStringList("completedRootAdvancements")
            HardcoreImpl.completedRootAdvancements.addAll(configCompletedAdvcs)

            componentLogger.warn(text("게임이 현재 진행 중인 상황입니다. 종료를 원하시는 게 아닌 경우 \"/hardcore\" 명령어를 다시 입력하실 필요가 없습니다."))
            HardcoreImpl.start(true)
        }

        HardcoreCommand.registerCommands()
    }
}