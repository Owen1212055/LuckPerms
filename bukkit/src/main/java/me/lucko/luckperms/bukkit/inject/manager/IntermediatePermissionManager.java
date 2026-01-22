package me.lucko.luckperms.bukkit.inject.manager;

import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.ServerOperator;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/***
 * This permission manager is responsible for creating permissible that delegate to another permissible that will
 * eventually be bound during plugin enabling.
 */
public class IntermediatePermissionManager extends WrappedPermissionManager {

    private final List<PermissibleRecord> collectedPermissibles = new ArrayList<>();
    private final WrappedPermissible commandblockPermissible = new WrappedPermissible();

    public IntermediatePermissionManager() {
        // We want to throw away everything else
        setPermissionManager(DummyPermissionManager.INSTANCE);
    }

    @Override
    public Permissible createPermissible(@NotNull ServerOperator operator) {
        WrappedPermissible wrappedPermissible = new WrappedPermissible();
        this.collectedPermissibles.add(new PermissibleRecord(operator, wrappedPermissible));

        return wrappedPermissible;
    }

    @Override
    public Permissible createCommandBlockPermissible() {
        return this.commandblockPermissible;
    }

    @Override
    public CompletableFuture<Optional<Permissible>> loadPlayerPermissible(@NotNull UUID playerUuid) {
        // Cant load permissions during plugin loading
        return CompletableFuture.completedFuture(Optional.empty());
    }

    public WrappedPermissible getCommandblockPermissible() {
        return commandblockPermissible;
    }

    public List<PermissibleRecord> getCollectedPermissibles() {
        return collectedPermissibles;
    }

    public record PermissibleRecord(ServerOperator operator, WrappedPermissible wrapper) {

    }
}
