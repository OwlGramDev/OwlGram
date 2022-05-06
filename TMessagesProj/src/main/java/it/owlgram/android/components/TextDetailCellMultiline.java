package it.owlgram.android.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.spoilers.SpoilersTextView;

@SuppressLint("ViewConstructor")
public class TextDetailCellMultiline extends LinearLayout {

    private final SpoilersTextView spoilersTextView;
    private final TextView valueTextView;
    private boolean needDivider;
    private boolean contentDescriptionValueFirst;
    private CharSequence charSequence;

    public TextDetailCellMultiline(Context context) {
        super(context);

        setOrientation(VERTICAL);
        spoilersTextView = new SpoilersTextView(context);
        spoilersTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        spoilersTextView.setTextSize(16);
        spoilersTextView.setMaxLines(Integer.MAX_VALUE);
        spoilersTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        String linkTextColorKey = Theme.key_windowBackgroundWhiteLinkText;
        spoilersTextView.setLinkTextColor(Theme.getColor(linkTextColorKey));
        spoilersTextView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        addView(spoilersTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 23, 8, 23, 0));

        valueTextView = new TextView(context);
        valueTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        valueTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        valueTextView.setLines(1);
        valueTextView.setMaxLines(1);
        valueTextView.setSingleLine(true);
        valueTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        valueTextView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        addView(valueTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 23, 3, 23, 8));
    }

    public boolean haveSpoilers() {
        return !spoilersTextView.isSpoilersRevealed && spoilersTextView.spoilers.size() > 0;
    }

    @SuppressLint("Recycle")
    public void revealSpoilers() {
        spoilersTextView.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, Math.round(getWidth() / 2.0), Math.round(getHeight() / 2.0), 0));
    }

    public void setTextAndValue(CharSequence text, String value, boolean divider) {
        charSequence = text;
        spoilersTextView.setText(text);
        valueTextView.setText(value);
        needDivider = divider;
        setWillNotDraw(!needDivider);
    }

    public void setTextWithEmojiAndValue(CharSequence text, CharSequence value, boolean divider) {
        charSequence = text;
        spoilersTextView.setText(Emoji.replaceEmoji(text, spoilersTextView.getPaint().getFontMetricsInt(), AndroidUtilities.dp(14), false));
        valueTextView.setText(value);
        needDivider = divider;
        setWillNotDraw(!divider);
    }

    public void setContentDescriptionValueFirst(boolean contentDescriptionValueFirst) {
        this.contentDescriptionValueFirst = contentDescriptionValueFirst;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        spoilersTextView.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (needDivider) {
            canvas.drawLine(
                    LocaleController.isRTL ? 0 : AndroidUtilities.dp(20),
                    getMeasuredHeight() - 1,
                    getMeasuredWidth() - (LocaleController.isRTL ? AndroidUtilities.dp(20) : 0),
                    getMeasuredHeight() - 1,
                    Theme.dividerPaint
            );
        }
    }

    public CharSequence getText() {
        return charSequence;
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        final CharSequence text = spoilersTextView.getText();
        final CharSequence valueText = valueTextView.getText();
        if (!TextUtils.isEmpty(text) && !TextUtils.isEmpty(valueText)) {
            info.setText((contentDescriptionValueFirst ? valueText : text) + ": " + (contentDescriptionValueFirst ? text : valueText));
        }
    }
}
