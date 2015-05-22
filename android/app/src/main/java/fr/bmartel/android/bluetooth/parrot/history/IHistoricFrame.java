package fr.bmartel.android.bluetooth.parrot.history;

/**
 * Generic template for historic frame
 *
 * @author Bertrand Martel
 */
public interface IHistoricFrame {

    /**
     * retrieve historic frame index
     *
     * @return
     */
    public int getIndex();

    /**
     * retrieve payload
     *
     * @return
     */
    public byte[] getPayload();

}
