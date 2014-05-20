package com.jerry.dynaviewer.app;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// CardListFragment: Fragment which shows list of DynaCards

public class CardListFragment extends ListFragment {

    // callback when item changes
    public interface OnItemChanged {
        public void onItemSelected(ParseObject obj);
    }

    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private OnItemChanged mOnItemChanged = null;
    private int mActivatedPosition = ListView.INVALID_POSITION;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CardListFragment() {
    }

    // TODO: use adapter from ParseObject instead of separate array.
    // note: at present these must be in the same order
    Map<String, ParseObject> cardInfo = new HashMap<String, ParseObject>();
    ArrayList<String> cardTitles = new ArrayList<String>();

    ArrayAdapter<String> adapter;


    // LoadLocalCards: Load names of list items from local catds
    // TODO: card source should be provided via interface
    public void LoadLocalCards() {
        cardInfo.clear();
        cardTitles.clear();

        AssetManager assets = getActivity().getApplicationContext().getAssets();
        try {
            String[] files = assets.list("");
            Calendar calendar = Calendar.getInstance();
            for (String item : files) {
                if (item.toLowerCase().endsWith(".xml"))
                {
                    ParseObject obj = new ParseObject(ParseKeys.ParseObjectClass);
                    obj.put(ParseKeys.ParseKeyFilename, item);
                    obj.put(ParseKeys.ParseKeyTimeStamp, calendar.getTime());
                    cardInfo.put(item, obj);
                    cardTitles.add(item);
                    calendar.add(Calendar.SECOND, 1);
                }
            }
        } catch (Exception ex) {
        }

        if (adapter!=null) {
            System.out.println("LoadRemoteCards::SetAdapter");
            System.out.println("cardTitles: " + Integer.toString(cardTitles.size()));
            System.out.println("cardNames: " + Integer.toString(cardInfo.size()));

            adapter.notifyDataSetChanged();
            ChangeSelectedItem(0);
        }
    }


    // LoadLocalCards: Load names of list items from remote catds
    // TODO: card source should be provided via interface
    public void LoadRemoteCards() {
        System.out.println("LoadRemoteCards: ");

        cardInfo.clear();
        cardTitles.clear();

        System.out.println("LoadRemoteCards:...");

        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseKeys.ParseObjectClass);
        query.selectKeys(Arrays.asList("facility", "index", "timestamp"));
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> results, ParseException e) {
                if (e == null) {
                    for (ParseObject obj : results) {
                        String facility = obj.getString(ParseKeys.ParseKeyFacility);
                        Number index = obj.getNumber(ParseKeys.ParseKeyIndex);
                        String title = facility + ":" + index.toString();
                        cardTitles.add(title);
                        cardInfo.put(title, obj);
                    }

                    if (adapter!=null) {
                        System.out.println("LoadRemoteCards::SetAdapter");
                        System.out.println("cardTitles: " + Integer.toString(cardTitles.size()));
                        System.out.println("cardNames: " + Integer.toString(cardInfo.size()));

                        Collections.sort(cardTitles);
                        adapter.notifyDataSetChanged();
                        ChangeSelectedItem(0);
                    }

                } else {
                    Toast.makeText(getActivity(), "Failed to Get Card List", Toast.LENGTH_LONG).show();
                    System.out.println("LoadRemoteCards:" + "Parse Exception: " + e.getMessage());
                }
            }
        });
    }


    // onCreate: called on fragment creation
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create the adapter to the data

        LoadLocalCards();

        adapter = new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_list_item_activated_1, cardTitles) {

            // override getView to return the correct view
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                LayoutInflater inflater = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View rowView = inflater.inflate(R.layout.list_item_detail, parent, false);
                TextView textView1 = (TextView) rowView.findViewById(R.id.listItemFirstLine);
                TextView textView2 = (TextView) rowView.findViewById(R.id.listItemSecondLine);
                String title = cardTitles.get(position);
                textView1.setText(title);
                ParseObject item = cardInfo.get(title);
                Date date = item.getDate(ParseKeys.ParseKeyTimeStamp);
                textView2.setText(date.toString());
                return rowView;
        }};

        // set the array adapter
        setListAdapter(adapter);
    }


    // onCreateView: overridden to provide opportunity to set list view to
    //  to single selection mode (listview maintains highlighted selection)

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        return view;
    }


    // called when the fragment is attached to an activity
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof OnItemChanged)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mOnItemChanged = (OnItemChanged) activity;
        //LoadRemoteCards();
    }


    // called when the fragment is detached from an activity
    @Override
    public void onDetach() {
        super.onDetach();

        // no one to call back to
        mOnItemChanged = null;
    }


    // hook into list item click event and inform parent activity
    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        if (mOnItemChanged!=null) {
            String title = cardTitles.get(position);
            mOnItemChanged.onItemSelected(cardInfo.get(title));
        }
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


    // ChangeSelectedItem: change the list view selection
    public void ChangeSelectedItem(int position)
    {
        System.out.println("ChangeSelectedItem: " + Integer.toString(position));
        System.out.println("cardTitles: " + Integer.toString(cardTitles.size()));
        System.out.println("cardNames: " + Integer.toString(cardInfo.size()));

        if (position == ListView.INVALID_POSITION) {
            // clear selection on invalid position
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            // call back if change card
            getListView().setItemChecked(position, true);
            if (mOnItemChanged!=null) {
                String title = cardTitles.get(position);
                mOnItemChanged.onItemSelected(cardInfo.get(title));
            }
        }

        getListView().setSelection(position);
        mActivatedPosition = position;
    }
}
