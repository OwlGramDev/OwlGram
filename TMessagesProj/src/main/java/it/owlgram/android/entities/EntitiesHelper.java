package it.owlgram.android.entities;


import android.graphics.Typeface;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.LocaleSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.view.View;

import com.google.android.exoplayer2.util.Log;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.EditTextCaption;
import org.telegram.ui.Components.TextStyleSpan;
import org.telegram.ui.Components.URLSpanMono;
import org.telegram.ui.Components.URLSpanNoUnderline;
import org.telegram.ui.Components.URLSpanReplacement;
import org.telegram.ui.Components.URLSpanUserMention;

import java.util.ArrayList;
import java.util.Locale;

import it.owlgram.android.entities.syntax_highlight.Prism4jGrammarLocator;
import it.owlgram.android.entities.syntax_highlight.SyntaxHighlight;
import it.owlgram.android.entities.syntax_highlight.prism4j.Prism4j;

public class EntitiesHelper {

    public static Spanned getSpannableString(String text, ArrayList<TLRPC.MessageEntity> entities, int offset) {
        for (int a = 0; a < entities.size(); a++) {
            TLRPC.MessageEntity entity = entities.get(a);
            entity.offset = entity.offset + offset;
        }
        return getSpannableString(text, entities, false);
    }

    public static Spanned getSpannableString(String text, ArrayList<TLRPC.MessageEntity> entities) {
        return getSpannableString(text, entities, false);
    }

    public static Spanned getSpannableString(String text, ArrayList<TLRPC.MessageEntity> entities, boolean includeLinks) {
        Editable messSpan = new SpannableStringBuilder(text);
        MediaDataController.addTextStyleRuns(entities, text, messSpan);
        applySpansToSpannable(-1, -1, messSpan, 0, text.length(), includeLinks);
        return messSpan;
    }

    public static Spanned getSpannableFromSpanEntities(CharSequence sequence) {
        Editable messSpan = new SpannableStringBuilder(sequence);
        applySpansToSpannable(-1, -1, messSpan, 0, sequence.length(), false);
        return messSpan;
    }

    public static void applySpansToEditable(int rS, int rE, EditTextCaption editTextCaption, int startSpan, int endSpan) {
        applySpansToSpannable(rS, rE, editTextCaption.getText(), startSpan, endSpan, false);
    }

