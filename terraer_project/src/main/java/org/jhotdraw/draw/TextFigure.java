/*
 * @(#)TextFigure.java  1.0.2  2007-05-02
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


package org.jhotdraw.draw;

import static org.jhotdraw.draw.AttributeKeys.FILL_COLOR;
import static org.jhotdraw.draw.AttributeKeys.FONT_SIZE;
import static org.jhotdraw.draw.AttributeKeys.FONT_UNDERLINE;
import static org.jhotdraw.draw.AttributeKeys.TEXT;
import static org.jhotdraw.draw.AttributeKeys.TEXT_COLOR;
import static org.jhotdraw.draw.AttributeKeys.STROKE_DASHES;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import org.jhotdraw.geom.Dimension2DDouble;
import org.jhotdraw.geom.Geom;
import org.jhotdraw.geom.Insets2D;
import org.jhotdraw.util.ResourceBundleUtil;
import org.jhotdraw.xml.DOMInput;
import org.jhotdraw.xml.DOMOutput;
/**
 * A text figure.
 * <p>
 * A DrawingEditor should provide the TextTool to create a TextFigure.
 *
 * @see TextTool
 *
 * @author Werner Randelshofer
 * @version 2.0.2 2007-05-02 Made all instance variables protected instead of
 * private. 
 * <br>2.0.1 2006-02-27 Draw UNDERLINE_LOW_ONE_PIXEL instead of UNDERLINE_ON.
 * <br>2.0 2006-01-14 Changed to support double precison coordinates.
 * <br>1.0 2003-12-01 Derived from JHotDraw 5.4b1.
 */
