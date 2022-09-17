package it.owlgram.android.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.widget.LinearLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.ChatMessageCell;
import org.telegram.ui.Components.BackgroundGradientDrawable;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.MotionBackgroundDrawable;

import java.util.ArrayList;

@SuppressLint("ViewConstructor")
public class DetailsPreviewMessagesCell extends LinearLayout {
    private BackgroundGradientDrawable.Disposable backgroundGradientDisposable;
    private BackgroundGradientDrawable.Disposable oldBackgroundGradientDisposable;

    private Drawable backgroundDrawable;
    private Drawable oldBackgroundDrawable;
    private final ArrayList<ChatMessageCell> cells;
    private final ArrayList<MessageObject> messageObjects;
    private final Drawable shadowDrawable;
    private final ActionBarLayout parentLayout;

    public DetailsPreviewMessagesCell(Context context, ActionBarLayout layout) {
        super(context);

        parentLayout = layout;
        cells = new ArrayList<>();
        messageObjects = new ArrayList<>();

        setWillNotDraw(false);
        setOrientation(LinearLayout.VERTICAL);
        setPadding(0, AndroidUtilities.dp(11), 0, AndroidUtilities.dp(11));

        shadowDrawable = Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow);
    }

    public void setMessages(MessageObject messageObject) {
        messageObjects.clear();
        cells.clear();
        if (messageObject.replyMessageObject != null) {
            if (messageObject.replyMessageObject.messageOwner.reply_to != null) {
                messageObject.replyMessageObject.messageOwner.reply_to.reply_to_msg_id = 0;
            }
            messageObject.replyMessageObject.replyMessageObject = null;
            messageObjects.add(messageObject.replyMessageObject);
        }
        messageObjects.add(messageObject);
        removeAllViews();

        for (MessageObject obj : messageObjects) {
            ChatMessageCell cell = new ChatMessageCell(getContext());
            cell.setDelegate(new ChatMessageCell.ChatMessageCellDelegate() {
                @Override
                public boolean canPerformActions() {
                    return true;
                }

                @Override
                public void didLongPress(ChatMessageCell cell, float x, float y) {
                    cell.resetPressedLink(-1);
                }
            });
            cell.isChat = true;
            cell.setFullyDraw(true);
            cell.setMessageObject(obj, null, false, false);
            addView(cell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            cells.add(cell);
        }
    }


    @Override
    public void invalidate() {
        super.invalidate();
        for (int a = 0; a < cells.size(); a++) {
            cells.get(a).setMessageObject(messageObjects.get(a), null, false, false);
            cells.get(a).invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable newDrawable = Theme.getCachedWallpaperNonBlocking();
        if (newDrawable != backgroundDrawable && newDrawable != null) {
            if (Theme.isAnimatingColor()) {
                oldBackgroundDrawable = backgroundDrawable;
                oldBackgroundGradientDisposable = backgroundGradientDisposable;
            } else if (backgroundGradientDisposable != null) {
                backgroundGradientDisposable.dispose();
                backgroundGradientDisposable = null;
            }
            backgroundDrawable = newDrawable;
        }
        float themeAnimationValue = parentLayout.getThemeAnimationValue();
        for (int a = 0; a < 2; a++) {
            Drawable drawable = a == 0 ? oldBackgroundDrawable : backgroundDrawable;
            if (drawable == null) {
                continue;
            }
            if (a == 1 && oldBackgroundDrawable != null) {
                drawable.setAlpha((int) (255 * themeAnimationValue));
            } else {
                drawable.setAlpha(255);
            }
            if (drawable instanceof ColorDrawable || drawable instanceof GradientDrawable || drawable instanceof MotionBackgroundDrawable) {
                drawable.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
                if (drawable instanceof BackgroundGradientDrawable) {
                    final BackgroundGradientDrawable backgroundGradientDrawable = (BackgroundGradientDrawable) drawable;
                    backgroundGradientDisposable = backgroundGradientDrawable.drawExactBoundsSize(canvas, this);
                } else {
                    drawable.draw(canvas);
                }
            } else if (drawable instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                if (bitmapDrawable.getTileModeX() == Shader.TileMode.REPEAT) {
                    canvas.save();
                    float scale = 2.0f / AndroidUtilities.density;
                    canvas.scale(scale, scale);
                    drawable.setBounds(0, 0, (int) Math.ceil(getMeasuredWidth() / scale), (int) Math.ceil(getMeasuredHeight() / scale));
                } else {
                    int viewHeight = getMeasuredHeight();
                    float scaleX = (float) getMeasuredWidth() / (float) drawable.getIntrinsicWidth();
                    float scaleY = (float) (viewHeight) / (float) drawable.getIntrinsicHeight();
                    float scale = Math.max(scaleX, scaleY);
                    int width = (int) Math.ceil(drawable.getIntrinsicWidth() * scale);
                    int height = (int) Math.ceil(drawable.getIntrinsicHeight() * scale);
                    int x = (getMeasuredWidth() - width) / 2;
                    int y = (viewHeight - height) / 2;
                    canvas.save();
                    canvas.clipRect(0, 0, width, getMeasuredHeight());
                    drawable.setBounds(x, y, x + width, y + height);
                }
                drawable.draw(canvas);
                canvas.restore();
            }
            if (a == 0 && oldBackgroundDrawable != null && themeAnimationValue >= 1.0f) {
                if (oldBackgroundGradientDisposable != null) {
                    oldBackgroundGradientDisposable.dispose();
                    oldBackgroundGradientDisposable = null;
                }
                oldBackgroundDrawable = null;
                invalidate();
            }
        }
        shadowDrawable.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
        shadowDrawable.draw(canvas);
        for (ChatMessageCell cell : cells) {
            ImageReceiver imageReceiver = cell.getAvatarImage();
            if (imageReceiver != null) {
                int top = cell.getTop();
                float tx = cell.getTranslationX();
                int y = cell.getTop() + cell.getLayoutHeight();
                if (y - AndroidUtilities.dp(48) < top) {
                    y = top + AndroidUtilities.dp(48);
                }
                if (tx != 0) {
                    canvas.save();
                    canvas.translate(tx, 0);
                }
                imageReceiver.setImageY(y - AndroidUtilities.dp(44));
                imageReceiver.draw(canvas);
                if (tx != 0) {
                    canvas.restore();
                }
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (backgroundGradientDisposable != null) {
            backgroundGradientDisposable.dispose();
            backgroundGradientDisposable = null;
        }
        if (oldBackgroundGradientDisposable != null) {
            oldBackgroundGradientDisposable.dispose();
            oldBackgroundGradientDisposable = null;
        }
    }

    @Override
    protected void dispatchSetPressed(boolean pressed) {

    }
}
