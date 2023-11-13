/**
 * *****************************************************************************
 * Copyright (c) 2011 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.commands;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.internal.tasks.ui.actions.DeleteTaskRepositoryAction;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.ui.handlers.HandlerUtil;
/**
 *
 * @author Steffen Pingel
 */
public class DeleteTaskRepositoryHandler extends org.eclipse.core.commands.AbstractHandler {
    public java.lang.Object execute(org.eclipse.core.commands.ExecutionEvent event) throws org.eclipse.core.commands.ExecutionException {
        org.eclipse.jface.viewers.ISelection selection = org.eclipse.ui.handlers.HandlerUtil.getCurrentSelection(event);
        if (selection instanceof org.eclipse.jface.viewers.IStructuredSelection) {
            java.lang.Object item = ((org.eclipse.jface.viewers.IStructuredSelection) (selection)).getFirstElement();
            if (item instanceof org.eclipse.mylyn.tasks.core.TaskRepository) {
                org.eclipse.mylyn.internal.tasks.ui.actions.DeleteTaskRepositoryAction.run(((org.eclipse.mylyn.tasks.core.TaskRepository) (item)));
            }
        }
        return null;
    }
}