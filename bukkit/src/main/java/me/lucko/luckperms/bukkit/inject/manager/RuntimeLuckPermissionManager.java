package me.lucko.luckperms.bukkit.inject.manager;

import com.google.common.collect.ImmutableSet;
import io.papermc.paper.plugin.PermissionManager;
import me.lucko.luckperms.bukkit.LPBukkitPlugin;
import me.lucko.luckperms.bukkit.inject.permissible.LuckPermsPermissible;
import me.lucko.luckperms.bukkit.inject.permissible.MonitoredPermissibleBase;
import me.lucko.luckperms.bukkit.inject.server.LuckPermsDefaultsMap;
import me.lucko.luckperms.bukkit.inject.server.LuckPermsPermissionMap;
import me.lucko.luckperms.bukkit.inject.server.LuckPermsSubscriptionMap;
import me.lucko.luckperms.bukkit.listeners.BukkitConnectionListener;
import me.lucko.luckperms.bukkit.util.PlayerLocaleUtil;
import me.lucko.luckperms.common.config.ConfigKeys;
import me.lucko.luckperms.common.locale.Message;
import me.lucko.luckperms.common.locale.TranslationManager;
import me.lucko.luckperms.common.model.User;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.permissions.ServerOperator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;

public class RuntimeLuckPermissionManager implements PermissionManager {

    private final ServerOperator DUMMY_OPERATOR = new ServerOperator() {
        @Override
        public boolean isOp() {
            return false;
        }

        @Override
        public void setOp(boolean value) {

        }
    };

    // behaviour copied from the implementation of obc.command.CraftBlockCommandSender
    private final ServerOperator COMMAND_BLOCK_OPERATOR = new ServerOperator() {
        @Override
        public boolean isOp() {
            return true;
        }

        @Override
        public void setOp(boolean value) {
            throw new UnsupportedOperationException("Cannot change operator status of a block");
        }
    };

    private final Permissible commandBlockPermissible = transform(new PermissibleBase(DUMMY_OPERATOR), "internal/commandblock");
    private final Permissible entityPermissible = transform(new PermissibleBase(DUMMY_OPERATOR), "internal/entity");
    private final Permissible consolePermissible = transform(new PermissibleBase(DUMMY_OPERATOR), "internal/console");

    private final LPBukkitPlugin plugin;

    private final LuckPermsPermissionMap permissions;
    private final LuckPermsDefaultsMap defaultPerms;
    private final LuckPermsSubscriptionMap permSubs;
    private final Map<Boolean, Map<Permissible, Boolean>> defSubs = new HashMap<>();

    public static PermissionManager from(LPBukkitPlugin bukkitPlugin, IntermediatePermissionManager intermediatePermissionManager) {
        RuntimeLuckPermissionManager runtimeLuckPermissionManager = new RuntimeLuckPermissionManager(bukkitPlugin);
        // Populate any bound permissibles with our own during runtime
        for (IntermediatePermissionManager.PermissibleRecord records : intermediatePermissionManager.getCollectedPermissibles()) {
            records.wrapper().setPermissible(
                    runtimeLuckPermissionManager.createPermissible(records.operator())
            );
        }
        // Bind our command block
        intermediatePermissionManager.getCommandblockPermissible().setPermissible(
                runtimeLuckPermissionManager.createCommandBlockPermissible()
        );

        return runtimeLuckPermissionManager;
    }

    private RuntimeLuckPermissionManager(LPBukkitPlugin bukkitPlugin) {
        this.plugin = bukkitPlugin;

        this.permissions = new LuckPermsPermissionMap(this.plugin, Map.of());
        this.defaultPerms = new LuckPermsDefaultsMap(this.plugin, Map.of());
        this.permSubs = new LuckPermsSubscriptionMap(this.plugin, Map.of());

        bukkitPlugin.setPermissionMap(this.permissions);
        bukkitPlugin.setDefaultPermissionMap(this.defaultPerms);
        bukkitPlugin.setSubscriptionMap(this.permSubs);
    }

    public Map<String, Permission> permissions() {
        return this.permissions;
    }

    public Map<Boolean, Set<Permission>> defaultPerms() {
        return this.defaultPerms;
    }

    public Map<String, Map<Permissible, Boolean>> permSubs() {
        return this.permSubs;
    }

    public Map<Boolean, Map<Permissible, Boolean>> defSubs() {
        return this.defSubs;
    }

