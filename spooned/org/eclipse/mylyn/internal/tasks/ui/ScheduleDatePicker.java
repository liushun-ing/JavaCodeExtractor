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
package org.eclipse.mylyn.internal.tasks.ui;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.commons.workbench.forms.DatePicker;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.DateRange;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
/**
 *
 * @author Rob Elves
 */
public class ScheduleDatePicker extends org.eclipse.swt.widgets.Composite {
    private org.eclipse.swt.widgets.Text scheduledDateText;

    private org.eclipse.swt.widgets.Button pickButton;

    private final java.util.List<org.eclipse.swt.events.SelectionListener> pickerListeners = new java.util.LinkedList<org.eclipse.swt.events.SelectionListener>();

    private final java.lang.String initialText = org.eclipse.mylyn.commons.workbench.forms.DatePicker.LABEL_CHOOSE;

    private final java.util.List<org.eclipse.mylyn.tasks.core.IRepositoryElement> tasks;

    private final org.eclipse.mylyn.internal.tasks.ui.ScheduleTaskMenuContributor contributor;

    private org.eclipse.mylyn.internal.tasks.core.DateRange scheduledDate;

    private final boolean isFloating = false;

    private org.eclipse.ui.forms.widgets.ImageHyperlink clearControl;

    public ScheduleDatePicker(org.eclipse.swt.widgets.Composite parent, org.eclipse.mylyn.internal.tasks.core.AbstractTask task, int style) {
        super(parent, style);
        if (task != null) {
            if (task.getScheduledForDate() != null) {
                this.scheduledDate = task.getScheduledForDate();
            }
        }
        initialize((style & org.eclipse.swt.SWT.FLAT) > 0 ? org.eclipse.swt.SWT.FLAT : 0);
        contributor = new org.eclipse.mylyn.internal.tasks.ui.ScheduleTaskMenuContributor() {
            @java.lang.Override
            protected org.eclipse.mylyn.internal.tasks.core.DateRange getScheduledForDate(org.eclipse.mylyn.internal.tasks.core.AbstractTask singleTaskSelection) {
                return ScheduleDatePicker.this.scheduledDate;
            }

            @java.lang.Override
            protected void setScheduledDate(org.eclipse.mylyn.internal.tasks.core.DateRange dateRange) {
                if (dateRange != null) {
                    scheduledDate = dateRange;
                } else {
                    scheduledDate = null;
                }
                updateDateText();
                notifyPickerListeners();
            }
        };
        tasks = new java.util.ArrayList<org.eclipse.mylyn.tasks.core.IRepositoryElement>();
        tasks.add(task);
    }

