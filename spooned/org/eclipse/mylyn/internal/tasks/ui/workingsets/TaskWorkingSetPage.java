/**
 * *****************************************************************************
 * Copyright (c) 2004, 2013 Eugene Kuleshov and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugene Kuleshov - initial API and implementation
 *     Tasktop Technologies - improvements
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.workingsets;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoryLabelProvider;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TaskElementLabelProvider;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetPage;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchLabelProvider;
/**
 * Adapted from org.eclipse.ui.internal.ide.dialogs.ResourceWorkingSetPage
 *
 * @author Eugene Kuleshov
 * @author Mik Kersten
 * @author David Green fix for bug 274390
 */
public class TaskWorkingSetPage extends org.eclipse.jface.wizard.WizardPage implements org.eclipse.ui.dialogs.IWorkingSetPage {
    private static final int SIZING_SELECTION_WIDGET_WIDTH = 50;

    private static final int SIZING_SELECTION_WIDGET_HEIGHT = 200;

    private org.eclipse.swt.widgets.Text text;

    private org.eclipse.jface.viewers.CheckboxTreeViewer treeViewer;

    private org.eclipse.ui.IWorkingSet workingSet;

    private final org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetPage.WorkingSetPageContentProvider workingSetPageContentProvider = new org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetPage.WorkingSetPageContentProvider();

    private boolean firstCheck = false;

    private final class WorkingSetPageContentProvider implements org.eclipse.jface.viewers.ITreeContentProvider {
        private org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetPage.ElementCategory tasksContainer;

        private org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetPage.ElementCategory resourcesContainer;

        private final java.util.Map<org.eclipse.mylyn.tasks.core.IRepositoryQuery, org.eclipse.mylyn.tasks.core.TaskRepository> queryMap = new java.util.HashMap<org.eclipse.mylyn.tasks.core.IRepositoryQuery, org.eclipse.mylyn.tasks.core.TaskRepository>();

        private final java.util.Map<org.eclipse.core.resources.IProject, org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetPage.TaskRepositoryProjectMapping> projectMap = new java.util.HashMap<org.eclipse.core.resources.IProject, org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetPage.TaskRepositoryProjectMapping>();

        public java.lang.Object[] getChildren(java.lang.Object parentElement) {
            if (parentElement instanceof java.util.List<?>) {
                java.util.List<org.eclipse.core.runtime.IAdaptable> taskRepositoriesContainers = new java.util.ArrayList<org.eclipse.core.runtime.IAdaptable>();
                java.util.List<org.eclipse.core.runtime.IAdaptable> resourcesRepositoriesContainers = new java.util.ArrayList<org.eclipse.core.runtime.IAdaptable>();
                for (org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer category : org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().getCategories()) {
                    taskRepositoriesContainers.add(category);
                }
                org.eclipse.core.resources.IProject[] projects = org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot().getProjects();
                java.util.Set<org.eclipse.core.resources.IProject> unmappedProjects = new java.util.HashSet<org.eclipse.core.resources.IProject>();
                for (java.lang.Object container : ((java.util.List<?>) (parentElement))) {
                    if (container instanceof org.eclipse.mylyn.tasks.core.TaskRepository) {
                        // NOTE: looking down, high complexity
                        if (hasChildren(container)) {
                            taskRepositoriesContainers.add(((org.eclipse.mylyn.tasks.core.TaskRepository) (container)));
                        }
                        // NOTE: O(n^2) complexity, could fix
                        java.util.Set<org.eclipse.core.resources.IProject> mappedProjects = new java.util.HashSet<org.eclipse.core.resources.IProject>();
                        for (org.eclipse.core.resources.IProject project : projects) {
                            org.eclipse.mylyn.tasks.core.TaskRepository taskRepository = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getRepositoryForResource(project);
                            if (container.equals(taskRepository)) {
                                mappedProjects.add(project);
                            } else if (taskRepository == null) {
                                unmappedProjects.add(project);
                            }
                        }
                        if (!mappedProjects.isEmpty()) {
                            org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetPage.TaskRepositoryProjectMapping projectMapping = new org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetPage.TaskRepositoryProjectMapping(((org.eclipse.mylyn.tasks.core.TaskRepository) (container)), mappedProjects);
                            resourcesRepositoriesContainers.add(projectMapping);
                            for (org.eclipse.core.resources.IProject mappedProject : mappedProjects) {
                                projectMap.put(mappedProject, projectMapping);
                            }
                        }
                    }
                }
                resourcesRepositoriesContainers.addAll(unmappedProjects);
                tasksContainer = new org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetPage.ElementCategory(Messages.TaskWorkingSetPage_Tasks, taskRepositoriesContainers);
                resourcesContainer = new org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetPage.ElementCategory(Messages.TaskWorkingSetPage_Resources, resourcesRepositoriesContainers);
                return new java.lang.Object[]{ tasksContainer, resourcesContainer };
            } else if (parentElement instanceof org.eclipse.mylyn.tasks.core.TaskRepository) {
                java.util.List<org.eclipse.core.runtime.IAdaptable> taskContainers = new java.util.ArrayList<org.eclipse.core.runtime.IAdaptable>();
                for (org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer element : org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().getRepositoryQueries(((org.eclipse.mylyn.tasks.core.TaskRepository) (parentElement)).getRepositoryUrl())) {
                    if (element instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery) {
                        taskContainers.add(element);
                        queryMap.put(((org.eclipse.mylyn.tasks.core.IRepositoryQuery) (element)), ((org.eclipse.mylyn.tasks.core.TaskRepository) (parentElement)));
                    }
                }
                return taskContainers.toArray();
            } else if (parentElement instanceof org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetPage.TaskRepositoryProjectMapping) {
                return ((org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetPage.TaskRepositoryProjectMapping) (parentElement)).getProjects().toArray();
            } else if (parentElement instanceof org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetPage.ElementCategory) {
                return ((org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetPage.ElementCategory) (parentElement)).getChildren(parentElement);
            } else {
                return new java.lang.Object[0];
            }
        }

