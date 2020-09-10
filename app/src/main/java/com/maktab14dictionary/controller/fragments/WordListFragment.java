package com.maktab14dictionary.controller.fragments;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;

import com.airbnb.lottie.LottieAnimationView;
import com.maktab14dictionary.R;
import com.maktab14dictionary.adapter.DictionaryAdapter;
import com.maktab14dictionary.controller.activity.MainActivity;
import com.maktab14dictionary.controller.fragments.dialog.WordDialogFragment;
import com.maktab14dictionary.enums.WordState;
import com.maktab14dictionary.model.Word;
import com.maktab14dictionary.repositiory.DictionaryRepository;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class WordListFragment extends Fragment implements DictionaryAdapter.OnDictionaryAdapterListener {

    public static WordListFragment newInstance() {

        Bundle args = new Bundle();
        WordListFragment fragment = new WordListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static final String SHARED_PREFERENCES_LANGUAGE_SELECTED_DIALOG = "SharedPreferencesLanguageSelectedDialog";
    public static final String SHARED_PREFERENCE_BOOLEAN_IS_DATABASE_COPIED = "SharedPreferenceBooleanIsDatabaseCopied";

    public static final int REQUEST_CODE_WORD_DIALOG = 0;
    public static final String TAG_WORD_DIALOG = "TagWordDialog";
    public static final String TAG = "tag";
    public static final String BUNDLE_BOOLEAN_IS_EN = "BundleBooleanIsEn";


    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private SearchView mSearchView;
    private EditText mEditTextSearch;
    private ImageView mImageViewEmptyList;


    private SharedPreferences mPreferences;
    private DictionaryAdapter mAdapter;
    private OnWordListListener mOnWordListListener;
    private DictionaryRepository mRepository;

    private int mLanguageState;
    private int mPosition;
    private boolean mBooleanIsEn = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mLanguageState = mPreferences.getInt(SHARED_PREFERENCES_LANGUAGE_SELECTED_DIALOG, 0);
        mRepository = DictionaryRepository.getInstance(getActivity());

        onConfiguration(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_word_list, container, false);

        findViews(view);

        setToolbar();

        setListener();

        setSearchView();

        copyDialog();

        setAdapter();

        return view;
    }

    private void findViews(View view) {
        mToolbar = view.findViewById(R.id.toolbar);

        mImageViewEmptyList = view.findViewById(R.id.imgEmptyList);
        mSearchView = view.findViewById(R.id.search);
        mEditTextSearch = mSearchView.findViewById(R.id.search_src_text);
        mRecyclerView = view.findViewById(R.id.wordListRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void setToolbar() {
        setHasOptionsMenu(true);

        ((MainActivity) getActivity()).setSupportActionBar(mToolbar);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name);
    }

    private void setAdapter() {

        if (mAdapter == null) {
            mAdapter = new DictionaryAdapter(mRepository.getWords(), this, mBooleanIsEn);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setWordList(mRepository.getWords());
            mAdapter.notifyDataSetChanged();
        }
        setCountOfWords();
        handleEmptyList();
    }

    private void copyDialog() {

        if (!mPreferences.getBoolean(SHARED_PREFERENCE_BOOLEAN_IS_DATABASE_COPIED, false)){
            mPreferences.edit().putBoolean(SHARED_PREFERENCE_BOOLEAN_IS_DATABASE_COPIED, true).apply();
            showQuestionDialog();

        }
    }

    private void showQuestionDialog() {

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.copy_database)
                .setMessage(R.string.import_question)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showLoadingDialog();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();

    }

    @SuppressLint("ResourceType")
    private void showLoadingDialog() {

        AlertDialog.Builder loadingBuilder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.loading_layout, null);
        final LottieAnimationView lottie = view.findViewById(R.id.loadingLottie);
        lottie.setAnimation(R.raw.load_import);

        loadingBuilder.setTitle(R.string.loading).setView(view);

        final AlertDialog loadingDialog = loadingBuilder.create();
        loadingDialog.show();


        lottie.playAnimation();
        lottie.setMaxProgress(0.75f);


        lottie.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mRepository.copyDatabase();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                loadingDialog.dismiss();
                setAdapter();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void showDeletingDialog(){

        AlertDialog.Builder loadingBuilder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.loading_layout, null);
        final LottieAnimationView lottie = view.findViewById(R.id.loadingLottie);
        lottie.setAnimation(R.raw.lottie_delete);

        loadingBuilder.setTitle(R.string.loading).setView(view);

        final AlertDialog loadingDialog = loadingBuilder.create();
        loadingDialog.show();


        lottie.playAnimation();



        lottie.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mRepository.copyDatabase();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mRepository.deleteDatabase();
                setAdapter();
                loadingDialog.dismiss();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void setCountOfWords() {
        ((MainActivity)getActivity()).getSupportActionBar().setSubtitle(getString(R.string.word_count) + mRepository.getWords().size());
    }

    private void showWordDialog(Word word) {
        WordDialogFragment fragment = WordDialogFragment.newInstance(word);
        fragment.setTargetFragment(WordListFragment.this, REQUEST_CODE_WORD_DIALOG);
        fragment.show(getActivity().getSupportFragmentManager(), TAG_WORD_DIALOG);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(BUNDLE_BOOLEAN_IS_EN, mBooleanIsEn);
    }

    private void onConfiguration(Bundle saveInstanceState) {
        if (saveInstanceState != null)
            mBooleanIsEn = saveInstanceState.getBoolean(BUNDLE_BOOLEAN_IS_EN);
    }

    private void setKey() {

        if (mEditTextSearch.getText().toString().length() > 0 && mEditTextSearch.getText().toString().charAt(0) >= 97 && mEditTextSearch.getText().toString().charAt(0) <= 122)
            mBooleanIsEn = true;
         else if (mEditTextSearch.getText().toString().length() > 0 && (mEditTextSearch.getText().toString().charAt(0) < 97 || mEditTextSearch.getText().toString().charAt(0) > 122))
            mBooleanIsEn = false;

        mAdapter.setBooleanIsEn(mBooleanIsEn);
        setAdapter();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.my_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menuAddWord:
                showWordDialog(null);
                return true;
            case R.id.menuLanguage:
                getLanguageDialog();
                return true;
            case R.id.menuDatabaseManager:
                item.getSubMenu().clearHeader();
                if (mRepository.getWords().size() == 0)
                    item.getSubMenu().findItem(R.id.menuRemoveDatabase).setEnabled(false);
                else
                    item.getSubMenu().findItem(R.id.menuRemoveDatabase).setEnabled(true);
                return true;
            case R.id.menuReset:
                dialogDatabaseManager(WordState.UPDATE);
                return true;
            case R.id.menuRemoveDatabase:
                dialogDatabaseManager(WordState.DELETE);
                return true;
            case R.id.menuExit:
                mOnWordListListener.finished();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void dialogDatabaseManager(final WordState wordState) {
        String title = "";

        if (wordState == WordState.DELETE)
            title = getString(R.string.delete);
        else if (wordState == WordState.UPDATE)
            title = getString(R.string.reset);

        new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(getString(R.string.sure_delete_reset, title.toLowerCase()))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (wordState == WordState.DELETE) {
                            showDeletingDialog();
                        }
                        else if (wordState == WordState.UPDATE) {
                            showLoadingDialog();
                        }

                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void setListener() {
        mImageViewEmptyList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWordDialog(null);
            }
        });
    }

    private void handleEmptyList() {
        if (mRepository.getWords().size() == 0) {
            mImageViewEmptyList.setVisibility(View.VISIBLE);
            mImageViewEmptyList.bringToFront();
        } else
            mImageViewEmptyList.setVisibility(View.GONE);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnWordListListener)
            mOnWordListListener = (OnWordListListener) context;
    }

    private void getLanguageDialog() {

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.choose)
                .setSingleChoiceItems(new String[]{getString(R.string.english), getString(R.string.persian), getString(R.string.arabic), getString(R.string.french)}, mLanguageState, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String lang = "en";
                        if (which == 0) {
                            lang = "en";
                            mLanguageState = 0;

                        } else if (which == 1) {
                            lang = "fa";
                            mLanguageState = 1;
                        }else if (which == 2) {
                            lang = "ar";
                            mLanguageState = 2;
                        }else if (which == 3){
                            lang = "fr";
                            mLanguageState = 3;
                        }
                        mPreferences.edit().putInt(SHARED_PREFERENCES_LANGUAGE_SELECTED_DIALOG, mLanguageState).apply();
                        mOnWordListListener.onChangeLanguage(lang);
                    }
                })
                .show();
    }

    @Override
    public void onItemHolderListener(Word word, int position) {
        mPosition = position;
        showWordDialog(word);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK || data == null)
            return;

        if (requestCode == REQUEST_CODE_WORD_DIALOG) {
            WordState wordState = (WordState) data.getSerializableExtra(WordDialogFragment.EXTRA_ITEM_DELETE_UPDATE_ADD);

            //todo --> if search an item then notify in specific item doesn't work properly because searchList closed.

            if (wordState == WordState.DELETE) {
                mAdapter.notifyItemRemoved(mPosition);
            } else if (wordState == WordState.UPDATE) {
                mAdapter.notifyItemChanged(mPosition);
                mAdapter.setHighLight(true, mRepository.getWords().get(mPosition).getUUID());
            } else if (wordState == WordState.ADD) {
                mAdapter.notifyItemInserted(mRepository.getWords().size());
                mAdapter.setHighLight(true, mRepository.getWords().get(mRepository.getWords().size() - 1).getUUID());
                mRecyclerView.scrollToPosition(mRepository.getWords().size() - 1);
            }
            setCountOfWords();
            mAdapter.setWordList(mRepository.getWords());
            handleEmptyList();
        }


    }

    private void setSearchView() {

        mSearchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return true;
            }
        });


        mEditTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setKey();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public interface OnWordListListener {
        void onChangeLanguage(String lang);
        void finished();
    }
}
