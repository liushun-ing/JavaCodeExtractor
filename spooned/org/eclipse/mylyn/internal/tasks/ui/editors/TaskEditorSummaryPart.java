/**
 * *****************************************************************************
 * Copyright (c) 2004, 2013 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Raphael Ackermann - initial API and implementation, bug 195514
 *     Tasktop Technologies - improvements
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.editors;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.ITask.PriorityLevel;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
/**
 *
 * @author Raphael Ackermann
 * @author Steffen Pingel
 * @author Miles Parker
 */
public class TaskEditorSummaryPart extends org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart {
    private org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor summaryEditor;

    public TaskEditorSummaryPart() {
        setPartName(Messages.TaskEditorSummaryPart_Summary);
    }

    protected void addAttribute(org.eclipse.swt.widgets.Composite composite, org.eclipse.ui.forms.widgets.FormToolkit toolkit, org.eclipse.mylyn.tasks.core.data.TaskAttribute attribute, int indent, boolean showLabel, boolean decorationEnabled, boolean readOnly) {
        org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor editor = createAttributeEditor(attribute);
        if (editor != null) {
            // having editable controls in the header looks odd
            editor.setReadOnly(readOnly);
            editor.setDecorationEnabled(decorationEnabled);
            boolean isPriority = isAttribute(attribute, org.eclipse.mylyn.tasks.core.data.TaskAttribute.PRIORITY);
            if (showLabel && (!isPriority)) {
                editor.createLabelControl(composite, toolkit);
                org.eclipse.jface.layout.GridDataFactory.defaultsFor(editor.getLabelControl()).indent(indent, 0).applyTo(editor.getLabelControl());
            }
            if (isPriority) {
                org.eclipse.mylyn.tasks.core.ITaskMapping mapping = getTaskEditorPage().getConnector().getTaskMapping(getTaskData());
                if (mapping != null) {
                    org.eclipse.mylyn.tasks.core.ITask.PriorityLevel priorityLevel = mapping.getPriorityLevel();
                    if (priorityLevel != null) {
                        org.eclipse.swt.graphics.Image image = org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getPriorityImage(getTaskEditorPage().getTask()));
                        if (image != null) {
                            org.eclipse.swt.widgets.Label label = toolkit.createLabel(composite, null);
                            label.setImage(image);
                            org.eclipse.jface.layout.GridDataFactory.defaultsFor(label).indent(5, -3).applyTo(label);
                        }
                    }
                }
            }
            if (isAttribute(attribute, org.eclipse.mylyn.tasks.core.data.TaskAttribute.DATE_MODIFICATION) && (editor instanceof org.eclipse.mylyn.internal.tasks.ui.editors.DateAttributeEditor)) {
                ((org.eclipse.mylyn.internal.tasks.ui.editors.DateAttributeEditor) (editor)).setShowTime(true);
            }
            editor.createControl(composite, toolkit);
            getTaskEditorPage().getAttributeEditorToolkit().adapt(editor);
        }
    }

    protected org.eclipse.swt.widgets.Control addPriorityAttributeWithIcon(org.eclipse.swt.widgets.Composite composite, org.eclipse.ui.forms.widgets.FormToolkit toolkit, org.eclipse.mylyn.tasks.core.data.TaskAttribute attribute, boolean forceReadOnly) {
        if (attribute != null) {
            // can't be added via a factory because these attributes already have a default editor?
            org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor editor = new org.eclipse.mylyn.internal.tasks.ui.editors.PriorityAttributeEditor(getModel(), attribute);
            if (editor != null) {
                editor.setDecorationEnabled(false);
                if (forceReadOnly) {
                    editor.setReadOnly(true);
                }
                editor.createControl(composite, toolkit);
                getTaskEditorPage().getAttributeEditorToolkit().adapt(editor);
            }
            return editor.getControl();
        } else {
            // some connectors don't have priorities.  in this case we just show no icon.
            // this can't be handled within the attribute editor, as it asserts that the attribute cannot be null
            return null;
        }
    }

    private boolean isAttribute(org.eclipse.mylyn.tasks.core.data.TaskAttribute attribute, java.lang.String id) {
        return attribute.getId().equals(attribute.getTaskData().getAttributeMapper().mapToRepositoryKey(attribute.getParentAttribute(), id));
    }

    private void addSummaryText(org.eclipse.swt.widgets.Composite composite, final org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        org.eclipse.mylyn.tasks.core.data.TaskAttribute summaryAttrib = getTaskData().getRoot().getMappedAttribute(org.eclipse.mylyn.tasks.core.data.TaskAttribute.SUMMARY);
        summaryEditor = createAttributeEditor(summaryAttrib);
        if (summaryEditor != null) {
            if (summaryAttrib.getMetaData().isReadOnly()) {
                summaryEditor.setReadOnly(true);
            }
            if (summaryEditor instanceof org.eclipse.mylyn.internal.tasks.ui.editors.RichTextAttributeEditor) {
                // create composite to hold rounded border
                org.eclipse.swt.widgets.Composite roundedBorder = org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.createBorder(composite, toolkit, !summaryEditor.isReadOnly());
                summaryEditor.createControl(roundedBorder, toolkit);
                org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.setHeaderFontSizeAndStyle(summaryEditor.getControl());
            } else {
                final org.eclipse.swt.widgets.Composite border = toolkit.createComposite(composite);
                org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.BEGINNING).hint(EditorUtil.MAXIMUM_WIDTH, org.eclipse.swt.SWT.DEFAULT).grab(true, false).applyTo(border);
                // leave some padding for the border of the attribute editor
                border.setLayout(org.eclipse.jface.layout.GridLayoutFactory.fillDefaults().margins(1, 4).create());
                summaryEditor.createControl(border, toolkit);
                org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.CENTER).grab(true, false).applyTo(summaryEditor.getControl());
                toolkit.paintBordersFor(border);
            }
            getTaskEditorPage().getAttributeEditorToolkit().adapt(summaryEditor);
        }
    }

    @java.lang.Override
    public void createControl(org.eclipse.swt.widgets.Composite parent, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        org.eclipse.swt.widgets.Composite composite = toolkit.createComposite(parent);
        org.eclipse.swt.layout.GridLayout layout = org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.createSectionClientLayout();
        layout.numColumns = 1;
        layout.marginHeight = 0;
        layout.marginTop = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 3;
        composite.setLayout(layout);
        org.eclipse.mylyn.tasks.core.data.TaskAttribute priorityAttribute = getTaskData().getRoot().getMappedAttribute(org.eclipse.mylyn.tasks.core.data.TaskAttribute.PRIORITY);
        final org.eclipse.swt.widgets.Control priorityEditor = addPriorityAttributeWithIcon(composite, toolkit, priorityAttribute, false);
        if (priorityEditor != null) {
            org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.CENTER, org.eclipse.swt.SWT.CENTER).span(1, 2).applyTo(priorityEditor);
            // forward focus to the summary editor
            priorityEditor.addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
                @java.lang.Override
                public void focusGained(org.eclipse.swt.events.FocusEvent e) {
                    if ((summaryEditor != null) && (summaryEditor.getControl() != null)) {
                        summaryEditor.getControl().setFocus();
                        // only forward it on first view
                        priorityEditor.removeFocusListener(this);
                    }
                }
            });
            layout.numColumns++;
        }
        addSummaryText(composite, toolkit);
        if (java.lang.Boolean.parseBoolean(getModel().getTaskRepository().getProperty(TaskEditorExtensions.REPOSITORY_PROPERTY_AVATAR_SUPPORT))) {
            org.eclipse.mylyn.tasks.core.data.TaskAttribute userAssignedAttribute = getTaskData().getRoot().getMappedAttribute(org.eclipse.mylyn.tasks.core.data.TaskAttribute.USER_ASSIGNED);
            if (userAssignedAttribute != null) {
                org.eclipse.mylyn.internal.tasks.ui.editors.UserAttributeEditor editor = new org.eclipse.mylyn.internal.tasks.ui.editors.UserAttributeEditor(getModel(), userAssignedAttribute);
                editor.createControl(composite, toolkit);
                org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.CENTER, org.eclipse.swt.SWT.CENTER).span(1, 2).indent(0, 2).applyTo(editor.getControl());
                layout.marginRight = 1;
                layout.numColumns++;
            }
        }
        if (needsHeader()) {
            createHeaderLayout(composite, toolkit);
        }
        toolkit.paintBordersFor(composite);
        setControl(composite);
    }

    protected org.eclipse.swt.widgets.Composite createHeaderLayout(org.eclipse.swt.widgets.Composite composite, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        org.eclipse.swt.widgets.Composite headerComposite = toolkit.createComposite(composite);
        org.eclipse.swt.layout.GridLayout layout = new org.eclipse.swt.layout.GridLayout(1, false);
        layout.verticalSpacing = 1;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        headerComposite.setLayout(layout);
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().grab(true, false).applyTo(headerComposite);
        createAttributeEditors(toolkit, headerComposite);
        // ensure layout does not wrap
        layout.numColumns = headerComposite.getChildren().length;
        // ensure that the composite does not show a bunch of blank space
        if (layout.numColumns == 0) {
            layout.numColumns = 1;
            toolkit.createLabel(headerComposite, " ");// $NON-NLS-1$

        }
        return headerComposite;
    }

    protected void createAttributeEditors(org.eclipse.ui.forms.widgets.FormToolkit toolkit, org.eclipse.swt.widgets.Composite headerComposite) {
        org.eclipse.mylyn.tasks.core.data.TaskAttribute statusAtribute = getTaskData().getRoot().getMappedAttribute(org.eclipse.mylyn.tasks.core.data.TaskAttribute.STATUS);
        addAttribute(headerComposite, toolkit, statusAtribute, 0, true, true, true);
        org.eclipse.mylyn.tasks.core.ITaskMapping mapping = getTaskEditorPage().getConnector().getTaskMapping(getTaskData());
        if (((mapping != null) && (mapping.getResolution() != null)) && (mapping.getResolution().length() > 0)) {
            org.eclipse.mylyn.tasks.core.data.TaskAttribute resolutionAtribute = getTaskData().getRoot().getMappedAttribute(org.eclipse.mylyn.tasks.core.data.TaskAttribute.RESOLUTION);
            addAttribute(headerComposite, toolkit, resolutionAtribute, 0, false, false, true);
        }
        org.eclipse.mylyn.tasks.core.data.TaskAttribute dateCreation = getTaskData().getRoot().getMappedAttribute(org.eclipse.mylyn.tasks.core.data.TaskAttribute.DATE_CREATION);
        addAttribute(headerComposite, toolkit, dateCreation, EditorUtil.HEADER_COLUMN_MARGIN, true, false, true);
        org.eclipse.mylyn.tasks.core.data.TaskAttribute dateModified = getTaskData().getRoot().getMappedAttribute(org.eclipse.mylyn.tasks.core.data.TaskAttribute.DATE_MODIFICATION);
        addAttribute(headerComposite, toolkit, dateModified, EditorUtil.HEADER_COLUMN_MARGIN, true, false, true);
        java.util.List<org.eclipse.mylyn.tasks.core.data.TaskAttribute> commentAttributes = getTaskData().getAttributeMapper().getAttributesByType(getTaskData(), org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_COMMENT);
        if (!commentAttributes.isEmpty()) {
            org.eclipse.mylyn.tasks.core.data.TaskAttribute lastComment = commentAttributes.get(commentAttributes.size() - 1);
            if (lastComment.getMappedAttribute(org.eclipse.mylyn.tasks.core.data.TaskAttribute.COMMENT_DATE) != null) {
                addAttribute(headerComposite, toolkit, lastComment.getMappedAttribute(org.eclipse.mylyn.tasks.core.data.TaskAttribute.COMMENT_DATE), EditorUtil.HEADER_COLUMN_MARGIN, true, true, true);
            }
        }
    }

    public boolean needsHeader() {
        return !getTaskData().isNew();
    }

    @java.lang.Override
    public void setFocus() {
        if ((summaryEditor != null) && (summaryEditor.getControl() != null)) {
            summaryEditor.getControl().setFocus();
        }
    }
}