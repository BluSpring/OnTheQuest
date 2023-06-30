scoreboard objectives add otq_foundShipwrecks dummy
scoreboard players add @s otq_foundShipwrecks 1

# you can do this multiple times
advancement revoke @s only questsmp:quests/workarounds/open_shipwreck_chest

function questsmp:check/shipwreck_chest_increment