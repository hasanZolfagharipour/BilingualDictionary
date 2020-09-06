package com.maktab14dictionary.controller.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.maktab14dictionary.DialogWordState;
import com.maktab14dictionary.R;
import com.maktab14dictionary.adapter.DictionaryAdapter;
import com.maktab14dictionary.controller.fragments.dialog.WordDialogFragment;
import com.maktab14dictionary.model.Word;
import com.maktab14dictionary.repositiory.DictionaryRepository;
import com.maktab14dictionary.utils.Globals;

public class WordListFragment extends Fragment implements DictionaryAdapter.OnDictionaryAdapterListener {

    public static final String SHARED_PREFERENCES_LANGUAGE_SELECTED_DIALOG = "SharedPreferencesLanguageSelectedDialog";
    public static final int REQUEST_CODE_WORD_DIALOG = 0;
    public static final String TAG_WORD_DIALOG = "TagWordDialog";
    public static final String TAG = "tag";


    public static WordListFragment newInstance() {

        Bundle args = new Bundle();

        WordListFragment fragment = new WordListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private Toolbar mToolbar;
    private FloatingActionButton mFabAddWord;
    private RecyclerView mRecyclerView;
    private SearchView mSearchView;


    private DictionaryAdapter mAdapter;
    private int mLanguageState;
    private OnWordListListener mOnWordListListener;
    private DictionaryRepository mRepository;
    private int mPosition;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLanguageState = Globals.getSavedState(getActivity(), SHARED_PREFERENCES_LANGUAGE_SELECTED_DIALOG);
        mRepository = DictionaryRepository.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_word_list, container, false);

        findViews(view);

        setToolbar();

        setSearchView();

        setAdapter();

        setListener();

        return view;
    }

    private void findViews(View view){
        mToolbar = view.findViewById(R.id.toolbar);
        mFabAddWord = view.findViewById(R.id.fabAddWord);
        mSearchView = view.findViewById(R.id.search);
        mRecyclerView = view.findViewById(R.id.wordListRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void setToolbar() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        setHasOptionsMenu(true);
        activity.setSupportActionBar(mToolbar);
        mToolbar.setTitleTextColor(Color.WHITE);
        activity.getSupportActionBar().setTitle(R.string.app_name);
        activity.getSupportActionBar().setSubtitle("Words: " + mRepository.getWords().size());

    }

    private void setAdapter(){

        if (mAdapter == null){
            mAdapter = new DictionaryAdapter(mRepository.getWords(), this);
            mRecyclerView.setAdapter(mAdapter);
        }else
        {
            mAdapter.setWordList(mRepository.getWords());
            mAdapter.notifyDataSetChanged();
        }
    }

    private void setListener(){

        mFabAddWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWordDialog(null);
            }
        });
    }

    private void showWordDialog(Word word){
        WordDialogFragment fragment = WordDialogFragment.newInstance(word);
        fragment.setTargetFragment(WordListFragment.this, REQUEST_CODE_WORD_DIALOG);
        fragment.show(getActivity().getSupportFragmentManager(),TAG_WORD_DIALOG);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
       inflater.inflate(R.menu.my_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menuShare:
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

    public interface OnWordListListener{
        void onChangeLanguage(String lang);
        void finished();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK || data == null)
            return;

        if (requestCode == REQUEST_CODE_WORD_DIALOG){
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
            mAdapter.setWordList(mRepository.getWords());


        }




    }

    private void setSearchView(){

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
    }
}
