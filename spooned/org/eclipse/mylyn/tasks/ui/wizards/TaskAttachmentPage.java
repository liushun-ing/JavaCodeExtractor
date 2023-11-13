/**
 * *****************************************************************************
 * Copyright (c) 2004, 2014 Jeff Pound and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jeff Pound - initial API and implementation
 *     Tasktop Technologies - improvements
 *     Tomasz Zarna (IBM) - fixes for bug 364240
 * *****************************************************************************
 */
package org.eclipse.mylyn.tasks.ui.wizards;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.editors.CommentEditor;
import org.eclipse.mylyn.internal.tasks.ui.wizards.Messages;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentModel;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
/**
 * A wizard page to enter details of a new attachment.
 *
 * @author Jeff Pound
 * @author Mik Kersten
 * @author Steffen Pingel
 * @since 3.0
 */
public class TaskAttachmentPage extends org.eclipse.jface.wizard.WizardPage {
    private org.eclipse.swt.widgets.Button attachContextButton;

    private org.eclipse.mylyn.internal.tasks.ui.editors.CommentEditor commentEditor;

    private org.eclipse.swt.widgets.Text descriptionText;

    private org.eclipse.swt.widgets.Combo contentTypeList;

    private org.eclipse.swt.widgets.Text fileNameText;

    private org.eclipse.swt.widgets.Button isPatchButton;

    private final org.eclipse.mylyn.tasks.core.data.TaskAttachmentModel model;

    private boolean needsDescription;

    private final org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper taskAttachment;

    private boolean first = true;

    private boolean needsReplaceExisting;

    private org.eclipse.swt.widgets.Button replaceExistingButton;

    public TaskAttachmentPage(org.eclipse.mylyn.tasks.core.data.TaskAttachmentModel model) {
        super("AttachmentDetails");// $NON-NLS-1$

        this.model = model;
        this.taskAttachment = org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper.createFrom(model.getAttribute());
        setTitle(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.TaskAttachmentPage_Attachment_Details);
        setNeedsDescription(true);
    }

