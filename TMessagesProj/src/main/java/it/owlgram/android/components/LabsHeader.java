package it.owlgram.android.components;

import android.content.Context;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;

public class LabsHeader extends LinearLayout {
    public LabsHeader(Context context) {
        super(context);
        setGravity(Gravity.CENTER);
        ImageView imageView = new ImageView(context);
        int colorBackground = Theme.getColor(Theme.key_windowBackgroundGray);
        if (AndroidUtilities.isLight(colorBackground)) {
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.labs_settings_preview));
        } else {
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.labs_settings_preview_dark));
        }
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        addView(imageView, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        setPadding(AndroidUtilities.dp(5), AndroidUtilities.dp(10), AndroidUtilities.dp(5), 0);
    }
}
