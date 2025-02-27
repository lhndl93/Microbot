package net.runelite.client.plugins.microbot.breakhandler;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.plugins.microbot.util.antiban.enums.PlaySchedule;

@ConfigGroup(BreakHandlerConfig.configGroup)
public interface BreakHandlerConfig extends Config {
    String configGroup = "break-handler";
    String hideOverlay = "hideOverlay";

    @ConfigItem(
            keyName = "hideOverlay",
            name = "Overlay Hider",
            description = "Select this if you want to hide overlay"
    )
    default boolean isHideOverlay() {
        return false;
    }

    // Play Schedule section
    @ConfigSection(
            name = "Play Schedule",
            description = "Options related to using a play schedule",
            position = 51
    )
    String usePlaySchedule = "usePlaySchedule";

    @ConfigItem(
            keyName = "Min Playtime",
            name = "Min Playtime",
            description = "Time until break start in minutes",
            position = 0
    )
    default int timeUntilBreakStart() {
        return 60;
    }

    @ConfigItem(
            keyName = "Max Playtime",
            name = "Max Playtime",
            description = "Time until break ends in minutes",
            position = 1
    )
    default int timeUntilBreakEnd() {
        return 120;
    }

    @ConfigItem(
            keyName = "Min BreakTime",
            name = "Min BreakTime",
            description = "Break duration start in minutes",
            position = 2
    )
    default int breakDurationStart() {
        return 10;
    }

    @ConfigItem(
            keyName = "Max BreakTime",
            name = "Max BreakTime",
            description = "Break duration end in minutes",
            position = 3
    )
    default int breakDurationEnd() {
        return 15;
    }

    // boolean to only use microbreaks
    @ConfigItem(
            keyName = "OnlyMicroBreaks",
            name = "Micro Breaks Only",
            description = "Only use micro breaks if enabled",
            position = 4
    )
    default boolean onlyMicroBreaks() {
        return false;
    }

    @ConfigItem(
            keyName = "Logout",
            name = "Logout",
            description = "Logout when taking a break",
            position = 5
    )
    default boolean logoutAfterBreak() {
        return true;
    }

    @ConfigItem(
            keyName = "useRandomWorld",
            name = "Use RandomWorld",
            description = "Change to a random world once break is finished",
            position = 6
    )
    default boolean useRandomWorld() {
        return false;
    }

    @ConfigItem(
            keyName = "UsePlaySchedule",
            name = "Use Play Schedule",
            description = "Enable or disable the use of a play schedule",
            position = 0,
            section = usePlaySchedule
    )
    default boolean usePlaySchedule() {
        return false;
    }

    @ConfigItem(
            keyName = "PlaySchedule",
            name = "Play Schedule",
            description = "Select the play schedule",
            position = 1,
            section = usePlaySchedule
    )
    default PlaySchedule playSchedule() {
        return PlaySchedule.MEDIUM_DAY;
    }
}
