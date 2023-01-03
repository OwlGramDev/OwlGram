package it.owlgram.android.settings;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.INavigationLayout;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.CreationTextCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ManageChatTextCell;
import org.telegram.ui.Cells.ManageChatUserCell;
import org.telegram.ui.Cells.RadioCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextCheckbox2Cell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextRadioCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Cells.UserCell2;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.FlickerLoadingView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.UndoView;

import java.util.List;
import java.util.Locale;

import it.owlgram.android.components.EditTopicCell;
import it.owlgram.android.components.EmojiSetCell;

public abstract class BaseSettingsActivity extends BaseFragment {
    protected static final Object PARTIAL = new Object();
    protected int rowCount;
    protected BaseListAdapter listAdapter;
    protected Context context;
    protected RecyclerListView listView;
    protected ActionBarMenuItem menuItem;
    protected UndoView restartTooltip;
    protected EmptyTextProgressView emptyView;
    private BaseFragment parentFragment;

    protected abstract String getActionBarTitle();

    protected ActionBarMenuItem createMenuItem() {
        return null;
    }

    protected boolean haveEmptyView() {
        return false;
    }

    @Override
    public View createView(Context context) {
        this.context = context;
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(getActionBarTitle());
        actionBar.setAllowOverlayTitle(true);
        menuItem = createMenuItem();
        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else {
                    onMenuItemClick(id);
                }
            }
        });

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;
        listView = new RecyclerListView(context);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setVerticalScrollBarEnabled(false);
        listView.setAdapter(listAdapter = createAdapter());
        if (listView.getItemAnimator() != null) {
            ((DefaultItemAnimator) listView.getItemAnimator()).setDelayAnimations(false);
        }
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setOnItemClickListener(this::onItemClick);

        if (haveEmptyView()) {
            emptyView = new EmptyTextProgressView(context);
            emptyView.setText(LocaleController.getString("NoResult", R.string.NoResult));
            emptyView.showTextView();
            emptyView.setShowAtCenter(true);
            frameLayout.addView(emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
            listView.setEmptyView(emptyView);
            listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
                    }
                }
            });
        }

        restartTooltip = new UndoView(context);
        frameLayout.addView(restartTooltip, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.LEFT, 8, 0, 8, 8));
        parentFragment = parentLayout.getLastFragment();
        return fragmentView;
    }

    protected void hideLastFragment() {
        hideLastFragment(parentFragment, parentLayout);
    }

    protected void showLastFragment() {
        showLastFragment(parentFragment, parentLayout);
    }

    public static void hideLastFragment(BaseFragment parentFragment, INavigationLayout parentLayout) {
        if (parentFragment != null && parentLayout.getFragmentStack().contains(parentFragment)) {
            parentLayout.removeFragmentFromStack(parentFragment);
        }
    }

    public static void showLastFragment(BaseFragment parentFragment, INavigationLayout parentLayout) {
        if (parentFragment != null && !parentLayout.getFragmentStack().contains(parentFragment)) {
            int position = parentLayout.getFragmentStack().size() - 1;
            parentFragment.rebuild();
            parentLayout.addFragmentToStack(parentFragment, position);
        }
    }

    protected void onItemClick(View view, int position, float x, float y) {

    }

    protected void onMenuItemClick(int id) {

    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResume() {
        super.onResume();
        updateRowsId();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    protected void updateRowsId() {
        rowCount = 0;
    }

    protected abstract BaseListAdapter createAdapter();

    protected abstract class BaseListAdapter extends RecyclerListView.SelectionAdapter {
        @Override
        public int getItemCount() {
            return rowCount;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List payloads) {
            Object payload = payloads.isEmpty() ? null : payloads.get(0);
            onBindViewHolder(holder, position, PARTIAL.equals(payload));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            onBindViewHolder(holder, position, false);
        }

        protected abstract void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, boolean partial);
        protected abstract ViewType getViewType(int position);
        protected abstract boolean isEnabled(ViewType viewType, int position);

        @NonNull
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            boolean canSetBackground = true;
            ViewType type = ViewType.fromInt(viewType);
            switch (type) {
                case SHADOW:
                    view = new ShadowSectionCell(context);
                    break;
                case HEADER:
                case HEADER_NO_SHADOW:
                    view = new HeaderCell(context);
                    break;
                case CHECKBOX:
                    view = new TextCheckbox2Cell(context);
                    break;
                case TEXT_RADIO:
                    view = new TextRadioCell(context);
                    break;
                case SWITCH:
                    view = new TextCheckCell(context);
                    break;
                case TEXT_CELL:
                    view = new TextCell(context);
                    break;
                case TEXT_HINT:
                    view = new TextInfoPrivacyCell(context);
                    break;
                case TEXT_HINT_WITH_PADDING:
                    TextInfoPrivacyCell textInfoPrivacyCell = new TextInfoPrivacyCell(context);
                    textInfoPrivacyCell.setBottomPadding(16);
                    view = textInfoPrivacyCell;
                    break;
                case SETTINGS:
                    view = new TextSettingsCell(context);
                    break;
                case DETAILED_SETTINGS:
                    view = new TextDetailSettingsCell(context);
                    break;
                case ADD_EXCEPTION:
                    view = new ManageChatTextCell(context);
                    break;
                case RADIO:
                    view = new RadioCell(context);
                    break;
                case MANAGE_CHAT:
                    view = new ManageChatUserCell(context, 7, 6, true);
                    break;
                case ACCOUNT:
                    view = new UserCell(context, 16, 1, false, false, null, false, true);
                    break;
                case CHAT:
                    view = new UserCell2(context, 4, 0);
                    break;
                case EDIT_TOPIC:
                    view = new EditTopicCell(context);
                    break;
                case PLACEHOLDER:
                    view = new FlickerLoadingView(parent.getContext());
                    break;
                case EMOJI_PACK_SET_CELL:
                    view = new EmojiSetCell(parent.getContext());
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case CREATION_TEXT_CELL:
                    CreationTextCell creationTextCell = new CreationTextCell(context);
                    creationTextCell.startPadding = 61;
                    view = creationTextCell;
                    break;
                default:
                    view = onCreateViewHolder(type);
                    canSetBackground = false;
                    if (view == null) throw new IllegalArgumentException("Unknown viewType: " + viewType);
            }
            switch (ViewType.fromInt(viewType)) {
                case HEADER_NO_SHADOW:
                case SHADOW:
                case TEXT_HINT:
                case TEXT_HINT_WITH_PADDING:
                    break;
                default:
                    if (canSetBackground) view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        protected View onCreateViewHolder(ViewType viewType) {
            return null;
        }

        @Override
        public int getItemViewType(int position) {
            return getViewType(position).toInt();
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return isEnabled(ViewType.fromInt(holder.getItemViewType()), holder.getAdapterPosition());
        }
    }

    protected static String getLanguage(String language) {
        Locale locale = Locale.forLanguageTag(language);
        if (!TextUtils.isEmpty(locale.getScript())) {
            return HtmlCompat.fromHtml(String.format("%s - %s", AndroidUtilities.capitalize(locale.getDisplayScript()), AndroidUtilities.capitalize(locale.getDisplayScript(locale))), HtmlCompat.FROM_HTML_MODE_LEGACY).toString();
        } else {
            return String.format("%s - %s", AndroidUtilities.capitalize(locale.getDisplayName()), AndroidUtilities.capitalize(locale.getDisplayName(locale)));
        }
    }

    protected void reloadInterface() {
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.reloadInterface);
    }

    protected void reloadMainInfo() {
        getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
    }

    protected void reloadDialogs() {
        getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
    }

    protected void rebuildAllFragmentsWithLast() {
        Parcelable recyclerViewState = null;
        if (listView.getLayoutManager() != null) {
            recyclerViewState = listView.getLayoutManager().onSaveInstanceState();
        }
        parentLayout.rebuildFragments(INavigationLayout.REBUILD_FLAG_REBUILD_LAST);
        listView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
    }

    protected void showItemsAnimated() {
        if (isPaused) {
            return;
        }
        View progressView = null;
        for (int i = 0; i < listView.getChildCount(); i++) {
            View child = listView.getChildAt(i);
            if (child instanceof FlickerLoadingView) {
                progressView = child;
            }
        }
        final View finalProgressView = progressView;
        if (progressView != null) {
            listView.removeView(progressView);
        }
        listView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                listView.getViewTreeObserver().removeOnPreDrawListener(this);
                int n = listView.getChildCount();
                AnimatorSet animatorSet = new AnimatorSet();
                for (int i = 0; i < n; i++) {
                    View child = listView.getChildAt(i);
                    if (child == finalProgressView) {
                        continue;
                    }
                    child.setAlpha(0);
                    int s = Math.min(listView.getMeasuredHeight(), Math.max(0, child.getTop()));
                    int delay = (int) ((s / (float) listView.getMeasuredHeight()) * 100);
                    ObjectAnimator a = ObjectAnimator.ofFloat(child, View.ALPHA, 0, 1f);
                    a.setStartDelay(delay);
                    a.setDuration(200);
                    animatorSet.playTogether(a);
                }

                if (finalProgressView != null && finalProgressView.getParent() == null) {
                    listView.addView(finalProgressView);
                    RecyclerView.LayoutManager layoutManager = listView.getLayoutManager();
                    if (layoutManager != null) {
                        layoutManager.ignoreView(finalProgressView);
                        Animator animator = ObjectAnimator.ofFloat(finalProgressView, View.ALPHA, finalProgressView.getAlpha(), 0);
                        animator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                finalProgressView.setAlpha(1f);
                                layoutManager.stopIgnoringView(finalProgressView);
                                listView.removeView(finalProgressView);
                            }
                        });
                        animator.start();
                    }
                }

                animatorSet.start();
                return true;
            }
        });
    }
}
