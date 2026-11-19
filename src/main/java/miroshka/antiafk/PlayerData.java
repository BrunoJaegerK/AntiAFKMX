package miroshka.antiafk;

import cn.nukkit.Player;
import cn.nukkit.level.Level;

public class PlayerData {
    private long lastActivity;
    private boolean isAfk;
    private boolean isWarned;
    private double lastX;
    private double lastY;
    private double lastZ;
    private String lastLevelName;

    public PlayerData(Player player) {
        lastActivity = System.currentTimeMillis();
        capturePosition(player);
    }

    public void updateActivity() {
        lastActivity = System.currentTimeMillis();
        isAfk = false;
        isWarned = false;
    }

    public void capturePosition(Player player) {
        lastX = player.getX();
        lastY = player.getY();
        lastZ = player.getZ();
        Level level = player.getLevel();
        lastLevelName = level == null ? "" : level.getName();
    }

    public boolean hasMovedOrChangedWorld(Player player) {
        Level level = player.getLevel();
        String currentLevel = level == null ? "" : level.getName();
        if (!currentLevel.equals(lastLevelName)) {
            return true;
        }
        double dx = player.getX() - lastX;
        double dy = player.getY() - lastY;
        double dz = player.getZ() - lastZ;
        return dx * dx + dy * dy + dz * dz > 0.01D;
    }

    public boolean isAfk() { return isAfk; }
    public void setAfk(boolean afk) { isAfk = afk; }
    public boolean isWarned() { return isWarned; }
    public void setWarned(boolean warned) { isWarned = warned; }
    public long getInactiveTime() { return System.currentTimeMillis() - lastActivity; }
}
