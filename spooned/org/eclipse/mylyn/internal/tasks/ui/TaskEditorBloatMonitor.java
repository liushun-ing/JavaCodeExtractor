/**
 * *****************************************************************************
 * Copyright (c) 2004, 2012 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
/**
 *
 * @author Mik Kersten
 * @author Steffen Pingel
 */
public class TaskEditorBloatMonitor {
    private static final int MAX_EDITORS = 12;

    public static void editorOpened(org.eclipse.ui.IEditorPart editorPartOpened) {
        org.eclipse.ui.IWorkbenchPage page = editorPartOpened.getSite().getPage();
        java.util.List<org.eclipse.ui.IEditorReference> toClose = new java.util.ArrayList<org.eclipse.ui.IEditorReference>();
        int totalTaskEditors = 0;
        for (org.eclipse.ui.IEditorReference editorReference : page.getEditorReferences()) {
            if (org.eclipse.mylyn.tasks.ui.editors.TaskEditor.ID_EDITOR.equals(editorReference.getId())) {
                totalTaskEditors++;
            }
        }
        if (totalTaskEditors > org.eclipse.mylyn.internal.tasks.ui.TaskEditorBloatMonitor.MAX_EDITORS) {
            for (org.eclipse.ui.IEditorReference editorReference : page.getEditorReferences()) {
                try {
                    if ((editorPartOpened != editorReference.getPart(false)) && org.eclipse.mylyn.tasks.ui.editors.TaskEditor.ID_EDITOR.equals(editorReference.getId())) {
                        org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput taskEditorInput = ((org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput) (editorReference.getEditorInput()));
                        org.eclipse.mylyn.tasks.ui.editors.TaskEditor taskEditor = ((org.eclipse.mylyn.tasks.ui.editors.TaskEditor) (editorReference.getEditor(false)));
                        if (taskEditor == null) {
                            toClose.add(editorReference);
                        } else if ((((!taskEditor.equals(editorPartOpened)) && (!taskEditor.isDirty())) && (taskEditorInput.getTask() != null)) && taskEditorInput.getTask().getSynchronizationState().isSynchronized()) {
                            toClose.add(editorReference);
                        }
                    }
                    if ((totalTaskEditors - toClose.size()) <= org.eclipse.mylyn.internal.tasks.ui.TaskEditorBloatMonitor.MAX_EDITORS) {
                        break;
                    }
                } catch (org.eclipse.ui.PartInitException e) {
                    // ignore
                }
            }
        }
        if (toClose.size() > 0) {
            page.closeEditors(toClose.toArray(new org.eclipse.ui.IEditorReference[toClose.size()]), true);
        }
    }
}