package it.owlgram.android.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Html;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.RadialProgressView;

import java.util.Locale;
import java.util.Objects;

import it.owlgram.android.updates.ApkDownloader;

public class UpdateCell extends LinearLayout {
    private final BackupImageView backupImageView;
    private final TextView updateTitle;
    private final TextView descMessage;
    private final TextView note;
    private final TextView download_status;
    private final RadialProgressView radialProgress;
    private final LinearLayout downloadingUpdate;
    private final LinearLayout downloadUpdate;
    private final LinearLayout installUpdate;

    @SuppressLint("SetTextI18n")
    public UpdateCell(Context context) {
        super(context);
        int colorBackground = Theme.getColor(Theme.key_windowBackgroundWhite);
        int colorText = Theme.getColor(Theme.key_windowBackgroundWhiteBlackText);

        setBackgroundColor(colorBackground);
        RelativeLayout relativeLayout = new RelativeLayout(context);
        relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,AndroidUtilities.dp(330)));

        backupImageView = new BackupImageView(getContext());
        backupImageView.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        relativeLayout.addView(backupImageView);
        ImageView iv1 = new ImageView(context);
        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP,
                new int[] {colorBackground, AndroidUtilities.getTransparentColor(colorBackground, 0)});
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(0, AndroidUtilities.dp(20),0,0);
        iv1.setLayoutParams(layoutParams);
        iv1.setBackground(gd);
        relativeLayout.addView(iv1);
        ImageView iv2 = new ImageView(context);
        GradientDrawable gd2 = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] {colorBackground, AndroidUtilities.getTransparentColor(colorBackground, 0)});
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

        downloadUpdate = new LinearLayout(context);
        RelativeLayout.LayoutParams layoutParams7 = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams7.setMargins(AndroidUtilities.dp(25), 0, AndroidUtilities.dp(25),AndroidUtilities.dp(20));
        layoutParams7.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        downloadUpdate.setLayoutParams(layoutParams7);

        MaterialButton materialButton = new MaterialButton(context);
        LinearLayout.LayoutParams layoutParams10 = new LinearLayout.LayoutParams(0, AndroidUtilities.dp(40), 1.0f);
        layoutParams10.setMargins(0,0,AndroidUtilities.dp(10), 0);
        materialButton.setLayoutParams(layoutParams10);
        materialButton.setText(LocaleController.getString("DownloadUpdate", R.string.DownloadUpdate));
        materialButton.setOnClickListener(view -> onConfirmUpdate());

        MaterialButton materialButton2 = new MaterialButton(context);
        materialButton2.setOutlineMode();
        LinearLayout.LayoutParams layoutParams11 = new LinearLayout.LayoutParams(0, AndroidUtilities.dp(40), 1.0f);
        layoutParams11.setMargins(AndroidUtilities.dp(10),0,0, 0);
        materialButton2.setLayoutParams(layoutParams11);
        materialButton2.setLayoutParams(new LinearLayout.LayoutParams(0, AndroidUtilities.dp(40), 1.0f));
        materialButton2.setText(LocaleController.getString("RemindUpdate", R.string.RemindUpdate));
        materialButton2.setOnClickListener(view -> onRemindUpdate());

        downloadingUpdate = new LinearLayout(context);
        RelativeLayout.LayoutParams layoutParams8 = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams8.setMargins(AndroidUtilities.dp(25), 0, AndroidUtilities.dp(75),AndroidUtilities.dp(20));
        layoutParams8.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        downloadingUpdate.setLayoutParams(layoutParams8);
        downloadingUpdate.setGravity(Gravity.CENTER_VERTICAL);
        downloadingUpdate.setVisibility(GONE);

        RelativeLayout relativeLayout2 = new RelativeLayout(context);
        relativeLayout2.setLayoutParams(new LinearLayout.LayoutParams(AndroidUtilities.dp(52), AndroidUtilities.dp(52)));

        radialProgress = new RadialProgressView(context);
        radialProgress.setNoProgress(false);
        radialProgress.setLayoutParams(new LinearLayout.LayoutParams(AndroidUtilities.dp(52), AndroidUtilities.dp(52)));
        radialProgress.setProgressColor(colorText);
        radialProgress.setProgress(0);
        radialProgress.setStrokeWidth(3.2F);

        ImageView imageView = new ImageView(context);
        RelativeLayout.LayoutParams layoutParams9 = new RelativeLayout.LayoutParams(AndroidUtilities.dp(33), AndroidUtilities.dp(33));
        layoutParams9.addRule(RelativeLayout.CENTER_IN_PARENT);
        imageView.setLayoutParams(layoutParams9);
        Drawable d = ContextCompat.getDrawable(context, R.drawable.round_close_white_36);
        Objects.requireNonNull(d).setColorFilter(colorText, PorterDuff.Mode.SRC_ATOP);
        imageView.setBackground(d);
        imageView.setOnClickListener(view -> ApkDownloader.cancel());

        LinearLayout linearLayout4 = new LinearLayout(context);
        linearLayout4.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        linearLayout4.setOrientation(VERTICAL);

        TextView title_download = new TextView(context);
        LinearLayout.LayoutParams layoutParams12 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams12.setMargins(AndroidUtilities.dp(12), 0, 0,0);
        title_download.setLayoutParams(layoutParams12);
        title_download.setTextColor(colorText);
        setTextEntities(title_download, "<b>" + LocaleController.getString("DownloadingUpdate", R.string.DownloadingUpdate) + "</b>");
        title_download.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        title_download.setGravity(Gravity.LEFT | Gravity.TOP);
        title_download.setLineSpacing(AndroidUtilities.dp(2), 1.0f);

        download_status = new TextView(context);
        LinearLayout.LayoutParams layoutParams13 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams13.setMargins(AndroidUtilities.dp(12), 0, 0,0);
        download_status.setLayoutParams(layoutParams13);
        download_status.setTextColor(AndroidUtilities.getTransparentColor(colorText, 0.4F));
        download_status.setText(LocaleController.getString("Connecting", R.string.Connecting));
        download_status.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        download_status.setGravity(Gravity.LEFT | Gravity.TOP);
        download_status.setLineSpacing(AndroidUtilities.dp(2), 1.0f);

        installUpdate = new LinearLayout(context);
        RelativeLayout.LayoutParams layoutParams15 = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams15.setMargins(AndroidUtilities.dp(25), 0, AndroidUtilities.dp(25),AndroidUtilities.dp(20));
        layoutParams15.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        installUpdate.setLayoutParams(layoutParams15);
        installUpdate.setVisibility(GONE);

        MaterialButton materialButton3 = new MaterialButton(context);
        LinearLayout.LayoutParams layoutParams14 = new LinearLayout.LayoutParams(0, AndroidUtilities.dp(50), 1.0f);
        materialButton3.setLayoutParams(layoutParams14);
        materialButton3.setText(LocaleController.getString("InstallUpdate", R.string.InstallUpdate));
        materialButton3.setOnClickListener(view -> onInstallUpdate());

        downloadUpdate.addView(materialButton);
        downloadUpdate.addView(materialButton2);
        relativeLayout2.addView(radialProgress);
        relativeLayout2.addView(imageView);
        downloadingUpdate.addView(relativeLayout2);
        linearLayout4.addView(title_download);
        linearLayout4.addView(download_status);
        downloadingUpdate.addView(linearLayout4);
        installUpdate.addView(materialButton3);
        relativeLayout.addView(downloadUpdate);
        relativeLayout.addView(downloadingUpdate);
        relativeLayout.addView(installUpdate);
        relativeLayout.addView(linearLayout);
        addView(relativeLayout);
    }

    public void setDownloadMode() {
        downloadingUpdate.setVisibility(VISIBLE);
        downloadUpdate.setVisibility(GONE);
        installUpdate.setVisibility(GONE);
        radialProgress.setProgress(0);
        download_status.setText(LocaleController.getString("Connecting", R.string.Connecting));
    }

    public void setConfirmMode() {
        downloadingUpdate.setVisibility(GONE);
        downloadUpdate.setVisibility(VISIBLE);
        installUpdate.setVisibility(GONE);
    }

    public void setInstallMode() {
        downloadingUpdate.setVisibility(GONE);
        downloadUpdate.setVisibility(GONE);
        installUpdate.setVisibility(VISIBLE);
    }

    @SuppressLint("SetTextI18n")
    public void setPercentage(int percentage, long downBytes, long totBytes) {
        radialProgress.setProgress(percentage / 100F);
        download_status.setText(AndroidUtilities.formatFileSize(downBytes) + "/" + AndroidUtilities.formatFileSize(totBytes));
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
        setTextEntities(note, "<b>"+LocaleController.getString("UpdateNote", R.string.UpdateNote) + "</b> " + noteUpdate);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), heightMeasureSpec);
    }

    protected void onConfirmUpdate() {}

    protected void onInstallUpdate() {}

    protected void onRemindUpdate() {}
}
