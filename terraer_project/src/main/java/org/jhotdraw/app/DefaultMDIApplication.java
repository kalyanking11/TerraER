/*
 * @(#)DefaultMDIApplication.java  1.0  June 5, 2006
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

package org.jhotdraw.app;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.jhotdraw.app.action.AboutAction;
import org.jhotdraw.app.action.Actions;
import org.jhotdraw.app.action.ArrangeAction;
import org.jhotdraw.app.action.ClearRecentFilesAction;
import org.jhotdraw.app.action.CloseAction;
import org.jhotdraw.app.action.CopyAction;
import org.jhotdraw.app.action.CutAction;
import org.jhotdraw.app.action.DeleteAction;
import org.jhotdraw.app.action.DuplicateAction;
import org.jhotdraw.app.action.ExitAction;
import org.jhotdraw.app.action.ExportAction;
import org.jhotdraw.app.action.FocusAction;
import org.jhotdraw.app.action.GenerateDDLAction;
import org.jhotdraw.app.action.NewAction;
import org.jhotdraw.app.action.OpenAction;
import org.jhotdraw.app.action.OpenRecentAction;
import org.jhotdraw.app.action.PasteAction;
import org.jhotdraw.app.action.PrintAction;
import org.jhotdraw.app.action.RedoAction;
import org.jhotdraw.app.action.SaveAction;
import org.jhotdraw.app.action.SaveAsAction;
import org.jhotdraw.app.action.SelectAllAction;
import org.jhotdraw.app.action.ToggleToolBarAction;
import org.jhotdraw.app.action.UndoAction;
import org.jhotdraw.app.action.ValidateModelAction;
import org.jhotdraw.gui.Arrangeable;
import org.jhotdraw.gui.MDIDesktopPane;
import org.jhotdraw.util.ResourceBundleUtil;
import org.jhotdraw.util.ReversedList;
import org.jhotdraw.util.prefs.PreferencesUtil;
/**
 * A DefaultMDIApplication can handle the life cycle of multiple document windows each
 * being presented in a JInternalFrame of its own.  A parent JFrame provides all
 * the functionality needed to work with documents, such as a menu bar, tool
 * bars and palette windows.
 *
 *
 * @author Werner Randelshofer.
 * @version 1.0 June 5, 2006 Created.
 */
public class DefaultMDIApplication extends AbstractApplication {
    private JFrame parentFrame;
    private JScrollPane scrollPane;
    private MDIDesktopPane desktopPane;
    private Preferences prefs;
    private Project currentProject;
    private LinkedList<Action> toolBarActions;
    
    /** Creates a new instance. */
    public DefaultMDIApplication() {
    }
    
    protected void initApplicationActions() {
        ApplicationModel mo = getModel();
        mo.putAction(AboutAction.ID, new AboutAction(this));
        mo.putAction(ExitAction.ID, new ExitAction(this));
        
        mo.putAction(NewAction.ID, new NewAction(this));
        mo.putAction(OpenAction.ID, new OpenAction(this));
        mo.putAction(ClearRecentFilesAction.ID, new ClearRecentFilesAction(this));
        mo.putAction(SaveAction.ID, new SaveAction(this));
        mo.putAction(SaveAsAction.ID, new SaveAsAction(this));
        mo.putAction(CloseAction.ID, new CloseAction(this));
        mo.putAction(PrintAction.ID, new PrintAction(this));
        mo.putAction(ValidateModelAction.ID, new ValidateModelAction(this));
        mo.putAction(GenerateDDLAction.ID, new GenerateDDLAction(this));
        
        mo.putAction(UndoAction.ID, new UndoAction(this));
        mo.putAction(RedoAction.ID, new RedoAction(this));
        mo.putAction(CutAction.ID, new CutAction());
        mo.putAction(CopyAction.ID, new CopyAction());
        mo.putAction(PasteAction.ID, new PasteAction());
        mo.putAction(DeleteAction.ID, new DeleteAction());
        mo.putAction(DuplicateAction.ID, new DuplicateAction());
        mo.putAction(SelectAllAction.ID, new SelectAllAction());
        /*
        mo.putAction(MaximizeAction.ID, new MaximizeAction(this));
        mo.putAction(MinimizeAction.ID, new MinimizeAction(this));
         */
        mo.putAction(ArrangeAction.VERTICAL_ID, new ArrangeAction(desktopPane, Arrangeable.Arrangement.VERTICAL));
        mo.putAction(ArrangeAction.HORIZONTAL_ID, new ArrangeAction(desktopPane, Arrangeable.Arrangement.HORIZONTAL));
        mo.putAction(ArrangeAction.CASCADE_ID, new ArrangeAction(desktopPane, Arrangeable.Arrangement.CASCADE));
    }
    protected void initProjectActions(Project p) {
        p.putAction(FocusAction.ID, new FocusAction(p));
    }
    public void launch(String[] args) {
        super.launch(args);
    }
    
