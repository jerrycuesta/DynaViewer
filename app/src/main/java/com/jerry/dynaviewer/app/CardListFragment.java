package com.jerry.dynaviewer.app;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


import java.util.ArrayList;

/**
 * A list fragment representing a list of Cards. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link CardDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class CardListFragment extends ListFragment {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(String id);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CardListFragment() {
    }

    ArrayList<String> cardNames = new ArrayList<String>();

    public void LoadLocalCards() {
        cardNames.clear();
        AssetManager assets = getActivity().getApplicationContext().getAssets();
        try {
            String[] files = assets.list("");
            for (String item : files) {
                if (item.toLowerCase().endsWith(".xml"))
                {
                    cardNames.add(item);
                }
            }
        } catch (Exception ex) {
        }

        if (adapter!=null) {
            adapter.notifyDataSetChanged();
            ChangeSelectedItem(0);
        }
    }

    public void LoadRemoteCards() {
        cardNames.clear();
        final String[] parseKeys = {
                "i5BgALrYsg", "QlEt2tO2Bq", "quDe6WaRr5", "B07he3MbB0","t9pI4llTpR",
                "7An7eE5VW2","1uRkcj2V4C","cznt6i2tPG","kU1GPSs2qt","4pgzNZT10m",
                "p6WqnxlrhX","3PC5m6vB8C","6Nz5ELxdaY","2vtZVBvWDW","O5gkXfjoOB",
                "vTH1xzdiPs","ay6xRBkkuJ","wJY7GEPJYK","I36CnLBxQL","r7hTSWKYEy",
                "NrxGMMgTuC","EnLmMfcYef","5Z2UCtqdwj","mVivHmDBOQ","tYeeCFULxY",
                "GHRWiLOpKW","Lye9FWyhYu","i2FyackdQI","Z8rziqiP0w","EpGgYNVBwu",
                "bUWZlPstDg","HmZvaFXKpl","i9bjUAUxst" };

        for (String name  : parseKeys) {
            cardNames.add(name);
        }

        if (adapter!=null) {
            adapter.notifyDataSetChanged();
            ChangeSelectedItem(0);
        }
    }

    // called on fragment creation

    ArrayAdapter<String> adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create the adapter to the data

        LoadLocalCards();

        adapter = new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_list_item_activated_1, cardNames) {

            // override getView to return the correct view
            @Override
            public View getView(int position, View convertView,
                                ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                TextView textView = (TextView) view.findViewById(android.R.id.text1);

                textView.setTextColor(Color.WHITE);

                return view;
            }
        };

        setListAdapter(adapter);
    }

    // called to set the current item in the list view
    // calls back to report change

    void ChangeSelectedItem(int item)
    {
        getListView().setSelection(item);
        setActivatedPosition(item);
        mCallbacks.onItemSelected(cardNames.get(item));
    }


    // called when the fragment is attached to an activity

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
        LoadRemoteCards();
    }


    // called when the fragment is dettached to an activity

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }


    // hook into list item click event and inform parent activity
    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onItemSelected(cardNames.get(position));
    }

    // save state so that when resumed the same item is selected
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }
}
