package it.owlgram.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;

import java.util.Objects;

import it.owlgram.android.MenuOrderController;
import it.owlgram.ui.Cells.AddItem;
import it.owlgram.ui.Cells.HintHeader;
import it.owlgram.ui.Cells.SwapOrder;

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


    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onMenuItemClick(int id) {
        super.onMenuItemClick(id);
        if (id == 1) {
            MenuOrderController.addItem(MenuOrderController.DIVIDER_ITEM);
            updateRowsId();
            listAdapter.notifyItemInserted(menuItemsStartRow);
            if (MenuOrderController.IsDefaultPosition()) {
                menuItem.hideSubItem(2);
            } else {
                menuItem.showSubItem(2);
            }
            reloadMainInfo();
        } else if (id == 2) {
            MenuOrderController.resetToDefaultPosition();
            updateRowsId();
            listAdapter.notifyDataSetChanged();
            menuItem.hideSubItem(2);
            reloadMainInfo();
        }
    }

    @Override
    protected ActionBarMenuItem createMenuItem() {
        ActionBarMenu menu = actionBar.createMenu();
        ActionBarMenuItem menuItem = menu.addItem(0, R.drawable.ic_ab_other);
        menuItem.setContentDescription(LocaleController.getString("AccDescrMoreOptions", R.string.AccDescrMoreOptions));
        menuItem.addSubItem(1, R.drawable.msg_newfilter, LocaleController.getString("AddDivider", R.string.AddDivider));
        menuItem.addSubItem(2, R.drawable.msg_reset, LocaleController.getString("ResetItemsOrder", R.string.ResetItemsOrder));
        if (MenuOrderController.IsDefaultPosition()) {
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

        int size_hints = MenuOrderController.sizeHints();
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
        rowCount += MenuOrderController.sizeAvailable();
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
            switch (ViewType.fromInt(holder.getItemViewType())) {
                case SHADOW:
                    holder.itemView.setBackground(Theme.getThemedDrawable(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == headerSuggestedOptionsRow) {
                        headerCell.setText(LocaleController.getString("RecommendedItems", R.string.RecommendedItems));
                    } else if (position == headerMenuRow) {
                        headerCell.setText(LocaleController.getString("MenuItems", R.string.MenuItems));
                    }
                    break;
                case MENU_ITEM:
                    SwapOrder swapOrderCell = (SwapOrder) holder.itemView;
                    MenuOrderController.EditableMenuItem data = MenuOrderController.getSingleAvailableMenuItem(position - menuItemsStartRow);
                    if (data != null) {
                        swapOrderCell.setData(data.text, data.isDefault, data.isPremium, data.id, true);
                    }
                    break;
                case SUGGESTED_OPTIONS:
                    AddItem addItemCell = (AddItem) holder.itemView;
                    MenuOrderController.EditableMenuItem notData = MenuOrderController.getSingleNotAvailableMenuItem(position - menuHintsStartRow);
                    if (notData != null) {
                        addItemCell.setData(notData.text, notData.id, notData.isPremium, true);
                    }
                    break;
            }
        }

        @Override
        protected boolean isEnabled(ViewType viewType, int position) {
            return viewType == ViewType.MENU_ITEM;
        }

        @SuppressLint({"ClickableViewAccessibility", "NotifyDataSetChanged"})
        @Override
        protected View onCreateViewHolder(ViewType viewType) {
            View view = null;
            switch (viewType) {
                case HINT_HEADER:
                    view = new HintHeader(context, R.raw.filters, LocaleController.formatString("MenuItemsOrderDesc", R.string.MenuItemsOrderDesc));
                    view.setBackground(Theme.getThemedDrawable(context, R.drawable.greydivider_top, Theme.key_windowBackgroundGrayShadow));
                    break;
                case MENU_ITEM:
                    SwapOrder swapOrderCell = new SwapOrder(context);
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
                        if (MenuOrderController.isAvailable(swapOrderCell.menuId, position)) {
                            int prevRecommendedHeaderRow = 0, index2 = 0;
                            MenuOrderController.removeItem(position);
                            if (!Objects.equals(swapOrderCell.menuId, MenuOrderController.DIVIDER_ITEM)) {
                                index2 = MenuOrderController.getPositionOf(swapOrderCell.menuId);
                                index2 += menuHintsStartRow;
                                prevRecommendedHeaderRow = headerSuggestedOptionsRow;
                            }
                            updateRowsId();
                            if (!Objects.equals(swapOrderCell.menuId, MenuOrderController.DIVIDER_ITEM)) {
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
                            if (MenuOrderController.IsDefaultPosition()) {
                                menuItem.hideSubItem(2);
                            } else {
                                menuItem.showSubItem(2);
                            }
                            reloadMainInfo();
                        }
                    });
                    view = swapOrderCell;
                    break;
                case SUGGESTED_OPTIONS:
                    AddItem addItemCell = new AddItem(context);
                    addItemCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    addItemCell.setAddOnClickListener(v -> {
                        if (!MenuOrderController.isAvailable(addItemCell.menuId)) {
                            int index = MenuOrderController.getPositionOf(addItemCell.menuId);
                            if (index != -1) {
                                MenuOrderController.addItem(addItemCell.menuId);
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
                            if (MenuOrderController.IsDefaultPosition()) {
                                menuItem.hideSubItem(2);
                            } else {
                                menuItem.showSubItem(2);
                            }
                            reloadMainInfo();
                        }
                    });
                    view = addItemCell;
                    break;
            }
            return view;
        }

        @Override
        public ViewType getViewType(int position) {
            if (position == hintsDividerRow || position == menuItemsDividerRow) {
                return ViewType.SHADOW;
            } else if (position == headerHintRow) {
                return ViewType.HINT_HEADER;
            } else if (position == headerSuggestedOptionsRow || position == headerMenuRow) {
                return ViewType.HEADER;
            } else if (position >= menuItemsStartRow && position < menuItemsEndRow) {
                return ViewType.MENU_ITEM;
            } else if (position >= menuHintsStartRow && position < menuHintsEndRow) {
                return ViewType.SUGGESTED_OPTIONS;
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
            MenuOrderController.changePosition(idx1, idx2);
            notifyItemMoved(fromIndex, toIndex);
            if (MenuOrderController.IsDefaultPosition()) {
                menuItem.hideSubItem(2);
            } else {
                menuItem.showSubItem(2);
            }
            reloadMainInfo();
        }
    }

    public class TouchHelperCallback extends ItemTouchHelper.Callback {

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            if (viewHolder.getItemViewType() != ViewType.MENU_ITEM.toInt()) {
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
