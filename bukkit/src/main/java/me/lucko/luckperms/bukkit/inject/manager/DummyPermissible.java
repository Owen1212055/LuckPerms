package me.lucko.luckperms.bukkit.inject.manager;

import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collections;
import java.util.Set;

enum DummyPermissible implements Permissible {
    INSTANCE;

    @Override public boolean isOp() { return false; }
    @Override public void setOp(boolean value) {}
    @Override public boolean isPermissionSet(@NonNull String name) { return false; }
    @Override public boolean isPermissionSet(@NonNull Permission perm) { return false; }
    @Override public boolean hasPermission(@NonNull String inName) { return false; }
    @Override public boolean hasPermission(@NonNull Permission perm) { return false; }
    @Override public @NonNull PermissionAttachment addAttachment(@NonNull Plugin plugin, @NonNull String name, boolean value) { return null; }
    @Override public @NonNull PermissionAttachment addAttachment(@NonNull Plugin plugin) { return null; }
    @Override public void removeAttachment(@NonNull PermissionAttachment attachment) {}
    @Override public void recalculatePermissions() {}
    @Override public void clearPermissions() {}
    @Override public PermissionAttachment addAttachment(@NonNull Plugin plugin, @NonNull String name, boolean value, int ticks) { return null; }
    @Override public PermissionAttachment addAttachment(@NonNull Plugin plugin, int ticks) { return null; }
    @Override public @NonNull Set<PermissionAttachmentInfo> getEffectivePermissions() { return Collections.emptySet(); }

}
