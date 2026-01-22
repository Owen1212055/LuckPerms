/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.lucko.luckperms.bukkit.loader;

import io.papermc.paper.plugin.PermissionManager;
import io.papermc.paper.plugin.PermissionManagerRegistrar;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.lucko.luckperms.common.loader.JarInJarClassLoader;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class BukkitInjectionBootstrap implements PluginBootstrap {

    private static final String JAR_NAME = "luckperms-bukkit.jarinjar";
    private static final String INJECTION = "me.lucko.luckperms.bukkit.inject.manager.LuckPermsPermissionManager";

    private final JarInJarClassLoader classLoader;

    public BukkitInjectionBootstrap() {
        this.classLoader = new JarInJarClassLoader(getClass().getClassLoader(), JAR_NAME);
    }

    @Override
    public JavaPlugin createPlugin(PluginProviderContext context) {
        return new BukkitLoaderPlugin(this.classLoader);
    }

    @Override
    public void bootstrap(BootstrapContext bootstrapContext) {
        PermissionManager permissionManager;
        try {
            Class<?> clazz = this.classLoader.loadClass(INJECTION);

            permissionManager = (PermissionManager) clazz
                    .getDeclaredMethod("setupIntermediate")
                    .invoke(null);
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }


        bootstrapContext.getLifecycleManager().registerEventHandler(LifecycleEvents.PERMISSION_MANAGER_REGISTER, event -> {
            final PermissionManagerRegistrar permissionManagerRegistrar = event.registrar();

            permissionManagerRegistrar.register(permissionManager);
        });
    }

}
