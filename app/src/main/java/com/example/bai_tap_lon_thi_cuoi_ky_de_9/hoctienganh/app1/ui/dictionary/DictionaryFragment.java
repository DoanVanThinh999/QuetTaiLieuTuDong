package com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.ui.dictionary;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.Fragment;


import com.example.bai_tap_lon_thi_cuoi_ky_de_9.R;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.databinding.FragmentDictionaryBinding;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.DictionaryAdapter;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.exception.NotEnoughItemsException;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.util.Arrays;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.util.DictionaryEntry;
import com.example.bai_tap_lon_thi_cuoi_ky_de_9.hoctienganh.app1.util.Settings;

import java.util.ArrayList;
import java.util.List;

public class DictionaryFragment extends Fragment implements View.OnTouchListener, View.OnKeyListener {
    private FragmentDictionaryBinding mB;
    private DictionaryAdapter mAd;
    private Settings mSt;
    private boolean mInit; // Tránh tự động gọi onItemSelectedListener khi setSelection cho spinner

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mB = FragmentDictionaryBinding.inflate(inflater, container, false);
        mInit = true;
        mSt = Settings.from(getContext());

        // Adapter
        mAd = new DictionaryAdapter(getContext(), new ArrayList<>());
        mAd.setNotifyOnChange(false);
        mAd.addAll(DictionaryEntry.getAll(getContext()));
        resetList(mSt.getListViewType());

        // exploreWord
        TooltipCompat.setTooltipText(mB.exploreWord, getString(R.string.explore_word));
        mB.exploreWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (mB.filterTypeSpinner.getSelectedItemPosition() != DictionaryEntry.FILTER_NONE)
                        throw new UnsupportedOperationException("Explore word is only available when the list is not filtered");
                    List<String> wordsNotLearned = new ArrayList<>(DictionaryEntry.getAllNames(getContext()));
                    wordsNotLearned.removeAll(Settings.from(getContext()).getLearnedWords());
                    mB.txtSearch.setQuery(Arrays.pickRandom(wordsNotLearned), true);
                    mB.txtSearch.setIconified(false);
                    mB.txtSearch.clearFocus();
                }
                catch (UnsupportedOperationException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), R.string.dictionary_explore_word_select_filter_none, Toast.LENGTH_LONG).show();
                }
                catch (NotEnoughItemsException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), getString(R.string.learned_all), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // listViewTypeSpinner
        TooltipCompat.setTooltipText(mB.listViewTypeSpinner, getString(R.string.dictionary_view_type));
        mB.listViewTypeSpinner.setAdapter(createIconSpinnerAdapter(R.array.spinner_list_view_type));
        mB.listViewTypeSpinner.setOnTouchListener(this);
        mB.listViewTypeSpinner.setOnKeyListener(this);
        mB.listViewTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (mInit) return;
                mSt.setListViewType(position);
                resetList(position);
                mAd.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        // filterTypeSpinner
        TooltipCompat.setTooltipText(mB.filterTypeSpinner, getString(R.string.dictionary_filter));
        mB.filterTypeSpinner.setAdapter(createIconSpinnerAdapter(R.array.spinner_filter_type));
        mB.filterTypeSpinner.setSelection(0, false);
        mB.filterTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mB.setFilterMode(i);
                mB.setShowFavoriteGuide(i == DictionaryEntry.FILTER_FAVORITES && DictionaryEntry.getFavorites(getContext()).size() == 0);
                mB.txtSearch.setQuery("", false);
                mB.txtSearch.setIconified(true);
                mAd.clear();
                mAd.addAll(getList());
                mAd.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        // txtSearch
        mB.txtSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                String tuCanTim = s.trim();
                mAd.clear();
                for (DictionaryEntry entry : getList())
                    if (entry.getName().toLowerCase().contains(tuCanTim.toLowerCase()))
                        mAd.add(entry);
                mAd.notifyDataSetChanged();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        mB.txtSearch.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                mAd.clear();
                mAd.addAll(getList());
                mAd.notifyDataSetChanged();
                return false;
            }
        });

        mB.lstDictionary.setAdapter(mAd);
        return mB.getRoot();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        Log.d("listenersAllowed", "Enabled on touch");
        allowListeners(view);
        return false;
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        Log.d("listenersAllowed", "Enabled on key");
        allowListeners(view);
        return false;
    }

    private void allowListeners(View view) {
        mInit = false;
        if (view == null) return;
        view.setOnTouchListener(null);
        view.setOnKeyListener(null);
    }

    private int getFilterType() {
        return mB.filterTypeSpinner.getSelectedItemPosition();
    }

    private List<DictionaryEntry> getList() {
        return getFilterType() == DictionaryEntry.FILTER_FAVORITES
                ? DictionaryEntry.getFavorites(getContext())
                : getFilterType() == DictionaryEntry.FILTER_LEARNED
                ? DictionaryEntry.getLearned(getContext())
                : DictionaryEntry.getAll(getContext());
    }

    public void resetList(int i) {
        mB.lstDictionary.setNumColumns(i == DictionaryEntry.VIEW_TYPE_COMPACT ? 1 :
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 3 : 2
        );
        mAd.setListViewType(i);
    }

    public ArrayAdapter<String> createIconSpinnerAdapter(@ArrayRes int arrayId) {
        return new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_single_choice, getResources().getStringArray(arrayId)) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                view.setVisibility(View.GONE);
                return view;
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mB.listViewTypeSpinner.setSelection(mSt.getListViewType(), false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mB = null;
    }
}
