package it.owlgram.android.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.cardview.widget.CardView;

import com.google.android.exoplayer2.util.Log;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;

import it.owlgram.android.helpers.FolderIconHelper;

public class FolderIconView extends RelativeLayout {
    final private ImageView iv;
    private int resId = 0;

    @SuppressLint("ClickableViewAccessibility")
    public FolderIconView(Context context) {
        super(context);
        setGravity(Gravity.CENTER);
        setPadding(AndroidUtilities.dp(15), AndroidUtilities.dp(15), AndroidUtilities.dp(15), AndroidUtilities.dp(15));

        iv = new ImageView(context) {
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                if (resId != 0) {
                    @SuppressLint("DrawAllocation")
                    PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon), PorterDuff.Mode.SRC_ATOP);
                    @SuppressLint("DrawAllocation")
                    Rect boundRect = new Rect(0, 0,getMeasuredWidth(),getMeasuredHeight());
                    Drawable icon = getResources().getDrawable(resId);
                    icon.setBounds(boundRect);
                    icon.setAlpha(255);
                    icon.setColorFilter(colorFilter);
                    icon.draw(canvas);
                }

            }
        };
        iv.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        addView(iv);
    }

    public void setIcon(int icon) {
        resId = icon;
        iv.invalidate();
    }
}
