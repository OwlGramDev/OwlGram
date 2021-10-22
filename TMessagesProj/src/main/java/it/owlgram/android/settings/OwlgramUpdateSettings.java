package it.owlgram.android.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import it.owlgram.android.OwlConfig;
import it.owlgram.android.components.UpdateCell;
import it.owlgram.android.components.UpdateCheckCell;

public class OwlgramUpdateSettings extends BaseFragment {

    private int rowCount;
    private ListAdapter listAdapter;

    private int updateSectionAvailableRow;
    private int updateSectionDividerRow;
    private int updateSectionHeader;
    private int updateCheckRow;
    private int betaUpdatesRow;
    private int notifyWhenAvailableRow;
    private int apkChannelRow;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        updateRowsId();
        return true;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString("OwlgramUpdates", R.string.OwlgramUpdates));
        actionBar.setAllowOverlayTitle(false);
        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        listAdapter = new ListAdapter(context);
        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        RecyclerListView listView = new RecyclerListView(context);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setVerticalScrollBarEnabled(false);
        listView.setAdapter(listAdapter);
        if (listView.getItemAnimator() != null) {
            ((DefaultItemAnimator) listView.getItemAnimator()).setDelayAnimations(false);
        }
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setOnItemClickListener((view, position, x, y) -> {
            if (position == betaUpdatesRow) {
                OwlConfig.toggleBetaUpdates();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.betaUpdates);
                }
            } else if (position == notifyWhenAvailableRow) {
                OwlConfig.toggleNotifyUpdates();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.notifyUpdates
                    );
                }
            } else if (position == apkChannelRow) {
                MessagesController.getInstance(currentAccount).openByUserName("OwlGramAPKs", this, 1);
            }
        });
        return fragmentView;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateRowsId() {
        rowCount = 0;
        updateSectionAvailableRow = -1;
        updateSectionDividerRow = -1;

        updateSectionAvailableRow = rowCount++;
        updateSectionDividerRow = rowCount++;

        updateSectionHeader = rowCount++;
        updateCheckRow = rowCount++;
        betaUpdatesRow = rowCount++;
        notifyWhenAvailableRow = rowCount++;
        apkChannelRow = rowCount++;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {
        private final Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 1:
                    holder.itemView.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case 2:
                    UpdateCell updateCell = (UpdateCell) holder.itemView;
                    updateCell.setUpdate(
                            "<b>Upgrade to OwlGram 1.2.0 is ready!</b>",
                            "<b>Get the latest version of OwlGram with</b>\n<a href=\"https://www.google.it/\"><u><b>Chat Themes, Interactive Emoji, Read Receipts in Groups and Live Stream Recording</b></u></a>",
                            "There are some bugs fix like notification not working.",
                            "https://telegram.org/file/464001201/3/gtwtSMRXba0.204892/b102dc7205689001f3"
                    );
                    break;
                case 3:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == updateSectionHeader) {
                        headerCell.setText(LocaleController.getString("OwlgramInAppUpdates", R.string.OwlgramInAppUpdates));
                    }
                    break;
                case 5:
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    textCheckCell.setEnabled(true, null);
                    if (position == betaUpdatesRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("OwlgramBetaUpdates", R.string.OwlgramBetaUpdates), LocaleController.getString("OwlgramBetaUpdatesDesc", R.string.OwlgramBetaUpdatesDesc), OwlConfig.betaUpdates, true, true);
                    } else if (position == notifyWhenAvailableRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("OwlgramUpdatePopup", R.string.OwlgramUpdatePopup), LocaleController.getString("OwlgramUpdatePopupDesc", R.string.OwlgramUpdatePopupDesc), OwlConfig.notifyUpdates, true, true);
                    }
                    break;
                case 6:
                    TextCell textCell = (TextCell) holder.itemView;
                    if (position == apkChannelRow) {
                        textCell.setTextAndValue(LocaleController.getString("OwlgramApkChannel", R.string.OwlgramApkChannel), "@OwlGramAPKs", false);
                    }
                    break;
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            return type == 5 || type == 6;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 2:
                    view = new UpdateCell(mContext) {
                        @Override
                        protected void onConfirmUpdate() {
                            super.onConfirmUpdate();
                        }

                        @Override
                        protected void onRemindUpdate() {
                            super.onRemindUpdate();
                        }
                    };
                    break;
                case 3:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 4:
                    view = new UpdateCheckCell(mContext) {
                        @Override
                        protected void onCheckUpdate() {
                            super.onCheckUpdate();
                        }
                    };
                    break;
                case 5:
                    view = new TextCheckCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 6:
                    view = new TextCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                default:
                    view = new ShadowSectionCell(mContext);
                    break;
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == updateSectionDividerRow) {
                return 1;
            } else if (position == updateSectionAvailableRow) {
                return 2;
            } else if (position == updateSectionHeader) {
                return 3;
            } else if (position == updateCheckRow) {
                return 4;
            } else if (position == betaUpdatesRow || position == notifyWhenAvailableRow) {
                return 5;
            } else if (position == apkChannelRow) {
                return 6;
            }
            return 1;
        }
    }
}
