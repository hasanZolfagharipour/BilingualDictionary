package com.maktab14dictionary.controller.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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

import com.maktab14dictionary.R;
import com.maktab14dictionary.adapter.DictionaryAdapter;
import com.maktab14dictionary.controller.MainActivity;
import com.maktab14dictionary.controller.fragments.dialog.WordDialogFragment;
import com.maktab14dictionary.enums.DialogWordState;
import com.maktab14dictionary.model.Word;
import com.maktab14dictionary.repositiory.DictionaryRepository;
import com.maktab14dictionary.utils.Globals;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class WordListFragment extends Fragment implements DictionaryAdapter.OnDictionaryAdapterListener {

    public static final String SHARED_PREFERENCES_LANGUAGE_SELECTED_DIALOG = "SharedPreferencesLanguageSelectedDialog";
    public static final int REQUEST_CODE_WORD_DIALOG = 0;
    public static final String TAG_WORD_DIALOG = "TagWordDialog";
    public static final String TAG = "tag";
    public static final String BUNDLE_BOOLEAN_IS_EN = "BundleBooleanIsEn";
    private ImageView mImageViewFlag;
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private SearchView mSearchView;
    private EditText mEditTextSearch;
    private MainActivity mActivity;
    private DictionaryAdapter mAdapter;
    private int mLanguageState;
    private OnWordListListener mOnWordListListener;
    private DictionaryRepository mRepository;
    private int mPosition;
    private boolean mBooleanIsEn = true;

    public static WordListFragment newInstance() {

        Bundle args = new Bundle();

        WordListFragment fragment = new WordListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLanguageState = Globals.getSavedState(getActivity(), SHARED_PREFERENCES_LANGUAGE_SELECTED_DIALOG);
        mRepository = DictionaryRepository.getInstance(getActivity());
        onConfiguration(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_word_list, container, false);

        findViews(view);

        setToolbar();

        setFlag(mBooleanIsEn);
        setSearchView();

        setAdapter();

        return view;
    }

    private void findViews(View view) {
        mToolbar = view.findViewById(R.id.toolbar);
        mImageViewFlag = view.findViewById(R.id.imgFlag);
        mSearchView = view.findViewById(R.id.search);
        mEditTextSearch = mSearchView.findViewById(R.id.search_src_text);
        mRecyclerView = view.findViewById(R.id.wordListRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void setToolbar() {

        setHasOptionsMenu(true);

        mActivity = (MainActivity) getActivity();

        mActivity.setSupportActionBar(mToolbar);
        mActivity.getSupportActionBar().setTitle(R.string.app_name);

        setCountOfWords();

    }

    private void setAdapter() {

        if (mAdapter == null) {
            mAdapter = new DictionaryAdapter(mRepository.getWords(), this, mBooleanIsEn);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setWordList(mRepository.getWords());
            mAdapter.notifyDataSetChanged();
        }
    }

    private void setCountOfWords() {

        mActivity.getSupportActionBar().setSubtitle("Words: " + mRepository.getWords().size());
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

    private void onConfiguration(Bundle saveInstanceState){
        if (saveInstanceState != null)
            mBooleanIsEn = saveInstanceState.getBoolean(BUNDLE_BOOLEAN_IS_EN);

    }


    private void setKey() {

        if (mEditTextSearch.getText().toString().length() > 0 && mEditTextSearch.getText().toString().charAt(0) >= 97 && mEditTextSearch.getText().toString().charAt(0) <= 122) {
            setFlag(true);
        }
        else if (mEditTextSearch.getText().toString().length() > 0 && (mEditTextSearch.getText().toString().charAt(0) < 97 || mEditTextSearch.getText().toString().charAt(0) > 122)) {
            mBooleanIsEn  =  false;
            setFlag(false);
        }
        mAdapter.setBooleanIsEn(mBooleanIsEn);
        setAdapter();
    }

    private void setFlag(boolean isEn){
        mBooleanIsEn = isEn;
        if (mBooleanIsEn)
            mImageViewFlag.setImageDrawable(mActivity.getDrawable(R.drawable.ic_us));
        else
            mImageViewFlag.setImageDrawable(mActivity.getDrawable(R.drawable.ic_iran));
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
            case R.id.menuExit:
                mOnWordListListener.finished();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
                .setSingleChoiceItems(new String[]{getString(R.string.english), getString(R.string.persian)}, mLanguageState, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String lang = "en";
                        if (which == 0) {
                            lang = "en";
                            mLanguageState = 0;

                        } else if (which == 1) {
                            lang = "fa";
                            mLanguageState = 1;
                        }
                        Globals.saveLanguageState(getActivity(), SHARED_PREFERENCES_LANGUAGE_SELECTED_DIALOG, mLanguageState);
                        mOnWordListListener.onChangeLanguage(lang);
                    }
                })
                .show();
    }

    @Override
    public void onItemHolderListener(Word word, int position) {
        Log.d(TAG, "mPosition: " + mPosition);
        mPosition = position;
        Log.d(TAG, "mPosition: " + mPosition);
        showWordDialog(word);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK || data == null)
            return;

        if (requestCode == REQUEST_CODE_WORD_DIALOG) {
            DialogWordState wordState = (DialogWordState) data.getSerializableExtra(WordDialogFragment.EXTRA_ITEM_DELETE_UPDATE_ADD);

            //todo --> if search an item then notify in specific item doesn't work properly because searchList closed.

            if (wordState == DialogWordState.DELETE) {
                mAdapter.notifyItemRemoved(mPosition);
            } else if (wordState == DialogWordState.UPDATE) {
                mAdapter.notifyItemChanged(mPosition);
                mAdapter.setHighLight(true, mRepository.getWords().get(mPosition).getUUID());
            } else if (wordState == DialogWordState.ADD) {
                mAdapter.notifyItemInserted(mRepository.getWords().size());
                mAdapter.setHighLight(true, mRepository.getWords().get(mRepository.getWords().size() - 1).getUUID());
                mRecyclerView.smoothScrollToPosition(mRepository.getWords().size());
            }
            setCountOfWords();
            mAdapter.setWordList(mRepository.getWords());


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
