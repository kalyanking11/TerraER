/*
 * @(#)SelectionColorIcon.java  2.1  2007-05-03
 *
 * Copyright (c) 1996-2007 by the original authors of JHotDraw
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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.net.URL;

import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
/**
 * SelectionColorIcon draws a shape with the specified color for the selected
 * figures in the current drawing view.
 * If now figures are selcted, the specified color is taken from the DrawingEditor.
 * <p>
 * The behavior for choosing the drawn color matches with
 * {@see SelectionColorChooserAction }.
 * 
 * @author Werner Randelshofer
 * @version 2.1 2007-05-03 Added parameters for setting the color rect.
 * <br>2.0 2006-06-07 Reworked.
 * <br>1.0 25. November 2003  Created.
 */
public class SelectionColorIcon extends javax.swing.ImageIcon {
    private DrawingEditor editor;
    private AttributeKey<Color> key;
    private Shape colorShape;
    
    /** Creates a new instance.
     * @param editor The drawing editor.
     * @param key The key of the default attribute
     * @param imageLocation the icon image
     * @param colorShape The shape to be drawn with the color of the default
     * attribute.
     */
    public SelectionColorIcon(
            DrawingEditor editor,
            AttributeKey<Color> key,
            URL imageLocation,
            Shape colorShape) {
        super(imageLocation);
        this.editor = editor;
        this.key = key;
        this.colorShape = colorShape;
    }
    public SelectionColorIcon(
            DrawingEditor editor,
            AttributeKey<Color> key,
            Image image,
            Shape colorShape) {
        super(image);
        this.editor = editor;
        this.key = key;
        this.colorShape = colorShape;
    }
    
    public void paintIcon(java.awt.Component c, java.awt.Graphics gr, int x, int y) {
        Graphics2D g = (Graphics2D) gr;
        super.paintIcon(c, g, x, y);
        Color color;
        DrawingView view = editor.getActiveView();
        if (view != null && view.getSelectedFigures().size() == 1) {
            color = key.get(view.getSelectedFigures().iterator().next());
        } else {
            color = key.get(editor.getDefaultAttributes());
        }
        if (color != null) {
            g.setColor(color);
            g.translate(x, y);
            g.fill(colorShape);
            g.translate(-x, -y);
        }
    }
}
