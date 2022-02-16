package it.owlgram.android.components;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.ColoredImageSpan;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RLottieImageView;

import java.util.Objects;

import it.owlgram.android.helpers.EntitiesHelper;

public class DatacenterHeaderRow extends LinearLayout {
    public DatacenterHeaderRow(Context context) {
        super(context);
        setGravity(Gravity.CENTER_HORIZONTAL);
        setOrientation(VERTICAL);
        RLottieImageView rLottieImageView = new RLottieImageView(context);
        rLottieImageView.setAnimation(R.raw.duck_server, 120, 120);
        rLottieImageView.playAnimation();
        rLottieImageView.getAnimatedDrawable().setAutoRepeat(1);
        addView(rLottieImageView, LayoutHelper.createLinear(120, 120, Gravity.CENTER_HORIZONTAL, 0, 20, 0, 0));

        TextView textView = new TextView(context);
        addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 36, 26, 36, 0));
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        textView.setLinkTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkText));
        textView.setHighlightColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkSelection));
        String text = LocaleController.getString("DatacenterDesc", R.string.DatacenterDesc);
        Spannable htmlParsed;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            htmlParsed = new SpannableString(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
        }else{
            htmlParsed = new SpannableString(Html.fromHtml(text));
        }
        textView.setText(EntitiesHelper.getUrlNoUnderlineText(htmlParsed));
        textView.setMovementMethod(new AndroidUtilities.LinkMovementMethodMy());

        TextView buttonTextView = new TextView(context);
        buttonTextView.setPadding(AndroidUtilities.dp(34), 0, AndroidUtilities.dp(34), 0);
        buttonTextView.setGravity(Gravity.CENTER);
        buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        buttonTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(".  ").append(LocaleController.getString("WebVersion", R.string.WebVersion));
        spannableStringBuilder.setSpan(new ColoredImageSpan(Objects.requireNonNull(ContextCompat.getDrawable(getContext(), R.drawable.web_access))), 0, 1, 0);
        buttonTextView.setText(spannableStringBuilder);
        buttonTextView.setOnClickListener(view -> Browser.openUrl(AndroidUtilities.findActivity(context), "https://status.owlgram.org/"));

        buttonTextView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        buttonTextView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(6), Theme.getColor(Theme.key_featuredStickers_addButton), Theme.getColor(Theme.key_featuredStickers_addButtonPressed)));
        addView(buttonTextView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM, 16, 15, 16, 16));
    }
}
