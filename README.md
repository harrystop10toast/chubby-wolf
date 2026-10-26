# Chubby Dog

Each time a wolf is successfully offered cooked beef, it has a 5% chance to become chubby and turn into the distinct `chubby-dog:chubby_dog` entity. Chubby dogs remain tameable with bones, including after becoming chubby. They have three times a normal wolf's maximum health, 75% of its movement speed, and 1.2 times its attack damage. Wolf skin textures are selected by the wolf's species; if a matching texture is missing, Minecraft's original texture is used. Chubby dogs are not vanilla wolves, so commands such as `/kill @e[type=wolf]` do not target them.

Sneak-right-click your tamed chubby dog to carry it overhead with both arms. Hold **V** to aim, then release to throw it; holding V longer increases the throw distance. A dotted white arc previews the dog's approximate flight and landing point.

When a thrown chubby dog hits the ground or a wall, it explodes like TNT, damaging nearby entities and destroying blocks. The dog itself survives the explosion and is immune to fall damage, including falls into its crater. The generated `config/chubby-dog.properties` file lets you edit `explosion_power` (default `4.0`, allowed `0.1`-`100.0`) and `destroy_blocks` (default `true`). Restart the game after changing the file.

## Pickup and throw debugging

Debug messages are written to the game log (`run/logs/latest.log` when using the development run, or `.minecraft/logs/latest.log` in a normal installation). To diagnose pickup, crouch and right-click the chubby dog once, then search for `[ChubbyDog Debug]`:

- `mobInteract reached` confirms the interaction mixin ran; check `sneaking=true`, `chubby=true`, and `owned=true`.
- `Client accepted pickup interaction` should appear on the client, followed by the server's `Dog marked as carried`.
- `Client carry state changed: carrying=true` and `Carried dog positioned` on the client confirm the client sees and positions the dog overhead.

To diagnose throwing, hold **V** briefly and release. Expect `V throw charge started`, `V released; throw packet sent`, `Server received throw packet`, and `Throw launched`, in that order. If a step is missing or a warning/error appears, share the matching `[ChubbyDog Debug]` lines.

## Building

Run `./gradlew build` to build the mod.

## Wolf textures

Place chubby texture replacements under `src/main/resources/assets/chubby-dog/textures/entity/wolf/` using the same filenames as the matching vanilla wolf textures, such as `wolf_woods.png` and `wolf_woods_tame.png`. The renderer uses a replacement only when that file exists.

## Testing chubby wolf variants

Use these commands in a world with cheats enabled to summon each breed directly as a chubby dog. The variant also selects the matching chubby texture:

```mcfunction
/summon chubby-dog:chubby_dog ~ ~ ~ {variant:"minecraft:pale"}
/summon chubby-dog:chubby_dog ~ ~ ~ {variant:"minecraft:ashen"}
/summon chubby-dog:chubby_dog ~ ~ ~ {variant:"minecraft:black"}
/summon chubby-dog:chubby_dog ~ ~ ~ {variant:"minecraft:chestnut"}
/summon chubby-dog:chubby_dog ~ ~ ~ {variant:"minecraft:rusty"}
/summon chubby-dog:chubby_dog ~ ~ ~ {variant:"minecraft:snowy"}
/summon chubby-dog:chubby_dog ~ ~ ~ {variant:"minecraft:spotted"}
/summon chubby-dog:chubby_dog ~ ~ ~ {variant:"minecraft:striped"}
/summon chubby-dog:chubby_dog ~ ~ ~ {variant:"minecraft:woods"}
```
