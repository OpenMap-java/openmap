package com.bbn.openmap.gui.time;

/**
 * Interface that allows TimePanel to reach back into application when user clicks certain buttons.
 */
public interface ITimeBoundsUserActionsListener {

    /**
     * Used with 'set bounds based on selection' button.
     * @param start New start time.
     * @param end New end time.
     */
    void setTimeBounds(long start, long end);

    /**
     * Immediately switch from historical mode to real-time mode (if necessary) and jump to current real time.
     */
    void jumpToRealTime();

    /**
     * Bring up the GUI that allows time bounds to be set manually.
     */
    void invokeDateSelectionGUI();
}
