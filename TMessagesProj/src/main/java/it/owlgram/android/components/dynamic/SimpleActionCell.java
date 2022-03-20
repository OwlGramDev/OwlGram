package it.owlgram.android.components.dynamic;

import android.content.Context;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

public class SimpleActionCell extends LinearLayout {
    public SimpleActionCell(Context context) {
        super(context);
    }

    public ThemeInfo getTheme() {return null;}

    public void updateColors() {}

    public static class ThemeInfo {
        public final int radius;
        public final boolean withBackground;
        public ThemeInfo(boolean withBackground, int radius) {
            this.withBackground = withBackground;
            this.radius = radius;
        }
    }

    public static int getBackColor() {
        return AndroidUtilities.isLight(Theme.getColor(Theme.key_windowBackgroundWhite)) ? 0xFF000000:0xFFFFFFFF;
    }
}
