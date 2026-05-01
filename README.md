# 🦇 Whack-a-Bat

A custom PaperMC Minecraft plugin built for a multiplayer party server. Players compete to whack as many bats as possible before time runs out! Watch out! bats multiply, and you can stun other players by hitting them with your bat whacker :)

First Minecraft plugin! Written in Java

---

## Features

- **Bat splitting** — every bat you kill spawns two more at the same location, creating escalating chaos as the game goes on
- **Live scoreboard** — real-time sidebar scoreboard shows everyone's score as they play
- **Player stunning** — hit another player with your bat whacker to launch them and stun them for 3 seconds
- **Auto cleanup** — all bats despawn when the game ends, no mess left behind
- **Block protection** — players can't break blocks during a game
- **60 second rounds** — game ends automatically with a podium-style final leaderboard

---

## Requirements

- Java 25+
- PaperMC 1.21.x or newer

---

## Installation

1. Download the latest `whackabat-1.0-SNAPSHOT.jar` from this repo
2. Drop it into your server's `/plugins` folder
3. Restart your server or run `reload confirm` in the server console
4. Look for `WhackABat loaded!` in the server logs to confirm it's running

---

## How to Play

### Starting a game
Run this command in Minecraft chat (requires operator permissions):
```
/whackabat start
```
All online players will receive a **Bat Whacker** (stick) in their main hand and the game begins.

### Scoring
- Hit a **bat** with your Bat Whacker → **+1 point**
- Bats die in one hit and immediately split into 2 new bats at the same location
- Bats cap out at 40 to keep things from getting too unhinged

### Stunning players
- Hit **another player** with your Bat Whacker → launches them and stuns them for 3 seconds
- Stunned players move slowly and can't score
- No actual damage is dealt

### Ending a game
The game ends automatically after 60 seconds, or you can end it early:
```
/whackabat stop
```
Final scores are broadcast to all players with 🥇🥈🥉 medals.

### Check scores mid-game
```
/whackabat score
```

---

## Commands

| Command | Description |
|---|---|
| `/whackabat start` | Start a new game |
| `/whackabat stop` | End the current game early |
| `/whackabat score` | Show current scores |

---

## Building from Source

```bash
git clone https://github.com/karabarrese/whackabat.git
cd whackabat
mvn package
```

The compiled jar will be at `target/whackabat-1.0-SNAPSHOT.jar`.
