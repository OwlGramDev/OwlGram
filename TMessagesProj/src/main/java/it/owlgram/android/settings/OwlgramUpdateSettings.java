package it.owlgram.android.settings;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;

import it.owlgram.android.OwlConfig;
import it.owlgram.android.StoreUtils;
import it.owlgram.android.components.UpdateCell;
import it.owlgram.android.components.UpdateCheckCell;
import it.owlgram.android.updates.ApkDownloader;
import it.owlgram.android.updates.AppDownloader;
import it.owlgram.android.updates.PlayStoreAPI;
import it.owlgram.android.updates.UpdateManager;

public class OwlgramUpdateSettings extends BaseSettingsActivity {

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

    // TYPES
    private static final int TYPE_UPDATE = 200;
    private static final int TYPE_UPDATE_CHECK = 201;

    @Override
    public boolean onFragmentCreate() {
        String data = OwlConfig.updateData;
        try {
            if (data.length() > 0) {
                JSONObject jsonObject = new JSONObject(data);
                updateAvailable = UpdateManager.loadUpdate(jsonObject);
                if (updateAvailable.isReminded()) {
                    updateAvailable = null;
                } else if (updateAvailable.version <= BuildVars.BUILD_VERSION) {
                    updateAvailable = null;
                    OwlConfig.saveUpdateStatus(0);
                }
            }
        } catch (Exception ignored) {
        }
        AppDownloader.setDownloadListener(new AppDownloader.UpdateListener() {
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
                if (!StoreUtils.isFromPlayStore()) changeBetaMode.setEnabled(!AppDownloader.updateDownloaded(), null);
                updateCheckCell.setCanCheckForUpdate(!AppDownloader.updateDownloaded() && !PlayStoreAPI.isRunningDownload());
                if (PlayStoreAPI.updateDownloaded()) {
                    updateCheckCell.setDownloaded();
                }
                if (updateCell != null) {
                    if (AppDownloader.updateDownloaded()) {
                        updateCell.setInstallMode();
                    } else {
                        updateCell.setConfirmMode();
                    }
                }
            }
        });
        return super.onFragmentCreate();
    }

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString("OwlUpdates", R.string.OwlUpdates);
    }

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        if (position == betaUpdatesRow) {
            if (!ApkDownloader.updateDownloaded() && !checkingUpdates) {
                OwlConfig.toggleBetaUpdates();
                ApkDownloader.cancel();
                ApkDownloader.deleteUpdate();
                if (updateAvailable != null) {
                    OwlConfig.remindUpdate(updateAvailable.version);
                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateAvailable);
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
    }

    @Override
    protected void updateRowsId() {
        super.updateRowsId();
        updateSectionAvailableRow = -1;
        updateSectionDividerRow = -1;
        betaUpdatesRow = -1;

        if (updateAvailable != null && !StoreUtils.isDownloadedFromAnyStore()) {
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

    @Override
    protected BaseListAdapter createAdapter() {
        return new ListAdapter();
    }

    private class ListAdapter extends BaseListAdapter {

        @Override
        protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, boolean partial) {
            switch (holder.getItemViewType()) {
                case TYPE_SHADOW:
                    holder.itemView.setBackground(Theme.getThemedDrawable(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case TYPE_UPDATE:
                    UpdateCell updateCell = (UpdateCell) holder.itemView;
                    OwlgramUpdateSettings.this.updateCell = updateCell;
                    updateCell.setUpdate(
                            updateAvailable.title,
                            updateAvailable.desc,
                            updateAvailable.note,
                            updateAvailable.banner
                    );
                    if (AppDownloader.isRunningDownload()) {
                        updateCell.setDownloadMode();
                    } else {
                        if (AppDownloader.updateDownloaded()) {
                            updateCell.setInstallMode();
                        } else {
                            updateCell.setConfirmMode();
                        }
                    }
                    updateCell.setPercentage(AppDownloader.getDownloadProgress(), AppDownloader.downloadedBytes(), AppDownloader.totalBytes());
                    break;
                case TYPE_HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == updateSectionHeader) {
                        headerCell.setText(LocaleController.getString("InAppUpdates", R.string.InAppUpdates));
                    }
                    break;
                case TYPE_UPDATE_CHECK:
                    UpdateCheckCell updateCheckCell = (UpdateCheckCell) holder.itemView;
                    updateCheckCell.loadLastStatus();
                    updateCheckCell.setCanCheckForUpdate(!AppDownloader.updateDownloaded() && !PlayStoreAPI.isRunningDownload());
                    if (PlayStoreAPI.updateDownloaded()) {
                        updateCheckCell.setDownloaded();
                    }
                    break;
                case TYPE_SWITCH:
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    textCheckCell.setEnabled(!AppDownloader.updateDownloaded() || position != betaUpdatesRow, null);
                    if (position == betaUpdatesRow) {
                        changeBetaMode = textCheckCell;
                        changeBetaMode.setTextAndValueAndCheck(LocaleController.getString("InstallBetas", R.string.InstallBetas), LocaleController.getString("InstallBetasDesc", R.string.InstallBetasDesc), OwlConfig.betaUpdates, true, true);
                    } else if (position == notifyWhenAvailableRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("AutoUpdate", R.string.AutoUpdate), LocaleController.getString("AutoUpdatePrompt", R.string.AutoUpdatePrompt), OwlConfig.notifyUpdates, true, true);
                    }
                    break;
                case TYPE_TEXT_CELL:
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
            return type == TYPE_SWITCH || type == TYPE_TEXT_CELL;
        }

        @Override
        protected View onCreateViewHolder(int viewType) {
            View view = null;
            switch (viewType) {
                case TYPE_UPDATE:
                    view = new UpdateCell(context) {
                        @Override
                        protected void onInstallUpdate() {
                            super.onInstallUpdate();
                            ApkDownloader.installUpdate(getParentActivity());
                        }

                        @Override
                        protected void onConfirmUpdate() {
                            super.onConfirmUpdate();
                            if (!ApkDownloader.isRunningDownload()) {
                                ApkDownloader.downloadAPK(context, updateAvailable.link_file, updateAvailable.version);
                                updateCell.setDownloadMode();
                            }
                        }

                        @Override
                        protected void onRemindUpdate() {
                            super.onRemindUpdate();
                            updateCheckCell.setCheckTime();
                            if (updateAvailable != null) {
                                ApkDownloader.deleteUpdate();
                                OwlConfig.remindUpdate(updateAvailable.version);
                                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateAvailable);
                                updateCheckCell.setCheckTime();
                                updateAvailable = null;
                                listAdapter.notifyItemRangeRemoved(updateSectionAvailableRow, 2);
                                listAdapter.notifyItemRangeChanged(updateSectionAvailableRow, 1);
                                updateRowsId();
                            }
                        }
                    };
                    break;
                case TYPE_UPDATE_CHECK:
                    view = new UpdateCheckCell(context, true) {
                        @Override
                        protected void onCheckUpdate() {
                            super.onCheckUpdate();
                            if (PlayStoreAPI.updateDownloaded()) {
                                PlayStoreAPI.installUpdate();
                            } else if (StoreUtils.isFromPlayStore() && !PlayStoreAPI.updateDownloaded() && OwlConfig.lastUpdateStatus == 1 && updateAvailable != null) {
                                PlayStoreAPI.openUpdatePopup(getParentActivity());
                            } else if (!AppDownloader.updateDownloaded()) {
                                checkUpdates();
                            }
                        }
                    };
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    updateCheckCell = (UpdateCheckCell) view;
                    break;
            }
            return view;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == updateSectionDividerRow) {
                return TYPE_SHADOW;
            } else if (position == updateSectionAvailableRow) {
                return TYPE_UPDATE;
            } else if (position == updateSectionHeader) {
                return TYPE_HEADER;
            } else if (position == updateCheckRow) {
                return TYPE_UPDATE_CHECK;
            } else if (position == betaUpdatesRow || position == notifyWhenAvailableRow) {
                return TYPE_SWITCH;
            } else if (position == apkChannelRow) {
                return TYPE_TEXT_CELL;
            }
            throw new IllegalArgumentException("Invalid position");
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
                    if (updateAvailable == null) {
                        OwlConfig.saveUpdateStatus(1);
                        OwlConfig.remindUpdate(-1);
                        OwlConfig.setUpdateData(updateResult.toString());
                        updateAvailable = (UpdateManager.UpdateAvailable) updateResult;
                        if (StoreUtils.isFromPlayStore()) {
                            PlayStoreAPI.openUpdatePopup(getParentActivity());
                        } else {
                            listAdapter.notifyItemRangeInserted(updateSectionAvailableRow, 2);
                            listAdapter.notifyItemRangeChanged(updateSectionAvailableRow, 1);
                            updateRowsId();
                        }
                        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateAvailable);
                    }
                    updateCheckCell.setUpdateAvailableStatus();
                } else {
                    OwlConfig.saveUpdateStatus(0);
                    updateCheckCell.setCheckTime();
                    if (updateAvailable != null) {
                        OwlConfig.setUpdateData("");
                        updateAvailable = null;
                        if (!StoreUtils.isFromPlayStore()) {
                            listAdapter.notifyItemRangeRemoved(updateSectionAvailableRow, 2);
                            listAdapter.notifyItemRangeChanged(updateSectionAvailableRow, 1);
                            updateRowsId();
                        }
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                FileLog.e(e);
                OwlConfig.saveUpdateStatus(2);
                updateCheckCell.setFailedStatus();
            }
        });
    }
}
