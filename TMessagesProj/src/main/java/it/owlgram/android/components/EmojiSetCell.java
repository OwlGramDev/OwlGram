package it.owlgram.android.components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedTextView;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadialProgressView;

import java.util.Objects;

import it.owlgram.android.helpers.CustomEmojiHelper;

public class EmojiSetCell extends FrameLayout {
    private final TextView textView;
    private final AnimatedTextView valueTextView;
    private final BackupImageView imageView;
    private boolean needDivider;
    private final ImageView optionsButton;
    public String packId;
    public String packFileLink;
    private Long packFileSize;
    public String versionWithMD5;
    private final RadialProgressView radialProgress;

    public EmojiSetCell(Context context) {
        super(context);

        imageView = new BackupImageView(context);
        imageView.setAspectFit(true);
        imageView.setLayerNum(1);
        addView(imageView, LayoutHelper.createFrame(40, 40, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? 0 : 13, 9, LocaleController.isRTL ? 13 : 0, 0));

        optionsButton = new ImageView(context);
        optionsButton.setFocusable(false);
        optionsButton.setScaleType(ImageView.ScaleType.CENTER);
        optionsButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_featuredStickers_addedIcon), PorterDuff.Mode.MULTIPLY));
        optionsButton.setImageResource(R.drawable.floating_check);
        optionsButton.setVisibility(GONE);
        addView(optionsButton, LayoutHelper.createFrame(40, 40, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.TOP, (LocaleController.isRTL ? 10 : 0), 9, (LocaleController.isRTL ? 0 : 10), 0));

        radialProgress = new RadialProgressView(context);
        radialProgress.setNoProgress(false);
        radialProgress.setProgressColor(Theme.getColor(Theme.key_featuredStickers_addedIcon));
        radialProgress.setStrokeWidth(2.8F);
        radialProgress.setSize(AndroidUtilities.dp(30));
        radialProgress.setLayoutParams(new LinearLayout.LayoutParams(AndroidUtilities.dp(40), AndroidUtilities.dp(40)));
        radialProgress.setVisibility(INVISIBLE);
        addView(radialProgress, LayoutHelper.createFrame(40, 40, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.TOP, (LocaleController.isRTL ? 10 : 0), 9, (LocaleController.isRTL ? 0 : 10), 0));

        textView = new TextView(context) {
            @Override
            public void setText(CharSequence text, BufferType type) {
                text = Emoji.replaceEmoji(text, getPaint().getFontMetricsInt(), AndroidUtilities.dp(14), false);
                super.setText(text, type);
            }
        };
        NotificationCenter.listenEmojiLoading(textView);
        textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        textView.setLines(1);
        textView.setMaxLines(1);
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setGravity(LayoutHelper.getAbsoluteGravityStart());
        addView(textView, LayoutHelper.createFrameRelatively(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.START, 71, 9, 70, 0));

        valueTextView = new AnimatedTextView(context);
        valueTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        valueTextView.setAnimationProperties(.55f, 0, 320, CubicBezierInterpolator.EASE_OUT_QUINT);
        valueTextView.setTextSize(AndroidUtilities.dp(13));
        valueTextView.setGravity(LayoutHelper.getAbsoluteGravityStart());
        addView(valueTextView, LayoutHelper.createFrameRelatively(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.START, 71, 25, 70, 0));
    }

    public void setData(CustomEmojiHelper.EmojiPackBase emojiPackInfo, boolean animated, boolean divider) {
        needDivider = divider;
        textView.setText(emojiPackInfo.getPackName());
        packFileSize = emojiPackInfo.getFileSize();
        packId = emojiPackInfo.getPackId();
        if (emojiPackInfo instanceof CustomEmojiHelper.EmojiPackInfo) {
            versionWithMD5 = ((CustomEmojiHelper.EmojiPackInfo) emojiPackInfo).getVersionWithMd5();
        }
        packFileLink = emojiPackInfo.getFileLocation();
        if (Objects.equals(packId, "default")) {
            valueTextView.setText(LocaleController.getString("Default", R.string.Default), animated);
        } else if (CustomEmojiHelper.emojiDir(packId, versionWithMD5).exists() || TextUtils.isEmpty(versionWithMD5)) {
            valueTextView.setText(LocaleController.getString("InstalledEmojiSet", R.string.InstalledEmojiSet), animated);
        } else {
            String status = LocaleController.getString("DownloadUpdate", R.string.DownloadUpdate);
            if (CustomEmojiHelper.isInstalledOldVersion(packId, versionWithMD5)) {
                status = LocaleController.getString("UpdateEmojiSet", R.string.UpdateEmojiSet);
            }
            valueTextView.setText(String.format(
                    "%s %s",
                    status,
                    AndroidUtilities.formatFileSize(packFileSize)
            ), animated);
        }
        textView.setTranslationY(0);
        imageView.setImage(emojiPackInfo.getPreview(), null, null);
    }

    public void setChecked(boolean checked, boolean animated) {
        if (animated) {
            optionsButton.animate().cancel();
            optionsButton.animate().setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!checked) {
                        optionsButton.setVisibility(INVISIBLE);
                    }
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    if (checked) {
                        optionsButton.setVisibility(VISIBLE);
                    }
                }
            }).alpha(checked ? 1 : 0).scaleX(checked ? 1 : 0.1f).scaleY(checked ? 1 : 0.1f).setDuration(150).start();
        } else {
            optionsButton.setVisibility(checked ? VISIBLE : INVISIBLE);
            if (!checked) {
                optionsButton.setScaleX(0.1f);
                optionsButton.setScaleY(0.1f);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (needDivider) {
            canvas.drawLine(LocaleController.isRTL ? 0 : AndroidUtilities.dp(71), getHeight() - 1, getWidth() - getPaddingRight() - (LocaleController.isRTL ? AndroidUtilities.dp(71) : 0), getHeight() - 1, Theme.dividerPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(58) + (needDivider ? 1 : 0), MeasureSpec.EXACTLY));
    }

    public void setProgress(boolean visible, boolean animated) {
        if (animated) {
            radialProgress.animate().cancel();
            radialProgress.animate().setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!visible) {
                        radialProgress.setVisibility(INVISIBLE);
                    }
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    if (visible) {
                        radialProgress.setVisibility(VISIBLE);
                    }
                }
            }).alpha(visible ? 1 : 0).setDuration(150).start();
        } else {
            radialProgress.setVisibility(visible ? VISIBLE : INVISIBLE);
        }
    }

    public void setProgress(float percentage, long downBytes, boolean animated) {
        radialProgress.setProgress(percentage / 100f);
        valueTextView.setText(LocaleController.formatString(
                "AccDescrDownloadProgress",
                R.string.AccDescrDownloadProgress,
                AndroidUtilities.formatFileSize(downBytes),
                AndroidUtilities.formatFileSize(packFileSize)
        ), animated);
    }

    public void checkDownloaded() {
        if (CustomEmojiHelper.emojiTmpDownloaded(packId) || CustomEmojiHelper.emojiDir(packId, versionWithMD5).exists()) {
            setProgress(false, true);
            if (CustomEmojiHelper.emojiDir(packId, versionWithMD5).exists()) {
                valueTextView.setText(LocaleController.getString("InstalledEmojiSet", R.string.InstalledEmojiSet));
                setChecked(true, true);
            } else {
                valueTextView.setText(LocaleController.getString("InstallingEmojiSet", R.string.InstallingEmojiSet));
            }
        } else {
            setProgress(false, true);
            setChecked(false, false);
            String status = LocaleController.getString("DownloadUpdate", R.string.DownloadUpdate);
            if (CustomEmojiHelper.isInstalledOldVersion(packId, versionWithMD5)) {
                status = LocaleController.getString("UpdateEmojiSet", R.string.UpdateEmojiSet);
            }
            valueTextView.setText(String.format(
                    "%s %s",
                    status,
                    AndroidUtilities.formatFileSize(packFileSize)
            ));
        }
    }
}
