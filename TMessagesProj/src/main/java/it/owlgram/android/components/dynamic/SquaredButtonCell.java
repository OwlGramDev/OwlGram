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
public class SquaredButtonCell extends SimpleActionCell {

    @SuppressLint("ClickableViewAccessibility")
    public SquaredButtonCell(Context context, String text, int iconId, int color, int myId) {
        super(context);
        setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f));
        setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8));
        setGravity(Gravity.CENTER);

        CardView cardView = new CardView(context);
        cardView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, AndroidUtilities.dp(80)));
        cardView.setCardElevation(0);
        cardView.setRadius(AndroidUtilities.dp(10.0f));
        cardView.setCardBackgroundColor(AndroidUtilities.getTransparentColor(color, 0.15f));

        RelativeLayout rl = new RelativeLayout(context);
        rl.setLayoutParams(new CardView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        LinearLayout ll = new LinearLayout(context);
        RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams1.addRule(RelativeLayout.CENTER_IN_PARENT);
        ll.setLayoutParams(layoutParams1);
        ll.setGravity(Gravity.CENTER);
        ll.setOrientation(LinearLayout.VERTICAL);

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
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(AndroidUtilities.dp(30), AndroidUtilities.dp(30));
        layoutParams2.setMargins(0,0,0,AndroidUtilities.dp(5));
        iv.setLayoutParams(layoutParams2);
        Drawable d = ContextCompat.getDrawable(context, iconId);
        Objects.requireNonNull(d).setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        iv.setBackground(d);

        TextView tv = new TextView(context);
        tv.setTextColor(color);
        tv.setText(text);
        tv.setLines(1);
        tv.setMaxLines(1);
        tv.setSingleLine(true);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11);
        tv.setGravity(Gravity.CENTER);
        tv.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        tv.setEllipsize(TextUtils.TruncateAt.END);

        ll.addView(iv);
        ll.addView(tv);
        rl.addView(mt);
        rl.addView(ll);
        cardView.addView(rl);
        addView(cardView);
    }

    public static LinearLayout getShimmerButton(Context context) {
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f));
        mainLayout.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8));
        mainLayout.setGravity(Gravity.CENTER);

        CardView cardView = new CardView(context);
        cardView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, AndroidUtilities.dp(80)));
        cardView.setCardElevation(0);
        cardView.setRadius(AndroidUtilities.dp(10.0f));
        cardView.setCardBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));

        mainLayout.addView(cardView);
        return mainLayout;
    }

    public static void drawButtonPreview(Canvas canvas, int x, int y, int w) {
        int color = Theme.getColor(Theme.key_switchTrack);
        RectF rectF = new RectF(x, y, x + w, y + w);
        int rad1 = Math.round((w * 14.77F) / 100F);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(AndroidUtilities.getTransparentColor(color,0.5f));
        canvas.drawRoundRect(rectF, rad1, rad1, p);

        int marginBottomText = Math.round((w * 12F) / 100F);
        int textWidth = Math.round((w * 80F) / 100F);
        int textHeight = Math.round((w * 19F) / 100F);
        int xText = x + (w >> 1) - (textWidth >> 1);
        int yText = y + w - marginBottomText - textHeight;
        RectF rectText = new RectF(xText, yText, xText + textWidth, yText + textHeight);
        p.setColor(AndroidUtilities.getTransparentColor(color,0.75f));
        canvas.drawRoundRect(rectText, textHeight >> 1, textHeight >> 1, p);
    }

    public ThemeInfo getTheme() {
        return new ThemeInfo(
                Theme.getColor(Theme.key_dialogTextBlue),
                AndroidUtilities.getTransparentColor(Theme.getColor(Theme.key_dialogTextBlue), 0.15f),
                AndroidUtilities.dp(10)
        );
    }

    protected void onItemClick(int id) {}
}
