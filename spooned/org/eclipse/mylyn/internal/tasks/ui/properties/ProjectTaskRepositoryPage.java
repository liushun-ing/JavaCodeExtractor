/**
 * *****************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.properties;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.actions.AddRepositoryAction;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoriesSorter;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoryLabelProvider;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.internal.dialogs.DialogUtil;
/**
 * A property page that support per project configuration of an associated task repository.
 *
 * @author Rob Elves
 * @see Adapted from org.eclipse.ui.internal.ide.dialogs.ProjectReferencePage
 */
public class ProjectTaskRepositoryPage extends org.eclipse.ui.dialogs.PropertyPage {
    private static final int REPOSITORY_LIST_MULTIPLIER = 30;

    private org.eclipse.core.resources.IProject project;

    private boolean modified = false;

    private org.eclipse.jface.viewers.CheckboxTableViewer listViewer;

    public ProjectTaskRepositoryPage() {
    }

    @java.lang.Override
    protected org.eclipse.swt.widgets.Control createContents(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.graphics.Font font = parent.getFont();
        org.eclipse.swt.widgets.Composite composite = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NONE);
        org.eclipse.swt.layout.GridLayout layout = new org.eclipse.swt.layout.GridLayout();
        composite.setLayout(layout);
        composite.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_HORIZONTAL));
        composite.setFont(font);
        initialize();
        org.eclipse.swt.widgets.Label description = createDescriptionLabel(composite);
        description.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.TOP, true, false));
        listViewer = org.eclipse.jface.viewers.CheckboxTableViewer.newCheckList(composite, org.eclipse.swt.SWT.TOP | org.eclipse.swt.SWT.BORDER);
        listViewer.getTable().setFont(font);
        org.eclipse.swt.layout.GridData data = new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_BOTH);
        data.grabExcessHorizontalSpace = true;
        // Only set a height hint if it will not result in a cut off dialog
        if (org.eclipse.ui.internal.dialogs.DialogUtil.inRegularFontMode(parent)) {
            data.heightHint = org.eclipse.mylyn.internal.tasks.ui.properties.ProjectTaskRepositoryPage.getDefaultFontHeight(listViewer.getTable(), org.eclipse.mylyn.internal.tasks.ui.properties.ProjectTaskRepositoryPage.REPOSITORY_LIST_MULTIPLIER);
        }
        listViewer.getTable().setLayoutData(data);
        listViewer.getTable().setFont(font);
        listViewer.setLabelProvider(new org.eclipse.jface.viewers.DecoratingLabelProvider(new org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoryLabelProvider(), org.eclipse.ui.PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator()));
        listViewer.setContentProvider(new org.eclipse.jface.viewers.IStructuredContentProvider() {
            public void inputChanged(org.eclipse.jface.viewers.Viewer v, java.lang.Object oldInput, java.lang.Object newInput) {
            }

            public void dispose() {
            }

            public java.lang.Object[] getElements(java.lang.Object parent) {
                return org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getAllRepositories().toArray();
            }
        });
        listViewer.setSorter(new org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoriesSorter());
        listViewer.setInput(project.getWorkspace());
        listViewer.addCheckStateListener(new org.eclipse.jface.viewers.ICheckStateListener() {
            public void checkStateChanged(org.eclipse.jface.viewers.CheckStateChangedEvent event) {
                if (event.getChecked()) {
                    // only allow single selection
                    listViewer.setAllChecked(false);
                    listViewer.setChecked(event.getElement(), event.getChecked());
                }
                modified = true;
            }
        });
        updateLinkedRepository();
        // TODO this code was copied from SelectRepositoryPage
        final org.eclipse.mylyn.internal.tasks.ui.actions.AddRepositoryAction action = new org.eclipse.mylyn.internal.tasks.ui.actions.AddRepositoryAction();
        action.setPromptToAddQuery(false);
        org.eclipse.swt.widgets.Button button = new org.eclipse.swt.widgets.Button(composite, org.eclipse.swt.SWT.NONE);
        button.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.HORIZONTAL_ALIGN_BEGINNING | org.eclipse.swt.layout.GridData.VERTICAL_ALIGN_BEGINNING));
        button.setText(org.eclipse.mylyn.internal.tasks.ui.actions.AddRepositoryAction.TITLE);
        button.setEnabled(action.isEnabled());
        button.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                org.eclipse.mylyn.tasks.core.TaskRepository taskRepository = action.showWizard();
                if (taskRepository != null) {
                    listViewer.setInput(project.getWorkspace());
                    listViewer.setSelection(new org.eclipse.jface.viewers.StructuredSelection(taskRepository));
                    updateLinkedRepository();
                }
            }
        });
        return composite;
    }

    void updateLinkedRepository() {
        org.eclipse.mylyn.tasks.core.TaskRepository repository = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getRepositoryForResource(project);
        if (repository != null) {
            listViewer.setCheckedElements(new java.lang.Object[]{ repository });
        }
        listViewer.getControl().setEnabled(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().canSetRepositoryForResource(project));
    }

    private static int getDefaultFontHeight(org.eclipse.swt.widgets.Control control, int lines) {
        org.eclipse.swt.graphics.FontData[] viewerFontData = control.getFont().getFontData();
        int fontHeight = 10;
        // If we have no font data use our guess
        if (viewerFontData.length > 0) {
            fontHeight = viewerFontData[0].getHeight();
        }
        return lines * fontHeight;
    }

    private void initialize() {
        project = ((org.eclipse.core.resources.IProject) (getElement().getAdapter(org.eclipse.core.resources.IResource.class)));
        noDefaultAndApplyButton();
        setDescription(Messages.ProjectTaskRepositoryPage_Select_a_task_repository_to_associate_with_this_project_below);
    }

    @java.lang.Override
    public boolean performOk() {
        if (!modified) {
            return true;
        }
        if (listViewer.getCheckedElements().length > 0) {
            org.eclipse.mylyn.tasks.core.TaskRepository selectedRepository = ((org.eclipse.mylyn.tasks.core.TaskRepository) (listViewer.getCheckedElements()[0]));
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().setRepositoryForResource(project, selectedRepository);
        } else {
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().setRepositoryForResource(project, null);
        }
        return true;
    }
}