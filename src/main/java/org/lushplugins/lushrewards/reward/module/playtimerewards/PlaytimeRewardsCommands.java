package org.lushplugins.lushrewards.reward.module.playtimerewards;

import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;
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

@SuppressWarnings("unused")
public record PlaytimeRewardsCommands(String moduleId) implements OrphanCommand {

    @CommandPlaceholder
    public void command(ExecutionContext<BukkitCommandActor> context) {
        throw new UnknownCommandException(context.input().source());
    }

    @Subcommand("get <user> last-collected-playtime")
    @CommandPermission("lushrewards.edituser.playtimerewards.lastcollectedplaytime.get")
    public void getLastCollectedPlaytime(BukkitCommandActor actor, RewardUser user) {
        user.getModuleData(this.moduleId, PlaytimeRewardsUserData.class).thenAccept(userData -> {
            ChatColorHandler.sendMessage(actor.sender(), "&#66b04f%s's &#b7faa2last collected playtime is &#66b04f%s"
                .formatted(user.getUsername(), userData.getLastCollectedPlaytime()));
        });
    }

    @Subcommand("modify <user> last-collected-playtime")
    @CommandPermission("lushrewards.edituser.playtimerewards.lastcollectedplaytime.modify")
    public void setLastCollectedPlaytime(BukkitCommandActor actor, RewardUser user, int lastCollectedPlaytime) {
        user.getModuleData(this.moduleId, PlaytimeRewardsUserData.class).thenAccept(userData -> {
            userData.setLastCollectedPlaytime(lastCollectedPlaytime);

            LushRewards.getInstance().getStorageManager().saveModuleUserData(userData);
            ChatColorHandler.sendMessage(actor.sender(), "&#66b04f%s's &#b7faa2last collected playtime has been set to &#66b04f%s"
                .formatted(user.getUsername(), lastCollectedPlaytime));
        });
    }

    @Subcommand("get <user> start-date")
    @CommandPermission("lushrewards.edituser.playtimerewards.startdate.get")
    public void getStartDate(BukkitCommandActor actor, RewardUser user) {
        user.getModuleData(this.moduleId, PlaytimeRewardsUserData.class).thenAccept(userData -> {
            ChatColorHandler.sendMessage(actor.sender(), "&#66b04f%s's &#b7faa2start date is &#66b04f%s"
                .formatted(user.getUsername(), userData.getStartDate()));
        });
    }

    @Subcommand("modify <user> start-date")
    @CommandPermission("lushrewards.edituser.playtimerewards.startdate.modify")
    public void setStartDate(BukkitCommandActor actor, RewardUser user, LocalDate startDate) {
        user.getModuleData(this.moduleId, PlaytimeRewardsUserData.class).thenAccept(userData -> {
            userData.setStartDate(startDate);

            LushRewards.getInstance().getStorageManager().saveModuleUserData(userData);
            ChatColorHandler.sendMessage(actor.sender(), "&#66b04f%s's &#b7faa2start date has been set to &#66b04f%s"
                .formatted(user.getUsername(), startDate.toString()));
        });
    }

    @Subcommand("get <user> previous-day-end-playtime")
    @CommandPermission("lushrewards.edituser.playtimerewards.previousdayendplaytime.get")
    public void getPreviousDayEndPlaytime(BukkitCommandActor actor, RewardUser user) {
        user.getModuleData(this.moduleId, PlaytimeRewardsUserData.class).thenAccept(userData -> {
            ChatColorHandler.sendMessage(actor.sender(), "&#66b04f%s's &#b7faa2previous day end playtime is &#66b04f%s"
                .formatted(user.getUsername(), userData.getPreviousDayEndPlaytime()));
        });
    }

    @Subcommand("modify <user> previous-day-end-playtime")
    @CommandPermission("lushrewards.edituser.playtimerewards.previousdayendplaytime.modify")
    public void setPreviousDayEndPlaytime(BukkitCommandActor actor, RewardUser user, int previousDayEndPlaytime) {
        user.getModuleData(this.moduleId, PlaytimeRewardsUserData.class).thenAccept(userData -> {
            userData.setPreviousDayEndPlaytime(previousDayEndPlaytime);

            LushRewards.getInstance().getStorageManager().saveModuleUserData(userData);
            ChatColorHandler.sendMessage(actor.sender(), "&#66b04f%s's &#b7faa2previous day end playtime has been set to &#66b04f%s"
                .formatted(user.getUsername(), previousDayEndPlaytime));
        });
    }
}
