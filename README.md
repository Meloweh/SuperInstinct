# "Super Instinct" (2021-2022)

- A fork of [Altoclef](https://github.com/gaucho-matrero/altoclef)

- This project is about a modification for the game [Minecraft](https://www.minecraft.net/)

## Description

The project "Super Instinct" was developed by my partner and me over a timespan of about 12 months in our spare time.

It was developed as a kind of add-on for a Minecraft modification called "Altoclef" as a supplement.
Altoclef is an automation tool with the end goal of fully automating the game.

Our goal was to develop a completely accurate arrow tracking algorithm to provide predictions about future collisions (Meaning the arrow as a projectile of a bow).
The player should get an early warning system in his environment and by that introducing effective projectile avoidance to the automated movement system of baritone.

We did reverse engineering to derive formulas from the original code that would give double precision readings, then proved each formula and idea mathematically and kept records of it.

We worked out numerous ideas and solutions to problems, taking into account every conceivable factor and complexity that might arise during interaction with the video game itself.
It required a steady stream of creative problem solving and a great deal of perseverance to avoid getting stuck in dead ends.
Whether the project would succeed was unclear for the longest time due to its nature.

We heavily optimized our formulas and code system because it was important to us to find a clean and scalable solution. It was also necessary because it was not foreseeable how well our add-on would perform on a conventional computer.

The project is very large and includes about 60 Java class files but also numerous notes, sketches, concepts and proofs that need some preparation before we release them too.

## Bugs
Y-Tracing has a buggy case distinction somewhere that we did not fix and that results in a crash.
We did not fix it because we concluded the project prior to it.

## Conclusion
- The project was completed at the end of 2022.
During our efforts we found that there is a loss of positional and velocity accuracy of projectiles on client side, since they are being synchronized with a compressed short (16-bit) value by the server.
The original vector is represented by double precision (64-bit) what means that with each execution cycle of the game there is an accumulation of imprecision.
If we could get ahold of one double precision value at some point while a projectile is airborn on client side, we would be able to precicely track the server side arrow on the client.
- Execution time depends on your pc hardware. On a ten year old computer we got execution times around 0.5ms up to 15ms but 3-10 ms most of the time. We also the algorithm non-blocking for that reason.

## Early test measurements in the game (videos)

[![IMAGE ALT TEXT](http://img.youtube.com/vi/amOkU9PKZEo/0.jpg)](http://www.youtube.com/watch?v=amOkU9PKZEo "Flexibility Arrow Tracing Test")
[![IMAGE ALT TEXT](http://img.youtube.com/vi/Rnh-gSojYYI/0.jpg)](http://www.youtube.com/watch?v=Rnh-gSojYYI "Y-Axis Arrow Tracing Test")

## Primitive evasion strategies as proof of concept (videos).

[![IMAGE ALT TEXT](http://img.youtube.com/vi/kbSDZqueH8c/0.jpg)](http://www.youtube.com/watch?v=kbSDZqueH8c "Proof of Concept Demo (Dodge by Stopping) #1")
[![IMAGE ALT TEXT](http://img.youtube.com/vi/t7_LrYOVkU4/0.jpg)](http://www.youtube.com/watch?v=t7_LrYOVkU4 "Proof of Concept Demo (Dodge by Stopping) #2")
[![IMAGE ALT TEXT](http://img.youtube.com/vi/xZwwnwIu9Ic/0.jpg)](http://www.youtube.com/watch?v=xZwwnwIu9Ic "Proof of Concept Demo (Dodge by Jumping)")

## Demonstration of the first executable version with independent entities as attackers (videos)

[![IMAGE ALT TEXT](http://img.youtube.com/vi/tdR7Gxb9jjk/0.jpg)](http://www.youtube.com/watch?v=tdR7Gxb9jjk "Skeleton Arrow Dodge Test #1")
[![IMAGE ALT TEXT](http://img.youtube.com/vi/8MFC06wTh4E/0.jpg)](http://www.youtube.com/watch?v=8MFC06wTh4E "Skeleton Arrow Dodge Test #2")

## (Extra)

In the Altoclef project, in addition to "Super Instinct", I also worked on the compatibility between the automatic building function of the "Baritone" mod and the Altoclef mod, where the focus is on the production of resources, but uses the resource acquisition function of Baritone.
However, it was also necessary to significantly improve the overall survivability of the player bot so that it can complete construction even on difficult difficulty settings.
The features I implemented include:
- Representation strategies against monsters (traps, deciding when, what to do).
- Bug fix (Various soft locks fixed)
- Server compatibility (teleportation when constructing the trap according to multiplayer server guidelines)
- Collaboration of the Building feature of Baritone with Altoclef

### Video

[![IMAGE ALT TEXT](http://img.youtube.com/vi/xA-V-ruogsk/0.jpg)](http://www.youtube.com/watch?v=xA-V-ruogsk "(1.19.2) Builder task successful for the first time in hard difficulty")

## Languages
- Java
