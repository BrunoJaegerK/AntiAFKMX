package miroshka.antiafk;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.NukkitRunnable;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

public class AntiAFK extends PluginBase implements Listener {
    private Map<String, PlayerData> playerDataMap;
    private Config config;
    private MessageUtils messageUtils;
    private int afkTimeMinutes;
    private int kickTimeMinutes;
    private int warningTimeMinutes;
    private boolean broadcastAfk;
    private List<String> exemptPlayers;
    private List<String> disabledWorlds;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.config = getConfig();
        this.playerDataMap = new HashMap<>();
        this.messageUtils = new MessageUtils(config);
        loadConfig();
        getServer().getPluginManager().registerEvents(this, this);
        startAfkCheckTask();

        getLogger().info(TextFormat.GREEN + "AntiAFK v" + getDescription().getVersion() + " activado correctamente.");
        getLogger().info(TextFormat.AQUA + "-----------------------------------");
        getLogger().info(TextFormat.YELLOW + "Detección multimundo habilitada.");
        getLogger().info(TextFormat.AQUA + "-----------------------------------");
    }

    private void loadConfig() {
        afkTimeMinutes = Math.max(1, config.getInt("afk-time", 5));
        kickTimeMinutes = Math.max(1, config.getInt("kick-time", 10));
        warningTimeMinutes = Math.max(0, config.getInt("warning-time", 2));
        broadcastAfk = config.getBoolean("broadcast-afk", true);
        exemptPlayers = config.getStringList("exempt-players");
        disabledWorlds = config.getStringList("disabled-worlds");
    }

    private void startAfkCheckTask() {
        // Muestrea posición y mundo cada segundo; no depende únicamente de PlayerMoveEvent.
        new NukkitRunnable() {
            @Override
            public void run() {
                samplePlayerActivity();
                checkPlayersAfkStatus();
            }
        }.runTaskTimer(this, 20, 20);
    }

    private void samplePlayerActivity() {
        for (Player player : getServer().getOnlinePlayers().values()) {
            String worldName = player.getLevel() == null ? "" : player.getLevel().getName();
            if (disabledWorlds.contains(worldName)) {
                PlayerData ignoredData = playerDataMap.get(player.getName());
                if (ignoredData != null) {
                    ignoredData.updateActivity();
                    ignoredData.capturePosition(player);
                }
                continue;
            }

            PlayerData data = playerDataMap.get(player.getName());
            if (data == null) {
                playerDataMap.put(player.getName(), new PlayerData(player));
            } else if (data.hasMovedOrChangedWorld(player)) {
                updatePlayerActivity(player);
            }
        }
    }

    private void checkPlayersAfkStatus() {
        for (Player player : getServer().getOnlinePlayers().values()) {
            String worldName = player.getLevel() == null ? "" : player.getLevel().getName();
            if (disabledWorlds.contains(worldName) || isPlayerExempt(player)) {
                continue;
            }

            String playerName = player.getName();
            PlayerData data = playerDataMap.get(playerName);
            if (data == null) {
                playerDataMap.put(playerName, new PlayerData(player));
                continue;
            }

            long inactiveMinutes = data.getInactiveTime() / 60000L;
            if (inactiveMinutes >= kickTimeMinutes) {
                player.kick(messageUtils.getKickReason());
                playerDataMap.remove(playerName);
                continue;
            }

            int warningThreshold = Math.max(0, kickTimeMinutes - warningTimeMinutes);
            if (inactiveMinutes >= warningThreshold && !data.isWarned()) {
                data.setWarned(true);
                player.sendMessage(messageUtils.formatMessage("warning", player, "time", warningTimeMinutes));
                continue;
            }

            if (inactiveMinutes >= afkTimeMinutes && !data.isAfk()) {
                data.setAfk(true);
                if (broadcastAfk) {
                    getServer().broadcastMessage(messageUtils.formatMessage("afk", player));
                }
            }
        }
    }

    private boolean isPlayerExempt(Player player) {
        return player.hasPermission("antiafk.bypass")
                || exemptPlayers.contains(player.getName().toLowerCase());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        playerDataMap.put(event.getPlayer().getName(), new PlayerData(event.getPlayer()));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerDataMap.remove(event.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        updatePlayerActivity(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        updatePlayerActivity(event.getPlayer());
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        updatePlayerActivity(event.getPlayer());
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        updatePlayerActivity(event.getPlayer());
    }

    private void updatePlayerActivity(Player player) {
        PlayerData data = playerDataMap.get(player.getName());
        if (data == null) {
            playerDataMap.put(player.getName(), new PlayerData(player));
            return;
        }
        if (data.isAfk() && broadcastAfk) {
            getServer().broadcastMessage(messageUtils.formatMessage("no-longer-afk", player));
        }
        data.updateActivity();
        data.capturePosition(player);
    }
}
