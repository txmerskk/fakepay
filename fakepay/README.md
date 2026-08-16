# Fake Pay — Fabric 1.21.10

Client-only cosmetic payment simulator.

## Features
- `/pay <player> <amount>` creates a local-only chat message.
- `O` opens the settings menu.
- Toggle Fake Pay, sound, and the corner indicator.
- Corner indicator shows `FAKE PAY: ON/OFF`.
- The mod is declared client-only and never intentionally sends a payment command or custom payment packet.

## Build
Requires JDK 21 and Gradle/Fabric Loom. Run:

`./gradlew build`

The compiled jar will be in `build/libs/`.

## Important
The fake message is only rendered by your Minecraft client. Other players do not receive it from this mod.
