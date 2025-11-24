package org.lushplugins.lushrewards.command;

import org.lushplugins.lushrewards.reward.module.playtimerewards.PlaytimeRewardsUserData;
import org.lushplugins.lushrewards.user.RewardUser;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@SuppressWarnings("unused")
@Command("rewards edit-user <user>")
public class EditUserCommand {

    @Subcommand("playtime set")
    @CommandPermission("lushrewards.edituser.playtime.set")
    public void setPlaytime(RewardUser user, int playtime) {
        for (PlaytimeRewardsUserData userData : user.getAllCachedModuleData(PlaytimeRewardsUserData.class)) {
            if (userData.getLastCollectedPlaytime() > playtime) {
                userData.setLastCollectedPlaytime(playtime);
            }

            if (userData.getPreviousDayEndPlaytime() > playtime) {
                userData.setPreviousDayEndPlaytime(playtime);
            }
        }

        // The below method also saves the user, so we rely on this
        user.setMinutesPlayed(playtime);
    }

    @Subcommand("playtime reset")
    @CommandPermission("lushrewards.edituser.playtime.reset")
    public void resetPlaytime(RewardUser user) {
        this.setPlaytime(user, 0);
    }

    @Subcommand("reset")
    @CommandPermission("lushrewards.edituser.reset")
    public void reset(RewardUser user) {
        // TODO: Implement and require additional `confirm` option
    }
}
