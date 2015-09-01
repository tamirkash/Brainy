package com.example.first.kaganmoshe.brainy.AppActivities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.PopupWindow;

import com.example.first.kaganmoshe.brainy.AppActivities.ActionBarActivity.ActionBarAppActivity;
import com.example.first.kaganmoshe.brainy.AppManagement.AppManager;
import com.example.first.kaganmoshe.brainy.Feedback.FeedbackActivity;
import com.example.first.kaganmoshe.brainy.Feedback.FeedbackClass;
import com.example.first.kaganmoshe.brainy.Games.Dialogs.GameHelpDialog;
import com.example.first.kaganmoshe.brainy.Games.Dialogs.QuitGameDialog;
import com.example.first.kaganmoshe.brainy.Games.Dialogs.ResumeGameCountDown;
import com.example.first.kaganmoshe.brainy.Games.Dialogs.FinishGameDialog;
import com.example.first.kaganmoshe.brainy.Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by tamirkash on 8/3/15.
 */
public abstract class GameActivity extends ActionBarAppActivity implements ResumeGameCountDown.ResumeGameCommunicator,
        QuitGameDialog.QuitGameCommunicator, FinishGameDialog.FinishGameCommunicator, GameHelpDialog.GameHelpCommunicator {
    // Data Members
    protected FeedbackClass mFeedback;
    protected QuitGameDialog mQuitGameDialog = new QuitGameDialog();
    protected FinishGameDialog mFinishGameDialog = new FinishGameDialog();
    protected ResumeGameCountDown mResumeGameCountDown = new ResumeGameCountDown();
    protected GameHelpDialog mGameHelpDialog = new GameHelpDialog();
    protected Class mBackPressedActivityTarget = null;
    protected boolean mWasGameStarted = false;
    protected LinkedHashMap<String, String> mExtraStats = new LinkedHashMap<>();

//    protected GraphView mGraphView;
    //    private Class targetActivity = null;
    //    protected GraphFragment graphFragment;
//    protected LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
//    protected int lastXGraphAtt = 0;

    // Need To Implements
    protected abstract void startFeedbackSession();

    protected void onMenuPopupShow() {
        Log.d("FEEDBACK_USER_PAUSE", "PAUSED");
    }

//    protected abstract void onMenuPopupDismiss();

    protected int calculateScore(){
        return 100;
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        onPause();
        mQuitGameDialog.show(mFragmentManager, "SHOW QUIT");
    }

    // Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mQuitGameDialog.setListener(this);
        mFinishGameDialog.setListener(this);
        mResumeGameCountDown.setListener(this);
        mGameHelpDialog.setListener(this);

        initGameHelpDialog();
//        try {
//            mBackPressedActivityTarget = Class.forName(getIntent().getStringExtra(Utils.CALLING_CLASS));
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }

        mBackPressedActivityTarget = MainActivity.class;
    }

    private void initGameHelpDialog() {
        String gameTitle = MainActivity.EGameItem.getGameNameByClass(this.getClass());

        mGameHelpDialog.setHelpTitleText(gameTitle);
    }

