package com.example.first.kaganmoshe.brainy.HistoryDataBase;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.first.kaganmoshe.brainy.AppManagement.AppManager;
import com.example.first.kaganmoshe.brainy.AppActivities.ActionBarActivity.ActionBarAppActivity;
import com.example.first.kaganmoshe.brainy.AppActivities.MainActivity;
import com.example.first.kaganmoshe.brainy.R;
import com.example.first.kaganmoshe.brainy.Utils;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;
import com.weiwangcn.betterspinner.library.BetterSpinner;

import java.util.ArrayList;
import java.util.Date;

public class HistoryActivity extends ActionBarAppActivity {

    public class HistoryDataPoint extends com.jjoe64.graphview.series.DataPoint {
        private final HistoryRecordData mData;

        public HistoryDataPoint(double x, double y, HistoryRecordData data) {
            super(x, y);
            this.mData = data;
        }

        public HistoryDataPoint(Date x, double y, HistoryRecordData data) {
            super((double) x.getTime(), y);
            this.mData = data;
        }

        public HistoryRecordData getData() {
            return this.mData;
        }
    }

    private BetterSpinner mShownItemsSpinner;
    private BetterSpinner mTimeRangeSpinner;
    private HistoryDBAdapter.ETimeRange mCurrTimeRangeSelection = HistoryDBAdapter.ETimeRange.LAST_WEEK;
    private String mCurrGameSelection = "Guess The Number";
    private GraphView mGraphView;
    private LineGraphSeries<DataPoint> mConcentrationSeries;
    private LineGraphSeries<DataPoint> mScoreSeries;
    private TextView mNameTextView;
    private TextView mScoreTextView;
    private TextView mDateTextView;
    private TextView mConcentrationTextView;
//    private Button mBackButton;

    private static final int SCORE_COLOR = Color.RED;
    private static final int CONCENTRATION_COLOR = Color.BLUE;

    private static final String[] ITEMS = new String[]{
            "Guess The Number",
            "Crazy Cube",
            "MindShooter",
            "Hot Air Balloon",
            "Daily Practices"
    };

    private static final ArrayList<String> TIME_RANGES = new ArrayList<>();

    static {
        for (HistoryDBAdapter.ETimeRange timeRange : HistoryDBAdapter.ETimeRange.values()) {
            TIME_RANGES.add(timeRange.toString());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        String[] timeRangesStringArray = new String[TIME_RANGES.size()];

        for (int i = 0; i < TIME_RANGES.size(); i++) {
            timeRangesStringArray[i] = TIME_RANGES.get(i);
        }

        ArrayAdapter<String> itemsAdapter = new ArrayAdapter<>(this, R.layout.app_spinner_row, ITEMS);
        ArrayAdapter<String> timeRangeAdapter = new ArrayAdapter<>(this, R.layout.app_spinner_row, timeRangesStringArray);

        mShownItemsSpinner = (BetterSpinner) findViewById(R.id.showListSpinner);
        mTimeRangeSpinner = (BetterSpinner) findViewById(R.id.timeRangeSpinner);
        mGraphView = (GraphView) findViewById(R.id.graph);
        mNameTextView = (TextView) findViewById(R.id.nameTextView);
        mScoreTextView = (TextView) findViewById(R.id.scoreTextView);
        mConcentrationTextView = (TextView) findViewById(R.id.concentrationTextView);
        mDateTextView = (TextView) findViewById(R.id.dateTextView);
//        mBackButton = (Button) findViewById(R.id.historyBackButton);

        setTouchNClick(R.id.historyBackButton);

        findViewById(R.id.historyBackButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mTimeRangeSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HistoryDBAdapter.ETimeRange selection = HistoryDBAdapter.ETimeRange.enumValueOf(parent.getItemAtPosition(position).toString());

                resetTextViews();

                if (mCurrTimeRangeSelection != selection) {
                    mCurrTimeRangeSelection = HistoryDBAdapter.ETimeRange.enumValueOf(parent.getItemAtPosition(position).toString());
                    addRecords();

                    mTimeRangeSpinner.onItemClick(parent, view, position, id);
                }
            }
        });
        mTimeRangeSpinner.setAdapter(timeRangeAdapter);

        mShownItemsSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selection = parent.getItemAtPosition(position).toString();

                resetTextViews();

                if (!mCurrGameSelection.equals(selection)) {
                    mCurrGameSelection = parent.getItemAtPosition(position).toString();
                    addRecords();

                    mShownItemsSpinner.onItemClick(parent, view, position, id);
                }

                Log.d("HISTORY ACTIVITY", "currNameSelection: " + mCurrGameSelection);
            }
        });
        mShownItemsSpinner.setAdapter(itemsAdapter);

        setTouchNClick(R.id.showListSpinner);
        setTouchNClick(R.id.timeRangeSpinner);

        initConcentrationSeries();
        initScoreSeries();
        initGraph();
        addRecords();

    }

    private void addRecords() {
        ArrayList<HistoryRecordData> records = AppManager.getHistoryDBInstance(getApplicationContext()).getRecords(mCurrGameSelection,
                mCurrTimeRangeSelection);

        makeGraph(records);
//        for(String record : records){
//            addRecord(record);
//        }
    }

//    private void addRecord(String record) {
//        TextView recordTextView = new TextView(getApplicationContext());
//
//        recordTextView.setText(record);
//
//        itemsLayout.addView(recordTextView);
//    }

    @Override
    public void onBackPressed() {
        Utils.startNewActivity(this, MainActivity.class);
    }

    private void makeGraph(ArrayList<HistoryRecordData> records) {
        mGraphView.removeAllSeries();
        mGraphView.getSecondScale().getSeries().clear();

        if(records.size() > 0) {
            initConcentrationSeries();
            getRecordsToSeries(mConcentrationSeries, records);
            mGraphView.addSeries(mConcentrationSeries);

            if (records.get(0).score.length() > 0) {
                initScoreSeries();
                getRecordsToSeries(mScoreSeries, records);
                mGraphView.getSecondScale().addSeries(mScoreSeries);
            }
        }
//        mConcentrationSeries.appendData(new DataPoint(index++, 0), false, Integer.MAX_VALUE);
//
//        for (HistoryRecordData record : records) {
//            mConcentrationSeries.appendData(new HistoryDataPoint(index++, Double.valueOf(record.concentration), record),
//                    false, Integer.MAX_VALUE);
//
////            mScoreSeries.appendData(new HistoryDataPoint(index++, Double.valueOf(record.score), record),
////                    false, Integer.MAX_VALUE);
//        }
//
//
//        mConcentrationSeries.setOnDataPointTapListener(new OnDataPointTapListener() {
//            @Override
//            public void onTap(Series series, DataPointInterface dataPoint) {
//                if (dataPoint instanceof HistoryDataPoint) {
//                    showData(((HistoryDataPoint) dataPoint).getData());
//                }
//            }
//        });

//        if (records.size() > 0) {
//            index = 0;
//            mScoreSeries.appendData(new DataPoint(index++, 0), false, Integer.MAX_VALUE);
//
//
//            if (records.get(0).score.length() > 0) {
//                for (HistoryRecordData record : records) {
//                    mScoreSeries.appendData(new HistoryDataPoint(index++, Double.valueOf(record.score), record),
//                            false, Integer.MAX_VALUE);
//                }
//
//                mScoreSeries.setOnDataPointTapListener(new OnDataPointTapListener() {
//                    @Override
//                    public void onTap(Series series, DataPointInterface dataPoint) {
//                        if (dataPoint instanceof HistoryDataPoint) {
//                            showData(((HistoryDataPoint) dataPoint).getData());
//                        }
//                    }
//                });
//
//
//            }
//        }
    }

    private void getRecordsToSeries(LineGraphSeries<DataPoint> series, ArrayList<HistoryRecordData> records){
        int index = 0;

        series.appendData(new DataPoint(index++, 0), false, Integer.MAX_VALUE);

        for (HistoryRecordData record : records) {
            String value = series == mScoreSeries ? record.score : record.concentration;

            series.appendData(new HistoryDataPoint(index++, Double.valueOf(value), record),
                    false, Integer.MAX_VALUE);
        }


        series.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                if (dataPoint instanceof HistoryDataPoint) {
                    showData(((HistoryDataPoint) dataPoint).getData());
                }
            }
        });
    }

    private void showData(HistoryRecordData data) {
        if (!data.date.equals(mDateTextView.getText())) {
            resetTextViews();

            mNameTextView.setText(data.name);
            mScoreTextView.setText(data.score);
            mDateTextView.setText(data.date);
            mConcentrationTextView.setText(data.concentration);
        }
    }

    private void resetTextViews() {
        mNameTextView.setText("");
        mScoreTextView.setText("");
        mConcentrationTextView.setText("");
        mDateTextView.setText("");
    }

    private void initGraph() {
        GridLabelRenderer gridLabelRenderer = mGraphView.getGridLabelRenderer();

//        NumberFormat nf = NumberFormat.getInstance();
//        nf.setMinimumFractionDigits(0);
//        nf.setMinimumIntegerDigits(0);
//        nf.setMaximumIntegerDigits(5);
//        nf.setMaximumFractionDigits(5);
//        mGraphView.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(nf, nf));

        mGraphView.getSecondScale().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // show normal x values
                    return super.formatLabel(value, isValueX);
                }

                return String.valueOf((int) value) + " \u00A0";
            }
        });


