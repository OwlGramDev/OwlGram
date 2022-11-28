package it.owlgram.android.translator;

import android.content.Context;
import android.content.SharedPreferences;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import it.owlgram.android.OwlConfig;

public class AutoTranslateConfig {
    private static final SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("OwlDialogConfig", Context.MODE_PRIVATE);

    public static boolean isAutoTranslateEnabled(long dialog_id, int topicId) {
        if (hasAutoTranslateConfig(dialog_id, topicId)) {
            return preferences.getBoolean(getExceptionsKey(dialog_id, topicId), OwlConfig.autoTranslate);
        } else {
            return preferences.getBoolean(getExceptionsKey(dialog_id, 0), OwlConfig.autoTranslate);
        }
    }

    public static boolean hasAutoTranslateConfig(long dialog_id, int topicId) {
        return preferences.contains(getExceptionsKey(dialog_id, topicId));
    }

    public static void setEnabled(long dialog_id, int topicId, boolean enable) {
        preferences.edit().putBoolean(getExceptionsKey(dialog_id, topicId), enable).apply();
        if (isAllTopicEnabledOrDisabled(dialog_id, enable) || topicId == 0) {
            preferences.edit().putBoolean(getExceptionsKey(dialog_id, 0), enable).apply();
            deleteAllTopicExceptions(dialog_id);
        }
    }

    public static void removeGroupException(long dialog_id) {
        preferences.edit().remove(getExceptionsKey(dialog_id, 0)).apply();
        deleteAllTopicExceptions(dialog_id);
    }

    private static void deleteAllTopicExceptions(long dialog_id) {
        getAllExceptions().stream()
                .filter(e -> e.dialog_id == dialog_id)
                .filter(e -> e.topicId != 0)
                .forEach(e -> preferences.edit().remove(getExceptionsKey(e.dialog_id, e.topicId)).apply());
    }

    public static void removeAllTypeExceptions(boolean isAllowed) {
        getAllExceptions().stream()
                .filter(e -> e.isAllow == isAllowed)
                .forEach(e -> preferences.edit().remove(getExceptionsKey(e.dialog_id, e.topicId)).apply());
    }

    private static boolean isAllTopicEnabledOrDisabled(long dialog_id, boolean enabled) {
        List<TLRPC.TL_forumTopic> topics = MessagesController.getInstance(UserConfig.selectedAccount).getTopicsController().getTopics(-dialog_id);
        if (topics != null) {
            return topics.stream().allMatch(t -> isAutoTranslateEnabled(dialog_id, t.id) == enabled);
        } else {
            return true;
        }
    }

    public static void setDefault(long dialog_id, int topicId) {
        preferences.edit().remove(getExceptionsKey(dialog_id, topicId)).apply();
    }

    private static String getExceptionsKey(long dialog_id, int topicId) {
        return "exceptions_" + UserConfig.selectedAccount + "_" + dialog_id + (topicId != 0 ? "_" + topicId : "");
    }

    public static void migrate() {
        preferences.getAll().entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("autoTranslate_"))
                .forEach(entry -> {
                    String key = entry.getKey();
                    String[] parts = key.split("_");
                    long dialog_id = Long.parseLong(parts[1]);
                    int topicId = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
                    boolean value = (boolean) entry.getValue();
                    setEnabled(dialog_id, topicId, value);
                    preferences.edit().remove(key).apply();
                });
    }

    public static class AutoTranslateException {
        public final long dialog_id;
        public final int topicId;
        public final boolean isAllow;
        public TLObject chat;

        public AutoTranslateException(long dialog_id, int topicId, boolean isAllow) {
            this.dialog_id = dialog_id;
            this.topicId = topicId;
            this.isAllow = isAllow;
            if (dialog_id > 0) {
                this.chat = MessagesController.getInstance(UserConfig.selectedAccount).getUser(dialog_id);
            } else {
                this.chat = MessagesController.getInstance(UserConfig.selectedAccount).getChat(-dialog_id);
            }
        }
    }

    public static List<AutoTranslateException> getAllExceptions() {
        return preferences.getAll().entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("exceptions_" + UserConfig.selectedAccount))
                .map(entry -> new AutoTranslateException(
                        Long.parseLong(entry.getKey().split("_")[2]),
                        entry.getKey().split("_").length > 3 ? Integer.parseInt(entry.getKey().split("_")[3]) : 0,
                        (boolean) entry.getValue()))
                .filter(exception -> exception.chat != null)
                .sorted((o1, o2) -> {
                    String n1 = o1.chat instanceof TLRPC.User ? ((TLRPC.User) o1.chat).first_name : ((TLRPC.Chat) o1.chat).title;
                    String n2 = o2.chat instanceof TLRPC.User ? ((TLRPC.User) o2.chat).first_name : ((TLRPC.Chat) o2.chat).title;
                    return n1.compareTo(n2);
                })
                .collect(Collectors.toList());
    }

    public static List<AutoTranslateException> getExceptions(boolean isAllow) {
        return getAllExceptions().stream()
                .filter(exception -> exception.isAllow == isAllow)
                .filter(distinctByKey(exception -> exception.dialog_id))
                .filter(exception -> isAllTopicEnabledOrDisabled(exception.dialog_id, isAllow) || isAllow)
                .collect(Collectors.toList());
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        final Set<Object> seen = new HashSet<>();
        return t -> seen.add(keyExtractor.apply(t));
    }

    public static boolean resetExceptions() {
        return preferences.edit().clear().commit();
    }
}