/**
 * *****************************************************************************
 * Copyright (c) 2004, 2009 Jingwen Ou and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jingwen Ou - initial API and implementation
 *     Tasktop Technologies - improvements
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.commands;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.jface.action.IAction;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
/**
 *
 * @author Jingwen Ou
 * @author Steffen Pingel
 */
public class ViewSourceHandler extends org.eclipse.core.commands.AbstractHandler implements org.eclipse.ui.commands.IElementUpdater {
    private static boolean checked;

    private static org.eclipse.mylyn.internal.tasks.ui.commands.ViewSourceHandler instance;

    public ViewSourceHandler() {
        org.eclipse.mylyn.internal.tasks.ui.commands.ViewSourceHandler.instance = this;
    }

    public boolean isChecked() {
        return org.eclipse.mylyn.internal.tasks.ui.commands.ViewSourceHandler.checked;
    }

    public static void setChecked(boolean checked) {
        org.eclipse.mylyn.internal.tasks.ui.commands.ViewSourceHandler.checked = checked;
        if (org.eclipse.mylyn.internal.tasks.ui.commands.ViewSourceHandler.instance != null) {
            org.eclipse.mylyn.internal.tasks.ui.commands.ViewSourceHandler.instance.fireHandlerChanged(new org.eclipse.core.commands.HandlerEvent(org.eclipse.mylyn.internal.tasks.ui.commands.ViewSourceHandler.instance, true, false));
        }
    }

    private org.eclipse.swt.widgets.Control getFocusControl() {
        return org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().getFocusControl();
    }

    @java.lang.Override
    public boolean isEnabled() {
        org.eclipse.swt.widgets.Control focusControl = getFocusControl();
        if ((focusControl instanceof org.eclipse.swt.custom.StyledText) && (focusControl.getData(org.eclipse.mylyn.internal.tasks.ui.commands.ViewSourceHandler.VIEW_SOURCE_ACTION) instanceof org.eclipse.jface.action.IAction)) {
            return true;
        }
        return false;
    }

    public static final java.lang.String VIEW_SOURCE_ACTION = "viewSourceAction";// $NON-NLS-1$


    public java.lang.Object execute(org.eclipse.core.commands.ExecutionEvent event) throws org.eclipse.core.commands.ExecutionException {
        org.eclipse.ui.IWorkbenchSite site = org.eclipse.ui.handlers.HandlerUtil.getActiveSite(event);
        if (site instanceof org.eclipse.ui.IEditorSite) {
            org.eclipse.ui.IWorkbenchPart part = ((org.eclipse.ui.IEditorSite) (site)).getPart();
            if (part instanceof org.eclipse.ui.forms.editor.FormEditor) {
                org.eclipse.ui.forms.editor.IFormPage page = ((org.eclipse.ui.forms.editor.FormEditor) (part)).getActivePageInstance();
                org.eclipse.swt.widgets.Control focusedControl = org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.getFocusControl(page);
                if (focusedControl != null) {
                    java.lang.Object data = focusedControl.getData(org.eclipse.mylyn.internal.tasks.ui.commands.ViewSourceHandler.VIEW_SOURCE_ACTION);
                    if (data instanceof org.eclipse.jface.action.IAction) {
                        org.eclipse.jface.action.IAction action = ((org.eclipse.jface.action.IAction) (data));
                        action.setChecked(!action.isChecked());
                        action.run();
                        org.eclipse.mylyn.internal.tasks.ui.commands.ViewSourceHandler.setChecked(action.isChecked());
                    }
                }
            }
        }
        return null;
    }

    @java.lang.SuppressWarnings("rawtypes")
    public void updateElement(org.eclipse.ui.menus.UIElement element, java.util.Map parameters) {
        element.setChecked(org.eclipse.mylyn.internal.tasks.ui.commands.ViewSourceHandler.checked);
    }
}