package com.example.first.kaganmoshe.brainy.Games.MindShooter;

import android.graphics.Point;

import com.example.first.kaganmoshe.brainy.AppManagement.AppManager;

import java.util.Random;

import EEG.EConnectionState;
import EEG.EHeadSetType;
import EEG.ESignalVolume;
import EEG.IHeadSetData;

/**
 * Created by kaganmoshe on 8/13/15.
 */
public class MindShooterLogic implements IHeadSetData{
    enum EEdges {
        LEFT (1),
        TOP (2),
        RIGHT(3),
        BOTTOM (4);

        private int val;
        EEdges(int val){ this.val = val; }
        public int getValue() { return val; }
        public static EEdges getEgdeByValue(int val){ 
            EEdges res = LEFT;
            switch (val){
                case 2: res = TOP; break;
                case 3: res = RIGHT; break;
                case 4: res = BOTTOM; break;
            }
            return res;
        }
    }

    // Data Members
    public static final String MIND_SHOOTER_LOGIC = "MindShooterLogic";
    private final int screenWidth;
    private final int screenHeight;
    private final int X_MinRangeScreen;
    private final int X_MaxRangeScreen;
    private final int Y_MinRangeScreen;
    private final int Y_MaxRangeScreen;

    private int m_CurrentAttention;
    private IMindShooter m_MindShooter;
    private Point m_CurrentBalloonLocation = new Point();
    private Point m_CurrentIntentLocation = new Point();
    private Point m_BalloonSize = new Point(250, 250);
    private Point m_IntentSize = new Point(180, 180);
    private boolean m_ListenToHeadSet= false;
    private int m_SpaceVal = 20;
    private int m_CurrentScore = 0;
    private Random rand = new Random();
    private EEdges m_CurrentEdge = EEdges.BOTTOM; 

    // Methods
    public MindShooterLogic(int screenWidth, int screenHeight, IMindShooter mindShooter) throws Exception {
        if (mindShooter == null)
            throw new Exception("Can't be a NULL listener! (IMindShooter is NULL)");

        if (AppManager.getInstance().getAppSettings().getHeadSetType() != EHeadSetType.Moker){
            AppManager.getInstance().getHeadSet().registerListener(this); // TODO - unregister
            m_ListenToHeadSet = true;
        }
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.m_MindShooter = mindShooter;
        m_CurrentBalloonLocation.set(screenWidth/2, screenHeight/4);
        m_CurrentIntentLocation.set(m_CurrentBalloonLocation.x/2, m_CurrentBalloonLocation.y*3);
        X_MaxRangeScreen = getX_MaxRangeForBalloon();
        X_MinRangeScreen = getX_MinRange();
        Y_MaxRangeScreen = getY_MaxRangeForBalloon();
        Y_MinRangeScreen = getY_MinRange();
    }

    public void startGame(){
        // TODO - Init: timer,
        m_MindShooter.setIntentLocation(m_CurrentIntentLocation);
        m_MindShooter.setBalloonLocation(m_CurrentBalloonLocation, true);
        m_MindShooter.setScore(m_CurrentScore);
    }

    @Override
    public void onAttentionReceived(int attValue) {
        if (m_ListenToHeadSet) {
            m_CurrentAttention = attValue;
            float attFranction = getAttentionAsFraction(attValue);

            if (attValue >= 75)  // Case that bring the intent right to the target
                goToTheTarget(attFranction);
            else if(attValue >= 65)
                newPositionForIntent(1);
            else if (attValue >= 55)
                newPositionForIntent(2);
            else if (attValue >= 50)
                newPositionForIntent(3);
            else if (attValue >= 45)
                newPositionForIntent(4);
            else if (attValue >= 40)
                newPositionForIntent(5);
            else if (attValue >= 35)
                newPositionForIntent(6);
            else if (attValue >= 30)
                newPositionForIntent(7);
            else if (attValue >= 25)
                newPositionForIntent(8);
            else { // Taking the intent far from
                goFarAwayFromTarget(attFranction, 2);
            }
        }
    }

