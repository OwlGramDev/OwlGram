package it.owlgram.android.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;

import androidx.cardview.widget.CardView;

import org.telegram.messenger.AndroidUtilities;

import it.owlgram.android.OwlConfig;
import it.owlgram.android.components.dynamic.IceladButtonCell;
import it.owlgram.android.components.dynamic.PillsButtonCell;
import it.owlgram.android.components.dynamic.SquaredButtonCell;
import it.owlgram.android.components.dynamic.SimpleActionCell;
import it.owlgram.android.components.dynamic.RoundedButtonCell;

@SuppressLint("ViewConstructor")
public class ActionPanelCell extends LinearLayout {
    private int currId;
    private final LinearLayout mainLayout;
    private final ShimmerFrameLayout shimmerFrameLayout;

    public ActionPanelCell(Context context) {
        super(context);
        currId = -1;
        setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8), 0);
        mainLayout = new LinearLayout(context);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        mainLayout.setGravity(Gravity.CENTER_HORIZONTAL);

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
        mainLayout.setVisibility(GONE);
        addView(mainLayout);
        shimmerFrameLayout.addView(placeholderLayout);
        addView(shimmerFrameLayout);
    }
    public void setLoaded(){
        mainLayout.setVisibility(VISIBLE);
        removeView(shimmerFrameLayout);
    }
    public void addItem(String text, int icon, int color) {
        mainLayout.addView(getButton(getContext(), text, icon, color));
    }

    public void clear() {
        mainLayout.removeAllViews();
        currId = -1;
    }

    protected void onItemClick(int itemId) {}

    public LinearLayout getShimmerButton(Context context) {
        switch (OwlConfig.buttonStyleType) {
            case 1:
                return RoundedButtonCell.getShimmerButton(context);
            case 2:
                return IceladButtonCell.getShimmerButton(context);
            case 3:
                return PillsButtonCell.getShimmerButton(context);
            default:
                return SquaredButtonCell.getShimmerButton(context);
        }
    }

    public SimpleActionCell getButton(Context context, String text, int iconId, int color){
        currId++;
        int myId = currId;
        switch (OwlConfig.buttonStyleType) {
            case 1:
                return new RoundedButtonCell(context, text, iconId, color, myId) {
                    @Override
                    protected void onItemClick(int id) {
                        super.onItemClick(id);
                        ActionPanelCell.this.onItemClick(id);
                    }
                };
            case 2:
                return new IceladButtonCell(context, text, iconId, color, myId) {
                    @Override
                    protected void onItemClick(int id) {
                        super.onItemClick(id);
                        ActionPanelCell.this.onItemClick(id);
                    }
                };
            case 3:
                return new PillsButtonCell(context, text, iconId, color, myId) {
                    @Override
                    protected void onItemClick(int id) {
                        super.onItemClick(id);
                        ActionPanelCell.this.onItemClick(id);
                    }
                };
            default:
                return new SquaredButtonCell(context, text, iconId, color, myId) {
                    @Override
                    protected void onItemClick(int id) {
                        super.onItemClick(id);
                        ActionPanelCell.this.onItemClick(id);
                    }
                };
        }
    }

    public SimpleActionCell.ThemeInfo getTheme() {
        try {
            SimpleActionCell simpleActionCell = (SimpleActionCell) mainLayout.getChildAt(0);
            if(simpleActionCell != null) {
                return simpleActionCell.getTheme();
            }
        } catch (Exception ignored) {}
        return null;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), heightMeasureSpec);
    }
}
