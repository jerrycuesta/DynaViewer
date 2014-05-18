package com.jerry.dynaviewer.app;

import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;

import com.parse.Parse;
import com.parse.ParseObject;

// CardListActivity: Main (and only activity)
// delegates work to fragments and acts and intermediary
//      for communication between fragments
// handles options menu

public class CardListActivity extends Activity
        implements CardListFragment.OnItemChanged {

    CardListFragment listFragment;
    CardDetailFragment detailFragment;

    // options menu flag on whether content comes from local files or from Parse
    boolean usingLocalContent = true;

    // onCreate: Called on Activity Creation
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //  Initialize Parse Keys
        Parse.initialize(   this,
                            ParseKeys.ParseApplicationID,
                            ParseKeys.ParseClientKey);

        // set view
        setContentView(R.layout.activity_card_twopane);

        // create list and detail fragments
        listFragment = (CardListFragment) getFragmentManager().
                                    findFragmentById(R.id.card_list);

        //listFragment.setActivateOnItemClick(true);
        detailFragment = (CardDetailFragment) getFragmentManager().
                                    findFragmentById(R.id.card_detail_container);

        // set background
        // TODO: move to layout?
        findViewById(R.id.root_view).setBackgroundColor(Color.GRAY);
    }


    // onStart:  called when activity is started. (after onCreate)
    // force selection of the first list item (which would not want
    // to be done in single fragment mode
    @Override
    protected void onStart() {
        super.onStart();

        // TODO: Move the set of initial item in CardListFragment class?
        listFragment.ChangeSelectedItem(0);

        // TODO: move to layout or to detail fragment implementation
        detailFragment.setBackGroundColor(Color.GRAY);
    }


    // onItemSelected: ListFragment callback when list item selected
    //  this method is part of the ListFragment.Callbacks interface
    @Override
    public void onItemSelected(ParseObject obj) {
        detailFragment.setCard(obj, usingLocalContent);
    }


    // onCreateOptionsMenu: Create options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    // onOptionsItemSelected: options menu handler
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_local_content) {
            // toggle using local content flag and
            // change options menu text
            // TODO: Change to checkbox
            if (usingLocalContent) {
                item.setTitle("Use Local  Content");
                usingLocalContent = false;
                listFragment.LoadRemoteCards();
            } else {
                item.setTitle("Use Remote Content");
                usingLocalContent = true;
                listFragment.LoadLocalCards();
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
