package it.owlgram.android.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
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

import com.google.android.exoplayer2.util.Log;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

public class MaterialButton extends CardView {
    final private TextView textView;
    private boolean pressed = false;
    private View.OnClickListener listener;
    private ImageView mt;

    @SuppressLint("ClickableViewAccessibility")
    public MaterialButton(Context context) {
        super(context);
        int blueColor = Theme.getColor(Theme.key_dialogTextBlue);
        setCardBackgroundColor(blueColor);
        LinearLayout.LayoutParams layoutParams10 = new LinearLayout.LayoutParams(AndroidUtilities.dp(80), AndroidUtilities.dp(35));
        setLayoutParams(layoutParams10);
        setCardElevation(0);
        setRadius(AndroidUtilities.dp(5.0f));

        RelativeLayout rl = new RelativeLayout(context);
        rl.setLayoutParams(new CardView.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        mt = new ImageView(context);
        mt.setScaleType(ImageView.ScaleType.FIT_CENTER);
        mt.setBackground(Theme.createSimpleSelectorRoundRectDrawable(0, Color.TRANSPARENT, Color.argb(55, 0,0,0)));
        mt.setLayoutParams(new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        /*mt.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pressed = true;
                    break;
                case MotionEvent.ACTION_UP:
                    if (pressed && listener != null) {
                        listener.onClick(MaterialButton.this);
                    }
                case MotionEvent.ACTION_CANCEL:
                    pressed = false;
                    break;
            }
            return false;
        });*/
        mt.setClickable(true);
        mt.setOnTouchListener((View view, MotionEvent motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP && listener != null) {
                listener.onClick(this);
            }
            return false;
        });

        textView = new TextView(context);
        textView.setTextColor(Color.WHITE);
        textView.setLines(1);
        textView.setSingleLine(true);
        RelativeLayout.LayoutParams layoutParams8 = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams8.addRule(RelativeLayout.CENTER_IN_PARENT);
        textView.setLayoutParams(layoutParams8);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));

        rl.addView(mt);
        rl.addView(textView);
        addView(rl);
    }

    public void setOutlineMode() {
        int blueColor = Theme.getColor(Theme.key_dialogTextBlue);
        setCardBackgroundColor(Color.TRANSPARENT);
        textView.setTextColor(blueColor);
        mt.setBackground(Theme.createSimpleSelectorRoundRectDrawable(0, Color.TRANSPARENT, AndroidUtilities.getTransparentColor(blueColor, 0.5f)));
    }

    public void setOnClickListener(View.OnClickListener l) {
        listener = l;
    }

    public void setText(String text) {
        textView.setText(text);
    }
}
