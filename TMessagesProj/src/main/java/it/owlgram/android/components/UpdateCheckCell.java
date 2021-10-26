package it.owlgram.android.components;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
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
import java.util.Objects;

import it.owlgram.android.OwlConfig;

public class UpdateCheckCell extends RelativeLayout {
    final private TextView updateStatus;
    final private TextView updateTitle;
    final private CardView updateDotContainer;
    final private CardView updateDot;
    final private MaterialButton checkUpdateButton;
    final private LinearLayout container;

    public UpdateCheckCell(Context context) {
        super(context);
        int colorBackground = Theme.getColor(Theme.key_windowBackgroundWhite);
        int colorDescText = Theme.getColor(Theme.key_windowBackgroundWhiteGrayText);
        int colorText = Theme.getColor(Theme.key_windowBackgroundWhiteBlackText);
        setBackgroundColor(colorBackground);

        container = new LinearLayout(context);
        container.setGravity(Gravity.CENTER_VERTICAL);
        container.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        container.setPadding(AndroidUtilities.dp(20),AndroidUtilities.dp(10),0,AndroidUtilities.dp(10));

        RelativeLayout relativeLayout = new RelativeLayout(context);
        relativeLayout.setLayoutParams(new LinearLayout.LayoutParams(AndroidUtilities.dp(40), AndroidUtilities.dp(40)));

        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(AndroidUtilities.dp(40), AndroidUtilities.dp(40)));
        imageView.setRotation(45);
        Drawable d = ContextCompat.getDrawable(context, R.drawable.round_autorenew_white_36);
        Objects.requireNonNull(d).setColorFilter(colorText, PorterDuff.Mode.SRC_ATOP);
        imageView.setBackground(d);

        updateDotContainer = new CardView(context);
        updateDotContainer.setCardBackgroundColor(colorBackground);
        updateDotContainer.setCardElevation(0);
        updateDotContainer.setRadius(AndroidUtilities.dp(15));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(AndroidUtilities.dp(15), AndroidUtilities.dp(15));
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams.setMargins(0,0,AndroidUtilities.dp(3), AndroidUtilities.dp(3));
        updateDotContainer.setLayoutParams(layoutParams);

        LinearLayout linearLayout2 = new LinearLayout(context);
        linearLayout2.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        linearLayout2.setGravity(Gravity.CENTER);

        updateDot = new CardView(context);
        updateDot.setCardElevation(0);
        updateDot.setRadius(AndroidUtilities.dp(15));
        updateDot.setLayoutParams(new CardView.LayoutParams(AndroidUtilities.dp(10), AndroidUtilities.dp(10)));

        LinearLayout linearLayout3 = new LinearLayout(context);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams2.setMargins(AndroidUtilities.dp(15), 0,0,0);
        linearLayout3.setLayoutParams(layoutParams2);
        linearLayout3.setOrientation(LinearLayout.VERTICAL);