        public boolean hasChildren(java.lang.Object element) {
            return getChildren(element).length > 0;
        }

        public java.lang.Object[] getElements(java.lang.Object element) {
            return getChildren(element);
        }

        public java.lang.Object getParent(java.lang.Object element) {
            if ((element instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory) || (element instanceof org.eclipse.mylyn.tasks.core.TaskRepository)) {
                return tasksContainer;
            } else if (element instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery) {
                return queryMap.get(element);
            } else if (element instanceof org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetPage.TaskRepositoryProjectMapping) {
                return resourcesContainer;
            } else if (element instanceof org.eclipse.core.resources.IProject) {
                java.lang.Object repository = projectMap.get(element);
                if (repository != null) {
                    return repository;
                } else {
                    return resourcesContainer;
                }
            } else {
                return null;
            }
        }

        public void dispose() {
        }

        public void inputChanged(org.eclipse.jface.viewers.Viewer viewer, java.lang.Object oldInput, java.lang.Object newInput) {
        }
    }

    private class TaskRepositoryProjectMapping extends org.eclipse.core.runtime.PlatformObject {
        private final org.eclipse.mylyn.tasks.core.TaskRepository taskRepository;

        private final java.util.Set<org.eclipse.core.resources.IProject> projects;

        public TaskRepositoryProjectMapping(org.eclipse.mylyn.tasks.core.TaskRepository taskRepository, java.util.Set<org.eclipse.core.resources.IProject> mappedProjects) {
            this.taskRepository = taskRepository;
            this.projects = mappedProjects;
        }

        public java.util.Set<org.eclipse.core.resources.IProject> getProjects() {
            return projects;
        }

        public org.eclipse.mylyn.tasks.core.TaskRepository getTaskRepository() {
            return taskRepository;
        }
    }

    class ElementCategory extends org.eclipse.core.runtime.PlatformObject implements org.eclipse.ui.model.IWorkbenchAdapter {
        private final java.lang.String label;

        private final java.util.List<org.eclipse.core.runtime.IAdaptable> children;

        public ElementCategory(java.lang.String label, java.util.List<org.eclipse.core.runtime.IAdaptable> children) {
            this.label = label;
            this.children = children;
        }

