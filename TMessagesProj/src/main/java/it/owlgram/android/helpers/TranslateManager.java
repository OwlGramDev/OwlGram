package it.owlgram.android.helpers;

import static org.telegram.messenger.AndroidUtilities.displayMetrics;
import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.lerp;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LanguageDetector;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.URLSpanNoUnderline;

import java.util.ArrayList;
import java.util.Locale;

import it.owlgram.android.OwlConfig;
import it.owlgram.android.components.LoadingTextView;
import it.owlgram.android.translator.BaseTranslator;
import it.owlgram.android.translator.Translator;

public class TranslateManager extends Dialog {
    private final FrameLayout bulletinContainer;
    private final FrameLayout contentView;
    private final FrameLayout container;
    private final TextView titleView;
    private final LinearLayout subtitleView;
    private final LoadingTextView subtitleFromView;
    private final ImageView backButton;
    private final FrameLayout header;
    private final FrameLayout headerShadowView;
    private final NestedScrollView scrollView;
    private final LinearLayout textsView;
    private final FrameLayout buttonView;
    private final FrameLayout buttonShadowView;
    private final TextView allTextsView;
    private final FrameLayout textsContainerView;
    private final FrameLayout allTextsContainer;

    private final FrameLayout.LayoutParams titleLayout;
    private final FrameLayout.LayoutParams subtitleLayout;
    private final FrameLayout.LayoutParams headerLayout;
    private final FrameLayout.LayoutParams scrollViewLayout;

    private int blockIndex = 0;
    private final ArrayList<CharSequence> textBlocks;

    private float containerOpenAnimationT = 0f;

    private void openAnimation(float t) {
        t = Math.min(Math.max(t, 0f), 1f);
        if (containerOpenAnimationT == t)
            return;
        containerOpenAnimationT = t;

        titleView.setScaleX(lerp(1f, 0.9473f, t));
        titleView.setScaleY(lerp(1f, 0.9473f, t));
        titleLayout.setMargins(
                dp(lerp(22, 72, t)),
                dp(lerp(22, 8, t)),
                titleLayout.rightMargin,
                titleLayout.bottomMargin
        );
        titleView.setLayoutParams(titleLayout);

        subtitleLayout.setMargins(
                dp(lerp(22, 72, t)) - subtitleFromView.padHorz,
                dp(lerp(47, 30, t)) - subtitleFromView.padVert,
                subtitleLayout.rightMargin,
                subtitleLayout.bottomMargin
        );
        subtitleView.setLayoutParams(subtitleLayout);
        backButton.setAlpha(t);
        backButton.setScaleX(.75f + .25f * t);
        backButton.setScaleY(.75f + .25f * t);
        backButton.setClickable(t > .5f);
        headerShadowView.setAlpha(scrollView.getScrollY() > 0 ? 1f : t);

        headerLayout.height = (int) lerp(dp(70), dp(56), t);
        header.setLayoutParams(headerLayout);
        scrollViewLayout.setMargins(
                scrollViewLayout.leftMargin,
                (int) lerp(dp(70), dp(56), t),
                scrollViewLayout.rightMargin,
                scrollViewLayout.bottomMargin
        );
        scrollView.setLayoutParams(scrollViewLayout);
        container.requestLayout();
    }


    private boolean openAnimationToAnimatorPriority = false;
    private ValueAnimator openAnimationToAnimator = null;
    private void openAnimationTo(float to, boolean priority) {
        if (openAnimationToAnimatorPriority && !priority)
            return;
        openAnimationToAnimatorPriority = priority;
        to = Math.min(Math.max(to, 0), 1);
        if (openAnimationToAnimator != null)
            openAnimationToAnimator.cancel();
        openAnimationToAnimator = ValueAnimator.ofFloat(containerOpenAnimationT, to);
        openAnimationToAnimator.addUpdateListener(a -> openAnimation((float) a.getAnimatedValue()));
        openAnimationToAnimator.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animator) { }
            @Override public void onAnimationRepeat(Animator animator) { }

