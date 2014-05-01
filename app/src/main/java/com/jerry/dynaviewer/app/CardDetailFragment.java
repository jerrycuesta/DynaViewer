package com.jerry.dynaviewer.app;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.jerry.dynaviewer.app.dummy.DummyContent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A fragment representing a single Card detail screen.
 * This fragment is either contained in a {@link CardListActivity}
 * in two-pane mode (on tablets) or a {@link CardDetailActivity}
 * on handsets.
 */
public class CardDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private DummyContent.DummyItem mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CardDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }
    }

    //load file from apps res/raw folder or Assets folder
    private String GetData(String fileName) throws IOException
    {
        //Create a InputStream to read the file into
        InputStream iS;

        //get the file as a stream
        iS = getResources().getAssets().open(fileName);

        //create a buffer that has the same size as the InputStream
        byte[] buffer = new byte[iS.available()];
        //read the text file as a stream, into the buffer
        iS.read(buffer);
        //create a output stream to write the buffer into
        ByteArrayOutputStream oS = new ByteArrayOutputStream();
        //write this buffer to the output stream
        oS.write(buffer);
        //Close the Input and Output streams
        oS.close();
        iS.close();

        //return the output stream as a String
        return oS.toString();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_card_detail, container, false);
        TextView textView = (TextView) rootView.findViewById(R.id.card_detail);

        final String filename = mItem.content;
        String xmlText;

        try {
            xmlText = GetData(filename);
        }
        catch (Exception ex) {
                textView.setText("Failed to read file: " + filename);
                return rootView;
        }

        DynaCard card = new DynaCard(xmlText);

        try {
            card.Load();
        }
        catch (Exception ex) {
            textView.setText("Xml Exception: " + ex.toString());
            return rootView;
        }

        StringBuilder text = new StringBuilder();
        for (DynaCard.LoadPosPair pair : card.SurfacePoints) {

            text.append(pair.load).append(", ").append(pair.pos);
            text.append(System.getProperty("line.separator"));
        }
        textView.setText(text);

        return rootView;
    }
}