        public java.lang.Object[] getChildren(java.lang.Object o) {
            return children.toArray();
        }

        public org.eclipse.jface.resource.ImageDescriptor getImageDescriptor(java.lang.Object object) {
            return org.eclipse.ui.internal.WorkbenchImages.getImageDescriptor(org.eclipse.ui.internal.IWorkbenchGraphicConstants.IMG_OBJ_WORKING_SETS);
        }

        public java.lang.String getLabel(java.lang.Object o) {
            return label;
        }

        public java.lang.Object getParent(java.lang.Object o) {
            return null;
        }
    }

    class AggregateLabelProvider implements org.eclipse.jface.viewers.ILabelProvider {
        private final org.eclipse.mylyn.tasks.ui.TaskElementLabelProvider taskLabelProvider = new org.eclipse.mylyn.tasks.ui.TaskElementLabelProvider(false);

        private final org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoryLabelProvider taskRepositoryLabelProvider = new org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoryLabelProvider();

        private final org.eclipse.ui.model.WorkbenchLabelProvider workbenchLabelProvider = new org.eclipse.ui.model.WorkbenchLabelProvider();

        public org.eclipse.swt.graphics.Image getImage(java.lang.Object element) {
            if (element instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer) {
                return taskLabelProvider.getImage(element);
            } else if (element instanceof org.eclipse.mylyn.tasks.core.TaskRepository) {
                return taskRepositoryLabelProvider.getImage(element);
            } else if (element instanceof org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetPage.TaskRepositoryProjectMapping) {
                return getImage(((org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetPage.TaskRepositoryProjectMapping) (element)).getTaskRepository());
            } else {
                return workbenchLabelProvider.getImage(element);
            }
        }

        public java.lang.String getText(java.lang.Object element) {
            if (element instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer) {
                return taskLabelProvider.getText(element);
            } else if (element instanceof org.eclipse.mylyn.tasks.core.TaskRepository) {
                return taskRepositoryLabelProvider.getText(element);
            } else if (element instanceof org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetPage.TaskRepositoryProjectMapping) {
                return getText(((org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetPage.TaskRepositoryProjectMapping) (element)).getTaskRepository());
            } else {
                return workbenchLabelProvider.getText(element);
            }
        }

        public void addListener(org.eclipse.jface.viewers.ILabelProviderListener listener) {
        }

        public void dispose() {
        }

        public boolean isLabelProperty(java.lang.Object element, java.lang.String property) {
            return false;
        }

        public void removeListener(org.eclipse.jface.viewers.ILabelProviderListener listener) {
        }
    }

    class CustomSorter extends org.eclipse.jface.viewers.ViewerSorter {
        @java.lang.Override
        public int compare(org.eclipse.jface.viewers.Viewer viewer, java.lang.Object e1, java.lang.Object e2) {
            if ((e1 instanceof org.eclipse.mylyn.tasks.core.TaskRepository) || (e1 instanceof org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetPage.TaskRepositoryProjectMapping)) {
                return -1;
            } else if ((e2 instanceof org.eclipse.mylyn.tasks.core.TaskRepository) || (e2 instanceof org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetPage.TaskRepositoryProjectMapping)) {
                return 1;
            } else if ((e1 instanceof org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetPage.ElementCategory) && ((org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetPage.ElementCategory) (e1)).getLabel(e1).equals(Messages.TaskWorkingSetPage_Tasks)) {
                return -1;
            } else if ((e2 instanceof org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetPage.ElementCategory) && ((org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetPage.ElementCategory) (e1)).getLabel(e1).equals(Messages.TaskWorkingSetPage_Tasks)) {
                return 1;
            } else {
                return super.compare(viewer, e1, e2);
            }
        }
    }

    public TaskWorkingSetPage() {
        super("taskWorkingSetPage", Messages.TaskWorkingSetPage_Select_Working_Set_Elements, null);// $NON-NLS-1$

        setDescription(Messages.TaskWorkingSetPage_Page_Description);
        setImageDescriptor(org.eclipse.mylyn.tasks.ui.TasksUiImages.BANNER_WORKING_SET);
    }

