/**
 * *****************************************************************************
 * Copyright (c) 2004, 2014 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Perforce - fixes for bug 318396
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.editors;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil;
import org.eclipse.mylyn.internal.tasks.ui.notifications.TaskDiffUtil;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.views.UpdateRepositoryConfigurationAction;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.sync.TaskJob;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
/**
 * Editor part that shows {@link TaskAttribute}s in a two column layout.
 *
 * @author Steffen Pingel
 * @author Kevin Sawicki
 */
public abstract class AbstractTaskEditorAttributeSection extends org.eclipse.mylyn.internal.tasks.ui.editors.AbstractTaskEditorSection {
    private static final int LABEL_WIDTH = 110;

    private static final int COLUMN_WIDTH = 140;

    private static final int COLUMN_GAP = 20;

    private static final int MULTI_COLUMN_WIDTH = ((((org.eclipse.mylyn.internal.tasks.ui.editors.AbstractTaskEditorAttributeSection.COLUMN_WIDTH + 5) + org.eclipse.mylyn.internal.tasks.ui.editors.AbstractTaskEditorAttributeSection.COLUMN_GAP) + org.eclipse.mylyn.internal.tasks.ui.editors.AbstractTaskEditorAttributeSection.LABEL_WIDTH) + 5) + org.eclipse.mylyn.internal.tasks.ui.editors.AbstractTaskEditorAttributeSection.COLUMN_WIDTH;

    private static final int MULTI_ROW_HEIGHT = 55;

    private java.util.List<org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor> attributeEditors;

    private boolean hasIncoming;

    private org.eclipse.swt.widgets.Composite attributesComposite;

    private boolean needsRefresh;

    public AbstractTaskEditorAttributeSection() {
    }

    @java.lang.Override
    public void createControl(org.eclipse.swt.widgets.Composite parent, final org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        initialize();
        super.createControl(parent, toolkit);
    }

    public boolean hasIncoming() {
        return hasIncoming;
    }

    public void selectReveal(org.eclipse.mylyn.tasks.core.data.TaskAttribute attribute) {
        if (attribute == null) {
            return;
        }
        if (!getSection().isExpanded()) {
            org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil.setExpanded(getSection(), true);
        }
        org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.reveal(getTaskEditorPage().getManagedForm().getForm(), attribute.getId());
    }

    @java.lang.Override
    public boolean setFormInput(java.lang.Object input) {
        if (input instanceof java.lang.String) {
            java.lang.String text = ((java.lang.String) (input));
            java.util.Collection<org.eclipse.mylyn.tasks.core.data.TaskAttribute> attributes = getAttributes();
            for (org.eclipse.mylyn.tasks.core.data.TaskAttribute attribute : attributes) {
                if (text.equals(attribute.getId())) {
                    selectReveal(attribute);
                }
            }
        }
        return super.setFormInput(input);
    }

