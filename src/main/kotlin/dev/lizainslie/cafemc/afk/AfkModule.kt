package dev.lizainslie.cafemc.afk

import dev.lizainslie.cafemc.CafeMC
import dev.lizainslie.cafemc.afk.commands.AfkCommand
import dev.lizainslie.cafemc.chat.ChatUtil
import dev.lizainslie.cafemc.chat.nicknameOrDisplayName
import dev.lizainslie.cafemc.chat.sendRichMessage
import dev.lizainslie.cafemc.chat.toPlainText
import dev.lizainslie.cafemc.core.PluginModule
import me.neznamy.tab.api.TabAPI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent

object AfkModule : PluginModule(), Listener {
    private val afkMap = mutableMapOf<Player, Boolean>()
    private val idleMap = mutableMapOf<Player, Int>()
    
    const val IDLE_TIMEOUT = 300 // 5 minutes
    
    init {
        commands += AfkCommand
    }
    
    // region Event Handlers
    
    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        // Reset idle time and remove AFK status when player moves
        if (isAfk(event.player)) toggleAfk(event.player)
        idleMap[event.player] = 0
    }
    
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        // Remove player from idle map
        idleMap.remove(event.player)
        
        // If player is AFK, remove AFK status silently
        if (isAfk(event.player)) toggleAfk(event.player, notify = false, announce = false)
    }
    
    // endregion
    
    

    // region Public API
    
    /**
     * Toggle AFK status for [player]
     */
    fun toggleAfk(player: Player, notify: Boolean = true, announce: Boolean = true) {
        val newAfkStatus = !isAfk(player)

        afkMap[player] = newAfkStatus

        // Ignore player for sleep checks & update tab list
        updateSleepRequirement(player, newAfkStatus)
        updateTabList(player, newAfkStatus)
        
        if (notify) notifyAfk(player, newAfkStatus)
        if (announce) announceAfk(player, newAfkStatus)
    }

    /**
     * Check if [player] is AFK
     */
    fun isAfk(player: Player): Boolean {
        return afkMap[player] ?: false
    }
    
    // endregion
    
    
    
    // region Private API
    
    private fun nowOrNoLonger(newAfkStatus: Boolean) = if (newAfkStatus) "now" else "no longer"
    
    /**
     * Notify [player] of their [newAfkStatus]
     */
    private fun notifyAfk(player: Player, newAfkStatus: Boolean) {
//        player.sendMessage("${ChatColor.GRAY}You are ${if (newAfkStatus) "now" else "no longer"} AFK.")
        player.sendRichMessage { 
            text("You are ${nowOrNoLonger(newAfkStatus)} AFK.") { color = NamedTextColor.GRAY }
        }
    }
    
    /**
     * Broadcast a message to all players of the [newAfkStatus] of [player] 
     */
    private fun announceAfk(player: Player, newAfkStatus: Boolean, playerFilter: (Player) -> Boolean = { it != player }) {
        ChatUtil.broadcast(false, playerFilter) {
            component(player.nicknameOrDisplayName())
            text(" is ") { color = NamedTextColor.GRAY }
            text(nowOrNoLonger(newAfkStatus)) { color = NamedTextColor.GRAY }
            text(" AFK.") { color = NamedTextColor.GRAY }
        }
        
        ChatUtil.broadcastEmbedToDiscord {
            author {
                name = "${player.nicknameOrDisplayName().toPlainText()} is ${nowOrNoLonger(newAfkStatus)} AFK."
                iconUrl = "https://api.mineatar.io/face/${player.uniqueId}"
            }
        }
    }
    
    /**
     * Update the tab list for [player] based on their [newAfkStatus]
     */
    private fun updateTabList(player: Player, newAfkStatus: Boolean) {
        // Update Tab List
        val tab = TabAPI.getInstance()
        val tabPlayer = tab.getPlayer(player.uniqueId) ?: return
        
        val prefix = if (newAfkStatus) Component.text("[AFK]").color(NamedTextColor.GRAY) else null
        
        (tab.tabListFormatManager ?: return).setPrefix(tabPlayer, prefix?.let { LegacyComponentSerializer.legacySection().serialize(prefix) })
    }
    
    /**
     * Update the sleep requirement for [player] based on their [newAfkStatus]
     */
    private fun updateSleepRequirement(player: Player, newAfkStatus: Boolean) {
        player.isSleepingIgnored = newAfkStatus
    }
    
    // endregion
    
    

    // region Module Lifecycle
    
    override fun register(cafeMC: CafeMC) {
        super.register(cafeMC)
        
        // Register idle task
        Bukkit.getServer().scheduler.runTaskTimer(cafeMC, ::checkIdleTask, 20, 20)
    }

    override fun onDisable(cafeMC: CafeMC) {
        super.onDisable(cafeMC)
        
        // Clear all AFK statuses
        
        afkMap.keys.forEach {
            updateSleepRequirement(it, false)
            updateTabList(it, false)
        }
        
        afkMap.clear()
    }
    
    // endregion
    
    
    // region Tasks
    
    private fun checkIdleTask() {
        // Iterate over all online players who are not AFK
        for (player in Bukkit.getOnlinePlayers().filter { !isAfk(it) }) {
            // If player is not in the idle map, add them with a base count of 1
            if (idleMap[player] == null) idleMap[player] = 1
            
            // Otherwise, if player is already in the idle map, increment their idle time
            else idleMap[player] = idleMap[player]!! + 1

            // If player is idle for more than 2 minutes, toggle AFK
            if (idleMap[player]!! >= IDLE_TIMEOUT) // 2 minutes
                toggleAfk(player)
        }
    }
    
    // endregion
}
