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
 * *****************************************************************************
 */
package org.eclipse.mylyn.tasks.ui.editors;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.mylyn.internal.tasks.ui.editors.BooleanAttributeEditor;
import org.eclipse.mylyn.internal.tasks.ui.editors.DateAttributeEditor;
import org.eclipse.mylyn.internal.tasks.ui.editors.DoubleAttributeEditor;
import org.eclipse.mylyn.internal.tasks.ui.editors.IntegerAttributeEditor;
import org.eclipse.mylyn.internal.tasks.ui.editors.LabelsAttributeEditor;
import org.eclipse.mylyn.internal.tasks.ui.editors.LastCommentedAttributeEditor;
import org.eclipse.mylyn.internal.tasks.ui.editors.LongAttributeEditor;
import org.eclipse.mylyn.internal.tasks.ui.editors.LongTextAttributeEditor;
import org.eclipse.mylyn.internal.tasks.ui.editors.MultiSelectionAttributeEditor;
import org.eclipse.mylyn.internal.tasks.ui.editors.PersonAttributeEditor;
import org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode;
import org.eclipse.mylyn.internal.tasks.ui.editors.RichTextAttributeEditor;
import org.eclipse.mylyn.internal.tasks.ui.editors.SingleSelectionAttributeEditor;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions;
import org.eclipse.mylyn.internal.tasks.ui.editors.TextAttributeEditor;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan;
import org.eclipse.swt.SWT;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.services.IServiceLocator;
/**
 *
 * @since 3.0
 * @author Steffen Pingel
 */
public class AttributeEditorFactory {
    private final org.eclipse.mylyn.tasks.core.data.TaskDataModel model;

    private final org.eclipse.mylyn.tasks.core.TaskRepository taskRepository;

    private final org.eclipse.ui.services.IServiceLocator serviceLocator;

    private org.eclipse.mylyn.tasks.ui.editors.AttributeEditorToolkit editorToolkit;

    public AttributeEditorFactory(@org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.core.data.TaskDataModel model, @org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.core.TaskRepository taskRepository) {
        this(model, taskRepository, null);
    }

    /**
     *
     * @since 3.1
     */
    public AttributeEditorFactory(@org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.core.data.TaskDataModel model, @org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.core.TaskRepository taskRepository, @org.eclipse.jdt.annotation.Nullable
    org.eclipse.ui.services.IServiceLocator serviceLocator) {
        org.eclipse.core.runtime.Assert.isNotNull(model);
        org.eclipse.core.runtime.Assert.isNotNull(taskRepository);
        this.model = model;
        this.taskRepository = taskRepository;
        this.serviceLocator = serviceLocator;
    }

    /**
     *
     * @since 3.1
     */
    @org.eclipse.jdt.annotation.Nullable
    public org.eclipse.mylyn.tasks.ui.editors.AttributeEditorToolkit getEditorToolkit() {
        return editorToolkit;
    }

    /**
     *
     * @since 3.1
     */
    public void setEditorToolkit(@org.eclipse.jdt.annotation.Nullable
    org.eclipse.mylyn.tasks.ui.editors.AttributeEditorToolkit editorToolkit) {
        this.editorToolkit = editorToolkit;
    }

