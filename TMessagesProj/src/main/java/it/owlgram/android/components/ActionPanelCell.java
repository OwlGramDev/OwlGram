package it.owlgram.android.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;

import androidx.cardview.widget.CardView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.RLottieDrawable;
import org.telegram.ui.Components.RLottieImageView;

import it.owlgram.android.OwlConfig;
import it.owlgram.android.components.dynamic.IceledButtonCell;
import it.owlgram.android.components.dynamic.LinearButtonCell;
import it.owlgram.android.components.dynamic.PillsButtonCell;
import it.owlgram.android.components.dynamic.RoundedButtonCell;
import it.owlgram.android.components.dynamic.SimpleActionCell;
import it.owlgram.android.components.dynamic.SquaredButtonCell;

@SuppressLint("ViewConstructor")
public class ActionPanelCell extends LinearLayout {
    private int currId;
    private final LinearLayout mainLayout;
    private final ShimmerFrameLayout shimmerFrameLayout;
    private final Theme.ResourcesProvider resourcesProvider;

    public ActionPanelCell(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        this.resourcesProvider = resourcesProvider;
        currId = -1;
        setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8), 0);
        mainLayout = new LinearLayout(context) {
            @Override
            public void invalidate() {
                super.invalidate();
                for (int i = 0; i < mainLayout.getChildCount(); i++) {
                    if (mainLayout.getChildAt(i) instanceof SimpleActionCell) {
                        SimpleActionCell contents = ((SimpleActionCell) mainLayout.getChildAt(i));
                        contents.updateColors();
                    }
                }
            }
        };
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
        placeholderLayout.addView(getShimmerButton(context, 0));
        placeholderLayout.addView(getShimmerButton(context, 1));
        placeholderLayout.addView(getShimmerButton(context, 2));
        placeholderLayout.addView(getShimmerButton(context, 3));
        mainLayout.setVisibility(GONE);
        addView(mainLayout);
        shimmerFrameLayout.addView(placeholderLayout);
        addView(shimmerFrameLayout);
    }

    public void setLoaded() {
        mainLayout.setVisibility(VISIBLE);
        removeView(shimmerFrameLayout);
    }

    public void addItem(String text, int icon, String color) {
        mainLayout.addView(getButton(getContext(), text, icon, color));
    }

    public void clear() {
        mainLayout.removeAllViews();
        currId = -1;
    }

    protected void onItemClick(int itemId) {
    }

    public LinearLayout getShimmerButton(Context context, int pos) {
        switch (OwlConfig.buttonStyleType) {
            case 1:
                return RoundedButtonCell.getShimmerButton(context);
            case 2:
                return IceledButtonCell.getShimmerButton(context);
            case 3:
                return PillsButtonCell.getShimmerButton(context);
            case 4:
                return LinearButtonCell.getShimmerButton(context, pos);
            default:
                return SquaredButtonCell.getShimmerButton(context);
        }
    }

    public SimpleActionCell getButton(Context context, String text, int iconId, String color) {
        currId++;
        int myId = currId;
        switch (OwlConfig.buttonStyleType) {
            case 1:
                return new RoundedButtonCell(context, resourcesProvider, text, iconId, color, myId) {
                    @Override
                    protected void onItemClick(int id) {
                        super.onItemClick(id);
                        ActionPanelCell.this.onItemClick(id);
                    }
                };
            case 2:
                return new IceledButtonCell(context, resourcesProvider, text, iconId, color, myId) {
                    @Override
                    protected void onItemClick(int id) {
                        super.onItemClick(id);
                        ActionPanelCell.this.onItemClick(id);
                    }
                };
            case 3:
                return new PillsButtonCell(context, resourcesProvider, text, iconId, color, myId) {
                    @Override
                    protected void onItemClick(int id) {
                        super.onItemClick(id);
                        ActionPanelCell.this.onItemClick(id);
                    }
                };
            case 4:
                return new LinearButtonCell(context, resourcesProvider, text, iconId, color, myId) {
                    @Override
                    protected void onItemClick(int id) {
                        super.onItemClick(id);
                        ActionPanelCell.this.onItemClick(id);
                    }
                };
            default:
                return new SquaredButtonCell(context, resourcesProvider, text, iconId, color, myId) {
                    @Override
                    protected void onItemClick(int id) {
                        super.onItemClick(id);
                        ActionPanelCell.this.onItemClick(id);
                    }
                };
        }
    }

    public RLottieDrawable getPhotoAnimationDrawable() {
        try {
            SimpleActionCell simpleActionCell = (SimpleActionCell) mainLayout.getChildAt(0);
            if (simpleActionCell != null) {
                return simpleActionCell.getAnimatedDrawable();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public RLottieImageView getPhotoImageView() {
        try {
            SimpleActionCell simpleActionCell = (SimpleActionCell) mainLayout.getChildAt(0);
            if (simpleActionCell != null) {
                return simpleActionCell.getLottieImageView();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public SimpleActionCell.ThemeInfo getTheme() {
        try {
            SimpleActionCell simpleActionCell = (SimpleActionCell) mainLayout.getChildAt(0);
            if (simpleActionCell != null) {
                return simpleActionCell.getTheme();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), heightMeasureSpec);
    }
}
