package it.owlgram.android.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;

import java.util.Objects;

import it.owlgram.android.components.AddItemCell;
import it.owlgram.android.components.HintHeaderCell;
import it.owlgram.android.components.SwapOrderCell;
import it.owlgram.android.helpers.MenuOrderManager;

public class DrawerOrderSettings extends BaseSettingsActivity {
    private ItemTouchHelper itemTouchHelper;

    private int headerHintRow;
    private int headerSuggestedOptionsRow;
    private int headerMenuRow;
    private int menuHintsStartRow;
    private int menuHintsEndRow;
    private int hintsDividerRow;
    private int menuItemsStartRow;
    private int menuItemsEndRow;
    private int menuItemsDividerRow;

    // TYPES
    private static final int TYPE_HINT_HEADER = 200;
    private static final int TYPE_MENU_ITEM = 201;
    private static final int TYPE_SUGGESTED_OPTIONS = 202;


    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onMenuItemClick(int id) {
        super.onMenuItemClick(id);
        if (id == 1) {
            MenuOrderManager.addItem(MenuOrderManager.DIVIDER_ITEM);
            updateRowsId();
            listAdapter.notifyItemInserted(menuItemsStartRow);
            if (MenuOrderManager.IsDefaultPosition()) {
                menuItem.hideSubItem(2);
            } else {
                menuItem.showSubItem(2);
            }
            getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
        } else if (id == 2) {
            MenuOrderManager.resetToDefaultPosition();
            updateRowsId();
            listAdapter.notifyDataSetChanged();
            menuItem.hideSubItem(2);
            getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
        }
    }

    @Override
    protected ActionBarMenuItem createMenuItem() {
        ActionBarMenu menu = actionBar.createMenu();
        ActionBarMenuItem menuItem = menu.addItem(0, R.drawable.ic_ab_other);
        menuItem.setContentDescription(LocaleController.getString("AccDescrMoreOptions", R.string.AccDescrMoreOptions));
        menuItem.addSubItem(1, R.drawable.msg_newfilter, LocaleController.getString("AddDivider", R.string.AddDivider));
        menuItem.addSubItem(2, R.drawable.msg_reset, LocaleController.getString("ResetItemsOrder", R.string.ResetItemsOrder));
        if (MenuOrderManager.IsDefaultPosition()) {
            menuItem.hideSubItem(2);
        } else {
            menuItem.showSubItem(2);
        }
        return menuItem;
    }

