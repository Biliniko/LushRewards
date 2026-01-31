package org.lushplugins.lushrewards.module;

import org.lushplugins.lushlib.utils.DisplayItemStack;
import org.lushplugins.lushlib.utils.converter.YamlConverter;
import org.lushplugins.lushrewards.LushRewards;
import org.lushplugins.lushrewards.gui.GuiCommandButton;
import org.bukkit.configuration.ConfigurationSection;
import org.lushplugins.lushlib.module.Module;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public abstract class RewardModule extends Module {
    protected final File moduleFile;
    private final boolean requiresTimeTracker;
    private boolean shouldNotify = false;
    private final ConcurrentHashMap<String, DisplayItemStack> itemTemplates = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Character, GuiCommandButton> guiButtons = new ConcurrentHashMap<>();

    public RewardModule(String id, File moduleFile) {
        super(id);
        this.moduleFile = moduleFile;
        this.requiresTimeTracker = false;
    }

    public RewardModule(String id, File moduleFile, boolean requiresTimeTracker) {
        super(id);
        this.moduleFile = moduleFile;
        this.requiresTimeTracker = requiresTimeTracker;
    }

    public abstract boolean hasClaimableRewards(Player player);

    @SuppressWarnings("UnusedReturnValue")
    public abstract boolean claimRewards(Player player);

    public File getModuleFile() {
        return moduleFile;
    }

    public boolean requiresPlaytimeTracker() {
        return requiresTimeTracker;
    }

    public boolean shouldNotify() {
        return shouldNotify;
    }

    public void setShouldNotify(boolean shouldNotify) {
        this.shouldNotify = shouldNotify;
    }

    public DisplayItemStack getItemTemplate(String key) {
        DisplayItemStack itemTemplate = itemTemplates.get(key);
        return itemTemplate != null ? itemTemplate : DisplayItemStack.empty();
    }

    public GuiCommandButton getGuiButton(char key) {
        return guiButtons.get(key);
    }

    public void reloadGuiButtons(ConfigurationSection guiSection) {
        guiButtons.clear();

        if (guiSection == null) {
            return;
        }

        guiSection.getValues(false).forEach((key, value) -> {
            if (key.length() == 1 && value instanceof ConfigurationSection buttonSection) {
                List<String> commands = getCommandList(buttonSection);
                DisplayItemStack displayItem = YamlConverter.getDisplayItem(buttonSection);
                guiButtons.put(key.charAt(0), new GuiCommandButton(displayItem, commands));
                LushRewards.getInstance().getLogger().info("Loaded gui button: " + key);
            }
        });
    }

    private static List<String> getCommandList(ConfigurationSection section) {
        List<String> commands = new ArrayList<>();

        if (section.isList("commands")) {
            commands = section.getStringList("commands");
        } else if (section.isString("commands")) {
            commands.add(section.getString("commands"));
        } else if (section.isList("Commands")) {
            commands = section.getStringList("Commands");
        } else if (section.isString("Commands")) {
            commands.add(section.getString("Commands"));
        }

        return commands;
    }

    public void reloadItemTemplates(ConfigurationSection itemTemplatesSection) {
        // Clears category map
        itemTemplates.clear();

        // Checks if categories section exists
        if (itemTemplatesSection == null) {
            return;
        }

        // Repopulates category map
        itemTemplatesSection.getValues(false).forEach((key, value) -> {
            if (value instanceof ConfigurationSection categorySection) {
                itemTemplates.put(categorySection.getName(), YamlConverter.getDisplayItem(categorySection));
                LushRewards.getInstance().getLogger().info("Loaded item-template: " + categorySection.getName());
            }
        });
    }

    @FunctionalInterface
    public interface Constructor<T extends RewardModule> {
        T build(String id, File file) ;
    }

    public static class Type {
        public static final String DAILY_REWARDS = "daily-rewards";
        public static final String PLAYTIME_REWARDS = "playtime-rewards";
        public static final String PLAYTIME_TRACKER = "playtime-tracker";
    }
}
