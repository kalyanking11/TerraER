/*
 * @(#)MinimizeAction.java  2.0  2006-05-05
 *
 * Copyright (c) 1996-2006 by the original authors of JHotDraw
 * and all its contributors ("JHotDraw.org")
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * JHotDraw.org ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance
 * with the terms of the license agreement you entered into with
 * JHotDraw.org.
 */

package org.jhotdraw.application.action;

import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
/**
 * Minimizes a Frame.
 *
 * @author  Werner Randelshofer
 * @version 2.0 2006-05-05 Reworked.
 * <br>1.0  2005-06-10 Created.
 */
public class MinimizeAction extends AbstractDocumentViewAction {
    public final static String ID = "View.minimize";

    /** Creates a new instance. */
    public MinimizeAction() {
        initActionProperties(ID);
    }
    
    private JFrame getFrame() {
        return (JFrame) SwingUtilities.getWindowAncestor(
                getCurrentView().getComponent()
                );
    }
    public void actionPerformed(ActionEvent evt) {
        JFrame frame = getFrame();
        if (frame != null) {
            frame.setExtendedState(frame.getExtendedState() ^ Frame.ICONIFIED);
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }
}
