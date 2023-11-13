/**
 * *****************************************************************************
 * Copyright (c) 2004, 2011 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Ken Sueda - initial prototype
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.editors;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.commons.ui.SelectionProviderAdapter;
import org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.ITaskListChangeListener;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.internal.tasks.core.LocalTask;
import org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.actions.NewSubTaskAction;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskActivityListener;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.ITasksUiConstants;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.mylyn.tasks.ui.editors.TaskFormPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.handlers.IHandlerService;
/**
 *
 * @author Mik Kersten
 * @author Rob Elves
 * @author Shawn Minto
 * @author Steffen Pingel
 */
public class TaskPlanningEditor extends org.eclipse.mylyn.tasks.ui.editors.TaskFormPage {
    private org.eclipse.swt.widgets.Composite editorComposite;

    private org.eclipse.mylyn.tasks.core.TaskRepository repository;

    private org.eclipse.mylyn.internal.tasks.core.AbstractTask task;

    private org.eclipse.swt.widgets.Button saveButton;

    private final org.eclipse.ui.IPropertyListener dirtyStateListener = new org.eclipse.ui.IPropertyListener() {
        public void propertyChanged(java.lang.Object source, int propId) {
            if ((propId == org.eclipse.ui.IWorkbenchPartConstants.PROP_DIRTY) && (saveButton != null)) {
                saveButton.setEnabled(getEditor().isDirty());
            }
        }
    };

