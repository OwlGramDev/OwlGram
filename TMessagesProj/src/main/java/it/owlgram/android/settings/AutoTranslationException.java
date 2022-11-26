package it.owlgram.android.settings;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ManageChatTextCell;
import org.telegram.ui.GroupCreateActivity;

import it.owlgram.android.translator.AutoTranslateConfig;

public class AutoTranslationException extends BaseSettingsActivity {

    private final boolean isAllow;

    private int addUsersOrGroupsRow;
    private int spaceRow;
    private int headerExceptionsRow;

    public AutoTranslationException(boolean isAllow) {
        this.isAllow = isAllow;
    }

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        super.onItemClick(view, position, x, y);
        if (position == addUsersOrGroupsRow) {
            Bundle args = new Bundle();
            args.putBoolean(isAllow ? "isAlwaysShare" : "isNeverShare", true);
            args.putInt("chatAddType", 1);
            GroupCreateActivity fragment = new GroupCreateActivity(args);
            fragment.setDelegate(ids -> {
                for (int i = 0; i < ids.size(); i++) {
                    AutoTranslateConfig.setAutoTranslateEnable(ids.get(i), 0, isAllow);
                }
            });
            presentFragment(fragment);
        }
    }

    @Override
    protected void updateRowsId() {
        super.updateRowsId();

        addUsersOrGroupsRow = rowCount++;
        spaceRow = rowCount++;
        headerExceptionsRow = rowCount++;
    }

    @Override
    protected String getActionBarTitle() {
        return isAllow ? LocaleController.getString("AlwaysAllow", R.string.AlwaysAllow) : LocaleController.getString("NeverAllow", R.string.NeverAllow);
    }

    @Override
    protected BaseListAdapter createAdapter() {
        return new ListAdapter();
    }

    private class ListAdapter extends BaseListAdapter {

        @Override
        protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, boolean partial) {
            switch (holder.getItemViewType()) {
                case TYPE_ADD_EXCEPTION:
                    ManageChatTextCell actionCell = (ManageChatTextCell) holder.itemView;
                    if (position == addUsersOrGroupsRow) {
                        actionCell.setColors(Theme.key_windowBackgroundWhiteBlueIcon, Theme.key_windowBackgroundWhiteBlueButton);
                        actionCell.setText(LocaleController.getString("PrivacyAddAnException", R.string.PrivacyAddAnException), null, R.drawable.msg_contact_add, false);
                    }
                    break;
                case TYPE_HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == headerExceptionsRow) {
                        headerCell.setText(LocaleController.getString("PrivacyExceptions", R.string.PrivacyExceptions));
                    }
                    break;
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            return type == TYPE_ADD_EXCEPTION;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == addUsersOrGroupsRow) {
                return TYPE_ADD_EXCEPTION;
            } else if (position == spaceRow) {
                return TYPE_TEXT_HINT;
            } else if (position == headerExceptionsRow) {
                return TYPE_HEADER;
            }
            throw new IllegalArgumentException("Invalid position");
        }
    }
}