public class TextFigure extends AbstractAttributedDecoratedFigure
        implements TextHolderFigure {
    protected Point2D.Double origin = new Point2D.Double();
    protected boolean editable = true;
    
    
    // cache of the TextFigure's layout
    transient protected TextLayout textLayout;
    
    /** Creates a new instance. */
    public TextFigure() {
        this(ResourceBundleUtil.
                getLAFBundle("org.jhotdraw.draw.Labels").
                getString("TextFigure.defaultText")
        );
    }
    public TextFigure(String text) {
        setText(text);
    }
    
    // DRAWING
    protected void drawStroke(java.awt.Graphics2D g) {
    }
    protected void drawFill(java.awt.Graphics2D g) {
    }
    
    protected void drawText(java.awt.Graphics2D g) {
        if (getText() != null || isEditable()) {
            TextLayout layout = getTextLayout();
            layout.draw(g, (float) origin.x, (float) (origin.y + layout.getAscent()));
        }
    }
    
    // SHAPE AND BOUNDS
    public void transform(AffineTransform tx) {
        tx.transform(origin, origin);
    }
    public void setBounds(Point2D.Double anchor, Point2D.Double lead) {
        origin = new Point2D.Double(anchor.x, anchor.y);
    }
    
    
    public boolean figureContains(Point2D.Double p) {
        if (getBounds().contains(p)) {
            return true;
        }
        return false;
    }
    protected TextLayout getTextLayout() {
        if (textLayout == null) {
            String text = getText();
            if (text == null || text.length() == 0) {
                text = " ";
            }
            
            FontRenderContext frc = getFontRenderContext();
            HashMap<TextAttribute,Object> textAttributes = new HashMap<TextAttribute,Object>();
            textAttributes.put(TextAttribute.FONT, getFont());
            if (FONT_UNDERLINE.get(this)) {
                textAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_ONE_PIXEL);
            }
            if (STROKE_DASHES.get(this)!=null) {
                textAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_DASHED);
            }
            textLayout = new TextLayout(text, textAttributes, frc);
        }
        return textLayout;
    }
    public Rectangle2D.Double getBounds() {
        TextLayout layout = getTextLayout();
        Rectangle2D.Double r = new Rectangle2D.Double(origin.x, origin.y, layout.getAdvance(),
                layout.getAscent() + layout.getDescent());
        return r;
    }
    public Dimension2DDouble getPreferredSize() {
        Rectangle2D.Double b = getBounds();
        return new Dimension2DDouble(b.width, b.height);
    }
    /**
     * Gets the drawing area without taking the decorator into account.
     */
    protected Rectangle2D.Double getFigureDrawingArea() {
        if (getText() == null) {
            return getBounds();
        } else {
            TextLayout layout = getTextLayout();
            Rectangle2D.Double r = new Rectangle2D.Double(
                    origin.x, origin.y, layout.getAdvance(), layout.getAscent()
                    );
            Rectangle2D lBounds = layout.getBounds();
            if (! lBounds.isEmpty() && ! java.lang.Double.isNaN(lBounds.getX())) {
                r.add(new Rectangle2D.Double(
                        lBounds.getX()+origin.x,
                        (lBounds.getY()+origin.y+layout.getAscent()),
                        lBounds.getWidth(),
                        lBounds.getHeight()
                        ));
            }
            // grow by two pixels to take anti-aliasing into account
            Geom.grow(r, 2d, 2d);
            return r;
        }
    }
    public void restoreTransformTo(Object geometry) {
        Point2D.Double p = (Point2D.Double) geometry;
        origin.x = p.x;
        origin.y = p.y;
    }
    
    public Object getTransformRestoreData() {
        return origin.clone();
    }
    
    
    
    // ATTRIBUTES
    /**
     * Gets the text shown by the text figure.
     */
    public String getText() {
        return TEXT.get(this);
    }
    
    /**
     * Sets the text shown by the text figure.
     * This is a convenience method for calling willChange,
     * AttribuTEXT.basicSet, changed.
     */
    public void setText(String newText) {
        TEXT.set(this, newText);
    }
    
    public int getTextColumns() {
        return (getText() == null) ? 4 : Math.max(getText().length(), 4);
    }
    /**
     * Gets the number of characters used to expand tabs.
     */
    public int getTabSize() {
        return 8;
    }
    
    public TextHolderFigure getLabelFor() {
        return this;
    }
    
    public Insets2D.Double getInsets() {
        return new Insets2D.Double();
    }
    
    public Font getFont() {
        return AttributeKeys.getFont(this);
    }
    
    public Color getTextColor() {
        return TEXT_COLOR.get(this);
    }
    
    public Color getFillColor() {
        return FILL_COLOR.get(this);
    }
    
    public void setFontSize(float size) {
        FONT_SIZE.set(this, (double)size);
    }
    
    public float getFontSize() {
        return FONT_SIZE.get(this).floatValue();
    }
    
    
    // EDITING
    public boolean isEditable() {
        return editable;
    }
    public void setEditable(boolean b) {
        this.editable = b;
    }
    @Override public Collection<Handle> createHandles(int detailLevel) {
        LinkedList<Handle> handles = new LinkedList<Handle>();
        handles.add(new BoundsOutlineHandle(this));
        handles.add(new MoveHandle(this, RelativeLocator.northWest()));
        handles.add(new MoveHandle(this, RelativeLocator.northEast()));
        handles.add(new MoveHandle(this, RelativeLocator.southWest()));
        handles.add(new MoveHandle(this, RelativeLocator.southEast()));
        handles.add(new FontSizeHandle(this));
        return handles;
    }
    /**
     * Returns a specialized tool for the given coordinate.
     * <p>Returns null, if no specialized tool is available.
     */
    public Tool getTool(Point2D.Double p) {
        return (isEditable() && contains(p)) ? new TextTool(this) : null;
    }
    
    // CONNECTING
    // COMPOSITE FIGURES
    // CLONING
    // EVENT HANDLING
    public void invalidate() {
        super.invalidate();
        textLayout = null;
    }
    
    protected void validate() {
        super.validate();
        textLayout = null;
    }
    
    @Override
	public String getToolTipText(Double p) {
		return this.toString();
	}

    public void read(DOMInput in) throws IOException {
        setBounds(
                new Point2D.Double(in.getAttribute("x",0d), in.getAttribute("y",0d)),
                new Point2D.Double(0, 0)
                );
        readAttributes(in);
        readDecorator(in);
        invalidate();
    }
    
    
    public void write(DOMOutput out) throws IOException {
        Rectangle2D.Double b = getBounds();
        out.addAttribute("x",b.x);
        out.addAttribute("y",b.y);
        writeAttributes(out);
        writeDecorator(out);
    }
    public TextFigure clone() {
        TextFigure that = (TextFigure) super.clone();
        that.origin = (Point2D.Double) this.origin.clone();
        that.textLayout = null;
        return that;
    }

    public boolean isTextOverflow() {
        return false;
    }
    
    public String toString(){
    	return this.getText();
    }
}
