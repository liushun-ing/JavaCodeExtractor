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
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.commons.ui.SelectionProviderAdapter;
import org.eclipse.mylyn.commons.workbench.WorkbenchActionSupport;
import org.eclipse.mylyn.commons.workbench.WorkbenchActionSupport.WorkbenchActionCallback;
import org.eclipse.mylyn.internal.tasks.ui.actions.TaskEditorActionGroup;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;
/**
 *
 * @author Mik Kersten
 * @author Rob Elves
 * @author Steffen Pingel
 */
public class TaskEditorActionContributor extends org.eclipse.ui.part.MultiPageEditorActionBarContributor implements org.eclipse.jface.viewers.ISelectionChangedListener {
    private final class SelectionProviderAdapterExtension extends org.eclipse.mylyn.commons.ui.SelectionProviderAdapter implements org.eclipse.jface.viewers.ISelectionChangedListener {
        @java.lang.Override
        public org.eclipse.jface.viewers.ISelection getSelection() {
            if ((editor != null) && (editor.getSite().getSelectionProvider() != null)) {
                return editor.getSite().getSelectionProvider().getSelection();
            } else {
                return org.eclipse.jface.viewers.StructuredSelection.EMPTY;
            }
        }

        @java.lang.Override
        public void selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent event) {
            super.selectionChanged(event);
        }
    }

    private class EditorPageCallback extends org.eclipse.mylyn.commons.workbench.WorkbenchActionSupport.WorkbenchActionCallback {
        @java.lang.Override
        public boolean canPerformAction(java.lang.String actionId, org.eclipse.swt.widgets.Control control) {
            org.eclipse.ui.forms.editor.IFormPage activePage = getActivePage();
            if (activePage instanceof org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage) {
                org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage page = ((org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage) (activePage));
                return page.canPerformAction(actionId);
            } else if (activePage != null) {
                org.eclipse.mylyn.commons.workbench.WorkbenchActionSupport.WorkbenchActionCallback callback = ((org.eclipse.mylyn.commons.workbench.WorkbenchActionSupport.WorkbenchActionCallback) (activePage.getAdapter(org.eclipse.mylyn.commons.workbench.WorkbenchActionSupport.WorkbenchActionCallback.class)));
                if (callback != null) {
                    return callback.canPerformAction(actionId, control);
                }
            }
            return super.canPerformAction(actionId, control);
        }

        @java.lang.Override
        public void doAction(java.lang.String actionId, org.eclipse.swt.widgets.Control control) {
            org.eclipse.ui.forms.editor.IFormPage activePage = getActivePage();
            if (activePage instanceof org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage) {
                org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage page = ((org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage) (activePage));
                page.doAction(actionId);
                return;
            } else if (activePage != null) {
                org.eclipse.mylyn.commons.workbench.WorkbenchActionSupport.WorkbenchActionCallback callback = ((org.eclipse.mylyn.commons.workbench.WorkbenchActionSupport.WorkbenchActionCallback) (activePage.getAdapter(org.eclipse.mylyn.commons.workbench.WorkbenchActionSupport.WorkbenchActionCallback.class)));
                if (callback != null) {
                    callback.doAction(actionId, control);
                    return;
                }
            }
            super.doAction(actionId, control);
        }

        @java.lang.Override
        public org.eclipse.swt.widgets.Control getFocusControl() {
            org.eclipse.ui.forms.editor.IFormPage page = getActivePage();
            return page != null ? org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.getFocusControl(page) : null;
        }

        @java.lang.Override
        public org.eclipse.jface.viewers.ISelection getSelection() {
            return selectionProvider.getSelection();
        }
    }

    private org.eclipse.mylyn.tasks.ui.editors.TaskEditor editor;

    private final org.eclipse.mylyn.commons.workbench.WorkbenchActionSupport actionSupport;

    private final org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionContributor.SelectionProviderAdapterExtension selectionProvider;

    private final org.eclipse.mylyn.internal.tasks.ui.actions.TaskEditorActionGroup actionGroup;

    public TaskEditorActionContributor() {
        this.actionSupport = new org.eclipse.mylyn.commons.workbench.WorkbenchActionSupport();
        this.actionSupport.setCallback(new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionContributor.EditorPageCallback());
        this.selectionProvider = new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionContributor.SelectionProviderAdapterExtension();
        this.actionGroup = new org.eclipse.mylyn.internal.tasks.ui.actions.TaskEditorActionGroup(actionSupport);
    }

    public void contextMenuAboutToShow(org.eclipse.jface.action.IMenuManager mng) {
        org.eclipse.ui.forms.editor.IFormPage page = getActivePage();
        boolean addClipboard = (page instanceof org.eclipse.mylyn.internal.tasks.ui.editors.TaskPlanningEditor) || (page instanceof org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage);
        contextMenuAboutToShow(mng, addClipboard);
    }

    public void contextMenuAboutToShow(org.eclipse.jface.action.IMenuManager manager, boolean addClipboard) {
        actionGroup.fillContextMenu(manager, editor, addClipboard);
    }

    @java.lang.Override
    public void contributeToCoolBar(org.eclipse.jface.action.ICoolBarManager cbm) {
    }

    @java.lang.Override
    public void contributeToMenu(org.eclipse.jface.action.IMenuManager mm) {
    }

    @java.lang.Override
    public void contributeToStatusLine(org.eclipse.jface.action.IStatusLineManager slm) {
    }

    @java.lang.Override
    public void contributeToToolBar(org.eclipse.jface.action.IToolBarManager tbm) {
    }

    @java.lang.Override
    public void dispose() {
        actionGroup.setSelectionProvider(null);
    }

    public void forceActionsEnabled() {
        actionSupport.forceEditActionsEnabled();
    }

    private org.eclipse.ui.forms.editor.IFormPage getActivePage() {
        return editor != null ? editor.getActivePageInstance() : null;
    }

    public org.eclipse.mylyn.tasks.ui.editors.TaskEditor getEditor() {
        return editor;
    }

    @java.lang.Override
    public void init(org.eclipse.ui.IActionBars bars, org.eclipse.ui.IWorkbenchPage page) {
        super.init(bars, page);
        actionSupport.install(bars);
        bars.setGlobalActionHandler(org.eclipse.ui.actions.ActionFactory.REFRESH.getId(), actionGroup.getSynchronizeEditorAction());
    }

    public void selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent event) {
        actionSupport.selectionChanged(event);
        actionGroup.getNewTaskFromSelectionAction().selectionChanged(event.getSelection());
    }

    @java.lang.Override
    public void setActiveEditor(org.eclipse.ui.IEditorPart activeEditor) {
        if (this.editor != null) {
            this.editor.getSite().getSelectionProvider().removeSelectionChangedListener(selectionProvider);
        }
        if (activeEditor instanceof org.eclipse.mylyn.tasks.ui.editors.TaskEditor) {
            this.editor = ((org.eclipse.mylyn.tasks.ui.editors.TaskEditor) (activeEditor));
            this.editor.getSite().getSelectionProvider().addSelectionChangedListener(selectionProvider);
            actionGroup.getSynchronizeEditorAction().selectionChanged(new org.eclipse.jface.viewers.StructuredSelection(this.editor));
            updateSelectableActions(selectionProvider.getSelection());
        } else {
            actionGroup.getSynchronizeEditorAction().selectionChanged(org.eclipse.jface.viewers.StructuredSelection.EMPTY);
            this.editor = null;
        }
    }

    @java.lang.Override
    public void setActivePage(org.eclipse.ui.IEditorPart activePage) {
        updateSelectableActions(selectionProvider.getSelection());
    }

    public void updateSelectableActions(org.eclipse.jface.viewers.ISelection selection) {
        if (editor != null) {
            actionSupport.updateActions(selection);
            actionGroup.getNewTaskFromSelectionAction().selectionChanged(selection);
        }
    }
}