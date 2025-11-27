package org.lushplugins.lushrewards.reward.module.dailyrewards;

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

@SuppressWarnings("unused")
public record DailyRewardsCommands(String moduleId) implements OrphanCommand {

    @CommandPlaceholder
    public void command(ExecutionContext<BukkitCommandActor> context) {
        throw new UnknownCommandException(context.input().source());
    }

    @Subcommand("get <user> days")
    @CommandPermission("lushrewards.edituser.dailyrewards.daynum.get")
    public void getDayNum(BukkitCommandActor actor, RewardUser user) {
        user.getModuleData(this.moduleId, DailyRewardsUserData.class).thenAccept(userData -> {
            ChatColorHandler.sendMessage(actor.sender(), "&#66b04f%s's &#b7faa2day num is &#66b04f%s"
                .formatted(user.getUsername(), userData.getDayNum()));
        });
    }

    @Subcommand("modify <user> days")
    @CommandPermission("lushrewards.edituser.dailyrewards.daynum.modify")
    public void setDayNum(BukkitCommandActor actor, RewardUser user, int dayNum) {
        user.getModuleData(this.moduleId, DailyRewardsUserData.class).thenAccept(userData -> {
            userData.setDayNum(dayNum);
            userData.setLastCollectedDate(DailyRewardsUserData.NEVER_COLLECTED);

            LushRewards.getInstance().getStorageManager().saveModuleUserData(userData);
            ChatColorHandler.sendMessage(actor.sender(), "&#66b04f%s's &#b7faa2day num has been set to &#66b04f%s"
                .formatted(user.getUsername(), dayNum));
        });
    }

    @Subcommand("reset <user> days")
    @CommandPermission("lushrewards.edituser.dailyrewards.daynum.reset")
    public void resetDayNum(BukkitCommandActor actor, RewardUser user) {
        user.getModuleData(this.moduleId, DailyRewardsUserData.class).thenAccept(userData -> {
            userData.setDayNum(1);
            userData.setLastCollectedDate(DailyRewardsUserData.NEVER_COLLECTED);

            LushRewards.getInstance().getStorageManager().saveModuleUserData(userData);
            ChatColorHandler.sendMessage(actor.sender(), "&#66b04f%s's &#b7faa2day num has been set to &#66b04f1"
                .formatted(user.getUsername()));
        });
    }

    @Subcommand("get <user> streak")
    @CommandPermission("lushrewards.edituser.dailyrewards.streak.get")
    public void getStreak(BukkitCommandActor actor, RewardUser user) {
        user.getModuleData(this.moduleId, DailyRewardsUserData.class).thenAccept(userData -> {
            ChatColorHandler.sendMessage(actor.sender(), "&#66b04f%s's &#b7faa2streak is &#66b04f%s"
                .formatted(user.getUsername(), userData.getStreak()));
        });
    }

    @Subcommand("modify <user> streak")
    @CommandPermission("lushrewards.edituser.dailyrewards.streak.modify")
    public void setStreak(BukkitCommandActor actor, RewardUser user, int streak) {
        user.getModuleData(this.moduleId, DailyRewardsUserData.class).thenAccept(userData -> {
            userData.setStreak(streak);

            LushRewards.getInstance().getStorageManager().saveModuleUserData(userData);
            ChatColorHandler.sendMessage(actor.sender(), "&#66b04f%s's &#b7faa2streak has been set to &#66b04f%s"
                .formatted(user.getUsername(), streak));
        });
    }

    @Subcommand("reset <user> streak")
    @CommandPermission("lushrewards.edituser.dailyrewards.streak.reset")
    public void resetStreak(BukkitCommandActor actor, RewardUser user) {
        user.getModuleData(this.moduleId, DailyRewardsUserData.class).thenAccept(userData -> {
            userData.setStreak(0);

            LushRewards.getInstance().getStorageManager().saveModuleUserData(userData);
            ChatColorHandler.sendMessage(actor.sender(), "&#66b04f%s's &#b7faa2streak has been set to &#66b04f0"
                .formatted(user.getUsername()));
        });
    }

    @Subcommand("get <user> highest-streak")
    @CommandPermission("lushrewards.edituser.dailyrewards.higheststreak.get")
    public void getHighestStreak(BukkitCommandActor actor, RewardUser user) {
        user.getModuleData(this.moduleId, DailyRewardsUserData.class).thenAccept(userData -> {
            ChatColorHandler.sendMessage(actor.sender(), "&#66b04f%s's &#b7faa2highest streak is &#66b04f%s"
                .formatted(user.getUsername(), userData.getHighestStreak()));
        });
    }

    @Subcommand("modify <user> highest-streak")
    @CommandPermission("lushrewards.edituser.dailyrewards.higheststreak.modify")
    public void setHighestStreak(BukkitCommandActor actor, RewardUser user, int highestStreak) {
        user.getModuleData(this.moduleId, DailyRewardsUserData.class).thenAccept(userData -> {
            userData.setHighestStreak(highestStreak);

            LushRewards.getInstance().getStorageManager().saveModuleUserData(userData);
            ChatColorHandler.sendMessage(actor.sender(), "&#66b04f%s's &#b7faa2highest streak has been set to &#66b04f%s"
                .formatted(user.getUsername(), highestStreak));
        });
    }

    @Subcommand("reset <user> highest-streak")
    @CommandPermission("lushrewards.edituser.dailyrewards.higheststreak.reset")
    public void resetHighestStreak(BukkitCommandActor actor, RewardUser user) {
        user.getModuleData(this.moduleId, DailyRewardsUserData.class).thenAccept(userData -> {
            userData.setHighestStreak(0);

            LushRewards.getInstance().getStorageManager().saveModuleUserData(userData);
            ChatColorHandler.sendMessage(actor.sender(), "&#66b04f%s's &#b7faa2highest streak has been set to &#66b04f0"
                .formatted(user.getUsername()));
        });
    }
}
