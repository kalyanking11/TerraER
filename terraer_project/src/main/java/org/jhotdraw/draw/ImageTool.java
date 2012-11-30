/*
 * @(#)ImageTool.java  1.0  December 14, 2006
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


import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.jhotdraw.gui.Worker;
/**
 * A tool to create new figures that implement the ImageHolderFigure
 * interface, such as ImageFigure. The figure to be created is specified by a
 * prototype.
 * <p>
 * To create a figure using the ImageTool, the user does the following mouse
 * gestures on a DrawingView:
 * <ol>
 * <li>Press the mouse button over the DrawingView. This defines the
 * upper left point of the Figure bounds.</li>
 * </ol>
 * When the user has performed this mouse gestures, the ImageTool opens a
 * JFileChooser where the user can specify an image file to be loaded into the
 * figure. The width and height of the image is used to determine the lower right
 * corner of the Figure.
 * 
 * @author Werner Randelshofer
 * @version 1.0 December 14, 2006 Created.
 */
public class ImageTool extends CreationTool {
    JFileChooser fileChooser;
    
    
    
    /** Creates a new instance. */
    public ImageTool(ImageHolderFigure prototype) {
        super(prototype);
    }
    /** Creates a new instance. */
    public ImageTool(ImageHolderFigure prototype, Map attributes) {
        super(prototype, attributes);
    }
    
    public void creationFinished(final Figure createdFigure) {
        if (getFileChooser().showOpenDialog(getView().getComponent()) == JFileChooser.APPROVE_OPTION) {
            final File file = getFileChooser().getSelectedFile();
            new Worker() {
                public Object construct() {
                    try {
                        ((ImageHolderFigure) createdFigure).loadImage(file);
                    } catch (Throwable t) {
                        return t;
                    }
                    return null;
                }
               public void finished(Object value) {
                    if (value instanceof Throwable) {
                        Throwable t = (Throwable) value;
                        t.printStackTrace();
                        JOptionPane.showConfirmDialog(getView().getComponent(),
                                t.getMessage(),
                                null,
                                JOptionPane.ERROR_MESSAGE
                                );
                    }
                    BufferedImage img = ((ImageHolderFigure) createdFigure).getBufferedImage();
                    if (img != null) {
                        Point2D.Double p1 = createdFigure.getStartPoint();
                        Point2D.Double p2 = new Point2D.Double(p1.x+img.getWidth(), p1.y+img.getHeight());
                        createdFigure.willChange();
                        createdFigure.setBounds(p1,p2);
                        createdFigure.changed();
                    }
                    fireToolDone();
                }
            }.start();
        } else {
            fireToolDone();
        }
    }
    
    private JFileChooser getFileChooser() {
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
        }
        return fileChooser;
    }
}
