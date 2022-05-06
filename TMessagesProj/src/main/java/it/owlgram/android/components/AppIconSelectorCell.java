package it.owlgram.android.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import it.owlgram.android.helpers.IconsHelper;

@SuppressLint("ViewConstructor")
public class AppIconSelectorCell extends FrameLayout {
    private final ListAdapter listAdapter;
    private final RecyclerListView recyclerView;
    private final LinearSmoothScroller scroller;
    private final LinearLayoutManager layoutManager;
    private int rowCount = 0;
    private final Map<Integer, IconsHelper.Icons> map;
    private int prevSelectedPosition = -1;

    public AppIconSelectorCell(Context context, int selectedDefault) {
        super(context);
        map = new HashMap<>();
        listAdapter = new ListAdapter(context);
        recyclerView = new RecyclerListView(getContext());
        recyclerView.setAdapter(listAdapter);
        recyclerView.setClipChildren(false);
        recyclerView.setClipToPadding(false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(null);
        recyclerView.setNestedScrollingEnabled(false);
        scroller = new LinearSmoothScroller(getContext()) {
            @Override
            protected int calculateTimeForScrolling(int dx) {
                return super.calculateTimeForScrolling(dx) * 6;
            }
        };
        recyclerView.setLayoutManager(layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setOnItemClickListener((view, position) -> {
            AppIconCell currItem = (AppIconCell) view;
            if (currItem.isSelected) {
                return;
            }
            listAdapter.setSelectedItem(position);
            onSelectedIcon(currItem.getItemID());
            recyclerView.post(() -> {
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    final int targetPosition = position > prevSelectedPosition
                            ? Math.min(position + 1, rowCount - 1)
                            : Math.max(position - 1, 0);
                    scroller.setTargetPosition(targetPosition);
                    layoutManager.startSmoothScroll(scroller);
                }
                prevSelectedPosition = position;
            });
        });
        recyclerView.setFocusable(false);
        recyclerView.setPadding(AndroidUtilities.dp(12), 0, AndroidUtilities.dp(12), 0);
        addView(recyclerView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.START, 0, 8, 0, 8));
        updateRowsId();
        recyclerView.post(() -> {
            int selectedPosition = 0;
            for (int i = 0; i < rowCount; i++) {
                if (map.get(i) != null && Objects.requireNonNull(map.get(i)).ordinal() == selectedDefault) {
                    selectedPosition = i;
                }
            }
            prevSelectedPosition = selectedPosition;
            listAdapter.setSelectedItem(selectedPosition);
            if (selectedPosition > 0 && selectedPosition < rowCount / 2) {
                selectedPosition -= 1;
            }
            int finalSelectedPosition = Math.min(selectedPosition, rowCount - 1);
            layoutManager.scrollToPositionWithOffset(finalSelectedPosition, 0);
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateRowsId() {
        rowCount = 0;
        for (int i = 0; i < IconsHelper.Icons.values().length; i++) {
            map.put(rowCount++, IconsHelper.Icons.values()[i]);
        }
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private class ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final Context mContext;
        private int selectedItemPosition = -1;
        private int oldSelectedItem = -1;
        private WeakReference<AppIconCell> selectedViewRef;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RecyclerListView.Holder(new AppIconCell(mContext));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            AppIconCell appIconCell = (AppIconCell) holder.itemView;
            IconsHelper.Icons iconDescriptor = map.getOrDefault(position, IconsHelper.Icons.DEFAULT);
            boolean animated = oldSelectedItem != -1;
            if (iconDescriptor != null) {
                appIconCell.setIconDescription(iconDescriptor);
                appIconCell.setSelected(position == selectedItemPosition, animated);
                if (position == selectedItemPosition) {
                    selectedViewRef = new WeakReference<>(appIconCell);
                }
            }
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        public void setSelectedItem(int position) {
            if (selectedItemPosition == position) {
                return;
            }
            if (selectedItemPosition >= 0) {
                notifyItemChanged(selectedItemPosition);
                AppIconCell view = selectedViewRef == null ? null : selectedViewRef.get();
                if (view != null) {
                    view.setSelected(false);
                }
            }
            oldSelectedItem = selectedItemPosition;
            selectedItemPosition = position;
            notifyItemChanged(selectedItemPosition);
        }
    }

    protected void onSelectedIcon(int iconSelected) {}
}
