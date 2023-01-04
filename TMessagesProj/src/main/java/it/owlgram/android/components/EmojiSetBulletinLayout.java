package it.owlgram.android.components;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.Components.Bulletin;

import it.owlgram.android.helpers.CustomEmojiHelper;

@SuppressLint("ViewConstructor")
public class EmojiSetBulletinLayout extends Bulletin.TwoLineLayout {

    public EmojiSetBulletinLayout(@NonNull Context context, CustomEmojiHelper.EmojiPackBase data) {
        super(context, null);
        titleTextView.setText(LocaleController.getString("EmojiSetRemoved", R.string.EmojiSetRemoved));
        subtitleTextView.setText(LocaleController.formatString("EmojiSetRemovedInfo", R.string.EmojiSetRemovedInfo, data.getPackName()));
        imageView.setImage(data.getPreview(), null, null);
    }
}
