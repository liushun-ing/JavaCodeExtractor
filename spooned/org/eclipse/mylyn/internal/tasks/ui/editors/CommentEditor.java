/**
 * *****************************************************************************
 * Copyright (c) 2014 Tasktop Technologies and others.
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
import org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorExtension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ActiveShellExpression;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;
public abstract class CommentEditor {
    private org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor textEditor;

    private org.eclipse.ui.contexts.IContextService contextService;

    private org.eclipse.ui.contexts.IContextActivation commentContext;

    private org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport textSupport;

    private final org.eclipse.mylyn.tasks.core.ITask task;

    private final org.eclipse.mylyn.tasks.core.TaskRepository taskRepository;

    public CommentEditor(org.eclipse.mylyn.tasks.core.ITask task, org.eclipse.mylyn.tasks.core.TaskRepository taskRepository) {
        this.task = task;
        this.taskRepository = taskRepository;
    }

    public void createControl(org.eclipse.swt.widgets.Composite composite) {
        org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorExtension extension = org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.getTaskEditorExtension(taskRepository);
        if (extension != null) {
            java.lang.String contextId = extension.getEditorContextId();
            if (contextId != null) {
                contextService = ((org.eclipse.ui.contexts.IContextService) (org.eclipse.ui.PlatformUI.getWorkbench().getService(org.eclipse.ui.contexts.IContextService.class)));
                if (contextService != null) {
                    commentContext = contextService.activateContext(contextId, new org.eclipse.ui.ActiveShellExpression(composite.getShell()));
                }
            }
        }
        textEditor = new org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor(taskRepository, (org.eclipse.swt.SWT.V_SCROLL | org.eclipse.swt.SWT.BORDER) | org.eclipse.swt.SWT.WRAP, contextService, extension, task) {
            @java.lang.Override
            protected void valueChanged(java.lang.String value) {
                CommentEditor.this.valueChanged(value);
            }
        };
        textEditor.createControl(composite, null);
        textEditor.getControl().setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.FILL, true, true, 2, 1));
        org.eclipse.ui.handlers.IHandlerService handlerService = ((org.eclipse.ui.handlers.IHandlerService) (org.eclipse.ui.PlatformUI.getWorkbench().getService(org.eclipse.ui.handlers.IHandlerService.class)));
        if (handlerService != null) {
            textSupport = new org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport(handlerService);
            textSupport.install(textEditor.getViewer(), true);
        }
    }

    protected abstract void valueChanged(java.lang.String value);

    public org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor getTextEditor() {
        return textEditor;
    }

    public java.lang.String getText() {
        return textEditor.getText();
    }

    public void dispose() {
        if ((contextService != null) && (commentContext != null)) {
            contextService.deactivateContext(commentContext);
            commentContext = null;
        }
        if (textSupport != null) {
            textSupport.dispose();
        }
    }
}