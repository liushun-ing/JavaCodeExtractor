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
 *     Pawel Niewiadomski - fix for bug 287832
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.editors;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.commons.ui.dialogs.AbstractInPlaceDialog;
import org.eclipse.mylyn.commons.ui.dialogs.IInPlaceDialogListener;
import org.eclipse.mylyn.commons.ui.dialogs.InPlaceDialogEvent;
import org.eclipse.mylyn.commons.workbench.InPlaceCheckBoxTreeDialog;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.SharedScrolledComposite;
/**
 *
 * @author Shawn Minto
 * @author Sam Davis
 */
public class CheckboxMultiSelectAttributeEditor extends org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor {
    private org.eclipse.swt.widgets.Text valueText;

    private org.eclipse.swt.widgets.Composite parent;

    private org.eclipse.swt.widgets.Button button;

    private boolean suppressRefresh;

    public CheckboxMultiSelectAttributeEditor(org.eclipse.mylyn.tasks.core.data.TaskDataModel manager, org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute) {
        super(manager, taskAttribute);
        setLayoutHint(new org.eclipse.mylyn.tasks.ui.editors.LayoutHint(org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan.SINGLE, org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan.MULTIPLE));
    }

    @java.lang.Override
    public void createControl(org.eclipse.swt.widgets.Composite parent, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        if (isReadOnly()) {
            valueText = new org.eclipse.swt.widgets.Text(parent, (org.eclipse.swt.SWT.FLAT | org.eclipse.swt.SWT.READ_ONLY) | org.eclipse.swt.SWT.WRAP);
            valueText.setFont(EditorUtil.TEXT_FONT);
            toolkit.adapt(valueText, false, false);
            valueText.setData(org.eclipse.ui.forms.widgets.FormToolkit.KEY_DRAW_BORDER, java.lang.Boolean.FALSE);
            valueText.setToolTipText(getDescription());
            refresh();
            setControl(valueText);
        } else {
            this.parent = parent;
            org.eclipse.swt.widgets.Composite composite = toolkit.createComposite(parent);
            composite.setData(org.eclipse.ui.forms.widgets.FormToolkit.KEY_DRAW_BORDER, org.eclipse.ui.forms.widgets.FormToolkit.TREE_BORDER);
            org.eclipse.swt.layout.GridLayout layout = new org.eclipse.swt.layout.GridLayout(2, false);
            layout.marginWidth = 0;
            layout.marginBottom = 0;
            layout.marginLeft = 0;
            layout.marginRight = 0;
            layout.marginTop = 0;
            layout.marginHeight = 0;
            composite.setLayout(layout);
            valueText = toolkit.createText(composite, "", org.eclipse.swt.SWT.FLAT | org.eclipse.swt.SWT.WRAP);// $NON-NLS-1$

            org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.CENTER).grab(true, false).applyTo(valueText);
            valueText.setFont(EditorUtil.TEXT_FONT);
            valueText.setEditable(false);
            valueText.setToolTipText(getDescription());
            button = toolkit.createButton(composite, "", org.eclipse.swt.SWT.ARROW | org.eclipse.swt.SWT.DOWN);// $NON-NLS-1$

            org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.CENTER, org.eclipse.swt.SWT.TOP).applyTo(button);
            button.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                @java.lang.Override
                public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                    final java.util.List<java.lang.String> values = getValues();
                    java.util.Map<java.lang.String, java.lang.String> validValues = getAttributeMapper().getOptions(getTaskAttribute());
                    final org.eclipse.mylyn.commons.workbench.InPlaceCheckBoxTreeDialog selectionDialog = new org.eclipse.mylyn.commons.workbench.InPlaceCheckBoxTreeDialog(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell(), button, values, validValues, org.eclipse.osgi.util.NLS.bind(Messages.CheckboxMultiSelectAttributeEditor_Select_X, getLabel()));
                    selectionDialog.addEventListener(new org.eclipse.mylyn.commons.ui.dialogs.IInPlaceDialogListener() {
                        public void buttonPressed(org.eclipse.mylyn.commons.ui.dialogs.InPlaceDialogEvent event) {
                            suppressRefresh = true;
                            try {
                                if (event.getReturnCode() == org.eclipse.jface.window.Window.OK) {
                                    java.util.Set<java.lang.String> newValues = selectionDialog.getSelectedValues();
                                    if (!new java.util.HashSet<java.lang.String>(values).equals(newValues)) {
                                        setValues(new java.util.ArrayList<java.lang.String>(newValues));
                                        refresh();
                                    }
                                } else if (event.getReturnCode() == org.eclipse.mylyn.commons.ui.dialogs.AbstractInPlaceDialog.ID_CLEAR) {
                                    java.util.Set<java.lang.String> newValues = new java.util.HashSet<java.lang.String>();
                                    if (!new java.util.HashSet<java.lang.String>(values).equals(newValues)) {
                                        setValues(new java.util.ArrayList<java.lang.String>(newValues));
                                        refresh();
                                    }
                                }
                            } finally {
                                suppressRefresh = false;
                            }
                        }
                    });
                    selectionDialog.open();
                }
            });
            toolkit.adapt(valueText, false, false);
            refresh();
            setControl(composite);
        }
    }

    /**
     * Update scroll bars of the enclosing form.
     *
     * @see Section#reflow()
     */
    private void reflow() {
        org.eclipse.swt.widgets.Composite c = parent;
        while (c != null) {
            c.setRedraw(false);
            c = c.getParent();
            if ((c instanceof org.eclipse.ui.forms.widgets.SharedScrolledComposite) || (c instanceof org.eclipse.swt.widgets.Shell)) {
                break;
            }
        } 
        c = parent;
        while (c != null) {
            c.layout(true);
            c = c.getParent();
            if (c instanceof org.eclipse.ui.forms.widgets.SharedScrolledComposite) {
                ((org.eclipse.ui.forms.widgets.SharedScrolledComposite) (c)).reflow(true);
                break;
            }
        } 
        c = parent;
        while (c != null) {
            c.setRedraw(true);
            c = c.getParent();
            if ((c instanceof org.eclipse.ui.forms.widgets.SharedScrolledComposite) || (c instanceof org.eclipse.swt.widgets.Shell)) {
                break;
            }
        } 
    }

    public java.util.List<java.lang.String> getValues() {
        return getAttributeMapper().getValues(getTaskAttribute());
    }

    public java.util.List<java.lang.String> getValuesLabels() {
        return getAttributeMapper().getValueLabels(getTaskAttribute());
    }

    public void setValues(java.util.List<java.lang.String> newValues) {
        getAttributeMapper().setValues(getTaskAttribute(), newValues);
        attributeChanged();
    }

    @java.lang.Override
    protected void decorateIncoming(org.eclipse.swt.graphics.Color color) {
        super.decorateIncoming(color);
        if ((valueText != null) && (!valueText.isDisposed())) {
            valueText.setBackground(color);
        }
        if ((button != null) && (!button.isDisposed())) {
            button.setBackground(color);
        }
    }

    @java.lang.Override
    public void refresh() {
        if ((valueText == null) || valueText.isDisposed()) {
            return;
        }
        java.lang.StringBuilder valueString = new java.lang.StringBuilder();
        java.util.List<java.lang.String> values = getValuesLabels();
        java.util.Collections.sort(values);
        for (int i = 0; i < values.size(); i++) {
            valueString.append(values.get(i));
            if (i != (values.size() - 1)) {
                valueString.append(", ");// $NON-NLS-1$

            }
        }
        valueText.setText(valueString.toString());
        if ((((valueText != null) && (parent != null)) && (parent.getParent() != null)) && (parent.getParent().getParent() != null)) {
            org.eclipse.swt.graphics.Point size = valueText.getSize();
            // subtract 1 from size for border
            org.eclipse.swt.graphics.Point newSize = valueText.computeSize(size.x - 1, org.eclipse.swt.SWT.DEFAULT);
            if (newSize.y != size.y) {
                reflow();
            }
        }
    }

    @java.lang.Override
    public boolean shouldAutoRefresh() {
        return !suppressRefresh;
    }
}