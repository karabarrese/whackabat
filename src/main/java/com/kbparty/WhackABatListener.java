package com.kbparty;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.*;
import org.bukkit.util.Vector;

import java.util.*;

public class WhackABatListener implements Listener {

    private final WhackABat plugin;
    private final Map<UUID, Integer> scores = new HashMap<>();
    private final Set<UUID> stunned = new HashSet<>();
    private final Set<UUID> trackedBats = new HashSet<>();
    private boolean gameRunning = false;

    private Scoreboard scoreboard;
    private Objective objective;

    public WhackABatListener(WhackABat plugin) {
        this.plugin = plugin;
        setupScoreboard();
    }

    private void setupScoreboard() {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        objective = scoreboard.registerNewObjective(
            "whackabat", Criteria.DUMMY,
            ChatColor.GOLD + "🦇 Whack-a-Bat"
        );
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    private void updateScoreboard() {
        // Clear old scores
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }
        // Re-add all scores
        scores.entrySet().stream()
            .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
            .forEach(entry -> {
                Player p = Bukkit.getPlayer(entry.getKey());
                if (p != null) {
                    objective.getScore(p.getName()).setScore(entry.getValue());
                }
            });
    }

    public void startGame() {
        gameRunning = true;
        scores.clear();
        stunned.clear();
        trackedBats.clear();
        setupScoreboard();

        // Show scoreboard to all players
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setScoreboard(scoreboard);
            // Give them a bat whacker (stick)
            ItemStack bat = new ItemStack(Material.STICK);
            var meta = bat.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + "🦇 Bat Whacker");
            bat.setItemMeta(meta);
            p.getInventory().setItemInMainHand(bat);
        }
        updateScoreboard();
    }

    public void stopGame() {
        gameRunning = false;

        // Kill all tracked bats
        for (UUID id : trackedBats) {
            Entity e = Bukkit.getEntity(id);
            if (e != null) e.remove();
        }
        trackedBats.clear();

        // Reset scoreboards to default
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    public Map<UUID, Integer> getScores() {
        return scores;
    }

    public void resetScores() {
        scores.clear();
        stunned.clear();
    }

    public boolean isGameRunning() {
        return gameRunning;
    }

    // Track bats we spawn so we can clean them up
    public void trackBat(Bat bat) {
        // One hit kill
        Objects.requireNonNull(bat.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(1.0);
        bat.setHealth(1.0);
        trackedBats.add(bat.getUniqueId());
    }

    public int getBatCount() {
        return trackedBats.size();
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (!(e.getEntity() instanceof Bat bat)) return;
        if (!gameRunning) return;
        if (!trackedBats.contains(bat.getUniqueId())) return;

        trackedBats.remove(bat.getUniqueId());
        e.setDroppedExp(0);
        e.getDrops().clear();

        Player killer = bat.getKiller();
        if (killer == null) return;

        ItemStack hand = killer.getInventory().getItemInMainHand();
        if (hand.getType() != Material.STICK) return;

        // Add point
        UUID id = killer.getUniqueId();
        scores.put(id, scores.getOrDefault(id, 0) + 1);
        killer.sendMessage(ChatColor.GREEN + "🦇 +1! Score: " + scores.get(id));
        killer.playSound(killer.getLocation(), Sound.ENTITY_BAT_DEATH, 1f, 1.5f);

        // Update live scoreboard
        updateScoreboard();

        // Duplicate! Spawn 2 bats exactly where this one died
        if (trackedBats.size() < 40) {
            for (int i = 0; i < 2; i++) {
                Location spawnLoc = bat.getLocation().clone();
                // nudge until we find an air block
                for (int attempt = 0; attempt < 10; attempt++) {
                    spawnLoc = bat.getLocation().clone().add(
                        (Math.random() - 0.5) * 1.5,
                        (Math.random() - 0.5) * 1.5,
                        (Math.random() - 0.5) * 1.5
                    );
                    if (spawnLoc.getBlock().getType() == Material.AIR) break;
                }
                Bat newBat = (Bat) bat.getWorld().spawnEntity(spawnLoc, EntityType.BAT);
                trackBat(newBat);
            }
        }
    }

    @EventHandler
    public void onPlayerHitPlayer(EntityDamageByEntityEvent e) {
        if (!gameRunning) return;
        if (!(e.getDamager() instanceof Player attacker)) return;
        if (!(e.getEntity() instanceof Player victim)) return;

        ItemStack hand = attacker.getInventory().getItemInMainHand();
        if (hand.getType() != Material.STICK) return;

        e.setCancelled(true);
        if (stunned.contains(victim.getUniqueId())) return;

        // Launch them!
        Vector knockback = attacker.getLocation().getDirection().multiply(2).setY(0.8);
        victim.setVelocity(knockback);
        victim.sendMessage(ChatColor.RED + "💫 Stunned by " + attacker.getName() + " for 3 seconds!");
        attacker.sendMessage(ChatColor.YELLOW + "💥 You whacked " + victim.getName() + "!");
        attacker.playSound(attacker.getLocation(), Sound.ENTITY_BAT_HURT, 1f, 0.5f);

        stunned.add(victim.getUniqueId());
        victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 5));

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            stunned.remove(victim.getUniqueId());
            victim.sendMessage(ChatColor.GREEN + "You're no longer stunned!");
        }, 60L);
    }

    @EventHandler
    public void onBlockBreak(org.bukkit.event.block.BlockBreakEvent e) {
        if (!gameRunning) return;
        //if (e.getPlayer().isOp()) return; // admins can still break blocks
        e.setCancelled(true);
    }
}