package it.owlgram.android.settings;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RLottieImageView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.PasscodeActivity;

import java.util.ArrayList;

import it.owlgram.android.helpers.PasscodeHelper;

public class DoubleBottomSettings extends BaseFragment {
    private int rowCount;
    private ListAdapter listAdapter;
    private int dbAnRow;
    private int hintRow;
    private int accountsHeaderRow;
    private int accountsStartRow;
    private int accountsEndRow;
    private int accountsDetailsRow;
    private int disableDoubleBottomRow;
    private final ArrayList<TLRPC.User> accounts = new ArrayList<>();

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        updateRowsId();
        return true;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString("DoubleBottom", R.string.DoubleBottom));
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
        listView.setOnItemClickListener((view, position) -> {
           if (position == disableDoubleBottomRow) {
               AlertDialog alertDialog = new AlertDialog.Builder(getParentActivity())
                       .setTitle(LocaleController.getString("DisableDoubleBottom", R.string.DisableDoubleBottom))
                       .setMessage(LocaleController.getString("DisableDoubleBottomConfirmMessage", R.string.DisableDoubleBottomConfirmMessage))
                       .setNegativeButton(LocaleController.getString(R.string.Cancel), null)
                       .setPositiveButton(LocaleController.getString(R.string.DisablePasscodeTurnOff), (dialog, which) -> {
                           PasscodeHelper.disableDoubleBottom();
                           NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didSetPasscode);
                           finishFragment();
                       }).create();
               alertDialog.show();
               ((TextView)alertDialog.getButton(Dialog.BUTTON_POSITIVE)).setTextColor(Theme.getColor(Theme.key_dialogTextRed));
           } else if (position >= accountsStartRow && position < accountsEndRow) {
               TLRPC.User user = accounts.get(position - accountsStartRow);
               if (PasscodeHelper.isDoubleBottomAccount(user.id)) {
                   final ArrayList<String> items = new ArrayList<>();
                   final ArrayList<Integer> icons = new ArrayList<>();
                   final ArrayList<Integer> actions = new ArrayList<>();

                   items.add(LocaleController.getString("ChangePasscode", R.string.ChangePasscode));
                   icons.add(R.drawable.edit_passcode);
                   actions.add(0);
                   items.add(LocaleController.getString("DisablePasscode", R.string.DisablePasscode));
                   icons.add(R.drawable.msg_disable);
                   actions.add(1);

                   AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                   builder.setItems(items.toArray(new CharSequence[actions.size()]), AndroidUtilities.toIntArray(icons), (dialogInterface, i) -> {
                       if (actions.get(i) == 0) {
                           presentFragment(new PasscodeActivity(PasscodeActivity.TYPE_SETUP_CODE, user.id));
                       } else if (actions.get(i) == 1) {
                           AlertDialog alertDialog = new AlertDialog.Builder(getParentActivity())
                                   .setTitle(LocaleController.getString("DisablePasscode", R.string.DisablePasscode))
                                   .setMessage(LocaleController.getString("DisablePasscodeConfirmMessage", R.string.DisablePasscodeConfirmMessage))
                                   .setNegativeButton(LocaleController.getString(R.string.Cancel), null)
                                   .setPositiveButton(LocaleController.getString(R.string.DisablePasscodeTurnOff), (dialog, which) -> {
                                       PasscodeHelper.removePasscodeForAccount(user.id);
                                       NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didSetPasscode);
                                       updateRowsId();
                                       if (!PasscodeHelper.existAtLeastOnePasscode()) finishFragment();
                                   }).create();
                           alertDialog.show();
                           ((TextView)alertDialog.getButton(Dialog.BUTTON_POSITIVE)).setTextColor(Theme.getColor(Theme.key_dialogTextRed));
                       }
                   });
                   AlertDialog alertDialog = builder.create();
                   showDialog(alertDialog);
                   alertDialog.setItemColor(items.size() - 1, Theme.getColor(Theme.key_dialogTextRed2), Theme.getColor(Theme.key_dialogRedIcon));
               } else {
                   presentFragment(new PasscodeActivity(PasscodeActivity.TYPE_SETUP_CODE, user.id));
               }
           }
        });
        if(listView.getItemAnimator() != null){
            ((DefaultItemAnimator) listView.getItemAnimator()).setDelayAnimations(false);
        }
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        return fragmentView;
    }

    private int getActiveAccounts() {
        accounts.clear();
        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
            TLRPC.User u = AccountInstance.getInstance(a).getUserConfig().getCurrentUser();
            if (u != null) {
                if (u.id == UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId()) {
                    accounts.add(0, u);
                } else {
                    accounts.add(u);
                }
            }
        }
        return accounts.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateRowsId() {
        rowCount = 0;
        dbAnRow = rowCount++;
        hintRow = rowCount++;
        accountsHeaderRow = rowCount++;
        accountsStartRow = rowCount;
        rowCount += getActiveAccounts();
        accountsEndRow = rowCount;
        accountsDetailsRow = rowCount++;
        disableDoubleBottomRow = rowCount++;

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
                case 0:
                    TextInfoPrivacyCell cell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == hintRow) {
                        cell.setText(LocaleController.getString("DoubleBottomHint", R.string.DoubleBottomHint));
                        cell.setBackground(null);
                        cell.getTextView().setGravity(Gravity.CENTER_HORIZONTAL);
                    } else if (position == accountsDetailsRow) {
                        cell.setText(LocaleController.getString(R.string.PasscodeScreenHint));
                        cell.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                        cell.getTextView().setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
                    }
                    break;
                case 1:
                    RLottieImageHolderView holderView = (RLottieImageHolderView) holder.itemView;
                    holderView.imageView.setAnimation(R.raw.double_bottom, 100, 100);
                    holderView.imageView.getAnimatedDrawable().setAutoRepeat(1);
                    holderView.imageView.playAnimation();
                    break;
                case 2:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == accountsHeaderRow) {
                        headerCell.setText(LocaleController.getString("AllAccounts", R.string.AllAccounts));
                    }
                    break;
                case 3:
                    int accountNum = position - accountsStartRow;
                    TLRPC.User user = accounts.get(accountNum);
                    UserCell userCell = (UserCell) holder.itemView;
                    userCell.setCheckedRight(PasscodeHelper.isDoubleBottomAccount(user.id));
                    userCell.setData(user, null, null, 0, accountNum != accounts.size() - 1);
                    break;
                case 5:
                    TextSettingsCell textCell = (TextSettingsCell) holder.itemView;
                    if (position == disableDoubleBottomRow) {
                        textCell.setText(LocaleController.getString("DisableDoubleBottom", R.string.DisableDoubleBottom), false);
                        textCell.setTag(Theme.key_dialogTextRed);
                        textCell.setTextColor(Theme.getColor(Theme.key_dialogTextRed));
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
                case 1:
                    view = new RLottieImageHolderView(mContext);
                    break;
                case 2:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 3:
                    view = new UserCell(mContext, 16, 1, false, false, false, true);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 5:
                    view = new TextSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                default:
                    view = new TextInfoPrivacyCell(mContext);
                    break;
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == dbAnRow) {
                return 1;
            } else if (position == accountsHeaderRow) {
                return 2;
            } else if (position >= accountsStartRow && position < accountsEndRow) {
                return 3;
            } else if (position == disableDoubleBottomRow) {
                return 5;
            }
            return 0;
        }
    }

    private final static class RLottieImageHolderView extends FrameLayout {
        private final RLottieImageView imageView;

        private RLottieImageHolderView(@NonNull Context context) {
            super(context);
            imageView = new RLottieImageView(context);
            int size = AndroidUtilities.dp(120);
            LayoutParams params = new LayoutParams(size, size);
            params.gravity = Gravity.CENTER_HORIZONTAL;
            addView(imageView, params);

            setPadding(0, AndroidUtilities.dp(32), 0, 0);
            setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }
}
