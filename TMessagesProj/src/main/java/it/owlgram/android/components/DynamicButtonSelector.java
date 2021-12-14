package it.owlgram.android.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.cardview.widget.CardView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberPicker;

import it.owlgram.android.OwlConfig;
import it.owlgram.android.components.dynamic.IceledButtonCell;
import it.owlgram.android.components.dynamic.PillsButtonCell;
import it.owlgram.android.components.dynamic.SquaredButtonCell;
import it.owlgram.android.components.dynamic.RoundedButtonCell;
import it.owlgram.android.components.dynamic.LinearButtonCell;

public class DynamicButtonSelector extends LinearLayout {
    Paint pickerDividersPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    String[] strings = new String[]{
            LocaleController.getString("OwlgramButtonSquared", R.string.OwlgramButtonSquared),
            LocaleController.getString("OwlgramButtonRounded", R.string.OwlgramButtonRounded),
            LocaleController.getString("OwlgramButtonIceled", R.string.OwlgramButtonIceled),
            LocaleController.getString("OwlgramButtonPills", R.string.OwlgramButtonPills),
            LocaleController.getString("OwlgramButtonLinear", R.string.OwlgramButtonLinear),
    };

    private final LinearLayout mainLayout;

    public DynamicButtonSelector(Context context) {
        super(context);

        pickerDividersPaint.setStyle(Paint.Style.STROKE);
        pickerDividersPaint.setStrokeCap(Paint.Cap.ROUND);
        pickerDividersPaint.setStrokeWidth(AndroidUtilities.dp(2));

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f));
        linearLayout.setGravity(Gravity.CENTER_VERTICAL);

        int color = Theme.getColor(Theme.key_switchTrack);

        CardView cardView = new CardView(context);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, AndroidUtilities.dp(60));
        layoutParams2.setMargins(AndroidUtilities.dp(20),0,AndroidUtilities.dp(10),0);
        cardView.setLayoutParams(layoutParams2);
        cardView.setCardBackgroundColor(Color.TRANSPARENT);
        cardView.setCardElevation(0);
        cardView.setRadius(AndroidUtilities.dp(11));

        RelativeLayout relativeLayout = new RelativeLayout(context);
        relativeLayout.setBackgroundColor(AndroidUtilities.getTransparentColor(color, 0.5f));
        relativeLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        CardView cardView2 = new CardView(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(AndroidUtilities.dp(1),AndroidUtilities.dp(1),AndroidUtilities.dp(1),AndroidUtilities.dp(1));
        cardView2.setCardBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        cardView2.setLayoutParams(layoutParams);
        cardView2.setCardElevation(0);
        cardView2.setRadius(AndroidUtilities.dp(10));

        mainLayout = new LinearLayout(context);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mainLayout.setPadding(AndroidUtilities.dp(10), AndroidUtilities.dp(5),AndroidUtilities.dp(10),AndroidUtilities.dp(5));

        relativeLayout.addView(cardView2);
        relativeLayout.addView(mainLayout);
        cardView.addView(relativeLayout);
        linearLayout.addView(cardView);
        addView(linearLayout);

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
            loadButtons(newVal);
            OwlConfig.saveButtonStyle(newVal);
            invalidate();
            picker.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        });
        int selectedButton = OwlConfig.buttonStyleType;
        picker1.setValue(selectedButton);
        loadButtons(selectedButton);
        addView(picker1, LayoutHelper.createFrame(132, LayoutHelper.MATCH_PARENT, Gravity.RIGHT, 21, 0, 21, 0));
    }

    private void loadButtons(int index) {
        mainLayout.removeAllViews();
        for (int i = 0; i < 4; i++ ) {
            mainLayout.addView(getButtonPreview(index, i));
        }
    }

    private LinearLayout getButtonPreview(int index, int pos) {
        switch (index) {
            case 1:
                return RoundedButtonCell.getButtonPreview(getContext());
            case 2:
                return IceledButtonCell.getButtonPreview(getContext());
            case 3:
                return PillsButtonCell.getButtonPreview(getContext());
            case 4:
                return LinearButtonCell.getButtonPreview(getContext(), pos);
            default:
                return SquaredButtonCell.getButtonPreview(getContext());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(102), MeasureSpec.EXACTLY));
    }
}
