/**
 * *****************************************************************************
 * Copyright (c) 2015 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.provisional.tasks.ui.wizards;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.commons.workbench.forms.SectionComposite;
import org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Field;
import org.eclipse.mylyn.internal.tasks.core.TaskTask;
import org.eclipse.mylyn.internal.tasks.ui.notifications.TaskDiffUtil;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.ITaskDataWorkingCopy;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.core.data.TaskDataModelEvent;
import org.eclipse.mylyn.tasks.core.data.TaskDataModelListener;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.AttributeEditorFactory;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositoryQueryPage2;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
@java.lang.SuppressWarnings("nls")
public class RepositoryQuerySchemaPage extends org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositoryQueryPage2 {
    private static final java.lang.String QUERYPAGE_FILTER_ID = "org.eclipse.mylyn.tasks.ui.wizards.QueryPageFilter";

    private static final int LABEL_WIDTH = 110;

    private static final int COLUMN_WIDTH = 140;

    private static final int COLUMN_GAP = 20;

    private static final int MULTI_COLUMN_WIDTH = ((((org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.RepositoryQuerySchemaPage.COLUMN_WIDTH + 5) + org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.RepositoryQuerySchemaPage.COLUMN_GAP) + org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.RepositoryQuerySchemaPage.LABEL_WIDTH) + 5) + org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.RepositoryQuerySchemaPage.COLUMN_WIDTH;

    private static final int MULTI_ROW_HEIGHT = 55;

    protected final org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema schema;

    private final org.eclipse.mylyn.tasks.core.data.TaskData data;

    private final java.util.regex.Pattern URL_PATTERN;

    private org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.QueryPageSearch search;

    private final org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.QueryPageDetails pageDetails;

    private org.eclipse.mylyn.tasks.ui.editors.AttributeEditorFactory factory;

    private org.eclipse.mylyn.commons.workbench.forms.SectionComposite scrolledComposite;

    private org.eclipse.mylyn.tasks.core.data.TaskData targetTaskData;

    protected final java.util.Map<java.lang.String, org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor> editorMap = new java.util.HashMap<java.lang.String, org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor>();

    public RepositoryQuerySchemaPage(java.lang.String pageName, org.eclipse.mylyn.tasks.core.TaskRepository repository, org.eclipse.mylyn.tasks.core.IRepositoryQuery query, org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema schema, org.eclipse.mylyn.tasks.core.data.TaskData data, org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.QueryPageDetails pageDetails) {
        super(pageName, repository, query);
        this.schema = schema;
        this.data = data;
        this.pageDetails = pageDetails;
        setTitle(pageDetails.getPageTitle());
        setDescription(pageDetails.getPageDescription());
        URL_PATTERN = java.util.regex.Pattern.compile(pageDetails.getUrlPattern());
        if (query != null) {
            search = new org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.QueryPageSearch(query.getUrl());
        } else {
            search = new org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.QueryPageSearch();
        }
    }

    @java.lang.Override
    protected void createPageContent(@org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.commons.workbench.forms.SectionComposite parent) {
        this.scrolledComposite = parent;
        org.eclipse.swt.widgets.Composite scrolledBodyComposite = scrolledComposite.getContent();
        org.eclipse.swt.layout.GridLayout layout = new org.eclipse.swt.layout.GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 10;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        scrolledBodyComposite.setLayout(layout);
        org.eclipse.swt.widgets.Composite attributesComposite = new org.eclipse.swt.widgets.Composite(scrolledBodyComposite, org.eclipse.swt.SWT.NONE);
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.FILL).grab(true, true).span(2, 1).applyTo(attributesComposite);
        layout = new org.eclipse.swt.layout.GridLayout(6, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        attributesComposite.setLayout(layout);
        org.eclipse.swt.layout.GridData g = new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL, org.eclipse.swt.layout.GridData.FILL, true, true);
        g.widthHint = 400;
        attributesComposite.setLayoutData(g);
        attributesComposite.setForeground(parent.getDisplay().getSystemColor(org.eclipse.swt.SWT.COLOR_LIST_FOREGROUND));
        org.eclipse.ui.forms.widgets.FormToolkit toolkit = new org.eclipse.ui.forms.widgets.FormToolkit(parent.getDisplay());
        org.eclipse.mylyn.tasks.core.TaskRepository repository = getTaskRepository();
        org.eclipse.mylyn.tasks.core.ITask nTask = new org.eclipse.mylyn.internal.tasks.core.TaskTask(repository.getConnectorKind(), repository.getRepositoryUrl(), data.getTaskId());
        org.eclipse.mylyn.tasks.core.data.ITaskDataWorkingCopy workingCopy = org.eclipse.mylyn.tasks.ui.TasksUi.getTaskDataManager().createWorkingCopy(nTask, data);
        final org.eclipse.mylyn.tasks.core.data.TaskDataModel model = new org.eclipse.mylyn.tasks.core.data.TaskDataModel(repository, nTask, workingCopy);
        factory = new org.eclipse.mylyn.tasks.ui.editors.AttributeEditorFactory(model, repository);
        model.addModelListener(new org.eclipse.mylyn.tasks.core.data.TaskDataModelListener() {
            @java.lang.Override
            public void attributeChanged(org.eclipse.mylyn.tasks.core.data.TaskDataModelEvent event) {
                getContainer().updateButtons();
            }
        });
        targetTaskData = workingCopy.getLocalData();
        final org.eclipse.mylyn.tasks.core.data.TaskAttribute target = targetTaskData.getRoot();
        createFieldControls(attributesComposite, toolkit, layout.numColumns, target);
        org.eclipse.swt.graphics.Point p = scrolledBodyComposite.computeSize(org.eclipse.swt.SWT.DEFAULT, org.eclipse.swt.SWT.DEFAULT, true);
        scrolledComposite.setMinSize(p);
    }

    private void createFieldControls(org.eclipse.swt.widgets.Composite attributesComposite, org.eclipse.ui.forms.widgets.FormToolkit toolkit, int columnCount, org.eclipse.mylyn.tasks.core.data.TaskAttribute target) {
        int currentColumn = 1;
        int currentPriority = 0;
        int currentLayoutPriority = 0;
        for (org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Field field : schema.getFields()) {
            org.eclipse.mylyn.tasks.core.data.TaskAttribute dataAttribute = target.getAttribute(field.getKey());
            org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor attributeEditor = factory.createEditor(field.getType(), dataAttribute);
            editorMap.put(dataAttribute.getId(), attributeEditor);
            java.lang.String layoutPriorityString = dataAttribute.getMetaData().getValue("LayoutPriority");
            int layoutPriority = (layoutPriorityString == null) ? -1 : java.lang.Integer.parseInt(layoutPriorityString);
            int priority = (attributeEditor.getLayoutHint() != null) ? attributeEditor.getLayoutHint().getPriority() : org.eclipse.mylyn.tasks.ui.editors.LayoutHint.DEFAULT_PRIORITY;
            // TODO: copied from AbstractTaskEditorAttributeSection.createAttributeControls (only layoutPriority is new)
            if ((priority != currentPriority) || (currentLayoutPriority != layoutPriority)) {
                currentPriority = priority;
                currentLayoutPriority = layoutPriority;
                if (currentColumn > 1) {
                    while (currentColumn <= columnCount) {
                        org.eclipse.swt.widgets.Label l = toolkit.createLabel(attributesComposite, "");// $NON-NLS-1$

                        org.eclipse.swt.layout.GridData gd = org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.LEFT, org.eclipse.swt.SWT.CENTER).hint(0, org.eclipse.swt.SWT.DEFAULT).create();
                        l.setLayoutData(gd);
                        currentColumn++;
                    } 
                    currentColumn = 1;
                }
            }
            if (attributeEditor.hasLabel()) {
                attributeEditor.createLabelControl(attributesComposite, toolkit);
                org.eclipse.swt.widgets.Label label = attributeEditor.getLabelControl();
                label.setBackground(attributesComposite.getBackground());
                label.setForeground(attributesComposite.getForeground());
                java.lang.String text = label.getText();
                java.lang.String shortenText = org.eclipse.mylyn.internal.tasks.ui.notifications.TaskDiffUtil.shortenText(label, text, org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.RepositoryQuerySchemaPage.LABEL_WIDTH);
                label.setText(shortenText);
                if (!text.equals(shortenText)) {
                    label.setToolTipText(text);
                }
                org.eclipse.swt.layout.GridData gd = org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.RIGHT, org.eclipse.swt.SWT.CENTER).grab(true, true).hint(org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.RepositoryQuerySchemaPage.LABEL_WIDTH, org.eclipse.swt.SWT.DEFAULT).create();
                if (currentColumn > 1) {
                    gd.horizontalIndent = org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.RepositoryQuerySchemaPage.COLUMN_GAP;
                    gd.widthHint = org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.RepositoryQuerySchemaPage.LABEL_WIDTH + org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.RepositoryQuerySchemaPage.COLUMN_GAP;
                }
                label.setLayoutData(gd);
                currentColumn++;
            }
            attributeEditor.createControl(attributesComposite, toolkit);
            attributeEditor.getControl().setBackground(attributesComposite.getParent().getDisplay().getSystemColor(org.eclipse.swt.SWT.COLOR_LIST_BACKGROUND));
            attributeEditor.getControl().setForeground(attributesComposite.getForeground());
            org.eclipse.mylyn.tasks.ui.editors.LayoutHint layoutHint = attributeEditor.getLayoutHint();
            org.eclipse.swt.layout.GridData gd = new org.eclipse.swt.layout.GridData(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.FILL, true, true);
            org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan rowSpan = ((layoutHint != null) && (layoutHint.rowSpan != null)) ? layoutHint.rowSpan : org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan.SINGLE;
            org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan columnSpan = ((layoutHint != null) && (layoutHint.columnSpan != null)) ? layoutHint.columnSpan : org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan.SINGLE;
            gd.horizontalIndent = 1;// prevent clipping of decorators on Windows

            if ((rowSpan == org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan.SINGLE) && (columnSpan == org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan.SINGLE)) {
                gd.widthHint = org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.RepositoryQuerySchemaPage.COLUMN_WIDTH;
                gd.horizontalSpan = 1;
            } else {
                if (rowSpan == org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan.MULTIPLE) {
                    gd.heightHint = org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.RepositoryQuerySchemaPage.MULTI_ROW_HEIGHT;
                }
                if (columnSpan == org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan.SINGLE) {
                    gd.widthHint = org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.RepositoryQuerySchemaPage.COLUMN_WIDTH;
                    gd.horizontalSpan = 1;
                } else {
                    gd.widthHint = org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.RepositoryQuerySchemaPage.MULTI_COLUMN_WIDTH;
                    gd.horizontalSpan = (columnCount - currentColumn) + 1;
                }
            }
            attributeEditor.getControl().setLayoutData(gd);
            currentColumn += gd.horizontalSpan;
            currentColumn %= columnCount;
        }
    }

    @java.lang.Override
    public boolean isPageComplete() {
        setMessage(pageDetails.getPageDescription());
        boolean result = super.isPageComplete();
        if (!result) {
            return result;
        }
        setErrorMessage(null);
        setMessage("");
        boolean oneFieldHasValue = false;
        for (org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Field field : schema.getFields()) {
            oneFieldHasValue |= targetTaskData.getRoot().getAttribute(field.getKey()).hasValue() && (!targetTaskData.getRoot().getAttribute(field.getKey()).getValue().equals(""));
            if (field.isQueryRequired()) {
                java.lang.String text = targetTaskData.getRoot().getAttribute(field.getKey()).getValue();
                if ((text == null) || (text.length() == 0)) {
                    setMessage("Enter a value for " + field.getLabel());
                    return false;
                }
            }
            if (field.getType().equals("url")) {
                java.lang.String text = targetTaskData.getRoot().getAttribute(field.getKey()).getValue();
                if ((text != null) && (text.length() > 0)) {
                    java.util.regex.Matcher m = URL_PATTERN.matcher(text);
                    if (m.find()) {
                        setErrorMessage(null);
                        return true;
                    } else {
                        setErrorMessage("Please specify a valid URL in " + field.getLabel());
                        return false;
                    }
                }
            }
        }
        if (!oneFieldHasValue) {
            setErrorMessage("Please fill at least on field!");
        }
        return true;
    }

    protected java.lang.String getQueryUrl(java.lang.String repsitoryUrl) {
        java.lang.StringBuilder sb = new java.lang.StringBuilder();
        sb.append(repsitoryUrl);
        sb.append("/");
        sb.append(pageDetails.getQueryUrlPart());
        sb.append(search.toQuery());
        return sb.toString();
    }

    @java.lang.Override
    public void applyTo(org.eclipse.mylyn.tasks.core.IRepositoryQuery query) {
        query.setSummary(this.getQueryTitle());
        query.setUrl(getQueryUrl(getTaskRepository().getRepositoryUrl()));
        if (pageDetails.getQueryAttributeName() != null) {
            query.setAttribute(pageDetails.getQueryAttributeName(), java.lang.Boolean.TRUE.toString());
        }
    }

    // FIXME: REST überarbeiten
    @java.lang.Override
    protected void doRefreshControls() {
        // ignore
    }

    // FIXME: REST überarbeiten
    @java.lang.Override
    protected boolean hasRepositoryConfiguration() {
        // ignore
        return true;
    }

    // FIXME: REST überarbeiten
    @java.lang.Override
    protected boolean restoreState(@org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.core.IRepositoryQuery query) {
        // ignore
        return false;
    }

    protected org.eclipse.mylyn.tasks.core.data.TaskData getTargetTaskData() {
        return targetTaskData;
    }
}