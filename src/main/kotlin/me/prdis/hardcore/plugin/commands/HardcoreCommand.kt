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

package me.prdis.hardcore.plugin.commands

import io.papermc.paper.ban.BanListType
import me.prdis.hardcore.plugin.objects.HardcoreConsts.commandManager
import me.prdis.hardcore.plugin.objects.HardcoreConsts.plugin
import me.prdis.hardcore.plugin.objects.HardcoreConsts.server
import me.prdis.hardcore.plugin.objects.HardcoreImpl
import me.prdis.hardcore.plugin.objects.HardcoreImpl.start
import me.prdis.hardcore.plugin.objects.HardcoreImpl.stop
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.OfflinePlayer
import org.incendo.cloud.bukkit.parser.OfflinePlayerParser
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.permission.PredicatePermission
import org.incendo.cloud.suggestion.Suggestion
import java.util.concurrent.CompletableFuture

/**
 * @author Medlar Wreak
 */

object HardcoreCommand {
    fun registerCommands() {
        registerHardcore()
        registerUnban()
    }

    private fun registerHardcore() {
        val hardcore = commandManager.commandBuilder("hardcore", { "A hardcore command." })
            .permission(PredicatePermission.of { ctx -> ctx.source().isOp })

        commandManager.command(hardcore.handler { _ ->
            if (!HardcoreImpl.isRunning) {
                HardcoreImpl.isRunning = true
                server.broadcast(text("게임 시작"))
                start()
            } else {
                HardcoreImpl.isRunning = false
                server.broadcast(text("게임 종료"))
                stop()
            }
        })
    }

    private fun registerUnban() {
        val unban = commandManager.commandBuilder("unban", { "An unban command." })
            .permission(PredicatePermission.of { ctx -> ctx.source().isOp })

        commandManager.command(unban.handler { ctx ->
            ctx.sender().source().sendMessage(text("사용 가능한 차단 해제 횟수: ${HardcoreImpl.usableUnbans}"))
        })

        commandManager.command(unban.required("target", OfflinePlayerParser.offlinePlayerComponent<Source?>().apply {
            this.suggestionProvider { _, _ ->
                CompletableFuture.completedFuture(
                    server.offlinePlayers.toSet().filter { it.isBanned }.mapNotNull { it.name }
                        .map { Suggestion.suggestion(it) }
                )
            }
        }).handler { ctx ->
            val target = ctx.get<OfflinePlayer>("target")

            if (HardcoreImpl.usableUnbans > 0) {
                server.getBanList(BanListType.PROFILE).pardon(target.playerProfile)
                --HardcoreImpl.usableUnbans
                plugin.config.set("usableUnbans", HardcoreImpl.usableUnbans)
                plugin.saveConfig()

                ctx.sender().source().sendMessage(text("${target.name}님의 차단을 해제하였습니다."))
                ctx.sender().source().sendMessage(text("남아 있는 차단 해제 횟수: ${HardcoreImpl.usableUnbans}"))
            } else {
                ctx.sender().source().sendMessage(text("사용 가능한 차단 해제 횟수가 없습니다.", NamedTextColor.RED))
            }
        })
    }
}