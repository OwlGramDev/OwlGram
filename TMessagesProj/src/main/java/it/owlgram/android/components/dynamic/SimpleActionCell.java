package it.owlgram.android.components.dynamic;

import android.content.Context;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.core.graphics.ColorUtils;

import com.google.android.exoplayer2.util.Log;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.RLottieDrawable;
import org.telegram.ui.Components.RLottieImageView;

public class SimpleActionCell extends LinearLayout {
    protected RLottieDrawable cameraDrawable;
    protected RLottieImageView iv;

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

    public RLottieDrawable getAnimatedDrawable() {
        return cameraDrawable;
    }

    public RLottieImageView getLottieImageView() {
        return iv;
    }

    // TRYING TO KEEP 0.03f OF ALPHA WITH CONTRAST
    public static float getBackgroundAlpha() {
        float NEEDED_CONTRAST = 1.12f;
        int colorBack = Theme.getColor(Theme.key_windowBackgroundWhite);
        float alphaColor = 0;
        for (int ratio = 20; ratio > 0; ratio -= 1) {
            try {
                int blendedColor = ColorUtils.blendARGB(colorBack, getBackColor(), ratio / 100f);
                double contrast = ColorUtils.calculateContrast(colorBack, blendedColor);
                if (contrast <= NEEDED_CONTRAST) {
                    alphaColor = ratio / 100f;
                    break;
                }
            } catch (Exception ignored) {}
        }
        return alphaColor;
    }

    public static int getBackColor() {
        return ColorUtils.calculateLuminance(Theme.getColor(Theme.key_windowBackgroundWhite)) > 0.5f ? 0xFF000000:0xFFFFFFFF;
    }
}
