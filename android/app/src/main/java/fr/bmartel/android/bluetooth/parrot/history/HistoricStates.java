package fr.bmartel.android.bluetooth.parrot.history;

/**
 *
 * Define all state defining historic retrieval state machine
 *
 * <ul>
 *     <li>STANDBY   : set to STANDBY just before you send RX upload service as RECEIVING ==> you will switch to next state only if you receive a TRANSMITTING on tx status upload service</li>
 *     <li>FIRST_TX_BUFFER_FRAME : set to FIRST_TX_BUFFER_FRAME only if you receive TRANSMITTING tx status on upload service </li>
 *     <li>RECEIVING : set to RECEIVING when first tx buffer frame has been received </li>
 * </ul>
 * @author  Bertrand Martel
 */
public enum HistoricStates {

    NONE,STANDBY,FIRST_TX_BUFFER_FRAME,RECEIVING

}
