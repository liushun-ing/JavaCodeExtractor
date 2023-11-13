/**
 * *****************************************************************************
 * Copyright (c) 2004, 2016 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *     Tasktop Technologies - improvements
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.dialogs;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListSorter;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListSorter.GroupBy;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
/**
 *
 * @author Frank Becker
 * @author Steffen Pingel
 */
public class TaskListSortDialog extends org.eclipse.mylyn.internal.tasks.ui.dialogs.TaskCompareDialog {
    private org.eclipse.swt.widgets.Combo modeCombo;

    private final org.eclipse.mylyn.internal.tasks.ui.views.TaskListView taskListView;

    public TaskListSortDialog(org.eclipse.jface.window.IShellProvider parentShell, org.eclipse.mylyn.internal.tasks.ui.views.TaskListView taskListView) {
        super(parentShell, taskListView.getSorter().getTaskComparator());
        this.taskListView = taskListView;
        setTitle(Messages.TaskListSortDialog_Title);
    }

    @java.lang.Override
    protected org.eclipse.swt.widgets.Control createContentArea(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.widgets.Composite container = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NONE);
        container.setLayout(org.eclipse.jface.layout.GridLayoutFactory.fillDefaults().create());
        org.eclipse.swt.widgets.Group groupByComposite = new org.eclipse.swt.widgets.Group(container, org.eclipse.swt.SWT.NONE);
        groupByComposite.setLayout(new org.eclipse.swt.layout.GridLayout(2, false));
        groupByComposite.setText(Messages.TaskListSortDialog_Queries_and_Categories);
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().grab(true, false).applyTo(groupByComposite);
        org.eclipse.swt.widgets.Label numberLabel = new org.eclipse.swt.widgets.Label(groupByComposite, org.eclipse.swt.SWT.NULL);
        numberLabel.setText(Messages.TaskListSortDialog_Grouped_by);
        modeCombo = new org.eclipse.swt.widgets.Combo(groupByComposite, org.eclipse.swt.SWT.READ_ONLY);
        modeCombo.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_HORIZONTAL));
        org.eclipse.mylyn.internal.tasks.ui.views.TaskListSorter.GroupBy[] values = org.eclipse.mylyn.internal.tasks.ui.views.TaskListSorter.GroupBy.values();
        for (org.eclipse.mylyn.internal.tasks.ui.views.TaskListSorter.GroupBy groupBy : values) {
            modeCombo.add(groupBy.getLabel());
        }
        modeCombo.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                markDirty();
            }
        });
        modeCombo.select(taskListView.getSorter().getGroupBy().ordinal());
        org.eclipse.swt.widgets.Control child = super.createContentArea(container);
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().grab(true, false).applyTo(child);
        return container;
    }

    @java.lang.Override
    protected void okPressed() {
        if (isDirty()) {
            int selectionIndex = modeCombo.getSelectionIndex();
            if (selectionIndex != (-1)) {
                taskListView.getSorter().setGroupBy(org.eclipse.mylyn.internal.tasks.ui.views.TaskListSorter.GroupBy.values()[selectionIndex]);
            }
        }
        super.okPressed();
    }
}