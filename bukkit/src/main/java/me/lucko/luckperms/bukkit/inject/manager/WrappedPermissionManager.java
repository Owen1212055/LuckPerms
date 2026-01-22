package me.lucko.luckperms.bukkit.inject.manager;

import io.papermc.paper.plugin.PermissionManager;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.ServerOperator;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

class WrappedPermissionManager implements PermissionManager {

    private PermissionManager permissionManager = DummyPermissionManager.INSTANCE;

    public void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    @Override
    public @Nullable Permission getPermission(String name) {
        return this.permissionManager.getPermission(name);
    }

    @Override
    public void addPermission(Permission perm) {
        this.permissionManager.addPermission(perm);
    }

    @Override
    public void removePermission(Permission perm) {
        this.permissionManager.removePermission(perm);
    }

    @Override
    public void removePermission(String name) {
        this.permissionManager.removePermission(name);
    }

    @Override
    public Set<Permission> getDefaultPermissions(boolean op) {
        return this.permissionManager.getDefaultPermissions(op);
    }

    @Override
    public void recalculatePermissionDefaults(Permission perm) {
        this.permissionManager.recalculatePermissionDefaults(perm);
    }

    @Override
    public void subscribeToPermission(String permission, Permissible permissible) {
        this.permissionManager.subscribeToPermission(permission, permissible);
    }

    @Override
    public void unsubscribeFromPermission(String permission, Permissible permissible) {
        this.permissionManager.unsubscribeFromPermission(permission, permissible);
    }

    @Override
    public Set<Permissible> getPermissionSubscriptions(String permission) {
        return this.permissionManager.getPermissionSubscriptions(permission);
    }

    @Override
    public void subscribeToDefaultPerms(boolean op, Permissible permissible) {
        this.permissionManager.subscribeToDefaultPerms(op, permissible);
    }

    @Override
    public void unsubscribeFromDefaultPerms(boolean op, Permissible permissible) {
        this.permissionManager.unsubscribeFromDefaultPerms(op, permissible);
    }

    @Override
    public Set<Permissible> getDefaultPermSubscriptions(boolean op) {
        return this.permissionManager.getDefaultPermSubscriptions(op);
    }

    @Override
    public Set<Permission> getPermissions() {
        return this.permissionManager.getPermissions();
    }

    @Override
    public void addPermissions(List<Permission> perm) {
        this.permissionManager.addPermissions(perm);
    }

    @Override
    public void clearPermissions() {
        this.permissionManager.clearPermissions();
    }

    @Override
    public Permissible createPermissible(@NotNull ServerOperator operator) {
        return this.permissionManager.createPermissible(operator);
    }

    @Override
    public Permissible createCommandBlockPermissible() {
        return this.permissionManager.createCommandBlockPermissible();
    }

    @Override
    public CompletableFuture<Optional<Permissible>> loadPlayerPermissible(@NotNull UUID playerUuid) {
        return this.permissionManager.loadPlayerPermissible(playerUuid);
    }
}
