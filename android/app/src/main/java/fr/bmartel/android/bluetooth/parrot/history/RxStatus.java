package fr.bmartel.android.bluetooth.parrot.history;

/**
 * RX status field
 *
 * @author Bertrand Martel
 */
public class RxStatus {

    /**
     * states that can take rx status
     */
    public static enum states
    {
        NONE,STANDBY,RECEIVING,ACK,NACK,CANCEL,ERROR
    }

    /**
     * translate value received to enum
     *
     * @param rxByte
     * @return
     */
    public static states getRxState(byte rxByte) {
        switch (rxByte)
        {
            case 0:
                return states.STANDBY;
            case 1:
                return states.RECEIVING;
            case 2:
                return states.ACK;
            case 3:
                return states.NACK;
            case 4:
                return states.CANCEL;
            case 5:
                return states.ERROR;
        }
        return states.NONE;
    }

    /**
     * translate state into byte
     *
     * @param rxState
     * @return
     */
    public static byte getRxValue(states rxState)
    {
        if (rxState==states.STANDBY)
            return 0;
        else if  (rxState==states.RECEIVING)
            return 1;
        else if (rxState==states.ACK)
            return 2;
        else if (rxState==states.NACK)
            return 3;
        else if (rxState==states.CANCEL)
            return 4;
        else if (rxState==states.ERROR)
            return 5;
        else
            return -1;
    }
}
