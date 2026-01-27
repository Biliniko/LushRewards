package org.lushplugins.lushrewards.data;

import org.lushplugins.lushrewards.LushRewards;
import org.lushplugins.lushrewards.api.event.RewardUserPlaytimeChangeEvent;
import org.lushplugins.lushrewards.module.UserDataModule;
import org.jetbrains.annotations.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

public class RewardUser extends UserDataModule.UserData {
    private String username;
    private int minutesPlayed;

    private LocalDate dailyStartDate;
    private LocalDate weeklyStartDate;
    private LocalDate monthlyStartDate;
    private int dailyStartMinutes;
    private int weeklyStartMinutes;
    private int monthlyStartMinutes;

    public RewardUser(@NotNull UUID uuid, String username, int minutesPlayed) {
        super(uuid, null);
        this.username = username;
        this.minutesPlayed = minutesPlayed;
        updatePeriodBaselines(minutesPlayed, false);
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
        LushRewards.getInstance().getDataManager().saveRewardUser(this);
    }

    public int getMinutesPlayed() {
        return this.minutesPlayed;
    }

    public void setMinutesPlayed(int minutesPlayed) {
        LushRewards.getMorePaperLib().scheduling().globalRegionalScheduler().run(() -> LushRewards.getInstance().callEvent(new RewardUserPlaytimeChangeEvent(this, this.minutesPlayed, minutesPlayed)));

        this.minutesPlayed = minutesPlayed;
        updatePeriodBaselines(minutesPlayed, false);
        LushRewards.getInstance().getDataManager().saveRewardUser(this);
    }

    public int getDailyPlaytime() {
        return getDailyPlaytime(minutesPlayed);
    }

    public int getDailyPlaytime(int currentGlobalMinutes) {
        updatePeriodBaselines(currentGlobalMinutes, true);
        return Math.max(0, currentGlobalMinutes - dailyStartMinutes);
    }

    public int getWeeklyPlaytime() {
        return getWeeklyPlaytime(minutesPlayed);
    }

    public int getWeeklyPlaytime(int currentGlobalMinutes) {
        updatePeriodBaselines(currentGlobalMinutes, true);
        return Math.max(0, currentGlobalMinutes - weeklyStartMinutes);
    }

    public int getMonthlyPlaytime() {
        return getMonthlyPlaytime(minutesPlayed);
    }

    public int getMonthlyPlaytime(int currentGlobalMinutes) {
        updatePeriodBaselines(currentGlobalMinutes, true);
        return Math.max(0, currentGlobalMinutes - monthlyStartMinutes);
    }

    private boolean updatePeriodBaselines(int currentGlobalMinutes, boolean saveIfChanged) {
        LocalDate today = LocalDate.now();
        boolean changed = false;

        if (dailyStartDate == null || !dailyStartDate.equals(today) || currentGlobalMinutes < dailyStartMinutes) {
            dailyStartDate = today;
            dailyStartMinutes = currentGlobalMinutes;
            changed = true;
        }

        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        if (weeklyStartDate == null || !weeklyStartDate.equals(weekStart) || currentGlobalMinutes < weeklyStartMinutes) {
            weeklyStartDate = weekStart;
            weeklyStartMinutes = currentGlobalMinutes;
            changed = true;
        }

        LocalDate monthStart = today.withDayOfMonth(1);
        if (monthlyStartDate == null || !monthlyStartDate.equals(monthStart) || currentGlobalMinutes < monthlyStartMinutes) {
            monthlyStartDate = monthStart;
            monthlyStartMinutes = currentGlobalMinutes;
            changed = true;
        }

        if (changed && saveIfChanged) {
            LushRewards.getInstance().getDataManager().saveRewardUser(this);
        }

        return changed;
    }
}