    @org.eclipse.jdt.annotation.NonNull
    public org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor createEditor(@org.eclipse.jdt.annotation.NonNull
    java.lang.String type, @org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute) {
        org.eclipse.core.runtime.Assert.isNotNull(type);
        org.eclipse.core.runtime.Assert.isNotNull(taskAttribute);
        if (org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_BOOLEAN.equals(type)) {
            return new org.eclipse.mylyn.internal.tasks.ui.editors.BooleanAttributeEditor(model, taskAttribute);
        } else if (org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_DATE.equals(type)) {
            return new org.eclipse.mylyn.internal.tasks.ui.editors.DateAttributeEditor(model, taskAttribute);
        } else if (org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_DATETIME.equals(type)) {
            if ((taskAttribute.getParentAttribute() != null) && org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_COMMENT.equals(taskAttribute.getParentAttribute().getMetaData().getType())) {
                org.eclipse.mylyn.internal.tasks.ui.editors.LastCommentedAttributeEditor editor = new org.eclipse.mylyn.internal.tasks.ui.editors.LastCommentedAttributeEditor(model, taskAttribute);
                return editor;
            }
            org.eclipse.mylyn.internal.tasks.ui.editors.DateAttributeEditor editor = new org.eclipse.mylyn.internal.tasks.ui.editors.DateAttributeEditor(model, taskAttribute);
            editor.setShowTime(true);
            return editor;
        } else if (org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_PERSON.equals(type)) {
            return new org.eclipse.mylyn.internal.tasks.ui.editors.PersonAttributeEditor(model, taskAttribute);
        } else if (org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_LONG_RICH_TEXT.equals(type)) {
            org.eclipse.mylyn.internal.tasks.ui.editors.RichTextAttributeEditor editor = null;
            if (serviceLocator != null) {
                org.eclipse.ui.contexts.IContextService contextService = ((org.eclipse.ui.contexts.IContextService) (serviceLocator.getService(org.eclipse.ui.contexts.IContextService.class)));
                if (contextService != null) {
                    org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorExtension extension = org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.getTaskEditorExtension(model.getTaskRepository(), taskAttribute);
                    if (extension != null) {
                        editor = new org.eclipse.mylyn.internal.tasks.ui.editors.RichTextAttributeEditor(model, taskRepository, taskAttribute, org.eclipse.swt.SWT.MULTI, contextService, extension);
                    }
                }
            }
            if (editor == null) {
                editor = new org.eclipse.mylyn.internal.tasks.ui.editors.RichTextAttributeEditor(model, taskRepository, taskAttribute);
            }
            if (editorToolkit != null) {
                editor.setRenderingEngine(editorToolkit.getRenderingEngine(taskAttribute));
            }
            return editor;
        } else if (org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_LONG_TEXT.equals(type)) {
            return new org.eclipse.mylyn.internal.tasks.ui.editors.LongTextAttributeEditor(model, taskAttribute);
        } else if (org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_MULTI_SELECT.equals(type)) {
            return new org.eclipse.mylyn.internal.tasks.ui.editors.MultiSelectionAttributeEditor(model, taskAttribute);
        } else if (org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_SHORT_RICH_TEXT.equals(type)) {
            return new org.eclipse.mylyn.internal.tasks.ui.editors.RichTextAttributeEditor(model, taskRepository, taskAttribute, org.eclipse.swt.SWT.SINGLE);
        } else if (org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_SHORT_TEXT.equals(type)) {
            return new org.eclipse.mylyn.internal.tasks.ui.editors.TextAttributeEditor(model, taskAttribute);
        } else if (org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_SINGLE_SELECT.equals(type)) {
            return new org.eclipse.mylyn.internal.tasks.ui.editors.SingleSelectionAttributeEditor(model, taskAttribute);
        } else if (org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_TASK_DEPENDENCY.equals(type)) {
            org.eclipse.mylyn.internal.tasks.ui.editors.RichTextAttributeEditor editor = new org.eclipse.mylyn.internal.tasks.ui.editors.RichTextAttributeEditor(model, taskRepository, taskAttribute, org.eclipse.swt.SWT.MULTI | org.eclipse.swt.SWT.NO_SCROLL) {
                @java.lang.Override
                public java.lang.String getValue() {
                    return getAttributeMapper().getValueLabel(getTaskAttribute());
                }
            };
            editor.setMode(org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode.TASK_RELATION);
            editor.setLayoutHint(new org.eclipse.mylyn.tasks.ui.editors.LayoutHint(org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan.SINGLE, org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan.SINGLE) {
                @java.lang.Override
                public int getPriority() {
                    return DEFAULT_PRIORITY + 1;
                }
            });
            return editor;
        } else if (org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_URL.equals(type)) {
            org.eclipse.mylyn.internal.tasks.ui.editors.RichTextAttributeEditor editor = new org.eclipse.mylyn.internal.tasks.ui.editors.RichTextAttributeEditor(model, taskRepository, taskAttribute, org.eclipse.swt.SWT.SINGLE);
            editor.setMode(org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode.URL);
            return editor;
        } else if (org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_DOUBLE.equals(type)) {
            return new org.eclipse.mylyn.internal.tasks.ui.editors.DoubleAttributeEditor(model, taskAttribute);
        } else if (org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_INTEGER.equals(type)) {
            return new org.eclipse.mylyn.internal.tasks.ui.editors.IntegerAttributeEditor(model, taskAttribute);
        } else if (org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_LONG.equals(type)) {
            return new org.eclipse.mylyn.internal.tasks.ui.editors.LongAttributeEditor(model, taskAttribute);
        } else if (org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_LABEL.equals(type) || org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_MULTI_LABEL.equals(type)) {
            return new org.eclipse.mylyn.internal.tasks.ui.editors.LabelsAttributeEditor(model, taskAttribute);
        }
        throw new java.lang.IllegalArgumentException(("Unsupported editor type: \"" + type) + "\"");// $NON-NLS-1$ //$NON-NLS-2$

    }
}