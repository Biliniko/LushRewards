package org.lushplugins.lushrewards.module.playtimerewards;

import org.lushplugins.lushrewards.LushRewards;
import org.lushplugins.lushrewards.data.RewardUser;
import org.lushplugins.lushlib.module.Module;
import org.lushplugins.lushrewards.utils.MathUtils;
import org.lushplugins.lushrewards.utils.placeholder.Placeholder;
import org.lushplugins.lushrewards.utils.placeholder.SimplePlaceholder;
import org.lushplugins.lushrewards.utils.placeholder.TimePlaceholder;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

public class PlaytimeRewardsPlaceholder {
    private static final HashSet<Placeholder> placeholderCache = new HashSet<>();

    static {
        placeholderCache.add(new TimePlaceholder("playtime", (params, player) -> {
            if (player == null || LushRewards.getInstance().getModule(params[0]).isEmpty()) {
                return null;
            }

            RewardUser rewardUser = getRewardUser(player.getUniqueId());
            if (rewardUser == null) {
                return null;
            }

            Optional<Module> optionalModule = LushRewards.getInstance().getModule(params[0]);
            if (optionalModule.isPresent() && optionalModule.get() instanceof PlaytimeRewardsModule module) {
                PlaytimeRewardsModule.UserData userData = getUserData(module, player.getUniqueId());
                if (userData == null) {
                    return null;
                }

                PlaytimeRewardsModule.PlaceholderState state = module.getPlaceholderState(rewardUser, userData);
                if (module.getResetPlaytimeAt() <= 0) {
                    return state.currentGlobalPlaytime() * 60;
                }

                return (state.currentGlobalPlaytime() - state.previousDayEndPlaytime()) * 60;
            } else {
                return null;
            }
        }));

        placeholderCache.add(new TimePlaceholder("playtime_since_last_collected", (params, player) -> {
            if (player == null || LushRewards.getInstance().getModule(params[0]).isEmpty()) {
                return null;
            }

            RewardUser rewardUser = getRewardUser(player.getUniqueId());
            if (rewardUser == null) {
                return null;
            }

            Optional<Module> optionalModule = LushRewards.getInstance().getModule(params[0]);
            if (optionalModule.isPresent() && optionalModule.get() instanceof PlaytimeRewardsModule module) {
                PlaytimeRewardsModule.UserData userData = getUserData(module, player.getUniqueId());
                if (userData == null) {
                    return null;
                }

                PlaytimeRewardsModule.PlaceholderState state = module.getPlaceholderState(rewardUser, userData);
                return (state.currentGlobalPlaytime() - state.lastCollectedPlaytime()) * 60;
            } else {
                return null;
            }
        }));

        placeholderCache.add(new TimePlaceholder("time_since_reset", (params, player) -> {
            Optional<Module> optionalModule = LushRewards.getInstance().getModule(params[0]);
            if (optionalModule.isPresent() && optionalModule.get() instanceof PlaytimeRewardsModule module) {
                int resetPlaytimeAt = module.getResetPlaytimeAt();
                if (resetPlaytimeAt > 0) {
                    if (player == null) {
                        return null;
                    }

                    RewardUser rewardUser = getRewardUser(player.getUniqueId());
                    if (rewardUser == null) {
                        return null;
                    }

                    PlaytimeRewardsModule.UserData userData = getUserData(module, player.getUniqueId());
                    if (userData != null) {
                        PlaytimeRewardsModule.PlaceholderState state = module.getPlaceholderState(rewardUser, userData);
                        long start = LocalDateTime.of(state.startDate(), LocalTime.MIN).toEpochSecond(ZoneOffset.UTC);
                        long now = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
                        return (int) (now - start);
                    }
                }
            }

            return null;
        }));

        placeholderCache.add(new TimePlaceholder("time_until_next_reward", (params, player) -> {
            if (player == null) {
                return null;
            }

            Optional<Module> optionalModule = LushRewards.getInstance().getModule(params[0]);
            if (optionalModule.isEmpty()) {
                return null;
            }

            if (!(optionalModule.get() instanceof PlaytimeRewardsModule module)) {
                return null;
            }

            RewardUser rewardUser = getRewardUser(player.getUniqueId());
            if (rewardUser == null) {
                return null;
            }

            PlaytimeRewardsModule.UserData userData = getUserData(module, player.getUniqueId());
            if (userData == null) {
                return null;
            }

            PlaytimeRewardsModule.PlaceholderState state = module.getPlaceholderState(rewardUser, userData);
            int currentPlaytime = state.currentGlobalPlaytime();
            int startPlaytime = state.lastCollectedPlaytime();
            if (module.getResetPlaytimeAt() > 0) {
                int previousDayEnd = state.previousDayEndPlaytime();
                currentPlaytime -= previousDayEnd;
                startPlaytime = Math.max(startPlaytime - previousDayEnd, 0);
            }

            Integer nextRewardMinute = null;
            for (PlaytimeRewardCollection reward : module.getRewards()) {
                Integer minutes = MathUtils.findFirstNumInSequence(reward.getStartMinute(), reward.getRepeatFrequency(), startPlaytime);
                if (minutes != null) {
                    if (nextRewardMinute == null || minutes < nextRewardMinute) {
                        nextRewardMinute = minutes;
                    }
                }
            }

            if (nextRewardMinute == null) {
                return 0;
            }

            int remainingMinutes = Math.max(nextRewardMinute - currentPlaytime, 0);
            return remainingMinutes * 60;
        }));
    }

    private final String id;

    public PlaytimeRewardsPlaceholder(String id) {
        this.id = id;
    }

    public void register() {
        SimplePlaceholder modulePlaceholder = new SimplePlaceholder(id, (params, player) -> null);
        placeholderCache.forEach(modulePlaceholder::addChild);
        LushRewards.getInstance().getLocalPlaceholders().registerPlaceholder(modulePlaceholder);
    }

    public void unregister() {
        LushRewards.getInstance().getLocalPlaceholders().unregisterPlaceholder(id);
    }

    private static RewardUser getRewardUser(UUID uuid) {
        return LushRewards.getInstance().getDataManager().getOrRequestRewardUser(uuid);
    }

    private static PlaytimeRewardsModule.UserData getUserData(PlaytimeRewardsModule module, UUID uuid) {
        return LushRewards.getInstance().getDataManager().getOrRequestUserData(uuid, module);
    }
}
