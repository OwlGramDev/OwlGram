package it.owlgram.android.components.dynamic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;

import java.util.Objects;

@SuppressLint("ViewConstructor")
public class LinearButtonCell extends SimpleActionCell {

    @SuppressLint("ClickableViewAccessibility")
    public LinearButtonCell(Context context, String text, int iconId, int color, int myId) {
        super(context);
        int colorWhite = Theme.getColor(Theme.key_windowBackgroundWhiteBlackText);

        setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f));
        setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8));
        setGravity(Gravity.CENTER);
        setOrientation(VERTICAL);

        CardView cardView = new CardView(context);
        cardView.setLayoutParams(new LinearLayout.LayoutParams(AndroidUtilities.dp(50), AndroidUtilities.dp(50)));
        cardView.setCardElevation(0);
        cardView.setRadius(AndroidUtilities.dp(25));
        cardView.setCardBackgroundColor(Color.TRANSPARENT);

        RelativeLayout rl = new RelativeLayout(context);
        rl.setLayoutParams(new CardView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        ImageView mt = new ImageView(context);
        mt.setScaleType(ImageView.ScaleType.FIT_CENTER);
        mt.setClickable(true);
        mt.setOnTouchListener((View view, MotionEvent motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                onItemClick(myId);
            }
            return false;
        });
        mt.setBackground(Theme.createSimpleSelectorRoundRectDrawable(0, Color.TRANSPARENT, AndroidUtilities.getTransparentColor(colorWhite, 0.2f)));
        mt.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        ImageView iv = new ImageView(context);
        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(AndroidUtilities.dp(27), AndroidUtilities.dp(27));
        layoutParams2.setMargins(0, 0,0,0);
        layoutParams2.addRule(RelativeLayout.CENTER_IN_PARENT);
        iv.setLayoutParams(layoutParams2);
        Drawable d = ContextCompat.getDrawable(context, iconId);
        Objects.requireNonNull(d).setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        iv.setBackground(d);

        TextView tv = new TextView(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0,AndroidUtilities.dp(5),0,0);
        tv.setLayoutParams(layoutParams);
        tv.setTextColor(color);
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        tv.setGravity(Gravity.CENTER);
        tv.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        tv.setEllipsize(TextUtils.TruncateAt.END);

        addView(cardView);
        cardView.addView(rl);
        rl.addView(mt);
        rl.addView(iv);
        addView(tv);
    }

    public static LinearLayout getButtonPreview(Context context, int pos) {
        int color = Theme.getColor(Theme.key_switchTrack);
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f));
        mainLayout.setOrientation(VERTICAL);
        mainLayout.setGravity(Gravity.CENTER);

        ImageView iv = new ImageView(context);
        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(AndroidUtilities.dp(21), AndroidUtilities.dp(21));
        layoutParams2.setMargins(0, 0,0,0);
        layoutParams2.addRule(RelativeLayout.CENTER_IN_PARENT);
        iv.setLayoutParams(layoutParams2);
        int iconId;
        switch (pos) {
            case 1:
                iconId = R.drawable.profile_video;
                break;
            case 2:
                iconId = R.drawable.profile_phone;
                break;
            case 3:
                iconId = R.drawable.msg_block;
                break;
            default:
                iconId = R.drawable.profile_newmsg;
                break;
        }
        Drawable d = ContextCompat.getDrawable(context, iconId);
        Objects.requireNonNull(d).setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        iv.setBackground(d);
        iv.setAlpha(0.5f);

        CardView cardView2 = new CardView(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, AndroidUtilities.dp(5));
        layoutParams.setMargins(AndroidUtilities.dp(5), AndroidUtilities.dp(6), AndroidUtilities.dp(5), 0);
        cardView2.setLayoutParams(layoutParams);
        cardView2.setCardBackgroundColor(AndroidUtilities.getTransparentColor(color,0.5f));
        cardView2.setRadius(AndroidUtilities.dp(2));
        cardView2.setCardElevation(0);

        mainLayout.addView(iv);
        mainLayout.addView(cardView2);
        return mainLayout;
    }

    public static LinearLayout getShimmerButton(Context context, int pos) {
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f));
        mainLayout.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8));
        mainLayout.setGravity(Gravity.CENTER);
        mainLayout.setOrientation(VERTICAL);

        ImageView iv = new ImageView(context);
        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(AndroidUtilities.dp(27), AndroidUtilities.dp(27));
        layoutParams2.setMargins(0, 0,0,0);
        layoutParams2.addRule(RelativeLayout.CENTER_IN_PARENT);
        iv.setLayoutParams(layoutParams2);
        int iconId;
        switch (pos) {
            case 1:
                iconId = R.drawable.profile_video;
                break;
            case 2:
                iconId = R.drawable.profile_phone;
                break;
            case 3:
                iconId = R.drawable.msg_block;
                break;
            default:
                iconId = R.drawable.profile_newmsg;
                break;
        }
        Drawable d = ContextCompat.getDrawable(context, iconId);
        Objects.requireNonNull(d).setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText), PorterDuff.Mode.SRC_ATOP);
        iv.setBackground(d);

        CardView cardView2 = new CardView(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, AndroidUtilities.dp(10));
        layoutParams.setMargins(AndroidUtilities.dp(10), AndroidUtilities.dp(11), AndroidUtilities.dp(10), 0);
        cardView2.setLayoutParams(layoutParams);
        cardView2.setCardBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        cardView2.setRadius(AndroidUtilities.dp(5));
        cardView2.setCardElevation(0);

        mainLayout.addView(iv);
        mainLayout.addView(cardView2);
        return mainLayout;
    }

    public ThemeInfo getTheme() {
        return new ThemeInfo(
                Theme.getColor(Theme.key_dialogTextBlue),
                Color.TRANSPARENT,
                AndroidUtilities.dp(25)
        );
    }

    protected void onItemClick(int id) {}
}
