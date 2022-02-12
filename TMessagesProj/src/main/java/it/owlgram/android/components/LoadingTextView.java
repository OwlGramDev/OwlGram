package it.owlgram.android.components;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Shader;
import android.os.SystemClock;
import android.text.Layout;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.telegram.messenger.Emoji;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class LoadingTextView extends FrameLayout {
    private final TextView loadingTextView;
    public TextView textView;

    private CharSequence loadingString;
    private final Paint loadingPaint = new Paint();
    private final Paint loadingIdlePaint = new Paint();
    private final Path loadingPath = new Path();
    private final RectF fetchedPathRect = new RectF();
    public int padHorz, padVert;
    private final Path fetchPath = new Path() {
        private boolean got = false;

        @Override
        public void reset() {
            super.reset();
            got = false;
        }

        @Override
        public void addRect(float left, float top, float right, float bottom, @NonNull Direction dir) {
            if (!got) {
                fetchedPathRect.set(
                        left - padHorz,
                        top - padVert,
                        right + padHorz,
                        bottom + padVert
                );
                got = true;
            }
        }
    };

    public void resize() {
        post(() -> {
            loadingTextView.forceLayout();
            textView.forceLayout();
            updateLoadingLayout();
            updateTextLayout();
            updateHeight();
        });
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    private final boolean animateWidth;
    private final boolean scaleFromZero;
    private final long scaleFromZeroStart;
    private final long scaleFromZeroDuration = 220L;
    public LoadingTextView(Context context, int padHorz, int padVert, CharSequence loadingString, boolean scaleFromZero, boolean animateWidth) {
        super(context);

        this.animateWidth = animateWidth;
        this.scaleFromZero = scaleFromZero;
        this.scaleFromZeroStart = SystemClock.elapsedRealtime();

        this.padHorz = padHorz;
        this.padVert = padVert;
        setPadding(padHorz, padVert, padHorz, padVert);

        loadingT = 0f;
        loadingTextView = new TextView(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(
                        animateWidth ?
                                MeasureSpec.makeMeasureSpec(
                                        999999,
                                        MeasureSpec.AT_MOST
                                ) : widthMeasureSpec,
                        MeasureSpec.makeMeasureSpec(
                                MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST ? 999999 : MeasureSpec.getSize(heightMeasureSpec),
                                MeasureSpec.getMode(heightMeasureSpec)
                        )
                );
            }
        };
        loadingTextView.setText(this.loadingString = loadingString);
        loadingTextView.setVisibility(INVISIBLE);
        loadingTextView.measure(MeasureSpec.makeMeasureSpec(animateWidth ? 999999 : getWidth(), MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(9999999, MeasureSpec.AT_MOST));
        addView(loadingTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP));

        textView = new TextView(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(
                        animateWidth ?
                                MeasureSpec.makeMeasureSpec(
                                        999999,
                                        MeasureSpec.AT_MOST
                                ) : widthMeasureSpec,
                        MeasureSpec.makeMeasureSpec(
                                MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST ? 999999 : MeasureSpec.getSize(heightMeasureSpec),
                                MeasureSpec.getMode(heightMeasureSpec)
                        )
                );
            }
        };
        textView.setText("");
        textView.setVisibility(INVISIBLE);
        textView.measure(MeasureSpec.makeMeasureSpec(animateWidth ? 999999 : getWidth(), MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(9999999, MeasureSpec.AT_MOST));
        addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP));

        int c1 = Theme.getColor(Theme.key_dialogBackground),
                c2 = Theme.getColor(Theme.key_dialogBackgroundGray);
        LinearGradient gradient = new LinearGradient(0, 0, gradientWidth, 0, new int[]{ c1, c2, c1 }, new float[] { 0, 0.67f, 1f }, Shader.TileMode.REPEAT);
        loadingPaint.setShader(gradient);
        loadingIdlePaint.setColor(c2);

        setWillNotDraw(false);
        setClipChildren(false);

        updateLoadingLayout();
    }

    protected void scrollToBottom() {}
    protected void onLoadEnd() {}
    protected void onLoadStart() {}
    protected void onLoadAnimation(float t) {}

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateHeight();
    }

    public void updateHeight() {
        int loadingHeight = loadingTextView.getMeasuredHeight(),
                textHeight = textView == null ? loadingHeight : textView.getMeasuredHeight();
        float scaleFromZeroT = scaleFromZero ? Math.max(Math.min((float) (SystemClock.elapsedRealtime() - scaleFromZeroStart) / (float) scaleFromZeroDuration, 1f), 0f) : 1f;
        int height = (
                (int) (
                        (
                                padVert * 2 +
                                        loadingHeight + (
                                        textHeight -
                                                loadingHeight
                                ) * loadingT
                        ) * scaleFromZeroT
                )
        );
        ViewGroup.LayoutParams params = getLayoutParams();
        if (params == null) {
            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        }
        params.height = height;

        if (animateWidth) {
            int loadingWidth = loadingTextView.getMeasuredWidth() + padHorz * 2;
            int textWidth = (textView == null || textView.getMeasuredWidth() <= 0 ? loadingTextView.getMeasuredWidth() : textView.getMeasuredWidth()) + padHorz * 2;
            params.width = (int) ((loadingWidth + (textWidth - loadingWidth) * loadingT) * scaleFromZeroT);
        }

        this.setLayoutParams(params);
    }

    private final float gradientWidth = dp(350f);
    private void updateLoadingLayout() {
        float textWidth = loadingTextView.getMeasuredWidth();
        if (textWidth > 0) {
            Layout loadingLayout = loadingTextView.getLayout();
            if (loadingLayout != null) {
                for (int i = 0; i < loadingLayout.getLineCount(); ++i) {
                    int start = loadingLayout.getLineStart(i), end = loadingLayout.getLineEnd(i);
                    if (start + 1 == end)
                        continue;
                    loadingLayout.getSelectionPath(start, end, fetchPath);
                    loadingPath.addRoundRect(fetchedPathRect, dp(4), dp(4), Path.Direction.CW);
                }
            }
            updateHeight();
        }

        if (!loaded && loadingAnimator == null) {
            loadingAnimator = ValueAnimator.ofFloat(0f, 1f);
            loadingAnimator.addUpdateListener(a -> {
                loadingT = 0f;
                if (scaleFromZero && SystemClock.elapsedRealtime() < scaleFromZeroStart + scaleFromZeroDuration + 25)
                    updateHeight();
                invalidate();
            });
            loadingAnimator.setDuration(Long.MAX_VALUE);
            loadingAnimator.start();
        }
    }

    private void updateTextLayout() {
        textView.setWidth(getWidth() - padHorz * 2);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        updateLoadingLayout();
    }

    public boolean loading = true;
    public boolean loaded = false;
    private float loadingT;
    private ValueAnimator loadingAnimator = null;

    public void setEllipsizeNull() {
        loadingTextView.setEllipsize(null);
        if (textView != null)
            textView.setEllipsize(null);
    }

    public void setSingleLine(boolean singleLine) {
        loadingTextView.setSingleLine(singleLine);
        if (textView != null)
            textView.setSingleLine(singleLine);
    }

    public void setLines(int lines) {
        loadingTextView.setLines(lines);
        if (textView != null)
            textView.setLines(lines);
    }
    public void setGravity(int gravity) {
        loadingTextView.setGravity(gravity);
        if (textView != null)
            textView.setGravity(gravity);
    }
    public void setMaxLines(int maxLines) {
        loadingTextView.setMaxLines(maxLines);
        if (textView != null)
            textView.setMaxLines(maxLines);
    }
    private boolean showLoadingTextValue = true;
    public void showLoadingText(boolean show) {
        showLoadingTextValue = show;
    }

    public void setTextColor(int textColor) {
        loadingTextView.setTextColor(textColor);
        if (textView != null)
            textView.setTextColor(textColor);
    }

    public void setTextSize(int size) {
        loadingTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        if (textView != null)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        loadingTextView.setText(loadingString = Emoji.replaceEmoji(loadingString, loadingTextView.getPaint().getFontMetricsInt(), dp(14), false));
        loadingTextView.measure(MeasureSpec.makeMeasureSpec(animateWidth ? 999999 : getWidth(), MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(9999999, MeasureSpec.AT_MOST));

        if (textView != null) {
            textView.setText(Emoji.replaceEmoji(textView.getText(), textView.getPaint().getFontMetricsInt(), dp(14), false));
            textView.measure(MeasureSpec.makeMeasureSpec(animateWidth ? 999999 : getWidth(), MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(9999999, MeasureSpec.AT_MOST));
        }
        updateLoadingLayout();
    }
    public int multAlpha(int color, float mult) {
        return (color & 0x00ffffff) | ((int) ((color >> 24 & 0xff) * mult) << 24);
    }
    private ValueAnimator animator = null;
    public void setText(CharSequence text) {
        textView.setText(text);
        textView.measure(MeasureSpec.makeMeasureSpec(animateWidth ? 999999 : getWidth(), MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(9999999, MeasureSpec.AT_MOST));
        textView.layout(getLeft() + padHorz, getTop() + padVert, getLeft() + padHorz + textView.getMeasuredWidth(), getTop() + padVert + textView.getMeasuredHeight());

        if (!loaded) {
            loaded = true;
            loadingT = 0f;
            if (loadingAnimator != null) {
                loadingAnimator.cancel();
                loadingAnimator = null;
            }
            if (animator != null)
                animator.cancel();
            animator = ValueAnimator.ofFloat(0f, 1f);
            animator.addUpdateListener(a -> {
                loadingT = (float) a.getAnimatedValue();
                onLoadAnimation(loadingT);
                updateHeight();
                invalidate();
            });
            onLoadStart();
            animator.addListener(new Animator.AnimatorListener() {
                @Override public void onAnimationEnd(Animator animator) {
                    onLoadEnd();
                }
                @Override public void onAnimationCancel(Animator animator) {
                    onLoadEnd();
                }
                @Override public void onAnimationRepeat(Animator animator) {}
                @Override public void onAnimationStart(Animator animator) {}
            });
//                animator.setInterpolator(CubicBezierInterpolator.EASE_BOTH);
            animator.setDuration(300);
            animator.start();
        } else
            updateHeight();
    }

    private final long start = SystemClock.elapsedRealtime();
    private final Path shadePath = new Path();
    private final Path tempPath = new Path();
    private final Path inPath = new Path();
    private final RectF rect = new RectF();
    @Override
    protected void onDraw(Canvas canvas) {
        float w = getWidth(), h = getHeight();

        float cx = LocaleController.isRTL ? Math.max(w / 2f, w - 8f) : Math.min(w / 2f, 8f),
                cy = Math.min(h / 2f, 8f),
                R = (float) Math.sqrt(Math.max(
                        Math.max(cx*cx + cy*cy, (w-cx)*(w-cx) + cy*cy),
                        Math.max(cx*cx + (h-cy)*(h-cy), (w-cx)*(w-cx) + (h-cy)*(h-cy))
                )),
                r = loadingT * R;
        inPath.reset();
        inPath.addCircle(cx, cy, r, Path.Direction.CW);

        canvas.save();
        canvas.clipPath(inPath, Region.Op.DIFFERENCE);

        loadingPaint.setAlpha((int) ((1f - loadingT) * 255));
        float dx = gradientWidth - (((SystemClock.elapsedRealtime() - start) / 1000f * gradientWidth) % gradientWidth);
        shadePath.reset();
        shadePath.addRect(0, 0, w, h, Path.Direction.CW);

        canvas.translate(padHorz, padVert);
        canvas.clipPath(loadingPath);
        canvas.translate(-padHorz, -padVert);
        canvas.translate(-dx, 0);
        shadePath.offset(dx, 0f, tempPath);
        canvas.drawPath(tempPath, loading ? loadingPaint : loadingIdlePaint);
        canvas.translate(dx, 0);
        canvas.restore();

        canvas.save();
        rect.set(0, 0, w, h);
        canvas.clipPath(inPath, Region.Op.DIFFERENCE);
        canvas.translate(padHorz, padVert);
        canvas.clipPath(loadingPath);
        canvas.saveLayerAlpha(rect, (int) (255 * (showLoadingTextValue ? 0.08f : 0f)), Canvas.ALL_SAVE_FLAG);
        loadingTextView.draw(canvas);
        canvas.restore();
        canvas.restore();

        if (textView != null) {
            canvas.save();
            canvas.clipPath(inPath);
            canvas.translate(padHorz, padVert);
            canvas.saveLayerAlpha(rect, (int) (255 * loadingT), Canvas.ALL_SAVE_FLAG);
            textView.draw(canvas);
            if (loadingT < 1f)
                canvas.restore();
            canvas.restore();
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        return false;
    }
}