package it.owlgram.android.entities.syntax_highlight;

import android.text.Spannable;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import it.owlgram.android.entities.syntax_highlight.prism4j.Prism4j;

public interface Prism4jTheme {

    @ColorInt
    int textColor();

    void apply(
            @NonNull String language,
            @NonNull Prism4j.Syntax syntax,
            @NonNull Spannable spannable,
            int start,
            int end
    );
}
