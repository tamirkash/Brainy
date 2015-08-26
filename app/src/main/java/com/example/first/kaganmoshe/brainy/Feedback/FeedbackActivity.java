package com.example.first.kaganmoshe.brainy.Feedback;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.first.kaganmoshe.brainy.CustomActivity.ActionBarAppActivity;
import com.example.first.kaganmoshe.brainy.GamesActivity;
import com.example.first.kaganmoshe.brainy.Utils;
import com.example.first.kaganmoshe.brainy.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

public class FeedbackActivity extends ActionBarAppActivity {
    //TODO - take care of the onFinishDialogConfirmed method of the games
    //TODO - constants for the extra stats
    public static final String CURR_GAME_CONCENTRATION_POINTS = "currGameConcentrationPoints";
    public static final String CURR_GAME_TIME_MINUTES = "currGameTimeMinutes";
    public static final String CURR_GAME_TIME_SECONDS = "currGameTimeSeconds";
    public static final String EXTRA_STATS = "EXTRA_STATS";
    public static final String SCORE_STAT = "SCORE_STAT";
    public static final String DISTRACTION_STAT = "DISTRACTION_STAT";
    //    public static final String GAME_DISTRACTION_STAT = "GAME_DISTRACTION_STAT";
    public static final String SCORE_LABEL = "Score";
    public static final String PLAY_AGAIN_ACTIVITY_TARGET = "PLAY_AGAIN_ACTIVITY_TARGET";
    private static final int FACTOR = 20;
    public static final int BEST_CONCENTRATION_SCORE = 85;
    public static final String CONCENTRATION_AVERAGE = "Concentration";
    public static final String TOTAL_TIME = "TOTAL_TIME";
    private int finalScore;

    protected GraphView graphView;
    protected LineGraphSeries<DataPoint> graphConcentrationPoints = new LineGraphSeries<>();
    protected ArrayList<ParcelableDataPoint> parcelableConcentrationPointsList;
    protected TextView timeView;
    protected Button backButton;
    protected Button playAgainButton;
    protected LinearLayout feedbackStatsLayout;
    protected TextView bestScoreTextView;
    protected TextView scoreTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        feedbackStatsLayout = (LinearLayout) findViewById(R.id.feedbackStatsLayout);
        bestScoreTextView = (TextView) findViewById(R.id.feedbackBestScoreTextView);
        scoreTextView = (TextView) findViewById(R.id.feedbackScoreTextView);

