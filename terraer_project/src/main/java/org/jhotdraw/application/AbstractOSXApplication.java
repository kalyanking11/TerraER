/*
 * @(#)AbstractOSXApplication.java  1.1  2007-01-11
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRootPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.jhotdraw.application.action.AboutAction;
import org.jhotdraw.application.action.Actions;
import org.jhotdraw.application.action.ClearRecentFilesAction;
import org.jhotdraw.application.action.CloseAction;
import org.jhotdraw.application.action.CopyAction;
import org.jhotdraw.application.action.CutAction;
import org.jhotdraw.application.action.DeleteAction;
import org.jhotdraw.application.action.DuplicateAction;
import org.jhotdraw.application.action.ExitAction;
import org.jhotdraw.application.action.ExportAction;
import org.jhotdraw.application.action.FocusAction;
import org.jhotdraw.application.action.MaximizeAction;
import org.jhotdraw.application.action.MinimizeAction;
import org.jhotdraw.application.action.NewAction;
import org.jhotdraw.application.action.OSXDropOnDockAction;
import org.jhotdraw.application.action.OSXTogglePaletteAction;
import org.jhotdraw.application.action.OpenAction;
import org.jhotdraw.application.action.PasteAction;
import org.jhotdraw.application.action.PrintAction;
import org.jhotdraw.application.action.RedoAction;
import org.jhotdraw.application.action.SaveAction;
import org.jhotdraw.application.action.SaveAsAction;
import org.jhotdraw.application.action.SelectAllAction;
import org.jhotdraw.application.action.UndoAction;
import org.jhotdraw.gui.Worker;
import org.jhotdraw.util.ResourceBundleUtil;
import org.jhotdraw.util.prefs.PreferencesUtil;

import application.ResourceMap;
import ch.randelshofer.quaqua.QuaquaManager;
/**
 * A AbstractOSXApplication can handle the life cycle of multiple document windows each
 * being presented in a JFrame of its own.  The application provides all the
 * functionality needed to work with the document, such as a menu bar, tool bars
 * and palette windows.
 * <p>
 * OSX stands for Mac OS X Application Document Interface. An OSX application can handle
 * multiple DocumentView's at the same time. Each documentView gets a JFrame of its own.
 * An OSX application has one menu bar, attached to the top of the screen.
 * This 'screen menu bar' is shared by all DocumentView's.
 * <p>
 * AbstractOSXApplication is designed for Mac OS X. It will not work on other
 * platforms.
 * <p>
 * The screen menu bar has the following standard menus:
 * <pre>
 * "Application-Name" File Edit Window Help
 * </pre>
 * The first menu, is the <b>application menu</b>. It has the following standard menu
 * items. AbstractOSXApplication wires the menu items to the action objects
 * specified in brackets. The preferences menu item is only displayed,
 * if the application has an action with PreferencesAction.ID. The other menu
 * items are always displayed. Menu items without action wiring are generated by
 * Mac OS X and can not be changed.
 * <pre>
 *  About "Application-Name" (AboutAction.ID)
 *  -
 *  Preferences... (PreferencesAction.ID)
 *  -
 *  Services
 *  -
 *  Hide "Application-Name"
 *  Hide Others
 *  Show All
 *  -
 *  Quit "Application-Name" (ExitAction.ID)
 * </pre>
 *
 * The <b>file menu</b> has the following standard menu items.
 * AbstractOSXApplication wires the menu items to the action objects
 * specified in brackets. If the application hasn't an action with the
 * specified ID, the menu item is not displayed. Menu items without action
 * wiring are generated by this class, and can be changed by subclasses.
 * <pre>
 *  New (NewAction.ID)
 *  Open... (OpenAction.ID)
 *  Open Recent >
 *  -
 *  Close (CloseAction.ID)
 *  Save (SaveAction.ID)
 *  Save As... (SaveAsAction.ID)
 *  Save All
 *  Revert to Saved (RevertToSavedAction.ID)
 *  -
 *  Page Setup... (PrintPageSetupAction.ID)
 *  Print... (PrintAction.ID)
 * </pre>
 *
 * The <b>edit menu</b> has the following standard menu items.
 * AbstractOSXApplication wires the menu items to the action objects
 * specified in brackets. If the application hasn't an action with the
 * specified ID, the menu item is not displayed. Menu items without action
 * wiring are generated by this class, and can be changed by subclasses.
 * <pre>
 *  Undo (UndoAction.ID)
 *  Redo (RedoAction.ID)
 *  -
 *  Cut (CutAction.ID)
 *  Copy (CopyAction.ID)
 *  Paste (PasteAction.ID)
 *  Delete (DeleteAction.ID)
 *  Select All (SelectAllAction.ID)
 * </pre>
 *
 *
 * @author Werner Randelshofer
 * @version 1.1 2007-01-11 Removed method addStandardActionsTo.
 * <br>1.0.1 2007-01-02 Floating palettes disappear now if the application
 * looses the focus.
 * 1.0 October 4, 2005 Created.
 */
