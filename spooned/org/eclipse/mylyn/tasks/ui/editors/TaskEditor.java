/**
 * *****************************************************************************
 * Copyright (c) 2004, 2015 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Eric Booth - initial prototype
 * *****************************************************************************
 */
package org.eclipse.mylyn.tasks.ui.editors;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.commons.ui.CommonUiUtil;
import org.eclipse.mylyn.commons.ui.PlatformUiUtil;
import org.eclipse.mylyn.commons.ui.SelectionProviderAdapter;
import org.eclipse.mylyn.commons.workbench.BusyAnimator;
import org.eclipse.mylyn.commons.workbench.BusyAnimator.IBusyClient;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport;
import org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.ui.TaskEditorBloatMonitor;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.actions.OpenWithBrowserAction;
import org.eclipse.mylyn.internal.tasks.ui.actions.TaskEditorScheduleAction;
import org.eclipse.mylyn.internal.tasks.ui.actions.ToggleTaskActivationAction;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.mylyn.internal.tasks.ui.editors.Messages;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionContributor;
import org.eclipse.mylyn.internal.tasks.ui.util.TaskDragSourceListener;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.editor.SharedHeaderFormEditor;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.forms.widgets.BusyIndicator;
import org.eclipse.ui.internal.forms.widgets.FormHeading;
import org.eclipse.ui.internal.forms.widgets.TitleRegion;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.part.WorkbenchPart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.services.IDisposable;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
/**
 *
 * @author Mik Kersten
 * @author Rob Elves
 * @author Steffen Pingel
 * @author Thomas Ehrnhoefer
 * @since 2.0
 */
public class TaskEditor extends org.eclipse.ui.forms.editor.SharedHeaderFormEditor {
    /**
     *
     * @since 2.0
     */
    public static final java.lang.String ID_EDITOR = "org.eclipse.mylyn.tasks.ui.editors.task";// $NON-NLS-1$


    /**
     *
     * @since 3.2
     */
    public static final java.lang.String ID_TOOLBAR_HEADER = "org.eclipse.mylyn.tasks.ui.editors.task.toolbar.header";// $NON-NLS-1$


    private static final java.lang.String ID_LEFT_TOOLBAR_HEADER = "org.eclipse.mylyn.tasks.ui.editors.task.toolbar.header.left";// $NON-NLS-1$


    private static final int LEFT_TOOLBAR_HEADER_TOOLBAR_PADDING = 3;

    private org.eclipse.mylyn.internal.tasks.ui.actions.ToggleTaskActivationAction activateAction;

    @java.lang.Deprecated
    private final org.eclipse.ui.IEditorPart contentOutlineProvider = null;

    private org.eclipse.mylyn.commons.workbench.BusyAnimator editorBusyIndicator;

    private org.eclipse.jface.action.MenuManager menuManager;

    private org.eclipse.ui.forms.events.IHyperlinkListener messageHyperLinkListener;

    private org.eclipse.mylyn.tasks.core.ITask task;

    private org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput taskEditorInput;

    private org.eclipse.mylyn.internal.tasks.ui.util.TaskDragSourceListener titleDragSourceListener;

    private org.eclipse.swt.widgets.Composite editorParent;

    private org.eclipse.ui.menus.IMenuService menuService;

    private org.eclipse.jface.action.IToolBarManager toolBarManager;

    private org.eclipse.jface.action.ToolBarManager leftToolBarManager;

    private org.eclipse.swt.widgets.ToolBar leftToolBar;

    private org.eclipse.swt.graphics.Image headerImage;

    // private int initialLeftToolbarSize;
    private boolean noExtraPadding;

    // private boolean headerLabelInitialized;
    private org.eclipse.ui.internal.forms.widgets.BusyIndicator busyLabel;

    private org.eclipse.swt.custom.StyledText titleLabel;

    private org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport textSupport;

    private org.eclipse.mylyn.internal.tasks.ui.actions.TaskEditorScheduleAction scheduleAction;

    org.eclipse.mylyn.internal.tasks.ui.actions.OpenWithBrowserAction openWithBrowserAction;

    private boolean needsScheduling = true;

    private boolean needsContext = true;

    private static boolean toolBarFailureLogged;

    public TaskEditor() {
    }

    @java.lang.Override
    protected void createPages() {
        super.createPages();
        org.eclipse.mylyn.internal.tasks.ui.TaskEditorBloatMonitor.editorOpened(this);
    }

