package com.kbparty;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class WhackABatCommand implements CommandExecutor {

    private final WhackABat plugin;
    private final WhackABatListener listener;

    public WhackABatCommand(WhackABat plugin, WhackABatListener listener) {
        this.plugin = plugin;
        this.listener = listener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /whackabat start | stop | score");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start" -> startGame();
            case "stop" -> endGame();
            case "score" -> showScores(sender);
            default -> sender.sendMessage(ChatColor.RED + "Unknown subcommand!");
        }
        return true;
    }

    private void startGame() {
        if (listener.isGameRunning()) {
            Bukkit.broadcastMessage(ChatColor.RED + "A game is already running! Use /whackabat stop first.");
            return;
        }

        listener.startGame();
        Bukkit.broadcastMessage(ChatColor.GOLD + "🦇 Whack-a-Bat begins! You have 60 seconds! Bats multiply when killed!");

        // Spawn initial bats near each player
        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            if (!listener.isGameRunning()) {
                task.cancel();
                return;
            }
            // Only spawn from scratch if low on bats
            if (listener.getBatCount() < 5) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    for (int i = 0; i < 2; i++) {
                        // var loc = p.getLocation().add(
                        //     (Math.random() - 0.5) * 8, 0.5,
                        //     (Math.random() - 0.5) * 8
                        // );
                        var loc = p.getEyeLocation().clone();
                        var bat = (org.bukkit.entity.Bat) p.getWorld()
                            .spawnEntity(loc, org.bukkit.entity.EntityType.BAT);
                        listener.trackBat(bat);
                    }
                }
            }
        }, 0L, 80L);

        // End after 60 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> endGame(), 1200L);
    }

    private void endGame() {
        if (!listener.isGameRunning()) return;
        listener.stopGame();

        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "🦇 Whack-a-Bat is over! Final Scores:");
        showScores(Bukkit.getConsoleSender());

        // Broadcast scores to all players too
        Map<UUID, Integer> scores = listener.getScores();
        if (scores.isEmpty()) {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "No one scored!");
            return;
        }

        int[] place = {1};
        scores.entrySet().stream()
            .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
            .forEach(entry -> {
                Player p = Bukkit.getPlayer(entry.getKey());
                String name = p != null ? p.getName() : "Unknown";
                String prefix = place[0] == 1 ? ChatColor.GOLD + "🥇 " :
                                place[0] == 2 ? ChatColor.GRAY + "🥈 " :
                                place[0] == 3 ? ChatColor.DARK_RED + "🥉 " : ChatColor.WHITE + "" + place[0] + ". ";
                Bukkit.broadcastMessage(prefix + name + ChatColor.WHITE + ": "
                    + ChatColor.YELLOW + entry.getValue() + " pts");
                place[0]++;
            });
    }

    private void showScores(CommandSender sender) {
        Map<UUID, Integer> scores = listener.getScores();
        if (scores.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No scores yet!");
            return;
        }
        scores.entrySet().stream()
            .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
            .forEach(entry -> {
                Player p = Bukkit.getPlayer(entry.getKey());
                String name = p != null ? p.getName() : entry.getKey().toString();
                sender.sendMessage(ChatColor.AQUA + name + ChatColor.WHITE + ": "
                    + ChatColor.YELLOW + entry.getValue() + " pts");
            });
    }
}