public abstract class AbstractOSXApplication extends AbstractDocumentOrientedApplication {
    private OSXPaletteHandler paletteHandler;
    private DocumentView currentView;
    private LinkedList<Action> paletteActions;
    
    /** Creates a new instance. */
    public AbstractOSXApplication() {
    }
    
    public void initialize(String[] args) {
        super.initialize(null);
        Preferences prefs;
        prefs = Preferences.userNodeForPackage(getClass());
        initLookAndFeel();
        paletteHandler = new OSXPaletteHandler(this);
        
        createActionMap();
        paletteActions = new LinkedList<Action>();
        initPalettes(paletteActions);
        initScreenMenuBar();
    }
    
    public static void initAWT(String[] args) {
        System.setProperty("apple.laf.useScreenMenuBar","true");
        System.setProperty("com.apple.macos.useScreenMenuBar","true");
    }
    
    protected void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel(QuaquaManager.getLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    protected ActionMap createActionMap() {
ActionMap m = new ActionMap();
        m.put(AboutAction.ID, new AboutAction());
        m.put(ExitAction.ID, new ExitAction());
        m.put(OSXDropOnDockAction.ID, new OSXDropOnDockAction());
        
        m.put(NewAction.ID, new NewAction());
        m.put(OpenAction.ID, new OpenAction());
        m.put(ClearRecentFilesAction.ID, new ClearRecentFilesAction());
        m.put(SaveAction.ID, new SaveAction());
        m.put(SaveAsAction.ID, new SaveAsAction());
        m.put(PrintAction.ID, new PrintAction());
        m.put(CloseAction.ID, new CloseAction());
        
        m.put(UndoAction.ID, new UndoAction());
        m.put(RedoAction.ID, new RedoAction());
        m.put(CutAction.ID, new CutAction());
        m.put(CopyAction.ID, new CopyAction());
        m.put(PasteAction.ID, new PasteAction());
        m.put(DeleteAction.ID, new DeleteAction());
        m.put(DuplicateAction.ID, new DuplicateAction());
        m.put(SelectAllAction.ID, new SelectAllAction());
        
        m.put(MaximizeAction.ID, new MaximizeAction());
        m.put(MinimizeAction.ID, new MinimizeAction());
        
        return m;
    }
    
    protected void initView(DocumentView v) {
        super.initView(v);
        v.putAction(FocusAction.ID, new FocusAction(v));
    }
    
    
    public void addPalette(Window palette) {
        paletteHandler.addPalette(palette);
    }
    
    public void removePalette(Window palette) {
        paletteHandler.removePalette(palette);
    }
    
    public void show(final DocumentView p) {
        ResourceMap labels = getResourceMap();
        updateName(p);
        final JFrame f = new JFrame();
        f.setTitle(labels.getString("internalFrameTitle",
                labels.getString("Application.title"),
                p.getName()));
        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        f.setPreferredSize(new Dimension(400,400));
        
        Preferences prefs = Preferences.userNodeForPackage(getClass());
        PreferencesUtil.installFramePrefsHandler(prefs, "documentView", f);
        Point loc = f.getLocation();
        boolean moved;
        do {
            moved = false;
            for (Iterator i=getViews().iterator(); i.hasNext(); ) {
                DocumentView aProject = (DocumentView) i.next();
                if (aProject != p &&
                        SwingUtilities.getWindowAncestor(aProject.getComponent()).
                        getLocation().equals(loc)) {
                    loc.x += 22;
                    loc.y += 22;
                    moved = true;
                    break;
                }
            }
        } while (moved);
        f.setLocation(loc);
        
        paletteHandler.add(f, p);
        
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent evt) {
                setCurrentView(p);
                getAction(CloseAction.ID).actionPerformed(
                        new ActionEvent(f, ActionEvent.ACTION_PERFORMED,
                        "windowClosing")
                        );
            }
        });
        
        p.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                String name = evt.getPropertyName();
                if (name.equals("hasUnsavedChanges")) {
                    f.getRootPane().putClientProperty("windowModified",new Boolean(p.isModified()));
                } else if (name.equals("file")) {
                    f.setTitle((p.getFile() == null) ? "Unnamed" : p.getFile().getName());
                }
            }
        });
        
        f.setJMenuBar(createMenuBar(p));
        
        f.getContentPane().add(p.getComponent());
        f.setVisible(true);
    }
    
    public void hide(DocumentView p) {
        JFrame f = (JFrame) SwingUtilities.getWindowAncestor(p.getComponent());
        f.setVisible(false);
        f.remove(p.getComponent());
        paletteHandler.remove(f, p);
        f.dispose();
    }
    
    /**
     * Creates a menu bar.
     *
     * @param p The documentView for which the menu bar is created. This may be
     * <code>null</code> if the menu bar is attached to an application
     * component, such as the screen menu bar or a floating palette window.
     */
    protected JMenuBar createMenuBar(DocumentView p) {
        JMenuBar mb = new JMenuBar();
        mb.add(createFileMenu(p));
        for (JMenu mm : createMenus(p)) {
            mb.add(mm);
        }
        mb.add(createWindowMenu(p));
        return mb;
    }
    
    protected JMenu createWindowMenu(final DocumentView p) {
        ResourceBundleUtil labels = ResourceBundleUtil.getLAFBundle("org.jhotdraw.application.Labels");
        
        JMenuBar mb = new JMenuBar();
        JMenu m;
        JMenuItem mi;
        
        m = new JMenu();
        final JMenu windowMenu = m;
        labels.configureMenu(m, "window");
        mi = m.add(getAction(MinimizeAction.ID));
        mi.setIcon(null);
        mi = m.add(getAction(MaximizeAction.ID));
        mi.setIcon(null);
        m.addSeparator();
        for (DocumentView pr : getViews()) {
            if (pr.getAction(FocusAction.ID) != null) {
                windowMenu.add(pr.getAction(FocusAction.ID));
            }
        }
        if (paletteActions.size() > 0) {
            m.addSeparator();
            for (Action a: paletteActions) {
                JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(a);
                Actions.configureJCheckBoxMenuItem(cbmi, a);
                cbmi.setIcon(null);
                m.add(cbmi);
            }
        }
        
        addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                String name = evt.getPropertyName();
                if (name == PROP_VIEW_COUNT || name == "paletteCount") {
                    if (p == null || getViews().contains(p)) {
                        JMenu m = windowMenu;
                        m.removeAll();
                        m.add(getAction(MinimizeAction.ID));
                        m.add(getAction(MaximizeAction.ID));
                        m.addSeparator();
                        for (Iterator i=getViews().iterator(); i.hasNext(); ) {
                            DocumentView pr = (DocumentView) i.next();
                            if (pr.getAction(FocusAction.ID) != null) {
                                m.add(pr.getAction(FocusAction.ID));
                            }
                        }
                        if (paletteActions.size() > 0) {
                            m.addSeparator();
                            for (Action a: paletteActions) {
                                JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(a);
                                Actions.configureJCheckBoxMenuItem(cbmi, a);
                                cbmi.setIcon(null);
                                m.add(cbmi);
                            }
                        }
                    } else {
                        removePropertyChangeListener(this);
                    }
                }
            }
        });
        
        return m;
    }
    
    @Override protected JMenu createFileMenu(DocumentView p) {
        ResourceBundleUtil labels = ResourceBundleUtil.getLAFBundle("org.jhotdraw.application.Labels");
        
        JMenu m;
        JMenuItem mi;
        final JMenu openRecentMenu;
        
        m = new JMenu();
        labels.configureMenu(m, "file");
        mi = m.add(getAction(NewAction.ID));
        mi.setIcon(null);
        mi = m.add(getAction(OpenAction.ID));
        mi.setIcon(null);
        openRecentMenu = new JMenu();
        labels.configureMenu(openRecentMenu, "openRecent");
        openRecentMenu.setIcon(null);
        openRecentMenu.add(getAction(ClearRecentFilesAction.ID));
        updateOpenRecentMenu(openRecentMenu);
        m.add(openRecentMenu);
        m.addSeparator();
        mi = m.add(getAction(CloseAction.ID));
        mi.setIcon(null);
        mi = m.add(getAction(SaveAction.ID));
        mi.setIcon(null);
        mi = m.add(getAction(SaveAsAction.ID));
        mi.setIcon(null);
        if (getAction(ExportAction.ID) != null) {
            mi = m.add(getAction(ExportAction.ID));
            mi.setIcon(null);
        }
        if (getAction(PrintAction.ID) != null) {
            m.addSeparator();
            mi = m.add(getAction(PrintAction.ID));
            mi.setIcon(null);
        }
        
        addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                String name = evt.getPropertyName();
                if (name == "recentFiles") {
                    updateOpenRecentMenu(openRecentMenu);
                }
            }
        });
        
        return m;
    }
    
    public DocumentView getCurrentView() {
        return currentView;
    }
    
    public void setCurrentView(DocumentView newValue) {
        DocumentView oldValue = currentView;
        currentView = newValue;
        
        firePropertyChange(PROP_CURRENT_VIEW, oldValue, newValue);
    }
    
    protected void initScreenMenuBar() {
        net.roydesign.app.Application mrjapp = net.roydesign.app.Application.getInstance();
        mrjapp.setFramelessJMenuBar(createMenuBar(null));
        paletteHandler.add(SwingUtilities.getWindowAncestor(mrjapp.getFramelessJMenuBar()), null);
        mrjapp.getAboutJMenuItem().setAction(getAction(AboutAction.ID));
        mrjapp.getQuitJMenuItem().setAction(getAction(ExitAction.ID));
        mrjapp.addOpenDocumentListener(getAction(OSXDropOnDockAction.ID));
    }
    protected void initPalettes(final LinkedList<Action> paletteActions) {
        SwingUtilities.invokeLater(new Worker() {
            public Object construct() {
                LinkedList<JFrame> palettes = new LinkedList<JFrame>();
                LinkedList<JToolBar> toolBars = new LinkedList<JToolBar>(createToolBars(null));
                Preferences prefs = Preferences.userNodeForPackage(getClass());
                
                int i=0;
                int x=0;
                for (JToolBar tb : toolBars) {
                    i++;
                    tb.setFloatable(false);
                    tb.setOrientation(JToolBar.VERTICAL);
                    tb.setFocusable(false);
                    
                    JFrame d = new JFrame();
                    d.setFocusable(false);
                    d.setResizable(false);
                    d.getContentPane().setLayout(new BorderLayout());
                    d.getContentPane().add(tb,BorderLayout.CENTER);
                    d.setAlwaysOnTop(true);
                    d.setUndecorated(true);
                    d.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
                    d.getRootPane().setFont(
                            new Font("Lucida Grande", Font.PLAIN, 11)
                            );
                    d.getRootPane().putClientProperty("Quaqua.RootPane.isVertical", Boolean.FALSE);
                    d.getRootPane().putClientProperty("Quaqua.RootPane.isPalette", Boolean.TRUE);
                    
                    d.setJMenuBar(createMenuBar(null));
                    
                    d.pack();
                    d.setFocusableWindowState(false);
                    PreferencesUtil.installPalettePrefsHandler(prefs, "toolbar."+i, d, x);
                    x += d.getWidth();
                    
                    paletteActions.add(new OSXTogglePaletteAction(d, tb.getName()));
                    palettes.add(d);
                }
                return palettes;
                
            }
            public void finished(Object result) {
                LinkedList<JFrame> palettes = (LinkedList<JFrame>) result;
                if (palettes != null) {
                    for (JFrame p : palettes) {
                        addPalette(p);
                    }
                    firePropertyChange("paletteCount", 0, palettes.size());
                }
            }
        });
    }
    
    public boolean isEditorShared() {
        return true;
    }
    
    public Component getComponent() {
        return null;
    }
}
