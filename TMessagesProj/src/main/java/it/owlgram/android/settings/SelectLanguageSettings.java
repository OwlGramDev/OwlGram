package it.owlgram.android.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextRadioCell;
import org.telegram.ui.Components.EmptyTextProgressView;
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
    private EmptyTextProgressView emptyView;
    private boolean searchWas;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        fixMostImportantLanguages(loadLanguages().getCurrentAppLanguage());
        updateRowsId();
        return true;
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
            if (position != languageHeaderRow) {
                String selectedLanguage = targetLanguages.get(position - languagesStartRow);
                OwlConfig.setTranslationTarget(selectedLanguage);
                finishFragment();
            }
        });

        ActionBarMenu menu = actionBar.createMenu();
        ActionBarMenuItem item = menu.addItem(0, R.drawable.ic_ab_search).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener() {

            @Override
            public void onSearchCollapse() {
                searchWas = false;
                fixMostImportantLanguages(loadLanguages().getCurrentAppLanguage());
                emptyView.setVisibility(View.GONE);
                updateRowsId();
            }

            @Override
            public void onTextChanged(EditText editText) {
                String text = editText.getText().toString();
                if (text.length() != 0) {
                    searchWas = true;
                    getLanguagesFiltered(text);
                    emptyView.setVisibility(targetLanguages.size() == 0 ? View.VISIBLE : View.GONE);
                } else {
                    searchWas = false;
                    fixMostImportantLanguages(loadLanguages().getCurrentAppLanguage());
                    emptyView.setVisibility(View.GONE);
                }
                updateRowsId();
            }
        });
        item.setSearchFieldHint(LocaleController.getString("Search", R.string.Search));
        emptyView = new EmptyTextProgressView(context);
        emptyView.setText(LocaleController.getString("NoResult", R.string.NoResult));
        emptyView.showTextView();
        emptyView.setShowAtCenter(true);
        frameLayout.addView(emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setEmptyView(emptyView);
        listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
                }
            }
        });
        return fragmentView;
    }

    private BaseTranslator loadLanguages() {
        BaseTranslator translator = Translator.getCurrentTranslator();
        targetLanguages = new ArrayList<>(translator.getTargetLanguages());
        names = new ArrayList<>();
        for (String language : targetLanguages) {
            names.add(getLanguage(language));
        }
        AndroidUtilities.selectionSort(names, targetLanguages);
        return translator;
    }

    private void fixMostImportantLanguages(String appLanguage) {
        int indexLangEn = targetLanguages.indexOf("en");
        names.add(0, names.get(indexLangEn));
        targetLanguages.add(0, targetLanguages.get(indexLangEn));
        names.remove(indexLangEn + 1);
        targetLanguages.remove(indexLangEn + 1);
        int indexLangClient = targetLanguages.indexOf(appLanguage);
        names.add(0, names.get(indexLangClient));
        targetLanguages.add(0, targetLanguages.get(indexLangClient));
        names.remove(indexLangClient + 1);
        targetLanguages.remove(indexLangClient + 1);
        targetLanguages.add(0, "app");
        names.add(0, getLanguage(appLanguage) + " - " + LocaleController.getString("Default", R.string.Default));
        if (!OwlConfig.translationTarget.equals("app")) {
            int indexLangSelected = targetLanguages.indexOf(OwlConfig.translationTarget);
            names.add(0, names.get(indexLangSelected));
            targetLanguages.add(0, targetLanguages.get(indexLangSelected));
            names.remove(indexLangSelected + 1);
            targetLanguages.remove(indexLangSelected + 1);
        }
    }

    private static String getLanguage(String language) {
        Locale locale = Locale.forLanguageTag(language);
        if (!TextUtils.isEmpty(locale.getScript())) {
            return HtmlCompat.fromHtml(String.format("%s - %s", AndroidUtilities.capitalize(locale.getDisplayScript()), AndroidUtilities.capitalize(locale.getDisplayScript(locale))), HtmlCompat.FROM_HTML_MODE_LEGACY).toString();
        } else {
            return String.format("%s - %s", AndroidUtilities.capitalize(locale.getDisplayName()), AndroidUtilities.capitalize(locale.getDisplayName(locale)));
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateRowsId() {
        rowCount = 0;
        languageHeaderRow = -1;
        if (!searchWas) {
            languageHeaderRow = rowCount++;
        }
        languagesStartRow = rowCount;
        rowCount += targetLanguages.size();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    private void getLanguagesFiltered(String filter) {
        filter = filter.toLowerCase().trim();
        fixMostImportantLanguages(loadLanguages().getCurrentAppLanguage());
        ArrayList<String> targetLanguagesTemp = new ArrayList<>();
        ArrayList<CharSequence> namesTemp = new ArrayList<>();
        for (int i = 0; i < targetLanguages.size(); i++) {
            String[] languages = names.get(i).toString().split(" - ");
            if ((languages.length > 2 ? languages[2]:languages[1]).toLowerCase().contains(filter) || languages[0].toLowerCase().contains(filter)) {
                targetLanguagesTemp.add(targetLanguages.get(i));
                namesTemp.add(names.get(i));
            }
        }
        targetLanguages = targetLanguagesTemp;
        names = namesTemp;
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
                    textRadioCell.setTextAndValueAndCheck(
                            languages.length > 2 ? languages[2]:languages[1],
                            languages[0],
                            isSelectedLanguage,
                            false,
                            true
                    );
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