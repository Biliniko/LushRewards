package org.lushplugins.lushrewards.data;

import com.google.gson.JsonObject;
import org.lushplugins.lushrewards.LushRewards;
import org.lushplugins.lushrewards.module.UserDataModule;
import org.lushplugins.lushlib.manager.Manager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.lushrewards.storage.StorageManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class DataManager extends Manager {
    private static final long OFFLINE_PLACEHOLDER_CACHE_TTL_MILLIS = TimeUnit.MINUTES.toMillis(2);

    private StorageManager storageManager;
    private final ConcurrentHashMap<UUID, RewardUser> rewardUsersCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, TimedCacheEntry<RewardUser>> offlineRewardUsersCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, TimedCacheEntry<UserDataModule.UserData>> offlineModuleUserDataCache = new ConcurrentHashMap<>();
    private final AtomicLong nextOfflineCacheCleanupAt = new AtomicLong(0L);

    @Override
    public void onEnable() {
        storageManager = new StorageManager();

        Bukkit.getOnlinePlayers().forEach(player -> getOrLoadRewardUser(player.getUniqueId()).thenAccept((rewardUser) -> rewardUser.setUsername(player.getName())));
    }

    @Override
    public void onDisable() {
        if (storageManager != null) {
            saveCachedRewardUsers();
            storageManager.disable();
            storageManager = null;
        }

        offlineRewardUsersCache.clear();
        offlineModuleUserDataCache.clear();
    }

    @Nullable
    public RewardUser getRewardUser(@NotNull Player player) {
        return getRewardUser(player.getUniqueId());
    }

    @Nullable
    public RewardUser getRewardUser(@NotNull OfflinePlayer player) {
        return getRewardUser(player.getUniqueId());
    }

    @Nullable
    public RewardUser getRewardUser(@NotNull UUID uuid) {
        return rewardUsersCache.get(uuid);
    }

    @Nullable
    public RewardUser getOrRequestRewardUser(@NotNull OfflinePlayer player) {
        return getOrRequestRewardUser(player.getUniqueId());
    }

    @Nullable
    public RewardUser getOrRequestRewardUser(@NotNull UUID uuid) {
        long now = System.currentTimeMillis();
        cleanupOfflineCachesIfDue();

        RewardUser rewardUser = getRewardUser(uuid);
        if (rewardUser != null) {
            return rewardUser;
        }

        TimedCacheEntry<RewardUser> offlineEntry = offlineRewardUsersCache.get(uuid);
        if (offlineEntry != null && !offlineEntry.isExpired(now)) {
            return offlineEntry.value();
        }

        RewardUser loadedRewardUser = loadUserDataSync(uuid, null, RewardUser.class);
        if (loadedRewardUser != null) {
            offlineRewardUsersCache.put(uuid, new TimedCacheEntry<>(loadedRewardUser, System.currentTimeMillis() + OFFLINE_PLACEHOLDER_CACHE_TTL_MILLIS));
        }

        return loadedRewardUser;
    }

    public CompletableFuture<RewardUser> getOrLoadRewardUser(UUID uuid) {
        return getOrLoadRewardUser(uuid, true);
    }

    public CompletableFuture<RewardUser> getOrLoadRewardUser(UUID uuid, boolean cacheUser) {
        if (rewardUsersCache.containsKey(uuid)) {
            return CompletableFuture.completedFuture(rewardUsersCache.get(uuid));
        } else {
            return loadRewardUser(uuid, cacheUser);
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public CompletableFuture<RewardUser> loadRewardUser(@NotNull UUID uuid) {
        return loadRewardUser(uuid, true);
    }

    public CompletableFuture<RewardUser> loadRewardUser(@NotNull UUID uuid, boolean cacheUser) {
        CompletableFuture<RewardUser> future = new CompletableFuture<>();

        loadUserData(uuid, null, RewardUser.class).thenAccept(rewardUser -> {
            if (rewardUser != null && cacheUser) {
                rewardUsersCache.put(uuid, rewardUser);
            }

            future.complete(rewardUser);
        });

        return future;
    }

    public void unloadRewardUser(UUID uuid) {
        rewardUsersCache.remove(uuid);
        invalidateOfflineRewardUser(uuid);
    }

    /**
     * Reload all cached RewardUsers
     *
     * @param save Whether cached RewardUsers should be saved before reloading
     */
    public void reloadRewardUsers(boolean save) {
        rewardUsersCache.forEach((uuid, rewardUser) -> {
            if (save) {
                saveRewardUser(rewardUser);
            }

            unloadRewardUser(uuid);
            loadRewardUser(uuid);
        });
    }

    public void saveCachedRewardUsers() {
        rewardUsersCache.values().forEach(this::saveRewardUser);
    }

    @SuppressWarnings("UnusedReturnValue")
    public CompletableFuture<Boolean> saveRewardUser(RewardUser rewardUser) {
        return saveUserData(rewardUser);
    }

    public void saveRewardUser(Player player) {
        RewardUser rewardUser = getRewardUser(player);
        if (rewardUser != null) {
            saveRewardUser(rewardUser);
        }
    }

    public <T extends UserDataModule.UserData> CompletableFuture<T> getOrLoadUserData(UUID uuid, UserDataModule<T> module) {
        return getOrLoadUserData(uuid, module, true);
    }

    public <T extends UserDataModule.UserData> CompletableFuture<T> getOrLoadUserData(UUID uuid, UserDataModule<T> module, boolean cacheUser) {
        T userData = module.getUserData(uuid);
        if (userData != null) {
            return CompletableFuture.completedFuture(userData);
        } else {
            return loadUserData(uuid, module, cacheUser);
        }
    }

    @Nullable
    public <T extends UserDataModule.UserData> T getOrRequestUserData(UUID uuid, UserDataModule<T> module) {
        long now = System.currentTimeMillis();
        cleanupOfflineCachesIfDue();

        T userData = module.getUserData(uuid);
        if (userData != null) {
            return userData;
        }

        String key = module.getId() + ":" + uuid;
        TimedCacheEntry<UserDataModule.UserData> offlineEntry = offlineModuleUserDataCache.get(key);
        if (offlineEntry != null && !offlineEntry.isExpired(now)) {
            return module.getUserDataClass().cast(offlineEntry.value());
        }

        T loadedUserData = loadUserDataSync(uuid, module.getId(), module.getUserDataClass());
        if (loadedUserData != null) {
            offlineModuleUserDataCache.put(key, new TimedCacheEntry<>(loadedUserData, System.currentTimeMillis() + OFFLINE_PLACEHOLDER_CACHE_TTL_MILLIS));
        }

        return loadedUserData;
    }

    public <T extends UserDataModule.UserData> CompletableFuture<T> loadUserData(UUID uuid, UserDataModule<T> module) {
        return loadUserData(uuid, module, true);
    }

    public <T extends UserDataModule.UserData> CompletableFuture<T> loadUserData(UUID uuid, UserDataModule<T> module, boolean cacheUser) {
        CompletableFuture<T> future = new CompletableFuture<>();

        loadUserData(uuid, module.getId(), module.getUserDataClass()).thenAccept(userData -> {
            if (userData != null && cacheUser) {
                module.cacheUserData(uuid, userData);
            }

            future.complete(userData);
        });

        return future;
    }

    public <T extends UserDataModule.UserData> CompletableFuture<T> loadUserData(@NotNull UUID uuid, String moduleId, Class<T> dataClass) {
        CompletableFuture<T> future = new CompletableFuture<>();

        storageManager.loadModuleUserData(uuid, moduleId)
            .orTimeout(15, TimeUnit.SECONDS)
            .whenComplete((json, exception) -> {
                if (exception != null) {
                    LushRewards.getInstance().log(Level.WARNING, "Caught error when parsing data:", exception);
                    future.complete(null);
                    return;
                }

                if (json == null) {
                    LushRewards.getInstance().getLogger().info("No storage data found for '" + uuid + "' for module '" + (moduleId != null ? moduleId : "main") + "', creating default data!");

                    if (moduleId != null) {
                        UserDataModule<?> module = (UserDataModule<?>) LushRewards.getInstance().getModule(moduleId).orElse(null);
                        if (module != null) {
                            T userData = dataClass.cast(module.getDefaultData(uuid));
                            saveUserData(userData).thenAccept((ignored) -> future.complete(userData));
                        } else {
                            future.complete(null);
                        }
                    } else if (dataClass.isAssignableFrom(RewardUser.class)) {
                        T userData = dataClass.cast(new RewardUser(uuid, null, 0));
                        saveUserData(userData).thenAccept((ignored) -> future.complete(userData));
                    } else {
                        future.complete(null);
                    }

                    return;
                }

                try {
                    json.addProperty("uuid", uuid.toString());
                    json.addProperty("moduleId", moduleId);

                    T userData = LushRewards.getInstance().getGson().fromJson(json, dataClass);
                    if (userData == null) {
                        future.complete(null);
                        return;
                    }

                    future.complete(userData);
                } catch (Throwable e) {
                    LushRewards.getInstance().log(Level.WARNING, "Caught error when parsing user data:", e);
                    future.complete(null);
                }
            });

        return future;
    }

    @Nullable
    private <T extends UserDataModule.UserData> T loadUserDataSync(@NotNull UUID uuid, @Nullable String moduleId, Class<T> dataClass) {
        JsonObject json;
        try {
            json = storageManager.loadModuleUserDataSync(uuid, moduleId);
        } catch (Throwable e) {
            LushRewards.getInstance().log(Level.WARNING, "Caught error when loading user data:", e);
            return null;
        }

        if (json == null) {
            if (moduleId != null) {
                UserDataModule<?> module = (UserDataModule<?>) LushRewards.getInstance().getModule(moduleId).orElse(null);
                return module != null ? dataClass.cast(module.getDefaultData(uuid)) : null;
            }

            return dataClass.isAssignableFrom(RewardUser.class) ? dataClass.cast(new RewardUser(uuid, null, 0)) : null;
        }

        try {
            json.addProperty("uuid", uuid.toString());
            json.addProperty("moduleId", moduleId);

            return LushRewards.getInstance().getGson().fromJson(json, dataClass);
        } catch (Throwable e) {
            LushRewards.getInstance().log(Level.WARNING, "Caught error when parsing user data:", e);
            return null;
        }
    }

    public CompletableFuture<Boolean> saveUserData(UserDataModule.UserData userData) {
        invalidateOfflineCache(userData);

        return CompletableFuture.supplyAsync(() -> storageManager.saveModuleUserData(userData))
            .orTimeout(30, TimeUnit.SECONDS)
            .handle((storageData, exception) -> {
                if (exception != null) {
                    LushRewards.getInstance().log(Level.WARNING, "Caught error when saving data:", exception);
                    return false;
                }

                return storageData != null;
            });
    }

    public void loadModulesUserData(UUID uuid) {
        LushRewards.getInstance().getEnabledRewardModules().forEach(module -> {
            if (module instanceof UserDataModule<?> userDataModule) {
                userDataModule.getOrLoadUserData(uuid, true);
            }
        });
    }

    public void unloadModulesUserData(UUID uuid) {
        LushRewards.getInstance().getRewardModules().forEach(module -> {
            if (module instanceof UserDataModule<?> userDataModule) {
                userDataModule.uncacheUserData(uuid);
                invalidateOfflineModuleUserData(uuid, userDataModule.getId());
            }
        });
    }

    public void saveModulesUserData(UUID uuid) {
        LushRewards.getInstance().getEnabledRewardModules().forEach(module -> {
            if (module instanceof UserDataModule<?> userDataModule) {
                saveModuleUserData(uuid, userDataModule);
            }
        });
    }

    public void saveModuleUserData(UUID uuid, String moduleId) {
        LushRewards.getInstance().getModule(moduleId).ifPresent(module -> {
            if (module instanceof UserDataModule<?> userDataModule) {
                saveModuleUserData(uuid, userDataModule);
            }
        });
    }

    public CompletableFuture<Boolean> saveModuleUserData(UUID uuid, UserDataModule<?> userDataModule) {
        UserDataModule.UserData userData = userDataModule.getUserData(uuid);
        if (userData != null) {
            return saveUserData(userData);
        } else {
            return CompletableFuture.completedFuture(true);
        }
    }

    private void cleanupOfflineCachesIfDue() {
        long now = System.currentTimeMillis();
        long nextCleanup = nextOfflineCacheCleanupAt.get();
        if (now < nextCleanup || !nextOfflineCacheCleanupAt.compareAndSet(nextCleanup, now + OFFLINE_PLACEHOLDER_CACHE_TTL_MILLIS)) {
            return;
        }

        offlineRewardUsersCache.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
        offlineModuleUserDataCache.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }

    private void invalidateOfflineCache(UserDataModule.UserData userData) {
        if (userData instanceof RewardUser rewardUser) {
            invalidateOfflineRewardUser(rewardUser.getUniqueId());
            return;
        }

        invalidateOfflineModuleUserData(userData.getUniqueId(), userData.getModuleId());
    }

    private void invalidateOfflineRewardUser(UUID uuid) {
        offlineRewardUsersCache.remove(uuid);
    }

    private void invalidateOfflineModuleUserData(UUID uuid, @Nullable String moduleId) {
        if (moduleId != null) {
            String key = moduleId + ":" + uuid;
            offlineModuleUserDataCache.remove(key);
            return;
        }

        String suffix = ":" + uuid;
        offlineModuleUserDataCache.keySet().removeIf(key -> key.endsWith(suffix));
    }

    private record TimedCacheEntry<T>(T value, long expiresAt) {
        private boolean isExpired(long now) {
            return now >= expiresAt;
        }
    }
}