    public void createControl(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.widgets.Composite composite = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NONE);
        org.eclipse.swt.layout.GridLayout gridLayout = new org.eclipse.swt.layout.GridLayout();
        gridLayout.numColumns = 3;
        composite.setLayout(gridLayout);
        setControl(composite);
        composite.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.FILL, true, true));
        composite.setLayout(new org.eclipse.swt.layout.GridLayout(3, false));
        new org.eclipse.swt.widgets.Label(composite, org.eclipse.swt.SWT.NONE).setText(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.TaskAttachmentPage_File);
        fileNameText = new org.eclipse.swt.widgets.Text(composite, org.eclipse.swt.SWT.BORDER);
        fileNameText.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.TOP, true, false, 2, 1));
        if (needsReplaceExisting) {
            new org.eclipse.swt.widgets.Label(composite, org.eclipse.swt.SWT.NONE);
            replaceExistingButton = new org.eclipse.swt.widgets.Button(composite, org.eclipse.swt.SWT.CHECK);
            replaceExistingButton.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.SWT.DEFAULT, org.eclipse.swt.SWT.DEFAULT, false, false, 2, 1));
            replaceExistingButton.setText(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.TaskAttachmentPage_Replace_existing_attachment_Label);
            replaceExistingButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                @java.lang.Override
                public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                    taskAttachment.setReplaceExisting(replaceExistingButton.getSelection());
                    validate();
                }
            });
        }
        if (needsDescription) {
            new org.eclipse.swt.widgets.Label(composite, org.eclipse.swt.SWT.NONE).setText(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.TaskAttachmentPage_Description);
            descriptionText = new org.eclipse.swt.widgets.Text(composite, org.eclipse.swt.SWT.BORDER);
            descriptionText.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.TOP, true, false, 2, 1));
            descriptionText.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
                public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
                    taskAttachment.setDescription(descriptionText.getText().trim());
                    validate();
                }
            });
        }
        org.eclipse.swt.widgets.Label label = new org.eclipse.swt.widgets.Label(composite, org.eclipse.swt.SWT.NONE);
        label.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.SWT.LEFT, org.eclipse.swt.SWT.FILL, false, false));
        label.setText(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.TaskAttachmentPage_Comment);
        commentEditor = new org.eclipse.mylyn.internal.tasks.ui.editors.CommentEditor(getModel().getTask(), getModel().getTaskRepository()) {
            @java.lang.Override
            protected void valueChanged(java.lang.String value) {
                apply();
            }
        };
        commentEditor.createControl(composite);
        new org.eclipse.swt.widgets.Label(composite, org.eclipse.swt.SWT.NONE).setText(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.TaskAttachmentPage_Content_Type);// .setBackground(parent.getBackground());

        contentTypeList = new org.eclipse.swt.widgets.Combo(composite, (org.eclipse.swt.SWT.BORDER | org.eclipse.swt.SWT.DROP_DOWN) | org.eclipse.swt.SWT.READ_ONLY);
        contentTypeList.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.SWT.DEFAULT, org.eclipse.swt.SWT.DEFAULT, false, false, 2, 1));
        java.lang.String[] contentTypes = org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource.getContentTypes();
        int selection = 0;
        for (int i = 0; i < contentTypes.length; i++) {
            java.lang.String next = contentTypes[i];
            contentTypeList.add(next);
            if (next.equalsIgnoreCase(model.getContentType())) {
                selection = i;
            }
        }
        /* Update attachment on select content type */
        contentTypeList.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
            public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
                // ignore
            }

            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                taskAttachment.setContentType(contentTypeList.getItem(contentTypeList.getSelectionIndex()));
                validate();
            }
        });
        contentTypeList.select(selection);
        taskAttachment.setContentType(contentTypeList.getItem(selection));
        // TODO: is there a better way to pad?
        new org.eclipse.swt.widgets.Label(composite, org.eclipse.swt.SWT.NONE);
        isPatchButton = new org.eclipse.swt.widgets.Button(composite, org.eclipse.swt.SWT.CHECK);
        isPatchButton.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.SWT.DEFAULT, org.eclipse.swt.SWT.DEFAULT, false, false, 2, 1));
        isPatchButton.setText(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.TaskAttachmentPage_Patch);
        // TODO: is there a better way to pad?
        new org.eclipse.swt.widgets.Label(composite, org.eclipse.swt.SWT.NONE);
        attachContextButton = new org.eclipse.swt.widgets.Button(composite, org.eclipse.swt.SWT.CHECK);
        attachContextButton.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.SWT.DEFAULT, org.eclipse.swt.SWT.DEFAULT, false, false, 2, 1));
        attachContextButton.setText(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.TaskAttachmentPage_ATTACHE_CONTEXT);
        attachContextButton.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.CONTEXT_ATTACH));
        attachContextButton.setEnabled(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getContextStore().hasContext(model.getTask()));
        /* Attachment file name listener, update the local attachment
        accordingly
         */
        fileNameText.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
            public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
                // determine type by extension
                taskAttachment.setFileName(fileNameText.getText());
                setContentTypeFromFilename(fileNameText.getText());
                validate();
            }
        });
        /* Listener for isPatch */
        isPatchButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            private int lastSelected;

            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                taskAttachment.setPatch(isPatchButton.getSelection());
                if (isPatchButton.getSelection()) {
                    lastSelected = contentTypeList.getSelectionIndex();
                    contentTypeList.select(0);
                    contentTypeList.setEnabled(false);
                    if (attachContextButton.isEnabled()) {
                        attachContextButton.setSelection(true);
                    }
                } else {
                    contentTypeList.setEnabled(true);
                    contentTypeList.select(lastSelected);
                }
                validate();
            }
        });
        attachContextButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                validate();
            }
        });
        validate();
        setErrorMessage(null);
        if (descriptionText != null) {
            descriptionText.setFocus();
        } else {
            commentEditor.getTextEditor().getControl().setFocus();
        }
        org.eclipse.jface.dialogs.Dialog.applyDialogFont(composite);
    }

    private void validate() {
        apply();
        if ((fileNameText != null) && "".equals(fileNameText.getText().trim())) {
            // $NON-NLS-1$
            setMessage(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.TaskAttachmentPage_Enter_a_file_name);
            setPageComplete(false);
        } else if ((descriptionText != null) && "".equals(descriptionText.getText().trim())) {
            // $NON-NLS-1$
            setMessage(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.TaskAttachmentPage_Enter_a_description);
            setPageComplete(false);
        } else {
            setMessage(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.TaskAttachmentPage_Verify_the_content_type_of_the_attachment);
            setPageComplete(true);
        }
    }

    public org.eclipse.mylyn.tasks.core.data.TaskAttachmentModel getModel() {
        return model;
    }

    private void apply() {
        taskAttachment.applyTo(model.getAttribute());
        model.setComment(commentEditor.getText());
        model.setAttachContext(attachContextButton.getSelection());
        model.setContentType(taskAttachment.getContentType());
    }

    private void setContentType(java.lang.String contentType) {
        java.lang.String[] typeList = contentTypeList.getItems();
        for (int i = 0; i < typeList.length; i++) {
            if (typeList[i].equals(contentType)) {
                contentTypeList.select(i);
                taskAttachment.setContentType(contentType);
                validate();
                break;
            }
        }
    }

    private void setContentTypeFromFilename(java.lang.String fileName) {
        setContentType(org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource.getContentTypeFromFilename(fileName));
    }

    private void setFilePath(java.lang.String path) {
        fileNameText.setText(path);
        taskAttachment.setFileName(path);
        if (path.endsWith(".patch")) {
            // $NON-NLS-1$
            isPatchButton.setSelection(true);
            taskAttachment.setPatch(true);
            if (attachContextButton.isEnabled()) {
                attachContextButton.setSelection(true);
            }
        }
        validate();
    }

    public void setNeedsDescription(boolean supportsDescription) {
        this.needsDescription = supportsDescription;
    }

    /**
     *
     * @deprecated Use {@link #needsDescription()} instead
     */
    @java.lang.Deprecated
    public boolean supportsDescription() {
        return needsDescription();
    }

    /**
     * Returns true if the page has a description field.
     *
     * @since 3.4
     * @see #setDescription(String)
     */
    public boolean needsDescription() {
        return needsDescription;
    }

    @java.lang.Override
    public void setVisible(boolean visible) {
        if (visible) {
            if (!fileNameText.getText().equals(taskAttachment.getFileName())) {
                fileNameText.setText(taskAttachment.getFileName() == null ? "" : taskAttachment.getFileName());// $NON-NLS-1$

                if (fileNameText.getText().length() == 0) {
                    setFilePath(getModel().getSource().getName());
                    setContentType(getModel().getSource().getContentType());
                }
            }
        }
        super.setVisible(visible);
        if (first) {
            if (descriptionText != null) {
                descriptionText.setFocus();
            } else {
                commentEditor.getTextEditor().getControl().setFocus();
            }
            first = false;
        }
    }

    @java.lang.Override
    public void dispose() {
        super.dispose();
        commentEditor.dispose();
    }

    /**
     * Set to true if the page needs a check box for replacing existing attachments. Must be called before the page is
     * constructed, it has no effect otherwise.
     * <p>
     * This flag is set to false by default.
     * </p>
     *
     * @since 3.4
     * @see #needsReplaceExisting()
     */
    public void setNeedsReplaceExisting(boolean needsReplaceExisting) {
        this.needsReplaceExisting = needsReplaceExisting;
    }

    /**
     * Returns true, if the page has a check box to replace existing attachments.
     *
     * @since 3.4
     * @see #setNeedsReplaceExisting(boolean)
     */
    public boolean needsReplaceExisting() {
        return needsReplaceExisting;
    }
}