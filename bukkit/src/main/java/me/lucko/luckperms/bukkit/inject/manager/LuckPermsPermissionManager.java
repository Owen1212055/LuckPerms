package me.lucko.luckperms.bukkit.inject.manager;

import com.google.common.base.Preconditions;
import io.papermc.paper.plugin.PermissionManager;
import me.lucko.luckperms.bukkit.LPBukkitPlugin;

/**
 * This is responsible for dynamically binding the permision manager being used on the server.
 */
public class LuckPermsPermissionManager {

    private static final WrappedPermissionManager INSTANCE = new WrappedPermissionManager();
    public static Stage stage = Stage.PAPER_PLUGIN_BOOTSTRAP;

    public static PermissionManager setupIntermediate() {
        Preconditions.checkArgument(stage == Stage.PAPER_PLUGIN_BOOTSTRAP, "Should only setup intermediate right before bootstrap!");
        INSTANCE.setPermissionManager(new IntermediatePermissionManager());
        stage = Stage.LUCKPERM_PLUGIN_BOOTSTRAP;

        return INSTANCE;
    }

    public static void injectLuckpermsBootstrap(LPBukkitPlugin bukkitPlugin) {
        Preconditions.checkArgument(stage == Stage.LUCKPERM_PLUGIN_BOOTSTRAP, "Should only inject after plugin bootstrapping!");
        stage = Stage.BIND;

        IntermediatePermissionManager intermediatePermissionManager = (IntermediatePermissionManager) INSTANCE.getPermissionManager();

        INSTANCE.setPermissionManager(RuntimeLuckPermissionManager.from(bukkitPlugin, intermediatePermissionManager));
    }

    public static void uninject() {
        Preconditions.checkArgument(stage == Stage.BIND, "Should already be binded!");
        stage = Stage.UNBIND;
    }

    enum Stage {
        PAPER_PLUGIN_BOOTSTRAP,
        LUCKPERM_PLUGIN_BOOTSTRAP,
        BIND,
        UNBIND
    }
}
