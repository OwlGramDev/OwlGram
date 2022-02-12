package it.owlgram.android.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import org.telegram.messenger.AndroidUtilities;

import java.util.Locale;
import java.util.Objects;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;

import it.owlgram.android.components.Shimmer;
import it.owlgram.android.components.ShimmerFrameLayout;
import it.owlgram.android.components.dynamic.SimpleActionCell;
import it.owlgram.android.helpers.DCHelper;


public class DatacenterCell extends LinearLayout {
    private final TextView tv;
    private final TextView tv2;
    private final ImageView iv;
    private int color_icon;
    private int DC_CACHE = -1;
    private long ID_CACHE = -1;
    private final LinearLayout mainLayout;
    private final ShimmerFrameLayout shimmerFrameLayout;
    private final CardView mainCardView;

    @SuppressLint("SetTextI18n")
    public DatacenterCell(Context context) {
        super(context);
        setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(8), AndroidUtilities.dp(16), AndroidUtilities.dp(8));
        setGravity(Gravity.CENTER_VERTICAL);

        int colorIcon = Theme.getColor(Theme.key_windowBackgroundWhiteGrayText);
        int colorText = Theme.getColor(Theme.key_windowBackgroundWhiteBlackText);
        int blueColor = color_icon = Theme.getColor(Theme.key_dialogTextBlue);

        shimmerFrameLayout = new ShimmerFrameLayout(context);
        Shimmer.AlphaHighlightBuilder shimmer = new Shimmer.AlphaHighlightBuilder();
        shimmer.setBaseAlpha(0.05f);
        shimmer.setHighlightAlpha(0.1f);
        shimmer.setDuration(1500);
        shimmerFrameLayout.setShimmer(shimmer.build());

        shimmerFrameLayout.setLayoutParams(new CardView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        LinearLayout placeholderLayout = new LinearLayout(context);
        placeholderLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        placeholderLayout.addView(getShimmerDC(context));

        shimmerFrameLayout.addView(placeholderLayout);

        mainLayout = new LinearLayout(context);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        mainLayout.setGravity(Gravity.CENTER_VERTICAL);

        mainCardView = new CardView(context);
        mainCardView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        mainCardView.setCardElevation(0);
        mainCardView.setRadius(AndroidUtilities.dp(10.0f));
        mainCardView.setCardBackgroundColor(AndroidUtilities.getTransparentColor(blueColor, 0.15f));

        LinearLayout ll = new LinearLayout(context);
        ll.setLayoutParams(new CardView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        ll.setPadding(AndroidUtilities.dp(10), AndroidUtilities.dp(10), AndroidUtilities.dp(10), AndroidUtilities.dp(10));

        iv = new ImageView(context);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(AndroidUtilities.dp(30), AndroidUtilities.dp(30));
        iv.setLayoutParams(layoutParams2);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);

        LinearLayout textLayout = new LinearLayout(context);
        textLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        textLayout.setPadding(AndroidUtilities.dp(16), 0, AndroidUtilities.dp(16), 0);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        textLayout.setGravity(Gravity.LEFT);

        tv = new TextView(context);
        tv.setTextColor(colorText);
        tv.setLines(1);
        tv.setMaxLines(1);
        tv.setSingleLine(true);
        tv.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        tv.setGravity(Gravity.CENTER);
        tv.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        tv.setEllipsize(TextUtils.TruncateAt.END);

        tv2 = new TextView(context);
        tv2.setTextColor(colorIcon);

        tv2.setLines(1);
        tv2.setMaxLines(1);
        tv2.setSingleLine(true);
        tv2.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        tv2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        tv2.setGravity(Gravity.CENTER);
        tv2.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        tv2.setEllipsize(TextUtils.TruncateAt.END);

        mainLayout.addView(mainCardView);
        mainCardView.addView(ll);
        ll.addView(iv);
        mainLayout.addView(textLayout);
        textLayout.addView(tv);
        textLayout.addView(tv2);
        mainLayout.setVisibility(GONE);
        addView(mainLayout);
        addView(shimmerFrameLayout);
    }

    public LinearLayout getShimmerDC(Context context) {
        LinearLayout container = new LinearLayout(context);
        container.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        CardView cardView = new CardView(context);
        cardView.setLayoutParams(new LinearLayout.LayoutParams(AndroidUtilities.dp(50), AndroidUtilities.dp(50)));
        cardView.setCardElevation(0);
        cardView.setRadius(AndroidUtilities.dp(10));
        cardView.setCardBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));

        LinearLayout textLayout = new LinearLayout(context);
        textLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        textLayout.setPadding(AndroidUtilities.dp(16), 0, AndroidUtilities.dp(16), 0);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        textLayout.setGravity(Gravity.LEFT);

        CardView tv1 = new CardView(context);
        tv1.setLayoutParams(new LinearLayout.LayoutParams(AndroidUtilities.dp(150), AndroidUtilities.dp(17)));
        tv1.setCardElevation(0);
        tv1.setRadius(AndroidUtilities.dp(3));
        tv1.setCardBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));

        CardView tv2 = new CardView(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(AndroidUtilities.dp(100), AndroidUtilities.dp(14));
        layoutParams.setMargins(0,AndroidUtilities.dp(10),0,0);
        tv2.setLayoutParams(layoutParams);
        tv2.setCardElevation(0);
        tv2.setRadius(AndroidUtilities.dp(3));
        tv2.setCardBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));

        container.addView(cardView);
        container.addView(textLayout);
        textLayout.addView(tv1);
        textLayout.addView(tv2);
        return container;
    }

    public void setTheme(SimpleActionCell.ThemeInfo themeInfo) {
        mainCardView.setCardBackgroundColor(themeInfo.background);
        mainCardView.setRadius(themeInfo.radius);
        color_icon = themeInfo.colorIcon;
        if (ID_CACHE != -1) {
            setIdAndDC(ID_CACHE, DC_CACHE);
        }
    }

    public void setIdAndDC(long id, int DC){
        DC_CACHE = DC;
        ID_CACHE = id;
        mainLayout.setVisibility(VISIBLE);
        removeView(shimmerFrameLayout);
        DC = DC != 0 ? DC:-1;
        String DC_NAME = DCHelper.getDcName(DC);
        if(DC != -1){
            DC_NAME = String.format(Locale.ENGLISH, "%s - DC%d", DC_NAME, DC);
        }
        tv.setText(DC_NAME);
        tv2.setText(String.valueOf(id));
        Drawable d = ContextCompat.getDrawable(getContext(), DCHelper.getDcIcon(DC));
        Objects.requireNonNull(d).setColorFilter(color_icon, PorterDuff.Mode.SRC_ATOP);
        iv.setImageBitmap(drawableToBitmap(d));
    }
    public static Bitmap drawableToBitmap (@NonNull Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
