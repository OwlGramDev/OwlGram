package it.owlgram.android.components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.GradientDrawable;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;

import it.owlgram.android.StoreUtils;
import it.owlgram.android.updates.ApkDownloader;
import it.owlgram.android.updates.UpdateManager;

public class UpdateAlertDialog extends BottomSheet {

    private final LinearLayout linearLayout;
    private int scrollOffsetY;
    private final NestedScrollView scrollView;
    private final int[] location = new int[2];
    private final View shadow;
    private AnimatorSet shadowAnimation;

    private static class BottomSheetCell extends FrameLayout {

        private final View background;
        private final TextView[] textView = new TextView[2];
        private final boolean hasBackground;

        public BottomSheetCell(Context context, boolean withoutBackground) {
            super(context);

            hasBackground = !withoutBackground;
            setBackground(null);

            background = new View(context);
            if (hasBackground) {
                background.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(4), Theme.getColor(Theme.key_featuredStickers_addButton), Theme.getColor(Theme.key_featuredStickers_addButtonPressed)));
            }
            addView(background, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, 0, 16, withoutBackground ? 0 : 16, 16, 16));

            for (int a = 0; a < 2; a++) {
                textView[a] = new TextView(context);
                textView[a].setLines(1);
                textView[a].setSingleLine(true);
                textView[a].setGravity(Gravity.CENTER_HORIZONTAL);
                textView[a].setEllipsize(TextUtils.TruncateAt.END);
                textView[a].setGravity(Gravity.CENTER);
                if (hasBackground) {
                    textView[a].setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
                    textView[a].setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                } else {
                    textView[a].setTextColor(Theme.getColor(Theme.key_featuredStickers_addButton));
                }
                textView[a].setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                textView[a].setPadding(0, 0, 0, hasBackground ? 0 : AndroidUtilities.dp(13));
                addView(textView[a], LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
                if (a == 1) {
                    textView[a].setAlpha(0.0f);
                }
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(hasBackground ? 80 : 50), MeasureSpec.EXACTLY));
        }

        public void setText(CharSequence text, boolean animated) {
            if (!animated) {
                textView[0].setText(text);
            } else {
                textView[1].setText(text);
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.setDuration(180);
                animatorSet.setInterpolator(CubicBezierInterpolator.EASE_OUT);
                animatorSet.playTogether(
                        ObjectAnimator.ofFloat(textView[0], View.ALPHA, 1.0f, 0.0f),
                        ObjectAnimator.ofFloat(textView[0], View.TRANSLATION_Y, 0, -AndroidUtilities.dp(10)),
                        ObjectAnimator.ofFloat(textView[1], View.ALPHA, 0.0f, 1.0f),
                        ObjectAnimator.ofFloat(textView[1], View.TRANSLATION_Y, AndroidUtilities.dp(10), 0)
                );
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        TextView temp = textView[0];
                        textView[0] = textView[1];
                        textView[1] = temp;
                    }
                });
                animatorSet.start();
            }
        }
    }

    public UpdateAlertDialog(Context context, UpdateManager.UpdateAvailable updateAvailable) {
        super(context, false);
        setCanceledOnTouchOutside(false);

        int colorText = Theme.getColor(Theme.key_windowBackgroundWhiteBlackText);

        setApplyTopPadding(false);
        setApplyBottomPadding(false);

        int colorBackground = Theme.getColor(Theme.key_dialogBackground);

        shadowDrawable = context.getResources().getDrawable(R.drawable.sheet_shadow_round).mutate();
        shadowDrawable.setColorFilter(new PorterDuffColorFilter(colorBackground, PorterDuff.Mode.MULTIPLY));

        FrameLayout container = new FrameLayout(context) {
            @Override
            public void setTranslationY(float translationY) {
                super.setTranslationY(translationY);
                updateLayout();
            }

            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                if (ev.getAction() == MotionEvent.ACTION_DOWN && scrollOffsetY != 0 && ev.getY() < scrollOffsetY) {
                    dismiss();
                    return true;
                }
                return super.onInterceptTouchEvent(ev);
            }

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouchEvent(MotionEvent e) {
                return !isDismissed() && super.onTouchEvent(e);
            }

            @Override
            protected void onDraw(Canvas canvas) {
                int top = (int) (scrollOffsetY - backgroundPaddingTop - getTranslationY());
                shadowDrawable.setBounds(0, top, getMeasuredWidth(), getMeasuredHeight());
                shadowDrawable.draw(canvas);
            }
        };
        container.setWillNotDraw(false);
        containerView = container;

        scrollView = new NestedScrollView(context) {

            private boolean ignoreLayout;

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int height = MeasureSpec.getSize(heightMeasureSpec);
                measureChildWithMargins(linearLayout, widthMeasureSpec, 0, heightMeasureSpec, 0);
                int contentHeight = linearLayout.getMeasuredHeight();
                int padding = (height / 5 * 2);
                int visiblePart = height - padding;
                if (contentHeight - visiblePart < AndroidUtilities.dp(90) || contentHeight < height / 2 + AndroidUtilities.dp(90)) {
                    padding = height - contentHeight;
                }
                if (padding < 0) {
                    padding = 0;
                }
                if (getPaddingTop() != padding) {
                    ignoreLayout = true;
                    setPadding(0, padding, 0, 0);
                    ignoreLayout = false;
                }
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
            }

            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                super.onLayout(changed, left, top, right, bottom);
                updateLayout();
            }

            @Override
            public void requestLayout() {
                if (ignoreLayout) {
                    return;
                }
                super.requestLayout();
            }

            @Override
            protected void onScrollChanged(int l, int t, int old_l, int old_t) {
                super.onScrollChanged(l, t, old_l, old_t);
                updateLayout();
            }
        };
        scrollView.setFillViewport(true);
        scrollView.setWillNotDraw(false);
        scrollView.setClipToPadding(false);
        scrollView.setVerticalScrollBarEnabled(false);
        container.addView(scrollView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP, 0, 0, 0, 130));

        linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(linearLayout, LayoutHelper.createScroll(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP));

        CardView cardView = new CardView(context);
        cardView.setRadius(40);
        cardView.setCardElevation(0);
        cardView.setCardBackgroundColor(colorBackground);

        RelativeLayout relativeLayout = new RelativeLayout(context);
        linearLayout.addView(relativeLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 270, 0, 4, 0, 4, 0));
        relativeLayout.addView(cardView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 270));

        BackupImageView imageView = new BackupImageView(context);
        imageView.setImage(updateAvailable.banner, null, null);
        cardView.addView(imageView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        ImageView iv1 = new ImageView(context);
        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP,
                new int[] {colorBackground, AndroidUtilities.getTransparentColor(colorBackground, 0)});
        gd.setCornerRadius(38f);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(0, AndroidUtilities.dp(20),0,0);
        iv1.setLayoutParams(layoutParams);
        iv1.setBackground(gd);
        relativeLayout.addView(iv1);
        ImageView iv2 = new ImageView(context);
        GradientDrawable gd2 = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] {colorBackground, AndroidUtilities.getTransparentColor(colorBackground, 0)});
        gd2.setCornerRadius(38f);
        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams2.setMargins(0, 0, AndroidUtilities.dp(20),0);
        iv2.setLayoutParams(layoutParams2);
        iv2.setBackground(gd2);
        relativeLayout.addView(iv2);
        ImageView iv3 = new ImageView(context);
        iv3.setLayoutParams(new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        iv3.setBackgroundColor(AndroidUtilities.getTransparentColor(colorBackground, 0.3F));
        relativeLayout.addView(iv3);

        LinearLayout linearLayout2 = new LinearLayout(context);
        RelativeLayout.LayoutParams layoutParams3 = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams3.setMargins(AndroidUtilities.dp(25), 0, AndroidUtilities.dp(75),0);
        linearLayout2.setLayoutParams(layoutParams3);
        linearLayout2.setOrientation(LinearLayout.VERTICAL);

        TextView updateTitle = new TextView(context);
        LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams4.setMargins(0, AndroidUtilities.dp(25), 0,0);
        updateTitle.setLayoutParams(layoutParams4);
        updateTitle.setTextColor(colorText);
        updateTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 23);

        TextView descMessage = new TextView(context);
        LinearLayout.LayoutParams layoutParams5 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams5.setMargins(0, AndroidUtilities.dp(10), 0,0);
        descMessage.setLayoutParams(layoutParams5);
        descMessage.setTextColor(colorText);
        descMessage.setLinkTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkText));
        descMessage.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        descMessage.setMovementMethod(new AndroidUtilities.LinkMovementMethodMy());
        descMessage.setGravity(Gravity.LEFT | Gravity.TOP);
        descMessage.setLineSpacing(AndroidUtilities.dp(2), 1.0f);

        TextView note = new TextView(context);
        LinearLayout.LayoutParams layoutParams6 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams6.setMargins(0, AndroidUtilities.dp(20), 0,0);
        note.setLayoutParams(layoutParams6);
        note.setTextColor(colorText);
        note.setLinkTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkText));
        note.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        note.setMovementMethod(new AndroidUtilities.LinkMovementMethodMy());
        note.setGravity(Gravity.LEFT | Gravity.TOP);
        note.setLineSpacing(AndroidUtilities.dp(2), 1.0f);
        setTextEntities(updateTitle, updateAvailable.title);
        setTextEntities(descMessage, updateAvailable.desc);
        setTextEntities(note, "<b>"+LocaleController.getString("UpdateNote", R.string.UpdateNote) + "</b> " + updateAvailable.note);

        linearLayout2.addView(updateTitle);
        linearLayout2.addView(descMessage);
        linearLayout2.addView(note);
        relativeLayout.addView(linearLayout2);

        FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(LayoutHelper.MATCH_PARENT, AndroidUtilities.getShadowHeight(), Gravity.BOTTOM | Gravity.LEFT);
        frameLayoutParams.bottomMargin = AndroidUtilities.dp(130);
        shadow = new View(context);
        shadow.setBackgroundColor(Theme.getColor(Theme.key_dialogShadowLine));
        shadow.setAlpha(0.0f);
        shadow.setTag(1);
        container.addView(shadow, frameLayoutParams);

        BottomSheetCell doneButton = new BottomSheetCell(context, false);
        doneButton.setText(LocaleController.formatString("AppUpdateDownloadNow", R.string.AppUpdateDownloadNow), false);
        doneButton.background.setOnClickListener(v -> {
            if(!StoreUtils.isDownloadedFromAnyStore()){
                ApkDownloader.downloadAPK(context, updateAvailable.link_file, updateAvailable.version);
            } else if (StoreUtils.isFromPlayStore()) {
                Browser.openUrl(getContext(), BuildVars.PLAYSTORE_APP_URL);
            }
            dismiss();
        });
        container.addView(doneButton, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 50, Gravity.LEFT | Gravity.BOTTOM, 0, 0, 0, 50));
        BottomSheetCell scheduleButton = new BottomSheetCell(context, true);
        scheduleButton.setText(LocaleController.getString("AppUpdateRemindMeLater", R.string.AppUpdateRemindMeLater), false);
        scheduleButton.background.setOnClickListener(v -> dismiss());
        container.addView(scheduleButton, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 50, Gravity.LEFT | Gravity.BOTTOM, 0, 0, 0, 0));

    }

    private void runShadowAnimation(final boolean show) {
        if (show && shadow.getTag() != null || !show && shadow.getTag() == null) {
            shadow.setTag(show ? null : 1);
            if (show) {
                shadow.setVisibility(View.VISIBLE);
            }
            if (shadowAnimation != null) {
                shadowAnimation.cancel();
            }
            shadowAnimation = new AnimatorSet();
            shadowAnimation.playTogether(ObjectAnimator.ofFloat(shadow, View.ALPHA, show ? 1.0f : 0.0f));
            shadowAnimation.setDuration(150);
            shadowAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (shadowAnimation != null && shadowAnimation.equals(animation)) {
                        if (!show) {
                            shadow.setVisibility(View.INVISIBLE);
                        }
                        shadowAnimation = null;
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    if (shadowAnimation != null && shadowAnimation.equals(animation)) {
                        shadowAnimation = null;
                    }
                }
            });
            shadowAnimation.start();
        }
    }

    private void updateLayout() {
        View child = linearLayout.getChildAt(0);
        child.getLocationInWindow(location);
        int top = location[1];
        int newOffset = Math.max(top, 0);
        runShadowAnimation(!(location[1] + linearLayout.getMeasuredHeight() <= container.getMeasuredHeight() - AndroidUtilities.dp(113) + containerView.getTranslationY()));
        if (scrollOffsetY != newOffset) {
            scrollOffsetY = newOffset;
            scrollView.invalidate();
        }
    }

    @Override
    protected boolean canDismissWithSwipe() {
        return false;
    }

    private void setTextEntities(TextView tv, String text) {
        text = text.replace("\n", "<br>");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N){
            tv.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
        }else{
            tv.setText(Html.fromHtml(text));
        }
    }
}