    @java.lang.Override
    protected org.eclipse.swt.widgets.Composite createPageContainer(org.eclipse.swt.widgets.Composite parent) {
        this.editorParent = parent;
        org.eclipse.swt.widgets.Composite composite = super.createPageContainer(parent);
        org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.initializeScrollbars(getHeaderForm().getForm());
        // create left tool bar that replaces form heading label
        try {
            org.eclipse.ui.internal.forms.widgets.FormHeading heading = ((org.eclipse.ui.internal.forms.widgets.FormHeading) (getHeaderForm().getForm().getForm().getHead()));
            java.lang.reflect.Field field = org.eclipse.ui.internal.forms.widgets.FormHeading.class.getDeclaredField("titleRegion");// $NON-NLS-1$

            field.setAccessible(true);
            org.eclipse.ui.internal.forms.widgets.TitleRegion titleRegion = ((org.eclipse.ui.internal.forms.widgets.TitleRegion) (field.get(heading)));
            leftToolBarManager = new org.eclipse.jface.action.ToolBarManager(org.eclipse.swt.SWT.FLAT);
            leftToolBar = leftToolBarManager.createControl(titleRegion);
            leftToolBar.addControlListener(new org.eclipse.swt.events.ControlAdapter() {
                private boolean ignoreResizeEvents;

                @java.lang.Override
                public void controlResized(org.eclipse.swt.events.ControlEvent e) {
                    if (ignoreResizeEvents) {
                        return;
                    }
                    ignoreResizeEvents = true;
                    try {
                        // the tool bar contents has changed, update state
                        updateHeaderImage();
                        updateHeaderLabel();
                    } finally {
                        ignoreResizeEvents = false;
                    }
                }
            });
            // titleLabel = new Label(titleRegion, SWT.NONE);
            // need a viewer for copy support
            org.eclipse.jface.text.TextViewer titleViewer = new org.eclipse.jface.text.TextViewer(titleRegion, org.eclipse.swt.SWT.READ_ONLY);
            // Eclipse 3.3 needs a document, otherwise an NPE is thrown
            titleViewer.setDocument(new org.eclipse.jface.text.Document());
            titleLabel = titleViewer.getTextWidget();
            titleLabel.setForeground(heading.getForeground());
            titleLabel.setFont(heading.getFont());
            // XXX work-around problem that causes field to maintain selection when unfocused
            titleLabel.addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
                @java.lang.Override
                public void focusLost(org.eclipse.swt.events.FocusEvent e) {
                    titleLabel.setSelection(0);
                }
            });
            titleRegion.addControlListener(new org.eclipse.swt.events.ControlAdapter() {
                @java.lang.Override
                public void controlResized(org.eclipse.swt.events.ControlEvent e) {
                    // do not create busyLabel to avoid recursion
                    updateSizeAndLocations();
                }
            });
            org.eclipse.ui.handlers.IHandlerService handlerService = ((org.eclipse.ui.handlers.IHandlerService) (getSite().getService(org.eclipse.ui.handlers.IHandlerService.class)));
            if (handlerService != null) {
                textSupport = new org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport(handlerService);
                textSupport.install(titleViewer, false);
            }
        } catch (java.lang.Exception e) {
            if (!org.eclipse.mylyn.tasks.ui.editors.TaskEditor.toolBarFailureLogged) {
                org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Failed to obtain busy label toolbar", e));// $NON-NLS-1$

            }
            if (titleLabel != null) {
                titleLabel.dispose();
                titleLabel = null;
            }
            if (leftToolBar != null) {
                leftToolBar.dispose();
                leftToolBar = null;
            }
            if (leftToolBarManager != null) {
                leftToolBarManager.dispose();
                leftToolBarManager = null;
            }
        }
        updateHeader();
        return composite;
    }

    private org.eclipse.ui.internal.forms.widgets.BusyIndicator getBusyLabel() {
        if (busyLabel != null) {
            return busyLabel;
        }
        try {
            org.eclipse.ui.internal.forms.widgets.FormHeading heading = ((org.eclipse.ui.internal.forms.widgets.FormHeading) (getHeaderForm().getForm().getForm().getHead()));
            // ensure that busy label exists
            heading.setBusy(true);
            heading.setBusy(false);
            java.lang.reflect.Field field = org.eclipse.ui.internal.forms.widgets.FormHeading.class.getDeclaredField("titleRegion");// $NON-NLS-1$

            field.setAccessible(true);
            org.eclipse.ui.internal.forms.widgets.TitleRegion titleRegion = ((org.eclipse.ui.internal.forms.widgets.TitleRegion) (field.get(heading)));
            for (org.eclipse.swt.widgets.Control child : titleRegion.getChildren()) {
                if (child instanceof org.eclipse.ui.internal.forms.widgets.BusyIndicator) {
                    busyLabel = ((org.eclipse.ui.internal.forms.widgets.BusyIndicator) (child));
                }
            }
            if (busyLabel == null) {
                return null;
            }
            busyLabel.addControlListener(new org.eclipse.swt.events.ControlAdapter() {
                @java.lang.Override
                public void controlMoved(org.eclipse.swt.events.ControlEvent e) {
                    updateSizeAndLocations();
                }
            });
            // the busy label may get disposed if it has no image
            busyLabel.addDisposeListener(new org.eclipse.swt.events.DisposeListener() {
                public void widgetDisposed(org.eclipse.swt.events.DisposeEvent e) {
                    busyLabel.setMenu(null);
                    busyLabel = null;
                }
            });
            if (leftToolBar != null) {
                leftToolBar.moveAbove(busyLabel);
            }
            if (titleLabel != null) {
                titleLabel.moveAbove(busyLabel);
            }
            updateSizeAndLocations();
            return busyLabel;
        } catch (java.lang.Exception e) {
            if (!org.eclipse.mylyn.tasks.ui.editors.TaskEditor.toolBarFailureLogged) {
                org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Failed to obtain busy label toolbar", e));// $NON-NLS-1$

            }
            busyLabel = null;
        }
        return busyLabel;
    }

    private void updateSizeAndLocations() {
        if ((busyLabel == null) || busyLabel.isDisposed()) {
            return;
        }
        org.eclipse.swt.graphics.Point leftToolBarSize = new org.eclipse.swt.graphics.Point(0, 0);
        if ((leftToolBar != null) && (!leftToolBar.isDisposed())) {
            // bottom align tool bar in title region
            leftToolBarSize = leftToolBar.getSize();
            int y = (leftToolBar.getParent().getSize().y - leftToolBarSize.y) - 2;
            if (!hasLeftToolBar()) {
                // hide tool bar to avoid overlaying busyLabel on windows
                leftToolBarSize.x = 0;
            }
            leftToolBar.setBounds(busyLabel.getLocation().x, y, leftToolBarSize.x, leftToolBarSize.y);
        }
        if ((titleLabel != null) && (!titleLabel.isDisposed())) {
            // center align title text in title region
            org.eclipse.swt.graphics.Point size = titleLabel.computeSize(org.eclipse.swt.SWT.DEFAULT, org.eclipse.swt.SWT.DEFAULT, true);
            int y = (titleLabel.getParent().getSize().y - size.y) / 2;
            titleLabel.setBounds((busyLabel.getLocation().x + org.eclipse.mylyn.tasks.ui.editors.TaskEditor.LEFT_TOOLBAR_HEADER_TOOLBAR_PADDING) + leftToolBarSize.x, y, size.x, size.y);
        }
    }

    org.eclipse.swt.widgets.Composite getEditorParent() {
        return editorParent;
    }

    @java.lang.Override
    protected void addPages() {
        initialize();
        // determine factories
        java.util.Set<java.lang.String> conflictingIds = new java.util.HashSet<java.lang.String>();
        java.util.ArrayList<org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory> pageFactories = new java.util.ArrayList<org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory>();
        for (org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory pageFactory : org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getTaskEditorPageFactories()) {
            if (pageFactory.canCreatePageFor(getTaskEditorInput()) && org.eclipse.mylyn.commons.workbench.WorkbenchUtil.allowUseOf(pageFactory)) {
                pageFactories.add(pageFactory);
                java.lang.String[] ids = pageFactory.getConflictingIds(getTaskEditorInput());
                if (ids != null) {
                    conflictingIds.addAll(java.util.Arrays.asList(ids));
                }
            }
        }
        for (java.util.Iterator<org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory> it = pageFactories.iterator(); it.hasNext();) {
            if (conflictingIds.contains(it.next().getId())) {
                it.remove();
            }
        }
        // sort by priority
        java.util.Collections.sort(pageFactories, new java.util.Comparator<org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory>() {
            public int compare(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory o1, org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory o2) {
                return o1.getPriority() - o2.getPriority();
            }
        });
        // create pages
        for (org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory factory : pageFactories) {
            try {
                org.eclipse.ui.forms.editor.IFormPage page = factory.createPage(this);
                int index = addPage(page);
                setPageImage(index, factory.getPageImage(this, page));
                setPageText(index, factory.getPageText(this, page));
                if (factory.getPriority() == AbstractTaskEditorPageFactory.PRIORITY_TASK) {
                    setActivePage(index);
                }
                if (page instanceof org.eclipse.jface.viewers.ISelectionProvider) {
                    ((org.eclipse.jface.viewers.ISelectionProvider) (page)).addSelectionChangedListener(getActionBarContributor());
                }
            } catch (java.lang.Exception e) {
                org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Could not create editor via factory: " + factory, e));// $NON-NLS-1$

            }
        }
        updateTitleImage();
        updateHeaderToolBar();
        installTitleDrag(getHeaderForm().getForm().getForm());
        // do this late to allow pages to replace the selection provider
        getEditorSite().registerContextMenu(menuManager, getEditorSite().getSelectionProvider(), true);
    }

    private void initialize() {
        editorBusyIndicator = new org.eclipse.mylyn.commons.workbench.BusyAnimator(new org.eclipse.mylyn.commons.workbench.BusyAnimator.IBusyClient() {
            public org.eclipse.swt.graphics.Image getImage() {
                return TaskEditor.this.getTitleImage();
            }

            public void setImage(org.eclipse.swt.graphics.Image image) {
                TaskEditor.this.setTitleImage(image);
            }
        });
        menuManager = new org.eclipse.jface.action.MenuManager();
        configureContextMenuManager(menuManager);
        org.eclipse.swt.widgets.Menu menu = menuManager.createContextMenu(getContainer());
        getContainer().setMenu(menu);
        // install context menu on form heading and title
        getHeaderForm().getForm().setMenu(menu);
        org.eclipse.swt.widgets.Composite head = getHeaderForm().getForm().getForm().getHead();
        if (head != null) {
            org.eclipse.mylyn.commons.ui.CommonUiUtil.setMenu(head, menu);
        }
    }

    /**
     *
     * @since 3.0
     */
    @java.lang.Deprecated
    public void configureContextMenuManager(org.eclipse.jface.action.MenuManager manager) {
        if (manager == null) {
            return;
        }
        org.eclipse.jface.action.IMenuListener listener = new org.eclipse.jface.action.IMenuListener() {
            public void menuAboutToShow(org.eclipse.jface.action.IMenuManager manager) {
                contextMenuAboutToShow(manager);
            }
        };
        manager.setRemoveAllWhenShown(true);
        manager.addMenuListener(listener);
    }

    @java.lang.Deprecated
    protected void contextMenuAboutToShow(org.eclipse.jface.action.IMenuManager manager) {
        org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionContributor contributor = getActionBarContributor();
        if (contributor != null) {
            contributor.contextMenuAboutToShow(manager);
        }
    }

    @java.lang.Override
    protected org.eclipse.ui.forms.widgets.FormToolkit createToolkit(org.eclipse.swt.widgets.Display display) {
        // create a toolkit that shares colors between editors.
        return new org.eclipse.ui.forms.widgets.FormToolkit(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getFormColors(display));
    }

    @java.lang.Override
    protected void createHeaderContents(org.eclipse.ui.forms.IManagedForm headerForm) {
        getToolkit().decorateFormHeading(headerForm.getForm().getForm());
    }

    @java.lang.Override
    public void dispose() {
        disposeScheduleAction();
        if (headerImage != null) {
            headerImage.dispose();
        }
        if (editorBusyIndicator != null) {
            editorBusyIndicator.stop();
        }
        if (activateAction != null) {
            activateAction.dispose();
        }
        if (menuService != null) {
            if (leftToolBarManager != null) {
                menuService.releaseContributions(leftToolBarManager);
            }
            if (toolBarManager instanceof org.eclipse.jface.action.ContributionManager) {
                menuService.releaseContributions(((org.eclipse.jface.action.ContributionManager) (toolBarManager)));
            }
        }
        if (textSupport != null) {
            textSupport.dispose();
        }
        if (messageHyperLinkListener instanceof org.eclipse.ui.services.IDisposable) {
            ((org.eclipse.ui.services.IDisposable) (messageHyperLinkListener)).dispose();
        }
        super.dispose();
    }

    @java.lang.Override
    public void doSave(org.eclipse.core.runtime.IProgressMonitor monitor) {
        for (org.eclipse.ui.forms.editor.IFormPage page : getPages()) {
            if (page.isDirty()) {
                page.doSave(monitor);
            }
        }
        editorDirtyStateChanged();
    }

    @java.lang.Override
    public void doSaveAs() {
        throw new java.lang.UnsupportedOperationException();
    }

    private org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionContributor getActionBarContributor() {
        return ((org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionContributor) (getEditorSite().getActionBarContributor()));
    }

    @java.lang.SuppressWarnings("rawtypes")
    @java.lang.Override
    public java.lang.Object getAdapter(java.lang.Class adapter) {
        if (contentOutlineProvider != null) {
            return contentOutlineProvider.getAdapter(adapter);
        } else if (org.eclipse.ui.views.contentoutline.IContentOutlinePage.class.equals(adapter)) {
            org.eclipse.ui.forms.editor.IFormPage[] pages = getPages();
            for (org.eclipse.ui.forms.editor.IFormPage page : pages) {
                java.lang.Object outlinePage = page.getAdapter(adapter);
                if (outlinePage != null) {
                    return outlinePage;
                }
            }
        }
        return super.getAdapter(adapter);
    }

    /**
     *
     * @since 3.0
     */
    public org.eclipse.swt.widgets.Menu getMenu() {
        return getContainer().getMenu();
    }

    org.eclipse.ui.forms.editor.IFormPage[] getPages() {
        java.util.List<org.eclipse.ui.forms.editor.IFormPage> formPages = new java.util.ArrayList<org.eclipse.ui.forms.editor.IFormPage>();
        if (pages != null) {
            for (int i = 0; i < pages.size(); i++) {
                java.lang.Object page = pages.get(i);
                if (page instanceof org.eclipse.ui.forms.editor.IFormPage) {
                    formPages.add(((org.eclipse.ui.forms.editor.IFormPage) (page)));
                }
            }
        }
        return formPages.toArray(new org.eclipse.ui.forms.editor.IFormPage[formPages.size()]);
    }

    @java.lang.Deprecated
    protected org.eclipse.ui.progress.IWorkbenchSiteProgressService getProgressService() {
        java.lang.Object siteService = getEditorSite().getAdapter(org.eclipse.ui.progress.IWorkbenchSiteProgressService.class);
        if (siteService != null) {
            return ((org.eclipse.ui.progress.IWorkbenchSiteProgressService) (siteService));
        }
        return null;
    }

    @java.lang.Deprecated
    public org.eclipse.jface.viewers.ISelection getSelection() {
        if ((getSite() != null) && (getSite().getSelectionProvider() != null)) {
            return getSite().getSelectionProvider().getSelection();
        } else {
            return org.eclipse.jface.viewers.StructuredSelection.EMPTY;
        }
    }

    public org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput getTaskEditorInput() {
        return taskEditorInput;
    }

    @java.lang.Deprecated
    public org.eclipse.ui.forms.widgets.Form getTopForm() {
        return this.getHeaderForm().getForm().getForm();
    }

    @java.lang.Override
    public void init(org.eclipse.ui.IEditorSite site, org.eclipse.ui.IEditorInput input) throws org.eclipse.ui.PartInitException {
        if (!(input instanceof org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput)) {
            throw new org.eclipse.ui.PartInitException(("Invalid editor input \"" + input.getClass()) + "\"");// $NON-NLS-1$ //$NON-NLS-2$

        }
        super.init(site, input);
        this.taskEditorInput = ((org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput) (input));
        this.task = taskEditorInput.getTask();
        // initialize selection
        site.getSelectionProvider().setSelection(new org.eclipse.jface.viewers.StructuredSelection(task));
        setPartName(input.getName());
        // activate context
        org.eclipse.ui.contexts.IContextService contextSupport = ((org.eclipse.ui.contexts.IContextService) (site.getService(org.eclipse.ui.contexts.IContextService.class)));
        if (contextSupport != null) {
            contextSupport.activateContext(org.eclipse.mylyn.tasks.ui.editors.TaskEditor.ID_EDITOR);
        }
    }

    private void installTitleDrag(org.eclipse.ui.forms.widgets.Form form) {
        /* && !hasLeftToolBar() */
        if (titleDragSourceListener == null) {
            org.eclipse.swt.dnd.Transfer[] transferTypes;
            if (null == task) {
                transferTypes = new org.eclipse.swt.dnd.Transfer[]{ org.eclipse.swt.dnd.TextTransfer.getInstance() };
            } else {
                transferTypes = new org.eclipse.swt.dnd.Transfer[]{ org.eclipse.jface.util.LocalSelectionTransfer.getTransfer(), org.eclipse.swt.dnd.TextTransfer.getInstance(), org.eclipse.swt.dnd.FileTransfer.getInstance() };
            }
            titleDragSourceListener = new org.eclipse.mylyn.internal.tasks.ui.util.TaskDragSourceListener(new org.eclipse.mylyn.commons.ui.SelectionProviderAdapter() {
                @java.lang.Override
                public org.eclipse.jface.viewers.ISelection getSelection() {
                    return new org.eclipse.jface.viewers.StructuredSelection(task);
                }
            });
            if (titleLabel != null) {
                org.eclipse.swt.dnd.DragSource source = new org.eclipse.swt.dnd.DragSource(titleLabel, org.eclipse.swt.dnd.DND.DROP_MOVE | org.eclipse.swt.dnd.DND.DROP_LINK);
                source.setTransfer(transferTypes);
                source.addDragListener(titleDragSourceListener);
            } else {
                form.addTitleDragSupport(org.eclipse.swt.dnd.DND.DROP_MOVE | org.eclipse.swt.dnd.DND.DROP_LINK, transferTypes, titleDragSourceListener);
            }
        }
    }

    @java.lang.Override
    public boolean isDirty() {
        for (org.eclipse.ui.forms.editor.IFormPage page : getPages()) {
            if (page.isDirty()) {
                return true;
            }
        }
        return false;
    }

    @java.lang.Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @java.lang.Deprecated
    public void markDirty() {
        firePropertyChange(org.eclipse.mylyn.tasks.ui.editors.PROP_DIRTY);
    }

    /**
     * Refresh editor pages with new contents.
     *
     * @since 3.0
     */
    public void refreshPages() {
        for (org.eclipse.ui.forms.editor.IFormPage page : getPages()) {
            if (page instanceof org.eclipse.mylyn.tasks.ui.editors.TaskFormPage) {
                if ((page.getManagedForm() != null) && (!page.getManagedForm().getForm().isDisposed())) {
                    ((org.eclipse.mylyn.tasks.ui.editors.TaskFormPage) (page)).refresh();
                }
            }
        }
    }

    @java.lang.Override
    public void setFocus() {
        org.eclipse.ui.forms.editor.IFormPage page = getActivePageInstance();
        if (page != null) {
            page.setFocus();
        } else {
            super.setFocus();
        }
    }

    @java.lang.Deprecated
    public void setFocusOfActivePage() {
        if (this.getActivePage() > (-1)) {
            org.eclipse.ui.forms.editor.IFormPage page = this.getPages()[this.getActivePage()];
            if (page != null) {
                page.setFocus();
            }
        }
    }

    public void setMessage(java.lang.String message, int type) {
        setMessage(message, type, null);
    }

    private boolean isHeaderFormDisposed() {
        return ((getHeaderForm() == null) || (getHeaderForm().getForm() == null)) || getHeaderForm().getForm().isDisposed();
    }

    /**
     *
     * @since 2.3
     */
    public void setMessage(java.lang.String message, int type, org.eclipse.ui.forms.events.IHyperlinkListener listener) {
        if (isHeaderFormDisposed()) {
            return;
        }
        try {
            // avoid flicker of the left header toolbar
            getHeaderForm().getForm().setRedraw(false);
            org.eclipse.ui.forms.widgets.Form form = getHeaderForm().getForm().getForm();
            if (message != null) {
                message = message.replace('\n', ' ');
            }
            form.setMessage(message, type, null);
            if (messageHyperLinkListener != null) {
                form.removeMessageHyperlinkListener(messageHyperLinkListener);
                if (messageHyperLinkListener instanceof org.eclipse.ui.services.IDisposable) {
                    ((org.eclipse.ui.services.IDisposable) (messageHyperLinkListener)).dispose();
                }
            }
            if (listener != null) {
                form.addMessageHyperlinkListener(listener);
            }
            messageHyperLinkListener = listener;
            // make sure the busyLabel image is large enough to accommodate the tool bar
            if (hasLeftToolBar()) {
                org.eclipse.ui.internal.forms.widgets.BusyIndicator busyLabel = getBusyLabel();
                if ((message != null) && (busyLabel != null)) {
                    setHeaderImage(busyLabel.getImage());
                } else {
                    setHeaderImage(null);
                }
            }
        } finally {
            getHeaderForm().getForm().setRedraw(true);
        }
    }

    private void setHeaderImage(final org.eclipse.swt.graphics.Image image) {
        org.eclipse.ui.internal.forms.widgets.BusyIndicator busyLabel = getBusyLabel();
        if (busyLabel == null) {
            return;
        }
        final org.eclipse.swt.graphics.Point size = leftToolBar.computeSize(org.eclipse.swt.SWT.DEFAULT, org.eclipse.swt.SWT.DEFAULT, true);
        org.eclipse.swt.graphics.Point titleSize = titleLabel.computeSize(org.eclipse.swt.SWT.DEFAULT, org.eclipse.swt.SWT.DEFAULT, true);
        size.x += titleSize.x + org.eclipse.mylyn.tasks.ui.editors.TaskEditor.LEFT_TOOLBAR_HEADER_TOOLBAR_PADDING;
        size.y = java.lang.Math.max(titleSize.y, size.y);
        // padding between toolbar and image, ensure image is at least one pixel wide to avoid SWT error
        final int padding = ((size.x > 0) && (!noExtraPadding)) ? 10 : 1;
        final org.eclipse.swt.graphics.Rectangle imageBounds = (image != null) ? image.getBounds() : new org.eclipse.swt.graphics.Rectangle(0, 0, 0, 0);
        int tempHeight = (image != null) ? java.lang.Math.max(size.y + 1, imageBounds.height) : size.y + 1;
        // avoid extra padding due to margin added by TitleRegion.VMARGIN
        final int height = (tempHeight > (imageBounds.height + 5)) ? tempHeight - 5 : tempHeight;
        org.eclipse.jface.resource.CompositeImageDescriptor descriptor = new org.eclipse.jface.resource.CompositeImageDescriptor() {
            @java.lang.Override
            protected void drawCompositeImage(int width, int height) {
                if (image != null) {
                    drawImage(image.getImageData(), size.x + padding, (height - image.getBounds().height) / 2);
                }
            }

            @java.lang.Override
            protected org.eclipse.swt.graphics.Point getSize() {
                return new org.eclipse.swt.graphics.Point((size.x + padding) + imageBounds.width, height);
            }
        };
        org.eclipse.swt.graphics.Image newHeaderImage = descriptor.createImage();
        // directly set on busyLabel since getHeaderForm().getForm().setImage() does not update
        // the image if a message is currently displayed
        busyLabel.setImage(newHeaderImage);
        if (headerImage != null) {
            headerImage.dispose();
        }
        headerImage = newHeaderImage;
        // avoid extra padding due to large title font
        // TODO reset font in case tool bar is empty
        // leftToolBar.getParent().setFont(JFaceResources.getDefaultFont());
        getHeaderForm().getForm().reflow(true);
    }

    /**
     *
     * @since 3.1
     */
    public java.lang.String getMessage() {
        if ((getHeaderForm() != null) && (getHeaderForm().getForm() != null)) {
            if (!getHeaderForm().getForm().isDisposed()) {
                org.eclipse.ui.forms.widgets.Form form = getHeaderForm().getForm().getForm();
                return form.getMessage();
            }
        }
        return null;
    }

    /**
     *
     * @since 3.0
     */
    public void setStatus(java.lang.String message, final java.lang.String title, final org.eclipse.core.runtime.IStatus status) {
        setMessage(message, org.eclipse.jface.dialogs.IMessageProvider.ERROR, new org.eclipse.ui.forms.events.HyperlinkAdapter() {
            @java.lang.Override
            public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent event) {
                org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.displayStatus(title, status);
            }
        });
    }

    /**
     * Sets whether or not this editor supports scheduling affordances.
     *
     * @since 3.18
     */
    public void setNeedsScheduling(boolean needsScheduling) {
        this.needsScheduling = needsScheduling;
    }

    /**
     *
     * @since 3.18
     * @return Returns true if this editor supports scheduling affordances, otherwise returns false. If this returns
    false, then no scheduling is available to the UI.
     */
    public boolean needsScheduling() {
        return this.needsScheduling;
    }

    /**
     * Sets whether or not this editor supports context affordances.
     *
     * @since 3.18
     */
    public void setNeedsContext(boolean needsContext) {
        this.needsContext = needsContext;
    }

    /**
     *
     * @since 3.18 Returns true if this editor supports displaying context information, otherwise returns false. If this
    returns false, no context information will be available to the UI.
     */
    public boolean needsContext() {
        return this.needsContext;
    }

    @java.lang.Override
    public void showBusy(boolean busy) {
        if (editorBusyIndicator != null) {
            if (busy) {
                if (org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.isAnimationsEnabled()) {
                    editorBusyIndicator.start();
                }
            } else {
                editorBusyIndicator.stop();
            }
        }
        if (!isHeaderFormDisposed()) {
            org.eclipse.ui.forms.widgets.Form form = getHeaderForm().getForm().getForm();
            if ((form != null) && (!form.isDisposed())) {
                // TODO consider only disabling certain actions
                org.eclipse.jface.action.IToolBarManager toolBarManager = form.getToolBarManager();
                if (toolBarManager instanceof org.eclipse.jface.action.ToolBarManager) {
                    org.eclipse.swt.widgets.ToolBar control = ((org.eclipse.jface.action.ToolBarManager) (toolBarManager)).getControl();
                    if (control != null) {
                        control.setEnabled(!busy);
                    }
                }
                if (leftToolBar != null) {
                    leftToolBar.setEnabled(!busy);
                }
                if (titleLabel != null) {
                    titleLabel.setEnabled(!busy);
                }
                org.eclipse.mylyn.commons.ui.CommonUiUtil.setEnabled(form.getBody(), !busy);
                for (org.eclipse.ui.forms.editor.IFormPage page : getPages()) {
                    if (page instanceof org.eclipse.ui.part.WorkbenchPart) {
                        org.eclipse.ui.part.WorkbenchPart part = ((org.eclipse.ui.part.WorkbenchPart) (page));
                        part.showBusy(busy);
                    }
                }
            }
        }
    }

    private void updateHeader() {
        org.eclipse.ui.IEditorInput input = getEditorInput();
        updateHeaderImage();
        updateHeaderLabel();
        setTitleToolTip(input.getToolTipText());
        setPartName(input.getName());
    }

    /**
     *
     * @since 3.0
     */
    public void updateHeaderToolBar() {
        if (isHeaderFormDisposed()) {
            return;
        }
        final org.eclipse.ui.forms.widgets.Form form = getHeaderForm().getForm().getForm();
        toolBarManager = form.getToolBarManager();
        toolBarManager.removeAll();
        // toolBarManager.update(true);
        org.eclipse.mylyn.tasks.core.TaskRepository outgoingNewRepository = org.eclipse.mylyn.tasks.ui.TasksUiUtil.getOutgoingNewTaskRepository(task);
        final org.eclipse.mylyn.tasks.core.TaskRepository taskRepository = (outgoingNewRepository != null) ? outgoingNewRepository : taskEditorInput.getTaskRepository();
        org.eclipse.jface.action.ControlContribution repositoryLabelControl = new org.eclipse.jface.action.ControlContribution(org.eclipse.mylyn.internal.tasks.ui.editors.Messages.AbstractTaskEditorPage_Title) {
            @java.lang.Override
            protected org.eclipse.swt.widgets.Control createControl(org.eclipse.swt.widgets.Composite parent) {
                org.eclipse.ui.forms.widgets.FormToolkit toolkit = getHeaderForm().getToolkit();
                org.eclipse.swt.widgets.Composite composite = toolkit.createComposite(parent);
                org.eclipse.swt.layout.RowLayout layout = new org.eclipse.swt.layout.RowLayout();
                if (org.eclipse.mylyn.commons.ui.PlatformUiUtil.hasNarrowToolBar()) {
                    layout.marginTop = 0;
                    layout.marginBottom = 0;
                    layout.center = true;
                }
                composite.setLayout(layout);
                composite.setBackground(null);
                java.lang.String label = taskRepository.getRepositoryLabel();
                if (label.indexOf("//") != (-1)) {
                    // $NON-NLS-1$
                    label = label.substring(taskRepository.getRepositoryUrl().indexOf("//") + 2);// $NON-NLS-1$

                }
                org.eclipse.ui.forms.widgets.ImageHyperlink link = new org.eclipse.ui.forms.widgets.ImageHyperlink(composite, org.eclipse.swt.SWT.NONE);
                link.setText(label);
                link.setFont(org.eclipse.jface.resource.JFaceResources.getBannerFont());
                link.setForeground(toolkit.getColors().getColor(org.eclipse.ui.forms.IFormColors.TITLE));
                link.setToolTipText(org.eclipse.mylyn.internal.tasks.ui.editors.Messages.TaskEditor_Edit_Task_Repository_ToolTip);
                link.addHyperlinkListener(new org.eclipse.ui.forms.events.HyperlinkAdapter() {
                    @java.lang.Override
                    public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                        org.eclipse.mylyn.tasks.ui.TasksUiUtil.openEditRepositoryWizard(taskRepository);
                    }
                });
                return composite;
            }
        };
        toolBarManager.add(repositoryLabelControl);
        toolBarManager.add(new org.eclipse.jface.action.GroupMarker("repository"));// $NON-NLS-1$

        toolBarManager.add(new org.eclipse.jface.action.GroupMarker("new"));// $NON-NLS-1$

        toolBarManager.add(new org.eclipse.jface.action.GroupMarker("open"));// $NON-NLS-1$

        toolBarManager.add(new org.eclipse.jface.action.GroupMarker(org.eclipse.ui.IWorkbenchActionConstants.MB_ADDITIONS));
        openWithBrowserAction = new org.eclipse.mylyn.internal.tasks.ui.actions.OpenWithBrowserAction();
        openWithBrowserAction.selectionChanged(new org.eclipse.jface.viewers.StructuredSelection(task));
        if (openWithBrowserAction.isEnabled()) {
            // ImageDescriptor overlay = TasksUiPlugin.getDefault().getOverlayIcon(taskRepository.getConnectorKind());
            // ImageDescriptor compositeDescriptor = new TaskListImageDescriptor(TasksUiImages.REPOSITORY_SMALL_TOP,
            // overlay, false, true);
            openWithBrowserAction.setImageDescriptor(org.eclipse.mylyn.commons.ui.CommonImages.WEB);
            // openWithBrowserAction.setImageDescriptor(CommonImages.BROWSER_OPEN_TASK);
            openWithBrowserAction.setToolTipText(org.eclipse.mylyn.internal.tasks.ui.editors.Messages.AbstractTaskEditorPage_Open_with_Web_Browser);
            toolBarManager.appendToGroup("open", openWithBrowserAction);// $NON-NLS-1$

        } else {
            openWithBrowserAction = null;
        }
        if (activateAction == null) {
            activateAction = new org.eclipse.mylyn.internal.tasks.ui.actions.ToggleTaskActivationAction(task) {
                @java.lang.Override
                public void run() {
                    org.eclipse.mylyn.internal.tasks.core.TaskList taskList = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList();
                    if (taskList.getTask(task.getRepositoryUrl(), task.getTaskId()) == null) {
                        setMessage(org.eclipse.mylyn.internal.tasks.ui.editors.Messages.TaskEditor_Task_added_to_the_Uncategorized_container, org.eclipse.jface.dialogs.IMessageProvider.INFORMATION);
                    }
                    super.run();
                }
            };
        }
        toolBarManager.add(new org.eclipse.jface.action.Separator("planning"));// $NON-NLS-1$

        disposeScheduleAction();
        if (needsScheduling()) {
            scheduleAction = new org.eclipse.mylyn.internal.tasks.ui.actions.TaskEditorScheduleAction(task);
            toolBarManager.add(scheduleAction);
        }
        toolBarManager.add(new org.eclipse.jface.action.GroupMarker("page"));// $NON-NLS-1$

        for (org.eclipse.ui.forms.editor.IFormPage page : getPages()) {
            if (page instanceof org.eclipse.mylyn.tasks.ui.editors.TaskFormPage) {
                org.eclipse.mylyn.tasks.ui.editors.TaskFormPage taskEditorPage = ((org.eclipse.mylyn.tasks.ui.editors.TaskFormPage) (page));
                taskEditorPage.fillToolBar(toolBarManager);
            }
        }
        toolBarManager.add(new org.eclipse.jface.action.Separator("activation"));// $NON-NLS-1$

        // ContributionItem spacer = new ContributionItem() {
        // @Override
        // public void fill(ToolBar toolbar, int index) {
        // ToolItem item = new ToolItem(toolbar, SWT.NONE);
        // int scaleHeight = 42;
        // if (PlatformUtil.needsCarbonToolBarFix()) {
        // scaleHeight = 32;
        // }
        // final Image image = new Image(toolbar.getDisplay(), CommonImages.getImage(CommonImages.BLANK)
        // .getImageData()
        // .scaledTo(1, scaleHeight));
        // item.setImage(image);
        // item.addDisposeListener(new DisposeListener() {
        // public void widgetDisposed(DisposeEvent e) {
        // image.dispose();
        // }
        // });
        // item.setWidth(5);
        // item.setEnabled(false);
        // }
        // };
        // toolBarManager.add(spacer);
        // for (IFormPage page : getPages()) {
        // if (page instanceof AbstractTaskEditorPage) {
        // AbstractTaskEditorPage taskEditorPage = (AbstractTaskEditorPage) page;
        // taskEditorPage.fillLeftHeaderToolBar(toolBarManager);
        // } else if (page instanceof TaskPlanningEditor) {
        // TaskPlanningEditor taskEditorPage = (TaskPlanningEditor) page;
        // taskEditorPage.fillLeftHeaderToolBar(toolBarManager);
        // }
        // }
        // add external contributions
        menuService = ((org.eclipse.ui.menus.IMenuService) (getSite().getService(org.eclipse.ui.menus.IMenuService.class)));
        if ((menuService != null) && (toolBarManager instanceof org.eclipse.jface.action.ContributionManager)) {
            menuService.populateContributionManager(((org.eclipse.jface.action.ContributionManager) (toolBarManager)), (("toolbar:"// $NON-NLS-1$
             + org.eclipse.mylyn.tasks.ui.editors.TaskEditor.ID_TOOLBAR_HEADER) + ".") + taskRepository.getConnectorKind());// $NON-NLS-1$

            menuService.populateContributionManager(((org.eclipse.jface.action.ContributionManager) (toolBarManager)), "toolbar:"// $NON-NLS-1$
             + org.eclipse.mylyn.tasks.ui.editors.TaskEditor.ID_TOOLBAR_HEADER);
        }
        toolBarManager.update(true);
        // XXX move this call
        updateLeftHeaderToolBar();
        updateHeader();
    }

    private void disposeScheduleAction() {
        if (scheduleAction != null) {
            scheduleAction.dispose();
            scheduleAction = null;
        }
    }

    private void updateLeftHeaderToolBar() {
        leftToolBarManager.removeAll();
        leftToolBarManager.add(new org.eclipse.jface.action.Separator("activation"));// $NON-NLS-1$

        leftToolBarManager.add(new org.eclipse.jface.action.Separator(org.eclipse.ui.IWorkbenchActionConstants.MB_ADDITIONS));
        // initialLeftToolbarSize = leftToolBarManager.getSize();
        if (needsContext) {
            leftToolBarManager.add(activateAction);
        }
        // for (IFormPage page : getPages()) {
        // if (page instanceof AbstractTaskEditorPage) {
        // AbstractTaskEditorPage taskEditorPage = (AbstractTaskEditorPage) page;
        // taskEditorPage.fillLeftHeaderToolBar(leftToolBarManager);
        // } else if (page instanceof TaskPlanningEditor) {
        // TaskPlanningEditor taskEditorPage = (TaskPlanningEditor) page;
        // taskEditorPage.fillLeftHeaderToolBar(leftToolBarManager);
        // }
        // }
        // add external contributions
        menuService = ((org.eclipse.ui.menus.IMenuService) (getSite().getService(org.eclipse.ui.menus.IMenuService.class)));
        if ((menuService != null) && (leftToolBarManager instanceof org.eclipse.jface.action.ContributionManager)) {
            org.eclipse.mylyn.tasks.core.TaskRepository outgoingNewRepository = org.eclipse.mylyn.tasks.ui.TasksUiUtil.getOutgoingNewTaskRepository(task);
            org.eclipse.mylyn.tasks.core.TaskRepository taskRepository = (outgoingNewRepository != null) ? outgoingNewRepository : taskEditorInput.getTaskRepository();
            menuService.populateContributionManager(leftToolBarManager, (("toolbar:" + org.eclipse.mylyn.tasks.ui.editors.TaskEditor.ID_LEFT_TOOLBAR_HEADER) + ".")// $NON-NLS-1$ //$NON-NLS-2$
             + taskRepository.getConnectorKind());
        }
        leftToolBarManager.update(true);
        if (hasLeftToolBar()) {
            // XXX work around a bug in Gtk that causes the toolbar size to be incorrect if no
            // tool bar buttons are contributed
            // if (leftToolBar != null) {
            // Point size = leftToolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
            // boolean changed = false;
            // for (Control control : leftToolBar.getChildren()) {
            // final Point childSize = control.computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
            // if (childSize.y > size.y) {
            // size.y = childSize.y;
            // changed = true;
            // }
            // }
            // if (changed) {
            // leftToolBar.setSize(size);
            // }
            // }
            // 
            // if (PlatformUtil.isToolBarHeightBroken(leftToolBar)) {
            // ToolItem item = new ToolItem(leftToolBar, SWT.NONE);
            // item.setEnabled(false);
            // item.setImage(CommonImages.getImage(CommonImages.BLANK));
            // item.setWidth(1);
            // noExtraPadding = true;
            // } else if (PlatformUtil.needsToolItemToForceToolBarHeight()) {
            // ToolItem item = new ToolItem(leftToolBar, SWT.NONE);
            // item.setEnabled(false);
            // int scaleHeight = 22;
            // if (PlatformUtil.needsCarbonToolBarFix()) {
            // scaleHeight = 32;
            // }
            // final Image image = new Image(item.getDisplay(), CommonImages.getImage(CommonImages.BLANK)
            // .getImageData()
            // .scaledTo(1, scaleHeight));
            // item.setImage(image);
            // item.addDisposeListener(new DisposeListener() {
            // public void widgetDisposed(DisposeEvent e) {
            // image.dispose();
            // }
            // });
            // item.setWidth(1);
            // noExtraPadding = true;
            // }
            // fix size of toolbar on Gtk with Eclipse 3.3
            org.eclipse.swt.graphics.Point size = leftToolBar.getSize();
            if ((size.x == 0) && (size.y == 0)) {
                size = leftToolBar.computeSize(org.eclipse.swt.SWT.DEFAULT, org.eclipse.swt.SWT.DEFAULT, true);
                leftToolBar.setSize(size);
            }
        }
    }

    private void updateHeaderImage() {
        if (hasLeftToolBar()) {
            setHeaderImage(null);
        } else {
            getHeaderForm().getForm().setImage(getBrandingImage());
        }
    }

    private org.eclipse.swt.graphics.Image getBrandingImage() {
        java.lang.String connectorKind;
        org.eclipse.mylyn.tasks.core.TaskRepository outgoingNewRepository = org.eclipse.mylyn.tasks.ui.TasksUiUtil.getOutgoingNewTaskRepository(task);
        if (outgoingNewRepository != null) {
            connectorKind = outgoingNewRepository.getConnectorKind();
        } else {
            connectorKind = task.getConnectorKind();
        }
        if (org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector.CONNECTOR_KIND.equals(connectorKind)) {
            return org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.TASK);
        } else {
            org.eclipse.jface.resource.ImageDescriptor overlay;
            if (outgoingNewRepository != null) {
                overlay = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getBrandManager().getOverlayIcon(outgoingNewRepository);
            } else {
                overlay = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getBrandManager().getOverlayIcon(task);
            }
            org.eclipse.swt.graphics.Image image = org.eclipse.mylyn.commons.ui.CommonImages.getImageWithOverlay(org.eclipse.mylyn.tasks.ui.TasksUiImages.REPOSITORY, overlay, false, false);
            return image;
        }
    }

    private boolean hasLeftToolBar() {
        return (leftToolBar != null) && (leftToolBarManager != null);
        // && leftToolBarManager.getSize() > initialLeftToolbarSize;
    }

    private void updateHeaderLabel() {
        org.eclipse.mylyn.tasks.core.TaskRepository outgoingNewRepository = org.eclipse.mylyn.tasks.ui.TasksUiUtil.getOutgoingNewTaskRepository(task);
        final org.eclipse.mylyn.tasks.core.TaskRepository taskRepository = (outgoingNewRepository != null) ? outgoingNewRepository : taskEditorInput.getTaskRepository();
        // if (taskRepository.getConnectorKind().equals(LocalRepositoryConnector.CONNECTOR_KIND)) {
        // getHeaderForm().getForm().setText(Messages.TaskEditor_Task_ + task.getSummary());
        // } else {
        org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getConnectorUi(taskRepository.getConnectorKind());
        java.lang.String kindLabel = org.eclipse.mylyn.internal.tasks.ui.editors.Messages.TaskEditor_Task;
        if (connectorUi != null) {
            kindLabel = connectorUi.getTaskKindLabel(task);
        }
        java.lang.String idLabel = task.getTaskKey();
        if (idLabel != null) {
            kindLabel += " " + idLabel;// $NON-NLS-1$

        }
        if (hasLeftToolBar() && (titleLabel != null)) {
            titleLabel.setText(kindLabel);
            getHeaderForm().getForm().setText(null);
            setHeaderImage(null);
        } else {
            getHeaderForm().getForm().setText(kindLabel);
        }
    }

    /**
     * Update the title of the editor.
     *
     * @deprecated use {@link #updateHeaderToolBar()} instead
     */
    @java.lang.Deprecated
    public void updateTitle(java.lang.String name) {
        updateHeader();
    }

    private void updateTitleImage() {
        if (task != null) {
            org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getConnectorUi(task.getConnectorKind());
            if (connectorUi != null) {
                org.eclipse.jface.resource.ImageDescriptor overlayDescriptor = connectorUi.getTaskKindOverlay(task);
                setTitleImage(org.eclipse.mylyn.commons.ui.CommonImages.getCompositeTaskImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.TASK, overlayDescriptor, false));
            } else {
                setTitleImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.TASK));
            }
            // } else if (getEditorInput() instanceof AbstractRepositoryTaskEditorInput) {
            // setTitleImage(CommonImages.getImage(TasksUiImages.TASK_REMOTE));
        } else {
            setTitleImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.TASK));
        }
    }
}