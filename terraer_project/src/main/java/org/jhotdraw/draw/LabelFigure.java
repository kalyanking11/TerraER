/*
 * @(#)LabelFigure.java  2.0  2006-01-14
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

package org.jhotdraw.draw;

import java.awt.geom.Point2D;
import java.util.HashMap;
/**
 * A LabelFigure can be used to provide more double clickable area for a
 * TextHolderFigure.
 *
 * FIXME - Move FigureListener into inner class.
 *
 * @author  Werner Randelshofer
 * @version 2.0 2006-01-14 Changed to support double precison coordinates.
 * <br>1.0 8. March 2004  Created.
 */
public class LabelFigure extends TextFigure implements FigureListener {
    private TextHolderFigure target;
    
    /** Creates a new instance. */
    public LabelFigure() {
        this("Label");
    }
    public LabelFigure(String text) {
        setText(text);
        setEditable(false);
    }
    
    public void setLabelFor(TextHolderFigure target) {
        if (this.target != null) {
            this.target.removeFigureListener(this);
        }
        this.target = target;
        if (this.target != null) {
            this.target.addFigureListener(this);
        }
    }
    public TextHolderFigure getLabelFor() {
        return (target == null) ? this : target;
    }
    
    /**
     * Returns a specialized tool for the given coordinate.
     * <p>Returns null, if no specialized tool is available.
     */
    public Tool getTool(Point2D.Double p) {
        return (target != null && contains(p)) ? new TextTool(target) : null;
    }
    
    
    public void figureAreaInvalidated(FigureEvent e) {
    }
    
    public void figureAttributeChanged(FigureEvent e) {
    }
    
    public void figureAdded(FigureEvent e) {
    }
    
    public void figureChanged(FigureEvent e) {
    }
    
    public void figureRemoved(FigureEvent e) {
        if (e.getFigure() == target) {
            target.removeFigureListener(this);
            target = null;
        }
    }
    
    public void figureRequestRemove(FigureEvent e) {
    }
    
    public void remap(HashMap oldToNew) {
        super.remap(oldToNew);
        if (target != null) {
            Figure newTarget = (Figure) oldToNew.get(target);
            if (newTarget != null) {
                target.removeFigureListener(this);
                target = (TextHolderFigure) newTarget;
                newTarget.addFigureListener(this);
            }
        }
    }

    public void figureHandlesChanged(FigureEvent e) {
    }
}
