/**
 * *****************************************************************************
 * Copyright (c) 2009, 2013 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.context;
import com.ibm.icu.text.DateFormat;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskAttachment;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
/**
 *
 * @author Steffen Pingel
 */
public class RetrieveLatestContextDialog extends org.eclipse.jface.dialogs.MessageDialog {
    public static boolean openQuestion(org.eclipse.swt.widgets.Shell shell, org.eclipse.mylyn.tasks.core.ITask task) {
        org.eclipse.mylyn.tasks.core.TaskRepository repository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(task.getConnectorKind(), task.getRepositoryUrl());
        java.util.List<org.eclipse.mylyn.tasks.core.ITaskAttachment> contextAttachments = org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.getContextAttachments(repository, task);
        java.util.Collections.sort(contextAttachments, new org.eclipse.mylyn.internal.tasks.ui.context.TaskAttachmentComparator());
        if (contextAttachments.size() > 0) {
            org.eclipse.mylyn.tasks.core.ITaskAttachment attachment = contextAttachments.get(0);
            java.lang.String author = null;
            if (attachment.getAuthor() != null) {
                author = (attachment.getAuthor().getName() != null) ? attachment.getAuthor().getName() : attachment.getAuthor().getPersonId();
            }
            if (author == null) {
                author = Messages.RetrieveLatestContextDialog_Unknown;
            }
            java.util.Date date = attachment.getCreationDate();
            java.lang.String dateString = null;
            if (date != null) {
                dateString = com.ibm.icu.text.DateFormat.getDateInstance(com.ibm.icu.text.DateFormat.LONG).format(date);
            }
            if (dateString == null) {
                dateString = Messages.RetrieveLatestContextDialog_Unknown;
            }
            java.lang.String message = org.eclipse.osgi.util.NLS.bind(Messages.RetrieveLatestContextDialog_No_local_context_exists, author, dateString);
            int kind = QUESTION;
            int style = org.eclipse.swt.SWT.NONE;
            org.eclipse.mylyn.internal.tasks.ui.context.RetrieveLatestContextDialog dialog = new org.eclipse.mylyn.internal.tasks.ui.context.RetrieveLatestContextDialog(shell, Messages.RetrieveLatestContextDialog_Dialog_Title, null, message, kind, new java.lang.String[]{ org.eclipse.jface.dialogs.IDialogConstants.OK_LABEL, org.eclipse.jface.dialogs.IDialogConstants.CANCEL_LABEL }, 0, task, attachment);
            style &= org.eclipse.swt.SWT.SHEET;
            dialog.setShellStyle(dialog.getShellStyle() | style);
            return dialog.open() == 0;
        }
        return false;
    }

    private final org.eclipse.mylyn.tasks.core.ITaskAttachment attachment;

    private org.eclipse.swt.widgets.Link link;

    private org.eclipse.mylyn.internal.tasks.ui.context.ProgressContainer progressContainer;

    private org.eclipse.jface.wizard.ProgressMonitorPart progressMonitorPart;

    private final org.eclipse.mylyn.tasks.core.ITask task;

    public RetrieveLatestContextDialog(org.eclipse.swt.widgets.Shell parentShell, java.lang.String dialogTitle, org.eclipse.swt.graphics.Image dialogTitleImage, java.lang.String dialogMessage, int dialogImageType, java.lang.String[] dialogButtonLabels, int defaultIndex, org.eclipse.mylyn.tasks.core.ITask task, org.eclipse.mylyn.tasks.core.ITaskAttachment attachment) {
        super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels, defaultIndex);
        this.task = task;
        this.attachment = attachment;
    }

    @java.lang.Override
    protected void buttonPressed(int buttonId) {
        if (progressContainer.isActive()) {
            return;
        }
        if (buttonId == 0) {
            if (!org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.downloadContext(task, attachment, progressContainer)) {
                // failed
                return;
            }
        }
        super.buttonPressed(buttonId);
    }

    @java.lang.Override
    protected org.eclipse.swt.widgets.Control createButtonBar(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.widgets.Composite composite = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NONE);
        composite.setLayout(org.eclipse.jface.layout.GridLayoutFactory.fillDefaults().numColumns(3).create());
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(composite);
        org.eclipse.swt.widgets.Control control = createLink(composite);
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().grab(true, false).applyTo(control);
        super.createButtonBar(composite);
        return composite;
    }

    @java.lang.Override
    protected org.eclipse.swt.widgets.Control createContents(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.widgets.Control control = super.createContents(parent);
        progressContainer.setCancelButton(getButton(1));
        getButton(0).setFocus();
        return control;
    }

    @java.lang.Override
    protected org.eclipse.swt.widgets.Control createCustomArea(org.eclipse.swt.widgets.Composite parent) {
        progressMonitorPart = new org.eclipse.jface.wizard.ProgressMonitorPart(parent, null);
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().grab(true, false).applyTo(progressMonitorPart);
        progressContainer = new org.eclipse.mylyn.internal.tasks.ui.context.ProgressContainer(getShell(), progressMonitorPart) {
            @java.lang.Override
            protected void restoreUiState(java.util.Map<java.lang.Object, java.lang.Object> state) {
                link.setEnabled(true);
                getButton(0).setEnabled(true);
                getButton(1).setEnabled(true);
            }

            @java.lang.Override
            protected void saveUiState(java.util.Map<java.lang.Object, java.lang.Object> savedState) {
                link.setEnabled(false);
                getButton(0).setEnabled(false);
            }
        };
        return progressMonitorPart;
    }

    protected org.eclipse.swt.widgets.Control createLink(org.eclipse.swt.widgets.Composite parent) {
        link = new org.eclipse.swt.widgets.Link(parent, org.eclipse.swt.SWT.NONE);
        link.setText(Messages.RetrieveLatestContextDialog_Show_All_Contexts_Label);
        link.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                close();
                org.eclipse.mylyn.internal.tasks.ui.context.ContextRetrieveWizard wizard = new org.eclipse.mylyn.internal.tasks.ui.context.ContextRetrieveWizard(task);
                org.eclipse.jface.wizard.WizardDialog dialog = new org.eclipse.jface.wizard.WizardDialog(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell(), wizard);
                dialog.create();
                dialog.setBlockOnOpen(true);
                setReturnCode(dialog.open());
            }
        });
        return link;
    }
}