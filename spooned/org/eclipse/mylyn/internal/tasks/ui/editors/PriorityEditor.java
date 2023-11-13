/**
 * *****************************************************************************
 * Copyright (c) 2009, 2011 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Maarten Meijer - fix for bug 284559
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.editors;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.tasks.core.ITask.PriorityLevel;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.forms.widgets.FormToolkit;
/**
 *
 * @author David Shepherd
 * @author Steffen Pingel
 */
public class PriorityEditor {
    private org.eclipse.swt.widgets.Control control;

    private boolean ignoreNotification;

    private org.eclipse.swt.widgets.Label label;

    private java.util.Map<java.lang.String, java.lang.String> labelByValue;

    private org.eclipse.swt.widgets.Menu menu;

    private boolean readOnly;

    private org.eclipse.swt.widgets.ToolItem selectionButton;

    private org.eclipse.swt.widgets.ToolBar toolBar;

    private java.lang.String value;

    private final org.eclipse.mylyn.tasks.core.data.TaskAttribute attribute;

    public PriorityEditor() {
        this(null);
    }

    public PriorityEditor(org.eclipse.mylyn.tasks.core.data.TaskAttribute attribute) {
        this.attribute = attribute;
    }

    public void createControl(final org.eclipse.swt.widgets.Composite parent, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        if (isReadOnly()) {
            label = toolkit.createLabel(parent, "");// $NON-NLS-1$

            setControl(label);
        } else {
            toolBar = new org.eclipse.swt.widgets.ToolBar(parent, org.eclipse.swt.SWT.FLAT);
            selectionButton = new org.eclipse.swt.widgets.ToolItem(toolBar, org.eclipse.swt.SWT.DROP_DOWN);
            selectionButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                @java.lang.Override
                public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                    if (menu == null) {
                        createMenu(toolBar);
                    }
                    org.eclipse.swt.graphics.Point location = parent.toDisplay(toolBar.getLocation());
                    location.y = location.y + selectionButton.getBounds().height;
                    if (value != null) {
                        org.eclipse.swt.widgets.MenuItem[] items = menu.getItems();
                        for (org.eclipse.swt.widgets.MenuItem item : items) {
                            item.setSelection(value.equals(item.getData()));
                        }
                    }
                    menu.setLocation(location);
                    menu.setVisible(true);
                }
            });
            selectionButton.addDisposeListener(new org.eclipse.swt.events.DisposeListener() {
                public void widgetDisposed(org.eclipse.swt.events.DisposeEvent e) {
                    if (menu != null) {
                        menu.dispose();
                    }
                }
            });
            toolkit.adapt(toolBar);
            setControl(toolBar);
        }
    }

    private void createMenu(final org.eclipse.swt.widgets.ToolBar bar) {
        menu = new org.eclipse.swt.widgets.Menu(bar);
        for (java.lang.String key : labelByValue.keySet()) {
            final org.eclipse.swt.widgets.MenuItem item = new org.eclipse.swt.widgets.MenuItem(menu, org.eclipse.swt.SWT.CHECK);
            item.setText(labelByValue.get(key));
            item.setData(key);
            item.setImage(getSmallImage(key));
            item.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                @java.lang.Override
                public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                    if (!ignoreNotification) {
                        value = ((java.lang.String) (item.getData()));
                        valueChanged(value);
                    }
                }
            });
        }
    }

    public org.eclipse.swt.widgets.Control getControl() {
        return control;
    }

    public java.util.Map<java.lang.String, java.lang.String> getLabelByValue() {
        return java.util.Collections.unmodifiableMap(labelByValue);
    }

    private org.eclipse.jface.resource.ImageDescriptor getLargeImageDescriptor(org.eclipse.mylyn.tasks.core.ITask.PriorityLevel priorityLevel) {
        if (priorityLevel != null) {
            switch (priorityLevel) {
                case P1 :
                    return org.eclipse.mylyn.commons.ui.CommonImages.PRIORITY_1_LARGE;
                case P2 :
                    return org.eclipse.mylyn.commons.ui.CommonImages.PRIORITY_2_LARGE;
                case P3 :
                    return org.eclipse.mylyn.commons.ui.CommonImages.PRIORITY_3_LARGE;
                case P4 :
                    return org.eclipse.mylyn.commons.ui.CommonImages.PRIORITY_4_LARGE;
                case P5 :
                    return org.eclipse.mylyn.commons.ui.CommonImages.PRIORITY_5_LARGE;
            }
        }
        return org.eclipse.mylyn.commons.ui.CommonImages.PRIORITY_3_LARGE;
    }

    private org.eclipse.swt.graphics.Image getSmallImage(java.lang.String value) {
        org.eclipse.jface.resource.ImageDescriptor descriptor = getSmallImageDescriptor(value);
        if (descriptor != null) {
            return org.eclipse.mylyn.commons.ui.CommonImages.getImage(descriptor);
        }
        return null;
    }

    private org.eclipse.jface.resource.ImageDescriptor getSmallImageDescriptor(java.lang.String value) {
        org.eclipse.mylyn.tasks.core.ITask.PriorityLevel priorityLevel = getPriorityLevel(value);
        if (priorityLevel != null) {
            return org.eclipse.mylyn.tasks.ui.TasksUiImages.getImageDescriptorForPriority(priorityLevel);
        }
        return null;
    }

    private org.eclipse.mylyn.tasks.core.ITask.PriorityLevel getPriorityLevel(java.lang.String value) {
        if (attribute != null) {
            return attribute.getTaskData().getAttributeMapper().getPriorityLevel(attribute, value);
        }
        return org.eclipse.mylyn.tasks.core.ITask.PriorityLevel.fromString(value);
    }

    public java.lang.String getToolTipText() {
        if (label != null) {
            return label.getToolTipText();
        }
        if (selectionButton != null) {
            return selectionButton.getToolTipText();
        }
        return null;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void select(java.lang.String value, org.eclipse.mylyn.tasks.core.ITask.PriorityLevel level) {
        try {
            ignoreNotification = true;
            this.value = value;
            if (label != null) {
                label.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(getLargeImageDescriptor(level)));
            }
            if ((selectionButton != null) && (toolBar != null)) {
                selectionButton.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(getLargeImageDescriptor(level)));
            }
        } finally {
            ignoreNotification = false;
        }
    }

    private void setControl(org.eclipse.swt.widgets.Control control) {
        this.control = control;
    }

    public void setLabelByValue(java.util.Map<java.lang.String, java.lang.String> labelByValue) {
        this.labelByValue = new java.util.LinkedHashMap<java.lang.String, java.lang.String>(labelByValue);
        // the menu will be re-created with updated options when it is requested again
        if (menu != null) {
            menu.dispose();
        }
        menu = null;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public void setToolTipText(java.lang.String text) {
        if (label != null) {
            label.setToolTipText(text);
        }
        if (selectionButton != null) {
            selectionButton.setToolTipText(text);
        }
    }

    protected void valueChanged(java.lang.String key) {
    }

    public java.lang.String getValue() {
        return value;
    }
}