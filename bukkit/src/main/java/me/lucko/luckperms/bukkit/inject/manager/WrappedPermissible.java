package me.lucko.luckperms.bukkit.inject.manager;

import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

class WrappedPermissible implements Permissible {

    private Permissible permissible = DummyPermissible.INSTANCE;

    public void setPermissible(Permissible permissible) {
        this.permissible = permissible;
    }

    @Override
    public boolean isPermissionSet(@NotNull String name) {
        return this.permissible.isPermissionSet(name);
    }

    @Override
    public boolean isPermissionSet(@NotNull Permission perm) {
        return this.permissible.isPermissionSet(perm);
    }

    @Override
    public boolean hasPermission(@NotNull String name) {
        return this.permissible.hasPermission(name);
    }

    @Override
    public boolean hasPermission(@NotNull Permission perm) {
        return this.permissible.hasPermission(perm);
    }

    @Override
    public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value) {
        return this.permissible.addAttachment(plugin, name, value);
    }

    @Override
    public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin) {
        return this.permissible.addAttachment(plugin);

    }

    @Override
    public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value, int ticks) {
        return this.permissible.addAttachment(plugin, name, value, ticks);
    }

    @Override
    public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, int ticks) {
        return this.permissible.addAttachment(plugin, ticks);
    }

    @Override
    public void removeAttachment(@NotNull PermissionAttachment attachment) {
        this.permissible.removeAttachment(attachment);
    }

    @Override
    public void recalculatePermissions() {
        this.permissible.recalculatePermissions();
    }

    @Override
    public @NotNull Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return this.permissible.getEffectivePermissions();
    }

    @Override
    public boolean isOp() {
        return this.permissible.isOp();
    }

    @Override
    public void setOp(boolean value) {
        this.permissible.setOp(value);
    }
}
