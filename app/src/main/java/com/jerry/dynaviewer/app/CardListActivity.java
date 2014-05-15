package com.jerry.dynaviewer.app;

import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;

public class CardListActivity extends Activity
        implements CardListFragment.Callbacks {

    CardListFragment listFragment;
    CardDetailFragment detailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_twopane);

        listFragment = (CardListFragment) getFragmentManager().findFragmentById(R.id.card_list);
        listFragment.setActivateOnItemClick(true);
        detailFragment = (CardDetailFragment) getFragmentManager().findFragmentById(R.id.card_detail_container);

        View rootView = findViewById(R.id.root_view);
        rootView.setBackgroundColor(Color.GRAY);
    }


    // called when activity is started. this after creation.
    // force selection of the first list item (which would no want
    // to be done in single fragment mode

    @Override
    protected void onStart() {
        super.onStart();
        listFragment.ChangeSelectedItem(0);
        detailFragment.SetBackGroundColor(Color.GRAY);
    }

    /**
     * Callback method from {@link CardListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        detailFragment.setCard(id);
    }

            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
//            Bundle arguments = new Bundle();
//            arguments.putString(CardDetailFragment.ARG_ITEM_ID, id);
//            detailFragment = new CardDetailFragment();
//            detailFragment.setArguments(arguments);
//            getFragmentManager().beginTransaction()
//                    .replace(R.id.card_detail_container, detailFragment)
//                    .commit();
}
