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
 *     David Green - fixes for bug 237503
 *     Frank Becker - fixes for bug 252300
 * 	   Kevin Sawicki - fixes for bug 306029
 * *****************************************************************************
 */
package org.eclipse.mylyn.tasks.ui.editors;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.commons.ui.CommonUiUtil;
import org.eclipse.mylyn.commons.ui.GradientCanvas;
import org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport;
import org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.ITaskListChangeListener;
import org.eclipse.mylyn.internal.tasks.core.ITaskListRunnable;
import org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta;
import org.eclipse.mylyn.internal.tasks.core.data.ITaskDataManagerListener;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataManagerEvent;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.actions.ClearOutgoingAction;
import org.eclipse.mylyn.internal.tasks.ui.actions.DeleteTaskEditorAction;
import org.eclipse.mylyn.internal.tasks.ui.actions.NewSubTaskAction;
import org.eclipse.mylyn.internal.tasks.ui.actions.OpenWithBrowserAction;
import org.eclipse.mylyn.internal.tasks.ui.actions.SynchronizeEditorAction;
import org.eclipse.mylyn.internal.tasks.ui.editors.AbstractTaskEditorSection;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.mylyn.internal.tasks.ui.editors.FocusTracker;
import org.eclipse.mylyn.internal.tasks.ui.editors.Messages;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskAttachmentDropListener;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionContributor;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionPart;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorAttachmentPart;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorAttributePart;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorContributionExtensionReader;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorDescriptionPart;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorFindSupport;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorNewCommentPart;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlinePage;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorPeoplePart;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorPlanningPart;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorRichTextPart;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorSummaryPart;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskMigrator;
import org.eclipse.mylyn.internal.tasks.ui.editors.ToolBarButtonContribution;
import org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITask.SynchronizationState;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.ITaskDataWorkingCopy;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.core.data.TaskDataModelEvent;
import org.eclipse.mylyn.tasks.core.data.TaskDataModelListener;
import org.eclipse.mylyn.tasks.core.data.TaskRelation;
import org.eclipse.mylyn.tasks.core.sync.SubmitJob;
import org.eclipse.mylyn.tasks.core.sync.SubmitJobEvent;
import org.eclipse.mylyn.tasks.core.sync.SubmitJobListener;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.services.IDisposable;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
/**
 * Extend to provide a task editor page.
 *
 * @author Mik Kersten
 * @author Rob Elves
 * @author Steffen Pingel
 * @since 3.0
 */
public abstract class AbstractTaskEditorPage extends org.eclipse.mylyn.tasks.ui.editors.TaskFormPage implements org.eclipse.jface.viewers.ISelectionProvider , org.eclipse.jface.viewers.ISelectionChangedListener {
    /**
     * Causes the form page to reflow on resize.
     */
    private final class ParentResizeHandler implements org.eclipse.swt.widgets.Listener {
        private int generation;

        public void handleEvent(org.eclipse.swt.widgets.Event event) {
            ++generation;
            org.eclipse.swt.widgets.Display.getCurrent().timerExec(300, new java.lang.Runnable() {
                int scheduledGeneration = generation;

                public void run() {
                    if (getManagedForm().getForm().isDisposed()) {
                        return;
                    }
                    // only reflow if this is the latest generation to prevent
                    // unnecessary reflows while the form is being resized
                    if (scheduledGeneration == generation) {
                        getManagedForm().reflow(true);
                    }
                }
            });
        }
    }

    private class SubmitTaskJobListener extends org.eclipse.mylyn.tasks.core.sync.SubmitJobListener {
        private final boolean attachContext;

        private final boolean expandLastComment;

        public SubmitTaskJobListener(boolean attachContext, boolean expandLastComment) {
            this.attachContext = attachContext;
            this.expandLastComment = expandLastComment;
        }

