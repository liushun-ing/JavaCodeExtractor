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
 *     Frank Becker - improvements
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui;
import org.eclipse.mylyn.commons.ui.compatibility.CommonColors;
import org.eclipse.mylyn.commons.workbench.forms.ScalingHyperlink;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
/**
 *
 * @author Mik Kersten
 * @author Steffen Pingel
 * @author Frank Becker
 */
public class TaskScalingHyperlink extends org.eclipse.mylyn.commons.workbench.forms.ScalingHyperlink {
    private org.eclipse.mylyn.tasks.core.ITask task;

    public TaskScalingHyperlink(org.eclipse.swt.widgets.Composite parent, int style) {
        super(parent, style);
        setForeground(org.eclipse.mylyn.commons.ui.compatibility.CommonColors.HYPERLINK_WIDGET);
        addMouseTrackListener(org.eclipse.mylyn.internal.tasks.ui.MOUSE_TRACK_LISTENER);
    }

    public org.eclipse.mylyn.tasks.core.ITask getTask() {
        return task;
    }

    public void setTask(org.eclipse.mylyn.tasks.core.ITask task) {
        this.task = task;
        if (task != null) {
            if ((getStyle() & org.eclipse.swt.SWT.SHORT) != 0) {
                setText(task.getTaskKey());
                setToolTipText((task.getTaskKey() + ": ") + task.getSummary());// $NON-NLS-1$

                setStrikeThrough(task.isCompleted());
            } else {
                setText(task.getSummary());
                setToolTipText("");// $NON-NLS-1$

                setStrikeThrough(false);
            }
        } else {
            setText("");// $NON-NLS-1$

            setToolTipText("");// $NON-NLS-1$

            setStrikeThrough(false);
        }
        setUnderlined(false);
    }
}