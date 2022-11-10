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

import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
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
import it.owlgram.android.StoreUtils;
import it.owlgram.android.components.UpdateCell;
import it.owlgram.android.components.UpdateCheckCell;
import it.owlgram.android.updates.ApkDownloader;
import it.owlgram.android.updates.UpdateManager;

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
    private boolean checkingUpdates;
    private TextCheckCell changeBetaMode;

    private UpdateManager.UpdateAvailable updateAvailable;
    private UpdateCheckCell updateCheckCell;
    private UpdateCell updateCell;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        String data = OwlConfig.updateData;
        try {
            if (data.length() > 0) {
                JSONObject jsonObject = new JSONObject(data);
                updateAvailable = UpdateManager.loadUpdate(jsonObject);
                if (updateAvailable.version <= UpdateManager.currentVersion()) {
                    updateAvailable = null;
                }
            }
        } catch (Exception ignored) {
        }
        updateRowsId();
        return true;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString("OwlUpdates", R.string.OwlUpdates));
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
                if (!ApkDownloader.updateDownloaded() && !checkingUpdates) {
                    OwlConfig.toggleBetaUpdates();
                    ApkDownloader.cancel();
                    ApkDownloader.deleteUpdate();
                    if (updateAvailable != null) {
                        OwlConfig.setUpdateData("");
                        OwlConfig.remindUpdate(updateAvailable.version);
                        updateAvailable = null;
                        listAdapter.notifyItemRangeRemoved(updateSectionAvailableRow, 2);
                        listAdapter.notifyItemRangeChanged(updateSectionAvailableRow, 1);
                        updateRowsId();
                    }
                    checkUpdates();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(OwlConfig.betaUpdates);
                    }
                }
            } else if (position == notifyWhenAvailableRow) {
                OwlConfig.toggleNotifyUpdates();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.notifyUpdates);
                }
            } else if (position == apkChannelRow) {
                MessagesController.getInstance(currentAccount).openByUserName("OwlGramAPKs", this, 1);
            }
        });
        ApkDownloader.setDownloadListener(new ApkDownloader.UpdateListener() {
            @Override
            public void onPreStart() {
            }

            @Override
            public void onProgressChange(int percentage, long downBytes, long totBytes) {
                if (updateCell != null) {
                    updateCell.setPercentage(percentage, downBytes, totBytes);
                }
            }

            @Override
            public void onFinished() {
                if (updateCell != null) {
                    changeBetaMode.setEnabled(!ApkDownloader.updateDownloaded(), null);
                    updateCheckCell.setCanCheckForUpdate(!ApkDownloader.updateDownloaded());
                    if (ApkDownloader.updateDownloaded()) {
                        updateCell.setInstallMode();
                    } else {
                        updateCell.setConfirmMode();
                    }
                }
            }
        });
        return fragmentView;
    }

    private void updateRowsId() {
        rowCount = 0;
        updateSectionAvailableRow = -1;
        updateSectionDividerRow = -1;
        betaUpdatesRow = -1;

        if (updateAvailable != null) {
            updateSectionAvailableRow = rowCount++;
            updateSectionDividerRow = rowCount++;
        }

        updateSectionHeader = rowCount++;
        updateCheckRow = rowCount++;
        if (!StoreUtils.isDownloadedFromAnyStore()) {
            betaUpdatesRow = rowCount++;
        }
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
                    OwlgramUpdateSettings.this.updateCell = updateCell;
                    updateCell.setUpdate(
                            updateAvailable.title,
                            updateAvailable.desc,
                            updateAvailable.note,
                            updateAvailable.banner
                    );
                    if (ApkDownloader.isRunningDownload()) {
                        updateCell.setDownloadMode();
                    } else {
                        if (ApkDownloader.updateDownloaded()) {
                            updateCell.setInstallMode();
                        } else {
                            updateCell.setConfirmMode();
                        }
                    }
                    updateCell.setPercentage(ApkDownloader.percentage(), ApkDownloader.downloadedBytes(), ApkDownloader.totalBytes());
                    break;
                case 3:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == updateSectionHeader) {
                        headerCell.setText(LocaleController.getString("InAppUpdates", R.string.InAppUpdates));
                    }
                    break;
                case 4:
                    UpdateCheckCell updateCheckCell = (UpdateCheckCell) holder.itemView;
                    updateCheckCell.loadLastStatus();
                    updateCheckCell.setCanCheckForUpdate(!ApkDownloader.updateDownloaded());
                    break;
                case 5:
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    textCheckCell.setEnabled(!ApkDownloader.updateDownloaded() || position != betaUpdatesRow, null);
                    if (position == betaUpdatesRow) {
                        changeBetaMode = textCheckCell;
                        changeBetaMode.setTextAndValueAndCheck(LocaleController.getString("InstallBetas", R.string.InstallBetas), LocaleController.getString("InstallBetasDesc", R.string.InstallBetasDesc), OwlConfig.betaUpdates, true, true);
                    } else if (position == notifyWhenAvailableRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("AutoUpdate", R.string.AutoUpdate), LocaleController.getString("AutoUpdatePrompt", R.string.AutoUpdatePrompt), OwlConfig.notifyUpdates, true, true);
                    }
                    break;
                case 6:
                    TextCell textCell = (TextCell) holder.itemView;
                    if (position == apkChannelRow) {
                        textCell.setTextAndValue(LocaleController.getString("APKsChannel", R.string.APKsChannel), "@OwlGramAPKs", false);
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
                        protected void onInstallUpdate() {
                            super.onInstallUpdate();
                            ApkDownloader.installUpdate(getParentActivity());
                        }

                        @Override
                        protected void onConfirmUpdate() {
                            super.onConfirmUpdate();
                            if (!StoreUtils.isDownloadedFromAnyStore()) {
                                if (!ApkDownloader.isRunningDownload()) {
                                    ApkDownloader.downloadAPK(mContext, updateAvailable.link_file, updateAvailable.version);
                                    updateCell.setDownloadMode();
                                }
                            } else if (StoreUtils.isFromPlayStore()) {
                                Browser.openUrl(getContext(), BuildVars.PLAYSTORE_APP_URL);
                            }
                        }

                        @Override
                        protected void onRemindUpdate() {
                            super.onRemindUpdate();
                            updateCheckCell.setCheckTime();
                            if (updateAvailable != null) {
                                ApkDownloader.deleteUpdate();
                                OwlConfig.setUpdateData("");
                                OwlConfig.remindUpdate(updateAvailable.version);
                                updateAvailable = null;
                                listAdapter.notifyItemRangeRemoved(updateSectionAvailableRow, 2);
                                listAdapter.notifyItemRangeChanged(updateSectionAvailableRow, 1);
                                updateRowsId();
                            }
                        }
                    };
                    break;
                case 3:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 4:
                    view = new UpdateCheckCell(mContext, true) {
                        @Override
                        protected void onCheckUpdate() {
                            super.onCheckUpdate();
                            if (!ApkDownloader.updateDownloaded()) {
                                checkUpdates();
                            }
                        }
                    };
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    updateCheckCell = (UpdateCheckCell) view;
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

    private void checkUpdates() {
        updateCheckCell.setCheckingStatus();
        UpdateManager.checkUpdates(new UpdateManager.UpdateCallback() {
            @Override
            public void onSuccess(Object updateResult) {
                checkingUpdates = false;
                OwlConfig.saveLastUpdateCheck();
                if (updateResult instanceof UpdateManager.UpdateAvailable) {
                    updateCheckCell.setUpdateAvailableStatus();
                    if (updateAvailable == null) {
                        OwlConfig.setUpdateData(updateResult.toString());
                        updateAvailable = (UpdateManager.UpdateAvailable) updateResult;
                        listAdapter.notifyItemRangeInserted(updateSectionAvailableRow, 2);
                        listAdapter.notifyItemRangeChanged(updateSectionAvailableRow, 1);
                        updateRowsId();
                        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateAvailable);
                    }
                } else {
                    updateCheckCell.setCheckTime();
                    if (updateAvailable != null) {
                        OwlConfig.setUpdateData("");
                        updateAvailable = null;
                        listAdapter.notifyItemRangeRemoved(updateSectionAvailableRow, 2);
                        listAdapter.notifyItemRangeChanged(updateSectionAvailableRow, 1);
                        updateRowsId();
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                FileLog.e(e);
                updateCheckCell.setFailedStatus();
            }
        });
    }
}
