package it.owlgram.android.components.dynamic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.LinearLayout;

import androidx.core.graphics.ColorUtils;

import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.RLottieDrawable;
import org.telegram.ui.Components.RLottieImageView;

@SuppressLint("ViewConstructor")
public class SimpleActionCell extends LinearLayout {
    protected RLottieDrawable cameraDrawable;
    protected RLottieImageView iv;
    private static Theme.ResourcesProvider resourcesProvider;

    public SimpleActionCell(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        SimpleActionCell.resourcesProvider = resourcesProvider;
    }

    public ThemeInfo getTheme() {
        return null;
    }

    protected static int getThemedColor(String key) {
        return Theme.getColor(key, resourcesProvider);
    }

    public void updateColors() {
    }

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

    public static float getBackgroundAlpha() {
        int colorBack = Theme.getColor(Theme.key_windowBackgroundWhite);
        float alphaColor = 0;
        for (int ratio = 20; ratio > 0; ratio -= 1) {
            try {
                int blendedColor = ColorUtils.blendARGB(colorBack, getBackColor(), ratio / 100f);
                double contrast = ColorUtils.calculateContrast(colorBack, blendedColor) * 100.0;
                alphaColor = ratio / 100f;
                if (Math.round(contrast) <= 112) { // 112 IS CONTRAST OF 0.07f ALPHA
                    break;
                }
            } catch (Exception ignored) {
            }
        }
        return alphaColor;
    }

    public static int getBackColor() {
        return ColorUtils.calculateLuminance(Theme.getColor(Theme.key_windowBackgroundWhite)) > 0.5f ? 0xFF000000 : 0xFFFFFFFF;
    }
}
