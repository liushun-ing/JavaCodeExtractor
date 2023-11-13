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
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.editors;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport;
import org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector;
import org.eclipse.mylyn.internal.tasks.core.LocalTask;
import org.eclipse.mylyn.tasks.core.ITask.PriorityLevel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
/**
 *
 * @author Steffen Pingel
 */
public class SummaryPart extends org.eclipse.mylyn.internal.tasks.ui.editors.AbstractLocalEditorPart {
    private org.eclipse.swt.widgets.Composite headerComposite;

    private org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport textSupport;

    private org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor summaryEditor;

    private org.eclipse.swt.widgets.Button statusCompleteButton;

    private org.eclipse.swt.widgets.Button statusIncompleteButton;

    private org.eclipse.swt.widgets.Text creationDateText;

    private org.eclipse.swt.widgets.Text completionDateText;

    private org.eclipse.mylyn.internal.tasks.ui.editors.PriorityEditor priorityEditor;

    private boolean initialized;

    public SummaryPart() {
        super(Messages.SummaryPart_Section_Title);
    }

    private org.eclipse.swt.widgets.Label createLabel(org.eclipse.swt.widgets.Composite composite, org.eclipse.ui.forms.widgets.FormToolkit toolkit, java.lang.String label, int indent) {
        org.eclipse.swt.widgets.Label labelControl = toolkit.createLabel(composite, label);
        labelControl.setForeground(toolkit.getColors().getColor(org.eclipse.ui.forms.IFormColors.TITLE));
        org.eclipse.jface.layout.GridDataFactory.defaultsFor(labelControl).indent(indent, 0).applyTo(labelControl);
        return labelControl;
    }

