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
package org.eclipse.mylyn.tasks.ui.editors;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.internal.browser.BrowserViewer;
import org.eclipse.ui.internal.browser.IBrowserViewerContainer;
/**
 * A form page that contains a browser control.
 *
 * @since 3.0
 * @author Mik Kersten
 * @author Steffen Pingel
 */
public class BrowserFormPage extends org.eclipse.mylyn.tasks.ui.editors.TaskFormPage {
    public static final java.lang.String ID_EDITOR = "org.eclipse.mylyn.tasks.ui.editor.browser";// $NON-NLS-1$


    private org.eclipse.ui.internal.browser.BrowserViewer browserViewer;

    public BrowserFormPage(org.eclipse.ui.forms.editor.FormEditor editor, java.lang.String title) {
        super(editor, org.eclipse.mylyn.tasks.ui.editors.BrowserFormPage.ID_EDITOR, title);
    }

    @java.lang.Override
    protected void createFormContent(org.eclipse.ui.forms.IManagedForm managedForm) {
        super.createFormContent(managedForm);
        try {
            org.eclipse.ui.forms.widgets.ScrolledForm form = managedForm.getForm();
            form.getBody().setLayout(new org.eclipse.swt.layout.FillLayout());
            browserViewer = new org.eclipse.ui.internal.browser.BrowserViewer(form.getBody(), org.eclipse.swt.SWT.NONE);
            browserViewer.setLayoutData(null);
            browserViewer.setContainer(new org.eclipse.ui.internal.browser.IBrowserViewerContainer() {
                public boolean close() {
                    return false;
                }

                public org.eclipse.ui.IActionBars getActionBars() {
                    return BrowserFormPage.this.getEditorSite().getActionBars();
                }

                public void openInExternalBrowser(java.lang.String url) {
                    // ignore
                }
            });
            managedForm.getForm().setContent(browserViewer);
            java.lang.String url = getUrl();
            if (url != null) {
                browserViewer.setURL(url);
            }
        } catch (org.eclipse.swt.SWTError e) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Could not create browser page: "// $NON-NLS-1$
             + e.getMessage(), e));
        } catch (java.lang.RuntimeException e) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Could not create browser page", e));// $NON-NLS-1$

        }
    }

    /**
     * Returns a reference to the browser control.
     */
    public org.eclipse.swt.browser.Browser getBrowser() {
        if (browserViewer == null) {
            return null;
        }
        return browserViewer.getBrowser();
    }

    /**
     * Returns the initial URL that is displayed in the browser control. The default implementation tries to determine
     * the URL from the editor input.
     * <p>
     * Subclasses should override this method to display a specific URL.
     *
     * @return the URL to load when the page is created; null, if no URL should be loaded
     */
    protected java.lang.String getUrl() {
        org.eclipse.ui.IEditorInput input = getEditorInput();
        if (input instanceof org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput) {
            return ((org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput) (input)).getTask().getUrl();
        }
        return null;
    }

    @java.lang.Override
    public void init(org.eclipse.ui.IEditorSite site, org.eclipse.ui.IEditorInput input) {
        super.init(site, input);
        if (input instanceof org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput) {
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskDataManager().setTaskRead(((org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput) (input)).getTask(), true);
        }
    }

    @java.lang.Override
    protected void refresh() {
        init(getEditorSite(), getEditorInput());
    }
}