    private void createAttributeControls(org.eclipse.swt.widgets.Composite attributesComposite, org.eclipse.ui.forms.widgets.FormToolkit toolkit, int columnCount) {
        int currentColumn = 1;
        int currentPriority = 0;
        for (org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor attributeEditor : attributeEditors) {
            int priority = (attributeEditor.getLayoutHint() != null) ? attributeEditor.getLayoutHint().getPriority() : org.eclipse.mylyn.tasks.ui.editors.LayoutHint.DEFAULT_PRIORITY;
            if (priority != currentPriority) {
                currentPriority = priority;
                if (currentColumn > 1) {
                    while (currentColumn <= columnCount) {
                        getManagedForm().getToolkit().createLabel(attributesComposite, "");// $NON-NLS-1$

                        currentColumn++;
                    } 
                    currentColumn = 1;
                }
            }
            if (attributeEditor.hasLabel()) {
                attributeEditor.createLabelControl(attributesComposite, toolkit);
                org.eclipse.swt.widgets.Label label = attributeEditor.getLabelControl();
                java.lang.String text = label.getText();
                java.lang.String shortenText = org.eclipse.mylyn.internal.tasks.ui.notifications.TaskDiffUtil.shortenText(label, text, org.eclipse.mylyn.internal.tasks.ui.editors.AbstractTaskEditorAttributeSection.LABEL_WIDTH);
                label.setText(shortenText);
                if (!text.equals(shortenText)) {
                    label.setToolTipText(text);
                }
                org.eclipse.swt.layout.GridData gd = org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.RIGHT, org.eclipse.swt.SWT.CENTER).hint(org.eclipse.mylyn.internal.tasks.ui.editors.AbstractTaskEditorAttributeSection.LABEL_WIDTH, org.eclipse.swt.SWT.DEFAULT).create();
                if (currentColumn > 1) {
                    gd.horizontalIndent = org.eclipse.mylyn.internal.tasks.ui.editors.AbstractTaskEditorAttributeSection.COLUMN_GAP;
                    gd.widthHint = org.eclipse.mylyn.internal.tasks.ui.editors.AbstractTaskEditorAttributeSection.LABEL_WIDTH + org.eclipse.mylyn.internal.tasks.ui.editors.AbstractTaskEditorAttributeSection.COLUMN_GAP;
                }
                label.setLayoutData(gd);
                currentColumn++;
            }
            attributeEditor.createControl(attributesComposite, toolkit);
            org.eclipse.mylyn.tasks.ui.editors.LayoutHint layoutHint = attributeEditor.getLayoutHint();
            org.eclipse.swt.layout.GridData gd = new org.eclipse.swt.layout.GridData(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.CENTER, false, false);
            org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan rowSpan = ((layoutHint != null) && (layoutHint.rowSpan != null)) ? layoutHint.rowSpan : org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan.SINGLE;
            org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan columnSpan = ((layoutHint != null) && (layoutHint.columnSpan != null)) ? layoutHint.columnSpan : org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan.SINGLE;
            gd.horizontalIndent = 1;// prevent clipping of decorators on Windows

            if ((rowSpan == org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan.SINGLE) && (columnSpan == org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan.SINGLE)) {
                gd.widthHint = org.eclipse.mylyn.internal.tasks.ui.editors.AbstractTaskEditorAttributeSection.COLUMN_WIDTH;
                gd.horizontalSpan = 1;
            } else {
                if (rowSpan == org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan.MULTIPLE) {
                    gd.heightHint = org.eclipse.mylyn.internal.tasks.ui.editors.AbstractTaskEditorAttributeSection.MULTI_ROW_HEIGHT;
                }
                if (columnSpan == org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan.SINGLE) {
                    gd.widthHint = org.eclipse.mylyn.internal.tasks.ui.editors.AbstractTaskEditorAttributeSection.COLUMN_WIDTH;
                    gd.horizontalSpan = 1;
                } else {
                    gd.widthHint = org.eclipse.mylyn.internal.tasks.ui.editors.AbstractTaskEditorAttributeSection.MULTI_COLUMN_WIDTH;
                    gd.horizontalSpan = (columnCount - currentColumn) + 1;
                }
            }
            attributeEditor.getControl().setLayoutData(gd);
            getTaskEditorPage().getAttributeEditorToolkit().adapt(attributeEditor);
            currentColumn += gd.horizontalSpan;
            currentColumn %= columnCount;
        }
    }

    private void initialize() {
        attributeEditors = new java.util.ArrayList<org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor>();
        hasIncoming = false;
        java.util.Collection<org.eclipse.mylyn.tasks.core.data.TaskAttribute> attributes = getAttributes();
        for (org.eclipse.mylyn.tasks.core.data.TaskAttribute attribute : attributes) {
            org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor attributeEditor = createAttributeEditor(attribute);
            if (attributeEditor != null) {
                attributeEditors.add(attributeEditor);
                if (getModel().hasIncomingChanges(attribute)) {
                    hasIncoming = true;
                }
            }
        }
        java.util.Comparator<org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor> attributeSorter = createAttributeEditorSorter();
        if (attributeSorter != null) {
            java.util.Collections.sort(attributeEditors, attributeSorter);
        }
    }

    /**
     * Create a comparator by which attribute editors will be sorted. By default attribute editors are sorted by layout
     * hint priority. Subclasses may override this method to sort attribute editors in a custom way.
     *
     * @return comparator for {@link AbstractAttributeEditor} objects
     */
    protected java.util.Comparator<org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor> createAttributeEditorSorter() {
        return new java.util.Comparator<org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor>() {
            public int compare(org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor o1, org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor o2) {
                int p1 = (o1.getLayoutHint() != null) ? o1.getLayoutHint().getPriority() : org.eclipse.mylyn.tasks.ui.editors.LayoutHint.DEFAULT_PRIORITY;
                int p2 = (o2.getLayoutHint() != null) ? o2.getLayoutHint().getPriority() : org.eclipse.mylyn.tasks.ui.editors.LayoutHint.DEFAULT_PRIORITY;
                return p1 - p2;
            }
        };
    }

    @java.lang.Override
    protected org.eclipse.swt.widgets.Control createContent(org.eclipse.ui.forms.widgets.FormToolkit toolkit, org.eclipse.swt.widgets.Composite parent) {
        attributesComposite = toolkit.createComposite(parent);
        attributesComposite.addListener(org.eclipse.swt.SWT.MouseDown, new org.eclipse.swt.widgets.Listener() {
            public void handleEvent(org.eclipse.swt.widgets.Event event) {
                org.eclipse.swt.widgets.Control focus = event.display.getFocusControl();
                if ((focus instanceof org.eclipse.swt.widgets.Text) && (((org.eclipse.swt.widgets.Text) (focus)).getEditable() == false)) {
                    getManagedForm().getForm().setFocus();
                }
            }
        });
        org.eclipse.swt.layout.GridLayout attributesLayout = org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.createSectionClientLayout();
        attributesLayout.numColumns = 4;
        attributesLayout.horizontalSpacing = 9;
        attributesLayout.verticalSpacing = 6;
        attributesComposite.setLayout(attributesLayout);
        org.eclipse.swt.layout.GridData attributesData = new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_BOTH);
        attributesData.horizontalSpan = 1;
        attributesData.grabExcessVerticalSpace = false;
        attributesComposite.setLayoutData(attributesData);
        createAttributeControls(attributesComposite, toolkit, attributesLayout.numColumns);
        toolkit.paintBordersFor(attributesComposite);
        return attributesComposite;
    }

    protected org.eclipse.jface.action.IAction doCreateRefreshAction() {
        org.eclipse.mylyn.internal.tasks.ui.views.UpdateRepositoryConfigurationAction repositoryConfigRefresh = new org.eclipse.mylyn.internal.tasks.ui.views.UpdateRepositoryConfigurationAction() {
            @java.lang.Override
            public void run() {
                getTaskEditorPage().showEditorBusy(true);
                final org.eclipse.mylyn.tasks.core.sync.TaskJob job = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getJobFactory().createUpdateRepositoryConfigurationJob(getTaskEditorPage().getConnector(), getTaskEditorPage().getTaskRepository(), getTaskEditorPage().getTask());
                job.addJobChangeListener(new org.eclipse.core.runtime.jobs.JobChangeAdapter() {
                    @java.lang.Override
                    public void done(org.eclipse.core.runtime.jobs.IJobChangeEvent event) {
                        org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
                            public void run() {
                                getTaskEditorPage().showEditorBusy(false);
                                if (job.getStatus() != null) {
                                    getTaskEditorPage().getTaskEditor().setStatus(Messages.TaskEditorAttributePart_Updating_of_repository_configuration_failed, Messages.TaskEditorAttributePart_Update_Failed, job.getStatus());
                                } else {
                                    getTaskEditorPage().refresh();
                                }
                            }
                        });
                    }
                });
                job.setUser(true);
                // show the progress in the system task bar if this is a user job (i.e. forced)
                job.setProperty(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.SHOW_IN_TASKBAR_ICON_PROPERTY, java.lang.Boolean.TRUE);
                job.setPriority(org.eclipse.core.runtime.jobs.Job.INTERACTIVE);
                job.schedule();
            }
        };
        repositoryConfigRefresh.setImageDescriptor(org.eclipse.mylyn.tasks.ui.TasksUiImages.REPOSITORY_SYNCHRONIZE_SMALL);
        repositoryConfigRefresh.selectionChanged(new org.eclipse.jface.viewers.StructuredSelection(getTaskEditorPage().getTaskRepository()));
        repositoryConfigRefresh.setToolTipText(Messages.TaskEditorAttributePart_Refresh_Attributes);
        return repositoryConfigRefresh;
    }

    @java.lang.Override
    protected void fillToolBar(org.eclipse.jface.action.ToolBarManager toolBar) {
        if (needsRefresh()) {
            org.eclipse.jface.action.IAction repositoryConfigRefresh = doCreateRefreshAction();
            toolBar.add(repositoryConfigRefresh);
        }
    }

    /**
     * Returns the list of attributes that are show in the section.
     */
    protected abstract java.util.Collection<org.eclipse.mylyn.tasks.core.data.TaskAttribute> getAttributes();

    @java.lang.Override
    protected java.lang.String getInfoOverlayText() {
        java.lang.StringBuilder sb = new java.lang.StringBuilder();
        java.util.List<org.eclipse.mylyn.tasks.core.data.TaskAttribute> overlayAttributes = getOverlayAttributes();
        for (org.eclipse.mylyn.tasks.core.data.TaskAttribute attribute : overlayAttributes) {
            java.lang.String label = getModel().getTaskData().getAttributeMapper().getValueLabel(attribute);
            if (label != null) {
                if (sb.length() > 0) {
                    sb.append(" / ");// $NON-NLS-1$

                }
            }
            sb.append(label);
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    /**
     * Returns the attributes that are shown in the overlay text.
     *
     * @see #getInfoOverlayText()
     */
    protected abstract java.util.List<org.eclipse.mylyn.tasks.core.data.TaskAttribute> getOverlayAttributes();

    protected boolean needsRefresh() {
        return needsRefresh;
    }

    protected void setNeedsRefresh(boolean needsRefresh) {
        this.needsRefresh = needsRefresh;
    }

    /**
     * Integrator requested the ability to control whether the attributes section is expanded on creation.
     */
    @java.lang.Override
    protected boolean shouldExpandOnCreate() {
        return getTaskData().isNew() || hasIncoming;
    }
}