    public void finish() {
        java.lang.Object[] elements = treeViewer.getCheckedElements();
        java.util.Set<org.eclipse.core.runtime.IAdaptable> validElements = new java.util.HashSet<org.eclipse.core.runtime.IAdaptable>();
        for (java.lang.Object element : elements) {
            if ((element instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer) || (element instanceof org.eclipse.core.resources.IProject)) {
                validElements.add(((org.eclipse.core.runtime.IAdaptable) (element)));
            }
        }
        addSpecialContainers(validElements);
        if (workingSet == null) {
            org.eclipse.ui.IWorkingSetManager workingSetManager = org.eclipse.ui.PlatformUI.getWorkbench().getWorkingSetManager();
            workingSet = workingSetManager.createWorkingSet(getWorkingSetName(), validElements.toArray(new org.eclipse.core.runtime.IAdaptable[validElements.size()]));
        } else {
            if (!getWorkingSetName().equals(workingSet.getName())) {
                workingSet.setName(getWorkingSetName());
            }
            workingSet.setElements(validElements.toArray(new org.eclipse.core.runtime.IAdaptable[validElements.size()]));
        }
    }

    private void addSpecialContainers(java.util.Set<org.eclipse.core.runtime.IAdaptable> validElements) {
        java.util.HashSet<org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer> specialContainers = new java.util.HashSet<org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer>();
        for (org.eclipse.core.runtime.IAdaptable element : validElements) {
            if (element instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery) {
                org.eclipse.mylyn.tasks.core.IRepositoryQuery query = ((org.eclipse.mylyn.tasks.core.IRepositoryQuery) (element));
                if (query.getRepositoryUrl() != null) {
                    // Add Unmatched
                    org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer orphansContainer = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().getUnmatchedContainer(query.getRepositoryUrl());
                    if (orphansContainer != null) {
                        specialContainers.add(orphansContainer);
                    }
                    // Add Unsubmitted
                    org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer unsubmittedContainer = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().getUnsubmittedContainer(query.getRepositoryUrl());
                    if (unsubmittedContainer != null) {
                        specialContainers.add(unsubmittedContainer);
                    }
                }
            }
        }
        validElements.addAll(specialContainers);
    }

    public org.eclipse.ui.IWorkingSet getSelection() {
        return workingSet;
    }

    public void setSelection(org.eclipse.ui.IWorkingSet workingSet) {
        this.workingSet = workingSet;
        if ((getShell() != null) && (text != null)) {
            firstCheck = true;
            initializeCheckedState();
            text.setText(workingSet.getName());
        }
    }

    private java.lang.String getWorkingSetName() {
        return text.getText();
    }

