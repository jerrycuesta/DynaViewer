package com.jerry.dynaviewer.app;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.jerry.dynaviewer.app.dummy.DummyContent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

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

    private XYPlot plot;

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
    private String GetData(String fileName) throws IOException {
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

    private DynaCard getCard(String filename)
    {
        String xmlText;
        String error = "";

        try {
            xmlText = GetData(filename);
        }
        catch (Exception ex) {
            error = "Failed to read file: " + filename;
            return null;
        }

        DynaCard card = new DynaCard(xmlText);

        try {
            card.Load();
        }
        catch (Exception ex) {
            error = "Xml Exception: " + ex.toString();
            return null;
        }

        StringBuilder text = new StringBuilder();
        for (DynaCard.LoadPosPair pair : card.SurfacePoints) {

            text.append(pair.load).append(", ").append(pair.pos);
            text.append(System.getProperty("line.separator"));
        }

        return card;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_card_detail, container, false);

        //final String filename = mItem.content;

        Context context = getActivity();
        plot = (XYPlot) rootView.findViewById(R.id.cardPlot);
        plot.getGraphWidget().getGridBackgroundPaint().setColor(Color.GRAY);

        ArrayList<Number> xValues = new  ArrayList<Number>();
        ArrayList<Number> yValues = new  ArrayList<Number>();

        for (int i=0; i<Data.getLength(); i++)
        {
            xValues.add(Data.getXVal(i));
            yValues.add(Data.getYVal(i));
        }

        final String seriesTitle = "";

        // Turn the above arrays into XYSeries':
        XYSeries series1 = new SimpleXYSeries(
                xValues,
                yValues,
                seriesTitle);                             // Set the display title of the series


        // Create a formatter to use for drawing a series using LineAndPointRenderer
        // and configure it from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter();
        series1Format.setPointLabelFormatter(new PointLabelFormatter());
        series1Format.configure(context,
                R.xml.line_point_formatter_with_plf1);

        // add a new series' to the xyplot:
        //plot.addSeries(series1, series1Format);

        plot.addSeries(
                series1,
                new LineAndPointFormatter(Color.rgb(0, 0, 200), Color.rgb(0, 0, 100),
                        null,
                        (PointLabelFormatter) null));


        // reduce the number of range labels
        plot.setTicksPerRangeLabel(3);
        plot.getGraphWidget().setDomainLabelOrientation(-45);

        return rootView;
    }
}
