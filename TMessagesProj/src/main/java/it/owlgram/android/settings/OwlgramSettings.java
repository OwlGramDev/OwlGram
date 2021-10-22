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
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

public class OwlgramSettings extends BaseFragment{
    private int rowCount;
    private ListAdapter listAdapter;

    private int divisorInfoRow;
    private int categoryHeaderRow;
    private int generalSettingsRow;
    private int chatSettingsRow;
    private int updateSettingsRow;
    private int infoHeaderRow;
    private int channelUpdatesRow;
    private int sourceCodeRow;
    private int supportTranslationRow;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        updateRowsId();
        return true;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString("OwlgramSettings", R.string.OwlgramSettings));
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
        if(listView.getItemAnimator() != null){
            ((DefaultItemAnimator) listView.getItemAnimator()).setDelayAnimations(false);
        }
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setOnItemClickListener((view, position, x, y) -> {
            if(position == channelUpdatesRow){
                MessagesController.getInstance(currentAccount).openByUserName(LocaleController.getString("OwlgramUsername", R.string.OwlgramUsername), this, 1);
            } else if (position == sourceCodeRow) {
                Browser.openUrl(getParentActivity(), "https://github.com/OwlGramDev/OwlGram");
            } else if (position == supportTranslationRow) {
                Browser.openUrl(getParentActivity(), "https://crowdin.com/project/owlgram");
            } else if (position == generalSettingsRow){
                presentFragment(new OwlgramGeneralSettings());
            } else if (position == chatSettingsRow){
                presentFragment(new OwlgramChatSettings());
            } else if (position == updateSettingsRow){
                presentFragment(new OwlgramUpdateSettings());
            }
        });
        return fragmentView;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateRowsId() {
        rowCount = 0;
        categoryHeaderRow = rowCount++;
        generalSettingsRow = rowCount++;
        chatSettingsRow = rowCount++;
        updateSettingsRow = rowCount++;
        divisorInfoRow = rowCount++;
        infoHeaderRow = rowCount++;
        channelUpdatesRow = rowCount++;
        sourceCodeRow = rowCount++;
        supportTranslationRow = rowCount++;
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
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
                    TextCell textCell = (TextCell) holder.itemView;
                    textCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    if (position == generalSettingsRow) {
                        textCell.setTextAndIcon(LocaleController.getString("General", R.string.General), R.drawable.outline_pack, true);
                    }else if(position == chatSettingsRow){
                        textCell.setTextAndIcon(LocaleController.getString("OwlgramChat", R.string.OwlgramChat), R.drawable.menu_chats, true);
                    }else if(position == updateSettingsRow){
                        textCell.setTextAndIcon(LocaleController.getString("OwlgramUpdates", R.string.OwlgramUpdates), R.drawable.round_update_white_36, true);
                    }else if (position == channelUpdatesRow) {
                        textCell.setTextAndValueAndIcon(LocaleController.getString("OwlgramOfficialChannel", R.string.OwlgramOfficialChannel), "@" + LocaleController.getString("OwlgramUsername", R.string.OwlgramUsername), R.drawable.msg_channel, true);
                    }else if (position == sourceCodeRow) {
                        textCell.setTextAndIcon(LocaleController.getString("OwlgramSourceCode", R.string.OwlgramSourceCode), R.drawable.outline_source_white_36, true);
                    }
                    break;
                case 3:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == categoryHeaderRow) {
                        headerCell.setText(LocaleController.getString("Settings", R.string.Settings));
                    }else if (position == infoHeaderRow){
                        headerCell.setText(LocaleController.getString("Info", R.string.Info));
                    }
                    break;
                case 4:
                    TextDetailSettingsCell textDetailCell = (TextDetailSettingsCell) holder.itemView;
                    textDetailCell.setMultilineDetail(true);
                    if(position == supportTranslationRow) {
                        textDetailCell.setTextAndValueAndIcon(LocaleController.getString("OwlgramTranslation", R.string.OwlgramTranslation), LocaleController.getString("OwlgramTranslationDesc", R.string.OwlgramTranslationDesc), R.drawable.round_translate_white_36, false);
                    }
                    break;
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            return type == 2 || type == 4;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 2:
                    view = new TextCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 3:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 4:
                    view = new TextDetailSettingsCell(mContext);
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
            if (position == divisorInfoRow) {
                return 1;
            }else if(position == generalSettingsRow || position == chatSettingsRow || position == updateSettingsRow ||
                    position == channelUpdatesRow || position == sourceCodeRow){
                return 2;
            }else if(position == categoryHeaderRow || position == infoHeaderRow) {
                return 3;
            }else if(position == supportTranslationRow){
                return 4;
            }
            return 1;
        }
    }
}
