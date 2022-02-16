package it.owlgram.android.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.exoplayer2.util.Log;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberPicker;

import it.owlgram.android.OwlConfig;
import it.owlgram.android.components.dynamic.IceledButtonCell;
import it.owlgram.android.components.dynamic.LinearButtonCell;
import it.owlgram.android.components.dynamic.PillsButtonCell;
import it.owlgram.android.components.dynamic.RoundedButtonCell;
import it.owlgram.android.components.dynamic.SquaredButtonCell;

public class DynamicButtonSelector extends LinearLayout {
    Paint pickerDividersPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    String[] strings = new String[]{
            LocaleController.getString("ShapeSquared", R.string.ShapeSquared),
            LocaleController.getString("ShapeRounded", R.string.ShapeRounded),
            "Iceled",
            LocaleController.getString("ShapePills", R.string.ShapePills),
            LocaleController.getString("ShapeLinear", R.string.ShapeLinear),
    };
    private final ImageView imageview;

    public DynamicButtonSelector(Context context) {
        super(context);
        pickerDividersPaint.setStyle(Paint.Style.STROKE);
        pickerDividersPaint.setStrokeCap(Paint.Cap.ROUND);
        pickerDividersPaint.setStrokeWidth(AndroidUtilities.dp(2));
        int colorIcon = Theme.getColor(Theme.key_switchTrack);
        int color = AndroidUtilities.getTransparentColor(colorIcon,0.5f);
        imageview = new ImageView(context) {
            @SuppressLint("DrawAllocation")
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                int selectedStyle = OwlConfig.buttonStyleType;
                int w = ((getMeasuredWidth() - AndroidUtilities.dp(30)) >> 2) * 4;
                int maxWidth = ((getMeasuredHeight() - AndroidUtilities.dp(40)) * 4);
                w = Math.min(w, maxWidth);
                int h = w >> 2;
                int padding = Math.round((h * 15f) / 100f);
                h += padding;

                int left = (getMeasuredWidth() >> 1) - (w >> 1);
                int top = (getMeasuredHeight() >> 1) - (h >> 1);
                int right = w + left;
                int bottom = top + h;
                int radius = Math.round(((h - padding) * 14.77F) / 100F);

                RectF rectF = new RectF(left, top, right, bottom);
                Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
                p.setStyle(Paint.Style.STROKE);
                p.setColor(color);
                p.setStrokeWidth(AndroidUtilities.dp(1));
                canvas.drawRoundRect(rectF, radius, radius, p);
                int sizeContainer = w - padding;
                int xContainer = left + (w >> 1) - (sizeContainer >> 1);
                int wItem = sizeContainer >> 2;
                int yContainer = top + (h >> 1) - (wItem >> 1);
                for (int i = 0; i < 4; i++) {
                    int x = xContainer + (wItem * i);
                    int wSubItem = wItem - padding;
                    int xMiddle = x + (wItem >> 1) - (wSubItem >> 1);
                    int yMiddle = yContainer + (wItem >> 1) - (wSubItem >> 1);
                    drawButtonPreview(canvas, xMiddle, yMiddle, wSubItem, selectedStyle, i);
                }
            }
        };
        imageview.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f));
        addView(imageview);
        NumberPicker picker1 = new NumberPicker(context, 13) {
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                float y = AndroidUtilities.dp(31);
                pickerDividersPaint.setColor(Theme.getColor(Theme.key_radioBackgroundChecked));
                canvas.drawLine(AndroidUtilities.dp(2), y, getMeasuredWidth() - AndroidUtilities.dp(2), y, pickerDividersPaint);

                y = getMeasuredHeight() - AndroidUtilities.dp(31);
                canvas.drawLine(AndroidUtilities.dp(2), y, getMeasuredWidth() - AndroidUtilities.dp(2), y, pickerDividersPaint);
            }
        };

        picker1.setWrapSelectorWheel(true);
        picker1.setMinValue(0);
        picker1.setDrawDividers(false);
        picker1.setMaxValue(strings.length - 1);
        picker1.setFormatter(value -> strings[value]);
        picker1.setOnValueChangedListener((picker, oldVal, newVal) -> {
            imageview.invalidate();
            OwlConfig.saveButtonStyle(newVal);
            invalidate();
            picker.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        });
        int selectedButton = OwlConfig.buttonStyleType;
        picker1.setValue(selectedButton);
        addView(picker1, LayoutHelper.createLinear(132, LayoutHelper.MATCH_PARENT, Gravity.RIGHT, 21, 0, 21, 0));
    }

    private void drawButtonPreview(Canvas canvas, int x, int y, int w, int index, int pos) {
        switch (index) {
            case 1:
                RoundedButtonCell.drawButtonPreview(canvas, x, y, w);
                break;
            case 2:
                IceledButtonCell.drawButtonPreview(canvas, x, y, w);
                break;
            case 3:
                PillsButtonCell.drawButtonPreview(canvas, x, y, w);
                break;
            case 4:
                LinearButtonCell.drawButtonPreview(canvas, x, y, w, pos, getContext());
                break;
            default:
                SquaredButtonCell.drawButtonPreview(canvas, x, y, w);
                break;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(102), MeasureSpec.EXACTLY));
    }
}
