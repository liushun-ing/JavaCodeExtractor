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
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.AbstractRenderingEngine;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorExtension;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.forms.widgets.FormToolkit;
/**
 * A text attribute editor that can switch between a editor, preview and source view.
 *
 * @author Steffen Pingel
 * @see RichTextEditor
 */
public class RichTextAttributeEditor extends org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor {
    private final org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor editor;

    protected boolean ignoreNotification;

    protected boolean suppressRefresh;

    public RichTextAttributeEditor(org.eclipse.mylyn.tasks.core.data.TaskDataModel manager, org.eclipse.mylyn.tasks.core.TaskRepository taskRepository, org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute) {
        this(manager, taskRepository, taskAttribute, org.eclipse.swt.SWT.MULTI);
    }

    public RichTextAttributeEditor(org.eclipse.mylyn.tasks.core.data.TaskDataModel manager, org.eclipse.mylyn.tasks.core.TaskRepository taskRepository, org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute, int style) {
        this(manager, taskRepository, taskAttribute, style, null, null);
    }

    public RichTextAttributeEditor(org.eclipse.mylyn.tasks.core.data.TaskDataModel manager, org.eclipse.mylyn.tasks.core.TaskRepository taskRepository, org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute, int style, org.eclipse.ui.contexts.IContextService contextService, org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorExtension extension) {
        super(manager, taskAttribute);
        this.editor = new org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor(taskRepository, style, contextService, extension, getModel().getTask()) {
            @java.lang.Override
            public void valueChanged(java.lang.String value) {
                if (!ignoreNotification) {
                    try {
                        suppressRefresh = true;
                        RichTextAttributeEditor.this.setValue(value);
                    } finally {
                        suppressRefresh = false;
                    }
                }
            }
        };
        this.editor.setReadOnly(isReadOnly());
        if ((style & org.eclipse.swt.SWT.MULTI) != 0) {
            setLayoutHint(new org.eclipse.mylyn.tasks.ui.editors.LayoutHint(org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan.MULTIPLE, org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan.MULTIPLE));
        } else {
            setLayoutHint(new org.eclipse.mylyn.tasks.ui.editors.LayoutHint(org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan.SINGLE, org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan.MULTIPLE));
        }
        setMode(org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode.DEFAULT);
        refresh();
    }

    @java.lang.Override
    public void createControl(org.eclipse.swt.widgets.Composite parent, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        // refresh again, in case the value changed in the mean time, needs to be invoked before control is
        // created otherwise WikiText rendering breaks
        refresh();
        editor.createControl(parent, toolkit);
        editor.getViewer().getTextWidget().setToolTipText(getDescription());
        setControl(editor.getControl());
    }

    public org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor getEditor() {
        return editor;
    }

    public org.eclipse.jface.text.source.SourceViewer getEditorViewer() {
        return editor.getEditorViewer();
    }

    public org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode getMode() {
        return editor.getMode();
    }

    public org.eclipse.mylyn.tasks.ui.editors.AbstractRenderingEngine getRenderingEngine() {
        return editor.getRenderingEngine();
    }

    public java.lang.String getValue() {
        return getAttributeMapper().getValue(getTaskAttribute());
    }

    public org.eclipse.jface.text.source.SourceViewer getViewer() {
        return editor.getViewer();
    }

    public org.eclipse.jface.action.IAction getViewSourceAction() {
        return editor.getViewSourceAction();
    }

    public boolean hasBrowser() {
        return editor.hasBrowser();
    }

    public boolean hasPreview() {
        return editor.hasPreview();
    }

    public boolean isSpellCheckingEnabled() {
        return editor.isSpellCheckingEnabled();
    }

    public void setMode(org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode mode) {
        editor.setMode(mode);
    }

    public void setRenderingEngine(org.eclipse.mylyn.tasks.ui.editors.AbstractRenderingEngine renderingEngine) {
        editor.setRenderingEngine(renderingEngine);
    }

    public void setSpellCheckingEnabled(boolean spellCheckingEnabled) {
        editor.setSpellCheckingEnabled(spellCheckingEnabled);
    }

    public void setValue(java.lang.String value) {
        getAttributeMapper().setValue(getTaskAttribute(), value);
        attributeChanged();
    }

    public void showBrowser() {
        editor.showBrowser();
    }

    public void showDefault() {
        editor.showDefault();
    }

    public void showEditor() {
        editor.showEditor();
    }

    public void showPreview() {
        editor.showPreview();
    }

    @java.lang.Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        if (editor != null) {
            editor.setReadOnly(readOnly);
        }
    }

    @java.lang.Override
    public void refresh() {
        // proceed if the control is null (i.e. has not yet been created); setText will create it
        if ((editor.getControl() == null) || (!editor.getControl().isDisposed())) {
            try {
                ignoreNotification = true;
                editor.setText(getValue());
            } finally {
                ignoreNotification = false;
            }
        }
    }

    @java.lang.Override
    public boolean shouldAutoRefresh() {
        return !suppressRefresh;
    }

    @java.lang.Override
    protected void decorateIncoming(org.eclipse.swt.graphics.Color color) {
        super.decorateIncoming(color);
        if (editor != null) {
            editor.setBackground(color);
        }
    }

    /**
     * Tries to enable auto toggling of preview (editable on click)
     */
    public void enableAutoTogglePreview() {
        editor.enableAutoTogglePreview();
    }
}