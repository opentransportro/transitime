/* (C)2023 */
package org.transitclock.quickstart.resource;

import org.transitclock.gui.ExceptionPanel;

public class QuickStartException extends Exception {

    /**
     * @author Brendan Egan
     */
    private static final long serialVersionUID = 1L;

    public QuickStartException(String message, Exception ex) {
        super(message, ex);
        ExceptionPanel exception = new ExceptionPanel(message, ex);
    }
}