//        gridLabelRenderer.setHighlightZeroLines(false);


//        gridLabelRenderer.setTextSize(1);

//        gridLabelRenderer.setLabelsSpace(6);
        Viewport viewport = mGraphView.getViewport();
        viewport.setXAxisBoundsManual(true);
        viewport.setYAxisBoundsManual(true);
        viewport.setMaxX(10);
        viewport.setMinY(0);
        viewport.setMaxY(100);
        viewport.setScrollable(true);
        mGraphView.getSecondScale().setMaxY(1000);

        gridLabelRenderer.setNumHorizontalLabels(4);
        gridLabelRenderer.setHorizontalLabelsVisible(false);
        gridLabelRenderer.setVerticalLabelsSecondScaleColor(SCORE_COLOR);
        gridLabelRenderer.setVerticalLabelsColor(CONCENTRATION_COLOR);
    }

//    private void initSeries() {
//        mConcentrationSeries = new LineGraphSeries<>();
//        mScoreSeries = new LineGraphSeries<>();
//
//        mScoreSeries.setDataPointsRadius(7f);
//        mScoreSeries.setDrawDataPoints(true);
//        mScoreSeries.setThickness(6);
//        mConcentrationSeries.setDrawDataPoints(true);
//        mConcentrationSeries.setDataPointsRadius(7f);
//        mConcentrationSeries.setThickness(6);
//
//        mGraphView.getLegendRenderer().setVisible(true);
//        mConcentrationSeries.setTitle("Concentration");
//        mGraphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
//        mGraphView.getLegendRenderer().setBackgroundColor(android.R.color.transparent);
//        mConcentrationSeries.setColor(CONCENTRATION_COLOR);
//        mScoreSeries.setTitle("Score");
//        mScoreSeries.setColor(SCORE_COLOR);
//    }

    private void initConcentrationSeries(){
        mConcentrationSeries = new LineGraphSeries<>();

        mConcentrationSeries.setDrawDataPoints(true);
        mConcentrationSeries.setDataPointsRadius(7f);
        mConcentrationSeries.setThickness(6);

        mGraphView.getLegendRenderer().setVisible(true);
        mConcentrationSeries.setTitle("Concentration");
        mGraphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        mGraphView.getLegendRenderer().setBackgroundColor(android.R.color.transparent);
        mConcentrationSeries.setColor(CONCENTRATION_COLOR);
    }

    private void initScoreSeries(){
        mScoreSeries = new LineGraphSeries<>();

        mScoreSeries.setDataPointsRadius(7f);
        mScoreSeries.setDrawDataPoints(true);
        mScoreSeries.setThickness(6);

        mScoreSeries.setTitle("Score");
        mScoreSeries.setColor(SCORE_COLOR);
    }

    @Override
    protected void onResume(){
        super.onResume();
//        AppManager.playBackgroundMusic();
    }

    @Override
    protected void onPause(){
        super.onPause();
//        AppManager.pauseBackgroundMusic();
    }
}