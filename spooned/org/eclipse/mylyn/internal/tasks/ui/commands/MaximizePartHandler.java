/**
 * *****************************************************************************
 * Copyright (c) 2004, 2008 Jingwen Ou and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jingwen Ou - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.commands;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IAction;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.handlers.HandlerUtil;
/**
 *
 * @author  */
public class MaximizePartHandler extends org.eclipse.core.commands.AbstractHandler {
    public java.lang.Object execute(org.eclipse.core.commands.ExecutionEvent event) throws org.eclipse.core.commands.ExecutionException {
        org.eclipse.ui.IWorkbenchSite site = org.eclipse.ui.handlers.HandlerUtil.getActiveSite(event);
        if (site instanceof org.eclipse.ui.IEditorSite) {
            org.eclipse.ui.IWorkbenchPart part = ((org.eclipse.ui.IEditorSite) (site)).getPart();
            if (part instanceof org.eclipse.ui.forms.editor.FormEditor) {
                org.eclipse.ui.forms.editor.IFormPage page = ((org.eclipse.ui.forms.editor.FormEditor) (part)).getActivePageInstance();
                org.eclipse.swt.widgets.Control focusedControl = org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.getFocusControl(page);
                if (focusedControl != null) {
                    java.lang.Object data = focusedControl.getData(org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.KEY_TOGGLE_TO_MAXIMIZE_ACTION);
                    if (data instanceof org.eclipse.jface.action.IAction) {
                        org.eclipse.jface.action.IAction action = ((org.eclipse.jface.action.IAction) (data));
                        action.setChecked(!action.isChecked());
                        action.run();
                    }
                }
            }
        }
        return null;
    }
}