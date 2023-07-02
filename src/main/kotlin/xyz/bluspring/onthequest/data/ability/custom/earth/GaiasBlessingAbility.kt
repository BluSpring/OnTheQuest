package xyz.bluspring.onthequest.data.ability.custom.earth

import com.google.gson.JsonObject
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import xyz.bluspring.onthequest.data.ability.Ability
import xyz.bluspring.onthequest.data.jewel.Jewel
import xyz.bluspring.onthequest.data.particle.ParticleSpawnTypes
import xyz.bluspring.onthequest.data.particle.types.CircleParticleSpawn
import xyz.bluspring.onthequest.data.util.KeybindType
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

object GaiasBlessingAbility {
    private val markedPlayers = ConcurrentHashMap<Player, ConcurrentLinkedQueue<Player>>()

    class ShowMarked : Ability(0L) {
        init {
            keybindType = KeybindType.SHIFT
        }
        
        override fun <T : Event> canTriggerForEvent(player: Player, event: T): Boolean {
            return event is PlayerToggleSneakEvent
        }

        override fun <T : Event> triggerForEvent(player: Player, event: T): Boolean {
            if (event !is PlayerToggleSneakEvent)
                return false

            if (!markedPlayers.contains(player))
                return true

            val nmsPlayer = (player as CraftPlayer).handle
            markedPlayers[player]!!.forEach {
                if (event.isSneaking)
                    nmsPlayer.connection.send(ClientboundUpdateMobEffectPacket(it.entityId, MobEffectInstance(MobEffects.GLOWING, 300)))
                else
                    nmsPlayer.connection.send(ClientboundRemoveMobEffectPacket(it.entityId, MobEffects.GLOWING))
            }

            return true
        }
    }

    class ToggleMark : Ability(0L) {
        init {
            keybindType = KeybindType.SECONDARY_ABILITY
        }

        override fun <T : Event> canTriggerForEvent(player: Player, event: T): Boolean {
            return event is PlayerInteractEntityEvent
        }

        override fun <T : Event> triggerForEvent(player: Player, event: T): Boolean {
            if (event !is PlayerInteractEntityEvent)
                return false

            val heldItem = player.inventory.itemInMainHand
            if (!heldItem.hasItemMeta())
                return false

            if (!heldItem.itemMeta.persistentDataContainer.has(Jewel.JEWEL_TYPE_KEY))
                return false

            if (event.rightClicked !is Player)
                return false

            if (!player.isSneaking)
                return false

            if (!markedPlayers.contains(player))
                markedPlayers[player] = ConcurrentLinkedQueue()

            val nmsPlayer = (player as CraftPlayer).handle
            val markedPlayersForPlayer = markedPlayers[player]!!

            if (markedPlayersForPlayer.contains(event.rightClicked)) {
                nmsPlayer.connection.send(ClientboundRemoveMobEffectPacket(event.rightClicked.entityId, MobEffects.GLOWING))
                markedPlayersForPlayer.remove(event.rightClicked)
            } else {
                nmsPlayer.connection.send(ClientboundUpdateMobEffectPacket(event.rightClicked.entityId, MobEffectInstance(MobEffects.GLOWING, 300)))
                markedPlayersForPlayer.add(event.rightClicked as Player)
            }

            return true
        }
    }

    class HealArea : Ability(6000L) {
        init {
            keybindType = KeybindType.PRIMARY_ABILITY

            particles.add(
                AbilityParticles(
                    ParticleSpawnTypes.CIRCLE,
                    CircleParticleSpawn.CircleSpawnData(JsonObject().apply {
                        this.addProperty("radius", 5.25f)
                        this.addProperty("particle_type", "minecraft:happy_villager")
                        this.addProperty("y", 0.2)
                        this.addProperty("count", 2)
                        this.addProperty("speed", 0.2)
                    })
                )
            )
        }

        override fun trigger(player: Player, location: Location?): Boolean {
            if (!markedPlayers.contains(player))
                markedPlayers[player] = ConcurrentLinkedQueue()

            val players = player.world.getNearbyPlayers(player.location, 5.5)
            players.forEach {
                if (it == player || markedPlayers[player]!!.contains(it)) {
                    it.addPotionEffect(PotionEffect(PotionEffectType.HEAL, 1, 3))
                }
            }

            return true
        }
    }
}