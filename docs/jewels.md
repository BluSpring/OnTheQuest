# Jewels Format

### Example Jewel Format Layout
```json
{
  "model": {
    "item": "minecraft:emerald",
    "id": 35
  },
  "abilities": [
    {
      "level": {
        "min": 1
      },
      "passive": [ "questsmp:water/amphibious" ]
    },
    {
      "level": {
        "min": 2
      },
      "active": [
        "questsmp:water/puffer_zone"
      ]
    },
    {
      "level": {
        "min": 3
      },
      "active": [
        "questsmp:water/neptunes_might"
      ]
    }
  ],
  "color": "#2C7B9E",
  "char": "\ue007"
}
```

### Data types

```kotlin
data class Jewel(
    // Item data
    val model: Model,
    
    // Abilities list
    val abilities: List<AbilityLevel>,
    
    // This is a hex code, based on "#ABCDEF".
    val color: String,
    
    // This is what character will be used for
    // rendering the cooldown icon.
    val char: Char
)

data class Model(
    // This must be a valid Minecraft item ID
    val item: String,

    // This will be the starting point for 
    // the CustomModelData tag.
    //
    // For example, if the id is 15,
    // having a level -2 jewel will give a
    // custom model data of 13.
    val id: Int
)

data class IntRangePredicate(
    // Minimum value
    val min: Int?,
    // Maximum value
    val max: Int?
)

data class AbilityLevel(
    // If the player's current jewel level is
    // within this range, this ability level is granted.
    val level: IntRangePredicate,
    
    // Passive abilities are abilities that can be triggered
    // on their corresponding type's events.
    // Must lead to a valid ability.
    //
    // For example:
    // If the ability file is in 
    // "data/questsmp/abilities/earth/farmers_glitch.json"
    // then the resource location is
    // "questsmp:earth/farmers_glitch".
    val passive: List<String>?,
    
    // Active abilities are abilities that can only be
    // triggered if their equivalent keybinds are pushed,
    // and/or if their conditions are met.
    // Must lead to a valid ability.
    //
    // For example:
    // If the ability file is in 
    // "data/questsmp/abilities/earth/farmers_glitch.json"
    // then the resource location is
    // "questsmp:earth/farmers_glitch".
    val active: List<String>?
)
```