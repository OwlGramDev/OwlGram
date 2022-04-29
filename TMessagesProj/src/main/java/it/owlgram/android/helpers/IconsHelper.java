package it.owlgram.android.helpers;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Build;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

import it.owlgram.android.OwlConfig;

public class IconsHelper {

    public enum Icons {
        DEFAULT("Default", R.drawable.ic_icon_default),
        AQUA("Aqua", R.drawable.ic_icon_aqua),
        SUNSET("Sunset", R.drawable.ic_icon_sunset),
        MONO_BLACK("MonoBlack", R.drawable.ic_icon_mono_black),
        DEVELOPER("Developer", R.drawable.ic_icon_dev),
        ARCTIC("White", R.drawable.ic_icon_white),
        CLASSIC("Classic", R.drawable.ic_icon_classic);

        private final String name;
        private final int icon;

        Icons(String name, int icon) {
            this.name = name;
            this.icon = icon;
        }

        public String getName() {
            return name;
        }

        public int getIcon() {
            return icon;
        }

        public String getTitle() {
            switch (this) {
                case DEFAULT:
                    return LocaleController.getString("DefaultIcon", R.string.DefaultIcon);
                case AQUA:
                    return LocaleController.getString("AquaIcon", R.string.AquaIcon);
                case SUNSET:
                    return LocaleController.getString("SunsetIcon", R.string.SunsetIcon);
                case MONO_BLACK:
                    return LocaleController.getString("MonoBlackIcon", R.string.MonoBlackIcon);
                case DEVELOPER:
                    return LocaleController.getString("DeveloperIcon", R.string.DeveloperIcon);
                case ARCTIC:
                    return LocaleController.getString("ArcticIcon", R.string.ArcticIcon);
                case CLASSIC:
                    return LocaleController.getString("ClassicIcon", R.string.ClassicIcon);
            }
            return "???";
        }
    }

    private static String getActivityName(String name, boolean canBeDefault) {
        if (name.equals("Default") && OwlConfig.useMonetIcon && canBeDefault) {
            name = "Monet";
        }
        return String.format("org.telegram.messenger.OG_Icon_Alt_%s", name);
    }

    public static int getSelectedIcon() {
        for (Icons icon : Icons.values()) {
            int stateComponent = ApplicationLoader.applicationContext.getPackageManager().getComponentEnabledSetting(
                    new ComponentName(
                            BuildConfig.APPLICATION_ID,
                            getActivityName(icon.getName(), true)
                    )
            );
            if (stateComponent == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                return icon.ordinal();
            }
        }
        return Icons.DEFAULT.ordinal();
    }

    public static void switchToMonet() {
        if (isSelectedDefault()) {
            if (OwlConfig.useMonetIcon) {
                enableComponent("Default", false);
                disableComponent("Monet", false);
            } else {
                disableComponent("Default", false);
                enableComponent("Monet", false);
            }
        }
        OwlConfig.toggleUseMonetIcon();
    }

    public static boolean isSelectedMonet() {
        return getSelectedIcon() == Icons.DEFAULT.ordinal() && OwlConfig.useMonetIcon;
    }

    public static boolean needMonetMigration() {
        return isSelectedMonet() && Build.VERSION.SDK_INT > Build.VERSION_CODES.S;
    }

    public static boolean isSelectedDefault() {
        return getSelectedIcon() == Icons.DEFAULT.ordinal();
    }

    public static void saveSelectedIcon(int iconID) {
        for (Icons icon : Icons.values()) {
            if (icon.ordinal() == iconID) {
                enableComponent(icon.getName(), true);
            } else {
                disableComponent(icon.getName(), true);
            }
        }
    }

    private static void enableComponent(String name, boolean canBeDefault) {
        ApplicationLoader.applicationContext.getPackageManager().setComponentEnabledSetting(
                new ComponentName(BuildConfig.APPLICATION_ID, getActivityName(name, canBeDefault)),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
        );
    }

    private static void disableComponent(String name, boolean canBeDefault) {
        ApplicationLoader.applicationContext.getPackageManager().setComponentEnabledSetting(
                new ComponentName(BuildConfig.APPLICATION_ID, getActivityName(name, canBeDefault)),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
        );
    }
}
