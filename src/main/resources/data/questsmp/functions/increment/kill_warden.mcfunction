scoreboard objectives add otq_killWarden dummy
scoreboard players add @s otq_killWarden 1

# you can do this multiple times
advancement revoke @s only questsmp:quests/workarounds/kill_warden

function questsmp:check/kill_warden_increment