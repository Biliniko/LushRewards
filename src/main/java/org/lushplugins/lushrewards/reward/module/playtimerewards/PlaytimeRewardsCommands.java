package org.lushplugins.lushrewards.reward.module.playtimerewards;

import org.lushplugins.lushrewards.LushRewards;
import org.lushplugins.lushrewards.user.RewardUser;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.exception.UnknownCommandException;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.orphan.OrphanCommand;

import java.time.LocalDate;

// TODO: Register this command
@SuppressWarnings("unused")
public record PlaytimeRewardsCommands(String moduleId) implements OrphanCommand {

    @CommandPlaceholder
    public void command(ExecutionContext<BukkitCommandActor> context) {
        throw new UnknownCommandException(context.input().source()); // TODO: test
    }

    @Subcommand("<user> last-collected-playtime set")
    @CommandPermission("lushrewards.edituser.playtimerewards.lastcollectedplaytime.set")
    public void setLastCollectedPlaytime(RewardUser user, int lastCollectedPlaytime) {
        user.getModuleData(this.moduleId, PlaytimeRewardsUserData.class).thenAccept(userData -> {
            userData.setLastCollectedPlaytime(lastCollectedPlaytime);

            LushRewards.getInstance().getStorageManager().saveModuleUserData(userData);
        });
    }

    @Subcommand("<user> start-date set")
    @CommandPermission("lushrewards.edituser.playtimerewards.startdate.set")
    public void setStartDate(RewardUser user, LocalDate startDate) {
        user.getModuleData(this.moduleId, PlaytimeRewardsUserData.class).thenAccept(userData -> {
            userData.setStartDate(startDate);

            LushRewards.getInstance().getStorageManager().saveModuleUserData(userData);
        });
    }

    @Subcommand("<user> previous-day-end-playtime set")
    @CommandPermission("lushrewards.edituser.playtimerewards.previousdayendplaytime.set")
    public void setPreviousDayEndPlaytime(RewardUser user, int previousDayEndPlaytime) {
        user.getModuleData(this.moduleId, PlaytimeRewardsUserData.class).thenAccept(userData -> {
            userData.setPreviousDayEndPlaytime(previousDayEndPlaytime);

            LushRewards.getInstance().getStorageManager().saveModuleUserData(userData);
        });
    }
}
