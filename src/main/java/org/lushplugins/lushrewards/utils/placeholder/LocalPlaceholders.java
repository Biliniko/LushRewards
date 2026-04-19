package org.lushplugins.lushrewards.utils.placeholder;

import org.lushplugins.lushrewards.LushRewards;
import org.lushplugins.lushrewards.data.RewardUser;
import org.lushplugins.lushrewards.module.RewardModule;
import org.lushplugins.lushrewards.module.playtimetracker.PlaytimeTracker;
import org.lushplugins.lushrewards.module.playtimetracker.PlaytimeTrackerModule;
import org.lushplugins.lushlib.module.Module;
import org.bukkit.OfflinePlayer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// TODO: Either make shadeable Placeholder library or just add parseString method to parse messages/gui through
public class LocalPlaceholders {
    private static LocalDateTime nextDay = LocalDate.now().plusDays(1).atStartOfDay();

    private final ConcurrentHashMap<String, Placeholder> placeholders = new ConcurrentHashMap<>();

    public LocalPlaceholders() {

        registerPlaceholder(new SimplePlaceholder("countdown", (params, player) -> {
            LocalDateTime now = LocalDateTime.now();
            long secondsUntil = now.until(nextDay, ChronoUnit.SECONDS);

            if (secondsUntil < 0) {
                nextDay = LocalDate.now().plusDays(1).atStartOfDay();
                secondsUntil = now.until(nextDay, ChronoUnit.SECONDS);
            }

            long hours = secondsUntil / 3600;
            long minutes = (secondsUntil % 3600) / 60;
            long seconds = secondsUntil % 60;

            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }));

        registerPlaceholder(new SimplePlaceholder("global_playtime", (params, player) -> {
            if (player == null) {
                return null;
            }

            PlaytimeTracker playtimeTracker = getPlaytimeTracker(player);
            if (playtimeTracker != null) {
                return String.valueOf(playtimeTracker.getGlobalPlaytime());
            }

            RewardUser rewardUser = getRewardUser(player);
            return rewardUser != null ? String.valueOf(rewardUser.getMinutesPlayed()) : null;
        }));

        registerPlaceholder(new TimePlaceholder("daily_playtime", (params, player) -> {
            if (player == null) {
                return null;
            }

            RewardUser rewardUser = getRewardUser(player);
            if (rewardUser == null) {
                return null;
            }

            Integer currentGlobalMinutes = getCurrentGlobalMinutes(player);
            if (currentGlobalMinutes == null) {
                return null;
            }

            return rewardUser.getDailyPlaytime(currentGlobalMinutes) * 60;
        }));

        registerPlaceholder(new TimePlaceholder("weekly_playtime", (params, player) -> {
            if (player == null) {
                return null;
            }

            RewardUser rewardUser = getRewardUser(player);
            if (rewardUser == null) {
                return null;
            }

            Integer currentGlobalMinutes = getCurrentGlobalMinutes(player);
            if (currentGlobalMinutes == null) {
                return null;
            }

            return rewardUser.getWeeklyPlaytime(currentGlobalMinutes) * 60;
        }));

        registerPlaceholder(new TimePlaceholder("monthly_playtime", (params, player) -> {
            if (player == null) {
                return null;
            }

            RewardUser rewardUser = getRewardUser(player);
            if (rewardUser == null) {
                return null;
            }

            Integer currentGlobalMinutes = getCurrentGlobalMinutes(player);
            if (currentGlobalMinutes == null) {
                return null;
            }

            return rewardUser.getMonthlyPlaytime(currentGlobalMinutes) * 60;
        }));

        registerPlaceholder(new SimplePlaceholder("session_playtime", (params, player) -> {
            if (player == null) {
                return "0";
            }

            PlaytimeTracker playtimeTracker = getPlaytimeTracker(player);
            return playtimeTracker != null ? String.valueOf(playtimeTracker.getSessionPlaytime()) : "0";
        }));

        registerPlaceholder(new SimplePlaceholder("total_session_playtime", (params, player) -> {
            if (player == null) {
                return "0";
            }

            PlaytimeTracker playtimeTracker = getPlaytimeTracker(player);
            return playtimeTracker != null ? String.valueOf(playtimeTracker.getTotalSessionPlaytime()) : "0";
        }));
    }

    private PlaytimeTracker getPlaytimeTracker(OfflinePlayer player) {
        Optional<Module> optionalPlaytimeTracker = LushRewards.getInstance().getModule(RewardModule.Type.PLAYTIME_TRACKER);
        if (optionalPlaytimeTracker.isPresent() && optionalPlaytimeTracker.get() instanceof PlaytimeTrackerModule playtimeTrackerModule) {
            return playtimeTrackerModule.getPlaytimeTracker(player.getUniqueId());
        }

        return null;
    }

    private RewardUser getRewardUser(OfflinePlayer player) {
        return LushRewards.getInstance().getDataManager().getOrRequestRewardUser(player);
    }

    private Integer getCurrentGlobalMinutes(OfflinePlayer player) {
        if (player == null) {
            return null;
        }

        PlaytimeTracker playtimeTracker = getPlaytimeTracker(player);
        if (playtimeTracker != null) {
            return playtimeTracker.getGlobalPlaytime();
        }

        RewardUser rewardUser = getRewardUser(player);
        return rewardUser != null ? rewardUser.getMinutesPlayed() : null;
    }

    public String parsePlaceholder(String params, OfflinePlayer player) {
        String[] paramsArr = params.split("_");

        Placeholder currentPlaceholder = null;
        String currParams = params;
        for (int i = 0; i < paramsArr.length; i++) {
            boolean found = false;

            for (Placeholder subPlaceholder : currentPlaceholder != null ? currentPlaceholder.getChildren() : placeholders.values()) {
                if (subPlaceholder.matches(currParams)) {
                    currentPlaceholder = subPlaceholder;
                    currParams = currParams.replace(subPlaceholder.getContent() + "_", "");

                    found = true;
                    break;
                }
            }

            if (!found) {
                break;
            }
        }

        if (currentPlaceholder != null) {
            try {
                return currentPlaceholder.parse(paramsArr, player);
            } catch(Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        return null;
    }

    public void registerPlaceholder(Placeholder placeholder) {
        placeholders.put(placeholder.getContent(), placeholder);
    }

    public void unregisterPlaceholder(String content) {
        placeholders.remove(content);
    }

    @FunctionalInterface
    public interface PlaceholderFunction {
        String apply(String[] params, OfflinePlayer player) ;
    }
}