    private void calculateNewLocationForBalloon() {
        int newX_Position;
        int newY_Position;

        do {
            newX_Position = rand.nextInt(getX_MaxRangeForBalloon() - getX_MinRange()) + getX_MinRange();
            newY_Position = rand.nextInt(getY_MaxRangeForBalloon() - getY_MinRange()) + getY_MinRange();
        } while (newY_Position >= Y_MaxRangeScreen - 1.5*m_BalloonSize.y &&
                newX_Position >= screenWidth / 2 - 1.5*m_BalloonSize.x &&
                newX_Position <= screenWidth / 2 + m_BalloonSize.x);

        m_CurrentBalloonLocation.set(newX_Position, newY_Position);
    }

    private float getAttentionAsFraction(int attVal) { return (attVal * 0.01f); }

    int m_PreviousRandPosition = 1;
    private void goFarAwayFromTarget(float attFranction, int doItAgain) {
        int randPosition;
        while ((randPosition = rand.nextInt(8) + 1) == m_PreviousRandPosition);
        m_PreviousRandPosition = randPosition;

        int newXPosition = getX_MinRange();
        int newYPosition = getY_MinRange();
        int x2 = 0, y2 = 0;

        switch (randPosition){
            case 1:
                newXPosition = getX_MinRange();
                newYPosition = getY_MinRange();
                x2 = getX_MaxRangeForIntent();
                y2 = getY_MaxRangeForIntent() / 2;
                m_PreviousRandPosition = 7;
                break;
            case 2:
                newXPosition = getX_MaxRangeForIntent();
                newYPosition = getY_MinRange();
                x2 = getX_MaxRangeForIntent() / 2;
                y2 = getY_MaxRangeForIntent();
                m_PreviousRandPosition = 8;
                break;
            case 3:
                newXPosition = getX_MaxRangeForIntent();
                newYPosition = getY_MaxRangeForIntent();
                x2 = getX_MinRange();
                y2 = getY_MaxRangeForIntent() / 2;
                m_PreviousRandPosition = 5;
                break;
            case 4:
                newXPosition = getX_MinRange();
                newYPosition = getY_MaxRangeForIntent();
                x2 = getX_MaxRangeForIntent() / 2;
                y2 = getY_MinRange();
                m_PreviousRandPosition = 6;
                break;
            case 5:
                newXPosition = getX_MinRange();
                newYPosition = getY_MaxRangeForIntent() / 2;
                x2 = getX_MaxRangeForIntent();
                y2 = getY_MinRange();
                m_PreviousRandPosition = 2;
                break;
            case 6:
                newXPosition = getX_MaxRangeForIntent() / 2;
                newYPosition = getY_MinRange();
                x2 = getX_MaxRangeForIntent();
                y2 = getY_MaxRangeForIntent();
                m_PreviousRandPosition = 3;
                break;
            case 7:
                newXPosition = getX_MaxRangeForIntent();
                newYPosition = getY_MaxRangeForIntent() / 2;
                x2 = getX_MinRange();
                y2 = getY_MaxRangeForIntent();
                m_PreviousRandPosition = 4;
                break;
            case 8:
                newXPosition = getX_MaxRangeForIntent() / 2;
                newYPosition = getY_MaxRangeForIntent();
                x2 = getX_MinRange();
                y2 = getY_MinRange();
                m_PreviousRandPosition = 1;
                break;
        }

        m_CurrentIntentLocation.set(newXPosition, newYPosition);
        m_MindShooter.animateIntentForLocation(m_CurrentIntentLocation, 250);
        m_CurrentIntentLocation.set(x2, y2);
        m_MindShooter.animateIntentForLocation(m_CurrentIntentLocation, 250);

        if (--doItAgain > 0){
            goFarAwayFromTarget(attFranction, doItAgain);
        }
    }

    private void goToTheTarget(float attFranction) {
        m_CurrentIntentLocation.set(m_CurrentBalloonLocation.x  + m_BalloonSize.x/3 - m_SpaceVal,
                m_CurrentBalloonLocation.y +  m_BalloonSize.y/3 - m_SpaceVal);
        m_MindShooter.animateIntentForLocation(m_CurrentIntentLocation, 1000);
    }

