package com.jerry.dynaviewer.app;

import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.parse.Parse;

public class CardListActivity extends Activity
        implements CardListFragment.Callbacks {

    boolean useingLocalContent = true;

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
        Parse.initialize(this, "dAjQJONfviTs3J5qJtAMFj3ckOkP1jputnoZ7juq", "NpQ3JuMrqR0Gp4SioUp3CXBfvudu8LbKCBr8phPw");
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
        detailFragment.setCard(id, useingLocalContent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_local_content) {
            if (useingLocalContent) {
                item.setTitle("Use Remote  Content");
                useingLocalContent = false;
                listFragment.LoadRemoteCards();
            } else {
                item.setTitle("Use Local Content");
                useingLocalContent = true;
                listFragment.LoadLocalCards();
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