//    @Override
//    public void onGameDialogBackPressed() {
//        Utils.startNewActivity(this, MainActivity.class);
//    }

    @Override
    protected void onStart(){
        super.onStart();

//        AppManager.getInstance().muteMusicForAppRequest(true);

        if(!mWasGameStarted && !AppManager.getInstance().getGamesManager().isDailyPracticeModeOn()){
            mWasGameStarted = true;
            mGameHelpDialog.show(mFragmentManager, "helpDialog");
        }
    }

    @Override
    protected boolean playMusicInActivity(){
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d("RESUME", "onResume");

        if (!mQuitGameDialog.isShowing() && !mSettingsFragment.isShowing() && !mHomeButtonPopup.isShowing()
                && !mResumeGameCountDown.isShowing() && !mGameHelpDialog.isShowing()) {
            mResumeGameCountDown.show(mFragmentManager, "SHOW COUNTDOWN");
        }

//        if(this.hasWindowFocus() && !mHomeButtonPopup.isShowing()){
//            mResumeGameCountDown.show(mFragmentManager, "SHOW COUNTDOWN");
//        }

//        showResumeCountdown();
//                Log.d("GRAPH_LIFE", "RESUME_GRAPH_ON_RESUME");
//                resumeReceivingEEGData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("GRAPH_LIFE", "STOPPING_GRAPH_ON_PAUSE");
        mFeedback.incNumOfUserPauses();
        stopReceivingEEGData();
    }

    protected void stopReceivingEEGData() {
        Log.d("GRAPH_LIFE", "STOPPING_GRAPH");

        if (mFeedback != null) {
            mFeedback.stopTimerAndRecievingData();
        }
    }

    protected void resumeReceivingEEGData() {
        Log.d("GRAPH_LIFE", "RESUME_GRAPH");


        if (mFeedback != null) {
            mFeedback.resumeTimerAndReceivingData();
        }
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        graphFragment.stopReceivingData();
//        mFeedback.stopTimerAndRecievingData();
//    }

    protected void showFinishDialog() {
        onPause();
        AppManager.getInstance().getGamesManager().showFinishDialog(mFragmentManager, this);
//        mFinishGameDialog.show(mFragmentManager, "FinishGameDialog");
    }

//    @Override
//    public void onFinishDialogConfirmed() {
//        Intent intent = makeIntentForFeedback();
//
//        intent.putExtra("CALLING_CLASS", this.getClass().getCanonicalName());
//        Utils.startNewActivity(this, intent);
//    }

//    @Override
//    protected void homeMenuButtonClicked() {
//        mResumeGameCountDown.show(mFragmentManager, "Show Resume");
//        super.homeMenuButtonClicked();
//    }

//    private Intent makeIntentForFeedback() {
//        return makeIntent(FeedbackActivity.class);
//    }

    private Intent makeIntentForFinishedGame(Class targetActivity){
        Intent intent = new Intent(getApplicationContext(), targetActivity);
//        int score = calculateScore();

        intent.putExtra(FeedbackActivity.DISTRACTION_STAT, Integer.toString(mFeedback.getDistractionScore()));
        intent.putExtra(FeedbackActivity.SCORE_STAT, Integer.toString(mFeedback.getGameScore()));
        intent.putParcelableArrayListExtra(FeedbackActivity.CURR_GAME_CONCENTRATION_POINTS, mFeedback.getConcentrationPoints());
//        intent.putExtra(FeedbackActivity.CURR_GAME_TIME_SECONDS, mFeedback.getSessionTimeInSeconds());
//        intent.putExtra(FeedbackActivity.CURR_GAME_TIME_MINUTES, mFeedback.getSessionTimeInMinutes());
//        intent.putExtra(FeedbackActivity.PLAY_AGAIN_ACTIVITY_TARGET, getIntent().getStringExtra(Utils.CALLING_CLASS));
//        intent.putExtra(FeedbackActivity.TOTAL_TIME, mStopWatch.toString());
        intent.putExtra(FeedbackActivity.PLAY_AGAIN_ACTIVITY_TARGET, this.getClass().getCanonicalName());
        addTotalTimeSessionFeedbackStat(intent);

        return intent;
    }

//    private int calculateDistraction() {
//        int score;
//
//        switch (mFeedback.getNumOfUserPauses()) {
//            case 0:
//                score = 150;
//                break;
//            case 1:
//                score = 95;
//                break;
//            case 2:
//                score = 85;
//                break;
//            default:
//                score = 60;
//                break;
//        }
//
//        return score;
//    }

//    protected void setNewStatsListAndContinue(LinkedHashMap<String, String> mExtraStats) {
//        ArrayList<String> extraStatKeys = new ArrayList<>();
//        Intent intent = makeIntentForFeedback();
//
//        for (String extraStat : mExtraStats.keySet()) {
//            intent.putExtra(extraStat, mExtraStats.get(extraStat));
//            extraStatKeys.add(extraStat);
//        }
//
//        intent.putStringArrayListExtra(FeedbackActivity.EXTRA_STATS, extraStatKeys);
//        Utils.startNewActivity(this, intent);
//    }

//    protected void continueToNextActivity(Class targetActivity){
//        Intent intent = makeIntentForFinishedGame(targetActivity);
//
//        loadExtraStatsToIntent(intent);
//        Utils.startNewActivity(this, intent);
//    }

    protected void loadExtraStatsToIntent(Intent intent){
        ArrayList<String> extraStatKeys = new ArrayList<>();

        for (String extraStat : mExtraStats.keySet()) {
            intent.putExtra(extraStat, mExtraStats.get(extraStat));
            extraStatKeys.add(extraStat);
        }

        intent.putStringArrayListExtra(FeedbackActivity.EXTRA_STATS, extraStatKeys);
    }

    protected void addNewStatForFeedback(String name, String value){
        mExtraStats.put(name, value);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean value = super.onOptionsItemSelected(item);
        stopReceivingEEGData();
        onMenuPopupShow();

        mHomeButtonPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (!mQuitGameDialog.isShowing() && !mSettingsFragment.isShowing() && !mHomeButtonPopup.isShowing()) {
                    onResume();
                }
            }
        });

        return value;
    }

    @Override
    public void onQuitGameConfirmed() {
//        if (targetActivity == null) {
//            Utils.startNewActivity(this, mBackPressedActivityTarget);
//        } else {
//            Utils.startNewActivity(this, targetActivity);
//        }
        AppManager.getInstance().getGamesManager().continueAfterQuitConfirmed(this);
//        Utils.startNewActivity(this, MainActivity.class);
    }

    @Override
    public void onQuitGameCanceled() {
        onResume();
    }

    @Override
    public void onGameResumed() {
        resumeReceivingEEGData();
    }

    @Override
    public void onDialogBackClicked(Class thisClass) {
        //TODO - not good !!
        Log.d("Back class", thisClass.toString());

        if (thisClass == FinishGameDialog.class) {
            onBackClickedFinishGame();
        } else if (thisClass == QuitGameDialog.class) {
            onBackClickedQuitGame();
        } else if (thisClass == ResumeGameCountDown.class) {
            onBackClickedResumeGame();
        }
    }

    protected void onBackClickedResumeGame() {
//        mQuitGameDialog.show(mFragmentManager, "QUIT CONFIRMATION");
    }

    protected void onBackClickedQuitGame() {
        onResume();
    }

    protected void onBackClickedFinishGame() {
        Utils.startNewActivity(this, MainActivity.class);
    }

    @Override
    public void onDialogShow(Class thisClass) {
        mHomeButtonPopup.dismiss();
        mFeedback.stopTimerAndRecievingData();

        if (thisClass == FinishGameDialog.class) {
            onFinishGameShow();
        } else if (thisClass == QuitGameDialog.class) {
            onQuitGameShow();
        } else if (thisClass == ResumeGameCountDown.class) {
            onResumeGameShow();
        }
    }

    protected void onResumeGameShow() {

    }

    protected void onQuitGameShow() {

    }

    protected void onFinishGameShow() {
//        AppManager.getHistoryDBInstance(getApplicationContext())
        mFeedback.insertRecordToHistoryDB(getApplicationContext(), getGameName());
        Log.d("DAILY PRACTICE", "INSERT RECORD " + getGameName());
    }

    @Override
    protected void onQuitClicked() {
//        mQuitGameDialog.show(mFragmentManager, "QUIT CONFIRMATION");
        AppManager.getInstance().getGamesManager().showQuitDialog(mFragmentManager, mQuitGameDialog);
    }

    @Override
    public void onFinishGameContinueClicked() {
        AppManager.getInstance().getGamesManager().continueToActivityAfterGameFinished(this);
//        continueToNextActivity(FeedbackActivity.class);

//        Intent intent = makeIntentForFinishedGame(FeedbackActivity.class);
//        intent.putExtra("CALLING_CLASS", this.getClass().getCanonicalName());
//        Utils.startNewActivity(this, intent);
    }

    @Override
    protected void onPopupMenuOptionSelected() {
//        this.targetActivity = MainActivity.class;
        mQuitGameDialog.show(mFragmentManager, "QuitGameDialog");
    }

    @Override
    public void onSettingsShow() {
//        mSettingsFragment.show(mFragmentManager, "SETTINGS SHOW");
    }

//    protected abstract int calculateScore();

//    @Override
//    public void onSettingsBackPressed() {
//        onResume();
//        onGameDialogBackPressed();
//    }
//
//    @Override
//    public void onSettingsDonePressed(){
//        onResume();
//    }

    protected abstract void addTotalTimeSessionFeedbackStat(Intent intent);

    protected String getGameName(){
        return "Guess The Number";
    }

    public Intent prepareIntentForFeedback() {
//        Intent intent = makeIntentForFinishedGame(targetActivity);
        Intent intent = makeIntentForFinishedGame(FeedbackActivity.class);

        loadExtraStatsToIntent(intent);

        return intent;
    }

    public FeedbackClass getFeedback() {
        return mFeedback;
    }

    public FinishGameDialog getFinishGameDialog(){
        return mFinishGameDialog;
    }

    @Override
    public void onStartClicked() {
        mGameHelpDialog.dismiss();
        onResume();
    }
}