    @Override
    public Permissible createPermissible(@NotNull ServerOperator operator) {
        return switch (operator) {
            case Player player -> createPlayer(player);
            case CommandMinecart entity -> new PermissibleBase(operator);
            case Entity commandSender -> this.entityPermissible;
            case CommandSender sender -> this.consolePermissible;
            default -> throw new UnsupportedOperationException("Trying to create unknown permissible!");
        };
    }


    public Permissible createPlayer(Player player) {
        BukkitConnectionListener listener = this.plugin.getConnectionListener();
        if (this.plugin.getConfiguration().get(ConfigKeys.DEBUG_LOGINS)) {
            this.plugin.getLogger().info("Processing login for " + player.getUniqueId() + " - " + player.getName());
        }

        final User user = this.plugin.getUserManager().getIfLoaded(player.getUniqueId());

        /* User instance is null for whatever reason. Could be that it was unloaded between asyncpre and now. */
        if (user == null) {
            listener.deniedLogin.add(player.getUniqueId());

            if (!listener.getUniqueConnections().contains(player.getUniqueId())) {

                this.plugin.getLogger().warn("User " + player.getUniqueId() + " - " + player.getName() +
                        " doesn't have data pre-loaded, they have never been processed during pre-login in this session." +
                        " - denying login.");

                if (listener.detectedCraftBukkitOfflineMode) {
                    listener.printCraftBukkitOfflineModeError();

                    Component reason = TranslationManager.render(Message.LOADING_STATE_ERROR_CB_OFFLINE_MODE.build(), PlayerLocaleUtil.getLocale(player));
                    player.kick(reason);
                    return DummyPermissible.INSTANCE;
                }

            } else {
                this.plugin.getLogger().warn("User " + player.getUniqueId() + " - " + player.getName() +
                        " doesn't currently have data pre-loaded, but they have been processed before in this session." +
                        " - denying login.");
            }

            Component reason = TranslationManager.render(Message.LOADING_STATE_ERROR.build(), PlayerLocaleUtil.getLocale(player));
            player.kick(reason);
            return DummyPermissible.INSTANCE;
        }

        // User instance is there, now we can inject our custom Permissible into the player.
        // Care should be taken at this stage to ensure that async tasks which manipulate bukkit data check that the player is still online.
        try {
            // Make a new permissible for the user
            LuckPermsPermissible lpPermissible = new LuckPermsPermissible(player, user, this.plugin);

            this.plugin.getContextManager().signalContextUpdate(player);

            return lpPermissible;
        } catch (Throwable t) {
            this.plugin.getLogger().warn("Exception thrown when setting up permissions for " +
                    player.getUniqueId() + " - " + player.getName() + " - denying login.", t);

            Component reason = TranslationManager.render(Message.LOADING_SETUP_ERROR.build(), PlayerLocaleUtil.getLocale(player));
            player.kick(reason);
            return DummyPermissible.INSTANCE;
        }
    }

    @Override
    public Permissible createCommandBlockPermissible() {
        return this.commandBlockPermissible;
    }

    @Override
    public CompletableFuture<Optional<Permissible>> loadPlayerPermissible(@NotNull UUID playerUuid) {
        throw new UnsupportedOperationException(); // TODO
    }


    private PermissibleBase transform(PermissibleBase permBase, String name) {
        Objects.requireNonNull(permBase, "permBase");

        // create a monitored instance which delegates to the previous PermissibleBase
        return new MonitoredPermissibleBase(this.plugin, permBase, name);
    }

    // DEFAULT PAPER IMPL
    @Override
    @Nullable
    public Permission getPermission(@NotNull String name) {
        return this.permissions().get(name.toLowerCase(java.util.Locale.ENGLISH));
    }

    @Override
    public void addPermission(@NotNull Permission perm) {
        this.addPermission(perm, true);
    }

    @Override
    public void addPermissions(@NotNull List<Permission> permissions) {
        for (Permission permission : permissions) {
            this.addPermission(permission, false);
        }
        this.dirtyPermissibles();
    }

    // Allow suppressing permission default calculations
    private void addPermission(@NotNull Permission perm, boolean dirty) {
        String name = perm.getName().toLowerCase(java.util.Locale.ENGLISH);

        if (this.permissions().containsKey(name)) {
            throw new IllegalArgumentException("The permission " + name + " is already defined!");
        }

        this.permissions().put(name, perm);
        this.calculatePermissionDefault(perm, dirty);
    }

