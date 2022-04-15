package it.owlgram.android.helpers;

import androidx.annotation.NonNull;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

import android.content.ComponentName;
import android.content.pm.PackageManager;

public class IconsHelper {

    public enum Icons {
        DEFAULT(LocaleController.getString("DefaultIcon", R.string.DefaultIcon),"Default", 0, R.drawable.ic_icon_default),
        AQUA(LocaleController.getString("AquaIcon", R.string.AquaIcon), "Aqua", 1, R.drawable.ic_icon_aqua),
        SUNSET(LocaleController.getString("SunsetIcon", R.string.SunsetIcon), "Sunset", 2, R.drawable.ic_icon_sunset),
        MONO_BLACK(LocaleController.getString("MonoBlackIcon", R.string.MonoBlackIcon), "MonoBlack", 4, R.drawable.ic_icon_mono_black),
        DEVELOPER(LocaleController.getString("DeveloperIcon", R.string.DeveloperIcon), "Developer", 5, R.drawable.ic_icon_dev),
        ARCTIC(LocaleController.getString("ArcticIcon", R.string.ArcticIcon), "White",6, R.drawable.ic_icon_white),
        CLASSIC(LocaleController.getString("ClassicIcon", R.string.ClassicIcon), "Classic",7, R.drawable.ic_icon_classic);

        private final String title;
        private final String name;
        private final int id;
        private final int icon;

        Icons(String title, String name, int id, int icon) {
            this.title = title;
            this.name = name;
            this.id = id;
            this.icon = icon;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getIcon() {
            return icon;
        }

        public String getTitle() {
            return title;
        }
    }

    private static String getActivityName(String name) {
        return String.format("org.telegram.messenger.OG_Icon_Alt_%s", name);
    }

    public static int getSelectedIcon() {
        for (Icons icon : Icons.values()) {
            int stateComponent = ApplicationLoader.applicationContext.getPackageManager().getComponentEnabledSetting(
                    new ComponentName(
                            BuildConfig.APPLICATION_ID,
                            getActivityName(icon.getName())
                    )
            );
            if (stateComponent == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                return icon.ordinal();
            }
        }
        return Icons.DEFAULT.ordinal();
    }

    public static void saveSelectedIcon(int iconID) {
        for (Icons icon : Icons.values()) {
            if (icon.ordinal() == iconID) {
                enableComponent(icon.getName());
            } else {
                disableComponent(icon.getName());
            }
        }
    }

    private static void enableComponent(String name) {
        ApplicationLoader.applicationContext.getPackageManager().setComponentEnabledSetting(
                new ComponentName(BuildConfig.APPLICATION_ID, getActivityName(name)),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
        );
    }

    private static void disableComponent(String name) {
        ApplicationLoader.applicationContext.getPackageManager().setComponentEnabledSetting(
                new ComponentName(BuildConfig.APPLICATION_ID, getActivityName(name)),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
        );
    }
}