            @Override
            public void onAnimationEnd(Animator animator) {
                openAnimationToAnimatorPriority = false;
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                openAnimationToAnimatorPriority = false;
            }
        });
        openAnimationToAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT);
        openAnimationToAnimator.setDuration(220);
        openAnimationToAnimator.start();
        if (to >= .5 && blockIndex <= 1)
            fetchNext();
    }

    private int minHeight() {
        return (textsView == null ? 0 : textsView.getMeasuredHeight()) + dp(
                66 + // header
                        1 +  // button separator
                        16 + // button top padding
                        48 + // button
                        16   // button bottom padding
        );
    }
    private boolean canExpand() {
        return (
                textsView.getChildCount() < textBlocks.size() ||
                        minHeight() >= (AndroidUtilities.displayMetrics.heightPixels * heightMaxPercent)
        ) && textsView.getChildCount() > 0 && ((LoadingTextView) textsView.getChildAt(0)).loaded;
    }
    private void updateCanExpand() {
        boolean canExpand = canExpand();
        if (containerOpenAnimationT > 0f && !canExpand)
            openAnimationTo(0f, false);

        buttonShadowView.animate().alpha(canExpand ? 1f : 0f).setDuration((long) (Math.abs(buttonShadowView.getAlpha() - (canExpand ? 1f : 0f)) * 220)).start();
    }

    public interface OnLinkPress {
        void run(URLSpan urlSpan);
    }

    private final int textPadHorz, textPadVert;

    private boolean allowScroll = true;
    private String fromLanguage;
    private final String toLanguage;
    private final BaseFragment fragment;
    private final boolean noForwards;
    private final OnLinkPress onLinkPress;
    public TranslateManager(BaseFragment fragment, Context context, String fromLanguage, CharSequence text, boolean noForwards, OnLinkPress onLinkPress) {
        super(context, R.style.TransparentDialog);

        this.onLinkPress = onLinkPress;
        this.noForwards = noForwards;
        this.fragment = fragment;
        this.fromLanguage = fromLanguage != null && fromLanguage.equals("und") ? "auto" : fromLanguage;
        this.toLanguage = Translator.getCurrentTranslator().getCurrentTargetLanguage().split("-")[0];
        this.textBlocks = cutInBlocks(text);

        if (Build.VERSION.SDK_INT >= 30) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        } else if (Build.VERSION.SDK_INT >= 21) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        contentView = new FrameLayout(context);
        contentView.setBackground(backDrawable);
        contentView.setClipChildren(false);
        contentView.setClipToPadding(false);
        if (Build.VERSION.SDK_INT >= 21) {
            contentView.setFitsSystemWindows(true);
            if (Build.VERSION.SDK_INT >= 30) {
                contentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |  View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            } else {
                contentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }
        }
        Paint containerPaint = new Paint();
        containerPaint.setColor(Theme.getColor(Theme.key_dialogBackground));
        containerPaint.setShadowLayer(dp(2), 0, dp(-0.66f), 0x1e000000);
        container = new FrameLayout(context) {
            private int contentHeight = Integer.MAX_VALUE;
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int fullWidth = MeasureSpec.getSize(widthMeasureSpec);
                int minHeight = (int) (AndroidUtilities.displayMetrics.heightPixels * heightMaxPercent);
                int fromHeight = Math.min(minHeight, minHeight());
                int height = (int) (fromHeight + (AndroidUtilities.displayMetrics.heightPixels - fromHeight) * containerOpenAnimationT);
                updateCanExpand();
                super.onMeasure(
                        MeasureSpec.makeMeasureSpec(
                                (int) Math.max(fullWidth * 0.8f, Math.min(dp(480), fullWidth)),
                                MeasureSpec.getMode(widthMeasureSpec)
                        ),
                        MeasureSpec.makeMeasureSpec(
                                height,
                                MeasureSpec.EXACTLY
                        )
                );
            }

            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                super.onLayout(changed, left, top, right, bottom);
                contentHeight = Math.min(contentHeight, bottom - top);
            }

            private final RectF containerRect = new RectF();

            @Override
            protected void onDraw(Canvas canvas) {
                int w = getWidth(), h = getHeight(), r = dp(12 * (1f - containerOpenAnimationT));
                canvas.clipRect(0, 0, w, h);
                containerRect.set(0, 0, w, h + r);
                canvas.translate(0, (1f - openingT) * h);
                canvas.drawRoundRect(containerRect, r, r, containerPaint);
                super.onDraw(canvas);
            }
        };
        container.setWillNotDraw(false);

        header = new FrameLayout(context);

        titleView = new TextView(context);
        titleView.setPivotX(LocaleController.isRTL ? titleView.getWidth() : 0);
        titleView.setPivotY(0);
        titleView.setLines(1);
        titleView.setText(LocaleController.getString("AutomaticTranslation", R.string.AutomaticTranslation));
        titleView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        titleView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        titleView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp(19));
        header.addView(titleView, titleLayout = LayoutHelper.createFrame(
                LayoutHelper.MATCH_PARENT,
                LayoutHelper.WRAP_CONTENT,
                Gravity.FILL_HORIZONTAL | Gravity.TOP,
                22, 22,22, 0
        ));
        titleView.post(() -> titleView.setPivotX(LocaleController.isRTL ? titleView.getWidth() : 0));
        subtitleView = new LinearLayout(context);
        subtitleView.setOrientation(LinearLayout.HORIZONTAL);
        if (Build.VERSION.SDK_INT >= 17)
            subtitleView.setLayoutDirection(LocaleController.isRTL ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
        subtitleView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);

        textPadHorz = dp(6);
        textPadVert = dp(1.5f);

        String fromLanguageName = languageName(fromLanguage);
        subtitleFromView = new LoadingTextView(context, dp(6), dp(1.5f), fromLanguageName == null ? languageName(toLanguage) : fromLanguageName, false, true) {
            @Override
            protected void onLoadAnimation(float t) {
                MarginLayoutParams lp = (MarginLayoutParams) subtitleFromView.getLayoutParams();
                if (LocaleController.isRTL) {
                    lp.leftMargin = dp(2f - t * 6f);
                } else {
                    lp.rightMargin = dp(2f - t * 6f);
                }
                subtitleFromView.setLayoutParams(lp);
            }
        };
        subtitleFromView.showLoadingText(false);
        subtitleFromView.setLines(1);
        subtitleFromView.setTextColor(Theme.getColor(Theme.key_player_actionBarSubtitle));
        subtitleFromView.setTextSize(dp(14));
        if (fromLanguageName != null)
            subtitleFromView.setText(fromLanguageName);

        ImageView subtitleArrowView = new ImageView(context);
        subtitleArrowView.setImageResource(R.drawable.search_arrow);
        subtitleArrowView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_player_actionBarSubtitle), PorterDuff.Mode.MULTIPLY));
        if (LocaleController.isRTL)
            subtitleArrowView.setScaleX(-1f);

        TextView subtitleToView = new TextView(context);
        subtitleToView.setLines(1);
        subtitleToView.setTextColor(Theme.getColor(Theme.key_player_actionBarSubtitle));
        subtitleToView.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp(14));
        subtitleToView.setText(languageName(toLanguage));

        if (LocaleController.isRTL) {
            subtitleView.setPadding(subtitleFromView.padHorz, 0, textPadHorz - subtitleFromView.padHorz, 0);
            subtitleView.addView(subtitleToView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL));
            subtitleView.addView(subtitleArrowView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 3, 1, 0, 0));
            subtitleView.addView(subtitleFromView, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 2, 0, 0, 0));
        } else {
            subtitleView.setPadding(textPadHorz - subtitleFromView.padHorz, 0, subtitleFromView.padHorz, 0);
            subtitleView.addView(subtitleFromView, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 0, 0, 2, 0));
            subtitleView.addView(subtitleArrowView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 0, 1, 3, 0));
            subtitleView.addView(subtitleToView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL));
        }
        subtitleFromView.updateHeight();

        header.addView(subtitleView, subtitleLayout = LayoutHelper.createFrame(
                LayoutHelper.MATCH_PARENT,
                LayoutHelper.WRAP_CONTENT,
                Gravity.TOP | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT),
                22 - textPadHorz / AndroidUtilities.density,
                47 - textPadVert / AndroidUtilities.density,
                22 - textPadHorz / AndroidUtilities.density,
                0
        ));

        backButton = new ImageView(context);
        backButton.setImageResource(R.drawable.ic_ab_back);
        backButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextBlack), PorterDuff.Mode.MULTIPLY));
        backButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        backButton.setPadding(AndroidUtilities.dp(16), 0, AndroidUtilities.dp(16), 0);
        backButton.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_dialogButtonSelector)));
        backButton.setClickable(false);
        backButton.setAlpha(0f);
        backButton.setOnClickListener(e -> dismiss());
        header.addView(backButton, LayoutHelper.createFrame(56, 56, Gravity.LEFT | Gravity.CENTER_HORIZONTAL));

        headerShadowView = new FrameLayout(context);
        headerShadowView.setBackgroundColor(Theme.getColor(Theme.key_dialogShadowLine));
        headerShadowView.setAlpha(0);
        header.addView(headerShadowView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 1, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL));

        header.setClipChildren(false);
        container.addView(header, headerLayout = LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 70, Gravity.FILL_HORIZONTAL | Gravity.TOP));

        scrollView = new NestedScrollView(context) {
            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                return allowScroll && containerOpenAnimationT >= 1f && canExpand() && super.onInterceptTouchEvent(ev);
            }

            @Override
            public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
                super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
            }

            @Override
            protected void onScrollChanged(int l, int t, int oldl, int oldt) {
                super.onScrollChanged(l, t, oldl, oldt);
                if (scrollAtBottom() && fetchNext()) {
                    openAnimationTo(1f, true);
                }
            }
        };
        scrollView.setClipChildren(true);

        textsView = new LinearLayout(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(9999999, MeasureSpec.AT_MOST));
            }
        };
        textsView.setOrientation(LinearLayout.VERTICAL);
        textsView.setPadding(dp(22) - textPadHorz, dp(12) - textPadVert, dp(22) - textPadHorz, dp(12) - textPadVert);

        Paint selectionPaint = new Paint();
        selectionPaint.setColor(Theme.getColor(Theme.key_chat_inTextSelectionHighlight));
        allTextsContainer = new FrameLayout(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(textsContainerView.getMeasuredHeight(), MeasureSpec.AT_MOST));
            }
        };
        allTextsContainer.setClipChildren(false);
        allTextsContainer.setClipToPadding(false);
        allTextsContainer.setPadding(dp(22), dp(12), dp(22), dp(12));

        allTextsView = new TextView(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(99999999, MeasureSpec.AT_MOST));
            }
            private Paint pressedLinkPaint = null;
            private final Path pressedLinkPath = new Path() {
                private final RectF rectF = new RectF();
                @Override
                public void addRect(float left, float top, float right, float bottom, @NonNull Direction dir) {
                    rectF.set(left - (textPadHorz >> 1), top - textPadVert, right + (textPadHorz >> 1), bottom + textPadVert);
                    addRoundRect(rectF, dp(4), dp(4), Direction.CW);
                }
            };
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                if (pressedLink != null) {
                    try {
                        Layout layout = getLayout();
                        int start = allTexts.getSpanStart(pressedLink);
                        int end = allTexts.getSpanEnd(pressedLink);
                        layout.getSelectionPath(start, end, pressedLinkPath);

                        if (pressedLinkPaint == null) {
                            pressedLinkPaint = new Paint();
                            pressedLinkPaint.setColor(Theme.getColor(Theme.key_chat_linkSelectBackground));
                        }
                        canvas.drawPath(pressedLinkPath, pressedLinkPaint);
                    } catch (Exception ignored) {}
                }
            }

            @Override
            public boolean onTextContextMenuItem(int id) {
                if (id == android.R.id.copy && isFocused()) {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) ApplicationLoader.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText(
                            "label",
                            getText().subSequence(
                                    Math.max(0, Math.min(getSelectionStart(), getSelectionEnd())),
                                    Math.max(0, Math.max(getSelectionStart(), getSelectionEnd()))
                            )
                    );
                    clipboard.setPrimaryClip(clip);
                    BulletinFactory.of(bulletinContainer, null).createCopyBulletin(LocaleController.getString("TextCopied", R.string.TextCopied)).show();
                    clearFocus();
                    return true;
                } else
                    return super.onTextContextMenuItem(id);
            }
        };
        allTextsView.setTextColor(0x00000000);
        allTextsView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        allTextsView.setTextIsSelectable(!noForwards);
        allTextsView.setHighlightColor(Theme.getColor(Theme.key_chat_inTextSelectionHighlight));
        int handleColor = Theme.getColor(Theme.key_chat_TextSelectionCursor);
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                Drawable left = allTextsView.getTextSelectHandleLeft();
                left.setColorFilter(handleColor, PorterDuff.Mode.SRC_IN);
                allTextsView.setTextSelectHandleLeft(left);

                Drawable right = allTextsView.getTextSelectHandleRight();
                right.setColorFilter(handleColor, PorterDuff.Mode.SRC_IN);
                allTextsView.setTextSelectHandleRight(right);
            }
        } catch (Exception ignored) {}
        allTextsView.setMovementMethod(new LinkMovementMethod());
        allTextsContainer.addView(allTextsView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        textsContainerView = new FrameLayout(context);
        textsContainerView.addView(allTextsContainer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        textsContainerView.addView(textsView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        scrollView.addView(textsContainerView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 1f));

        container.addView(scrollView, scrollViewLayout = LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.FILL, 0, 70, 0, 81));

//        translateMoreView.bringToFront();
        fetchNext();

        buttonShadowView = new FrameLayout(context);
        buttonShadowView.setBackgroundColor(Theme.getColor(Theme.key_dialogShadowLine));
        container.addView(buttonShadowView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 1, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0, 0, 80));

        TextView buttonTextView = new TextView(context);
        buttonTextView.setLines(1);
        buttonTextView.setSingleLine(true);
        buttonTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        buttonTextView.setEllipsize(TextUtils.TruncateAt.END);
        buttonTextView.setGravity(Gravity.CENTER);
        buttonTextView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        buttonTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        buttonTextView.setText(LocaleController.getString("CloseTranslation", R.string.CloseTranslation));

        buttonView = new FrameLayout(context);
        buttonView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(4), Theme.getColor(Theme.key_featuredStickers_addButton), Theme.getColor(Theme.key_featuredStickers_addButtonPressed)));
        buttonView.addView(buttonTextView);
        buttonView.setOnClickListener(e -> dismiss());

        container.addView(buttonView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM, 16, 16, 16, 16));
        contentView.addView(container, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL));

        bulletinContainer = new FrameLayout(context);
        contentView.addView(bulletinContainer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.FILL, 0, 0, 0, 81));
    }

    private boolean scrollAtBottom() {
        View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
        int bottom = view.getBottom();
        if (textsView.getChildCount() > 0) {
            view = textsView.getChildAt(textsView.getChildCount() - 1);
            if (view instanceof LoadingTextView && !((LoadingTextView) view).loaded)
                bottom = view.getTop();
        }
        int diff = (bottom - (scrollView.getHeight() + scrollView.getScrollY()));
        return diff <= textsContainerView.getPaddingBottom();
    }

    private void setScrollY(float t) {
        openAnimation(t);
        openingT = Math.max(Math.min(1f + t, 1), 0);
        backDrawable.setAlpha((int) (openingT * 51));
        container.invalidate();
        bulletinContainer.setTranslationY((1f - openingT) * Math.min(minHeight(), displayMetrics.heightPixels * heightMaxPercent));
    }
    private void scrollYTo(float t) {
        openAnimationTo(t, false);
        openTo(1f + t, false);
    }
    private float fromScrollY = 0;
    private float getScrollY() {
        return Math.max(Math.min(containerOpenAnimationT - (1 - openingT), 1), 0);
    }

    private boolean hasSelection() {
        return allTextsView.hasSelection();
    }

    private final Rect containerRect = new Rect();
    private final Rect textRect = new Rect();
    private final Rect buttonRect = new Rect();
    private final Rect backRect = new Rect();
    private final Rect scrollRect = new Rect();
    private float fromY = 0;
    private boolean pressedOutside = false;
    private boolean maybeScrolling = false;
    private boolean scrolling = false;
    private boolean fromScrollRect = false;
    private float fromScrollViewY = 0;
    private Spannable allTexts = null;
    private ClickableSpan pressedLink;
    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        try {
            float x = event.getX();
            float y = event.getY();
//            container.invalidate();

            container.getGlobalVisibleRect(containerRect);
            if (!containerRect.contains((int) x, (int) y)) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    pressedOutside = true;
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (pressedOutside) {
                        pressedOutside = false;
                        dismiss();
                        return true;
                    }
                }
            }

            try {
                allTextsContainer.getGlobalVisibleRect(textRect);
                if (textRect.contains((int) x, (int) y) && !scrolling) {
                    Layout allTextsLayout = allTextsView.getLayout();
                    int tx = (int) (x - allTextsView.getLeft() - container.getLeft()),
                            ty = (int) (y - allTextsView.getTop() - container.getTop() - scrollView.getTop() + scrollView.getScrollY());
                    final int line = allTextsLayout.getLineForVertical(ty);
                    final int off = allTextsLayout.getOffsetForHorizontal(line, tx);

                    final float left = allTextsLayout.getLineLeft(line);
                    if (allTexts != null && left <= tx && left + allTextsLayout.getLineWidth(line) >= tx) {
                        ClickableSpan[] links = allTexts.getSpans(off, off, ClickableSpan.class);
                        if (links != null && links.length >= 1) {
                            if (event.getAction() == MotionEvent.ACTION_UP && pressedLink == links[0]) {
                                pressedLink.onClick(allTextsView);
                                pressedLink = null;
                                allTextsView.setTextIsSelectable(!noForwards);
                            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                pressedLink = links[0];
                            }
                            allTextsView.invalidate();
                            //                    return super.dispatchTouchEvent(event) || true;
                            return true;
                        } else if (pressedLink != null) {
                            allTextsView.invalidate();
                            pressedLink = null;
                        }
                    } else if (pressedLink != null) {
                        allTextsView.invalidate();
                        pressedLink = null;
                    }
                } else if (pressedLink != null) {
                    allTextsView.invalidate();
                    pressedLink = null;
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }

            scrollView.getGlobalVisibleRect(scrollRect);
            backButton.getGlobalVisibleRect(backRect);
            buttonView.getGlobalVisibleRect(buttonRect);
            if (pressedLink == null && !hasSelection()) {
                if (
                        !backRect.contains((int) x, (int) y) &&
                                !buttonRect.contains((int) x, (int) y) &&
                                event.getAction() == MotionEvent.ACTION_DOWN
                ) {
                    fromScrollRect = scrollRect.contains((int) x, (int) y) && (containerOpenAnimationT > 0 || !canExpand());
                    maybeScrolling = true;
                    scrolling = scrollRect.contains((int) x, (int) y) && textsView.getChildCount() > 0 && !((LoadingTextView) textsView.getChildAt(0)).loaded;
                    fromY = y;
                    fromScrollY = getScrollY();
                    fromScrollViewY = scrollView.getScrollY();
                    return true;
                } else if (maybeScrolling && (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_UP)) {
                    float dy = fromY - y;
                    if (fromScrollRect) {
                        dy = -Math.max(0, -(fromScrollViewY + dp(48)) - dy);
                        if (dy < 0) {
                            scrolling = true;
                            allTextsView.setTextIsSelectable(false);
                        }
                    } else if (Math.abs(dy) > dp(4) && !fromScrollRect) {
                        scrolling = true;
                        allTextsView.setTextIsSelectable(false);
                        scrollView.stopNestedScroll();
                        allowScroll = false;
                    }
                    float fullHeight = AndroidUtilities.displayMetrics.heightPixels,
                            minHeight = Math.min(fullHeight, fullHeight * heightMaxPercent);
                    float scrollYPx = minHeight * (1f - -Math.min(Math.max(fromScrollY, -1), 0)) +
                            (fullHeight - minHeight) * Math.min(1, Math.max(fromScrollY, 0)) + dy;
                    float scrollY = scrollYPx > minHeight ? (scrollYPx - minHeight) / (fullHeight - minHeight) : -(1f - scrollYPx / minHeight);
                    if (!canExpand())
                        scrollY = Math.min(scrollY, 0);
                    updateCanExpand();

                    if (scrolling) {
                        setScrollY(scrollY);
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            scrolling = false;
                            allTextsView.setTextIsSelectable(!noForwards);
                            maybeScrolling = false;
                            allowScroll = true;
                            scrollYTo(
                                    Math.abs(dy) > dp(16) ?
                                            Math.round(fromScrollY) + (scrollY > fromScrollY ? 1f : -1f) * (float) Math.ceil(Math.abs(fromScrollY - scrollY)) :
                                            Math.round(fromScrollY)
                            );
                        }
                        return true;
                    }
                }
            }
            if (hasSelection() && maybeScrolling) {
                scrolling = false;
                allTextsView.setTextIsSelectable(!noForwards);
                maybeScrolling = false;
                allowScroll = true;
                scrollYTo(Math.round(fromScrollY));
            }
            return super.dispatchTouchEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
            return super.dispatchTouchEvent(event);
        }
    }

    private LoadingTextView addBlock(CharSequence startText, boolean scaleFromZero) {
        LoadingTextView textView = new LoadingTextView(getContext(), textPadHorz, textPadVert, startText, scaleFromZero, false) {
            @Override
            protected void onLoadStart() {
                allTextsView.clearFocus();
            }
            @Override
            protected void onLoadEnd() {
                scrollView.post(() -> {
                    allTextsView.setText(allTexts);
                    allTextsView.measure(MeasureSpec.makeMeasureSpec(allTextsContainer.getWidth() - allTextsContainer.getPaddingLeft() - allTextsContainer.getPaddingRight(), MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(textsView.getHeight(), MeasureSpec.AT_MOST));
                    allTextsView.layout(
                            allTextsContainer.getLeft() + allTextsContainer.getPaddingLeft(),
                            allTextsContainer.getTop() + allTextsContainer.getPaddingTop(),
                            allTextsContainer.getLeft() + allTextsContainer.getPaddingLeft() + allTextsView.getMeasuredWidth(),
                            allTextsContainer.getTop() + allTextsContainer.getPaddingTop() + allTextsView.getMeasuredHeight()
                    );
                });

                contentView.post(() -> {
                    if (scrollAtBottom())
                        fetchNext();
                });
            }
        };
        textView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        textView.setTextSize(dp(16));
        textView.setTranslationY((textsView.getChildCount()) * (textPadVert * -4f + dp(.48f)));
        textsView.addView(textView, textsView.getChildCount(), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, 0, 0, 0, 0, 0));
        return textView;
    }

    private float openingT = 0f;
    private ValueAnimator openingAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        contentView.setPadding(0, 0, 0, 0);
        setContentView(contentView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        Window window = getWindow();

        window.setWindowAnimations(R.style.DialogNoAnimation);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.dimAmount = 0;
        params.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        params.flags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        if (Build.VERSION.SDK_INT >= 21) {
            params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                    WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;
        }
        params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        window.setAttributes(params);


        container.forceLayout();
    }

    protected ColorDrawable backDrawable = new ColorDrawable(0xff000000) {
        @Override
        public void setAlpha(int alpha) {
            super.setAlpha(alpha);
            container.invalidate();
        }
    };
    @Override
    public void show() {
        super.show();

        openAnimation(0);
        openTo(1, true, true);
    }

    private boolean dismissed = false;
    @Override
    public void dismiss() {
        if (dismissed)
            return;
        dismissed = true;

        openTo(0, true);
    }
    private void openTo(float t, boolean priority) {
        openTo(t, priority, false);
    }

    private final float heightMaxPercent = .85f;

    private boolean fastHide = false;
    private boolean openingAnimatorPriority = false;
    private void openTo(float t, boolean priority, boolean setAfter) {
        final float T = Math.min(Math.max(t, 0), 1);
        if (openingAnimatorPriority && !priority)
            return;
        openingAnimatorPriority = priority;
        if (openingAnimator != null)
            openingAnimator.cancel();
        openingAnimator = ValueAnimator.ofFloat(openingT, T);
        backDrawable.setAlpha((int) (openingT * 51));
        openingAnimator.addUpdateListener(a -> {
            openingT = (float) a.getAnimatedValue();
            container.invalidate();
            backDrawable.setAlpha((int) (openingT * 51));
            bulletinContainer.setTranslationY((1f - openingT) * Math.min(minHeight(), displayMetrics.heightPixels * heightMaxPercent));
        });
        openingAnimator.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationCancel(Animator animator) {
                if (T <= 0f)
                    dismissInternal();
                else if (setAfter) {
                    allTextsView.setTextIsSelectable(!noForwards);
                    allTextsView.invalidate();
                    scrollView.stopNestedScroll();
                    openAnimation(T - 1f);
                }
                openingAnimatorPriority = false;
            }
            @Override public void onAnimationEnd(Animator animator) {
                if (T <= 0f)
                    dismissInternal();
                else if (setAfter) {
                    allTextsView.setTextIsSelectable(!noForwards);
                    allTextsView.invalidate();
                    scrollView.stopNestedScroll();
                    openAnimation(T - 1f);
                }
                openingAnimatorPriority = false;
            }
            @Override public void onAnimationRepeat(Animator animator) { }
            @Override public void onAnimationStart(Animator animator) { }
        });
        openingAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        openingAnimator.setDuration((long) (Math.abs(openingT - T) * (fastHide ? 200 : 380)));
        openingAnimator.setStartDelay(setAfter ? 60 : 0);
        openingAnimator.start();
    }
    public void dismissInternal() {
        try {
            super.dismiss();
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    public String languageName(String language) {
        if (language == null || language.equals("und") || language.equals("auto"))
            return null;
        Locale locale = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? Locale.forLanguageTag(language) : new Locale(language);
        String value;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !TextUtils.isEmpty(locale.getScript())) {
            value = AndroidUtilities.capitalize(locale.getDisplayScript());
        } else {
            value = AndroidUtilities.capitalize(locale.getDisplayName());
        }
        return value;
    }

    public void updateSourceLanguage() {
        if (languageName(fromLanguage) != null) {
            subtitleView.setAlpha(1);
            if (!subtitleFromView.loaded)
                subtitleFromView.setText(languageName(fromLanguage));
        } else if (loaded) {
            subtitleView.animate().alpha(0).setDuration(150).start();
        }
    }

    private ArrayList<CharSequence> cutInBlocks(CharSequence full) {
        ArrayList<CharSequence> blocks = new ArrayList<>();
        if (full == null)
            return blocks;
        while (full.length() > 1024) {
            String maxBlockStr = full.subSequence(0, 1024).toString();
            int n;
            n = maxBlockStr.lastIndexOf("\n\n");
            if (n == -1) n = maxBlockStr.lastIndexOf("\n");
            if (n == -1) n = maxBlockStr.lastIndexOf(". ");
            blocks.add(full.subSequence(0, n + 1));
            full = full.subSequence(n + 1, full.length());
        }
        if (full.length() > 0)
            blocks.add(full);
        return blocks;
    }

    private boolean loading = false;
    private boolean loaded = false;
    private LoadingTextView lastLoadingBlock = null;
    private boolean fetchNext() {
        if (loading)
            return false;
        loading = true;

        if (blockIndex >= textBlocks.size())
            return false;

        CharSequence blockText = textBlocks.get(blockIndex);
        lastLoadingBlock = lastLoadingBlock == null ? addBlock(blockText, blockIndex != 0) : lastLoadingBlock;
        lastLoadingBlock.loading = true;
        Translator.translate(blockText, new Translator.TranslateCallBack() {

            @Override
            public void onSuccess(BaseTranslator.Result result) {
                loaded = true;
                Spannable spannable = new SpannableStringBuilder((String)result.translation);
                try {
                    MessageObject.addUrlsByPattern(false, spannable, false, 0, 0, true);
                    URLSpan[] urlSpans = spannable.getSpans(0, spannable.length(), URLSpan.class);
                    for (URLSpan urlSpan : urlSpans) {
                        int start = spannable.getSpanStart(urlSpan),
                                end = spannable.getSpanEnd(urlSpan);
                        if (start == -1 || end == -1)
                            continue;
                        spannable.removeSpan(urlSpan);
                        spannable.setSpan(
                                new ClickableSpan() {
                                    @Override
                                    public void onClick(@NonNull View view) {
                                        if (onLinkPress != null) {
                                            onLinkPress.run(urlSpan);
                                            fastHide = true;
                                            dismiss();
                                        } else
                                            AlertsCreator.showOpenUrlAlert(fragment, urlSpan.getURL(), false, false);
                                    }

                                    @Override
                                    public void updateDrawState(@NonNull TextPaint ds) {
                                        int alpha = Math.min(ds.getAlpha(), ds.getColor() >> 24 & 0xff);
                                        if (!(urlSpan instanceof URLSpanNoUnderline))
                                            ds.setUnderlineText(true);
                                        ds.setColor(Theme.getColor(Theme.key_dialogTextLink));
                                        ds.setAlpha(alpha);
                                    }
                                },
                                start, end,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        );
                    }

                    AndroidUtilities.addLinks(spannable, Linkify.WEB_URLS);
                    urlSpans = spannable.getSpans(0, spannable.length(), URLSpan.class);
                    for (URLSpan urlSpan : urlSpans) {
                        int start = spannable.getSpanStart(urlSpan),
                                end = spannable.getSpanEnd(urlSpan);
                        if (start == -1 || end == -1)
                            continue;
                        spannable.removeSpan(urlSpan);
                        spannable.setSpan(
                                new ClickableSpan() {
                                    @Override
                                    public void onClick(@NonNull View view) {
                                        AlertsCreator.showOpenUrlAlert(fragment, urlSpan.getURL(), false, false);
                                    }

                                    @Override
                                    public void updateDrawState(@NonNull TextPaint ds) {
                                        int alpha = Math.min(ds.getAlpha(), ds.getColor() >> 24 & 0xff);
                                        if (!(urlSpan instanceof URLSpanNoUnderline))
                                            ds.setUnderlineText(true);
                                        ds.setColor(Theme.getColor(Theme.key_dialogTextLink));
                                        ds.setAlpha(alpha);
                                    }
                                },
                                start, end,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        );
                    }
                    spannable = (Spannable) Emoji.replaceEmoji(spannable, allTextsView.getPaint().getFontMetricsInt(), dp(14), false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                allTexts = new SpannableStringBuilder(allTextsView.getText()).append(blockIndex == 0 ? "" : "\n").append(spannable);
                if (lastLoadingBlock != null) {
                    lastLoadingBlock.setText(spannable);
                    lastLoadingBlock = null;
                }
                fromLanguage = result.sourceLanguage;
                updateSourceLanguage();
                blockIndex++;
                loading = false;
                if (blockIndex < textBlocks.size()) {
                    CharSequence nextTextBlock = textBlocks.get(blockIndex);
                    lastLoadingBlock = addBlock(nextTextBlock, true);
                    lastLoadingBlock.loading = false;
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), LocaleController.getString("TranslationFailedAlert1", R.string.TranslationFailedAlert1), Toast.LENGTH_SHORT).show();
                if (blockIndex == 0)
                    dismiss();
            }
        });
        return true;
    }

    public interface OnTranslationSuccess {
        void run(String translated, String sourceLanguage);
    }
    public interface OnTranslationFail {
        void run(boolean rateLimit);
    }

    public static void translate(CharSequence text, Context context, BaseFragment fragment, OnLinkPress onLinkPress) {
        LanguageDetector.detectLanguage(
                text.toString(),
                str -> {
                    TranslateManager alert = new TranslateManager(fragment, context, str, text, true, onLinkPress);
                    if (fragment != null) {
                        if (fragment.getParentActivity() != null) {
                            fragment.showDialog(alert);
                        }
                    } else {
                        alert.show();
                    }
                },
                e -> {}
        );
    }

    public static void translateMessage(long dialog, MessageObject object, Context context, BaseFragment fragment, Theme.ResourcesProvider themeDelegate, OnLinkPress onLinkPress) {
        if ((OwlConfig.translatorStyle == 1 || Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) && object.type != MessageObject.TYPE_POLL) {
            CharSequence text = object.messageOwner.message;
            translate(text, context, fragment, onLinkPress);
        } else {
            TranslatorActionMessage.translateMessage(dialog, object, fragment, themeDelegate);
        }
    }
}