    @Override
    @NotNull
    public Set<Permission> getDefaultPermissions(boolean op) {
        return ImmutableSet.copyOf(this.defaultPerms().get(op));
    }


    @Override
    public void removePermission(@NotNull Permission perm) {
        this.removePermission(perm.getName());
    }


    @Override
    public void removePermission(@NotNull String name) {
        this.permissions().remove(name.toLowerCase(java.util.Locale.ENGLISH));
    }

    @Override
    public void recalculatePermissionDefaults(@NotNull Permission perm) {
        // we need a null check here because some plugins for some unknown reason pass null into this?
        if (perm != null && this.permissions().containsKey(perm.getName().toLowerCase(Locale.ROOT))) {
            this.defaultPerms().get(true).remove(perm);
            this.defaultPerms().get(false).remove(perm);

            this.calculatePermissionDefault(perm, true);
        }
    }

    private void calculatePermissionDefault(@NotNull Permission perm, boolean dirty) {
        if ((perm.getDefault() == PermissionDefault.OP) || (perm.getDefault() == PermissionDefault.TRUE)) {
            this.defaultPerms().get(true).add(perm);
            if (dirty) {
                this.dirtyPermissibles(true);
            }
        }
        if ((perm.getDefault() == PermissionDefault.NOT_OP) || (perm.getDefault() == PermissionDefault.TRUE)) {
            this.defaultPerms().get(false).add(perm);
            if (dirty) {
                this.dirtyPermissibles(false);
            }
        }
    }


    @Override
    public void subscribeToPermission(@NotNull String permission, @NotNull Permissible permissible) {
        String name = permission.toLowerCase(java.util.Locale.ENGLISH);
        Map<Permissible, Boolean> map = this.permSubs().computeIfAbsent(name, k -> new WeakHashMap<>());

        map.put(permissible, true);
    }

    @Override
    public void unsubscribeFromPermission(@NotNull String permission, @NotNull Permissible permissible) {
        String name = permission.toLowerCase(java.util.Locale.ENGLISH);
        Map<Permissible, Boolean> map = this.permSubs().get(name);

        if (map != null) {
            map.remove(permissible);

            if (map.isEmpty()) {
                this.permSubs().remove(name);
            }
        }
    }

    @Override
    @NotNull
    public Set<Permissible> getPermissionSubscriptions(@NotNull String permission) {
        String name = permission.toLowerCase(java.util.Locale.ENGLISH);
        Map<Permissible, Boolean> map = this.permSubs().get(name);

        if (map == null) {
            return ImmutableSet.of();
        } else {
            return ImmutableSet.copyOf(map.keySet());
        }
    }

    @Override
    public void subscribeToDefaultPerms(boolean op, @NotNull Permissible permissible) {
        Map<Permissible, Boolean> map = this.defSubs().computeIfAbsent(op, k -> new WeakHashMap<>());

        map.put(permissible, true);
    }

    @Override
    public void unsubscribeFromDefaultPerms(boolean op, @NotNull Permissible permissible) {
        Map<Permissible, Boolean> map = this.defSubs().get(op);

        if (map != null) {
            map.remove(permissible);

            if (map.isEmpty()) {
                this.defSubs().remove(op);
            }
        }
    }

    @Override
    @NotNull
    public Set<Permissible> getDefaultPermSubscriptions(boolean op) {
        Map<Permissible, Boolean> map = this.defSubs().get(op);

        if (map == null) {
            return ImmutableSet.of();
        } else {
            return ImmutableSet.copyOf(map.keySet());
        }
    }

    @Override
    @NotNull
    public Set<Permission> getPermissions() {
        return new HashSet<>(this.permissions().values());
    }

    @Override
    public void clearPermissions() {
        this.permissions().clear();
        this.defaultPerms().get(true).clear();
        this.defaultPerms().get(false).clear();
    }

    void dirtyPermissibles(boolean op) {
        Set<Permissible> permissibles = this.getDefaultPermSubscriptions(op);

        for (Permissible p : permissibles) {
            p.recalculatePermissions();
        }
    }

    void dirtyPermissibles() {
        this.dirtyPermissibles(true);
        this.dirtyPermissibles(false);
    }
}
