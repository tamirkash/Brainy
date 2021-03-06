package EEG;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by kaganmoshe on 5/9/15.
 */
public abstract class EegHeadSet {
    // Final Members

    static public final String EEGHEADSET_STR = "EegHeadSet";
    static public final String MEDITATION_STR = "Meditation";
    static public final String ATTENTION_STR = "Attention";
    static public final String POOR_SIGNAL_STR = "Poor Signal";

    // Data Members
    protected List<IHeadSetData> m_HeadSetData = new LinkedList<>();
    private int m_CurrentPoorSignal = ESignalVolume.HEAD_SET_NOT_COVERED.value();
    protected EConnectionState m_CurrentState = EConnectionState.IDLE;
    protected boolean m_IsConnected = false;

    // Methods
    public abstract EConnectionState connect();

    public abstract void close();

    public abstract boolean IsConnected();

    public void registerListener(IHeadSetData headSetDate){
        m_HeadSetData.add(headSetDate);
    }

    public void unregisterListener(IHeadSetData headSetDate){
        if (m_HeadSetData.contains(headSetDate))
            m_HeadSetData.remove(headSetDate);
    }

    public void raiseOnAttention(int attValue){

        if (attValue < 80 && attValue > 35)
            attValue += 13;
        else if (attValue <= 35 && attValue >= 15)
            attValue -= 11;
        for (IHeadSetData headSetData : m_HeadSetData){
            if (headSetData != null){
                headSetData.onAttentionReceived(attValue);
            }
        }
    }

    public void raiseOnMeditation(int medValue){

        for (IHeadSetData headSetData : m_HeadSetData){
            if (headSetData != null){
                headSetData.onMeditationReceived(medValue);
            }
        }
    }

    public void raiseOnHeadSetChangedState(String headSetName, EConnectionState connectionState){

        m_CurrentState = connectionState;
        for (IHeadSetData headSetData : m_HeadSetData){
            if (headSetData != null){
                headSetData.onHeadSetChangedState(headSetName, connectionState);
            }
        }
    }

    public void raiseOnPoorSignal(int poorSignalValue){

        ESignalVolume signalVolume = ESignalVolume.getSignalVolume(poorSignalValue);

        if (signalVolume != ESignalVolume.GOOD_SIGNAL || m_CurrentPoorSignal != poorSignalValue) {
            for (IHeadSetData headSetData : m_HeadSetData) {
                if (headSetData != null) {
                    headSetData.onPoorSignalReceived(signalVolume);
                }
            }
        }

        m_CurrentPoorSignal = poorSignalValue;
    }

    public EConnectionState getConnctionState(){
        return m_CurrentState;
    }
}