    public void createControl(org.eclipse.swt.widgets.Composite parent) {
        initializeDialogUnits(parent);
        org.eclipse.swt.widgets.Composite composite = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NULL);
        org.eclipse.swt.layout.GridLayout layout = new org.eclipse.swt.layout.GridLayout();
        layout.marginWidth = convertHorizontalDLUsToPixels(org.eclipse.jface.dialogs.IDialogConstants.HORIZONTAL_MARGIN);
        layout.marginHeight = convertVerticalDLUsToPixels(org.eclipse.jface.dialogs.IDialogConstants.VERTICAL_MARGIN);
        layout.verticalSpacing = convertVerticalDLUsToPixels(org.eclipse.jface.dialogs.IDialogConstants.VERTICAL_SPACING);
        composite.setLayout(layout);
        composite.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.HORIZONTAL_ALIGN_FILL));
        setControl(composite);
        // PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IIDEHelpContextIds.WORKING_SET_RESOURCE_PAGE);
        org.eclipse.swt.widgets.Label label = new org.eclipse.swt.widgets.Label(composite, org.eclipse.swt.SWT.WRAP);
        label.setText(Messages.TaskWorkingSetPage_Working_set_name);
        label.setLayoutData(new org.eclipse.swt.layout.GridData((org.eclipse.swt.layout.GridData.GRAB_HORIZONTAL | org.eclipse.swt.layout.GridData.HORIZONTAL_ALIGN_FILL) | org.eclipse.swt.layout.GridData.VERTICAL_ALIGN_CENTER));
        text = new org.eclipse.swt.widgets.Text(composite, org.eclipse.swt.SWT.SINGLE | org.eclipse.swt.SWT.BORDER);
        text.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.GRAB_HORIZONTAL | org.eclipse.swt.layout.GridData.HORIZONTAL_ALIGN_FILL));
        text.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
            public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
                validateInput();
            }
        });
        // text.setBackground(FieldAssistColors.getRequiredFieldBackgroundColor(text));
        label = new org.eclipse.swt.widgets.Label(composite, org.eclipse.swt.SWT.WRAP);
        label.setText(Messages.TaskWorkingSetPage_Working_set_contents);
        label.setLayoutData(new org.eclipse.swt.layout.GridData((org.eclipse.swt.layout.GridData.GRAB_HORIZONTAL | org.eclipse.swt.layout.GridData.HORIZONTAL_ALIGN_FILL) | org.eclipse.swt.layout.GridData.VERTICAL_ALIGN_CENTER));
        treeViewer = new org.eclipse.jface.viewers.CheckboxTreeViewer(composite);
        treeViewer.setUseHashlookup(true);
        treeViewer.setContentProvider(workingSetPageContentProvider);
        treeViewer.setLabelProvider(new org.eclipse.jface.viewers.DecoratingLabelProvider(new org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetPage.AggregateLabelProvider(), org.eclipse.ui.PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator()));
        treeViewer.setSorter(new org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetPage.CustomSorter());
        java.util.ArrayList<java.lang.Object> containers = new java.util.ArrayList<java.lang.Object>();
        for (org.eclipse.mylyn.tasks.core.TaskRepository repository : org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getAllRepositories()) {
            containers.add(repository);
        }
        containers.addAll(java.util.Arrays.asList(org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot().getProjects()));
        treeViewer.setInput(containers);
        // tree.setComparator(new ResourceComparator(ResourceComparator.NAME));
        org.eclipse.swt.layout.GridData data = new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_BOTH | org.eclipse.swt.layout.GridData.GRAB_VERTICAL);
        data.heightHint = org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetPage.SIZING_SELECTION_WIDGET_HEIGHT;
        data.widthHint = org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetPage.SIZING_SELECTION_WIDGET_WIDTH;
        treeViewer.getControl().setLayoutData(data);
        treeViewer.addCheckStateListener(new org.eclipse.jface.viewers.ICheckStateListener() {
            public void checkStateChanged(org.eclipse.jface.viewers.CheckStateChangedEvent event) {
                handleCheckStateChange(event);
            }
        });
        // Add select / deselect all buttons for bug 46669
        org.eclipse.swt.widgets.Composite buttonComposite = new org.eclipse.swt.widgets.Composite(composite, org.eclipse.swt.SWT.NONE);
        layout = new org.eclipse.swt.layout.GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(org.eclipse.jface.dialogs.IDialogConstants.HORIZONTAL_SPACING);
        buttonComposite.setLayout(layout);
        buttonComposite.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.HORIZONTAL_ALIGN_FILL));
        org.eclipse.swt.widgets.Button selectAllButton = new org.eclipse.swt.widgets.Button(buttonComposite, org.eclipse.swt.SWT.PUSH);
        selectAllButton.setText(Messages.TaskWorkingSetPage_Select_All);
        selectAllButton.setToolTipText("");// $NON-NLS-1$

        selectAllButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent selectionEvent) {
                treeViewer.setCheckedElements(workingSetPageContentProvider.getElements(treeViewer.getInput()));
                validateInput();
            }
        });
        setButtonLayoutData(selectAllButton);
        org.eclipse.swt.widgets.Button deselectAllButton = new org.eclipse.swt.widgets.Button(buttonComposite, org.eclipse.swt.SWT.PUSH);
        deselectAllButton.setText(Messages.TaskWorkingSetPage_Deselect_All);
        deselectAllButton.setToolTipText("");// $NON-NLS-1$

        deselectAllButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent selectionEvent) {
                treeViewer.setCheckedElements(new java.lang.Object[0]);
                validateInput();
            }
        });
        setButtonLayoutData(deselectAllButton);
        if (workingSet != null) {
            for (java.lang.Object object : workingSet.getElements()) {
                treeViewer.expandToLevel(object, 1);
            }
        } else {
            treeViewer.expandToLevel(2);
        }
        initializeCheckedState();
        if (workingSet != null) {
            text.setText(workingSet.getName());
            treeViewer.getControl().setFocus();
        } else {
            text.setFocus();
        }
        setPageComplete(false);
        org.eclipse.jface.dialogs.Dialog.applyDialogFont(composite);
    }

    private void initializeCheckedState() {
        org.eclipse.swt.custom.BusyIndicator.showWhile(getShell().getDisplay(), new java.lang.Runnable() {
            public void run() {
                java.lang.Object[] items = null;
                if (workingSet != null) {
                    items = workingSet.getElements();
                    if (items != null) {
                        // see bug 191342
                        treeViewer.setCheckedElements(new java.lang.Object[]{  });
                        for (java.lang.Object item : items) {
                            if (item != null) {
                                treeViewer.setChecked(item, true);
                            }
                        }
                    }
                }
            }
        });
    }

    protected void handleCheckStateChange(final org.eclipse.jface.viewers.CheckStateChangedEvent event) {
        org.eclipse.swt.custom.BusyIndicator.showWhile(getShell().getDisplay(), new java.lang.Runnable() {
            public void run() {
                org.eclipse.core.runtime.IAdaptable element = ((org.eclipse.core.runtime.IAdaptable) (event.getElement()));
                handleCheckStateChangeHelper(event, element);
                validateInput();
            }

            private void handleCheckStateChangeHelper(final org.eclipse.jface.viewers.CheckStateChangedEvent event, org.eclipse.core.runtime.IAdaptable element) {
                if ((element instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer) || (element instanceof org.eclipse.core.resources.IProject)) {
                    treeViewer.setGrayed(element, false);
                } else if (element instanceof org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetPage.ElementCategory) {
                    for (java.lang.Object child : ((org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetPage.ElementCategory) (element)).getChildren(null)) {
                        treeViewer.setChecked(child, event.getChecked());
                        if (child instanceof org.eclipse.core.runtime.IAdaptable) {
                            handleCheckStateChangeHelper(event, ((org.eclipse.core.runtime.IAdaptable) (child)));
                        }
                    }
                } else if ((element instanceof org.eclipse.mylyn.tasks.core.TaskRepository) || (element instanceof org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetPage.TaskRepositoryProjectMapping)) {
                    for (java.lang.Object child : workingSetPageContentProvider.getChildren(element)) {
                        treeViewer.setChecked(child, event.getChecked());
                    }
                }
            }
        });
    }

    protected void validateInput() {
        java.lang.String errorMessage = null;
        java.lang.String infoMessage = null;
        java.lang.String newText = text.getText();
        if (!newText.equals(newText.trim())) {
            errorMessage = Messages.TaskWorkingSetPage_The_name_must_not_have_a_leading_or_trailing_whitespace;
        } else if (firstCheck) {
            firstCheck = false;
            return;
        }
        if ("".equals(newText)) {
            // $NON-NLS-1$
            errorMessage = Messages.TaskWorkingSetPage_The_name_must_not_be_empty;
        }
        if ((errorMessage == null) && ((workingSet == null) || (!newText.equals(workingSet.getName())))) {
            org.eclipse.ui.IWorkingSet[] workingSets = org.eclipse.ui.PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSets();
            for (org.eclipse.ui.IWorkingSet workingSet2 : workingSets) {
                if (newText.equals(workingSet2.getName())) {
                    errorMessage = Messages.TaskWorkingSetPage_A_working_set_with_the_same_name_already_exists;
                }
            }
        }
        if (treeViewer.getCheckedElements().length == 0) {
            infoMessage = Messages.TaskWorkingSetPage_No_categories_queries_selected;
        }
        setMessage(infoMessage, org.eclipse.mylyn.internal.tasks.ui.workingsets.INFORMATION);
        setErrorMessage(errorMessage);
        setPageComplete(errorMessage == null);
    }
}