package it.owlgram.android.settings;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.CreationTextCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.ChatAttachAlert;
import org.telegram.ui.Components.ChatAttachAlertDocumentLayout;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.FlickerLoadingView;
import org.telegram.ui.Components.LayoutHelper;

import java.io.File;
import java.util.ArrayList;

import it.owlgram.android.OwlConfig;
import it.owlgram.android.components.EmojiSetCell;
import it.owlgram.android.helpers.CustomEmojiHelper;
import it.owlgram.android.helpers.FileDownloadHelper;
import it.owlgram.android.helpers.FileUnzipHelper;

public class EmojiPackSettings extends BaseSettingsActivity implements NotificationCenter.NotificationCenterDelegate, ChatAttachAlertDocumentLayout.DocumentSelectActivityDelegate {

    private int generalHeaderRow;
    private int emojiDividerRow;
    private int useSystemEmojiRow;
    private int useCustomEmojiHeaderRow;
    private int customEmojiHintRow;
    private int customEmojiStartRow;
    private int customEmojiEndRow;
    private int customEmojiAddRow;
    private int emojiPackHeaderRow;
    private int emojiPacksStartRow;
    private int emojiPacksEndRow;
    private int emojiHintRow;

    ChatAttachAlert chatAttachAlert;

    @Override
    public View createView(Context context) {
        View v = super.createView(context);

        FlickerLoadingView flickerLoadingView = new FlickerLoadingView(context, getResourceProvider());
        flickerLoadingView.setViewType(FlickerLoadingView.STICKERS_TYPE);
        flickerLoadingView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        emptyView = new EmptyTextProgressView(context, flickerLoadingView);
        ((FrameLayout) fragmentView).addView(emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setEmptyView(emptyView);
        emptyView.showProgress(false);
        return v;
    }

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        super.onItemClick(view, position, x, y);
        if (position >= emojiPacksStartRow && position < emojiPacksEndRow) {
            EmojiSetCell cell = (EmojiSetCell) view;
            int selectedOld = getSelectedOld();
            if (selectedOld != -1) {
                String currentDownloading = getCurrentDownloading();
                String currentUnzipping = getCurrentUnzipping();
                boolean isDownloading = FileDownloadHelper.isRunningDownload(cell.packId);
                boolean isUnzipping = FileUnzipHelper.isRunningUnzip(cell.packId);
                if (!isDownloading && currentDownloading != null) {
                    FileDownloadHelper.cancel(currentDownloading);
                }
                if (!isUnzipping && currentUnzipping != null) {
                    FileUnzipHelper.cancel(currentUnzipping);
                }
                if (isDownloading || isUnzipping) return;
                if (CustomEmojiHelper.emojiDir(cell.packId, cell.versionWithMD5).exists() || cell.packId.equals("default")) {
                    OwlConfig.setEmojiPackSelected(cell.packId);
                    cell.setChecked(true, true);
                    if (selectedOld != position) {
                        listAdapter.notifyItemChanged(selectedOld, PARTIAL);
                    }
                    Emoji.reloadEmoji();
                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.emojiLoaded);
                    if (OwlConfig.useSystemEmoji) {
                        OwlConfig.toggleUseSystemEmoji();
                        listAdapter.notifyItemChanged(useSystemEmojiRow, PARTIAL);
                    }
                } else {
                    cell.setProgress(true, true);
                    CustomEmojiHelper.mkDirs();
                    FileDownloadHelper.downloadFile(ApplicationLoader.applicationContext, cell.packId, CustomEmojiHelper.emojiTmp(cell.packId), cell.packFileLink);
                    listAdapter.notifyItemChanged(selectedOld, PARTIAL);
                }
            }
        } else if (position == useSystemEmojiRow) {
            OwlConfig.toggleUseSystemEmoji();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(OwlConfig.useSystemEmoji);
            }
            Emoji.reloadEmoji();
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.emojiLoaded);
            if (OwlConfig.useSystemEmoji) {
                FileDownloadHelper.cancel(getCurrentDownloading());
                FileUnzipHelper.cancel(getCurrentUnzipping());
            }
            listAdapter.notifyItemChanged(getSelectedOld(), PARTIAL);
        } else if (position == customEmojiAddRow) {
            chatAttachAlert = new ChatAttachAlert(context, EmojiPackSettings.this, false, false);
            chatAttachAlert.setEmojiPicker();
            chatAttachAlert.init();
            chatAttachAlert.show();
        } else if (position >= customEmojiStartRow && position < customEmojiEndRow) {
            EmojiSetCell cell = (EmojiSetCell) view;
            cell.setChecked(true, true);
            int selectedOld = getSelectedOld();
            if (selectedOld != position) {
                listAdapter.notifyItemChanged(selectedOld, PARTIAL);
            }
            OwlConfig.setEmojiPackSelected(cell.packId);
            FileDownloadHelper.cancel(getCurrentDownloading());
            FileUnzipHelper.cancel(getCurrentUnzipping());
            Emoji.reloadEmoji();
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.emojiLoaded);
            if (OwlConfig.useSystemEmoji) {
                OwlConfig.toggleUseSystemEmoji();
                listAdapter.notifyItemChanged(useSystemEmojiRow, PARTIAL);
            }
        }
    }

    private int getSelectedOld() {
        return getSelectedOld(CustomEmojiHelper.getEmojiPacksInfo(), false);
    }
    private int getSelectedOld(ArrayList<CustomEmojiHelper.EmojiPackBase> packBases, boolean isCustom) {
        int position = packBases
                .stream()
                .filter(emojiPackInfo -> emojiPackInfo.getPackId().equals(OwlConfig.emojiPackSelected))
                .findFirst()
                .map(packBases::indexOf)
                .orElse(-1);
        if (position != -1) {
            return position + (isCustom ? customEmojiStartRow:emojiPacksStartRow);
        } else if (isCustom) {
            return -1;
        }
        return getSelectedOld(CustomEmojiHelper.getEmojiCustomPacksInfo(), true);
    }

    private String getCurrentUnzipping() {
        return CustomEmojiHelper.getEmojiPacksInfo()
                .stream()
                .map(CustomEmojiHelper.EmojiPackBase::getPackId)
                .filter(FileUnzipHelper::isRunningUnzip)
                .findFirst()
                .orElse(null);
    }

    private String getCurrentDownloading() {
        return CustomEmojiHelper.getEmojiPacksInfo()
                .stream()
                .map(CustomEmojiHelper.EmojiPackBase::getPackId)
                .filter(FileDownloadHelper::isRunningDownload)
                .findFirst()
                .orElse(null);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void attachListeners(EmojiSetCell cell) {
        FileDownloadHelper.addListener(cell.packId, "emojiCellSettings", new FileDownloadHelper.FileDownloadListener() {
            @Override
            public void onPreStart(String id) {
                if (cell.packId.equals(id)) {
                    cell.setProgress(0, 0, true);
                }
            }

            @Override
            public void onProgressChange(String id, int percentage, long downBytes, long totBytes) {
                if (cell.packId.equals(id)) {
                    cell.setProgress(percentage, downBytes, false);
                }
            }

            @Override
            public void onFinished(String id) {
                cell.checkDownloaded();
                if (cell.packId.equals(id)) {
                    if (CustomEmojiHelper.emojiTmpDownloaded(cell.packId)) {
                        FileUnzipHelper.unzipFile(ApplicationLoader.applicationContext, cell.packId, CustomEmojiHelper.emojiTmp(cell.packId), CustomEmojiHelper.emojiDir(cell.packId, cell.versionWithMD5));
                    } else {
                        CustomEmojiHelper.emojiTmp(cell.packId).delete();
                        listAdapter.notifyItemChanged(getSelectedOld(), PARTIAL);
                    }
                }
            }
        });
        FileUnzipHelper.addListener(cell.packId, "emojiCellSettings", (id) -> {
            if (cell.packId.equals(id)) {
                CustomEmojiHelper.emojiTmp(cell.packId).delete();
                if (CustomEmojiHelper.emojiDir(cell.packId, cell.versionWithMD5).exists()) {
                    OwlConfig.setEmojiPackSelected(cell.packId);
                    CustomEmojiHelper.deleteOldVersions(cell.packId, cell.versionWithMD5);
                    Emoji.reloadEmoji();
                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.emojiLoaded);
                    if (OwlConfig.useSystemEmoji) {
                        OwlConfig.toggleUseSystemEmoji();
                        listAdapter.notifyItemChanged(useSystemEmojiRow, PARTIAL);
                    }
                }
            }
            listAdapter.notifyItemChanged(getSelectedOld(), PARTIAL);
            cell.checkDownloaded();
        });
    }

    @Override
    public boolean onFragmentCreate() {
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiPacksLoaded);
        CustomEmojiHelper.loadEmojisInfo();
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiPacksLoaded);
    }

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString("EmojiSets", R.string.EmojiSets);
    }

    @Override
    protected void updateRowsId() {
        super.updateRowsId();
        generalHeaderRow = -1;
        useSystemEmojiRow = -1;
        emojiDividerRow = -1;
        useCustomEmojiHeaderRow = -1;
        customEmojiAddRow = -1;
        customEmojiStartRow = -1;
        customEmojiEndRow = -1;
        customEmojiHintRow = -1;
        emojiPackHeaderRow = -1;
        emojiPacksStartRow = -1;
        emojiPacksEndRow = -1;
        emojiHintRow = -1;
        if (CustomEmojiHelper.loadedPackInfo()) {
            generalHeaderRow =  rowCount++;
            useSystemEmojiRow = rowCount++;
            emojiDividerRow = rowCount++;
            useCustomEmojiHeaderRow = rowCount++;
            customEmojiStartRow = rowCount;
            rowCount += CustomEmojiHelper.getEmojiCustomPacksInfo().size();
            customEmojiEndRow = rowCount;
            customEmojiAddRow = rowCount++;
            customEmojiHintRow = rowCount++;

            emojiPackHeaderRow = rowCount++;
            emojiPacksStartRow = rowCount;
            rowCount += CustomEmojiHelper.getEmojiPacksInfo().size();
            emojiPacksEndRow = rowCount;
            emojiHintRow = rowCount++;
        }
    }

    @Override
    protected BaseListAdapter createAdapter() {
        return new ListAdapter();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.emojiPacksLoaded) {
            if (!CustomEmojiHelper.loadedPackInfo()) {
                CustomEmojiHelper.loadEmojisInfo();
            } else {
                if (listAdapter != null) {
                    updateRowsId();
                    showItemsAnimated();
                    listAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void didSelectFiles(ArrayList<String> files, String caption, ArrayList<MessageObject> fMessages, boolean notify, int scheduleDate) {
        ArrayList<File> filesToUpload = new ArrayList<>();
        for (String file : files) {
            File f = new File(file);
            if (f.exists()) {
                filesToUpload.add(f);
            }
        }
        processFiles(filesToUpload);
    }

    private class ListAdapter extends BaseListAdapter {
        @Override
        protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, boolean partial) {
            switch (ViewType.fromInt(holder.getItemViewType())) {
                case HEADER:
                    HeaderCell headerViewHolder = (HeaderCell) holder.itemView;
                    if (position == emojiPackHeaderRow) {
                        headerViewHolder.setText(LocaleController.getString("EmojiSets", R.string.EmojiSets));
                    } else if (position == generalHeaderRow) {
                        headerViewHolder.setText(LocaleController.getString("General", R.string.General));
                    } else if (position == useCustomEmojiHeaderRow) {
                        headerViewHolder.setText(LocaleController.getString("MyEmojiSets", R.string.MyEmojiSets));
                    }
                    break;
                case EMOJI_PACK_SET_CELL:
                    EmojiSetCell emojiPackSetCell = (EmojiSetCell) holder.itemView;
                    CustomEmojiHelper.EmojiPackBase emojiPackInfo = null;
                    if (position >= emojiPacksStartRow && position < emojiPacksEndRow) {
                        emojiPackInfo = CustomEmojiHelper.getEmojiPacksInfo().get(position - emojiPacksStartRow);
                    } else if (position >= customEmojiStartRow && position < customEmojiEndRow) {
                        emojiPackInfo = CustomEmojiHelper.getEmojiCustomPacksInfo().get(position - customEmojiStartRow);
                    }
                    if (emojiPackInfo != null) {
                        emojiPackSetCell.setChecked(emojiPackInfo.getPackId().equals(OwlConfig.emojiPackSelected) && getCurrentDownloading() == null && !OwlConfig.useSystemEmoji, partial);
                        emojiPackSetCell.setData(
                                emojiPackInfo,
                                partial,
                                true
                        );
                        if (emojiPackInfo instanceof CustomEmojiHelper.EmojiPackInfo) {
                            attachListeners(emojiPackSetCell);
                        }
                    }
                    break;
                case TEXT_HINT_WITH_PADDING:
                    TextInfoPrivacyCell textInfoPrivacyCell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == emojiHintRow) {
                        textInfoPrivacyCell.setText(LocaleController.getString("EmojiSetHint", R.string.EmojiSetHint));
                    } else if (position == customEmojiHintRow) {
                        textInfoPrivacyCell.setText(LocaleController.getString("CustomEmojiSetHint", R.string.CustomEmojiSetHint));
                    }
                    break;
                case SWITCH:
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    if (position == useSystemEmojiRow) {
                        if (!partial) textCheckCell.setTextAndValueAndCheck(LocaleController.getString("UseSystemEmojis", R.string.UseSystemEmojis), LocaleController.getString("UseSystemEmojisDesc", R.string.UseSystemEmojisDesc), OwlConfig.useSystemEmoji, true, false);
                        if (partial) textCheckCell.setChecked(OwlConfig.useSystemEmoji);
                    }
                    break;
                case CREATION_TEXT_CELL:
                    CreationTextCell creationTextCell = (CreationTextCell) holder.itemView;
                    if (position == customEmojiAddRow) {
                        Drawable drawable1 = creationTextCell.getContext().getResources().getDrawable(R.drawable.poll_add_circle);
                        Drawable drawable2 = creationTextCell.getContext().getResources().getDrawable(R.drawable.poll_add_plus);
                        drawable1.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_switchTrackChecked), PorterDuff.Mode.MULTIPLY));
                        drawable2.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_checkboxCheck), PorterDuff.Mode.MULTIPLY));
                        CombinedDrawable combinedDrawable = new CombinedDrawable(drawable1, drawable2);
                        creationTextCell.setTextAndIcon(LocaleController.getString("AddEmojiSet", R.string.AddEmojiSet), combinedDrawable, false);
                    }
                    break;
            }
        }

        @Override
        protected ViewType getViewType(int position) {
            if (position == emojiPackHeaderRow || position == generalHeaderRow || position == useCustomEmojiHeaderRow) {
                return ViewType.HEADER;
            } else if ((position >= emojiPacksStartRow && position < emojiPacksEndRow) || (position >= customEmojiStartRow && position < customEmojiEndRow)) {
                return ViewType.EMOJI_PACK_SET_CELL;
            } else if (position == emojiHintRow || position == customEmojiHintRow) {
                return ViewType.TEXT_HINT_WITH_PADDING;
            } else if (position == useSystemEmojiRow) {
                return ViewType.SWITCH;
            } else if (position == emojiDividerRow) {
                return ViewType.SHADOW;
            } else if (position == customEmojiAddRow) {
                return ViewType.CREATION_TEXT_CELL;
            }
            throw new IllegalArgumentException("Invalid position");
        }

        @Override
        protected boolean isEnabled(ViewType viewType, int position) {
            return viewType == ViewType.EMOJI_PACK_SET_CELL || viewType == ViewType.SWITCH || viewType == ViewType.CREATION_TEXT_CELL;
        }
    }

    public void startDocumentSelectActivity() {
        try {
            Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
            photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            photoPickerIntent.setType("application/*");
            startActivityForResult(photoPickerIntent, 21);
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        if (requestCode == 21) {
            if (data == null) {
                return;
            }

            if (chatAttachAlert != null) {
                boolean apply = false;
                ArrayList<File> files = new ArrayList<>();
                if (data.getData() != null) {
                    String path = AndroidUtilities.getPath(data.getData());
                    if (path != null) {
                        File file = new File(path);
                        if (chatAttachAlert.getDocumentLayout().isEmojiFont(file)) {
                            apply = true;
                            files.add(file);
                        }
                    }
                } else if (data.getClipData() != null) {
                    ClipData clipData = data.getClipData();
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        String path = clipData.getItemAt(i).getUri().toString();
                        if (chatAttachAlert.getDocumentLayout().isEmojiFont(new File(path))) {
                            apply = true;
                            files.add(new File(path));
                        } else {
                            apply = false;
                            break;
                        }
                    }
                }
                if (apply) {
                    chatAttachAlert.dismiss();
                    processFiles(files);
                }
            }
        }
    }

    public void processFiles(ArrayList<File> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        AlertDialog progressDialog = new AlertDialog(getParentActivity(), 3);
        new Thread(() -> {
            int added = 0;
            for (File file : files) {
                if (CustomEmojiHelper.installEmoji(file)) {
                    added++;
                }
            }
            int finalAdded = added;
            AndroidUtilities.runOnUIThread(() -> {
                progressDialog.dismiss();
                updateRowsId();
                listAdapter.notifyItemRangeInserted(customEmojiEndRow, finalAdded);
            });
        }).start();
        progressDialog.setCanCancel(false);
        progressDialog.show();
    }
}
