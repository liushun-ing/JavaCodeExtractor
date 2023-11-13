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
package org.eclipse.mylyn.internal.tasks.ui.views;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskContainer;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.swt.graphics.Image;
/**
 *
 * @author Mik Kersten
 */
public class TableDecoratingLabelProvider extends org.eclipse.jface.viewers.DecoratingLabelProvider implements org.eclipse.jface.viewers.ITableLabelProvider {
    public TableDecoratingLabelProvider(org.eclipse.jface.viewers.ILabelProvider provider, org.eclipse.jface.viewers.ILabelDecorator decorator) {
        super(provider, decorator);
    }

    public org.eclipse.swt.graphics.Image getColumnImage(java.lang.Object element, int columnIndex) {
        if (!(element instanceof org.eclipse.mylyn.tasks.core.ITaskContainer)) {
            return null;
        }
        if (columnIndex == 0) {
            if ((element instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) && (!(element instanceof org.eclipse.mylyn.tasks.core.ITask))) {
                return super.getImage(element);
            } else if (element instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask) {
                org.eclipse.mylyn.internal.tasks.core.AbstractTask task = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (element));
                if (task.isActive()) {
                    return org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.CONTEXT_ACTIVE);
                } else if (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getContextStore().hasContext(task)) {
                    return org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.CONTEXT_INACTIVE);
                } else {
                    return org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.CONTEXT_INACTIVE_EMPTY);
                }
            } else {
                return org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.CONTEXT_INACTIVE_EMPTY);
            }
        } else if (columnIndex == 1) {
            if ((element instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) || (element instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery)) {
                return null;
            }
            return super.getImage(element);
        }
        return null;
    }

    public java.lang.String getColumnText(java.lang.Object element, int columnIndex) {
        return null;
    }
}