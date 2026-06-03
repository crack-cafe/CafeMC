package dev.lizainslie.cafemc.afk

import dev.lizainslie.cafemc.CafeMC
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer

class CafePlaceholderExpansion : PlaceholderExpansion() {
    override fun getIdentifier(): String = "cafemc"

    override fun getAuthor(): String = CafeMC.instance.description.authors.joinToString(", ")

    override fun getVersion(): String = CafeMC.instance.description.version

    override fun persist(): Boolean = true

    override fun onRequest(player: OfflinePlayer?, params: String): String? = when (params.lowercase()) {
        "afk_count", "afk_players", "afk_player_count" -> AfkModule.afkPlayerCount.toString()
        else -> null
    }
}
