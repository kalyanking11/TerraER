/*
 * @(#)EditorColorChooserAction.java  2.0  2006-06-07
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

import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JColorChooser;

import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.FigureSelectionEvent;
import org.jhotdraw.undo.CompositeEdit;
/**
 * EditorColorChooserAction.
 * <p>
 * The behavior for choosing the initial color of the JColorChooser matches with
 * {@see EditorColorIcon }.
 *
 * @author Werner Randelshofer
 * @version 2.0 2006-06-07 Reworked.
 * <br>1.0 2004-03-02  Created.
 */
public class EditorColorChooserAction extends AbstractSelectedAction {
    protected AttributeKey<Color> key;
    private static JColorChooser colorChooser;
    private HashMap<AttributeKey,Object> fixedAttributes;
    
    /** Creates a new instance. */
    public EditorColorChooserAction(DrawingEditor editor, AttributeKey<Color> key) {
        this(editor, key, null, null);
    }
    /** Creates a new instance. */
    public EditorColorChooserAction(DrawingEditor editor, AttributeKey<Color> key, Icon icon) {
        this(editor, key, null, icon);
    }
    /** Creates a new instance. */
    public EditorColorChooserAction(DrawingEditor editor, AttributeKey<Color> key, String name) {
        this(editor, key, name, null);
    }
    public EditorColorChooserAction(DrawingEditor editor, final AttributeKey<Color> key, String name, Icon icon) {
        this(editor, key, name, icon, new HashMap<AttributeKey,Object>());
    }
    public EditorColorChooserAction(DrawingEditor editor, final AttributeKey<Color> key, String name, Icon icon,
            Map<AttributeKey,Object> fixedAttributes) {
        super(editor);
        this.key = key;
        putValue(AbstractAction.NAME, name);
        //putValue(AbstractAction.MNEMONIC_KEY, new Integer('V'));
        putValue(AbstractAction.SMALL_ICON, icon);
        setEnabled(true);
    }
    
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if (colorChooser == null) {
            colorChooser = new JColorChooser();
        }
        Color initialColor = getInitialColor();
        Color chosenColor = colorChooser.showDialog((Component) e.getSource(), labels.getString("drawColor"), initialColor);
        if (chosenColor != null) {
            changeAttribute(chosenColor);
        }
    }
    
    public void changeAttribute(Color value) {
        CompositeEdit edit = new CompositeEdit("attributes");
        fireUndoableEditHappened(edit);
        Drawing drawing = getDrawing();
        Iterator i = getView().getSelectedFigures().iterator();
        while (i.hasNext()) {
            Figure figure = (Figure) i.next();
            figure.willChange();
            key.basicSet(figure, value);
            for (Map.Entry<AttributeKey,Object> entry : fixedAttributes.entrySet()) {
                entry.getKey().basicSet(figure, entry.getValue());
            }
            figure.changed();
        }
        getEditor().setDefaultAttribute(key, value);
        fireUndoableEditHappened(edit);
    }
    public void selectionChanged(FigureSelectionEvent evt) {
        //setEnabled(getView().getSelectionCount() > 0);
    }
    
    protected Color getInitialColor() {
        Color initialColor = (Color) getEditor().getDefaultAttribute(key);
        if (initialColor == null) {
            initialColor = Color.red;
        }
        return initialColor;
    }
}