        @java.lang.Override
        public void done(org.eclipse.mylyn.tasks.core.sync.SubmitJobEvent event) {
            final org.eclipse.mylyn.tasks.core.sync.SubmitJob job = event.getJob();
            org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
                private void addTask(org.eclipse.mylyn.tasks.core.ITask newTask) {
                    org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer parent = null;
                    org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart actionPart = getPart(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.ID_PART_ACTIONS);
                    if (actionPart instanceof org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionPart) {
                        parent = ((org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionPart) (actionPart)).getCategory();
                    }
                    org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().addTask(newTask, parent);
                }

                public void run() {
                    try {
                        if (job.getStatus() == null) {
                            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.synchronizeRepositoryInBackground(getTaskRepository());
                            if (job.getTask().equals(getTask())) {
                                refresh();
                            } else {
                                org.eclipse.mylyn.tasks.core.ITask oldTask = getTask();
                                org.eclipse.mylyn.tasks.core.ITask newTask = job.getTask();
                                addTask(newTask);
                                org.eclipse.mylyn.internal.tasks.ui.editors.TaskMigrator migrator = new org.eclipse.mylyn.internal.tasks.ui.editors.TaskMigrator(oldTask);
                                migrator.setDelete(true);
                                migrator.setEditor(getTaskEditor());
                                migrator.setMigrateDueDate(!connector.hasRepositoryDueDate(getTaskRepository(), newTask, taskData));
                                migrator.execute(newTask);
                            }
                            if (expandLastComment) {
                                expandLastComment();
                            }
                        }
                        handleTaskSubmitted(new org.eclipse.mylyn.tasks.core.sync.SubmitJobEvent(job));
                    } finally {
                        showEditorBusy(false);
                    }
                }
            });
        }

        @java.lang.Override
        public void taskSubmitted(org.eclipse.mylyn.tasks.core.sync.SubmitJobEvent event, org.eclipse.core.runtime.IProgressMonitor monitor) throws org.eclipse.core.runtime.CoreException {
            if ((!getModel().getTaskData().isNew()) && attachContext) {
                org.eclipse.mylyn.tasks.core.data.TaskData taskData = getModel().getTaskData();
                org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute = null;
                if (taskData != null) {
                    taskAttribute = taskData.getRoot().createMappedAttribute(org.eclipse.mylyn.tasks.core.data.TaskAttribute.NEW_ATTACHMENT);
                }
                org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.postContext(connector, getModel().getTaskRepository(), task, "", taskAttribute, monitor);// $NON-NLS-1$

            }
        }

        @java.lang.Override
        public void taskSynchronized(org.eclipse.mylyn.tasks.core.sync.SubmitJobEvent event, org.eclipse.core.runtime.IProgressMonitor monitor) {
        }
    }

    // private class TaskListChangeListener extends TaskListChangeAdapter {
    // @Override
    // public void containersChanged(Set<TaskContainerDelta> containers) {
    // if (refreshDisabled) {
    // return;
    // }
    // ITask taskToRefresh = null;
    // for (TaskContainerDelta taskContainerDelta : containers) {
    // if (task.equals(taskContainerDelta.getElement())) {
    // if (taskContainerDelta.getKind().equals(TaskContainerDelta.Kind.CONTENT)
    // && !taskContainerDelta.isTransient()) {
    // taskToRefresh = (ITask) taskContainerDelta.getElement();
    // break;
    // }
    // }
    // }
    // if (taskToRefresh != null) {
    // PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
    // public void run() {
    // if (!isDirty() && task.getSynchronizationState() == SynchronizationState.SYNCHRONIZED) {
    // // automatically refresh if the user has not made any changes and there is no chance of missing incomings
    // refreshFormContent();
    // } else {
    // getTaskEditor().setMessage("Task has incoming changes", IMessageProvider.WARNING,
    // new HyperlinkAdapter() {
    // @Override
    // public void linkActivated(HyperlinkEvent e) {
    // refreshFormContent();
    // }
    // });
    // setSubmitEnabled(false);
    // }
    // }
    // });
    // }
    // }
    // }
    private final org.eclipse.mylyn.internal.tasks.core.data.ITaskDataManagerListener TASK_DATA_LISTENER = new org.eclipse.mylyn.internal.tasks.core.data.ITaskDataManagerListener() {
        public void taskDataUpdated(final org.eclipse.mylyn.internal.tasks.core.data.TaskDataManagerEvent event) {
            org.eclipse.mylyn.tasks.core.ITask task = event.getTask();
            if (task.equals(AbstractTaskEditorPage.this.getTask()) && event.getTaskDataUpdated()) {
                refresh(task);
            }
        }

        private void refresh(final org.eclipse.mylyn.tasks.core.ITask task) {
            org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
                public void run() {
                    if (refreshDisabled || busy) {
                        return;
                    }
                    if ((!isDirty()) && (task.getSynchronizationState() == org.eclipse.mylyn.tasks.core.ITask.SynchronizationState.SYNCHRONIZED)) {
                        // automatically refresh if the user has not made any changes and there is no chance of missing incomings
                        AbstractTaskEditorPage.this.refresh();
                    } else {
                        getTaskEditor().setMessage(org.eclipse.mylyn.internal.tasks.ui.editors.Messages.AbstractTaskEditorPage_Task_has_incoming_changes, org.eclipse.jface.dialogs.IMessageProvider.WARNING, new org.eclipse.ui.forms.events.HyperlinkAdapter() {
                            @java.lang.Override
                            public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                                AbstractTaskEditorPage.this.refresh();
                            }
                        });
                        setSubmitEnabled(false);
                    }
                }
            });
        }

        public void editsDiscarded(org.eclipse.mylyn.internal.tasks.core.data.TaskDataManagerEvent event) {
            if (event.getTask().equals(AbstractTaskEditorPage.this.getTask())) {
                refresh(event.getTask());
            }
        }
    };

    private class NotInTaskListListener extends org.eclipse.ui.forms.events.HyperlinkAdapter implements org.eclipse.mylyn.internal.tasks.core.ITaskListChangeListener , org.eclipse.ui.services.IDisposable {
        public NotInTaskListListener() {
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().addChangeListener(this);
        }

        @java.lang.Override
        public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().addTaskIfAbsent(task);
            getTaskEditor().setMessage(null, org.eclipse.jface.dialogs.IMessageProvider.NONE);
        }

        public void containersChanged(java.util.Set<org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta> containers) {
            // clears message if task is added to Task List.
            for (org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta taskContainerDelta : containers) {
                if (task.equals(taskContainerDelta.getElement())) {
                    if (taskContainerDelta.getKind().equals(TaskContainerDelta.Kind.ADDED)) {
                        org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
                            public void run() {
                                getTaskEditor().setMessage(null, org.eclipse.jface.dialogs.IMessageProvider.NONE);
                            }
                        });
                    }
                }
            }
        }

        public void dispose() {
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().removeChangeListener(this);
        }
    }

    private class MenuCreator implements org.eclipse.jface.action.IMenuCreator {
        private org.eclipse.jface.action.MenuManager menuManager;

        private org.eclipse.swt.widgets.Menu menu;

        public MenuCreator() {
        }

        public void dispose() {
            if (menu != null) {
                menu.dispose();
                menu = null;
            }
            if (menuManager != null) {
                menuManager.dispose();
                menuManager = null;
            }
        }

        public org.eclipse.swt.widgets.Menu getMenu(org.eclipse.swt.widgets.Control parent) {
            if (menuManager == null) {
                menuManager = new org.eclipse.jface.action.MenuManager();
                initialize(menuManager);
            }
            return menuManager.createContextMenu(parent);
        }

        public org.eclipse.swt.widgets.Menu getMenu(org.eclipse.swt.widgets.Menu parent) {
            return null;
        }

        protected void initialize(org.eclipse.jface.action.MenuManager menuManager) {
        }
    }

    private static final java.lang.String ERROR_NOCONNECTIVITY = org.eclipse.mylyn.internal.tasks.ui.editors.Messages.AbstractTaskEditorPage_Unable_to_submit_at_this_time;

    public static final java.lang.String ID_PART_ACTIONS = "org.eclipse.mylyn.tasks.ui.editors.parts.actions";// $NON-NLS-1$


    public static final java.lang.String ID_PART_ATTACHMENTS = "org.eclipse.mylyn.tasks.ui.editors.parts.attachments";// $NON-NLS-1$


    public static final java.lang.String ID_PART_ATTRIBUTES = "org.eclipse.mylyn.tasks.ui.editors.parts.attributes";// $NON-NLS-1$


    public static final java.lang.String ID_PART_COMMENTS = "org.eclipse.mylyn.tasks.ui.editors.parts.comments";// $NON-NLS-1$


    public static final java.lang.String ID_PART_DESCRIPTION = "org.eclipse.mylyn.tasks.ui.editors.parts.descriptions";// $NON-NLS-1$


    public static final java.lang.String ID_PART_NEW_COMMENT = "org.eclipse.mylyn.tasks.ui.editors.parts.newComment";// $NON-NLS-1$


    public static final java.lang.String ID_PART_PEOPLE = "org.eclipse.mylyn.tasks.ui.editors.parts.people";// $NON-NLS-1$


    public static final java.lang.String ID_PART_PLANNING = "org.eclipse.mylyn.tasks.ui.editors.parts.planning";// $NON-NLS-1$


    public static final java.lang.String ID_PART_SUMMARY = "org.eclipse.mylyn.tasks.ui.editors.parts.summary";// $NON-NLS-1$


    public static final java.lang.String PATH_ACTIONS = "actions";// $NON-NLS-1$


    /**
     *
     * @since 3.7
     */
    public static final java.lang.String PATH_ASSOCIATIONS = "associations";// $NON-NLS-1$


    public static final java.lang.String PATH_ATTACHMENTS = "attachments";// $NON-NLS-1$


    public static final java.lang.String PATH_ATTRIBUTES = "attributes";// $NON-NLS-1$


    public static final java.lang.String PATH_COMMENTS = "comments";// $NON-NLS-1$


    public static final java.lang.String PATH_HEADER = "header";// $NON-NLS-1$


    public static final java.lang.String PATH_PEOPLE = "people";// $NON-NLS-1$


    public static final java.lang.String PATH_PLANNING = "planning";// $NON-NLS-1$


    // private static final String ID_POPUP_MENU = "org.eclipse.mylyn.tasks.ui.editor.menu.page";
    private org.eclipse.mylyn.tasks.ui.editors.AttributeEditorFactory attributeEditorFactory;

    private org.eclipse.mylyn.tasks.ui.editors.AttributeEditorToolkit attributeEditorToolkit;

    private org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector;

    private final java.lang.String connectorKind;

    private org.eclipse.jface.viewers.StructuredSelection defaultSelection;

    private org.eclipse.swt.widgets.Composite editorComposite;

    private org.eclipse.ui.forms.widgets.ScrolledForm form;

    private boolean busy;

    private org.eclipse.jface.viewers.ISelection lastSelection;

    private org.eclipse.mylyn.tasks.core.data.TaskDataModel model;

    private boolean needsAddToCategory;

    private boolean reflow;

    private volatile boolean refreshDisabled;

    private final org.eclipse.core.runtime.ListenerList selectionChangedListeners;

    private org.eclipse.mylyn.internal.tasks.ui.actions.SynchronizeEditorAction synchronizeEditorAction;

    private org.eclipse.mylyn.tasks.core.ITask task;

    private org.eclipse.mylyn.tasks.core.data.TaskData taskData;

    // private ITaskListChangeListener taskListChangeListener;
    private org.eclipse.ui.forms.widgets.FormToolkit toolkit;

    private org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlinePage outlinePage;

    private org.eclipse.mylyn.internal.tasks.ui.editors.TaskAttachmentDropListener defaultDropListener;

    private org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport textSupport;

    private org.eclipse.swt.widgets.Composite partControl;

    private org.eclipse.mylyn.commons.ui.GradientCanvas footerComposite;

    private boolean needsFooter;

    private org.eclipse.swt.widgets.Button submitButton;

    private boolean submitEnabled;

    private boolean needsSubmit;

    private boolean needsSubmitButton;

    private boolean needsPrivateSection;

    private org.eclipse.mylyn.internal.tasks.ui.editors.FocusTracker focusTracker;

    private org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorFindSupport findSupport;

    /**
     *
     * @since 3.1
     */
    public AbstractTaskEditorPage(org.eclipse.mylyn.tasks.ui.editors.TaskEditor editor, java.lang.String id, java.lang.String label, java.lang.String connectorKind) {
        super(editor, id, label);
        org.eclipse.core.runtime.Assert.isNotNull(connectorKind);
        this.connectorKind = connectorKind;
        this.reflow = true;
        this.selectionChangedListeners = new org.eclipse.core.runtime.ListenerList();
        this.submitEnabled = true;
        this.needsSubmit = true;
    }

    public AbstractTaskEditorPage(org.eclipse.mylyn.tasks.ui.editors.TaskEditor editor, java.lang.String connectorKind) {
        this(editor, "id", "label", connectorKind);// $NON-NLS-1$ //$NON-NLS-2$

    }

    /**
     *
     * @since 3.1
     * @see FormPage#getEditor()
     */
    @java.lang.Override
    public org.eclipse.mylyn.tasks.ui.editors.TaskEditor getEditor() {
        return ((org.eclipse.mylyn.tasks.ui.editors.TaskEditor) (super.getEditor()));
    }

    public void addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener listener) {
        selectionChangedListeners.add(listener);
    }

    public void appendTextToNewComment(java.lang.String text) {
        org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart newCommentPart = getPart(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.ID_PART_NEW_COMMENT);
        if (newCommentPart instanceof org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorRichTextPart) {
            ((org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorRichTextPart) (newCommentPart)).appendText(text);
            newCommentPart.setFocus();
        }
    }

    public boolean canPerformAction(java.lang.String actionId) {
        if ((findSupport != null) && actionId.equals(org.eclipse.ui.actions.ActionFactory.FIND.getId())) {
            return true;
        }
        return org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport.canPerformAction(actionId, org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.getFocusControl(this));
    }

    public void close() {
        if (org.eclipse.swt.widgets.Display.getCurrent() != null) {
            getSite().getPage().closeEditor(getTaskEditor(), false);
        } else {
            // TODO consider removing asyncExec()
            org.eclipse.swt.widgets.Display activeDisplay = getSite().getShell().getDisplay();
            activeDisplay.asyncExec(new java.lang.Runnable() {
                public void run() {
                    if (((getSite() != null) && (getSite().getPage() != null)) && (!getManagedForm().getForm().isDisposed())) {
                        if (getTaskEditor() != null) {
                            getSite().getPage().closeEditor(getTaskEditor(), false);
                        } else {
                            getSite().getPage().closeEditor(AbstractTaskEditorPage.this, false);
                        }
                    }
                }
            });
        }
    }

    protected org.eclipse.mylyn.tasks.ui.editors.AttributeEditorFactory createAttributeEditorFactory() {
        return new org.eclipse.mylyn.tasks.ui.editors.AttributeEditorFactory(getModel(), getTaskRepository(), getEditorSite());
    }

    org.eclipse.mylyn.tasks.ui.editors.AttributeEditorToolkit createAttributeEditorToolkit() {
        return new org.eclipse.mylyn.tasks.ui.editors.AttributeEditorToolkit(textSupport);
    }

    @java.lang.Override
    public void createPartControl(org.eclipse.swt.widgets.Composite parent) {
        parent.addListener(org.eclipse.swt.SWT.Resize, new org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.ParentResizeHandler());
        if (needsFooter()) {
            partControl = getEditor().getToolkit().createComposite(parent);
            org.eclipse.swt.layout.GridLayout partControlLayout = new org.eclipse.swt.layout.GridLayout(1, false);
            partControlLayout.marginWidth = 0;
            partControlLayout.marginHeight = 0;
            partControlLayout.verticalSpacing = 0;
            partControl.setLayout(partControlLayout);
            super.createPartControl(partControl);
            getManagedForm().getForm().setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.FILL, true, true));
            footerComposite = new org.eclipse.mylyn.commons.ui.GradientCanvas(partControl, org.eclipse.swt.SWT.NONE);
            footerComposite.setSeparatorVisible(true);
            footerComposite.setSeparatorAlignment(org.eclipse.swt.SWT.TOP);
            org.eclipse.swt.layout.GridLayout headLayout = new org.eclipse.swt.layout.GridLayout();
            headLayout.marginHeight = 0;
            headLayout.marginWidth = 0;
            headLayout.horizontalSpacing = 0;
            headLayout.verticalSpacing = 0;
            headLayout.numColumns = 1;
            footerComposite.setLayout(headLayout);
            footerComposite.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.FILL, true, false));
            org.eclipse.ui.forms.FormColors colors = getEditor().getToolkit().getColors();
            org.eclipse.swt.graphics.Color top = colors.getColor(org.eclipse.ui.forms.IFormColors.H_GRADIENT_END);
            org.eclipse.swt.graphics.Color bottom = colors.getColor(org.eclipse.ui.forms.IFormColors.H_GRADIENT_START);
            footerComposite.setBackgroundGradient(new org.eclipse.swt.graphics.Color[]{ bottom, top }, new int[]{ 100 }, true);
            footerComposite.putColor(org.eclipse.ui.forms.IFormColors.H_BOTTOM_KEYLINE1, colors.getColor(org.eclipse.ui.forms.IFormColors.H_BOTTOM_KEYLINE1));
            footerComposite.putColor(org.eclipse.ui.forms.IFormColors.H_BOTTOM_KEYLINE2, colors.getColor(org.eclipse.ui.forms.IFormColors.H_BOTTOM_KEYLINE2));
            footerComposite.putColor(org.eclipse.ui.forms.IFormColors.H_HOVER_LIGHT, colors.getColor(org.eclipse.ui.forms.IFormColors.H_HOVER_LIGHT));
            footerComposite.putColor(org.eclipse.ui.forms.IFormColors.H_HOVER_FULL, colors.getColor(org.eclipse.ui.forms.IFormColors.H_HOVER_FULL));
            footerComposite.putColor(org.eclipse.ui.forms.IFormColors.TB_TOGGLE, colors.getColor(org.eclipse.ui.forms.IFormColors.TB_TOGGLE));
            footerComposite.putColor(org.eclipse.ui.forms.IFormColors.TB_TOGGLE_HOVER, colors.getColor(org.eclipse.ui.forms.IFormColors.TB_TOGGLE_HOVER));
            footerComposite.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.BOTTOM, true, false));
            createFooterContent(footerComposite);
        } else {
            super.createPartControl(parent);
        }
    }

    @java.lang.Override
    protected void createFormContent(final org.eclipse.ui.forms.IManagedForm managedForm) {
        super.createFormContent(managedForm);
        form = managedForm.getForm();
        toolkit = managedForm.getToolkit();
        registerDefaultDropListener(form);
        org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil.disableScrollingOnFocus(form);
        try {
            setReflow(false);
            editorComposite = form.getBody();
            // TODO consider using TableWrapLayout, it makes resizing much faster
            org.eclipse.swt.layout.GridLayout editorLayout = new org.eclipse.swt.layout.GridLayout();
            editorLayout.verticalSpacing = 0;
            editorComposite.setLayout(editorLayout);
            // form.setData("focusScrolling", Boolean.FALSE);
            // menuManager = new MenuManager();
            // menuManager.setRemoveAllWhenShown(true);
            // getEditorSite().registerContextMenu(ID_POPUP_MENU, menuManager, this, true);
            // editorComposite.setMenu(menuManager.createContextMenu(editorComposite));
            editorComposite.setMenu(getTaskEditor().getMenu());
            org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getConnectorUi(getConnectorKind());
            if (connectorUi == null) {
                org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
                    public void run() {
                        getTaskEditor().setMessage(org.eclipse.mylyn.internal.tasks.ui.editors.Messages.AbstractTaskEditorPage_Synchronize_to_update_editor_contents, org.eclipse.jface.dialogs.IMessageProvider.INFORMATION, new org.eclipse.ui.forms.events.HyperlinkAdapter() {
                            @java.lang.Override
                            public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                                AbstractTaskEditorPage.this.refresh();
                            }
                        });
                    }
                });
            }
            if (taskData != null) {
                createFormContentInternal();
            }
            updateHeaderMessage();
        } finally {
            setReflow(true);
            // if the editor is restored as part of workbench startup then we must reflow() asynchronously
            // otherwise the editor layout is incorrect
            boolean reflowRequired = calculateReflowRequired(form);
            if (reflowRequired) {
                org.eclipse.swt.widgets.Display.getCurrent().asyncExec(new java.lang.Runnable() {
                    public void run() {
                        // this fixes a problem with layout that occurs when an editor
                        // is restored before the workbench is fully initialized
                        reflow();
                    }
                });
            }
        }
    }

    private boolean calculateReflowRequired(org.eclipse.ui.forms.widgets.ScrolledForm form) {
        org.eclipse.swt.widgets.Composite stopComposite = getEditor().getEditorParent().getParent().getParent();
        org.eclipse.swt.widgets.Composite composite = form.getParent();
        while (composite != null) {
            org.eclipse.swt.graphics.Rectangle clientArea = composite.getClientArea();
            if (clientArea.width > 1) {
                return false;
            }
            if (composite == stopComposite) {
                return true;
            }
            composite = composite.getParent();
        } 
        return true;
    }

    private void createFormContentInternal() {
        // end life-cycle of previous editor controls
        if (attributeEditorToolkit != null) {
            attributeEditorToolkit.dispose();
        }
        // start life-cycle of previous editor controls
        if (attributeEditorFactory == null) {
            attributeEditorFactory = createAttributeEditorFactory();
            org.eclipse.core.runtime.Assert.isNotNull(attributeEditorFactory);
        }
        attributeEditorToolkit = createAttributeEditorToolkit();
        org.eclipse.core.runtime.Assert.isNotNull(attributeEditorToolkit);
        attributeEditorToolkit.setMenu(editorComposite.getMenu());
        attributeEditorFactory.setEditorToolkit(attributeEditorToolkit);
        createParts();
        focusTracker = new org.eclipse.mylyn.internal.tasks.ui.editors.FocusTracker();
        focusTracker.track(editorComposite);
        // AbstractTaskEditorPart summaryPart = getPart(ID_PART_SUMMARY);
        // if (summaryPart != null) {
        // lastFocusControl = summaryPart.getControl();
        // }
    }

    protected org.eclipse.mylyn.tasks.core.data.TaskDataModel createModel(org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput input) throws org.eclipse.core.runtime.CoreException {
        org.eclipse.mylyn.tasks.core.data.ITaskDataWorkingCopy taskDataState;
        try {
            taskDataState = org.eclipse.mylyn.tasks.ui.TasksUi.getTaskDataManager().getWorkingCopy(task);
        } catch (org.eclipse.core.runtime.OperationCanceledException e) {
            // XXX retry once to work around bug 235479
            taskDataState = org.eclipse.mylyn.tasks.ui.TasksUi.getTaskDataManager().getWorkingCopy(task);
        }
        org.eclipse.mylyn.tasks.core.TaskRepository taskRepository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(taskDataState.getConnectorKind(), taskDataState.getRepositoryUrl());
        return new org.eclipse.mylyn.tasks.core.data.TaskDataModel(taskRepository, input.getTask(), taskDataState);
    }

    /**
     * To suppress a section, just remove its descriptor from the list. To add your own section in a specific order on
     * the page, use the path value for where you want it to appear (your descriptor will appear after previously added
     * descriptors with the same path), and add it to the descriptors list in your override of this method.
     */
    protected java.util.Set<org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor> createPartDescriptors() {
        java.util.Set<org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor> descriptors = new java.util.LinkedHashSet<org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor>();
        descriptors.add(new org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.ID_PART_SUMMARY) {
            @java.lang.Override
            public org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart createPart() {
                return new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorSummaryPart();
            }
        }.setPath(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.PATH_HEADER));
        descriptors.add(new org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.ID_PART_ATTRIBUTES) {
            @java.lang.Override
            public org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart createPart() {
                return new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorAttributePart();
            }
        }.setPath(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.PATH_ATTRIBUTES));
        if (((!taskData.isNew()) && (connector.getTaskAttachmentHandler() != null)) && (org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.canDownloadAttachment(task) || org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.canUploadAttachment(task))) {
            descriptors.add(new org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.ID_PART_ATTACHMENTS) {
                @java.lang.Override
                public org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart createPart() {
                    return new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorAttachmentPart();
                }
            }.setPath(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.PATH_ATTACHMENTS));
        }
        if (needsPrivateSection() || taskData.isNew()) {
            descriptors.add(new org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.ID_PART_PLANNING) {
                @java.lang.Override
                public org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart createPart() {
                    return new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorPlanningPart();
                }
            }.setPath(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.PATH_PLANNING));
        }
        descriptors.add(new org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.ID_PART_DESCRIPTION) {
            @java.lang.Override
            public org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart createPart() {
                org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorDescriptionPart part = new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorDescriptionPart();
                if (getModel().getTaskData().isNew()) {
                    part.setExpandVertically(true);
                    part.setSectionStyle(org.eclipse.ui.forms.widgets.ExpandableComposite.TITLE_BAR | org.eclipse.ui.forms.widgets.ExpandableComposite.EXPANDED);
                }
                return part;
            }
        }.setPath(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.PATH_COMMENTS));
        if (!taskData.isNew()) {
            descriptors.add(new org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.ID_PART_COMMENTS) {
                @java.lang.Override
                public org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart createPart() {
                    return new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart();
                }
            }.setPath(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.PATH_COMMENTS));
        }
        descriptors.add(new org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.ID_PART_NEW_COMMENT) {
            @java.lang.Override
            public org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart createPart() {
                return new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorNewCommentPart();
            }
        }.setPath(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.PATH_COMMENTS));
        descriptors.add(new org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.ID_PART_ACTIONS) {
            @java.lang.Override
            public org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart createPart() {
                return new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionPart();
            }
        }.setPath(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.PATH_ACTIONS));
        descriptors.add(new org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.ID_PART_PEOPLE) {
            @java.lang.Override
            public org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart createPart() {
                return new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorPeoplePart();
            }
        }.setPath(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.PATH_PEOPLE));
        descriptors.addAll(getContributionPartDescriptors());
        return descriptors;
    }

    private java.util.Collection<org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor> getContributionPartDescriptors() {
        return org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorContributionExtensionReader.getRepositoryEditorContributions();
    }

    protected void createParts() {
        java.util.List<org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor> descriptors = new java.util.LinkedList<org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor>(createPartDescriptors());
        // single column
        createParts(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.PATH_HEADER, editorComposite, descriptors);
        createParts(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.PATH_ASSOCIATIONS, editorComposite, descriptors);
        createParts(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.PATH_ATTRIBUTES, editorComposite, descriptors);
        createParts(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.PATH_ATTACHMENTS, editorComposite, descriptors);
        createParts(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.PATH_PLANNING, editorComposite, descriptors);
        createParts(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.PATH_COMMENTS, editorComposite, descriptors);
        // two column
        org.eclipse.swt.widgets.Composite bottomComposite = toolkit.createComposite(editorComposite);
        bottomComposite.setLayout(org.eclipse.jface.layout.GridLayoutFactory.fillDefaults().numColumns(2).create());
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().grab(true, false).applyTo(bottomComposite);
        createParts(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.PATH_ACTIONS, bottomComposite, descriptors);
        createParts(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.PATH_PEOPLE, bottomComposite, descriptors);
        bottomComposite.pack(true);
    }

    private void createParts(java.lang.String path, final org.eclipse.swt.widgets.Composite parent, final java.util.Collection<org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor> descriptors) {
        for (java.util.Iterator<org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor> it = descriptors.iterator(); it.hasNext();) {
            final org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor descriptor = it.next();
            if ((path == null) || path.equals(descriptor.getPath())) {
                org.eclipse.core.runtime.SafeRunner.run(new org.eclipse.core.runtime.ISafeRunnable() {
                    public void handleException(java.lang.Throwable e) {
                        org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, ("Error creating task editor part: \"" + descriptor.getId()) + "\"", e));// $NON-NLS-1$ //$NON-NLS-2$

                    }

                    public void run() throws java.lang.Exception {
                        org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart part = descriptor.createPart();
                        part.setPartId(descriptor.getId());
                        initializePart(parent, part, descriptors);
                    }
                });
                it.remove();
            }
        }
    }

    private void createSubParts(final org.eclipse.mylyn.internal.tasks.ui.editors.AbstractTaskEditorSection parentPart, final java.util.Collection<org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor> descriptors) {
        for (final org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor descriptor : descriptors) {
            int i;
            java.lang.String path = descriptor.getPath();
            if ((path != null) && ((i = path.indexOf("/")) != (-1))) {
                // $NON-NLS-1$
                java.lang.String parentId = path.substring(0, i);
                final java.lang.String subPath = path.substring(i + 1);
                if (parentId.equals(parentPart.getPartId())) {
                    org.eclipse.core.runtime.SafeRunner.run(new org.eclipse.core.runtime.ISafeRunnable() {
                        public void handleException(java.lang.Throwable e) {
                            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, ("Error creating task editor part: \"" + descriptor.getId()) + "\"", e));// $NON-NLS-1$ //$NON-NLS-2$

                        }

                        public void run() throws java.lang.Exception {
                            org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart part = descriptor.createPart();
                            part.setPartId(descriptor.getId());
                            getManagedForm().addPart(part);
                            part.initialize(AbstractTaskEditorPage.this);
                            parentPart.addSubPart(subPath, part);
                        }
                    });
                }
            }
        }
    }

    @java.lang.Override
    public void dispose() {
        if (textSupport != null) {
            textSupport.dispose();
        }
        if (attributeEditorToolkit != null) {
            attributeEditorToolkit.dispose();
        }
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskDataManager().removeListener(TASK_DATA_LISTENER);
        super.dispose();
    }

    public void doAction(java.lang.String actionId) {
        if ((findSupport != null) && actionId.equals(org.eclipse.ui.actions.ActionFactory.FIND.getId())) {
            findSupport.toggleFind();
        }
        org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport.doAction(actionId, org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.getFocusControl(this));
    }

    @java.lang.Override
    public void doSave(org.eclipse.core.runtime.IProgressMonitor monitor) {
        if (!isDirty()) {
            return;
        }
        getManagedForm().commit(true);
        if (model.isDirty()) {
            try {
                model.save(monitor);
            } catch (final org.eclipse.core.runtime.CoreException e) {
                org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Error saving task", e));// $NON-NLS-1$

                getTaskEditor().setMessage(org.eclipse.mylyn.internal.tasks.ui.editors.Messages.AbstractTaskEditorPage_Could_not_save_task, org.eclipse.jface.dialogs.IMessageProvider.ERROR, new org.eclipse.ui.forms.events.HyperlinkAdapter() {
                    @java.lang.Override
                    public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent event) {
                        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.displayStatus(org.eclipse.mylyn.internal.tasks.ui.editors.Messages.AbstractTaskEditorPage_Save_failed, e.getStatus());
                    }
                });
            }
        }
        // update the summary of unsubmitted repository tasks
        if (getTask().getSynchronizationState() == org.eclipse.mylyn.tasks.core.ITask.SynchronizationState.OUTGOING_NEW) {
            final java.lang.String summary = connector.getTaskMapping(model.getTaskData()).getSummary();
            try {
                org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().run(new org.eclipse.mylyn.internal.tasks.core.ITaskListRunnable() {
                    public void execute(org.eclipse.core.runtime.IProgressMonitor monitor) throws org.eclipse.core.runtime.CoreException {
                        task.setSummary(summary);
                    }
                });
                org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().notifyElementChanged(task);
            } catch (org.eclipse.core.runtime.CoreException e) {
                org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, ("Failed to set summary for task \"" + task) + "\"", e));// $NON-NLS-1$ //$NON-NLS-2$

            }
        }
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().addTaskIfAbsent(task);
        updateHeaderMessage();
        getManagedForm().dirtyStateChanged();
        getTaskEditor().updateHeaderToolBar();
    }

    @java.lang.Override
    public void doSaveAs() {
        throw new java.lang.UnsupportedOperationException();
    }

    public void doSubmit() {
        if ((!submitEnabled) || (!needsSubmit())) {
            return;
        }
        try {
            showEditorBusy(true);
            doSave(new org.eclipse.core.runtime.NullProgressMonitor());
            org.eclipse.mylyn.tasks.core.data.TaskAttribute newCommentAttribute = getModel().getTaskData().getRoot().getMappedAttribute(org.eclipse.mylyn.tasks.core.data.TaskAttribute.COMMENT_NEW);
            boolean expandLastComment = (newCommentAttribute != null) && getModel().getChangedAttributes().contains(newCommentAttribute);
            org.eclipse.mylyn.tasks.core.sync.SubmitJob submitJob = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getJobFactory().createSubmitTaskJob(connector, getModel().getTaskRepository(), task, getModel().getTaskData(), getModel().getChangedOldAttributes());
            submitJob.addSubmitJobListener(new org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.SubmitTaskJobListener(getAttachContext(), expandLastComment));
            submitJob.schedule();
        } catch (java.lang.RuntimeException e) {
            showEditorBusy(false);
            throw e;
        }
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().addTaskIfAbsent(task);
    }

    /**
     * Override for customizing the tool bar.
     */
    @java.lang.Override
    public void fillToolBar(org.eclipse.jface.action.IToolBarManager toolBarManager) {
        final org.eclipse.mylyn.tasks.core.TaskRepository taskRepository = (model != null) ? getModel().getTaskRepository() : null;
        if (taskData == null) {
            synchronizeEditorAction = new org.eclipse.mylyn.internal.tasks.ui.actions.SynchronizeEditorAction();
            synchronizeEditorAction.selectionChanged(new org.eclipse.jface.viewers.StructuredSelection(getTaskEditor()));
            toolBarManager.appendToGroup("repository", synchronizeEditorAction);// $NON-NLS-1$

        } else {
            if (taskData.isNew()) {
                org.eclipse.mylyn.internal.tasks.ui.actions.DeleteTaskEditorAction deleteAction = new org.eclipse.mylyn.internal.tasks.ui.actions.DeleteTaskEditorAction(getTask());
                deleteAction.setImageDescriptor(org.eclipse.mylyn.commons.ui.CommonImages.CLEAR);
                toolBarManager.appendToGroup("new", deleteAction);// $NON-NLS-1$

            } else if (taskRepository != null) {
                org.eclipse.mylyn.internal.tasks.ui.actions.ClearOutgoingAction clearOutgoingAction = new org.eclipse.mylyn.internal.tasks.ui.actions.ClearOutgoingAction(java.util.Collections.singletonList(((org.eclipse.mylyn.tasks.core.IRepositoryElement) (task))));
                clearOutgoingAction.setTaskEditorPage(this);
                if (clearOutgoingAction.isEnabled()) {
                    toolBarManager.appendToGroup("new", clearOutgoingAction);// $NON-NLS-1$

                }
                if (task.getSynchronizationState() != org.eclipse.mylyn.tasks.core.ITask.SynchronizationState.OUTGOING_NEW) {
                    synchronizeEditorAction = new org.eclipse.mylyn.internal.tasks.ui.actions.SynchronizeEditorAction();
                    synchronizeEditorAction.selectionChanged(new org.eclipse.jface.viewers.StructuredSelection(getTaskEditor()));
                    toolBarManager.appendToGroup("repository", synchronizeEditorAction);// $NON-NLS-1$

                }
                org.eclipse.mylyn.internal.tasks.ui.actions.NewSubTaskAction newSubTaskAction = new org.eclipse.mylyn.internal.tasks.ui.actions.NewSubTaskAction();
                newSubTaskAction.selectionChanged(newSubTaskAction, new org.eclipse.jface.viewers.StructuredSelection(task));
                if (newSubTaskAction.isEnabled()) {
                    toolBarManager.appendToGroup("new", newSubTaskAction);// $NON-NLS-1$

                }
                org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getConnectorUi(taskData.getConnectorKind());
                if (connectorUi != null) {
                    final java.lang.String historyUrl = connectorUi.getTaskHistoryUrl(taskRepository, task);
                    if (historyUrl != null) {
                        final org.eclipse.jface.action.Action historyAction = new org.eclipse.jface.action.Action() {
                            @java.lang.Override
                            public void run() {
                                org.eclipse.mylyn.tasks.ui.TasksUiUtil.openUrl(historyUrl);
                            }
                        };
                        historyAction.setText(org.eclipse.mylyn.internal.tasks.ui.editors.Messages.AbstractTaskEditorPage_History);
                        historyAction.setImageDescriptor(org.eclipse.mylyn.tasks.ui.TasksUiImages.TASK_REPOSITORY_HISTORY);
                        historyAction.setToolTipText(org.eclipse.mylyn.internal.tasks.ui.editors.Messages.AbstractTaskEditorPage_History);
                        if (getEditor().openWithBrowserAction != null) {
                            getEditor().openWithBrowserAction.setMenuCreator(new org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.MenuCreator() {
                                @java.lang.Override
                                protected void initialize(org.eclipse.jface.action.MenuManager menuManager) {
                                    org.eclipse.mylyn.internal.tasks.ui.actions.OpenWithBrowserAction openWithBrowserAction = new org.eclipse.mylyn.internal.tasks.ui.actions.OpenWithBrowserAction();
                                    openWithBrowserAction.selectionChanged(new org.eclipse.jface.viewers.StructuredSelection(task));
                                    menuManager.add(openWithBrowserAction);
                                    menuManager.add(new org.eclipse.jface.action.Separator());
                                    menuManager.add(historyAction);
                                }
                            });
                        } else {
                            toolBarManager.prependToGroup("open", historyAction);// $NON-NLS-1$

                        }
                    }
                }
            }
            if (needsSubmitButton()) {
                org.eclipse.mylyn.internal.tasks.ui.editors.ToolBarButtonContribution submitButtonContribution = new org.eclipse.mylyn.internal.tasks.ui.editors.ToolBarButtonContribution("org.eclipse.mylyn.tasks.toolbars.submit") {
                    // $NON-NLS-1$
                    @java.lang.Override
                    protected org.eclipse.swt.widgets.Control createButton(org.eclipse.swt.widgets.Composite composite) {
                        submitButton = new org.eclipse.swt.widgets.Button(composite, org.eclipse.swt.SWT.FLAT);
                        submitButton.setText(org.eclipse.mylyn.internal.tasks.ui.editors.Messages.TaskEditorActionPart_Submit + " ");// $NON-NLS-1$

                        submitButton.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.REPOSITORY_SUBMIT));
                        submitButton.setBackground(null);
                        submitButton.addListener(org.eclipse.swt.SWT.Selection, new org.eclipse.swt.widgets.Listener() {
                            public void handleEvent(org.eclipse.swt.widgets.Event e) {
                                doSubmit();
                            }
                        });
                        return submitButton;
                    }
                };
                submitButtonContribution.marginLeft = 10;
                toolBarManager.add(submitButtonContribution);
            }
            if (findSupport != null) {
                findSupport.addFindAction(toolBarManager);
            }
        }
    }

    protected void fireSelectionChanged(org.eclipse.jface.viewers.ISelection selection) {
        // create an event
        final org.eclipse.jface.viewers.SelectionChangedEvent event = new org.eclipse.jface.viewers.SelectionChangedEvent(this, selection);
        // fire the event
        java.lang.Object[] listeners = selectionChangedListeners.getListeners();
        for (java.lang.Object listener : listeners) {
            final org.eclipse.jface.viewers.ISelectionChangedListener l = ((org.eclipse.jface.viewers.ISelectionChangedListener) (listener));
            org.eclipse.core.runtime.SafeRunner.run(new org.eclipse.jface.util.SafeRunnable() {
                public void run() {
                    l.selectionChanged(event);
                }
            });
        }
    }

    @java.lang.SuppressWarnings("rawtypes")
    @java.lang.Override
    public java.lang.Object getAdapter(java.lang.Class adapter) {
        if (adapter == org.eclipse.ui.views.contentoutline.IContentOutlinePage.class) {
            updateOutlinePage();
            return outlinePage;
        }
        // TODO 3.5 replace by getTextSupport() method
        if (adapter == org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport.class) {
            return textSupport;
        }
        return super.getAdapter(adapter);
    }

    private void updateOutlinePage() {
        if (outlinePage == null) {
            outlinePage = new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlinePage();
            outlinePage.addSelectionChangedListener(new org.eclipse.jface.viewers.ISelectionChangedListener() {
                public void selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent event) {
                    org.eclipse.jface.viewers.ISelection selection = event.getSelection();
                    if (selection instanceof org.eclipse.jface.viewers.StructuredSelection) {
                        java.lang.Object select = ((org.eclipse.jface.viewers.StructuredSelection) (selection)).getFirstElement();
                        selectReveal(select);
                        getEditor().setActivePage(getId());
                    }
                }
            });
        }
        if (getModel() != null) {
            org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode node = org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode.parse(getModel().getTaskData(), false);
            outlinePage.setInput(getTaskRepository(), node);
        } else {
            outlinePage.setInput(null, null);
        }
    }

    private boolean getAttachContext() {
        org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart actionPart = getPart(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.ID_PART_ACTIONS);
        if (actionPart instanceof org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionPart) {
            return ((org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionPart) (actionPart)).getAttachContext();
        }
        return false;
    }

    public org.eclipse.mylyn.tasks.ui.editors.AttributeEditorFactory getAttributeEditorFactory() {
        return attributeEditorFactory;
    }

    public org.eclipse.mylyn.tasks.ui.editors.AttributeEditorToolkit getAttributeEditorToolkit() {
        return attributeEditorToolkit;
    }

    public org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector getConnector() {
        return connector;
    }

    public java.lang.String getConnectorKind() {
        return connectorKind;
    }

    /**
     *
     * @return The composite for the whole editor.
     */
    public org.eclipse.swt.widgets.Composite getEditorComposite() {
        return editorComposite;
    }

    public org.eclipse.mylyn.tasks.core.data.TaskDataModel getModel() {
        return model;
    }

    public org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart getPart(java.lang.String partId) {
        org.eclipse.core.runtime.Assert.isNotNull(partId);
        if (getManagedForm() != null) {
            for (org.eclipse.ui.forms.IFormPart part : getManagedForm().getParts()) {
                if (part instanceof org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart) {
                    org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart taskEditorPart = ((org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart) (part));
                    if (partId.equals(taskEditorPart.getPartId())) {
                        return taskEditorPart;
                    }
                }
            }
        }
        return null;
    }

    public org.eclipse.jface.viewers.ISelection getSelection() {
        return lastSelection;
    }

    public org.eclipse.mylyn.tasks.core.ITask getTask() {
        return task;
    }

    public org.eclipse.mylyn.tasks.ui.editors.TaskEditor getTaskEditor() {
        return getEditor();
    }

    public org.eclipse.mylyn.tasks.core.TaskRepository getTaskRepository() {
        // FIXME model can be null
        return getModel().getTaskRepository();
    }

    /**
     * Invoked after task submission has completed. This method is invoked on the UI thread in all cases whether
     * submission was successful, canceled or failed. The value returned by <code>event.getJob().getStatus()</code>
     * indicates the result of the submit job. Sub-classes may override but are encouraged to invoke the super method.
     *
     * @since 3.2
     * @see SubmitJob
     */
    protected void handleTaskSubmitted(org.eclipse.mylyn.tasks.core.sync.SubmitJobEvent event) {
        org.eclipse.core.runtime.IStatus status = event.getJob().getStatus();
        if ((status != null) && (status.getSeverity() != org.eclipse.core.runtime.IStatus.CANCEL)) {
            handleSubmitError(event.getJob());
        }
    }

    private void handleSubmitError(org.eclipse.mylyn.tasks.core.sync.SubmitJob job) {
        if ((form != null) && (!form.isDisposed())) {
            final org.eclipse.core.runtime.IStatus status = job.getStatus();
            java.lang.String message = null;
            if (status.getCode() == org.eclipse.mylyn.tasks.core.RepositoryStatus.REPOSITORY_COMMENT_REQUIRED) {
                org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.displayStatus(org.eclipse.mylyn.internal.tasks.ui.editors.Messages.AbstractTaskEditorPage_Comment_required, status);
                org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart newCommentPart = getPart(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.ID_PART_NEW_COMMENT);
                if (newCommentPart != null) {
                    newCommentPart.setFocus();
                }
                return;
            } else if (status.getCode() == org.eclipse.mylyn.tasks.core.RepositoryStatus.ERROR_REPOSITORY_LOGIN) {
                if (org.eclipse.mylyn.tasks.ui.TasksUiUtil.openEditRepositoryWizard(getTaskRepository()) == org.eclipse.jface.window.Window.OK) {
                    submitEnabled = true;
                    doSubmit();
                    return;
                } else {
                    message = getMessageFromStatus(status);
                }
            } else if (status.getCode() == org.eclipse.mylyn.tasks.core.RepositoryStatus.ERROR_IO) {
                message = org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.ERROR_NOCONNECTIVITY;
            } else {
                message = getMessageFromStatus(status);
            }
            getTaskEditor().setMessage(message, org.eclipse.jface.dialogs.IMessageProvider.ERROR, new org.eclipse.ui.forms.events.HyperlinkAdapter() {
                @java.lang.Override
                public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                    org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.displayStatus(org.eclipse.mylyn.internal.tasks.ui.editors.Messages.AbstractTaskEditorPage_Submit_failed, status);
                }
            });
        }
    }

    private java.lang.String getMessageFromStatus(final org.eclipse.core.runtime.IStatus status) {
        java.lang.String message;
        if (status.getMessage().length() > 0) {
            if (status.getMessage().length() < 256) {
                message = org.eclipse.mylyn.internal.tasks.ui.editors.Messages.AbstractTaskEditorPage_Submit_failed_ + status.getMessage();
            } else {
                message = (org.eclipse.mylyn.internal.tasks.ui.editors.Messages.AbstractTaskEditorPage_Submit_failed_ + status.getMessage().substring(0, 256)) + "...";// $NON-NLS-1$

            }
        } else {
            message = org.eclipse.mylyn.internal.tasks.ui.editors.Messages.AbstractTaskEditorPage_Submit_failed;
        }
        return message.replaceAll("\n", " ").replaceAll("\r", " ");// $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    }

    @java.lang.Override
    public void init(org.eclipse.ui.IEditorSite site, org.eclipse.ui.IEditorInput input) {
        super.init(site, input);
        org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput taskEditorInput = ((org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput) (input));
        this.task = taskEditorInput.getTask();
        this.defaultSelection = new org.eclipse.jface.viewers.StructuredSelection(task);
        this.lastSelection = defaultSelection;
        org.eclipse.ui.handlers.IHandlerService handlerService = ((org.eclipse.ui.handlers.IHandlerService) (getSite().getService(org.eclipse.ui.handlers.IHandlerService.class)));
        this.textSupport = new org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport(handlerService);
        this.textSupport.setSelectionChangedListener(this);
        createFindSupport();
        initModel(taskEditorInput);
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskDataManager().addListener(TASK_DATA_LISTENER);
    }

    private void initModel(org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput input) {
        org.eclipse.core.runtime.Assert.isTrue(model == null);
        try {
            this.model = createModel(input);
            this.connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(getConnectorKind());
            setTaskData(model.getTaskData());
            model.addModelListener(new org.eclipse.mylyn.tasks.core.data.TaskDataModelListener() {
                @java.lang.Override
                public void attributeChanged(org.eclipse.mylyn.tasks.core.data.TaskDataModelEvent event) {
                    org.eclipse.ui.forms.IManagedForm form = getManagedForm();
                    if ((form != null) && (!form.isDirty())) {
                        form.dirtyStateChanged();
                    }
                }
            });
            setNeedsAddToCategory(model.getTaskData().isNew());
        } catch (final org.eclipse.core.runtime.CoreException e) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Error opening task", e));// $NON-NLS-1$

            getTaskEditor().setStatus(org.eclipse.mylyn.internal.tasks.ui.editors.Messages.AbstractTaskEditorPage_Error_opening_task, org.eclipse.mylyn.internal.tasks.ui.editors.Messages.AbstractTaskEditorPage_Open_failed, e.getStatus());
        }
    }

    private void initializePart(org.eclipse.swt.widgets.Composite parent, org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart part, java.util.Collection<org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor> descriptors) {
        getManagedForm().addPart(part);
        part.initialize(this);
        if (part instanceof org.eclipse.mylyn.internal.tasks.ui.editors.AbstractTaskEditorSection) {
            createSubParts(((org.eclipse.mylyn.internal.tasks.ui.editors.AbstractTaskEditorSection) (part)), descriptors);
        }
        if (parent != null) {
            part.createControl(parent, toolkit);
            if (part.getControl() != null) {
                if (org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.ID_PART_ACTIONS.equals(part.getPartId())) {
                    // do not expand horizontally
                    org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.FILL).grab(false, false).applyTo(part.getControl());
                } else if (part.getExpandVertically()) {
                    org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.FILL).grab(true, true).applyTo(part.getControl());
                } else {
                    org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.TOP).grab(true, false).applyTo(part.getControl());
                }
                // for outline
                if (org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.ID_PART_COMMENTS.equals(part.getPartId())) {
                    org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.setMarker(part.getControl(), org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode.LABEL_COMMENTS);
                } else if (org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.ID_PART_ATTACHMENTS.equals(part.getPartId())) {
                    org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.setMarker(part.getControl(), org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode.LABEL_ATTACHMENTS);
                }
            }
        }
    }

    /**
     * Subclasses may override to disable the task editor find functionality.
     *
     * @since 3.11
     */
    protected void createFindSupport() {
        this.findSupport = new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorFindSupport(this);
    }

    @java.lang.Override
    public boolean isDirty() {
        return ((getModel() != null) && getModel().isDirty()) || ((getManagedForm() != null) && getManagedForm().isDirty());
    }

    @java.lang.Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    public boolean needsAddToCategory() {
        return needsAddToCategory;
    }

    /**
     * Force a re-layout of entire form.
     */
    public void reflow() {
        if (reflow) {
            try {
                form.setRedraw(false);
                // help the layout managers: ensure that the form width always matches
                // the parent client area width.
                org.eclipse.swt.graphics.Rectangle parentClientArea = form.getParent().getClientArea();
                org.eclipse.swt.graphics.Point formSize = form.getSize();
                if (formSize.x != parentClientArea.width) {
                    org.eclipse.swt.widgets.ScrollBar verticalBar = form.getVerticalBar();
                    int verticalBarWidth = (verticalBar != null) ? verticalBar.getSize().x : 15;
                    form.setSize(parentClientArea.width - verticalBarWidth, formSize.y);
                }
                form.layout(true, false);
                form.reflow(true);
            } finally {
                form.setRedraw(true);
            }
        }
    }

    /**
     * Updates the editor contents in place.
     *
     * @deprecated Use {@link #refresh()} instead
     */
    @java.lang.Deprecated
    public void refreshFormContent() {
        refresh();
    }

    /**
     * Updates the editor contents in place.
     */
    @java.lang.Override
    public void refresh() {
        if ((getManagedForm() == null) || getManagedForm().getForm().isDisposed()) {
            // editor possibly closed or page has not been initialized
            return;
        }
        try {
            showEditorBusy(true);
            boolean hasIncoming = false;
            if (getTask() != null) {
                hasIncoming = getTask().getSynchronizationState().isIncoming();
            }
            if (model != null) {
                doSave(new org.eclipse.core.runtime.NullProgressMonitor());
                refreshInput();
            } else {
                initModel(getTaskEditor().getTaskEditorInput());
            }
            if (taskData != null) {
                try {
                    setReflow(false);
                    // prevent menu from being disposed when disposing control on the form during refresh
                    org.eclipse.swt.widgets.Menu menu = editorComposite.getMenu();
                    org.eclipse.mylyn.commons.ui.CommonUiUtil.setMenu(editorComposite, null);
                    // clear old controls and parts
                    for (org.eclipse.swt.widgets.Control control : editorComposite.getChildren()) {
                        control.dispose();
                    }
                    if (focusTracker != null) {
                        focusTracker.reset();
                    }
                    lastSelection = null;
                    for (org.eclipse.ui.forms.IFormPart part : getManagedForm().getParts()) {
                        part.dispose();
                        getManagedForm().removePart(part);
                    }
                    // restore menu
                    editorComposite.setMenu(menu);
                    createFormContentInternal();
                    getTaskEditor().setMessage(null, 0);
                    if (hasIncoming) {
                        getTaskEditor().setActivePage(getId());
                    }
                    setSubmitEnabled(true);
                } finally {
                    setReflow(true);
                }
            }
            updateOutlinePage();
            updateHeaderMessage();
            getManagedForm().dirtyStateChanged();
            getTaskEditor().updateHeaderToolBar();
        } finally {
            showEditorBusy(false);
        }
        reflow();
    }

    private void refreshInput() {
        try {
            refreshDisabled = true;
            model.refresh(null);
        } catch (org.eclipse.core.runtime.CoreException e) {
            getTaskEditor().setMessage(org.eclipse.mylyn.internal.tasks.ui.editors.Messages.AbstractTaskEditorPage_Failed_to_read_task_data_ + e.getMessage(), org.eclipse.jface.dialogs.IMessageProvider.ERROR);
            taskData = null;
            return;
        } finally {
            refreshDisabled = false;
        }
        setTaskData(model.getTaskData());
    }

    /**
     * Registers a drop listener for <code>control</code>. The default implementation registers a listener for attaching
     * files. Does nothing if the editor is showing a new task.
     * <p>
     * Clients may override.
     * </p>
     *
     * @param control
     * 		the control to register the listener for
     */
    public void registerDefaultDropListener(final org.eclipse.swt.widgets.Control control) {
        if ((getModel() == null) || getModel().getTaskData().isNew()) {
            return;
        }
        org.eclipse.swt.dnd.DropTarget target = new org.eclipse.swt.dnd.DropTarget(control, org.eclipse.swt.dnd.DND.DROP_COPY | org.eclipse.swt.dnd.DND.DROP_DEFAULT);
        org.eclipse.jface.util.LocalSelectionTransfer localSelectionTransfer = org.eclipse.jface.util.LocalSelectionTransfer.getTransfer();
        final org.eclipse.swt.dnd.TextTransfer textTransfer = org.eclipse.swt.dnd.TextTransfer.getInstance();
        final org.eclipse.swt.dnd.FileTransfer fileTransfer = org.eclipse.swt.dnd.FileTransfer.getInstance();
        org.eclipse.swt.dnd.Transfer[] types = new org.eclipse.swt.dnd.Transfer[]{ localSelectionTransfer, textTransfer, fileTransfer };
        target.setTransfer(types);
        if (defaultDropListener == null) {
            defaultDropListener = new org.eclipse.mylyn.internal.tasks.ui.editors.TaskAttachmentDropListener(this);
        }
        target.addDropListener(defaultDropListener);
    }

    public void removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener listener) {
        selectionChangedListeners.remove(listener);
    }

    public void selectionChanged(java.lang.Object element) {
        selectionChanged(new org.eclipse.jface.viewers.SelectionChangedEvent(this, new org.eclipse.jface.viewers.StructuredSelection(element)));
    }

    public void selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent event) {
        org.eclipse.jface.viewers.ISelection selection = event.getSelection();
        if (selection instanceof org.eclipse.jface.text.TextSelection) {
            // only update global actions
            ((org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionContributor) (getEditorSite().getActionBarContributor())).updateSelectableActions(event.getSelection());
            return;
        }
        if (selection.isEmpty()) {
            // something was unselected, reset to default selection
            selection = defaultSelection;
            // XXX a styled text widget has lost focus, re-enable all edit actions
            ((org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionContributor) (getEditorSite().getActionBarContributor())).forceActionsEnabled();
        }
        if (!selection.equals(lastSelection)) {
            this.lastSelection = selection;
            fireSelectionChanged(lastSelection);
            getSite().getSelectionProvider().setSelection(selection);
        }
    }

    @java.lang.Override
    public void setFocus() {
        if ((focusTracker != null) && focusTracker.setFocus()) {
            return;
        } else {
            org.eclipse.ui.forms.IFormPart[] parts = getManagedForm().getParts();
            if (parts.length > 0) {
                parts[0].setFocus();
                return;
            }
        }
        super.setFocus();
    }

    public void setNeedsAddToCategory(boolean needsAddToCategory) {
        this.needsAddToCategory = needsAddToCategory;
    }

    public void setReflow(boolean reflow) {
        this.reflow = reflow;
        form.setRedraw(reflow);
    }

    public void setSelection(org.eclipse.jface.viewers.ISelection selection) {
        org.eclipse.ui.forms.IFormPart[] parts = getManagedForm().getParts();
        for (org.eclipse.ui.forms.IFormPart formPart : parts) {
            if (formPart instanceof org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart) {
                if (((org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart) (formPart)).setSelection(selection)) {
                    lastSelection = selection;
                    return;
                }
            }
        }
    }

    // TODO EDITOR this needs to be tracked somewhere else
    private void setSubmitEnabled(boolean enabled) {
        org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart actionPart = getPart(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.ID_PART_ACTIONS);
        if (actionPart instanceof org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionPart) {
            ((org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionPart) (actionPart)).setSubmitEnabled(enabled);
        }
        if ((submitButton != null) && (!submitButton.isDisposed())) {
            submitButton.setEnabled(enabled);
        }
        submitEnabled = enabled;
    }

    private void setTaskData(org.eclipse.mylyn.tasks.core.data.TaskData taskData) {
        this.taskData = taskData;
    }

    @java.lang.Override
    public void showBusy(boolean busy) {
        if (((getManagedForm() != null) && (!getManagedForm().getForm().isDisposed())) && (this.busy != busy)) {
            setSubmitEnabled(!busy);
            org.eclipse.mylyn.commons.ui.CommonUiUtil.setEnabled(editorComposite, !busy);
            this.busy = busy;
        }
    }

    // TODO m4.0 remove
    public void showEditorBusy(boolean busy) {
        getTaskEditor().showBusy(busy);
        refreshDisabled = busy;
    }

    private void updateHeaderMessage() {
        if (taskData == null) {
            getTaskEditor().setMessage(org.eclipse.mylyn.internal.tasks.ui.editors.Messages.AbstractTaskEditorPage_Synchronize_to_retrieve_task_data, org.eclipse.jface.dialogs.IMessageProvider.WARNING, new org.eclipse.ui.forms.events.HyperlinkAdapter() {
                @java.lang.Override
                public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                    if (synchronizeEditorAction != null) {
                        synchronizeEditorAction.run();
                    }
                }
            });
        }
        if ((getTaskEditor().getMessage() == null) && (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().getTask(task.getRepositoryUrl(), task.getTaskId()) == null)) {
            getTaskEditor().setMessage(org.eclipse.mylyn.internal.tasks.ui.editors.Messages.AbstractTaskEditorPage_Add_task_to_tasklist, org.eclipse.jface.dialogs.IMessageProvider.INFORMATION, new org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage.NotInTaskListListener());
        }
    }

    @java.lang.Override
    public org.eclipse.swt.widgets.Control getPartControl() {
        return partControl != null ? partControl : super.getPartControl();
    }

    /**
     * Returns true, if the page has an always visible footer.
     *
     * @see #setNeedsFooter(boolean)
     */
    private boolean needsFooter() {
        return needsFooter;
    }

    /**
     * Specifies that the page should provide an always visible footer. This flag is not set by default.
     *
     * @see #createFooterContent(Composite)
     * @see #needsFooter()
     */
    @java.lang.SuppressWarnings("unused")
    private void setNeedsFooter(boolean needsFooter) {
        this.needsFooter = needsFooter;
    }

    private void createFooterContent(org.eclipse.swt.widgets.Composite parent) {
        parent.setLayout(new org.eclipse.swt.layout.GridLayout());
        // submitButton = toolkit.createButton(parent, Messages.TaskEditorActionPart_Submit, SWT.NONE);
        // GridData submitButtonData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        // submitButtonData.widthHint = 100;
        // submitButton.setBackground(null);
        // submitButton.setImage(CommonImages.getImage(TasksUiImages.REPOSITORY_SUBMIT));
        // submitButton.setLayoutData(submitButtonData);
        // submitButton.addListener(SWT.Selection, new Listener() {
        // public void handleEvent(Event e) {
        // doSubmit();
        // }
        // });
    }

    /**
     * Returns true, if the page supports a submit operation.
     *
     * @since 3.2
     * @see #setNeedsSubmit(boolean)
     */
    public boolean needsSubmit() {
        return needsSubmit;
    }

    /**
     * Specifies that the page supports the submit operation. This flag is set to true by default.
     *
     * @since 3.2
     * @see #needsSubmit()
     * @see #doSubmit()
     */
    public void setNeedsSubmit(boolean needsSubmit) {
        this.needsSubmit = needsSubmit;
    }

    /**
     * Returns true, if the page provides a submit button.
     *
     * @since 3.2
     * @see #setNeedsSubmitButton(boolean)
     */
    public boolean needsSubmitButton() {
        return needsSubmitButton;
    }

    /**
     * Specifies that the page supports submitting. This flag is set to false by default.
     *
     * @since 3.2
     * @see #needsSubmitButton()
     */
    public void setNeedsSubmitButton(boolean needsSubmitButton) {
        this.needsSubmitButton = needsSubmitButton;
    }

    /**
     * Returns true, if the page provides a submit button.
     *
     * @since 3.2
     * @see #setNeedsPrivateSection(boolean)
     */
    public boolean needsPrivateSection() {
        return needsPrivateSection;
    }

    /**
     * Specifies that the page should provide the private section. This flag is not set by default.
     *
     * @since 3.2
     * @see #needsPrivateSection()
     */
    public void setNeedsPrivateSection(boolean needsPrivateSection) {
        this.needsPrivateSection = needsPrivateSection;
    }

    // private void fillLeftHeaderToolBar(IToolBarManager toolBarManager) {
    // if (needsSubmit()) {
    // ControlContribution submitButtonContribution = new ControlContribution(
    // "org.eclipse.mylyn.tasks.toolbars.submit") { //$NON-NLS-1$
    // @Override
    // protected int computeWidth(Control control) {
    // return super.computeWidth(control) + 5;
    // }
    // 
    // @Override
    // protected Control createControl(Composite parent) {
    // Composite composite = new Composite(parent, SWT.NONE);
    // composite.setBackground(null);
    // GridLayout layout = new GridLayout();
    // layout.marginWidth = 0;
    // layout.marginHeight = 0;
    // layout.marginLeft = 10;
    // composite.setLayout(layout);
    // 
    // submitButton = toolkit.createButton(composite, Messages.TaskEditorActionPart_Submit + " ", SWT.NONE); //$NON-NLS-1$
    // submitButton.setImage(CommonImages.getImage(TasksUiImages.REPOSITORY_SUBMIT));
    // submitButton.setBackground(null);
    // submitButton.addListener(SWT.Selection, new Listener() {
    // public void handleEvent(Event e) {
    // doSubmit();
    // }
    // });
    // GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BOTTOM).applyTo(submitButton);
    // return composite;
    // }
    // };
    // toolBarManager.add(submitButtonContribution);
    // }
    // }
    @java.lang.Override
    public boolean selectReveal(java.lang.Object object) {
        if (object instanceof org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode) {
            org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode node = ((org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode) (object));
            org.eclipse.mylyn.tasks.core.data.TaskAttribute attribute = node.getData();
            if (attribute != null) {
                super.selectReveal(attribute.getId());
            } else {
                org.eclipse.mylyn.tasks.core.data.TaskRelation taskRelation = node.getTaskRelation();
                org.eclipse.mylyn.tasks.core.TaskRepository taskRepository = node.getTaskRepository();
                if ((taskRelation != null) && (taskRepository != null)) {
                    java.lang.String taskID = taskRelation.getTaskId();
                    org.eclipse.mylyn.tasks.ui.TasksUiUtil.openTask(taskRepository, taskID);
                } else {
                    org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.reveal(this.getManagedForm().getForm(), node.getLabel());
                }
                return true;
            }
        }
        return super.selectReveal(object);
    }

    void expandLastComment() {
        if ((getManagedForm() == null) || getManagedForm().getForm().isDisposed()) {
            // editor possibly closed or page has not been initialized
            return;
        }
        if (taskData == null) {
            return;
        }
        java.util.List<org.eclipse.mylyn.tasks.core.data.TaskAttribute> commentAttributes = taskData.getAttributeMapper().getAttributesByType(taskData, org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_COMMENT);
        if (commentAttributes.size() > 0) {
            selectReveal(commentAttributes.get(commentAttributes.size() - 1).getId());
        }
    }
}