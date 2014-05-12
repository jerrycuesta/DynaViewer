package com.jerry.dynaviewer.app;

import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;

public class CardListActivity extends Activity
        implements CardListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */

    CardListFragment listFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_list);

        View detailContainer = findViewById(R.id.card_detail_container);

        if (detailContainer != null) {
            detailContainer.getRootView().setBackgroundColor(Color.GRAY);
            listFragment = (CardListFragment) getFragmentManager().findFragmentById(R.id.card_list);
            listFragment.setActivateOnItemClick(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }


    // called when activity is started. this after creation.
    // force selection of the first list item (which would no want
    // to be done in single fragment mode

    @Override
    protected void onStart() {
        super.onStart();
        listFragment.ChangeSelectedItem(0);
    }

    /**
     * Callback method from {@link CardListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {

            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(CardDetailFragment.ARG_ITEM_ID, id);
            CardDetailFragment fragment = new CardDetailFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .replace(R.id.card_detail_container, fragment)
                    .commit();
    }
}
