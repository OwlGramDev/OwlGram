package it.owlgram.android.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextRadioCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import java.util.Locale;

import it.owlgram.android.OwlConfig;
import it.owlgram.android.translator.BaseTranslator;
import it.owlgram.android.translator.Translator;

public class SelectLanguageSettings extends BaseFragment {

    private int rowCount;
    private int languageHeaderRow;
    private int languagesStartRow;
    private ArrayList<CharSequence> names;
    private ArrayList<String> targetLanguages;
    private ListAdapter listAdapter;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        BaseTranslator translator = Translator.getCurrentTranslator();
        targetLanguages = new ArrayList<>(translator.getTargetLanguages());
        names = new ArrayList<>();
        for (String language : targetLanguages) {
            names.add(getLanguage(language));
        }
        AndroidUtilities.selectionSort(names, targetLanguages);
        fixMostImportantLanguages(translator.getCurrentAppLanguage());
        updateRowsId();
        return true;
    }

    private void fixMostImportantLanguages(String appLanguage) {
        int indexLangEn = targetLanguages.indexOf("en");
        names.add(0, names.get(indexLangEn));
        targetLanguages.add(0, targetLanguages.get(indexLangEn));
        names.remove(indexLangEn + 1);
        targetLanguages.remove(indexLangEn + 1);
        targetLanguages.add(0, "app");
        names.add(0, LocaleController.getString("Default", R.string.Default) + " - " + getLanguage(appLanguage));
        int indexLangApp = targetLanguages.indexOf(appLanguage);
        names.remove(indexLangApp);
        targetLanguages.remove(indexLangApp);
        if (!OwlConfig.translationTarget.equals("app")) {
            int indexLangSelected = targetLanguages.indexOf(OwlConfig.translationTarget);
            names.add(0, names.get(indexLangSelected));
            targetLanguages.add(0, targetLanguages.get(indexLangSelected));
            names.remove(indexLangSelected + 1);
            targetLanguages.remove(indexLangSelected + 1);
        }
    }

    private static String getLanguage(String language) {
        Locale locale = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? Locale.forLanguageTag(language) : new Locale(language);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !TextUtils.isEmpty(locale.getScript())) {
            return HtmlCompat.fromHtml(String.format("%s - %s", AndroidUtilities.capitalize(locale.getDisplayScript()), AndroidUtilities.capitalize(locale.getDisplayScript(locale))), HtmlCompat.FROM_HTML_MODE_LEGACY).toString();
        } else {
            return String.format("%s - %s", AndroidUtilities.capitalize(locale.getDisplayName()), AndroidUtilities.capitalize(locale.getDisplayName(locale)));
        }
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString("TranslationLanguage", R.string.TranslationLanguage));
        actionBar.setAllowOverlayTitle(false);
        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        listAdapter = new ListAdapter(context);
        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        RecyclerListView listView = new RecyclerListView(context);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setVerticalScrollBarEnabled(false);
        listView.setAdapter(listAdapter);
        if (listView.getItemAnimator() != null) {
            ((DefaultItemAnimator) listView.getItemAnimator()).setDelayAnimations(false);
        }
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setOnItemClickListener((view, position, x, y) -> {
            String selectedLanguage = targetLanguages.get(position - languagesStartRow);
            OwlConfig.setTranslationTarget(selectedLanguage);
            finishFragment();
        });
        return fragmentView;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateRowsId() {
        rowCount = 0;
        languageHeaderRow = rowCount++;
        languagesStartRow = rowCount;
        rowCount += targetLanguages.size();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {
        private final Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 1:
                    TextRadioCell textRadioCell = (TextRadioCell) holder.itemView;
                    String[] languages = names.get(position - languagesStartRow).toString().split(" - ");
                    boolean isSelectedLanguage = OwlConfig.translationTarget.equals(targetLanguages.get(position - languagesStartRow));
                    textRadioCell.setTextAndValueAndCheck(languages[1], languages[0], isSelectedLanguage, false, true);
                    break;
                case 2:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == languageHeaderRow) {
                        headerCell.setText(LocaleController.getString("Language", R.string.Language));
                    }
                    break;
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            return type == 1;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            if (viewType == 2) {
                view = new HeaderCell(mContext);
            } else {
                view = new TextRadioCell(mContext);
            }
            view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == languageHeaderRow) {
                return 2;
            }
            return 1;
        }
    }
}