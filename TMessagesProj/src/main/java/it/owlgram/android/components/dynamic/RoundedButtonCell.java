package it.owlgram.android.components.dynamic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
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
import org.telegram.ui.ActionBar.Theme;

import java.util.Objects;

@SuppressLint("ViewConstructor")
public class RoundedButtonCell extends SimpleActionCell {

    @SuppressLint("ClickableViewAccessibility")
    public RoundedButtonCell(Context context, String text, int iconId, int color, int myId) {
        super(context);
        setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f));
        setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8));
        setGravity(Gravity.CENTER);
        setOrientation(VERTICAL);

        CardView cardView = new CardView(context);
        cardView.setLayoutParams(new LinearLayout.LayoutParams(AndroidUtilities.dp(50), AndroidUtilities.dp(50)));
        cardView.setCardElevation(0);
        cardView.setRadius(AndroidUtilities.dp(25));
        cardView.setCardBackgroundColor(AndroidUtilities.getTransparentColor(color, 0.15f));

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
        mt.setBackground(Theme.createSimpleSelectorRoundRectDrawable(0, Color.TRANSPARENT, AndroidUtilities.getTransparentColor(color, 0.5f)));
        mt.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        ImageView iv = new ImageView(context);
        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(AndroidUtilities.dp(25), AndroidUtilities.dp(25));
        layoutParams2.setMargins(0, AndroidUtilities.dp(5),0,0);
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

    public static LinearLayout getShimmerButton(Context context) {
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f));
        mainLayout.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8));
        mainLayout.setGravity(Gravity.CENTER);
        mainLayout.setOrientation(VERTICAL);

        CardView cardView = new CardView(context);
        cardView.setLayoutParams(new LinearLayout.LayoutParams(AndroidUtilities.dp(50), AndroidUtilities.dp(50)));
        cardView.setCardElevation(0);
        cardView.setRadius(AndroidUtilities.dp(25));
        cardView.setCardBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));

        CardView cardView2 = new CardView(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, AndroidUtilities.dp(10));
        layoutParams.setMargins(AndroidUtilities.dp(10), AndroidUtilities.dp(5), AndroidUtilities.dp(10), 0);
        cardView2.setLayoutParams(layoutParams);
        cardView2.setCardBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        cardView2.setRadius(AndroidUtilities.dp(5));
        cardView2.setCardElevation(0);

        mainLayout.addView(cardView);
        mainLayout.addView(cardView2);
        return mainLayout;
    }

    public static void drawButtonPreview(Canvas canvas, int x, int y, int w) {
        int color = Theme.getColor(Theme.key_switchTrack);

        int topTextMargin = Math.round((w * 15F) / 100F);
        int textWidth = Math.round((w * 100F) / 100F);
        int textHeight = Math.round((w * 19F) / 100F);
        int buttonHeight = w - textHeight - topTextMargin;
        int xButton = x + (w >> 1) - (buttonHeight >> 1);

        int totalHeight = buttonHeight + topTextMargin + textHeight;

        int totalYMiddle = y + (w >> 1) - (totalHeight >> 1);

        int xText = x + (w >> 1) - (textWidth >> 1);
        int yText = totalYMiddle + buttonHeight + topTextMargin;

        RectF rectF = new RectF(xButton, totalYMiddle, xButton + buttonHeight, totalYMiddle + buttonHeight);
        int rad1 = buttonHeight >> 1;
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(AndroidUtilities.getTransparentColor(color,0.5f));
        canvas.drawRoundRect(rectF, rad1, rad1, p);

        RectF rectText = new RectF(xText, yText, xText + textWidth, yText + textHeight);
        canvas.drawRoundRect(rectText, textHeight >> 1, textHeight >> 1, p);
    }

    public ThemeInfo getTheme() {
        return new ThemeInfo(
                Theme.getColor(Theme.key_dialogTextBlue),
                AndroidUtilities.getTransparentColor(Theme.getColor(Theme.key_dialogTextBlue), 0.15f),
                AndroidUtilities.dp(25)
        );
    }

    protected void onItemClick(int id) {}
}
