/*
 * @(#)DefaultApplicationModel.java  1.1  2007-01-11
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

package org.jhotdraw.application;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;

import org.jhotdraw.application.action.CopyAction;
import org.jhotdraw.application.action.CutAction;
import org.jhotdraw.application.action.DeleteAction;
import org.jhotdraw.application.action.DuplicateAction;
import org.jhotdraw.application.action.FindAction;
import org.jhotdraw.application.action.LoadAction;
import org.jhotdraw.application.action.NewAction;
import org.jhotdraw.application.action.OpenAction;
import org.jhotdraw.application.action.PasteAction;
import org.jhotdraw.application.action.RedoAction;
import org.jhotdraw.application.action.SaveAction;
import org.jhotdraw.application.action.SelectAllAction;
import org.jhotdraw.application.action.UndoAction;
import org.jhotdraw.beans.AbstractBean;
import org.jhotdraw.util.ResourceBundleUtil;
/**
 * DefaultApplicationModel.
 *
 * @author Werner Randelshofer.
 * @version 1.1 2007-01-11 Changed method createToolBars.
 * <br>1.0 June 10, 2006 Created.
 */
public class DefaultApplicationModel
        extends AbstractBean
        implements ApplicationModel {
    
    private HashMap<String,Action> actions;
    private String name;
    private String version;
    private String copyright;
    private Class projectClass;
    private String projectClassName;
    
    
    /** Creates a new instance. */
    public DefaultApplicationModel() {
    }
    
    public void setName(String newValue) {
        String oldValue = name;
        name = newValue;
        firePropertyChange("name", oldValue, newValue);
    }
    
    public String getName() {
        return name;
    }
    
    public void setVersion(String newValue) {
        String oldValue = version;
        version = newValue;
        firePropertyChange("version", oldValue, newValue);
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setCopyright(String newValue) {
        String oldValue = copyright;
        copyright = newValue;
        firePropertyChange("copyright", oldValue, newValue);
    }
    
    public String getCopyright() {
        return copyright;
    }
    /**
     * Use this method for best application startup performance.
     */
    public void setProjectClassName(String newValue) {
        String oldValue = projectClassName;
        projectClassName = newValue;
        firePropertyChange("projectClassName", oldValue, newValue);
    }
    
    /**
     * Use this method only, if setProjectClassName() does not suit you.
     */
    public void setProjectClass(Class newValue) {
        Class oldValue = projectClass;
        projectClass = newValue;
        firePropertyChange("projectClass", oldValue, newValue);
    }
    
    public Class getProjectClass() {
        if (projectClass == null) {
            if (projectClassName != null) {
                try {
                    projectClass = Class.forName(projectClassName);
                } catch (Exception e) {
                    InternalError error = new InternalError("unable to get documentView class");
                    error.initCause(e);
                    throw error;
                }
            }
        }
        return projectClass;
    }
    
    public DocumentView createProject() {
        try {
            return (DocumentView) getProjectClass().newInstance();
        } catch (Exception e) {
            InternalError error = new InternalError("unable to create documentView");
            error.initCause(e);
            throw error;
        }
    }
    
    /**
     * Creates toolbars for the application.
     * This class creates a standard toolbar with the following buttons in it:
     * <ul>
     * <li>File New</li>
     * <li>File Open</li>
     * <li>File Save</li>
     * <li>Undo</li>
     * <li>Redo</li>
     * <li>Cut</li>
     * <li>Copy</li>
     * <li>Paste</li>
     * </ul>
     */
    public List<JToolBar> createToolBars(DocumentOrientedApplication application, DocumentView p) {
        ResourceBundleUtil labels = ResourceBundleUtil.getLAFBundle("org.jhotdraw.application.Labels");
        
        JToolBar tb = new JToolBar();
        tb.setName(labels.getString("standardToolBarTitle"));
        
        JButton b;
        Action a;
        if (null != (a = getAction(NewAction.ID))) {
            b = tb.add(a);
            b.setFocusable(false);
        }
        if (null != (a = getAction(OpenAction.ID))) {
            b = tb.add(a);
            b.setFocusable(false);
        }
        if (null != (a = getAction(LoadAction.ID))) {
            b = tb.add(a);
            b.setFocusable(false);
        }
        b = tb.add(getAction(SaveAction.ID));
        tb.addSeparator();
        b = tb.add(getAction(UndoAction.ID));
        b.setFocusable(false);
        b = tb.add(getAction(RedoAction.ID));
        b.setFocusable(false);
        tb.addSeparator();
        b = tb.add(getAction(CutAction.ID));
        b.setFocusable(false);
        b = tb.add(getAction(CopyAction.ID));
        b.setFocusable(false);
        b = tb.add(getAction(PasteAction.ID));
        b.setFocusable(false);
        
        
        LinkedList<JToolBar> list = new LinkedList<JToolBar>();
        list.add(tb);
        return list;
    }
    public List<JMenu> createMenus(DocumentOrientedApplication a, DocumentView p) {
        LinkedList<JMenu> list = new LinkedList<JMenu>();
        list.add(createEditMenu(a, p));
        return list;
    }
    protected JMenu createEditMenu(DocumentOrientedApplication a, DocumentView p) {
        ResourceBundleUtil labels = ResourceBundleUtil.getLAFBundle("org.jhotdraw.application.Labels");
        
        JMenu m;
        JMenuItem mi;
        
        m = new JMenu();
        labels.configureMenu(m, "edit");
        mi = m.add(getAction(UndoAction.ID));
        mi.setIcon(null);
        mi = m.add(getAction(RedoAction.ID));
        mi.setIcon(null);
        m.addSeparator();
        mi = m.add(getAction(CutAction.ID));
        mi.setIcon(null);
        mi = m.add(getAction(CopyAction.ID));
        mi.setIcon(null);
        mi = m.add(getAction(PasteAction.ID));
        mi.setIcon(null);
        mi = m.add(getAction(DuplicateAction.ID));
        mi.setIcon(null);
        mi = m.add(getAction(DeleteAction.ID));
        mi.setIcon(null);
        m.addSeparator();
        mi = m.add(getAction(SelectAllAction.ID));
        mi.setIcon(null);
        if (getAction(FindAction.ID) != null) {
            m.addSeparator();
            m.add(getAction(FindAction.ID));
        }
        return m;
    }
    
    public void initProject(DocumentOrientedApplication a, DocumentView p) {
    }
    
    public void initApplication(DocumentOrientedApplication a) {
    }
    /**
     * Returns the action with the specified id.
     */
    public Action getAction(String id) {
        return (actions == null) ? null : (Action) actions.get(id);
    }
    
    /**
     * Puts an action with the specified id.
     */
    public void putAction(String id, Action action) {
        if (actions == null) {
            actions = new HashMap<String,Action>();
        }
        if (action == null) {
            actions.remove(id);
        } else {
            actions.put(id, action);
        }
    }
}
