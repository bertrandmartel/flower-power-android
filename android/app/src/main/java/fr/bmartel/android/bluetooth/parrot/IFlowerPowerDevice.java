package fr.bmartel.android.bluetooth.parrot;

/**
 * Generic interface for Flower power device
 */
public interface IFlowerPowerDevice {

    /**
     * add a listener for flower power device properties event related
     * @param listener
     */
    public void addListener(IFlowerPowerListener listener);

    /**
     * start procedure for retrieving history
     *
     * @return
     */
    public void startHistoryRetrievalProcedure();

    /**
     *
     * check if history procedure is already launch
     * @return
     */
    public boolean isRetrievingHistory();

    /**
     * switch flower power led state
     *
     * @param ledState
     */
    public void setLedOnOff(boolean ledState);

    /**
     * set live measurement period
     *
     * @param period
     */
    public void setMeasurementPeriod(int period);

    /**
     * set Flower power name charac
     * @param name
     */
    public void setFlowerPowerName(String name);

    /**
     * read Flower power name charac
     */
    public void readFlowerPowerName();

    /**
     * asynchronous call to trigger a read on battery characteristic
     */
    public void readBatteryState();

    /**
     * asynchronous call to trigger a read on flower power date characteristic
     */
    public void readFlowerPowerDate();

    /**
     * asynchronous call to trigger a read on flower power color characteristic
     */
    public void readFlowerPowerColor();

    /**
     * asynchronous call to trigger a read on led characteristic
     */
    public void readLedState();

    /**
     * asynchronous call to trigger a read on sunlight characteristic
     */
    public void readSunlight();

    /**
     * asynchronous call to trigger a read on soil ec characteristic
     */
    public void readSoilEc();

    /**
     * asynchronous call to trigger a read on soil temp characteristic
     */
    public void readSoilTemp();

    /**
     * asynchronous call to trigger a read on air temp characteristic
     */
    public void readAirTemp();

    /**
     * asynchronous call to trigger a read on soil WC characteristic
     */
    public void readSoilWc();

    public String getFlowerPowerName();

    public double getSunlight();

    public double getAirTemp();

    public double getSoilTemp();

    public double getSoilEC();

    public double getWc();

    public long getFlowerPowerDate();

    public int getBaterry();

    public FlowerPowerColor getColor();

    public int getMeasurementPeriod();
}

