package it.owlgram.android.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.widget.LinearLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;

@SuppressLint("ViewConstructor")
public class ThemeSelectorDrawerCell extends LinearLayout {
    final private ThemeDrawerCell themeDrawerCell1;
    final private ThemeDrawerCell themeDrawerCell2;
    final private ThemeDrawerCell themeDrawerCell3;
    final private ThemeDrawerCell themeDrawerCell4;

    public ThemeSelectorDrawerCell(Context context, int selectedDefault) {
        super(context);
        themeDrawerCell1 = new ThemeDrawerCell(
                context,
                R.raw.automatic,
                new int[] {
                        R.drawable.menu_groups,
                        R.drawable.menu_contacts,
                        R.drawable.menu_calls,
                        R.drawable.menu_saved,
                        R.drawable.menu_settings,
                }
        ) {
            @Override
            protected void onSelect() {
                super.onSelect();
                onSelectedEvent(0);
                themeDrawerCell2.setChecked(false, themeDrawerCell2.isChecked());
                themeDrawerCell3.setChecked(false, themeDrawerCell3.isChecked());
                themeDrawerCell4.setChecked(false, themeDrawerCell4.isChecked());
            }
        };
        themeDrawerCell2 = new ThemeDrawerCell(
                context,
                R.raw.christmas,
                new int[] {
                        R.drawable.menu_groups_ny,
                        R.drawable.menu_contacts_ny,
                        R.drawable.menu_calls_ny,
                        R.drawable.menu_bookmarks_ny,
                        R.drawable.menu_settings_ny,
                }
        ) {
            @Override
            protected void onSelect() {
                super.onSelect();
                onSelectedEvent(1);
                themeDrawerCell1.setChecked(false, themeDrawerCell1.isChecked());
                themeDrawerCell3.setChecked(false, themeDrawerCell3.isChecked());
                themeDrawerCell4.setChecked(false, themeDrawerCell4.isChecked());
            }
        };
        themeDrawerCell3 = new ThemeDrawerCell(
                context,
                R.raw.halloween,
                new int[] {
                        R.drawable.menu_groups_hw,
                        R.drawable.menu_contacts_hw,
                        R.drawable.menu_calls_hw,
                        R.drawable.menu_bookmarks_hw,
                        R.drawable.menu_settings_hw,
                }
        ) {
            @Override
            protected void onSelect() {
                super.onSelect();
                onSelectedEvent(3);
                themeDrawerCell1.setChecked(false, themeDrawerCell1.isChecked());
                themeDrawerCell2.setChecked(false, themeDrawerCell2.isChecked());
                themeDrawerCell4.setChecked(false, themeDrawerCell4.isChecked());
            }
        };
        themeDrawerCell4 = new ThemeDrawerCell(
                context,
                R.raw.valentine,
                new int[] {
                        R.drawable.menu_groups_14,
                        R.drawable.menu_contacts_14,
                        R.drawable.menu_calls_14,
                        R.drawable.menu_bookmarks_14,
                        R.drawable.menu_settings_14,
                }
        ) {
            @Override
            protected void onSelect() {
                super.onSelect();
                onSelectedEvent(2);
                themeDrawerCell1.setChecked(false, themeDrawerCell1.isChecked());
                themeDrawerCell2.setChecked(false, themeDrawerCell2.isChecked());
                themeDrawerCell3.setChecked(false, themeDrawerCell3.isChecked());
            }
        };
        addView(themeDrawerCell1);
        addView(themeDrawerCell4);
        addView(themeDrawerCell3);
        addView(themeDrawerCell2);
        switch (selectedDefault) {
            case 0:
                themeDrawerCell1.setChecked(true, false);
                break;
            case 1:
                themeDrawerCell2.setChecked(true, false);
                break;
            case 2:
                themeDrawerCell4.setChecked(true, false);
                break;
            case 3:
                themeDrawerCell3.setChecked(true, false);
                break;
        }
    }

    protected void onSelectedEvent(int eventSelected) {}

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawLine(AndroidUtilities.dp(8), getMeasuredHeight() - 1, getMeasuredWidth() - AndroidUtilities.dp(8), getMeasuredHeight() - 1, Theme.dividerPaint);
    }
}
