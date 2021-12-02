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
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import it.owlgram.android.OwlConfig;
import it.owlgram.android.components.StickerSizeCell;

public class OwlgramChatSettings extends BaseFragment {
    private ActionBarMenuItem menuItem;

    private int rowCount;
    private ListAdapter listAdapter;
    private int stickerSizeHeaderRow;
    private int stickerSizeRow;
    private int stickerSizeDividerRow;
    private int chatHeaderRow;
    private int mediaSwipeByTapRow;
    private int jumpChannelRow;
    private int showGreetings;
    private int hideKeyboardRow;
    private int scrollableRow;
    private int playGifAsVideoRow;
    private int chatDividerRow;
    private int foldersHeaderRow;
    private int showFolderWhenForwardRow;
    private int showFolderIconsRow;
    private int foldersDividerRow;
    private int messageMenuHeaderRow;
    private int showTranslateRow;
    private int showAddToSMRow;
    private int showNoQuoteForwardRow;
    private int showReportRow;
    private int showMessageDetails;
    private int messageMenuDividerRow;
    private int audioVideoHeaderRow;
    private int rearCameraStartingRow;
    private int confirmSendRow;
    private int showDeleteRow;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        updateRowsId();
        return true;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString("OwlgramChat", R.string.OwlgramChat));
        actionBar.setAllowOverlayTitle(false);
        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == 1) {
                    OwlConfig.setStickerSize(14.0f);
                    menuItem.setVisibility(View.GONE);
                    listAdapter.notifyItemChanged(stickerSizeRow, new Object());
                }
            }
        });

        ActionBarMenu menu = actionBar.createMenu();
        menuItem = menu.addItem(0, R.drawable.ic_ab_other);
        menuItem.setContentDescription(LocaleController.getString("AccDescrMoreOptions", R.string.AccDescrMoreOptions));
        menuItem.addSubItem(1, R.drawable.msg_reset, LocaleController.getString("OwlgramResetStickerSize", R.string.OwlgramResetStickerSize));
        menuItem.setVisibility(OwlConfig.stickerSize != 14.0f ? View.VISIBLE : View.GONE);

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
            if (position == mediaSwipeByTapRow){
                OwlConfig.toggleMediaFlipByTap();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.mediaFlipByTap);
                }
            } else if (position == jumpChannelRow) {
                OwlConfig.toggleJumpChannel();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.jumpChannel);
                }
            } else if (position == showGreetings) {
                OwlConfig.toggleShowGreetings();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.showGreetings);
                }
            } else if (position == hideKeyboardRow){
                OwlConfig.toggleHideKeyboard();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.hideKeyboard);
                }
            } else if (position == playGifAsVideoRow){
                OwlConfig.toggleGifAsVideo();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.gifAsVideo);
                }
            } else if (position == showFolderWhenForwardRow){
                OwlConfig.toggleShowFolderWhenForward();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.showFolderWhenForward);
                }
            } else if (position == rearCameraStartingRow){
                OwlConfig.toggleUseRearCamera();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.useRearCamera);
                }
            } else if (position == confirmSendRow){
                OwlConfig.toggleSendConfirm();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.sendConfirm);
                }
            } else if (position == showTranslateRow){
                OwlConfig.toggleShowTranslate();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.showTranslate);
                }
            } else if (position == showAddToSMRow){
                OwlConfig.toggleShowSaveMessage();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.showSaveMessage);
                }
            } else if (position == showMessageDetails){
                OwlConfig.toggleShowMessageDetails();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.showMessageDetails);
                }
            } else if (position == showNoQuoteForwardRow){
                OwlConfig.toggleShowNoQuoteForwardRow();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.showNoQuoteForward);
                }
            } else if (position == showReportRow){
                OwlConfig.toggleShowReportMessage();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.showReportMessage);
                }
            } else if (position == scrollableRow){
                OwlConfig.toggleScrollableChatPreview();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.scrollableChatPreview);
                }
            } else if (position == showDeleteRow){
                OwlConfig.toggleShowDeleteDownloadedFile();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.showDeleteDownloadedFile);
                }
            }
        });
        return fragmentView;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateRowsId() {
        rowCount = 0;
        showFolderIconsRow = -1;

        stickerSizeHeaderRow = rowCount++;
        stickerSizeRow = rowCount++;
        stickerSizeDividerRow = rowCount++;
        chatHeaderRow = rowCount++;
        mediaSwipeByTapRow = rowCount++;
        jumpChannelRow = rowCount++;
        showGreetings = rowCount++;
        playGifAsVideoRow = rowCount++;
        hideKeyboardRow = rowCount++;
        scrollableRow = rowCount++;
        chatDividerRow = rowCount++;
        foldersHeaderRow = rowCount++;
        showFolderWhenForwardRow = rowCount++;
        //showFolderIconsRow = rowCount++;
        foldersDividerRow = rowCount++;

        messageMenuHeaderRow = rowCount++;
        showDeleteRow = rowCount++;
        showNoQuoteForwardRow = rowCount++;
        showAddToSMRow = rowCount++;
        showTranslateRow = rowCount++;
        showReportRow = rowCount++;
        showMessageDetails = rowCount++;
        messageMenuDividerRow = rowCount++;

        audioVideoHeaderRow = rowCount++;
        rearCameraStartingRow = rowCount++;
        confirmSendRow = rowCount++;
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
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == chatHeaderRow) {
                        headerCell.setText(LocaleController.getString("OwlgramChat", R.string.OwlgramChat));
                    } else if (position == foldersHeaderRow) {
                        headerCell.setText(LocaleController.getString("Filters", R.string.Filters));
                    } else if (position == audioVideoHeaderRow) {
                        headerCell.setText(LocaleController.getString("OwlgramAudioVideo", R.string.OwlgramAudioVideo));
                    } else if (position == messageMenuHeaderRow) {
                        headerCell.setText(LocaleController.getString("OwlgramMessageMenu", R.string.OwlgramMessageMenu));
                    } else if (position == stickerSizeHeaderRow) {
                        headerCell.setText(LocaleController.getString("OwlgramStickerSize", R.string.OwlgramStickerSize));
                    }
                    break;
                case 3:
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    textCheckCell.setEnabled(true, null);
                    if (position == mediaSwipeByTapRow){
                        textCheckCell.setTextAndCheck(LocaleController.getString("OwlgramMediaFlipByTap", R.string.OwlgramMediaFlipByTap), OwlConfig.mediaFlipByTap, true);
                    } else if (position == jumpChannelRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("OwlgramJumpChannel", R.string.OwlgramJumpChannel), OwlConfig.jumpChannel, true);
                    } else if (position == showGreetings) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("OwlgramShowGreetings", R.string.OwlgramShowGreetings), OwlConfig.showGreetings, true);
                    } else if (position == hideKeyboardRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("OwlgramHideKeyboardScrolling", R.string.OwlgramHideKeyboardScrolling), OwlConfig.hideKeyboard, true);
                    } else if (position == playGifAsVideoRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("OwlgramPlayGifAsVideo", R.string.OwlgramPlayGifAsVideo), OwlConfig.gifAsVideo, true);
                    } else if (position == showFolderWhenForwardRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("OwlgramFolderWhenForward", R.string.OwlgramFolderWhenForward), OwlConfig.showFolderWhenForward, true);
                    } else if (position == showFolderIconsRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("OwlgramShowFolderIcons", R.string.OwlgramShowFolderIcons), OwlConfig.showFolderIcons, true);
                    } else if (position == rearCameraStartingRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("OwlgramCameraStarting", R.string.OwlgramCameraStarting), LocaleController.getString("OwlgramDescCamera", R.string.OwlgramDescCamera), OwlConfig.useRearCamera, true, true);
                    } else if (position == confirmSendRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("OwlgramSendConfirm", R.string.OwlgramSendConfirm), OwlConfig.sendConfirm, true);
                    } else if (position == showTranslateRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("OwlgramShowTranslate", R.string.OwlgramShowTranslate), OwlConfig.showTranslate, true);
                    } else if (position == showDeleteRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("OwlgramDeleteCacheFile", R.string.OwlgramDeleteCacheFile), OwlConfig.showDeleteDownloadedFile, true);
                    } else if (position == showAddToSMRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("OwlgramShowSaveMessage", R.string.OwlgramShowSaveMessage), OwlConfig.showSaveMessage, true);
                    } else if (position == showMessageDetails) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("OwlgramShowDetails", R.string.OwlgramShowDetails), OwlConfig.showMessageDetails, false);
                    } else if (position == showNoQuoteForwardRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("OwlgramNoQuoteForward", R.string.OwlgramNoQuoteForward), OwlConfig.showNoQuoteForward, true);
                    } else if (position == showReportRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("ReportChat", R.string.ReportChat), OwlConfig.showReportMessage, true);
                    } else if (position == scrollableRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("OwlgramScrollablePreview", R.string.OwlgramScrollablePreview), OwlConfig.scrollableChatPreview, true);
                    }
                    break;
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            return type == 3 || type == 5;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 2:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 3:
                    view = new TextCheckCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 4:
                    view = new StickerSizeCell(mContext, parentLayout) {
                        @Override
                        protected void onSeek() {
                            super.onSeek();
                            menuItem.setVisibility(VISIBLE);
                        }
                    };
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 5:
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
            if(position == chatDividerRow || position == foldersDividerRow || position == messageMenuDividerRow ||
                    position == stickerSizeDividerRow){
                return 1;
            } else if (position == chatHeaderRow || position == foldersHeaderRow || position == audioVideoHeaderRow ||
                    position == messageMenuHeaderRow || position == stickerSizeHeaderRow) {
                return 2;
            } else if (position == mediaSwipeByTapRow || position == jumpChannelRow || position == hideKeyboardRow ||
                    position == playGifAsVideoRow || position == showFolderWhenForwardRow ||
                    position == showFolderIconsRow || position == rearCameraStartingRow  || position == confirmSendRow ||
                    position == showGreetings || position == showTranslateRow || position == showAddToSMRow ||
                    position == showMessageDetails || position == showNoQuoteForwardRow || position == showReportRow ||
                    position == scrollableRow || position == showDeleteRow) {
                return 3;
            } else if (position == stickerSizeRow) {
                return 4;
            }
            return 1;
        }
    }
}
