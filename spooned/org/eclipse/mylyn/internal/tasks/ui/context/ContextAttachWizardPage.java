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
package org.eclipse.mylyn.internal.tasks.ui.context;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
/**
 *
 * @author Rob Elves
 */
public class ContextAttachWizardPage extends org.eclipse.jface.wizard.WizardPage {
    private final org.eclipse.mylyn.tasks.core.TaskRepository repository;

    private final org.eclipse.mylyn.tasks.core.ITask task;

    private org.eclipse.swt.widgets.Text commentText;

    protected ContextAttachWizardPage(org.eclipse.mylyn.tasks.core.TaskRepository repository, org.eclipse.mylyn.tasks.core.ITask task) {
        super(Messages.ContextAttachWizardPage_Enter_comment);
        this.repository = repository;
        this.task = task;
        setTitle(Messages.ContextAttachWizardPage_Enter_comment);
        setDescription(Messages.ContextAttachWizardPage_Attaches_local_context_to_repository_task);
    }

    public void createControl(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.widgets.Composite composite = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NONE);
        composite.setLayout(new org.eclipse.swt.layout.GridLayout());
        org.eclipse.swt.widgets.Text summary = new org.eclipse.swt.widgets.Text(composite, org.eclipse.swt.SWT.NONE);
        summary.setText(org.eclipse.osgi.util.NLS.bind(Messages.ContextAttachWizardPage_Task, task.getSummary()));
        summary.setEditable(false);
        org.eclipse.swt.widgets.Text repositoryText = new org.eclipse.swt.widgets.Text(composite, org.eclipse.swt.SWT.NONE);
        repositoryText.setText(Messages.ContextAttachWizardPage_Repository_ + repository.getRepositoryUrl());
        repositoryText.setEditable(false);
        new org.eclipse.swt.widgets.Label(composite, org.eclipse.swt.SWT.NONE).setText(Messages.ContextAttachWizardPage_Comment_);
        commentText = new org.eclipse.swt.widgets.Text(composite, ((org.eclipse.swt.SWT.V_SCROLL | org.eclipse.swt.SWT.MULTI) | org.eclipse.swt.SWT.BORDER) | org.eclipse.swt.SWT.WRAP);
        commentText.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.FILL, true, true));
        commentText.addKeyListener(new org.eclipse.swt.events.KeyListener() {
            public void keyPressed(org.eclipse.swt.events.KeyEvent e) {
                getWizard().getContainer().updateButtons();
            }

            public void keyReleased(org.eclipse.swt.events.KeyEvent e) {
                getWizard().getContainer().updateButtons();
            }
        });
        setControl(composite);
        commentText.setFocus();
        org.eclipse.jface.dialogs.Dialog.applyDialogFont(composite);
    }

    public java.lang.String getComment() {
        return commentText.getText();
    }
}