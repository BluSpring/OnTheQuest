# Ability Format

### Example Ability Format Layout
#### Passive
```json
{
  "type": "questsmp:loot/harvest",
  "data": {
    "apply": ["#questsmp:harvestable"],
    "modifier": {
      "type": "add",
      "min": 0,
      "max": 5
    }
  }
}
```

### Active
```json
{
  "type": "questsmp:effects/add",
  "cooldown": 6000,
  "data": {
    "effects": [
      {
        "id": "minecraft:haste",
        "duration": 3600,
        "amplifier": 1,
        "ambient": true,
        "particles": false
      }
    ]
  },
  "keybind": {
    "key": "key.questsmp.primary_ability"
  }
}
```

### Data types
```kotlin
data class Ability(
    // Must correspond to a valid ability type.
    // This can be referenced from AbilityTypes.kt.
    val type: String,
    
    // Cooldown time in ticks.
    val cooldown: Long?,
    
    // Ability data. This is different for each
    // ability type.
    val data: AbilityData,
    
    // Keybind to press to activate this ability.
    val keybind: Keybind?
)

abstract class AbilityData

data class Keybind(
    // Key type.
    // Valid types:
    // key.questsmp.primary_ability - Right click
    // key.questsmp.secondary_ability - Left click
    // key.questsmp.ternary_ability - Sneak + Right click
    // key.questsmp.quaternary_ability - Sneak + Left click
    val key: String
)
```

`questsmp:effects/add`
```kotlin
data class EffectAddAbilityData(
    // Effects list
    val effects: List<Effect>
)

data class Effect(
    // Must be a valid potion effect ID.
    val id: String,
    
    // The duration of the effect, in ticks.
    val duration_tick: Int,
    
    // The amplifier for the effect.
    // Example: 0 = Speed I.
    val amplifier: Int,
    
    // Should the particles be less visible?
    // Default: false
    val ambient: Boolean?,
    
    // Should there be particles?
    // Default: true
    val particles: Boolean?,
    
    // Should the icon be shown?
    // Default: true
    val showIcon: Boolean?
)
```

`questsmp:effects/clear`
```kotlin
data class EffectClearAbilityData(
    
)
```

# CURRENTLY INCOMPLETE!