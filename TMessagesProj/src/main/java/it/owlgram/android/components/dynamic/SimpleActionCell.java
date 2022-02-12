package it.owlgram.android.components.dynamic;

import android.content.Context;
import android.widget.LinearLayout;

public class SimpleActionCell extends LinearLayout {
    public SimpleActionCell(Context context) {
        super(context);
    }

    public ThemeInfo getTheme() {return null;}

    public static class ThemeInfo {
        public final int colorIcon;
        public final int background;
        public final int radius;
        public ThemeInfo(int colorIcon, int background, int radius) {
            this.colorIcon = colorIcon;
            this.background = background;
            this.radius = radius;
        }
    }
}