    private final org.eclipse.mylyn.internal.tasks.core.ITaskListChangeListener taskListChangeListener = new org.eclipse.mylyn.internal.tasks.ui.editors.TaskListChangeAdapter() {
        @java.lang.Override
        public void containersChanged(java.util.Set<org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta> containers) {
            for (org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta taskContainerDelta : containers) {
                if (taskContainerDelta.getElement() instanceof org.eclipse.mylyn.tasks.core.ITask) {
                    final org.eclipse.mylyn.internal.tasks.core.AbstractTask updateTask = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (taskContainerDelta.getElement()));
                    if (((updateTask != null) && (task != null)) && updateTask.getHandleIdentifier().equals(task.getHandleIdentifier())) {
                        if ((org.eclipse.ui.PlatformUI.getWorkbench() != null) && (!org.eclipse.ui.PlatformUI.getWorkbench().isClosing())) {
                            org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
                                public void run() {
                                    refresh();
                                }
                            });
                        }
                        break;
                    }
                }
            }
        }
    };

    private org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport textSupport;

    private org.eclipse.mylyn.tasks.core.ITaskActivityListener timingListener;

    private org.eclipse.ui.forms.widgets.FormToolkit toolkit;

    private org.eclipse.mylyn.internal.tasks.ui.editors.FocusTracker focusTracker;

    public TaskPlanningEditor(org.eclipse.mylyn.tasks.ui.editors.TaskEditor editor) {
        super(editor, org.eclipse.mylyn.tasks.ui.ITasksUiConstants.ID_PAGE_PLANNING, Messages.TaskPlanningEditor_Planning);
        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().addChangeListener(taskListChangeListener);
    }

    private void createContributions(final org.eclipse.swt.widgets.Composite editorComposite) {
        java.util.Collection<org.eclipse.mylyn.internal.tasks.ui.editors.LocalTaskEditorContributionDescriptor> localEditorContributions = org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorContributionExtensionReader.getLocalEditorContributions();
        for (final org.eclipse.mylyn.internal.tasks.ui.editors.LocalTaskEditorContributionDescriptor descriptor : localEditorContributions) {
            org.eclipse.core.runtime.SafeRunner.run(new org.eclipse.core.runtime.ISafeRunnable() {
                public void handleException(java.lang.Throwable e) {
                    org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, ("Error creating task editor contribution: \"" + descriptor.getId()) + "\"", e));// $NON-NLS-1$ //$NON-NLS-2$

                }

                public void run() throws java.lang.Exception {
                    org.eclipse.mylyn.internal.tasks.ui.editors.AbstractLocalEditorPart part = descriptor.createPart();
                    initializePart(editorComposite, part);
                }
            });
        }
    }

    @java.lang.Override
    protected void createFormContent(org.eclipse.ui.forms.IManagedForm managedForm) {
        super.createFormContent(managedForm);
        org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput taskEditorInput = ((org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput) (getEditorInput()));
        task = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (taskEditorInput.getTask()));
        repository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(task.getConnectorKind(), task.getRepositoryUrl());
        toolkit = managedForm.getToolkit();
        editorComposite = managedForm.getForm().getBody();
        org.eclipse.swt.layout.GridLayout layout = new org.eclipse.swt.layout.GridLayout();
        layout.verticalSpacing = 0;
        editorComposite.setLayout(layout);
        if (task instanceof org.eclipse.mylyn.internal.tasks.core.LocalTask) {
            org.eclipse.mylyn.internal.tasks.ui.editors.SummaryPart part = new org.eclipse.mylyn.internal.tasks.ui.editors.SummaryPart();
            part.setTextSupport(textSupport);
            initializePart(editorComposite, part);
            initializePart(editorComposite, new org.eclipse.mylyn.internal.tasks.ui.editors.AttributePart());
            // currently there is only one location for extensions
            createContributions(editorComposite);
        }
        org.eclipse.mylyn.internal.tasks.ui.editors.PlanningPart planningPart = new org.eclipse.mylyn.internal.tasks.ui.editors.PlanningPart(org.eclipse.swt.SWT.NONE);
        // disable due date picker if it's a repository due date
        boolean needsDueDate = true;
        if (task != null) {
            try {
                org.eclipse.mylyn.tasks.core.data.TaskData taskData = org.eclipse.mylyn.tasks.ui.TasksUi.getTaskDataManager().getTaskData(task);
                if (taskData != null) {
                    org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryConnector(taskData.getConnectorKind());
                    org.eclipse.mylyn.tasks.core.TaskRepository taskRepository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(taskData.getConnectorKind(), taskData.getRepositoryUrl());
                    if (((connector != null) && (taskRepository != null)) && connector.hasRepositoryDueDate(taskRepository, task, taskData)) {
                        needsDueDate = false;
                    }
                }
            } catch (org.eclipse.core.runtime.CoreException e) {
                // ignore
            }
        }
        planningPart.initialize(getManagedForm(), repository, task, needsDueDate, this, textSupport);
        planningPart.createControl(editorComposite, toolkit);
        planningPart.getSection().setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_BOTH));
        getManagedForm().addPart(planningPart);
        focusTracker = new org.eclipse.mylyn.internal.tasks.ui.editors.FocusTracker();
        focusTracker.track(editorComposite);
    }

    @java.lang.Override
    public void dispose() {
        getEditor().removePropertyListener(dirtyStateListener);
        if (timingListener != null) {
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().removeActivityListener(timingListener);
        }
        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().removeChangeListener(taskListChangeListener);
        super.dispose();
    }

    @java.lang.Override
    public void doSave(org.eclipse.core.runtime.IProgressMonitor monitor) {
        super.doSave(monitor);
        // update task title
        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().notifyElementChanged(task);
    }

    @java.lang.Override
    public void doSaveAs() {
        // don't support saving as
    }

    /**
     * Override for customizing the tool bar.
     */
    @java.lang.Override
    public void fillToolBar(org.eclipse.jface.action.IToolBarManager toolBarManager) {
        org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput taskEditorInput = ((org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput) (getEditorInput()));
        org.eclipse.mylyn.tasks.core.ITask task = taskEditorInput.getTask();
        if ((task instanceof org.eclipse.mylyn.internal.tasks.core.LocalTask) && (task.getAttribute(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.ATTRIBUTE_OUTGOING_NEW_CONNECTOR_KIND) == null)) {
            org.eclipse.mylyn.internal.tasks.ui.actions.NewSubTaskAction newSubTaskAction = new org.eclipse.mylyn.internal.tasks.ui.actions.NewSubTaskAction();
            newSubTaskAction.selectionChanged(newSubTaskAction, new org.eclipse.jface.viewers.StructuredSelection(task));
            if (newSubTaskAction.isEnabled()) {
                toolBarManager.add(newSubTaskAction);
            }
        }
    }

    @java.lang.Override
    public org.eclipse.mylyn.tasks.ui.editors.TaskEditor getEditor() {
        return ((org.eclipse.mylyn.tasks.ui.editors.TaskEditor) (super.getEditor()));
    }

    @java.lang.Override
    public void init(org.eclipse.ui.IEditorSite site, org.eclipse.ui.IEditorInput input) {
        super.init(site, input);
        this.textSupport = new org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport(((org.eclipse.ui.handlers.IHandlerService) (getSite().getService(org.eclipse.ui.handlers.IHandlerService.class))));
        this.textSupport.setSelectionChangedListener(((org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionContributor) (getEditorSite().getActionBarContributor())));
        site.setSelectionProvider(new org.eclipse.mylyn.commons.ui.SelectionProviderAdapter(new org.eclipse.jface.viewers.StructuredSelection(((org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput) (input)).getTask())));
    }

    private void initializePart(final org.eclipse.swt.widgets.Composite editorComposite, final org.eclipse.mylyn.internal.tasks.ui.editors.AbstractLocalEditorPart part) {
        part.initialize(getManagedForm(), repository, task);
        org.eclipse.swt.widgets.Control control = part.createControl(editorComposite, toolkit);
        part.setControl(control);
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().grab(true, false).applyTo(control);
        getManagedForm().addPart(part);
    }

    @java.lang.Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @java.lang.Override
    public void refresh() {
        if ((getManagedForm() == null) || getManagedForm().getForm().isDisposed()) {
            // editor possibly closed
            return;
        }
        getEditor().updateHeaderToolBar();
        org.eclipse.ui.forms.IFormPart[] parts = getManagedForm().getParts();
        // refresh will not be invoked unless parts are stale
        for (org.eclipse.ui.forms.IFormPart part : parts) {
            if (part instanceof org.eclipse.mylyn.internal.tasks.ui.editors.AbstractLocalEditorPart) {
                ((org.eclipse.mylyn.internal.tasks.ui.editors.AbstractLocalEditorPart) (part)).refresh(false);
            } else {
                part.refresh();
            }
        }
        getManagedForm().reflow(true);
    }

    public void fillLeftHeaderToolBar(org.eclipse.jface.action.IToolBarManager toolBarManager) {
        if ((getEditorInput() instanceof org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput) && (((org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput) (getEditorInput())).getTask() instanceof org.eclipse.mylyn.internal.tasks.core.LocalTask)) {
            org.eclipse.jface.action.ControlContribution submitButtonContribution = new org.eclipse.jface.action.ControlContribution("org.eclipse.mylyn.tasks.toolbars.save") {
                // $NON-NLS-1$
                @java.lang.Override
                protected org.eclipse.swt.widgets.Control createControl(org.eclipse.swt.widgets.Composite parent) {
                    saveButton = new org.eclipse.swt.widgets.Button(parent, org.eclipse.swt.SWT.FLAT);
                    saveButton.setText(Messages.TaskPlanningEditor_Save);
                    saveButton.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.SAVE));
                    saveButton.setBackground(null);
                    saveButton.addListener(org.eclipse.swt.SWT.Selection, new org.eclipse.swt.widgets.Listener() {
                        public void handleEvent(org.eclipse.swt.widgets.Event e) {
                            doSave(new org.eclipse.core.runtime.NullProgressMonitor());
                        }
                    });
                    saveButton.setEnabled(getEditor().isDirty());
                    return saveButton;
                }
            };
            getEditor().addPropertyListener(dirtyStateListener);
            toolBarManager.add(submitButtonContribution);
        }
    }

    @java.lang.Override
    public void setFocus() {
        if (focusTracker.setFocus()) {
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
}