    public static void applySpansToSpannable(int rS, int rE, Editable spannableString, int startSpan, int endSpan, boolean includeLinks) {
        if (endSpan - startSpan <= 0) {
            return;
        }
        if (rS >= 0 && rE >= 0) {
            CharacterStyle[] mSpansDelete = spannableString.getSpans(rS, rE, CharacterStyle.class);
            for (CharacterStyle mSpan : mSpansDelete) {
                spannableString.removeSpan(mSpan);
            }
        }
        CharacterStyle[] mSpans = spannableString.getSpans(startSpan, endSpan, CharacterStyle.class);
        for (CharacterStyle mSpan : mSpans) {
            int start = spannableString.getSpanStart(mSpan);
            int end = spannableString.getSpanEnd(mSpan);
            if (mSpan instanceof URLSpanMono) {
                TextStyleSpan.TextStyleRun run = new TextStyleSpan.TextStyleRun();
                run.flags |= TextStyleSpan.FLAG_STYLE_MONO;
                mSpan = new TextStyleSpan(run);
            }
            if (mSpan instanceof URLSpanUserMention) {
                TextStyleSpan.TextStyleRun run = new TextStyleSpan.TextStyleRun();
                run.flags |= TextStyleSpan.FLAG_STYLE_MENTION;
                TLRPC.TL_messageEntityMentionName entityMention = new TLRPC.TL_messageEntityMentionName();
                entityMention.user_id = Long.parseLong(((URLSpanUserMention) mSpan).getURL());
                run.urlEntity = entityMention;
                mSpan = new TextStyleSpan(run);
            }
            if (mSpan instanceof URLSpanReplacement) {
                TextStyleSpan.TextStyleRun run = new TextStyleSpan.TextStyleRun();
                run.flags = ((URLSpanReplacement) mSpan).getTextStyleRun().flags;
                run.urlEntity = ((URLSpanReplacement) mSpan).getTextStyleRun().urlEntity;
                mSpan = new TextStyleSpan(run);
            }
            if (mSpan instanceof TextStyleSpan) {
                boolean isBold = (((TextStyleSpan) mSpan).getStyleFlags() & TextStyleSpan.FLAG_STYLE_BOLD) > 0;
                boolean isItalic = (((TextStyleSpan) mSpan).getStyleFlags() & TextStyleSpan.FLAG_STYLE_ITALIC) > 0;
                if (isBold && !isItalic) {
                    spannableString.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if (!isBold && isItalic) {
                    spannableString.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if (isBold && isItalic) {
                    spannableString.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if ((((TextStyleSpan) mSpan).getStyleFlags() & TextStyleSpan.FLAG_STYLE_MONO) > 0) {
                    TextStyleSpan.TextStyleRun runner = ((TextStyleSpan) mSpan).getTextStyleRun();
                    if (runner.urlEntity != null && !TextUtils.isEmpty(runner.urlEntity.language)) {
                        spannableString.setSpan(new LocaleSpan(Locale.forLanguageTag(runner.urlEntity.language + "-og")), runner.urlEntity.offset, runner.urlEntity.offset + runner.urlEntity.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        SyntaxHighlight.highlight(runner.urlEntity.language, runner.urlEntity.offset, runner.urlEntity.offset + runner.urlEntity.length, spannableString);
                    }
                    spannableString.setSpan(new TypefaceSpan("monospace"), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if ((((TextStyleSpan) mSpan).getStyleFlags() & TextStyleSpan.FLAG_STYLE_UNDERLINE) > 0) {
                    spannableString.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if ((((TextStyleSpan) mSpan).getStyleFlags() & TextStyleSpan.FLAG_STYLE_STRIKE) > 0) {
                    spannableString.setSpan(new StrikethroughSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if (((((TextStyleSpan) mSpan).getStyleFlags() & TextStyleSpan.FLAG_STYLE_SPOILER) > 0)) {
                    spannableString.setSpan(new ForegroundColorSpan(Theme.getColor(Theme.key_chat_messagePanelText)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if ((((TextStyleSpan) mSpan).getStyleFlags() & TextStyleSpan.FLAG_STYLE_URL) > 0) {
                    String url = ((TextStyleSpan) mSpan).getTextStyleRun().urlEntity.url;
                    if (url != null || includeLinks) {
                        spannableString.setSpan(new URLSpan(url), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
                if ((((TextStyleSpan) mSpan).getStyleFlags() & TextStyleSpan.FLAG_STYLE_MENTION) > 0) {
                    long id = ((TLRPC.TL_messageEntityMentionName)((TextStyleSpan) mSpan).getTextStyleRun().urlEntity).user_id;
                    spannableString.setSpan(new URLSpan("tg://user?id=" + id), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } else if (mSpan instanceof URLSpan) {
                spannableString.removeSpan(mSpan);
            }
        }
    }

    static class RawSpannableInfo {
        public final TextStyleSpan textStyleSpan;
        public final int start;
        public final int end;
        public RawSpannableInfo(TextStyleSpan textStyleSpan, int start, int end) {
            this.textStyleSpan = textStyleSpan;
            this.start = start;
            this.end = end;
        }
    }

    public static void applySpannableToEditText(EditTextCaption editTextCaption, int startSpan, int endSpan) {
        Editable spannableString = new Editable.Factory().newEditable(editTextCaption.getText());
        CharacterStyle[] mSpans = spannableString.getSpans(startSpan, endSpan, CharacterStyle.class);
        RawSpannableInfo rawSpannableInfo = null;
        for (CharacterStyle mSpan : mSpans) {
            int start = spannableString.getSpanStart(mSpan);
            int end = spannableString.getSpanEnd(mSpan);
            CharacterStyle result = null;
            if (mSpan instanceof URLSpan) {
                URLSpan urlSpan = (URLSpan) mSpan;
                if (urlSpan.getURL() != null) {
                    if (urlSpan.getURL().startsWith("tg://user?id=")) {
                        String id = urlSpan.getURL().replace("tg://user?id=", "");
                        result = new URLSpanUserMention(id,1);
                    } else {
                        result = new URLSpanReplacement(urlSpan.getURL());
                    }
                }
            } else if (mSpan instanceof StyleSpan) {
                StyleSpan styleSpan = (StyleSpan) mSpan;
                switch (styleSpan.getStyle()) {
                    case Typeface.BOLD:
                        TextStyleSpan.TextStyleRun run = new TextStyleSpan.TextStyleRun();
                        run.flags |= TextStyleSpan.FLAG_STYLE_BOLD;
                        result = new TextStyleSpan(run);
                        break;
                    case Typeface.ITALIC:
                        TextStyleSpan.TextStyleRun run2 = new TextStyleSpan.TextStyleRun();
                        run2.flags |= TextStyleSpan.FLAG_STYLE_ITALIC;
                        result = new TextStyleSpan(run2);
                        break;
                    case Typeface.BOLD_ITALIC:
                        TextStyleSpan.TextStyleRun run3 = new TextStyleSpan.TextStyleRun();
                        run3.flags |= TextStyleSpan.FLAG_STYLE_ITALIC;
                        run3.flags |= TextStyleSpan.FLAG_STYLE_BOLD;
                        result = new TextStyleSpan(run3);
                        break;
                }
            } else if (mSpan instanceof TypefaceSpan) {
                TextStyleSpan.TextStyleRun run = new TextStyleSpan.TextStyleRun();
                run.flags |= TextStyleSpan.FLAG_STYLE_MONO;
                LocaleSpan[] localeSpans = spannableString.getSpans(start, end, LocaleSpan.class);
                if (localeSpans != null && localeSpans.length > 0 && localeSpans[0].getLocale() != null && "og".equalsIgnoreCase(localeSpans[0].getLocale().getCountry())) {
                    TLRPC.TL_messageEntityPre entity = new TLRPC.TL_messageEntityPre();
                    entity.offset = start;
                    entity.length = end - entity.offset;
                    entity.language = localeSpans[0].getLocale().getLanguage();
                    run.urlEntity = entity;
                }
                result = new TextStyleSpan(run);
            } else if (mSpan instanceof UnderlineSpan) {
                TextStyleSpan.TextStyleRun run = new TextStyleSpan.TextStyleRun();
                run.flags |= TextStyleSpan.FLAG_STYLE_UNDERLINE;
                result = new TextStyleSpan(run);
            } else if (mSpan instanceof StrikethroughSpan) {
                TextStyleSpan.TextStyleRun run = new TextStyleSpan.TextStyleRun();
                run.flags |= TextStyleSpan.FLAG_STYLE_STRIKE;
                result = new TextStyleSpan(run);
            } else if ((mSpan instanceof ForegroundColorSpan && ((ForegroundColorSpan)mSpan).getForegroundColor() == Theme.getColor(Theme.key_chat_messagePanelText)) || mSpan instanceof BackgroundColorSpan) {
                if (mSpan instanceof BackgroundColorSpan) {
                    ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Theme.getColor(Theme.key_chat_messagePanelText));
                    editTextCaption.getText().removeSpan(mSpan);
                    editTextCaption.getText().setSpan(foregroundColorSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                TextStyleSpan.TextStyleRun run = new TextStyleSpan.TextStyleRun();
                run.flags |= TextStyleSpan.FLAG_STYLE_SPOILER;
                result = new TextStyleSpan(run);
            }
            if (result != null) {
                if (result instanceof TextStyleSpan) {
                    if (rawSpannableInfo != null) {
                        if (rawSpannableInfo.start == start && rawSpannableInfo.end == end) {
                            rawSpannableInfo.textStyleSpan.getTextStyleRun().merge(((TextStyleSpan) result).getTextStyleRun());
                        } else {
                            editTextCaption.getText().setSpan(rawSpannableInfo.textStyleSpan, rawSpannableInfo.start, rawSpannableInfo.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            rawSpannableInfo = new RawSpannableInfo((TextStyleSpan) result, start, end);
                        }
                    } else {
                        rawSpannableInfo = new RawSpannableInfo((TextStyleSpan) result, start, end);
                    }
                } else {
                    if (rawSpannableInfo != null) {
                        editTextCaption.getText().setSpan(rawSpannableInfo.textStyleSpan, rawSpannableInfo.start, rawSpannableInfo.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        rawSpannableInfo = null;
                    }
                    editTextCaption.getText().setSpan(result, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        if (rawSpannableInfo != null) {
            editTextCaption.getText().setSpan(rawSpannableInfo.textStyleSpan, rawSpannableInfo.start, rawSpannableInfo.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    public static CharSequence getUrlNoUnderlineText(CharSequence charSequence) {
        Spannable spannable = new SpannableString(charSequence);
        URLSpan[] spans = spannable.getSpans(0, charSequence.length(), URLSpan.class);
        for (URLSpan urlSpan : spans) {
            URLSpan span = urlSpan;
            int start = spannable.getSpanStart(span);
            int end = spannable.getSpanEnd(span);
            spannable.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL()) {
                @Override
                public void onClick(View widget) {
                    super.onClick(widget);
                }
            };
            spannable.setSpan(span, start, end, 0);
        }
        return spannable;
    }

    public static boolean containsMarkdown(CharSequence text) {
        text = AndroidUtilities.getTrimmedString(text);
        CharSequence[] message = new CharSequence[]{AndroidUtilities.getTrimmedString(text)};
        return MediaDataController.getInstance(UserConfig.selectedAccount).getEntities(message, true, false).size() > 0;
    }

    public static String entityToString(TLRPC.MessageEntity entity) {
        return "{ offset: "
            + entity.offset
            + ", length: "
            + entity.length
            + ", language: "
            + entity.language
            + " }";
    }

    public static CharSequence applySyntaxHighlight(CharSequence text, ArrayList<TLRPC.MessageEntity> entities) {
        SpannableStringBuilder messSpan = new SpannableStringBuilder(text);
        MediaDataController.addTextStyleRuns(entities, text, messSpan);
        Prism4j grammarCheck = new Prism4j(new Prism4jGrammarLocator());
        TextStyleSpan[] result = messSpan.getSpans(0, messSpan.length(), TextStyleSpan.class);
        for (TextStyleSpan span:result) {
            if (span.isMono()) {
                CharSequence language = messSpan.subSequence(messSpan.getSpanStart(span), messSpan.getSpanEnd(span)).toString().split("\n")[0];
                if (grammarCheck.grammar(language.toString()).isPresent()) {
                    int start = messSpan.getSpanStart(span);
                    int end = messSpan.getSpanEnd(span);
                    int endCode = end - AndroidUtilities.getTrimmedString(messSpan.subSequence(start + language.length(), end)).length() - (end == messSpan.length() ? 0 : 1);
                    messSpan = messSpan.delete(start, endCode);
                    TLRPC.TL_messageEntityPre entity = new TLRPC.TL_messageEntityPre();
                    entity.offset = start;
                    entity.length = end - entity.offset;
                    entity.language = language.toString();
                    span.getTextStyleRun().urlEntity = entity;
                    span.getTextStyleRun().end -= endCode;
                }
            }
        }
        CharSequence[] message = new CharSequence[]{AndroidUtilities.getTrimmedString(messSpan)};
        ArrayList<TLRPC.MessageEntity> entitiesNew = MediaDataController.getInstance(UserConfig.selectedAccount).getEntities(message, true);
        entities.clear();
        entities.addAll(entitiesNew);
        return messSpan.toString();
    }

    public static boolean isEmoji(String message){
        return message.matches("(?:[\uD83D\uDE00-\uD83D\uDE4F]|" +
                "[\u2600-\u26FF]\uFE0F?|[\u2700-\u27BF]\uFE0F?|\u24C2\uFE0F?|" +
                "[\uD83C\uDDE6-\uD83C\uDDFF]{1,2}|" +
                "[\uD83C\uDD70\uD83C\uDD71\uD83C\uDD7E\uD83C\uDD7F\uD83C\uDD8E\uD83C\uDD91-\uD83C\uDD9A]\uFE0F?|" +
                "[\u0023\u002A\u0030-\u0039]\uFE0F?\u20E3|[\u2194-\u2199\u21A9-\u21AA]\uFE0F?|[\u2B05-\u2B07\u2B1B\u2B1C\u2B50\u2B55]\uFE0F?|" +
                "[\u2934\u2935]\uFE0F?|[\u3030\u303D]\uFE0F?|[\u3297\u3299]\uFE0F?|" +
                "[\uD83C\uDE01\uD83C\uDE02\uD83C\uDE1A\uD83C\uDE2F\uD83C\uDE32-\uD83C\uDE3A\uD83C\uDE50\uD83C\uDE51]\uFE0F?|" +
                "[\u203C\u2049]\uFE0F?|[\u25AA\u25AB\u25B6\u25C0\u25FB-\u25FE]\uFE0F?|" +
                "[\u00A9\u00AE]\uFE0F?|[\u2122\u2139]\uFE0F?|\uD83C\uDC04\uFE0F?|" +
                "[\u231A\u231B\u2328\u23CF\u23E9-\u23F3\u23F8-\u23FA]\uFE0F?)+");
    }
}
