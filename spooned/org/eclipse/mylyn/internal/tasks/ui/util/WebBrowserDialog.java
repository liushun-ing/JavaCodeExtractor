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
package org.eclipse.mylyn.internal.tasks.ui.util;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.workbench.browser.BrowserUtil;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
/**
 * Dialog to show the contents of an html page to the user
 *
 * @author Shawn Minto
 * @deprecated use {@link org.eclipse.mylyn.commons.workbench.browser.WebBrowserDialog} instead
 */
@java.lang.Deprecated
public class WebBrowserDialog extends org.eclipse.jface.dialogs.MessageDialog {
    private java.lang.String data = null;

    private static java.lang.Boolean browserAvailable;

    public WebBrowserDialog(org.eclipse.swt.widgets.Shell parentShell, java.lang.String dialogTitle, org.eclipse.swt.graphics.Image dialogTitleImage, java.lang.String dialogMessage, int dialogImageType, java.lang.String[] dialogButtonLabels, int defaultIndex, java.lang.String data) {
        super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels, defaultIndex);
        this.data = data;
        this.setShellStyle(org.eclipse.swt.SWT.SHELL_TRIM | org.eclipse.swt.SWT.RESIZE);
    }

    public static int openAcceptAgreement(org.eclipse.swt.widgets.Shell parent, java.lang.String title, java.lang.String message, java.lang.String data) {
        if (org.eclipse.mylyn.internal.tasks.ui.util.WebBrowserDialog.isInternalBrowserAvailable(parent)) {
            org.eclipse.mylyn.internal.tasks.ui.util.WebBrowserDialog dialog = // accept
            new org.eclipse.mylyn.internal.tasks.ui.util.WebBrowserDialog(parent, title, null, message, NONE, new java.lang.String[]{ org.eclipse.jface.dialogs.IDialogConstants.OK_LABEL }, 0, data);
            // ok is the default
            return dialog.open();
        } else {
            java.io.File file = null;
            try {
                file = java.io.File.createTempFile("mylyn-error", ".html");// $NON-NLS-1$ //$NON-NLS-2$

                file.deleteOnExit();
                java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(file));
                try {
                    writer.write(message);
                } finally {
                    writer.close();
                }
            } catch (java.io.IOException e) {
                if (file != null) {
                    file.delete();
                }
                org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Unexpected error while displaying error", e));// $NON-NLS-1$

                return org.eclipse.jface.window.Window.CANCEL;
            }
            org.eclipse.mylyn.commons.workbench.browser.BrowserUtil.openUrl(file.toURI().toString(), org.eclipse.ui.browser.IWorkbenchBrowserSupport.AS_EXTERNAL);
            return org.eclipse.jface.window.Window.OK;
        }
    }

    @java.lang.Override
    public org.eclipse.swt.widgets.Control createCustomArea(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.layout.GridLayout layout = new org.eclipse.swt.layout.GridLayout();
        parent.setLayout(layout);
        layout.numColumns = 1;
        org.eclipse.swt.browser.Browser b = new org.eclipse.swt.browser.Browser(parent, org.eclipse.swt.SWT.NONE);
        org.eclipse.swt.layout.GridData gd = new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_BOTH);
        gd.horizontalSpan = 1;
        gd.verticalSpan = 50;
        b.setLayoutData(gd);
        b.setText(data);
        return parent;
    }

    private static synchronized boolean isInternalBrowserAvailable(org.eclipse.swt.widgets.Composite composite) {
        if (org.eclipse.mylyn.internal.tasks.ui.util.WebBrowserDialog.browserAvailable == null) {
            try {
                org.eclipse.swt.browser.Browser browser = new org.eclipse.swt.browser.Browser(composite, org.eclipse.swt.SWT.NULL);
                browser.dispose();
                org.eclipse.mylyn.internal.tasks.ui.util.WebBrowserDialog.browserAvailable = true;
            } catch (org.eclipse.swt.SWTError e) {
                org.eclipse.mylyn.internal.tasks.ui.util.WebBrowserDialog.browserAvailable = false;
            }
        }
        return org.eclipse.mylyn.internal.tasks.ui.util.WebBrowserDialog.browserAvailable;
    }
}