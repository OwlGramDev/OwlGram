package it.owlgram.android.components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import it.owlgram.android.helpers.IconsHelper;

public class AppIconCell extends LinearLayout {
    private final float STROKE = AndroidUtilities.dp(2);
    private final float INNER_RECT_SPACE = STROKE + AndroidUtilities.dp(2);
    private ValueAnimator strokeAlphaAnimator;
    private float selectionProgress;
    private float changeIconProgress = 1f;
    boolean isSelected;
    private IconsHelper.Icons iconDescriptor;
    private final TextView nameTextView;
    private final FrameLayout frameLayout;
    private final IconStyleDrawable themeDrawable = new IconStyleDrawable();
    private final RectF rectF = new RectF();

    public AppIconCell(Context context) {
        super(context);
        setGravity(Gravity.CENTER);
        setOrientation(VERTICAL);
        frameLayout = new FrameLayout(context) {
            @Override
            protected void dispatchDraw(Canvas canvas) {
                if (changeIconProgress != 0) {
                    themeDrawable.drawBackground(canvas);
                }
                if (changeIconProgress != 0) {
                    themeDrawable.draw(canvas, changeIconProgress);
                }
                if (changeIconProgress != 1f) {
                    changeIconProgress += 16 / 150f;
                    if (changeIconProgress >= 1f) {
                        changeIconProgress = 1f;
                    }
                    invalidate();
                }
                super.dispatchDraw(canvas);
            }
        };
        addView(frameLayout, LayoutHelper.createLinear(70, 70, 5, 7, 5, 0));

        nameTextView = new TextView(context);
        nameTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        nameTextView.setMaxLines(2);
        nameTextView.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        nameTextView.setLines(2);
        nameTextView.setEllipsize(TextUtils.TruncateAt.END);
        addView(nameTextView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 6, 7, 6, 0));
    }

    public void setIconDescription(IconsHelper.Icons iconDescriptor) {
        this.iconDescriptor = iconDescriptor;
        nameTextView.setText(iconDescriptor.getTitle());
        invalidate();
    }

    public int getItemID() {
        return iconDescriptor.ordinal();
    }

    private class IconStyleDrawable {
        private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        IconStyleDrawable() {
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeWidth(AndroidUtilities.dp(2));
        }

        public void drawBackground(Canvas canvas) {
            canvas.save();
            if (iconDescriptor != null) {
                Path clipPath = new Path();
                int space = Math.round(INNER_RECT_SPACE);
                int w = frameLayout.getMeasuredWidth() - space;
                int h = frameLayout.getMeasuredHeight() - space;
                clipPath.addRoundRect(new RectF(space, space, w, h), w >> 1, w >> 1, Path.Direction.CW);
                canvas.clipPath(clipPath);
                Drawable d = getResources().getDrawable(iconDescriptor.getIcon());
                d.setBounds(space, space, w, h);
                d.draw(canvas);
            }
            canvas.restore();
        }

        public void draw(Canvas canvas, float alpha) {
            if (isSelected || strokeAlphaAnimator != null) {
                int w = frameLayout.getMeasuredWidth();
                int h = frameLayout.getMeasuredHeight();
                int radius = w >> 1;
                strokePaint.setColor(Theme.getColor(Theme.key_dialogTextBlue));
                strokePaint.setAlpha((int) (selectionProgress * alpha * 255));
                float rectSpace = strokePaint.getStrokeWidth() * 0.5f + AndroidUtilities.dp(4) * (1f - selectionProgress);
                rectF.set(rectSpace, rectSpace, w - rectSpace, h - rectSpace);
                canvas.drawRoundRect(rectF, radius, radius, strokePaint);
            }
        }
    }

    public void setSelected(boolean selected, boolean animated) {
        if (!animated) {
            if (strokeAlphaAnimator != null) {
                strokeAlphaAnimator.cancel();
            }
            isSelected = selected;
            selectionProgress = selected ? 1f : 0;
            frameLayout.invalidate();
            return;
        }
        if (isSelected != selected) {
            float currentProgress = selectionProgress;
            if (strokeAlphaAnimator != null) {
                strokeAlphaAnimator.cancel();
            }
            strokeAlphaAnimator = ValueAnimator.ofFloat(currentProgress, selected ? 1f : 0);
            strokeAlphaAnimator.addUpdateListener(valueAnimator -> {
                selectionProgress = (float) valueAnimator.getAnimatedValue();
                frameLayout.invalidate();
            });
            strokeAlphaAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    selectionProgress = selected ? 1f : 0;
                    frameLayout.invalidate();
                }
            });
            strokeAlphaAnimator.setDuration(250);
            strokeAlphaAnimator.start();
        }
        isSelected = selected;
    }
}
