package it.owlgram.android.helpers;


import android.graphics.Typeface;
import android.os.Build;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.text.util.Linkify;
import android.view.View;

import org.apache.commons.text.StringEscapeUtils;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MediaDataController;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.EditTextCaption;
import org.telegram.ui.Components.TextStyleSpan;
import org.telegram.ui.Components.URLSpanMono;
import org.telegram.ui.Components.URLSpanNoUnderline;
import org.telegram.ui.Components.URLSpanReplacement;
import org.telegram.ui.Components.URLSpanUserMention;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntitiesHelper {
    final private static String[] list_params = new String[]{"b", "i", "u", "s", "tt", "a", "q"};
    public static String entitiesToHtml(String text, ArrayList<TLRPC.MessageEntity> entities, boolean includeLink) {
        text = text.replace("\n", "\u2029");
        if (!includeLink) {
            text = text.replace("<", "\u2027");
        }
        SpannableStringBuilder messSpan = SpannableStringBuilder.valueOf(text);
        MediaDataController.addTextStyleRuns(entities, text, messSpan);
        CharacterStyle[] mSpans = messSpan.getSpans(0, messSpan.length(), CharacterStyle.class);
        for (CharacterStyle mSpan : mSpans) {
            int start = messSpan.getSpanStart(mSpan);
            int end = messSpan.getSpanEnd(mSpan);
            boolean isBold = (((TextStyleSpan) mSpan).getStyleFlags() & TextStyleSpan.FLAG_STYLE_BOLD) > 0;
            boolean isItalic = (((TextStyleSpan) mSpan).getStyleFlags() & TextStyleSpan.FLAG_STYLE_ITALIC) > 0;
            if (isBold && !isItalic || isBold && !includeLink) {
                messSpan.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (!isBold && isItalic || isItalic && !includeLink) {
                messSpan.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (isBold && isItalic && includeLink) {
                messSpan.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if ((((TextStyleSpan) mSpan).getStyleFlags() & TextStyleSpan.FLAG_STYLE_MONO) > 0) {
                messSpan.setSpan(new TypefaceSpan("monospace"), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if ((((TextStyleSpan) mSpan).getStyleFlags() & TextStyleSpan.FLAG_STYLE_UNDERLINE) > 0) {
                messSpan.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if ((((TextStyleSpan) mSpan).getStyleFlags() & TextStyleSpan.FLAG_STYLE_STRIKE) > 0) {
                messSpan.setSpan(new StrikethroughSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if ((((TextStyleSpan) mSpan).getStyleFlags() & TextStyleSpan.FLAG_STYLE_SPOILER) > 0) {
                messSpan.setSpan(new ForegroundColorSpan(Theme.getColor(Theme.key_chat_messagePanelText)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if ((((TextStyleSpan) mSpan).getStyleFlags() & TextStyleSpan.FLAG_STYLE_URL) > 0) {
                String url = ((TextStyleSpan) mSpan).getTextStyleRun().urlEntity.url;
                if (url != null || !includeLink) {
                    messSpan.setSpan(new URLSpan(url), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            if ((((TextStyleSpan) mSpan).getStyleFlags() & TextStyleSpan.FLAG_STYLE_MENTION) > 0) {
                long id = ((TLRPC.TL_messageEntityMentionName)((TextStyleSpan) mSpan).getTextStyleRun().urlEntity).user_id;
                messSpan.setSpan(new URLSpan("tg://user?id=" + id), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        String html_result = Html.toHtml(messSpan);
        html_result = html_result.replace("<p dir=\"ltr\">", "");
        html_result = html_result.replace("</p>", "");
        html_result = html_result.replaceAll("<span style=\"text-decoration:line-through;\">(.*?)</span>", "<s>$1</s>");
        if (!includeLink) {
            html_result = html_result.replaceAll("<a href=\".*?\">", "<a>");
            html_result = html_result.replaceAll("<span style=\"color:.*?;\">(.*?)</span>", "<q>$1</q>");
            html_result = StringEscapeUtils.unescapeHtml4(html_result);
        } else {
            html_result = html_result.replace("&#8233;", "\u2029");
        }
        html_result = html_result.replace("\n", "");
        html_result = html_result.replace("\u2029", "\n");
        return html_result;
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
                result = new TextStyleSpan(run);
            } else if (mSpan instanceof UnderlineSpan) {
                TextStyleSpan.TextStyleRun run = new TextStyleSpan.TextStyleRun();
                run.flags |= TextStyleSpan.FLAG_STYLE_UNDERLINE;
                result = new TextStyleSpan(run);
            } else if (mSpan instanceof StrikethroughSpan) {
                TextStyleSpan.TextStyleRun run = new TextStyleSpan.TextStyleRun();
                run.flags |= TextStyleSpan.FLAG_STYLE_STRIKE;
                result = new TextStyleSpan(run);
            } else if (mSpan instanceof ForegroundColorSpan) {
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

    private static String fixDoubleSpace(String string) {
        for (String list_param : list_params) {
            string = string.replace(" <" + list_param + "> ", " <" + list_param + ">");
            string = string.replace(" </" + list_param + "> ", "</" + list_param + "> ");
        }
        string = string.replace("<a> ", "<a>");
        string = string.replace(" </a>", "</a> ");
        return string;
    }

    private static String fixDoubleHtmlElement(String string) {
        for (String list_param : list_params) {
            for (String list_param2 : list_params) {
                string = string.replace("<" + list_param + "-" + list_param2 + ">", "<" + list_param + "><" + list_param2 + ">");
                string = string.replace("</" + list_param + "-" + list_param2 + ">", "</" + list_param2 + "></" + list_param + ">");
            }
        }
        return string;
    }

    private static String fixHtmlCorrupted(String string) {
        Pattern p = Pattern.compile("<(.*?)>");
        Matcher m = p.matcher(string);
        ArrayList<String> listUnclosedTags = new ArrayList<>();
        ArrayList<String> listUnopenedTags = new ArrayList<>();
        while(m.find()){
            String tag = m.group(0);
            String originalTag = tag;
            if (tag != null) {
                tag = tag.replace("<", "").replace(">", "").replace(" ", "");
                if (!tag.contains("/")) {
                    listUnclosedTags.add(0, tag);
                    listUnopenedTags.add(0, originalTag);
                } else {
                    tag = tag.replace("/", "");
                    if (listUnclosedTags.contains(tag)) {
                        listUnclosedTags.remove(0);
                        listUnopenedTags.remove(0);
                    } else if (listUnclosedTags.size() > 0) {
                        boolean isValidData = new ArrayList<>(Arrays.asList(list_params)).contains(tag);
                        String tagToReplace = isValidData ? listUnopenedTags.get(0):originalTag;
                        int start = string.indexOf(tagToReplace);
                        int end = start + tagToReplace.length();
                        String htmlClosingTag = "<" + (isValidData ? tag:"/" + listUnclosedTags.get(0)) + ">";
                        string = replaceByPosition(string, start, end, htmlClosingTag);
                        listUnclosedTags.remove(0);
                        listUnopenedTags.remove(0);
                    }
                }
            }
        }
        return string;
    }

    private static String replaceByPosition(String string, int start, int end, String replace) {
        String firstString = string.substring(0, start);
        String lastString = string.substring(end);
        return firstString + replace + lastString;
    }

    private static String fixStrangeSpace(String string) {
        for (String list_param : list_params) {
            string = string.replace("< " + list_param + ">", "<" + list_param + ">");
            string = string.replace("<" + list_param + " >", "<" + list_param + ">");
            string = string.replace("</ " + list_param + ">", "</" + list_param + ">");
            string = string.replace("</" + list_param + " >", "</" + list_param + ">");
            string = string.replace("< /" + list_param + ">", "</" + list_param + ">");
            string = string.replace("< / " + list_param + ">", "</" + list_param + ">");
        }
        return string;
    }

    public static TextWithMention getEntities(String text, ArrayList<TLRPC.MessageEntity> entities, boolean internalLinks) {
        ArrayList<TLRPC.MessageEntity> returnEntities = new ArrayList<>();
        ArrayList<TLRPC.MessageEntity> copyEntities = null;
        if (entities != null) {
            copyEntities = new ArrayList<>(entities);
        }
        text = fixDoubleSpace(text);
        text = fixDoubleHtmlElement(text);
        text = fixStrangeSpace(text);
        text = fixHtmlCorrupted(text);
        text = text.replaceAll("[\n\r]$", "");
        text = text.replace("\n", "<br/>");
        text = text.replaceAll("\n", "<br/>");
        text = text.replace("<a>", "<a href=\"https://telegram.org/\">");
        text = text.replaceAll("<q>(.*?)</q>", "<span style=\"color:#000000;\">$1</span>");
        text = text.replace("\u2027", "&lt;");
        text = text.replace("\u0327", "<");
        SpannableString htmlParsed;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            htmlParsed = new SpannableString(Html.fromHtml(text,Html.FROM_HTML_MODE_LEGACY));
        }else{
            htmlParsed =  new SpannableString(Html.fromHtml(text));
        }
        if (internalLinks) {
            AndroidUtilities.addLinks(htmlParsed, Linkify.ALL);
        }
        CharacterStyle[] mSpans = htmlParsed.getSpans(0, htmlParsed.length(), CharacterStyle.class);
        for (CharacterStyle mSpan : mSpans) {
            int start = htmlParsed.getSpanStart(mSpan);
            int end = htmlParsed.getSpanEnd(mSpan);
            TLRPC.MessageEntity entity = null;
            if (mSpan instanceof URLSpan) {
                URLSpan urlSpan = (URLSpan) mSpan;
                if (copyEntities != null) {
                    for (int i = 0; i < copyEntities.size(); i++) {
                        TLRPC.MessageEntity old_entity = copyEntities.get(i);
                        boolean found = false;
                        if (old_entity instanceof TLRPC.TL_messageEntityMentionName) {
                            found = true;
                            entity = new TLRPC.TL_messageEntityMentionName();
                            ((TLRPC.TL_messageEntityMentionName) entity).user_id = ((TLRPC.TL_messageEntityMentionName) old_entity).user_id;
                        } else if (old_entity instanceof TLRPC.TL_inputMessageEntityMentionName) {
                            found = true;
                            entity = new TLRPC.TL_inputMessageEntityMentionName();
                            ((TLRPC.TL_inputMessageEntityMentionName) entity).user_id = ((TLRPC.TL_inputMessageEntityMentionName) old_entity).user_id;
                        } else if (old_entity instanceof TLRPC.TL_messageEntityTextUrl) {
                            found = true;
                            entity = new TLRPC.TL_messageEntityTextUrl();
                            entity.url = old_entity.url;
                        } else if (old_entity instanceof TLRPC.TL_messageEntityUrl) {
                            found = true;
                            entity = new TLRPC.TL_messageEntityUrl();
                        } else if (old_entity instanceof TLRPC.TL_messageEntityMention) {
                            found = true;
                            entity = new TLRPC.TL_messageEntityMention();
                        } else if (old_entity instanceof TLRPC.TL_messageEntityBotCommand) {
                            found = true;
                            entity = new TLRPC.TL_messageEntityBotCommand();
                        } else if (old_entity instanceof TLRPC.TL_messageEntityHashtag) {
                            found = true;
                            entity = new TLRPC.TL_messageEntityHashtag();
                        } else if (old_entity instanceof TLRPC.TL_messageEntityCashtag) {
                            found = true;
                            entity = new TLRPC.TL_messageEntityCashtag();
                        } else if (old_entity instanceof TLRPC.TL_messageEntityEmail) {
                            found = true;
                            entity = new TLRPC.TL_messageEntityEmail();
                        } else if (old_entity instanceof TLRPC.TL_messageEntityBankCard) {
                            found = true;
                            entity = new TLRPC.TL_messageEntityBankCard();
                        } else if (old_entity instanceof TLRPC.TL_messageEntityPhone) {
                            found = true;
                            entity = new TLRPC.TL_messageEntityPhone();
                        }
                        if(found){
                            copyEntities.remove(i);
                            break;
                        }
                    }
                } else {
                    entity = new TLRPC.TL_messageEntityTextUrl();
                    entity.url = urlSpan.getURL();
                }
            } else if (mSpan instanceof StyleSpan) {
                StyleSpan styleSpan = (StyleSpan) mSpan;
                switch (styleSpan.getStyle()) {
                    case 1:
                        entity = new TLRPC.TL_messageEntityBold();
                        break;
                    case 2:
                        entity = new TLRPC.TL_messageEntityItalic();
                        break;
                }
            } else if (mSpan instanceof TypefaceSpan) {
                entity = new TLRPC.TL_messageEntityCode();
            } else if (mSpan instanceof UnderlineSpan) {
                entity = new TLRPC.TL_messageEntityUnderline();
            } else if (mSpan instanceof StrikethroughSpan) {
                entity = new TLRPC.TL_messageEntityStrike();
            } else if (mSpan instanceof ForegroundColorSpan) {
                entity = new TLRPC.TL_messageEntitySpoiler();
            }
            if(entity != null){
                entity.offset = start;
                entity.length = end - start;
                returnEntities.add(entity);
            }
        }
        TextWithMention textWithMention = new TextWithMention();
        textWithMention.text = htmlParsed.toString();
        textWithMention.entities = returnEntities;
        return textWithMention;
    }

    public static class TextWithMention {
        public String text;
        public ArrayList<TLRPC.MessageEntity> entities;
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
}
