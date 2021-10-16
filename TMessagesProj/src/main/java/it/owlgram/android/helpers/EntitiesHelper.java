package it.owlgram.android.helpers;


import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.Collections;

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
        if(string.contains("\n</")){
            return fixEndString(string);
        }
        return string;
    }
}