        String update_text = LocaleController.getString("OwlgramCheckUpdates", R.string.OwlgramCheckUpdates);
        updateTitle = new TextView(context);
        LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams4.setMargins(0,0,AndroidUtilities.dp(5+15+50) + (AndroidUtilities.dp(4) * update_text.length()) + AndroidUtilities.dp(40), 0);
        updateTitle.setLayoutParams(layoutParams4);
        updateTitle.setTextColor(colorText);
        updateTitle.setSingleLine(true);
        updateTitle.setEllipsize(TextUtils.TruncateAt.END);
        updateTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);

        updateStatus = new TextView(context);
        updateStatus.setLayoutParams(layoutParams4);
        updateStatus.setTextColor(colorDescText);
        updateStatus.setSingleLine(true);
        updateStatus.setEllipsize(TextUtils.TruncateAt.END);
        updateStatus.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);

        checkUpdateButton = new MaterialButton(context);
        RelativeLayout.LayoutParams layoutParams10 = new RelativeLayout.LayoutParams((AndroidUtilities.dp(4) * update_text.length()) + AndroidUtilities.dp(40), AndroidUtilities.dp(35));
        layoutParams10.addRule(RelativeLayout.CENTER_VERTICAL);
        layoutParams10.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams10.setMargins(0,0,AndroidUtilities.dp(25),0);
        checkUpdateButton.setLayoutParams(layoutParams10);
        checkUpdateButton.setOnClickListener(view -> onCheckUpdate());
        checkUpdateButton.setText(update_text);

        DividerCell dividerCell = new DividerCell(context);
        RelativeLayout.LayoutParams layoutParams3 = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams3.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        dividerCell.setLayoutParams(layoutParams3);
        dividerCell.setPadding(AndroidUtilities.dp(20), AndroidUtilities.dp(4), 0, 0);

        linearLayout3.addView(updateTitle);
        linearLayout3.addView(updateStatus);
        linearLayout2.addView(updateDot);
        updateDotContainer.addView(linearLayout2);
        relativeLayout.addView(imageView);
        relativeLayout.addView(updateDotContainer);
        container.addView(relativeLayout);
        container.addView(linearLayout3);
        addView(checkUpdateButton);
        addView(container);
        addView(dividerCell);
    }

    public void setCheckingStatus() {
        updateDotContainer.setVisibility(GONE);
        checkUpdateButton.setVisibility(GONE);
        updateStatus.setText(LocaleController.getString("OwlgramUpdateChecking", R.string.OwlgramUpdateChecking));
    }

    public void setClickable(boolean enabled) {
        checkUpdateButton.setAlpha(enabled ? 1.0f:0.5f);
        container.setAlpha(enabled ? 1.0f:0.5f);
    }

    public void loadLastStatus() {
        switch (OwlConfig.lastUpdateStatus) {
            case 1:
                if(OwlConfig.updateData.length() > 0){
                    setUpdateAvailableStatus();
                } else {
                    setCheckTime();
                }
                break;
            case 2:
                setFailedStatus();
                break;
            default:
                setCheckTime();
                break;
        }
    }

    public void setFailedStatus() {
        int redColor = Theme.getColor(Theme.key_windowBackgroundWhiteRedText4);
        updateDotContainer.setVisibility(VISIBLE);
        checkUpdateButton.setVisibility(VISIBLE);
        updateDot.setCardBackgroundColor(redColor);
        setTextEntities(updateTitle, "<b>" + LocaleController.getString("OwlgramCheckFailed", R.string.OwlgramCheckFailed) + "</b>");
        updateStatus.setText(LocaleController.getString("OwlgramUpdateChecking", R.string.OwlgramUpdateChecking));
        setTime(OwlConfig.lastUpdateCheck);
        OwlConfig.saveUpdateStatus(2);
    }

    public void setUpdateAvailableStatus() {
        int orangeColor = Theme.getColor(Theme.key_statisticChartLine_orange);
        updateDotContainer.setVisibility(VISIBLE);
        checkUpdateButton.setVisibility(VISIBLE);
        setTextEntities(updateTitle, "<b>" + LocaleController.getString("OwlgramUpdateAvailable", R.string.OwlgramUpdateAvailable) + "</b>");
        updateDot.setCardBackgroundColor(orangeColor);
        setTime(OwlConfig.lastUpdateCheck);
        OwlConfig.saveUpdateStatus(1);
    }

    public void setCheckTime() {
        int greenColor = Theme.getColor(Theme.key_statisticChartLine_green);
        int orangeColor = Theme.getColor(Theme.key_statisticChartLine_orange);
        updateDotContainer.setVisibility(VISIBLE);
        checkUpdateButton.setVisibility(VISIBLE);
        long date = OwlConfig.lastUpdateCheck;
        if (date != 0) {
            setTextEntities(updateTitle, "<b>" + LocaleController.getString("OwlgramNoUpdate", R.string.OwlgramNoUpdate) + "</b>");
            updateDot.setCardBackgroundColor(greenColor);
        } else {
            setTextEntities(updateTitle, "<b>" + LocaleController.getString("OwlgramNeverChecked", R.string.OwlgramNeverChecked) + "</b>");
            updateDot.setCardBackgroundColor(orangeColor);
        }
        setTime(date);
        OwlConfig.saveUpdateStatus(0);
    }

    private void setTime(long date) {
        String dateString;
        if (date != 0){
            dateString = LocaleController.formatString("formatDateAtTime", R.string.formatDateAtTime, LocaleController.getInstance().formatterYear.format(new Date(date)), LocaleController.getInstance().formatterDay.format(new Date(date)));
        } else {
            dateString = LocaleController.getString("OwlgramNeverLastCheck", R.string.OwlgramNeverLastCheck);
        }
        updateStatus.setText(LocaleController.formatString("OwlgramLastCheck", R.string.OwlgramLastCheck, dateString));
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
