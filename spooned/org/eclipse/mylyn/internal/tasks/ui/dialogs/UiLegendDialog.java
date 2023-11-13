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
package org.eclipse.mylyn.internal.tasks.ui.dialogs;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
/**
 *
 * @author Mik Kersten
 * @author Leo Dos Santos
 */
public class UiLegendDialog extends org.eclipse.jface.dialogs.PopupDialog {
    private org.eclipse.ui.forms.widgets.FormToolkit toolkit;

    private org.eclipse.ui.forms.widgets.ScrolledForm form;

    private org.eclipse.mylyn.internal.tasks.ui.dialogs.UiLegendControl content;

    public UiLegendDialog(org.eclipse.swt.widgets.Shell parent) {
        super(parent, org.eclipse.jface.dialogs.PopupDialog.INFOPOPUP_SHELLSTYLE | org.eclipse.swt.SWT.ON_TOP, false, false, false, false, false, null, null);
    }

    @java.lang.Override
    protected org.eclipse.swt.widgets.Control createContents(org.eclipse.swt.widgets.Composite parent) {
        getShell().setBackground(getShell().getDisplay().getSystemColor(org.eclipse.swt.SWT.COLOR_DARK_GRAY));
        return createDialogArea(parent);
    }

    @java.lang.Override
    public int open() {
        int open = super.open();
        // getShell().setLocation(getShell().getLocation().x, getShell().getLocation().y+20);
        getShell().setFocus();
        return open;
    }

    @java.lang.Override
    public boolean close() {
        if (toolkit != null) {
            toolkit.dispose();
        }
        return super.close();
    }

    @java.lang.Override
    protected org.eclipse.swt.widgets.Control createDialogArea(org.eclipse.swt.widgets.Composite parent) {
        toolkit = new org.eclipse.ui.forms.widgets.FormToolkit(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getFormColors(parent.getDisplay()));
        form = toolkit.createScrolledForm(parent);
        form.setText(Messages.UiLegendControl_Tasks_UI_Legend);
        form.getToolBarManager().add(new org.eclipse.mylyn.internal.tasks.ui.dialogs.UiLegendDialog.CloseDialogAction());
        form.getToolBarManager().update(true);
        form.getBody().setLayout(new org.eclipse.ui.forms.widgets.TableWrapLayout());
        toolkit.decorateFormHeading(form.getForm());
        content = new org.eclipse.mylyn.internal.tasks.ui.dialogs.UiLegendControl(form.getBody(), toolkit);
        content.setWindow(this);
        return parent;
    }

    private class CloseDialogAction extends org.eclipse.jface.action.Action {
        private CloseDialogAction() {
            setImageDescriptor(org.eclipse.mylyn.commons.ui.CommonImages.NOTIFICATION_CLOSE);
            setText(Messages.UiLegendDialog_Close_Dialog);
        }

        @java.lang.Override
        public void run() {
            close();
        }
    }
}