package org.lushplugins.lushrewards.reward.module.dailyrewards;

import org.lushplugins.lushrewards.LushRewards;
import org.lushplugins.lushrewards.user.RewardUser;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.exception.UnknownCommandException;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.orphan.OrphanCommand;

// TODO: Make commands return messages and add get commands
// TODO: Change command format to what NiceRon suggested (we love ron)
@SuppressWarnings("unused")
public record DailyRewardsCommands(String moduleId) implements OrphanCommand {

    @CommandPlaceholder
    public void command(ExecutionContext<BukkitCommandActor> context) {
        throw new UnknownCommandException(context.input().source());
    }

    @Subcommand("<user> days set")
    @CommandPermission("lushrewards.edituser.dailyrewards.daynum.set")
    public void setDayNum(RewardUser user, int dayNum) {
        user.getModuleData(this.moduleId, DailyRewardsUserData.class).thenAccept(userData -> {
            userData.setDayNum(dayNum);
            userData.setLastCollectedDate(DailyRewardsUserData.NEVER_COLLECTED);

            LushRewards.getInstance().getStorageManager().saveModuleUserData(userData);
        });
    }

    @Subcommand("<user> days reset")
    @CommandPermission("lushrewards.edituser.dailyrewards.daynum.reset")
    public void resetDayNum(RewardUser user) {
        user.getModuleData(this.moduleId, DailyRewardsUserData.class).thenAccept(userData -> {
            userData.setDayNum(0);
            userData.setLastCollectedDate(DailyRewardsUserData.NEVER_COLLECTED);

            LushRewards.getInstance().getStorageManager().saveModuleUserData(userData);
        });
    }

    @Subcommand("<user> streak set")
    @CommandPermission("lushrewards.edituser.dailyrewards.streak.set")
    public void setStreak(RewardUser user, int streak) {
        user.getModuleData(this.moduleId, DailyRewardsUserData.class).thenAccept(userData -> {
            userData.setStreak(streak);

            LushRewards.getInstance().getStorageManager().saveModuleUserData(userData);
        });
    }

    @Subcommand("<user> streak reset")
    @CommandPermission("lushrewards.edituser.dailyrewards.streak.reset")
    public void resetStreak(RewardUser user) {
        user.getModuleData(this.moduleId, DailyRewardsUserData.class).thenAccept(userData -> {
            userData.setStreak(0);

            LushRewards.getInstance().getStorageManager().saveModuleUserData(userData);
        });
    }

    @Subcommand("<user> highest-streak set")
    @CommandPermission("lushrewards.edituser.dailyrewards.higheststreak.set")
    public void setHighestStreak(RewardUser user, int highestStreak) {
        user.getModuleData(this.moduleId, DailyRewardsUserData.class).thenAccept(userData -> {
            userData.setHighestStreak(highestStreak);

            LushRewards.getInstance().getStorageManager().saveModuleUserData(userData);
        });
    }

    @Subcommand("<user> highest-streak reset")
    @CommandPermission("lushrewards.edituser.dailyrewards.higheststreak.reset")
    public void resetHighestStreak(RewardUser user) {
        user.getModuleData(this.moduleId, DailyRewardsUserData.class).thenAccept(userData -> {
            userData.setHighestStreak(0);

            LushRewards.getInstance().getStorageManager().saveModuleUserData(userData);
        });
    }
}
