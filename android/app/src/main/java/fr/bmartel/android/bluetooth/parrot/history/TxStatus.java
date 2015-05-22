package fr.bmartel.android.bluetooth.parrot.history;

/**
 * TX status field
 *
 * @author Bertrand Martel
 */
public class TxStatus {

    /**
     * states that can take tx status
     */
    public static enum states
    {
        NONE,IDLE,TRANSFERRING,WAITING_ACK
    }

    /**
     * translate value received to enum
     *
     * @param rxByte
     * @return
     */
    public static states getTxState(byte rxByte)
    {
        switch (rxByte)
        {
            case 0:
                return states.IDLE;
            case 1:
                return states.TRANSFERRING;
            case 2:
                return states.WAITING_ACK;
        }
        return states.NONE;
    }

    /**
     * translate state into byte
     *
     * @param rxState
     * @return
     */
    public static byte getTxValue(states rxState)
    {
        if (rxState==states.IDLE)
            return 0;
        else if  (rxState==states.TRANSFERRING)
            return 1;
        else if (rxState==states.WAITING_ACK)
            return 2;
        else
            return -1;
    }


}