    private void createSummaryControl(org.eclipse.swt.widgets.Composite composite, final org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        org.eclipse.swt.widgets.Composite borderComposite = org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.createBorder(composite, toolkit);
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.CENTER).hint(EditorUtil.MAXIMUM_WIDTH, org.eclipse.swt.SWT.DEFAULT).grab(true, false).applyTo(borderComposite);
        summaryEditor = new org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor(getRepository(), org.eclipse.swt.SWT.SINGLE, null, null, getTask());
        summaryEditor.setSpellCheckingEnabled(true);
        summaryEditor.setReadOnly(!isSummaryEditable());
        summaryEditor.createControl(borderComposite, toolkit);
        if (textSupport != null) {
            textSupport.install(summaryEditor.getViewer(), true);
        }
        summaryEditor.getViewer().addTextListener(new org.eclipse.jface.text.ITextListener() {
            public void textChanged(org.eclipse.jface.text.TextEvent event) {
                if (!getTask().getSummary().equals(summaryEditor.getText())) {
                    markDirty(summaryEditor.getControl());
                }
            }
        });
        summaryEditor.getViewer().getControl().setMenu(composite.getMenu());
        org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.setHeaderFontSizeAndStyle(summaryEditor.getControl());
    }

    @java.lang.Override
    public org.eclipse.swt.widgets.Control createControl(org.eclipse.swt.widgets.Composite parent, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        org.eclipse.swt.widgets.Composite composite = toolkit.createComposite(parent);
        org.eclipse.swt.layout.GridLayout layout = org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.createSectionClientLayout();
        layout.numColumns = 2;
        layout.marginTop = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 3;
        composite.setLayout(layout);
        priorityEditor = new org.eclipse.mylyn.internal.tasks.ui.editors.PriorityEditor() {
            @java.lang.Override
            protected void valueChanged(java.lang.String value) {
                priorityEditor.select(value, org.eclipse.mylyn.tasks.core.ITask.PriorityLevel.fromString(value));
                priorityEditor.setToolTipText(value);
                markDirty(priorityEditor.getControl());
            }
        };
        java.util.Map<java.lang.String, java.lang.String> labelByValue = new java.util.LinkedHashMap<java.lang.String, java.lang.String>();
        for (org.eclipse.mylyn.tasks.core.ITask.PriorityLevel level : org.eclipse.mylyn.tasks.core.ITask.PriorityLevel.values()) {
            labelByValue.put(level.toString(), level.getDescription());
        }
        priorityEditor.setLabelByValue(labelByValue);
        priorityEditor.createControl(composite, toolkit);
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.CENTER, org.eclipse.swt.SWT.CENTER).span(1, 2).applyTo(priorityEditor.getControl());
        createSummaryControl(composite, toolkit);
        createHeaderControls(composite, toolkit);
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().grab(true, false).applyTo(headerComposite);
        toolkit.paintBordersFor(composite);
        return composite;
    }

    protected org.eclipse.swt.widgets.Composite createHeaderControls(org.eclipse.swt.widgets.Composite composite, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        headerComposite = toolkit.createComposite(composite);
        org.eclipse.swt.layout.GridLayout layout = new org.eclipse.swt.layout.GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        headerComposite.setLayout(layout);
        createLabel(headerComposite, toolkit, Messages.TaskPlanningEditor_Status, 0);
        statusIncompleteButton = toolkit.createButton(headerComposite, Messages.TaskPlanningEditor_Incomplete, org.eclipse.swt.SWT.RADIO);
        statusIncompleteButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                if (statusIncompleteButton.getSelection()) {
                    statusCompleteButton.setSelection(false);
                    markDirty(statusCompleteButton);
                }
            }
        });
        statusCompleteButton = toolkit.createButton(headerComposite, Messages.TaskPlanningEditor_Complete, org.eclipse.swt.SWT.RADIO);
        statusCompleteButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                if (statusCompleteButton.getSelection()) {
                    statusIncompleteButton.setSelection(false);
                    markDirty(statusCompleteButton);
                }
            }
        });
        // right align controls
        // Composite spacer = toolkit.createComposite(headerComposite, SWT.NONE);
        // GridDataFactory.fillDefaults().hint(0, 10).grab(true, false).applyTo(spacer);
        createLabel(headerComposite, toolkit, getCreatedDateLabel(), EditorUtil.HEADER_COLUMN_MARGIN);
        // do not use toolkit.createText() to avoid border on Windows
        creationDateText = new org.eclipse.swt.widgets.Text(headerComposite, org.eclipse.swt.SWT.FLAT | org.eclipse.swt.SWT.READ_ONLY);
        toolkit.adapt(creationDateText, false, false);
        creationDateText.setData(org.eclipse.ui.forms.widgets.FormToolkit.KEY_DRAW_BORDER, java.lang.Boolean.FALSE);
        createLabel(headerComposite, toolkit, Messages.TaskPlanningEditor_Completed, EditorUtil.HEADER_COLUMN_MARGIN);
        // do not use toolkit.createText() to avoid border on Windows
        completionDateText = new org.eclipse.swt.widgets.Text(headerComposite, org.eclipse.swt.SWT.FLAT | org.eclipse.swt.SWT.READ_ONLY);
        toolkit.adapt(completionDateText, false, false);
        completionDateText.setData(org.eclipse.ui.forms.widgets.FormToolkit.KEY_DRAW_BORDER, java.lang.Boolean.FALSE);
        // ensure layout does not wrap
        layout.numColumns = headerComposite.getChildren().length;
        toolkit.paintBordersFor(headerComposite);
        return headerComposite;
    }

    @java.lang.Override
    public void setFocus() {
        if (summaryEditor != null) {
            summaryEditor.getControl().setFocus();
        }
    }

    private java.lang.String getDateString(java.util.Date date) {
        if (date == null) {
            return "-";// $NON-NLS-1$

        }
        return org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.getDateFormat().format(date);
    }

    private java.lang.String getDateTimeString(java.util.Date date) {
        if (date == null) {
            return "-";// $NON-NLS-1$

        }
        return org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.getDateTimeFormat().format(date);
    }

    @java.lang.Override
    public void refresh(boolean discardChanges) {
        if (shouldRefresh(priorityEditor.getControl(), discardChanges)) {
            org.eclipse.mylyn.tasks.core.ITask.PriorityLevel level = org.eclipse.mylyn.tasks.core.ITask.PriorityLevel.fromString(getTask().getPriority());
            priorityEditor.select(level.toString(), level);
        }
        if (shouldRefresh(statusCompleteButton, discardChanges)) {
            statusIncompleteButton.setSelection(!getTask().isCompleted());
            statusCompleteButton.setSelection(getTask().isCompleted());
        }
        if (shouldRefresh(summaryEditor.getControl(), discardChanges)) {
            summaryEditor.setText(getTask().getSummary());
            if (!initialized) {
                initialized = true;
                if (org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector.DEFAULT_SUMMARY.equals(getTask().getSummary())) {
                    summaryEditor.getViewer().setSelectedRange(0, summaryEditor.getText().length());
                }
            }
        }
        creationDateText.setText(getDateString(getTask().getCreationDate()));
        updateToolTip(creationDateText, getTask().getCreationDate());
        completionDateText.setText(getDateString(getTask().getCompletionDate()));
        updateToolTip(completionDateText, getTask().getCompletionDate());
        // re-layout date fields
        headerComposite.layout(true);
    }

    private void updateToolTip(org.eclipse.swt.widgets.Text text, java.util.Date date) {
        if (date != null) {
            text.setToolTipText(getDateTimeString(date));
        } else {
            text.setToolTipText(null);
        }
    }

    @java.lang.Override
    public void commit(boolean onSave) {
        org.eclipse.mylyn.tasks.core.ITask.PriorityLevel level = org.eclipse.mylyn.tasks.core.ITask.PriorityLevel.fromString(priorityEditor.getValue());
        if (level != null) {
            getTask().setPriority(level.toString());
        }
        clearState(priorityEditor.getControl());
        getTask().setSummary(summaryEditor.getText());
        clearState(summaryEditor.getControl());
        if (statusCompleteButton.getSelection()) {
            if (!getTask().isCompleted()) {
                getTask().setCompletionDate(new java.util.Date());
            }
        } else if (getTask().isCompleted()) {
            getTask().setCompletionDate(null);
        }
        clearState(statusCompleteButton);
        super.commit(onSave);
    }

    public void setTextSupport(org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport textSupport) {
        this.textSupport = textSupport;
    }

    public org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport getTextSupport() {
        return textSupport;
    }

    public void setSummary(java.lang.String value) {
        if (!summaryEditor.getControl().isDisposed()) {
            summaryEditor.setText(value);
        }
    }

    protected java.lang.String getCreatedDateLabel() {
        return Messages.TaskPlanningEditor_Created;
    }

    protected boolean isSummaryEditable() {
        return getTask() instanceof org.eclipse.mylyn.internal.tasks.core.LocalTask;
    }
}