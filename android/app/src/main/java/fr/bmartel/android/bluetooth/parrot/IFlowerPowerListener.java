package fr.bmartel.android.bluetooth.parrot;

import java.util.ArrayList;

import fr.bmartel.android.bluetooth.parrot.history.IHistoricFrame;

/**
 * Generic interface listener for flower power device
 *
 * @author Bertrand Martel Bouygues Telecom on 04/03/15.
 */
public interface IFlowerPowerListener {

    /**
     * called when sunlight notification value is retrieved
     *
     * @param value
     */
    public void onSunLightChange(double value);

    /**
     * called when soil EC notification value is retrieved
     *
     * @param soilEC
     */
    public void onSoilEcChange(double soilEC);

    /**
     * called when soil temperature notification value is retrieved
     *
     * @param temp
     */
    public void onSoilTempChange(double temp);

    /**
     * called when air temperature notification value is retrieved
     *
     * @param temp
     */
    public void onAirTempChange(double temp);

    /**
     * called when soil WC notification value is retrieved
     *
     * @param wc
     */
    public void onSoilWcChange(double wc);

    /**
     * called when history data has been received successfully
     *
     * @param frameList
     */
    public void onFullHistoryDataReceived(ArrayList<IHistoricFrame> frameList);

    /**
     *
     * called when history progress has changed
     *
     * @param progressChange
     *      history retrieval procedure progress in %
     */
    public void onFullHistoryProgressChange(int progressChange);

    /**
     * called when battery value changed has been received
     *
     * @param batteryValuePercent
     *      batery value in %
     */
    public void onBatteryValueReceived(int batteryValuePercent);

    /**
     * clock change received
     *
     * @param date
     */
    public void onFlowerPowerClockReceived(long date);

    /**
     * called when led state change is detected
     *
     * @param ledState
     *      led state
     */
    public void onLedStateChange(boolean ledState);

    /**
     * last move change is detected
     *
     * @param date
     *      date of last move change
     */
    public void onLastMoveChange(long date);

    /**
     * called when flower power color is detected
     *
     * @param color
     *      color of flower power device
     */
    public void onFlowerPowerColorChange(FlowerPowerColor color);

    /**
     * called when flower power measurement period change is detected
     *
     * @param measurement_period
     */
    public void onMeasurementPeriod(int measurement_period);

    /**
     * called when flower power name change is detected
     * @param name
     */
    public void onFlowerPowerNameChange(String name);
}
