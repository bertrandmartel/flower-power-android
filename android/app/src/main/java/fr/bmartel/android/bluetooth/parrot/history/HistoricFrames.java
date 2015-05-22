package fr.bmartel.android.bluetooth.parrot.history;

/**
 * Decode Historic Frames
 *
 * @author Bertrand Martel
 */
public class HistoricFrames implements  IHistoricFrame {

    private int index = -1;

    private byte[] payload = new byte[]{};

    /**
     * Decode historic frame
     *
     * @param fullFrames
     */
    public HistoricFrames(byte[] fullFrames)
    {
        if (fullFrames.length>2) {

            index =  (fullFrames[1]&0xFF) | ((fullFrames[0] & 0xFF) << 8);

            payload = new byte[18];

            for (int i = 2; i < fullFrames.length; i++) {
                payload[i - 2] = fullFrames[i];
            }

        }
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public byte[] getPayload() {
        return payload;
    }
}