    @Override
    public View createView(Context context) {
        View res = super.createView(context);
        itemTouchHelper = new ItemTouchHelper(new TouchHelperCallback());
        itemTouchHelper.attachToRecyclerView(listView);
        return res;
    }

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString("MenuItems", R.string.MenuItems);
    }

    protected void updateRowsId() {
        super.updateRowsId();
        headerSuggestedOptionsRow = -1;
        hintsDividerRow = -1;

        int size_hints = MenuOrderManager.sizeHints();
        headerHintRow = rowCount++;

        if (size_hints > 0) {
            headerSuggestedOptionsRow = rowCount++;
            menuHintsStartRow = rowCount;
            rowCount += size_hints;
            menuHintsEndRow = rowCount;
            hintsDividerRow = rowCount++;
        }

        headerMenuRow = rowCount++;
        menuItemsStartRow = rowCount;
        rowCount += MenuOrderManager.sizeAvailable();
        menuItemsEndRow = rowCount;
        menuItemsDividerRow = rowCount++;
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
                case TYPE_HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == headerSuggestedOptionsRow) {
                        headerCell.setText(LocaleController.getString("RecommendedItems", R.string.RecommendedItems));
                    } else if (position == headerMenuRow) {
                        headerCell.setText(LocaleController.getString("MenuItems", R.string.MenuItems));
                    }
                    break;
                case TYPE_MENU_ITEM:
                    SwapOrderCell swapOrderCell = (SwapOrderCell) holder.itemView;
                    MenuOrderManager.EditableMenuItem data = MenuOrderManager.getSingleAvailableMenuItem(position - menuItemsStartRow);
                    if (data != null) {
                        swapOrderCell.setData(data.text, data.isDefault, data.isPremium, data.id, true);
                    }
                    break;
                case TYPE_SUGGESTED_OPTIONS:
                    AddItemCell addItemCell = (AddItemCell) holder.itemView;
                    MenuOrderManager.EditableMenuItem notData = MenuOrderManager.getSingleNotAvailableMenuItem(position - menuHintsStartRow);
                    if (notData != null) {
                        addItemCell.setData(notData.text, notData.id, notData.isPremium, true);
                    }
                    break;
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return holder.getItemViewType() == TYPE_MENU_ITEM;
        }

        @SuppressLint({"ClickableViewAccessibility", "NotifyDataSetChanged"})
        @Override
        protected View onCreateViewHolder(int viewType) {
            View view = null;
            switch (viewType) {
                case TYPE_HINT_HEADER:
                    view = new HintHeaderCell(context, R.raw.filters, LocaleController.formatString("MenuItemsOrderDesc", R.string.MenuItemsOrderDesc));
                    view.setBackground(Theme.getThemedDrawable(context, R.drawable.greydivider_top, Theme.key_windowBackgroundGrayShadow));
                    break;
                case TYPE_MENU_ITEM:
                    SwapOrderCell swapOrderCell = new SwapOrderCell(context);
                    swapOrderCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    swapOrderCell.setOnReorderButtonTouchListener((v, event) -> {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            itemTouchHelper.startDrag(listView.getChildViewHolder(swapOrderCell));
                        }
                        return false;
                    });
                    swapOrderCell.setOnDeleteClick(v -> {
                        int index = listView.getChildViewHolder(swapOrderCell).getAdapterPosition();
                        int position = index - menuItemsStartRow;
                        if (MenuOrderManager.isAvailable(swapOrderCell.menuId, position)) {
                            int prevRecommendedHeaderRow = 0, index2 = 0;
                            MenuOrderManager.removeItem(position);
                            if (!Objects.equals(swapOrderCell.menuId, MenuOrderManager.DIVIDER_ITEM)) {
                                index2 = MenuOrderManager.getPositionOf(swapOrderCell.menuId);
                                index2 += menuHintsStartRow;
                                prevRecommendedHeaderRow = headerSuggestedOptionsRow;
                            }
                            updateRowsId();
                            if (!Objects.equals(swapOrderCell.menuId, MenuOrderManager.DIVIDER_ITEM)) {
                                if (prevRecommendedHeaderRow == -1 && headerSuggestedOptionsRow != -1) {
                                    int itemsCount = hintsDividerRow - headerSuggestedOptionsRow + 1;
                                    index += itemsCount;
                                    listAdapter.notifyItemRangeInserted(headerSuggestedOptionsRow, itemsCount);
                                } else {
                                    index += 1;
                                    listAdapter.notifyItemInserted(index2);
                                }
                            }
                            listAdapter.notifyItemRemoved(index);
                            if (MenuOrderManager.IsDefaultPosition()) {
                                menuItem.hideSubItem(2);
                            } else {
                                menuItem.showSubItem(2);
                            }
                            getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
                        }
                    });
                    view = swapOrderCell;
                    break;
                case TYPE_SUGGESTED_OPTIONS:
                    AddItemCell addItemCell = new AddItemCell(context);
                    addItemCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    addItemCell.setAddOnClickListener(v -> {
                        if (!MenuOrderManager.isAvailable(addItemCell.menuId)) {
                            int index = MenuOrderManager.getPositionOf(addItemCell.menuId);
                            if (index != -1) {
                                MenuOrderManager.addItem(addItemCell.menuId);
                                int prevRecommendedHintHeaderRow = headerSuggestedOptionsRow;
                                int prevRecommendedHintSectionRow = hintsDividerRow;
                                index += menuHintsStartRow;
                                updateRowsId();
                                if (prevRecommendedHintHeaderRow != -1 && headerSuggestedOptionsRow == -1) {
                                    listAdapter.notifyItemRangeRemoved(prevRecommendedHintHeaderRow, prevRecommendedHintSectionRow - prevRecommendedHintHeaderRow + 1);
                                } else {
                                    listAdapter.notifyItemRemoved(index);
                                }
                                listAdapter.notifyItemInserted(menuItemsStartRow);
                            } else {
                                updateRowsId();
                                listAdapter.notifyDataSetChanged();
                            }
                            if (MenuOrderManager.IsDefaultPosition()) {
                                menuItem.hideSubItem(2);
                            } else {
                                menuItem.showSubItem(2);
                            }
                            getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
                        }
                    });
                    view = addItemCell;
                    break;
            }
            return view;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == hintsDividerRow || position == menuItemsDividerRow) {
                return TYPE_SHADOW;
            } else if (position == headerHintRow) {
                return TYPE_HINT_HEADER;
            } else if (position == headerSuggestedOptionsRow || position == headerMenuRow) {
                return TYPE_HEADER;
            } else if (position >= menuItemsStartRow && position < menuItemsEndRow) {
                return TYPE_MENU_ITEM;
            } else if (position >= menuHintsStartRow && position < menuHintsEndRow) {
                return TYPE_SUGGESTED_OPTIONS;
            }
            throw new IllegalArgumentException("Invalid position");
        }

        public void swapElements(int fromIndex, int toIndex) {
            int idx1 = fromIndex - menuItemsStartRow;
            int idx2 = toIndex - menuItemsStartRow;
            int count = menuItemsEndRow - menuItemsStartRow;
            if (idx1 < 0 || idx2 < 0 || idx1 >= count || idx2 >= count) {
                return;
            }
            MenuOrderManager.changePosition(idx1, idx2);
            notifyItemMoved(fromIndex, toIndex);
            if (MenuOrderManager.IsDefaultPosition()) {
                menuItem.hideSubItem(2);
            } else {
                menuItem.showSubItem(2);
            }
            getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
        }
    }

    public class TouchHelperCallback extends ItemTouchHelper.Callback {

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            if (viewHolder.getItemViewType() != TYPE_MENU_ITEM) {
                return makeMovementFlags(0, 0);
            }
            return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
            if (source.getItemViewType() != target.getItemViewType()) {
                return false;
            }
            ((ListAdapter) listAdapter).swapElements(source.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                listView.cancelClickRunnables(false);
                viewHolder.itemView.setPressed(true);
            }
            super.onSelectedChanged(viewHolder, actionState);
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        }

        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            viewHolder.itemView.setPressed(false);
        }
    }
}
