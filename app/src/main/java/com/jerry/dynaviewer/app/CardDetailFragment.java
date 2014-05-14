package com.jerry.dynaviewer.app;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.jerry.dynacard.DynaCard;

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

    private XYPlot mPlot;
    private String mId;

    public CardDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mId = getArguments().getString(ARG_ITEM_ID);
        }
    }

    // AsyncTask<Params, Progress, Result>

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_card_detail, container, false);

        mPlot = (XYPlot) rootView.findViewById(R.id.cardPlot);
        mPlot.setTitle("Surface Card " + mId);
        mPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.GRAY);

        LoadCardTask task = new LoadCardTask();
        task.execute(mId);

        //new LoadCardTaskSync().Load(mItem);

        return rootView;
    }

    private class LoadCardTask extends AsyncTask<String, Void, DynaCard> {
        @Override
        protected DynaCard doInBackground(String... urls) {
            String Filename = urls[0];
            AssetManager assets = getActivity().getApplicationContext().getAssets();
            DynaCard card = new DynaCard("");
            try {
                InputStream inString = assets.open(Filename);
                card.Load(inString);
                StringBuilder line = new StringBuilder("Dynacard: ");
            } catch (Exception ex) {
                // TODO: propogate error
                //Toast.makeText(getActivity().getApplicationContext(), "Exception loading card " + mItem, Toast.LENGTH_SHORT).show();
            }
            return card;
        }

        @Override
        protected void onPostExecute(DynaCard result) {
            PlotCard(result);
            mPlot.redraw();
        }
    }

    private class LoadCardTaskSync
    {
        protected void Load(String Filename) {
            AssetManager assets = getActivity().getApplicationContext().getAssets();
            DynaCard card = new DynaCard("");
            try {
                InputStream inString = assets.open(Filename);
                card.Load(inString);
                StringBuilder line = new StringBuilder("Dynacard: ");
            } catch (Exception ex) {
                // TODO: propogate error
                //Toast.makeText(getActivity().getApplicationContext(), "Exception loading card " + mItem, Toast.LENGTH_SHORT).show();
            }
            PlotCard(card);
        }
    }

    // Plot the card

    void PlotCard(DynaCard card)
    {
        Context context = getActivity();

        ArrayList<Number> xValues = new ArrayList<Number>();
        ArrayList<Number> yValues = new ArrayList<Number>();

        for (int i=0; i<card.SurfacePoints.size(); i++)
        {
            xValues.add(card.SurfacePoints.get(i).pos);
            yValues.add(card.SurfacePoints.get(i).load);
        }

        final String seriesTitle = "";

        // Turn the above arrays into XYSeries':
        XYSeries series1 = new SimpleXYSeries(
                xValues,
                yValues,
                seriesTitle); // Set the display title of the series


        // Create a formatter to use for drawing a series using LineAndPointRenderer
        // and configure it from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter();
        series1Format.setPointLabelFormatter(new PointLabelFormatter());
        series1Format.configure(context,
                R.xml.line_point_formatter_with_plf1);


        mPlot.addSeries(
                series1,                            /* #1896BD */
                new LineAndPointFormatter(Color.rgb(0, 0, 0), Color.rgb(128, 128, 128),
                                            null, null)
        );


        // reduce the number of range labels
        mPlot.setTicksPerRangeLabel(3);
        mPlot.getGraphWidget().setDomainLabelOrientation(-45);
    }
}