    private void initialize(int style) {
        org.eclipse.swt.layout.GridLayout gridLayout = new org.eclipse.swt.layout.GridLayout(3, false);
        gridLayout.horizontalSpacing = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        this.setLayout(gridLayout);
        scheduledDateText = new org.eclipse.swt.widgets.Text(this, style);
        scheduledDateText.setEditable(false);
        org.eclipse.swt.layout.GridData dateTextGridData = new org.eclipse.swt.layout.GridData(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.FILL, false, false);
        dateTextGridData.heightHint = 5;
        dateTextGridData.grabExcessHorizontalSpace = true;
        dateTextGridData.verticalAlignment = org.eclipse.swt.SWT.FILL;
        scheduledDateText.setLayoutData(dateTextGridData);
        scheduledDateText.setText(initialText);
        clearControl = new org.eclipse.ui.forms.widgets.ImageHyperlink(this, org.eclipse.swt.SWT.NONE);
        clearControl.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.FIND_CLEAR_DISABLED));
        clearControl.setHoverImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.FIND_CLEAR));
        clearControl.setToolTipText(Messages.ScheduleDatePicker_Clear);
        clearControl.addHyperlinkListener(new org.eclipse.ui.forms.events.HyperlinkAdapter() {
            @java.lang.Override
            public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                setScheduledDate(null);
                for (org.eclipse.mylyn.tasks.core.IRepositoryElement task : tasks) {
                    if (task instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask) {
                        // XXX why is this set here?
                        ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (task)).setReminded(false);
                    }
                }
                notifyPickerListeners();
            }
        });
        clearControl.setBackground(clearControl.getDisplay().getSystemColor(org.eclipse.swt.SWT.COLOR_WHITE));
        org.eclipse.swt.layout.GridData clearButtonGridData = new org.eclipse.swt.layout.GridData();
        clearButtonGridData.horizontalIndent = 3;
        clearControl.setLayoutData(clearButtonGridData);
        pickButton = new org.eclipse.swt.widgets.Button(this, (style | org.eclipse.swt.SWT.ARROW) | org.eclipse.swt.SWT.DOWN);
        org.eclipse.swt.layout.GridData pickButtonGridData = new org.eclipse.swt.layout.GridData(org.eclipse.swt.SWT.RIGHT, org.eclipse.swt.SWT.FILL, false, true);
        pickButtonGridData.verticalIndent = 0;
        pickButtonGridData.horizontalIndent = 3;
        pickButton.setLayoutData(pickButtonGridData);
        pickButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent arg0) {
                org.eclipse.jface.action.MenuManager menuManager = contributor.getSubMenuManager(tasks);
                org.eclipse.swt.widgets.Menu menu = menuManager.createContextMenu(pickButton);
                pickButton.setMenu(menu);
                menu.setVisible(true);
                org.eclipse.swt.graphics.Point location = pickButton.toDisplay(pickButton.getLocation());
                org.eclipse.swt.graphics.Rectangle bounds = pickButton.getBounds();
                menu.setLocation(location.x - pickButton.getBounds().x, (location.y + bounds.height) + 2);
            }
        });
        updateDateText();
        pack();
    }

    private void updateClearControlVisibility() {
        if ((clearControl != null) && (clearControl.getLayoutData() instanceof org.eclipse.swt.layout.GridData)) {
            org.eclipse.swt.layout.GridData gd = ((org.eclipse.swt.layout.GridData) (clearControl.getLayoutData()));
            gd.exclude = scheduledDate == null;
            clearControl.getParent().layout();
        }
    }

    public void addPickerSelectionListener(org.eclipse.swt.events.SelectionListener listener) {
        pickerListeners.add(listener);
    }

    @java.lang.Override
    public void setForeground(org.eclipse.swt.graphics.Color color) {
        pickButton.setForeground(color);
        scheduledDateText.setForeground(color);
        super.setForeground(color);
    }

    @java.lang.Override
    public void setBackground(org.eclipse.swt.graphics.Color backgroundColor) {
        pickButton.setBackground(backgroundColor);
        scheduledDateText.setBackground(backgroundColor);
        super.setBackground(backgroundColor);
    }

    private void notifyPickerListeners() {
        for (org.eclipse.swt.events.SelectionListener listener : pickerListeners) {
            listener.widgetSelected(null);
        }
    }

    private void updateDateText() {
        if (scheduledDate != null) {
            scheduledDateText.setText(scheduledDate.toString());
        } else {
            scheduledDateText.setEnabled(false);
            scheduledDateText.setText(org.eclipse.mylyn.commons.workbench.forms.DatePicker.LABEL_CHOOSE);
            scheduledDateText.setEnabled(true);
        }
        updateClearControlVisibility();
    }

    @java.lang.Override
    public void setEnabled(boolean enabled) {
        scheduledDateText.setEnabled(enabled);
        pickButton.setEnabled(enabled);
        clearControl.setEnabled(enabled);
        super.setEnabled(enabled);
    }

    public org.eclipse.mylyn.internal.tasks.core.DateRange getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(org.eclipse.mylyn.internal.tasks.core.DateRange date) {
        scheduledDate = date;
        updateDateText();
    }

    public boolean isFloatingDate() {
        return isFloating;
    }
}