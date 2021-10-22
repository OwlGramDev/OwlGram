package it.owlgram.android.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BackupImageView;

public class UpdateCell extends LinearLayout {
    private final BackupImageView backupImageView;
    private final TextView updateTitle;
    private final TextView descMessage;
    private final TextView note;

    public UpdateCell(Context context) {
        super(context);
        int colorBackground = Theme.getColor(Theme.key_windowBackgroundWhite);
        int blue_color = Theme.getColor(Theme.key_dialogTextBlue);
        int colorText = Theme.getColor(Theme.key_windowBackgroundWhiteBlackText);

        setBackgroundColor(colorBackground);
        RelativeLayout relativeLayout = new RelativeLayout(context);
        relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, AndroidUtilities.dp(330)));

        backupImageView = new BackupImageView(getContext());
        backupImageView.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        relativeLayout.addView(backupImageView);
        ImageView iv1 = new ImageView(context);
        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP,
                new int[] {colorBackground, AndroidUtilities.getTransparentColor(colorBackground, 0)});
        gd.setCornerRadius(0f);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(0, AndroidUtilities.dp(20),0,0);
        iv1.setLayoutParams(layoutParams);
        iv1.setBackground(gd);
        relativeLayout.addView(iv1);
        ImageView iv2 = new ImageView(context);
        GradientDrawable gd2 = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] {colorBackground, AndroidUtilities.getTransparentColor(colorBackground, 0)});
        gd2.setCornerRadius(0f);
        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams2.setMargins(0, 0, AndroidUtilities.dp(20),0);
        iv2.setLayoutParams(layoutParams2);
        iv2.setBackground(gd2);
        relativeLayout.addView(iv2);
        ImageView iv3 = new ImageView(context);
        iv3.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        iv3.setBackgroundColor(AndroidUtilities.getTransparentColor(colorBackground, 0.3F));
        relativeLayout.addView(iv3);

        LinearLayout linearLayout = new LinearLayout(context);
        RelativeLayout.LayoutParams layoutParams3 = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams3.setMargins(AndroidUtilities.dp(25), 0, AndroidUtilities.dp(75),0);
        linearLayout.setLayoutParams(layoutParams3);
        linearLayout.setOrientation(VERTICAL);

        updateTitle = new TextView(context);
        LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams4.setMargins(0, AndroidUtilities.dp(25), 0,0);
        updateTitle.setLayoutParams(layoutParams4);
        updateTitle.setTextColor(colorText);
        updateTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 23);

        descMessage = new TextView(context);
        LinearLayout.LayoutParams layoutParams5 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams5.setMargins(0, AndroidUtilities.dp(10), 0,0);
        descMessage.setLayoutParams(layoutParams5);
        descMessage.setTextColor(colorText);
        descMessage.setLinkTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkText));
        descMessage.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        descMessage.setMovementMethod(new AndroidUtilities.LinkMovementMethodMy());
        descMessage.setGravity(Gravity.LEFT | Gravity.TOP);
        descMessage.setLineSpacing(AndroidUtilities.dp(2), 1.0f);

        note = new TextView(context);
        LinearLayout.LayoutParams layoutParams6 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams6.setMargins(0, AndroidUtilities.dp(20), 0,0);
        note.setLayoutParams(layoutParams6);
        note.setTextColor(colorText);
        note.setLinkTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkText));
        note.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        note.setMovementMethod(new AndroidUtilities.LinkMovementMethodMy());
        note.setGravity(Gravity.LEFT | Gravity.TOP);
        note.setLineSpacing(AndroidUtilities.dp(2), 1.0f);

        linearLayout.addView(updateTitle);
        linearLayout.addView(descMessage);
        linearLayout.addView(note);

        LinearLayout linearLayout2 = new LinearLayout(context);
        RelativeLayout.LayoutParams layoutParams7 = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams7.setMargins(AndroidUtilities.dp(25), 0, AndroidUtilities.dp(75),AndroidUtilities.dp(20));
        layoutParams7.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        linearLayout2.setLayoutParams(layoutParams7);

        CardView cardView = new CardView(context);
        cardView.setCardBackgroundColor(blue_color);
        LinearLayout.LayoutParams layoutParams10 = new LinearLayout.LayoutParams(0, AndroidUtilities.dp(40), 1.0f);
        layoutParams10.setMargins(0,0,AndroidUtilities.dp(10), 0);
        cardView.setLayoutParams(layoutParams10);
        cardView.setCardElevation(0);
        cardView.setRadius(AndroidUtilities.dp(5.0f));

        RelativeLayout rl = new RelativeLayout(context);
        rl.setLayoutParams(new CardView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        ImageView mt = new ImageView(context);
        mt.setScaleType(ImageView.ScaleType.FIT_CENTER);
        mt.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(4), Color.TRANSPARENT, Color.argb(55, 0,0,0)));
        mt.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mt.setOnClickListener(view -> onConfirmUpdate());

        TextView textView = new TextView(context);
        textView.setTextColor(Color.WHITE);
        textView.setLines(1);
        textView.setSingleLine(true);
        RelativeLayout.LayoutParams layoutParams8 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams8.addRule(RelativeLayout.CENTER_IN_PARENT);
        textView.setLayoutParams(layoutParams8);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        textView.setText(LocaleController.getString("OwlgramUpdateDownload", R.string.OwlgramUpdateDownload));

        CardView cardView2 = new CardView(context);
        cardView2.setCardBackgroundColor(Color.TRANSPARENT);
        LinearLayout.LayoutParams layoutParams11 = new LinearLayout.LayoutParams(0, AndroidUtilities.dp(40), 1.0f);
        layoutParams11.setMargins(AndroidUtilities.dp(10),0,0, 0);
        cardView2.setLayoutParams(layoutParams11);
        cardView2.setLayoutParams(new LinearLayout.LayoutParams(0, AndroidUtilities.dp(40), 1.0f));
        cardView2.setCardElevation(0);
        cardView2.setRadius(AndroidUtilities.dp(5.0f));

        RelativeLayout rl2 = new RelativeLayout(context);
        rl2.setLayoutParams(new CardView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        ImageView mt2 = new ImageView(context);
        mt2.setScaleType(ImageView.ScaleType.FIT_CENTER);
        mt2.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(4), Color.TRANSPARENT, AndroidUtilities.getTransparentColor(blue_color, 0.5f)));
        mt2.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mt2.setOnClickListener(view -> onRemindUpdate());

        TextView textView2 = new TextView(context);
        textView2.setTextColor(blue_color);
        textView2.setLines(1);
        textView2.setSingleLine(true);
        RelativeLayout.LayoutParams layoutParams9 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams9.addRule(RelativeLayout.CENTER_IN_PARENT);
        textView2.setLayoutParams(layoutParams9);
        textView2.setGravity(Gravity.CENTER_HORIZONTAL);
        textView2.setEllipsize(TextUtils.TruncateAt.END);
        textView2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        textView2.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        textView2.setText(LocaleController.getString("OwlgramUpdateRemind", R.string.OwlgramUpdateRemind));


        rl.addView(mt);
        rl.addView(textView);
        cardView.addView(rl);
        linearLayout2.addView(cardView);

        rl2.addView(mt2);
        rl2.addView(textView2);
        cardView2.addView(rl2);
        linearLayout2.addView(cardView2);

        relativeLayout.addView(linearLayout2);
        relativeLayout.addView(linearLayout);
        addView(relativeLayout);
    }

    private void setTextEntities(TextView tv, String text) {
        text = text.replace("\n", "<br>");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N){
            tv.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
        }else{
            tv.setText(Html.fromHtml(text));
        }
    }

    @SuppressLint("SetTextI18n")
    public void setUpdate(String titleUpdate, String descUpdate, String noteUpdate, String bannerUpdate){
        backupImageView.setImage(bannerUpdate, null, null);
        setTextEntities(updateTitle, titleUpdate);
        setTextEntities(descMessage, descUpdate);
        setTextEntities(note, "<b>"+LocaleController.getString("OwlgramUpdateNote", R.string.OwlgramUpdateNote) + "</b> " + noteUpdate);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), heightMeasureSpec);
    }

    protected void onConfirmUpdate() {}
    protected void onRemindUpdate() {}
}
