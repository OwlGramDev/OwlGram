package it.owlgram.android.components;

import android.content.Context;
import android.widget.LinearLayout;

import org.telegram.messenger.ImageReceiver;

public class UpdateCell extends LinearLayout {
    public UpdateCell(Context context) {
        super(context);
        ImageReceiver imageReceiver = new ImageReceiver();
        imageReceiver.setImage("", null, null, null, 0);

    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), heightMeasureSpec);
    }
}
