package org.telegram.ui.Cells;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
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
import java.util.Objects;

import it.owlgram.android.components.Shimmer;
import it.owlgram.android.components.ShimmerFrameLayout;

@SuppressLint("ViewConstructor")
public class ActionPanel extends LinearLayout {
    private int currId;
    private final LinearLayout mainLayout;
    private final ShimmerFrameLayout shimmerFrameLayout;

    public ActionPanel(Context context, boolean isGroup) {
        super(context);
        setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8), 0);
        int blue_color = Theme.getColor(Theme.key_dialogTextBlue);
        int red_color = Theme.getColor(Theme.key_windowBackgroundWhiteRedText4);
        currId = isGroup ? 4:0;
        mainLayout = new LinearLayout(context);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        shimmerFrameLayout = new ShimmerFrameLayout(context);
        Shimmer.AlphaHighlightBuilder shimmer = new Shimmer.AlphaHighlightBuilder();
        shimmer.setBaseAlpha(0.05f);
        shimmer.setHighlightAlpha(0.1f);
        shimmer.setDuration(1500);
        shimmerFrameLayout.setShimmer(shimmer.build());

        shimmerFrameLayout.setLayoutParams(new CardView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        LinearLayout placeholderLayout = new LinearLayout(context);
        placeholderLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        placeholderLayout.addView(getShimmerButton(context));
        placeholderLayout.addView(getShimmerButton(context));
        placeholderLayout.addView(getShimmerButton(context));
        placeholderLayout.addView(getShimmerButton(context));

        if(isGroup){
            mainLayout.addView(getButton(context, LocaleController.getString("Add", R.string.Add), R.drawable.group_addmember, blue_color));
            mainLayout.addView(getButton(context, LocaleController.getString("StartVoipChatTitle", R.string.StartVoipChatTitle), R.drawable.msg_voicechat2, blue_color));
            mainLayout.addView(getButton(context, LocaleController.getString("Edit", R.string.Edit), R.drawable.group_edit, blue_color));
            mainLayout.addView(getButton(context, LocaleController.getString("AccDescrChannel", R.string.AccDescrChannel), R.drawable.msg_channel, blue_color));
            mainLayout.addView(getButton(context, LocaleController.getString("VoipGroupLeave", R.string.VoipGroupLeave), R.drawable.chats_leave, red_color));
        }else{
            mainLayout.addView(getButton(context, LocaleController.getString("Send", R.string.Send), R.drawable.profile_newmsg, blue_color));
            mainLayout.addView(getButton(context, LocaleController.getString("Call", R.string.Call), R.drawable.profile_phone, blue_color));
            mainLayout.addView(getButton(context, LocaleController.getString("VideoCall", R.string.VideoCall), R.drawable.profile_video, blue_color));
            mainLayout.addView(getButton(context, LocaleController.getString("BlockUser", R.string.BlockUser), R.drawable.msg_block, red_color));
        }
        mainLayout.setVisibility(GONE);
        addView(mainLayout);
        shimmerFrameLayout.addView(placeholderLayout);
        addView(shimmerFrameLayout);
    }
    public void setLoaded(){
        mainLayout.setVisibility(VISIBLE);
        shimmerFrameLayout.setVisibility(GONE);
    }
    public void hideLeave(){
        mainLayout.getChildAt(4).setVisibility(GONE);
    }
    public void hideChannel(){
        mainLayout.getChildAt(3).setVisibility(GONE);
    }
    public void hideVideoCall(){
        mainLayout.getChildAt(2).setVisibility(GONE);
    }
    public void hideCall(){
        mainLayout.getChildAt(1).setVisibility(GONE);
    }
    public void hideAdd(){
        mainLayout.getChildAt(0).setVisibility(GONE);
    }
    protected void onItemClick(int itemId) {}
    public void changeItemLogo(int index, int iconId, int color, String text){
        LinearLayout ll = (LinearLayout)mainLayout.getChildAt(index);
        CardView cv = (CardView)ll.getChildAt(0);
        cv.setCardBackgroundColor(AndroidUtilities.getTransparentColor(color, 0.15f));
        RelativeLayout rl = (RelativeLayout) cv.getChildAt(0);
        LinearLayout ll2 = (LinearLayout)rl.getChildAt(1);
        ImageView iv = (ImageView) ll2.getChildAt(0);
        Drawable d = ContextCompat.getDrawable(getContext(), iconId);
        Objects.requireNonNull(d).setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        iv.setBackground(d);
        TextView tv = (TextView) ll2.getChildAt(1);
        tv.setTextColor(color);
        tv.setText(text);
        ImageView iv2 = (ImageView) rl.getChildAt(0);
        iv2.setBackgroundDrawable(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(4), Color.TRANSPARENT, AndroidUtilities.getTransparentColor(color, 0.5f)));
    }
    public LinearLayout getShimmerButton(Context context) {
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f));
        mainLayout.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8));
        mainLayout.setGravity(Gravity.CENTER);

        CardView cardView = new CardView(context);
        cardView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, AndroidUtilities.dp(80)));
        cardView.setCardElevation(0);
        cardView.setRadius(AndroidUtilities.dp(10.0f));
        cardView.setCardBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));

        mainLayout.addView(cardView);
        return mainLayout;
    }
    public LinearLayout getButton(Context context, String text, int iconId, int color){
        currId++;
        int myId = currId;
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f));
        mainLayout.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8));
        mainLayout.setGravity(Gravity.CENTER);

        CardView cardView = new CardView(context);
        cardView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, AndroidUtilities.dp(80)));
        cardView.setCardElevation(0);
        cardView.setRadius(AndroidUtilities.dp(10.0f));
        cardView.setCardBackgroundColor(AndroidUtilities.getTransparentColor(color, 0.15f));

        RelativeLayout rl = new RelativeLayout(context);
        rl.setLayoutParams(new CardView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        LinearLayout ll = new LinearLayout(context);
        RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams1.addRule(RelativeLayout.CENTER_IN_PARENT);
        ll.setLayoutParams(layoutParams1);
        ll.setGravity(Gravity.CENTER);
        ll.setOrientation(LinearLayout.VERTICAL);

        ImageView mt = new ImageView(context);
        mt.setScaleType(ImageView.ScaleType.FIT_CENTER);
        mt.setOnClickListener(view -> onItemClick(myId));
        mt.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(4), Color.TRANSPARENT, AndroidUtilities.getTransparentColor(color, 0.5f)));
        mt.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        ImageView iv = new ImageView(context);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(AndroidUtilities.dp(30), AndroidUtilities.dp(30));
        layoutParams2.setMargins(0,0,0,AndroidUtilities.dp(5));
        iv.setLayoutParams(layoutParams2);
        Drawable d = ContextCompat.getDrawable(context, iconId);
        Objects.requireNonNull(d).setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        iv.setBackground(d);

        TextView tv = new TextView(context);
        tv.setTextColor(color);
        tv.setText(text);
        tv.setLines(1);
        tv.setMaxLines(1);
        tv.setSingleLine(true);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11);
        tv.setGravity(Gravity.CENTER);
        tv.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        tv.setEllipsize(TextUtils.TruncateAt.END);

        ll.addView(iv);
        ll.addView(tv);

        rl.addView(mt);
        rl.addView(ll);
        cardView.addView(rl);
        mainLayout.addView(cardView);
        return mainLayout;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), heightMeasureSpec);
    }
}
