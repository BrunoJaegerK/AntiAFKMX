package miroshka.antiafk;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

public class MessageUtils {
    private final Config config;
    private final String prefix;

    public MessageUtils(Config config) {
        this.config = config;
        this.prefix = config.getString("messages.prefix", "&7[&cAntiAFK&7] ");
    }

    public String formatMessage(String key, Player player, Object... params) {
        String message = config.getString("messages." + key, "");
        if (message.isEmpty()) return "";
        message = message.replace("{prefix}", prefix);
        if (player != null) message = message.replace("{player}", player.getName());
        for (int i = 0; i + 1 < params.length; i += 2) {
            if (params[i] instanceof String) {
                message = message.replace("{" + params[i] + "}", String.valueOf(params[i + 1]));
            }
        }
        return TextFormat.colorize(message);
    }

    public String getKickReason() {
        return TextFormat.colorize(config.getString(
                "messages.kick-reason",
                "Has sido expulsado por permanecer inactivo demasiado tiempo."
        ));
    }
}
