/**
 * *****************************************************************************
 * Copyright (c) 2004, 2012 Tasktop Technologies and others.
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
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil;
import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskAttachment;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TaskElementLabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
/**
 *
 * @author Rob Elves
 * @author Mik Kersten
 */
public class ContextRetrieveWizardPage extends org.eclipse.jface.wizard.WizardPage {
    private final org.eclipse.mylyn.tasks.core.TaskRepository repository;

    private final org.eclipse.mylyn.tasks.core.ITask task;

    private final org.eclipse.mylyn.tasks.ui.TaskElementLabelProvider labelProvider = new org.eclipse.mylyn.tasks.ui.TaskElementLabelProvider(false);

    private org.eclipse.mylyn.tasks.core.ITaskAttachment selectedContextAttachment;

    protected ContextRetrieveWizardPage(org.eclipse.mylyn.tasks.core.TaskRepository repository, org.eclipse.mylyn.tasks.core.ITask task) {
        super(Messages.ContextRetrieveWizardPage_Select_context);
        this.repository = repository;
        this.task = task;
        setDescription(Messages.ContextRetrieveWizardPage_SELECT_A_CONTEXT_TO_RETTRIEVE_FROM_TABLE_BELOW);
        setTitle(Messages.ContextRetrieveWizardPage_Select_context);
    }

    public void createControl(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.widgets.Composite composite = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NONE);
        composite.setLayout(new org.eclipse.swt.layout.GridLayout(1, false));
        org.eclipse.swt.widgets.Text summary = new org.eclipse.swt.widgets.Text(composite, org.eclipse.swt.SWT.NONE);
        summary.setText(org.eclipse.osgi.util.NLS.bind(Messages.ContextRetrieveWizardPage_Task, labelProvider.getText(task)));
        summary.setEditable(false);
        // new Label(composite, SWT.NONE).setText("Repository: " +
        // repository.getUrl());
        // new Label(composite, SWT.NONE).setText("Select context below:");
        final org.eclipse.swt.widgets.Table contextTable = new org.eclipse.swt.widgets.Table(composite, org.eclipse.swt.SWT.FULL_SELECTION | org.eclipse.swt.SWT.BORDER);
        contextTable.setHeaderVisible(true);
        contextTable.setLinesVisible(true);
        contextTable.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent event) {
                if (contextTable.getSelectionIndex() > (-1)) {
                    selectedContextAttachment = ((org.eclipse.mylyn.tasks.core.ITaskAttachment) (contextTable.getItem(contextTable.getSelectionIndex()).getData()));
                    getWizard().getContainer().updateButtons();
                }
            }
        });
        contextTable.addMouseListener(new org.eclipse.swt.events.MouseListener() {
            public void mouseDoubleClick(org.eclipse.swt.events.MouseEvent e) {
                selectedContextAttachment = ((org.eclipse.mylyn.tasks.core.ITaskAttachment) (contextTable.getItem(contextTable.getSelectionIndex()).getData()));
                getWizard().getContainer().updateButtons();
                getWizard().performFinish();
                // TODO: is there a better way of closing?
                getWizard().getContainer().getShell().close();
            }

            public void mouseDown(org.eclipse.swt.events.MouseEvent e) {
            }

            public void mouseUp(org.eclipse.swt.events.MouseEvent e) {
            }
        });
        java.util.List<org.eclipse.mylyn.tasks.core.ITaskAttachment> contextAttachments = org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.getContextAttachments(repository, task);
        java.util.Collections.sort(contextAttachments, new org.eclipse.mylyn.internal.tasks.ui.context.TaskAttachmentComparator());
        org.eclipse.swt.widgets.TableColumn[] columns = new org.eclipse.swt.widgets.TableColumn[3];
        columns[0] = new org.eclipse.swt.widgets.TableColumn(contextTable, org.eclipse.swt.SWT.LEFT);
        columns[0].setText(Messages.ContextRetrieveWizardPage_Date);
        columns[1] = new org.eclipse.swt.widgets.TableColumn(contextTable, org.eclipse.swt.SWT.LEFT);
        columns[1].setText(Messages.ContextRetrieveWizardPage_Author);
        columns[2] = new org.eclipse.swt.widgets.TableColumn(contextTable, org.eclipse.swt.SWT.CENTER);
        columns[2].setText(Messages.ContextRetrieveWizardPage_Description);
        for (org.eclipse.mylyn.tasks.core.ITaskAttachment attachment : contextAttachments) {
            org.eclipse.swt.widgets.TableItem item = new org.eclipse.swt.widgets.TableItem(contextTable, org.eclipse.swt.SWT.NONE);
            java.util.Date creationDate = attachment.getCreationDate();
            if (creationDate != null) {
                item.setText(0, com.ibm.icu.text.DateFormat.getInstance().format(creationDate));
            }
            org.eclipse.mylyn.tasks.core.IRepositoryPerson author = attachment.getAuthor();
            if (author != null) {
                item.setText(1, author.toString());
            }
            item.setText(2, attachment.getDescription());
            item.setData(attachment);
        }
        for (org.eclipse.swt.widgets.TableColumn column : columns) {
            column.pack();
        }
        contextTable.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_BOTH));
        setControl(composite);
        if (contextAttachments.size() > 0) {
            contextTable.setSelection(0);
            selectedContextAttachment = contextAttachments.get(0);
            getWizard().getContainer().updateButtons();
        }
        contextTable.setFocus();
        org.eclipse.jface.dialogs.Dialog.applyDialogFont(composite);
    }

    public org.eclipse.mylyn.tasks.core.ITaskAttachment getSelectedContext() {
        return selectedContextAttachment;
    }

    @java.lang.Override
    public boolean isPageComplete() {
        if (selectedContextAttachment == null) {
            return false;
        }
        return super.isPageComplete();
    }
}