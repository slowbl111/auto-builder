# Schematic Builder Assistant

Client-side Fabric mod scaffold for Minecraft Java 1.21.11. It is intended to
work with a Litematica schematic that the player has already loaded and
positioned.

## Build and install

Use Java 21 and Fabric Loader for Minecraft 1.21.11. Install matching
1.21.11 versions of Litematica and MaLiLib in the same Minecraft instance.
From this folder, run `gradlew.bat remapJar` on Windows. The mod jar is written
to `build/libs/`; copy that jar into the instance's `mods` folder.

## Current state

Press B to open the pastel Build Buddy menu, P to start/pause, and X to stop.
The first builder pass reads the selected Litematica placement and places simple
blocks when they are in reach, supported by a solid block, and present in the
hotbar. It will not break blocks or change them to make room. It currently
supports unrotated, unmirrored placements and does not walk between build areas.
Automation remains visible and user-controlled.

## Planned building flow

1. Add placement transforms and complex block support.
2. Add pathfinding between reachable build areas, with cautious edge movement.
3. Add inventory refill guidance and a preview/material summary.

The Litematica bridge should be tested against the installed 1.21.11 Litematica
and MaLiLib builds, since their internal Java APIs can change between releases.
The project currently uses Fabric's official 1.21.11 template versions and
official Mojang mappings.