        initGameTime();
        initConcentrationPoints();
        initScoreStat();
        initBestScore();
        initExtraStats();
        initGraph();
        initButtons();
    }

    private void initBestScore() {
        checkBestScore();
    }

    private void checkBestScore(){
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        int defaultValue = getResources().getInteger(R.integer.default_high_score);
        long highScore = sharedPref.getInt(getIntent().getStringExtra(Utils.CALLING_CLASS), defaultValue);

        if(highScore < finalScore){
            setBestScore();
            scoreTextView.setTextColor(getResources().getColor(R.color.feedback_best_score_text));
//            addStat("New high score", String.valueOf(finalScore));
        }

        bestScoreTextView.append(String.valueOf(sharedPref.getInt(getIntent().getStringExtra(Utils.CALLING_CLASS), defaultValue)));
    }

    private void setBestScore(){
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getIntent().getStringExtra(Utils.CALLING_CLASS), finalScore);
        editor.commit();
    }

    private void initScoreStat() {
        int gameScore = Integer.valueOf(getIntent().getStringExtra(SCORE_STAT));
        int generalDistraction = Integer.valueOf(getIntent().getStringExtra(DISTRACTION_STAT));
//        int gameDistraction = Integer.valueOf(getIntent().getStringExtra(GAME_DISTRACTION_STAT));

//        int finalScore = (int) (gameScore * GAME_SCORE_WEIGHT + generalDistraction * GENERAL_DISTRACTION_WEIGHT
//                + concentrationScore() * CONCENTRATION_WEIGHT);
        int concentrationScore = FeedbackClass.getConcentrationScore(parcelableConcentrationPointsList);

        addConcentrationStat(concentrationScore);

        if(concentrationScore > BEST_CONCENTRATION_SCORE) {
            concentrationScore = 100;
        }

        finalScore = gameScore + generalDistraction + concentrationScore;

        scoreTextView.setText(Integer.toString(finalScore));
    }

    private void addConcentrationStat(int score){
        addStat(CONCENTRATION_AVERAGE, Integer.toString(score) + " (0-100)");
    }

    private void initExtraStats() {
        Intent intent = getIntent();
        ArrayList<String> stats = intent.getStringArrayListExtra(EXTRA_STATS);

        if (stats != null) {
            for (String statName : stats) {
                String statValue = intent.getStringExtra(statName);
                addStat(statName, statValue);
            }
        }
    }

    private void addStat(String statName, String statValue) {
        LinearLayout newStatLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.layout_feedback_stat, null);
        TextView statKeyText = (TextView) newStatLayout.findViewById(R.id.statNameText);
        TextView statValueText = (TextView) newStatLayout.findViewById(R.id.statValueText);

        statKeyText.setText(formatStatKey(statName));
        statValueText.setText(formatStatValue(statValue));

        feedbackStatsLayout.addView(newStatLayout);
    }

    private String formatStatValue(String statValue) {
        return statValue.substring(0, 1).toUpperCase() + statValue.substring(1, statValue.length()).toLowerCase();
    }

    private String formatStatKey(String statName) {
        return statName.substring(0, 1).toUpperCase() + statName.substring(1, statName.length()).toLowerCase() + ":";
    }

    private void initGameTime() {
//        int sessionTimeMin = (int) getIntent().getLongExtra(CURR_GAME_TIME_MINUTES, 0);
//        int sessionTimeSec = (int) getIntent().getLongExtra(CURR_GAME_TIME_SECONDS, 0) % 60;
        timeView = (TextView) findViewById(R.id.feedbackTimeViewText);
        timeView.append(getIntent().getStringExtra(TOTAL_TIME));

//        timeView.append("" + Integer.toString(sessionTimeMin) + ":" + Integer.toString(sessionTimeSec));
    }

    private void initButtons() {
        backButton = (Button) findViewById(R.id.backFeedbackButton);
        playAgainButton = (Button) findViewById(R.id.playAgainFeedbackButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewActivity(GamesActivity.class);
            }
        });

        playAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("PLAY_AGAIN_TARGET", getIntent().getStringExtra(PLAY_AGAIN_ACTIVITY_TARGET));
                if (getIntent().getStringExtra(PLAY_AGAIN_ACTIVITY_TARGET) != null) {
                    try {
                        startNewActivity(Class.forName(getIntent().getStringExtra(PLAY_AGAIN_ACTIVITY_TARGET)));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    startNewActivity(GamesActivity.class);
                }
            }
        });
    }

    private void startNewActivity(Class toActivity) {
        Utils.startNewActivity(this, toActivity);
    }

    @Override
    public void onBackPressed() {
        Utils.startNewActivity(this, GamesActivity.class);
    }

    private void initConcentrationPoints() {
        parcelableConcentrationPointsList = getIntent().getParcelableArrayListExtra(CURR_GAME_CONCENTRATION_POINTS);

//        prepareConcentrationPoints();
        for (ParcelableDataPoint p : parcelableConcentrationPointsList) {
            graphConcentrationPoints.appendData(p, false, Integer.MAX_VALUE);
        }
    }

    private void initGraph() {
//        ArrayList<ParcelableDataPoint> parcelableConcentrationPointsList = getIntent().getParcelableArrayListExtra(CURR_GAME_CONCENTRATION_POINTS);
//
////        prepareConcentrationPoints();
//        for(ParcelableDataPoint p : parcelableConcentrationPointsList){
//            graphConcentrationPoints.appendData(p, false, Integer.MAX_VALUE);
//        }

//        initTitle();

        graphView = (GraphView) findViewById(R.id.graph);
        graphConcentrationPoints.setThickness(6);

        GridLabelRenderer gridLabelRenderer = graphView.getGridLabelRenderer();
        gridLabelRenderer.setNumHorizontalLabels(2);
        gridLabelRenderer.setNumVerticalLabels(3);
        gridLabelRenderer.setHorizontalLabelsVisible(false);

        Viewport viewport = graphView.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setXAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(100);
        viewport.setMaxX(graphConcentrationPoints.getHighestValueX());
        viewport.setScrollable(false);

        graphView.addSeries(graphConcentrationPoints);
    }

    private void prepareConcentrationPoints() {
//        ArrayList<ParcelableDataPoint> parcelableConcentrationPointsList = getIntent().getParcelableArrayListExtra(CURR_GAME_CONCENTRATION_POINTS);
//        double size = parcelableConcentrationPointsList.size() / FACTOR;
//        int currX = 0;
//        int currY = 0;
//        int currConcentrationListIndex = 0;
//
//        for(int i = 0; i < size; i++){
//            for(int y = 0; y < FACTOR; y++){
//                currY += parcelableConcentrationPointsList.get(currX++).getY();
//            }
//
//            graphConcentrationPoints.appendData(new DataPoint(currConcentrationListIndex++, currY / FACTOR), false, Integer.MAX_VALUE);
//            currY = 0;
//        }
    }

//    private void initTitle() {
//        feedbackTitle = (TextView) findViewById(R.id.feedbackTitle);
//        Utils.changeFont(getAssets(), feedbackTitle);
//    }
}
