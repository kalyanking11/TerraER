/*
 * @(#)RectangleFigure.java  2.3  2006-12-23
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

/**
 * RectangleFigure.
 *
 * @author Werner Randelshofer
 * @version 2.3 2006-12-23 Made rectangle protected. 
 * <br>2.2 2006-03-23 Take stroke size into account in method contains.
 * <br>2.1 2006-03-22 Method getFigureDrawBounds added.
 * <br>2.0 2006-01-14 Changed to support double precison coordinates.
 * <br>1.0 2003-12-01 Derived from JHotDraw 5.4b1.
 */
public class DisjuncaoFigure extends GroupFigure {

    private static int counter = 0;
    private String title; 

	public DisjuncaoFigure() {
		super();
		title="Disjun��o"+Integer.toString(counter++);
	}
	
	public DisjuncaoFigure init(){
		this.add(new CircleFigure());
    	this.add(new TextNegritoFigure("d"));
    	return this;
	}
	
    public AbstractCompositeFigure clone() {
    	return (new DisjuncaoFigure()).init();
    }
	
	public String toString(){
		return title;
	}
	
    
}