    public void init() {
        initLookAndFeel();
        super.init();
        prefs = Preferences.userNodeForPackage((getModel() == null) ? getClass() : getModel().getClass());
        initLabels();
        
        parentFrame = new JFrame(getName());
        parentFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        desktopPane = new MDIDesktopPane();
        
        scrollPane = new JScrollPane();
        scrollPane.setViewportView(desktopPane);
        toolBarActions = new LinkedList<Action>();
        
        
        initApplicationActions();
        parentFrame.getContentPane().add(
                wrapDesktopPane(scrollPane, toolBarActions)
                );
        
        parentFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent evt) {
                getModel().getAction(ExitAction.ID).actionPerformed(
                        new ActionEvent(parentFrame, ActionEvent.ACTION_PERFORMED, "windowClosing")
                        );
            }
        });
        parentFrame.setJMenuBar(createMenuBar());
        
        PreferencesUtil.installFramePrefsHandler(prefs, "parentFrame", parentFrame);
        
        parentFrame.setVisible(true);
    }
    public void configure(String[] args) {
        System.setProperty("apple.laf.useScreenMenuBar","false");
        System.setProperty("com.apple.macos.useScreenMenuBar","false");
        System.setProperty("apple.awt.graphics.UseQuartz","false");
        System.setProperty("swing.aatext","true");
    }
    protected void initLookAndFeel() {
        try {
            String lafName;
            if (System.getProperty("os.name").toLowerCase().startsWith("mac os x")) {
                JFrame.setDefaultLookAndFeelDecorated(true);
                JDialog.setDefaultLookAndFeelDecorated(true);
                lafName = UIManager.getCrossPlatformLookAndFeelClassName();
            } else {
                lafName = UIManager.getSystemLookAndFeelClassName();
            }
            UIManager.setLookAndFeel(lafName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (UIManager.getString("OptionPane.css") == null) {
            UIManager.put("OptionPane.css", "");
        }
    }
    
    public void show(final Project p) {
        if (! p.isShowing()) {
            p.setShowing(true);
            File file = p.getFile();
            final JInternalFrame f = new JInternalFrame();
            String title;
            if (file == null) {
                title = labels.getString("unnamedFile");
            } else {
                title = file.getName();
            }
            f.setTitle(labels.getFormatted("internalFrameTitle", title, getName(), p.getMultipleOpenId()));
            f.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
            f.setClosable(true);
            f.setMaximizable(true);
            f.setResizable(true);
            f.setIconifiable(false);
            
            PreferencesUtil.installInternalFramePrefsHandler(prefs, "project", f, desktopPane);
            Point loc = f.getLocation();
            boolean moved;
            do {
                moved = false;
                for (Iterator i=projects().iterator(); i.hasNext(); ) {
                    Project aProject = (Project) i.next();
                    if (aProject != p && aProject.isShowing() &&
                            SwingUtilities.getRootPane(aProject.getComponent()).getParent().
                            getLocation().equals(loc)) {
                        loc.x += 22;
                        loc.y += 22;
                        moved = true;
                        break;
                    }
                }
            } while (moved);
            f.setLocation(loc);
            
            //paletteHandler.add(f, p);
            
            f.addInternalFrameListener(new InternalFrameAdapter() {
                @Override public void internalFrameClosing(final InternalFrameEvent evt) {
                    setCurrentProject(p);
                    getModel().getAction(CloseAction.ID).actionPerformed(
                            new ActionEvent(f, ActionEvent.ACTION_PERFORMED,
                            "windowClosing")
                            );
                }
            });
            
            p.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    String name = evt.getPropertyName();
                    if (name.equals("hasUnsavedChanges")) {
                        ((JInternalFrame) f.getRootPane().getParent()).putClientProperty("windowModified",new Boolean(p.hasUnsavedChanges()));
                    } else if (name.equals("file")) {
                        f.setTitle((p.getFile() == null) ? "Unnamed" : p.getFile().getName());
                    }
                }
            });
            
            f.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    String name = evt.getPropertyName();
                    if (name.equals("selected")) {
                        if (evt.getNewValue().equals(Boolean.TRUE)) {
                            setCurrentProject(p);
                        }
                    }
                }
            });
            
            //f.setJMenuBar(createMenuBar(p));
            
            f.getContentPane().add(p.getComponent());
            f.setVisible(true);
            desktopPane.add(f);
            f.toFront();
            try {
                f.setSelected(true);
            } catch (PropertyVetoException e) {
                // Don't care.
            }
            p.getComponent().requestFocusInWindow();
        }
    }
    
    public void hide(Project p) {
        if (p.isShowing()) {
            JInternalFrame f = (JInternalFrame) SwingUtilities.getRootPane(p.getComponent()).getParent();
            f.setVisible(false);
            f.remove(p.getComponent());
            desktopPane.remove(f);
            f.dispose();
        }
    }
    
    public Project getCurrentProject() {
        return currentProject;
    }
    
    public void setCurrentProject(Project newValue) {
        Project oldValue = currentProject;
        currentProject = newValue;
        firePropertyChange("currentProject", oldValue, newValue);
    }
    
    public boolean isSharingToolsAmongProjects() {
        return true;
    }
    
    public Component getComponent() {
        return parentFrame;
    }
    
    /**
     * Returns the wrapped desktop pane.
     */
    protected Component wrapDesktopPane(Component c, LinkedList<Action> toolBarActions) {
        if (getModel() != null) {
            int id=0;
            for (JToolBar tb : new ReversedList<JToolBar>(getModel().createToolBars(this, null))) {
                id++;
                JPanel panel = new JPanel(new BorderLayout());
                panel.add(tb, BorderLayout.NORTH);
                panel.add(c, BorderLayout.CENTER);
                c = panel;
                PreferencesUtil.installToolBarPrefsHandler(prefs, "toolbar."+id, tb);
                toolBarActions.addFirst(new ToggleToolBarAction(tb, tb.getName()));
            }
            /*
            JToolBar tb = new JToolBar();
            tb.setName(labels.getString("standardToolBarTitle"));
            addStandardActionsTo(tb);
            id++;
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(tb, BorderLayout.NORTH);
            panel.add(c, BorderLayout.CENTER);
            c = panel;
            PreferencesUtil.installToolBarPrefsHandler(prefs, "toolbar."+id, tb);
            toolBarActions.addFirst(new ToggleToolBarAction(tb, tb.getName()));
            panel.putClientProperty("toolBarActions", toolBarActions);
             */
        }
        return c;
    }
    /*
    protected void addStandardActionsTo(JToolBar tb) {
        JButton b;
        ApplicationModel mo = getModel();
     
        b = tb.add(mo.getAction(NewAction.ID));
        b.setFocusable(false);
        b = tb.add(mo.getAction(OpenAction.ID));
        b.setFocusable(false);
        b = tb.add(mo.getAction(SaveAction.ID));
        tb.addSeparator();
        b = tb.add(mo.getAction(UndoAction.ID));
        b.setFocusable(false);
        b = tb.add(mo.getAction(RedoAction.ID));
        b.setFocusable(false);
        tb.addSeparator();
        b = tb.add(mo.getAction(CutAction.ID));
        b.setFocusable(false);
        b = tb.add(mo.getAction(CopyAction.ID));
        b.setFocusable(false);
        b = tb.add(mo.getAction(PasteAction.ID));
        b.setFocusable(false);
    }*/
    /**
     * Creates a menu bar.
     */
    protected JMenuBar createMenuBar() {
        JMenuBar mb = new JMenuBar();
        mb.add(createFileMenu());
        for (JMenu mm : getModel().createMenus(this, null)) {
            mb.add(mm);
        }
        mb.add(createWindowMenu());
        mb.add(createHelpMenu());
        return mb;
    }
    protected JMenu createFileMenu() {
        ResourceBundleUtil labels = ResourceBundleUtil.getLAFBundle("org.jhotdraw.app.Labels");
        ApplicationModel mo = getModel();
        
        JMenuBar mb = new JMenuBar();
        JMenu m;
        JMenuItem mi;
        final JMenu openRecentMenu;
        
        m = new JMenu();
        labels.configureMenu(m, "file");
        m.add(mo.getAction(NewAction.ID));
        m.add(mo.getAction(OpenAction.ID));
        openRecentMenu = new JMenu();
        labels.configureMenu(openRecentMenu, "openRecent");
        openRecentMenu.add(mo.getAction(ClearRecentFilesAction.ID));
        updateOpenRecentMenu(openRecentMenu);
        m.add(openRecentMenu);
        m.addSeparator();
        m.add(mo.getAction(CloseAction.ID));
        m.add(mo.getAction(SaveAction.ID));
        m.add(mo.getAction(SaveAsAction.ID));
        if (mo.getAction(ExportAction.ID) != null) {
            mi = m.add(mo.getAction(ExportAction.ID));
        }
        if (mo.getAction(PrintAction.ID) != null) {
            m.addSeparator();
            m.add(mo.getAction(PrintAction.ID));
        }
        m.addSeparator();
        m.add(mo.getAction(ExitAction.ID));
        
        addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                String name = evt.getPropertyName();
                ApplicationModel mo = getModel();
                if (name == "recentFiles") {
                    updateOpenRecentMenu(openRecentMenu);
                }
            }
        });
        
        return m;
    }
    private void updateOpenRecentMenu(JMenu openRecentMenu) {
        if (openRecentMenu.getItemCount() > 0) {
            JMenuItem clearRecentFilesItem = (JMenuItem) openRecentMenu.getItem(
                    openRecentMenu.getItemCount() - 1
                    );
            openRecentMenu.removeAll();
            for (File f : recentFiles()) {
                openRecentMenu.add(new OpenRecentAction(DefaultMDIApplication.this, f));
            }
            if (recentFiles().size() > 0) {
                openRecentMenu.addSeparator();
            }
            openRecentMenu.add(clearRecentFilesItem);
        }
    }
    protected JMenu createWindowMenu() {
        ResourceBundleUtil labels = ResourceBundleUtil.getLAFBundle("org.jhotdraw.app.Labels");
        ApplicationModel mo = getModel();
        
        JMenu m;
        JMenuItem mi;
        
        m = new JMenu();
        final JMenu windowMenu = m;
        labels.configureMenu(m, "window");
        m.add(mo.getAction(ArrangeAction.CASCADE_ID));
        m.add(mo.getAction(ArrangeAction.VERTICAL_ID));
        m.add(mo.getAction(ArrangeAction.HORIZONTAL_ID));
        
        m.addSeparator();
        for (Project pr : projects()) {
            if (pr.getAction(FocusAction.ID) != null) {
                windowMenu.add(pr.getAction(FocusAction.ID));
            }
        }
        if (toolBarActions.size() > 0) {
            m.addSeparator();
            for (Action a: toolBarActions) {
                JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(a);
                Actions.configureJCheckBoxMenuItem(cbmi, a);
                
                m.add(cbmi);
            }
        }
        
        addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                String name = evt.getPropertyName();
                ApplicationModel mo = getModel();
                if (name == "projectCount") {
                    JMenu m = windowMenu;
                    m.removeAll();
                    
                    m.add(mo.getAction(ArrangeAction.CASCADE_ID));
                    m.add(mo.getAction(ArrangeAction.VERTICAL_ID));
                    m.add(mo.getAction(ArrangeAction.HORIZONTAL_ID));
                    
                    m.addSeparator();
                    for (Iterator i=projects().iterator(); i.hasNext(); ) {
                        Project pr = (Project) i.next();
                        if (pr.getAction(FocusAction.ID) != null) {
                            m.add(pr.getAction(FocusAction.ID));
                        }
                    }
                    if (toolBarActions.size() > 0) {
                        m.addSeparator();
                        for (Action a: toolBarActions) {
                            JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(a);
                            Actions.configureJCheckBoxMenuItem(cbmi, a);
                            m.add(cbmi);
                        }
                    }
                }
            }
        });
        
        return m;
    }
    protected JMenu createHelpMenu() {
        ResourceBundleUtil labels = ResourceBundleUtil.getLAFBundle("org.jhotdraw.app.Labels");
        ApplicationModel mo = getModel();
        
        JMenu m;
        JMenuItem mi;
        
        m = new JMenu();
        labels.configureMenu(m, labels.getString("help"));
        m.add(mo.getAction(AboutAction.ID));
        return m;
    }
}