    private void newPositionForIntent(int level){
        m_CurrentIntentLocation.set(newXPositionForIntent(level), newYPositionForIntent(level));
        m_MindShooter.animateIntentForLocation(m_CurrentIntentLocation, 1000);
    }

    private int newXPositionForIntent(int level){
        float val = calcValByLevel(level);

        int res = (int)(m_CurrentBalloonLocation.x - m_CurrentBalloonLocation.x*val /*+ m_SpaceVal + m_BalloonSize.x/2*/);

        if (m_CurrentBalloonLocation.x < m_CurrentIntentLocation.x)
            res = (int)(m_CurrentBalloonLocation.x + m_BalloonSize.x +
                    (screenWidth - (m_CurrentIntentLocation.x + m_BalloonSize.x))*val /*- m_SpaceVal - m_BalloonSize.x/2*/);

        return res;
    }

    private int newYPositionForIntent(int level){
        float val = calcValByLevel(level);
        int res;

        res = (int)(m_CurrentBalloonLocation.y - m_CurrentBalloonLocation.y*val);

        if (m_CurrentBalloonLocation.y < m_CurrentIntentLocation.y)
            res = (int)(m_CurrentBalloonLocation.y + m_BalloonSize.y +
                    (screenHeight - (m_CurrentIntentLocation.y + m_BalloonSize.y))*val);

        return res;
    }

    private float calcValByLevel(int level) {
        float val = 1;

        if (level == 1) val = 0.15f;
        else if (level == 2) val = 0.25f;
        else if (level == 3) val = 0.30f;
        else if (level == 4) val = 0.40f;
        else if (level == 5) val = 0.50f;
        else if (level == 6) val = 0.60f;
        else if (level == 7) val = 0.70f;
        else if (level == 8) val = 0.80f;

        return val;
    }

    public void shoot(){
        int XmiddelIntentLocation = m_CurrentIntentLocation.x + (m_IntentSize.x/2);
        int YmiddelIntentLocation = m_CurrentIntentLocation.y + (m_IntentSize.y/2);

        if ( XmiddelIntentLocation >= m_CurrentBalloonLocation.x + m_SpaceVal &&
                XmiddelIntentLocation <= m_CurrentBalloonLocation.x + m_BalloonSize.x - m_SpaceVal &&
                YmiddelIntentLocation >= m_CurrentBalloonLocation.y + m_SpaceVal &&
                YmiddelIntentLocation <= m_CurrentBalloonLocation.y + m_BalloonSize.y - m_SpaceVal)
        { // Good Shoot
            calculateNewLocationForBalloon();
            m_MindShooter.theBalloonExploded(m_CurrentBalloonLocation, ++m_CurrentScore);
        }
    }

    int count = -1;

    public void test(){
        count++;
        if (count == 0)
            newPositionForIntent(1);
        else if (count == 1)
            newPositionForIntent(2);
        else if (count == 2)
            newPositionForIntent(3);
        else if (count == 3)
            newPositionForIntent(4);
        else if (count == 4){
            goFarAwayFromTarget(0, 1);
            count = -1;
        }
    }

    @Override
    public void onMeditationReceived(int medValue) {}

    @Override
    public void onHeadSetChangedState(String headSetName, EConnectionState connectionState) {}

    @Override
    public void onPoorSignalReceived(ESignalVolume signalVolume) {}

    public int getX_MaxRangeForBalloon() { return screenWidth - m_BalloonSize.x - 2*getX_MinRange(); }

    public int getY_MaxRangeForBalloon() { return screenHeight - m_BalloonSize.y - 2*getY_MinRange(); }

    public int getX_MaxRangeForIntent() { return screenWidth - m_IntentSize.x - 2*getX_MinRange(); }

    public int getY_MaxRangeForIntent() { return screenHeight - m_IntentSize.y - 2*getY_MinRange(); }

    public int getX_MinRange() { return 80; }

    public int getY_MinRange() { return 80; }

    public void setBalloonSize(Point p){ this.m_BalloonSize.set(p.x, p.y); }

    public void setIntentSize(Point p){ this.m_IntentSize.set(p.x, p.y); }

    public int getScore() { return m_CurrentScore; }
}
