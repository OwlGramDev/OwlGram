package it.owlgram.android.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.RLottieImageView;

import java.util.Objects;

@SuppressLint("ViewConstructor")
public class ThemeDrawerCell extends LinearLayout {
    private boolean checkedElement = false;
    private final RLottieImageView imageView;
    private final ImageView background_selection;
    private Handler handler;

    public ThemeDrawerCell(Context context, int icon, int[] icons) {
        super(context);
        setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f));
        setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8));
        setGravity(Gravity.CENTER);
        setOrientation(VERTICAL);

        CardView cardView2 = new CardView(context);
        cardView2.setCardElevation(0);
        cardView2.setCardBackgroundColor(Color.TRANSPARENT);
        cardView2.setRadius(AndroidUtilities.dp(12.0f));
        cardView2.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, AndroidUtilities.dp(144)));

        RelativeLayout relativeLayout2 = new RelativeLayout(context);
        relativeLayout2.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        background_selection = new ImageView(context);
        background_selection.setBackgroundColor(Theme.getColor(Theme.key_dialogTextBlue));
        background_selection.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        background_selection.setAlpha(0.0f);

        RelativeLayout relativeLayout3 = new RelativeLayout(context);
        relativeLayout3.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        relativeLayout3.setPadding(AndroidUtilities.dp(2), AndroidUtilities.dp(2), AndroidUtilities.dp(2), AndroidUtilities.dp(2));

        CardView cardView = new CardView(context);
        cardView.setCardElevation(0);
        cardView.setCardBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        cardView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        cardView.setRadius(AndroidUtilities.dp(10.0f));

        RelativeLayout relativeLayout = new RelativeLayout(context);
        relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        LinearLayout linearLayout4 = new LinearLayout(context);
        linearLayout4.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        linearLayout4.setBackgroundColor(AndroidUtilities.getTransparentColor(Color.BLACK, 0.25f));

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        linearLayout.setWeightSum(2.0f);

        LinearLayout linearLayout2 = new LinearLayout(context);
        linearLayout2.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.75f));
        linearLayout2.setBackgroundColor(Theme.getColor(Theme.key_chats_menuBackground));
        linearLayout2.setOrientation(VERTICAL);

        LinearLayout linearLayout3 = new LinearLayout(context);
        linearLayout3.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, AndroidUtilities.dp(36)));
        linearLayout3.setBackgroundColor(Theme.getColor(Theme.key_chats_menuTopBackgroundCats));

        linearLayout2.addView(linearLayout3);
        float[] text_widths = new float[] {
                1.9f,
                1.7f,
                1.8f,
                1.9f,
                2.0f,
        };
        for (int i = 0;i < icons.length;i++) {
            LinearLayout linearLayout6 = new LinearLayout(context);
            linearLayout6.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            linearLayout6.setWeightSum(2.0f);

            LinearLayout linearLayout5 = new LinearLayout(context);
            LayoutParams layoutParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, text_widths[i]);
            layoutParams.setMargins(AndroidUtilities.dp(5), AndroidUtilities.dp(5), AndroidUtilities.dp(5), 0);
            linearLayout5.setLayoutParams(layoutParams);
            linearLayout5.setGravity(Gravity.CENTER_VERTICAL);

            ImageView imageView1 = new ImageView(context);
            Drawable d = ContextCompat.getDrawable(context, icons[i]);
            Objects.requireNonNull(d).setColorFilter(Theme.getColor(Theme.key_chats_menuItemIcon), PorterDuff.Mode.SRC_ATOP);
            imageView1.setBackground(d);
            imageView1.setLayoutParams(new LayoutParams(AndroidUtilities.dp(15), AndroidUtilities.dp(15)));

            CardView cardView3 = new CardView(context);
            cardView3.setCardElevation(0);
            cardView3.setCardBackgroundColor(Theme.getColor(Theme.key_chats_menuItemIcon));
            cardView3.setRadius(AndroidUtilities.dp(5));
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, AndroidUtilities.dp(10));
            layoutParams2.setMargins(AndroidUtilities.dp(5), 0,0,0);
            cardView3.setLayoutParams(layoutParams2);

            linearLayout5.addView(imageView1);
            linearLayout5.addView(cardView3);
            linearLayout6.addView(linearLayout5);
            linearLayout2.addView(linearLayout6);
        }

        int colorBackground = Theme.getColor(Theme.key_chats_menuBackground);
        ImageView iv1 = new ImageView(context);
        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP,
                new int[] {colorBackground, AndroidUtilities.getTransparentColor(colorBackground, 0)});
        gd.setCornerRadius(0f);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, AndroidUtilities.dp(70));
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        iv1.setLayoutParams(layoutParams);
        iv1.setBackground(gd);

        imageView = new RLottieImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(AndroidUtilities.dp(30), AndroidUtilities.dp(30));
        layoutParams2.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParams2.setMargins(0,0,0, AndroidUtilities.dp(10));
        imageView.setLayoutParams(layoutParams2);
        imageView.setAnimation(icon, 30, 30);
        float scale_factor = 2.0f;
        float translate_pos = ((AndroidUtilities.dp(30) * scale_factor) - AndroidUtilities.dp(40));

        setOnClickListener(v -> {
            if(!checkedElement) {
                imageView.stopAnimation();
                imageView.setProgress(0.0f);
                imageView.playAnimation();
                imageView.animate()
                        .scaleX(scale_factor)
                        .scaleY(scale_factor)
                        .translationY(-translate_pos)
                        .setDuration(250);
                background_selection.animate().alpha(1.0f).setDuration(250);
                if(handler != null) {
                    handler.removeCallbacksAndMessages(null);
                }
                handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(() -> {
                    if(checkedElement) {
                        imageView.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .translationY(0)
                                .setDuration(250);
                    }
                }, imageView.getAnimatedDrawable().getDuration());
                checkedElement = true;
                onSelect();
            }
        });

        addView(cardView2);
        cardView2.addView(relativeLayout2);
        relativeLayout2.addView(background_selection);
        relativeLayout2.addView(relativeLayout3);
        relativeLayout3.addView(cardView);
        cardView.addView(relativeLayout);
        relativeLayout.addView(linearLayout4);
        relativeLayout.addView(linearLayout);
        relativeLayout.addView(iv1);
        relativeLayout.addView(imageView);
        linearLayout.addView(linearLayout2);
    }

    protected void onSelect() {}

    public void setChecked(boolean checked, boolean animated) {
        checkedElement = checked;
        if (!checked && animated) {
            imageView.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .translationY(0)
                    .setDuration(250);
        }
        if(animated) {
            background_selection.animate().alpha(checked ? 1.0f:0.0f).setDuration(250);
        } else {
            background_selection.setAlpha(checked ? 1.0f:0.0f);
        }
    }

    public boolean isChecked() {
        return checkedElement;
    }
}
