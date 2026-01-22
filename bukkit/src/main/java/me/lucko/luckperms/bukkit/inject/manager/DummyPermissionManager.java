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

public enum DummyPermissionManager implements PermissionManager {
    INSTANCE;

    @Override
    public @Nullable Permission getPermission(String name) {
        return null;
    }

    @Override
    public void addPermission(Permission perm) {

    }

    @Override
    public void removePermission(Permission perm) {

    }

    @Override
    public void removePermission(String name) {

    }

    @Override
    public Set<Permission> getDefaultPermissions(boolean op) {
        return Set.of();
    }

    @Override
    public void recalculatePermissionDefaults(Permission perm) {

    }

    @Override
    public void subscribeToPermission(String permission, Permissible permissible) {

    }

    @Override
    public void unsubscribeFromPermission(String permission, Permissible permissible) {

    }

    @Override
    public Set<Permissible> getPermissionSubscriptions(String permission) {
        return Set.of();
    }

    @Override
    public void subscribeToDefaultPerms(boolean op, Permissible permissible) {

    }

    @Override
    public void unsubscribeFromDefaultPerms(boolean op, Permissible permissible) {

    }

    @Override
    public Set<Permissible> getDefaultPermSubscriptions(boolean op) {
        return Set.of();
    }

    @Override
    public Set<Permission> getPermissions() {
        return Set.of();
    }

    @Override
    public void addPermissions(List<Permission> perm) {

    }

    @Override
    public void clearPermissions() {

    }

    @Override
    public Permissible createPermissible(@NotNull ServerOperator operator) {
        throw new IllegalStateException();
    }

    @Override
    public Permissible createCommandBlockPermissible() {
        throw new IllegalStateException();
    }

    @Override
    public CompletableFuture<Optional<Permissible>> loadPlayerPermissible(@NotNull UUID playerUuid) {
        throw new IllegalStateException();
    }
}
