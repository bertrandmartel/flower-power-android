package fr.bmartel.android.bluetooth.parrot.history;

import java.util.ArrayList;
import java.util.Arrays;

import fr.bmartel.android.utils.ByteUtils;

/**
 * Historic management functions
 *
 * @author Bertrand Martel
 */
public class HistoricUtils {

    /**
     * parse first frame buffer to retrieve file length
     *
     * @param fullFrame
     * @return
     */
    public static int getFileLength(byte[] fullFrame)
    {
        if (fullFrame.length>6)
        {
            if (fullFrame[0]==0x00 && fullFrame[1]==0x00)
            {
                byte[] lengthBytes = Arrays.copyOfRange(fullFrame,2,6);
                int result = ((lengthBytes[3] & 0xFF) << 24) | ((lengthBytes[2]&0xFF) << 16) | ((lengthBytes[1]&0xFF) << 8) | ((lengthBytes[0]&0xFF) << 0);
                return result;
            }
            else
            {
                return -1;
            }
        }
        return -1;
    }

    /**
     * Will rearrange element of array list according to payload index
     *
     * @param historicFrameList
     * @return
     */
    public static ArrayList<IHistoricFrame> rearrangeIndex(ArrayList<IHistoricFrame> historicFrameList) {

        ArrayList<IHistoricFrame> newFrameList = new ArrayList<>();

        //index start from 1 because of first frame buffer
        int count = 0;

        while (newFrameList.size()!=historicFrameList.size())
        {

            for (int i =0; i <  historicFrameList.size();i++)
            {

                if (historicFrameList.get(i).getIndex()==count)
                {
                    newFrameList.add(historicFrameList.get(i));
                    break;
                }
            }
            count++;
        }

        return newFrameList;
    }

    public static boolean isAlreadyReceived(ArrayList<IHistoricFrame> tempHistoricFrameList, IHistoricFrame frame) {

        for (int i = 0; i  < tempHistoricFrameList.size();i++)
        {
            if (tempHistoricFrameList.get(i).getIndex()==frame.getIndex())
            {
                return true;
            }
        }
        return false;
    }
}
