/*
 * @(#)MoveAction.java  1.0  2004-03-17
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

package org.jhotdraw.draw.action;

import java.awt.geom.AffineTransform;

import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.TransformEdit;
import org.jhotdraw.undo.CompositeEdit;

/**
 * Moves the selected figures by one unit.
 *
 * @author  Werner Randelshofer
 * @version 1.0 17. March 2004  Created.
 */
public abstract class MoveAction extends AbstractSelectedAction {
    private int dx, dy;
    
    /** Creates a new instance. */
    public MoveAction(DrawingEditor editor, int dx, int dy) {
        super(editor);
        this.dx = dx;
        this.dy = dy;
    }
    
    public void actionPerformed(java.awt.event.ActionEvent e) {
        CompositeEdit edit;
        AffineTransform tx = new AffineTransform();
        tx.translate(dx, dy);
        for (Figure f : getView().getSelectedFigures()) {
            f.willChange();
            f.transform(tx);
            f.changed();
        }
        fireUndoableEditHappened(new TransformEdit(getView().getSelectedFigures(), tx));
        
    }
    
    public static class East extends MoveAction {
        public final static String ID = "moveEast";
        public East(DrawingEditor editor) {
            super(editor, 1, 0);
            labels.configureAction(this, ID);
        }
    }
    public static class West extends MoveAction {
        public final static String ID = "moveWest";
        public West(DrawingEditor editor) {
            super(editor, -1, 0);
            labels.configureAction(this, ID);
        }
    }
    public static class North extends MoveAction {
        public final static String ID = "moveNorth";
        public North(DrawingEditor editor) {
            super(editor, 0, -1);
            labels.configureAction(this, ID);
        }
    }
    public static class South extends MoveAction {
        public final static String ID = "moveSouth";
        public South(DrawingEditor editor) {
            super(editor, 0, 1);
            labels.configureAction(this, ID);
        }
    }
}
