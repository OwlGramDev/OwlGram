package it.owlgram.android.helpers;


import android.os.Build;
import android.text.Html;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class EntitiesHelper {
    public static String entitiesToHtml(String text, ArrayList<TLRPC.MessageEntity> entities, boolean justTranslate){
        ArrayList<TextHelper> runs = new ArrayList<>();
        ArrayList<TLRPC.MessageEntity> entitiesCopy = new ArrayList<>(entities);
        Collections.sort(entitiesCopy, (o1, o2) -> {
            if (o1.offset > o2.offset) {
                return 1;
            } else if (o1.offset < o2.offset) {
                return -1;
            }
            return 0;
        });
        for (int a = 0, N = entitiesCopy.size(); a < N; a++) {
            TLRPC.MessageEntity entity = entitiesCopy.get(a);
            if (entity.length <= 0 || entity.offset < 0 || entity.offset >= text.length()) {
                continue;
            } else if (entity.offset + entity.length > text.length()) {
                entity.length = text.length() - entity.offset;
            }
            TextHelper newRun = new TextHelper();
            newRun.start = entity.offset;
            newRun.end = newRun.start + entity.length;
            newRun.setFlag(entity);
            for (int b = 0, N2 = runs.size(); b < N2; b++) {
                TextHelper run = runs.get(b);
                if (newRun.start > run.start) {
                    if (newRun.start >= run.end) {
                        continue;
                    }
                    if (newRun.end < run.end) {
                        TextHelper r = new TextHelper(newRun);
                        r.merge(run);
                        b++;
                        N2++;
                        runs.add(b, r);

                        r = new TextHelper(run);
                        r.start = newRun.end;
                        b++;
                        N2++;
                        runs.add(b, r);
                    } else {
                        TextHelper r = new TextHelper(newRun);
                        r.merge(run);
                        r.end = run.end;
                        b++;
                        N2++;
                        runs.add(b, r);
                    }
                    int temp = newRun.start;
                    newRun.start = run.end;
                    run.end = temp;
                } else {
                    if (run.start >= newRun.end) {
                        continue;
                    }
                    int temp = run.start;
                    if (newRun.end == run.end) {
                        run.merge(newRun);
                    } else if (newRun.end < run.end) {
                        TextHelper r = new TextHelper(run);
                        r.merge(newRun);
                        r.end = newRun.end;
                        b++;
                        N2++;
                        runs.add(b, r);
                        run.start = newRun.end;
                    } else {
                        TextHelper r = new TextHelper(newRun);
                        r.start = run.end;
                        b++;
                        N2++;
                        runs.add(b, r);
                        run.merge(newRun);
                    }
                    newRun.end = temp;
                }
            }
            if (newRun.start < newRun.end) {
                runs.add(newRun);
            }
        }
        CharSequence new_text = text;
        int count = runs.size();
        int start_position = 0;
        for (int a = 0; a < count; a++) {
            TextHelper run = runs.get(a);
            int start = start_position + run.start;
            int end = start_position + run.end;
            String[] tag = run.flag.split("-");
            StringBuilder start_tag = new StringBuilder();
            StringBuilder end_tag = new StringBuilder();
            if(justTranslate){
                start_tag.append("<").append(run.flag).append(">");
                end_tag.insert(0, "</" + run.flag + ">");
            }else{
                for (String s: tag) {
                    start_tag.append("<").append(s).append(">");
                    end_tag.insert(0, "</" + s + ">");
                }
            }
            new_text = new_text.subSequence(0, start) + start_tag.toString() + new_text.subSequence(start, new_text.length());
            new_text = new_text.subSequence(0, end + start_tag.length()) + end_tag.toString() + new_text.subSequence(end + start_tag.length(), new_text.length());
            start_position += start_tag.length() + end_tag.length();
        }
        return fixEndString(new_text.toString());
    }
    private static String fixEndString(String string) {
        String[] list_params = new String[]{"b", "i", "u", "s", "p", "c", "q", "a"};
        for (String list_param : list_params) {
            string = string.replace("\n</" + list_param + ">", "</" + list_param + ">\n");
        }
        boolean found_any_replaces = false;
        for (String list_param : list_params) {
            if (string.contains("\n</" + list_param + ">")) {
                found_any_replaces = true;
                break;
            }
        }
        if (found_any_replaces){
            return fixEndString(string);
        }
        return string;
    }

    private static String fixDoubleSpace(String string) {
        String[] list_params = new String[]{"b", "i", "u", "s", "p", "c", "q", "a"};
        for (String list_param : list_params) {
            string = string.replace(" <" + list_param + "> ", " <" + list_param + ">");
            string = string.replace(" </" + list_param + "> ", "</" + list_param + "> ");
        }
        string = string.replace("<a> ", "<a>");
        string = string.replace(" </a>", "</a> ");
        return string;
    }

    public static TextWithMention getEntities(String text, ArrayList<TLRPC.MessageEntity> entities) {
        ArrayList<TLRPC.MessageEntity> returnEntities = new ArrayList<>();
        ArrayList<TLRPC.MessageEntity> copyEntities = new ArrayList<>(entities);
        text = text.replace("</ ", "</");
        text = fixDoubleSpace(text);
        text = text.replace("\n", "§");

        Pattern p = Pattern.compile("<(.*?)>(.*?)</\\1>");
        Matcher m = p.matcher(text);
        String new_text = text;
        while(m.find()){
            String html = m.group(0);
            String html_tag = m.group(1);
            String content = m.group(2);
            if(html != null && content != null && html_tag != null){
                int offset = new_text.indexOf(html);
                int length = content.length();
                new_text = new_text.replace(html, content);
                String[] html_tags = html_tag.split("-");
                for (String tag: html_tags) {
                    TLRPC.MessageEntity entity = null;
                    switch (tag) {
                        case "b":
                            entity = new TLRPC.TL_messageEntityBold();
                            break;
                        case "i":
                            entity = new TLRPC.TL_messageEntityItalic();
                            break;
                        case "u":
                            entity = new TLRPC.TL_messageEntityUnderline();
                            break;
                        case "s":
                            entity = new TLRPC.TL_messageEntityStrike();
                            break;
                        case "c":
                            entity = new TLRPC.TL_messageEntityCode();
                            break;
                        case "p":
                            entity = new TLRPC.TL_messageEntityPre();
                            break;
                        case "q":
                            entity = new TLRPC.TL_messageEntityBlockquote();
                            break;
                        case "a":
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
                                    entity.offset = old_entity.offset;
                                    entity.length = old_entity.length;
                                    copyEntities.remove(i);
                                    break;
                                }
                            }
                            break;
                    }
                    if(entity != null){
                        entity.offset = offset;
                        entity.length = length;
                        returnEntities.add(entity);
                    }
                }
            }
        }
        new_text = new_text.replace("§", "\n");
        TextWithMention textWithMention = new TextWithMention();
        textWithMention.text = new_text;
        textWithMention.entities = returnEntities;
        return textWithMention;
    }

    public static class TextWithMention {
        public String text;
        public ArrayList<TLRPC.MessageEntity> entities;
    }
}
