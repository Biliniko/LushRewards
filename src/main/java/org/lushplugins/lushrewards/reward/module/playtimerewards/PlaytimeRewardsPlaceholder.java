package org.lushplugins.lushrewards.reward.module.playtimerewards;

import org.lushplugins.lushrewards.LushRewards;
import org.lushplugins.lushrewards.reward.module.RewardModule;
import org.lushplugins.lushrewards.user.RewardUser;
import org.lushplugins.lushrewards.utils.MathUtils;
import org.lushplugins.lushrewards.utils.placeholder.Placeholder;
import org.lushplugins.lushrewards.utils.placeholder.TimePlaceholder;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.HashSet;

public class PlaytimeRewardsPlaceholder {
    private static final HashSet<Placeholder> placeholderCache = new HashSet<>();

    static {
        placeholderCache.add(new TimePlaceholder("playtime", (params, player) -> {
            if (player == null || LushRewards.getInstance().getModule(params[0]).isEmpty()) {
                return null;
            }

            RewardUser user = LushRewards.getInstance().getUserCache().getCachedUser(player.getUniqueId());
            if (user == null) {
                return null;
            }

            RewardModule module = LushRewards.getInstance().getRewardModuleManager().getModule(params[0]);
            if (module instanceof PlaytimeRewardsModule playtimeModule) {
                int globalPlaytime = user.getMinutesPlayed();
                if (playtimeModule.getResetPlaytimeAt() <= 0) {
                    return globalPlaytime * 60;
                } else {
                    PlaytimeRewardsUserData userData = user.getCachedModuleData(module.getId(), PlaytimeRewardsUserData.class);
                    return (globalPlaytime - userData.getPreviousDayEndPlaytime()) * 60;
                }
            } else {
                return null;
            }
        }));

        placeholderCache.add(new TimePlaceholder("playtime_since_last_collected", (params, player) -> {
            if (player == null || LushRewards.getInstance().getModule(params[0]).isEmpty()) {
                return null;
            }

            RewardUser user = LushRewards.getInstance().getUserCache().getCachedUser(player.getUniqueId());
            if (user == null) {
                return null;
            }

            RewardModule module = LushRewards.getInstance().getRewardModuleManager().getModule(params[0]);
            if (module instanceof PlaytimeRewardsModule) {
                PlaytimeRewardsUserData userData = user.getCachedModuleData(module.getId(), PlaytimeRewardsUserData.class);
                return (user.getMinutesPlayed() - userData.getLastCollectedPlaytime()) * 60;
            } else {
                return null;
            }
        }));

        placeholderCache.add(new TimePlaceholder("time_since_reset", (params, player) -> {
            RewardUser user = LushRewards.getInstance().getUserCache().getCachedUser(player.getUniqueId());
            if (user == null) {
                return null;
            }

            RewardModule module = LushRewards.getInstance().getRewardModuleManager().getModule(params[0]);
            if (module instanceof PlaytimeRewardsModule playtimeModule) {
                int resetPlaytimeAt = playtimeModule.getResetPlaytimeAt();
                if (resetPlaytimeAt > 0) {
                    PlaytimeRewardsUserData userData = user.getCachedModuleData(module.getId(),  PlaytimeRewardsUserData.class);
                    if (userData != null) {
                        long start = LocalDateTime.of(userData.getStartDate(), LocalTime.MIN).toEpochSecond(ZoneOffset.UTC);
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

            RewardModule module = LushRewards.getInstance().getRewardModuleManager().getModule(params[0]);
            if (!(module instanceof PlaytimeRewardsModule playtimeModule)) {
                return null;
            }

            RewardUser user = LushRewards.getInstance().getUserCache().getCachedUser(player.getUniqueId());
            if (user == null) {
                return null;
            }

            PlaytimeRewardsUserData userData = user.getCachedModuleData(module.getId(), PlaytimeRewardsUserData.class);
            if (userData == null) {
                return null;
            }

            int startPlaytime = userData.getLastCollectedPlaytime();
            Integer nextRewardMinute = null;
            for (PlaytimeRewardCollection reward : playtimeModule.getRewards()) {
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

            int remainingMinutes = Math.max(nextRewardMinute - user.getMinutesPlayed(), 0);
            return remainingMinutes * 60;
        }));
    }
}
