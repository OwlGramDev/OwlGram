package it.owlgram.android.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.DividerCell;

import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import it.owlgram.android.OwlConfig;

public class UpdateCheckCell extends RelativeLayout {
    public UpdateCheckCell(Context context) {
        super(context);
        int colorBackground = Theme.getColor(Theme.key_windowBackgroundWhite);
        int orangeColor = Theme.getColor(Theme.key_statisticChartLine_orange);
        int colorDescText = Theme.getColor(Theme.key_windowBackgroundWhiteGrayText);
        int blueColor = Theme.getColor(Theme.key_dialogTextBlue);
        int colorText = Theme.getColor(Theme.key_windowBackgroundWhiteBlackText);
        setBackgroundColor(colorBackground);

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setGravity(Gravity.CENTER_VERTICAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        linearLayout.setPadding(AndroidUtilities.dp(20),AndroidUtilities.dp(10),0,AndroidUtilities.dp(10));

        RelativeLayout relativeLayout = new RelativeLayout(context);
        relativeLayout.setLayoutParams(new LinearLayout.LayoutParams(AndroidUtilities.dp(40), AndroidUtilities.dp(40)));

        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(AndroidUtilities.dp(40), AndroidUtilities.dp(40)));
        imageView.setRotation(45);
        Drawable d = ContextCompat.getDrawable(context, R.drawable.round_autorenew_white_36);
        Objects.requireNonNull(d).setColorFilter(colorText, PorterDuff.Mode.SRC_ATOP);
        imageView.setBackground(d);

        CardView cardView = new CardView(context);
        cardView.setCardBackgroundColor(colorBackground);
        cardView.setCardElevation(0);
        cardView.setRadius(AndroidUtilities.dp(15));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(AndroidUtilities.dp(15), AndroidUtilities.dp(15));
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams.setMargins(0,0,AndroidUtilities.dp(3), AndroidUtilities.dp(3));
        cardView.setLayoutParams(layoutParams);

        LinearLayout linearLayout2 = new LinearLayout(context);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        linearLayout2.setGravity(Gravity.CENTER);

        CardView cardView2 = new CardView(context);
        cardView2.setCardBackgroundColor(orangeColor);
        cardView2.setCardElevation(0);
        cardView2.setRadius(AndroidUtilities.dp(15));
        cardView2.setLayoutParams(new CardView.LayoutParams(AndroidUtilities.dp(10), AndroidUtilities.dp(10)));

        LinearLayout linearLayout3 = new LinearLayout(context);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams2.setMargins(AndroidUtilities.dp(15), 0,0,0);
        linearLayout3.setLayoutParams(layoutParams2);
        linearLayout3.setOrientation(LinearLayout.VERTICAL);

        TextView textView = new TextView(context);
        textView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        textView.setTextColor(colorText);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        setTextEntities(textView, "<b>" + LocaleController.getString("OwlgramUpdateAvailable", R.string.OwlgramUpdateAvailable) + "</b>");

        TextView textView2 = new TextView(context);
        textView2.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        textView2.setTextColor(colorDescText);
        textView2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        long date = OwlConfig.lastUpdateCheck;
        String dateString;
        if (date != 0){
            dateString = LocaleController.formatString("formatDateAtTime", R.string.formatDateAtTime, LocaleController.getInstance().formatterYear.format(new Date(date)), LocaleController.getInstance().formatterDay.format(new Date(date)));
        } else {
            dateString = LocaleController.getString("OwlgramNeverLastCheck", R.string.OwlgramNeverLastCheck);
        }
        textView2.setText(LocaleController.formatString("OwlgramLastCheck", R.string.OwlgramLastCheck, dateString));
        CardView cardView3 = new CardView(context);
        cardView3.setCardBackgroundColor(blueColor);
        RelativeLayout.LayoutParams layoutParams10 = new RelativeLayout.LayoutParams(AndroidUtilities.dp(80), AndroidUtilities.dp(35));
        layoutParams10.addRule(RelativeLayout.CENTER_VERTICAL);
        layoutParams10.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams10.setMargins(0,0,AndroidUtilities.dp(25),0);
        cardView3.setLayoutParams(layoutParams10);
        cardView3.setCardElevation(0);
        cardView3.setRadius(AndroidUtilities.dp(5.0f));

        RelativeLayout rl = new RelativeLayout(context);
        rl.setLayoutParams(new CardView.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        ImageView mt = new ImageView(context);
        mt.setScaleType(ImageView.ScaleType.FIT_CENTER);
        mt.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(4), Color.TRANSPARENT, Color.argb(55, 0,0,0)));
        mt.setLayoutParams(new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        mt.setOnClickListener(view -> onCheckUpdate());

        TextView textView3 = new TextView(context);
        textView3.setTextColor(Color.WHITE);
        textView3.setLines(1);
        textView3.setSingleLine(true);
        RelativeLayout.LayoutParams layoutParams8 = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams8.addRule(RelativeLayout.CENTER_IN_PARENT);
        textView3.setLayoutParams(layoutParams8);
        textView3.setGravity(Gravity.CENTER_HORIZONTAL);
        textView3.setEllipsize(TextUtils.TruncateAt.END);
        textView3.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        textView3.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        textView3.setText(LocaleController.getString("OwlgramCheckUpdates", R.string.OwlgramCheckUpdates));

        DividerCell dividerCell = new DividerCell(context);
        RelativeLayout.LayoutParams layoutParams3 = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams3.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        dividerCell.setLayoutParams(layoutParams3);
        dividerCell.setPadding(AndroidUtilities.dp(20), AndroidUtilities.dp(4), 0, 0);

        rl.addView(mt);
        rl.addView(textView3);
        cardView3.addView(rl);
        linearLayout3.addView(textView);
        linearLayout3.addView(textView2);
        linearLayout2.addView(cardView2);
        cardView.addView(linearLayout2);
        relativeLayout.addView(imageView);
        relativeLayout.addView(cardView);
        linearLayout.addView(relativeLayout);
        linearLayout.addView(linearLayout3);
        addView(cardView3);
        addView(linearLayout);
        addView(dividerCell);
    }
    private void setTextEntities(TextView tv, String text) {
        text = text.replace("\n", "<br>");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N){
            tv.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
        }else{
            tv.setText(Html.fromHtml(text));
        }
    }

    protected void onCheckUpdate() {}
}
