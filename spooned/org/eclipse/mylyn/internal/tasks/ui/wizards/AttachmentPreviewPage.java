/**
 * *****************************************************************************
 * Copyright (c) 2004, 2011 Jeff Pound and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jeff Pound - initial API and implementation
 *     Tasktop Technologies - improvement
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.wizards;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Text;
/**
 * Shows a preview of an attachment.
 *
 * @author Jeff Pound
 * @author Steffen Pingel
 */
public class AttachmentPreviewPage extends org.eclipse.jface.wizard.WizardPage {
    private static final java.lang.String DIALOG_SETTING_RUN_IN_BACKGROUND = "run-in-background";// $NON-NLS-1$


    private static final java.lang.String DIALOG_SETTINGS_SECTION_ATTACHMENTS_WIZARD = "attachments-wizard";// $NON-NLS-1$


    protected static final int MAX_TEXT_SIZE = 50000;

    private static final java.lang.String PAGE_NAME = "PreviewAttachmentPage";// $NON-NLS-1$


    private final java.util.Set<java.lang.String> imageTypes;

    private final org.eclipse.mylyn.tasks.core.data.TaskAttachmentModel model;

    private org.eclipse.swt.widgets.Button runInBackgroundButton;

    private org.eclipse.swt.custom.ScrolledComposite scrolledComposite;

    private final java.util.Set<java.lang.String> textTypes;

    private org.eclipse.swt.widgets.Composite contentComposite;

    public AttachmentPreviewPage(org.eclipse.mylyn.tasks.core.data.TaskAttachmentModel model) {
        super(org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentPreviewPage.PAGE_NAME);
        this.model = model;
        setTitle(Messages.AttachmentPreviewPage_Attachment_Preview);
        setDescription(Messages.AttachmentPreviewPage_Review_the_attachment_before_submitting);
        textTypes = new java.util.HashSet<java.lang.String>();
        textTypes.add("text/plain");// $NON-NLS-1$

        textTypes.add("text/html");// $NON-NLS-1$

        textTypes.add("application/xml");// $NON-NLS-1$

        imageTypes = new java.util.HashSet<java.lang.String>();
        imageTypes.add("image/jpeg");// $NON-NLS-1$

        imageTypes.add("image/gif");// $NON-NLS-1$

        imageTypes.add("image/png");// $NON-NLS-1$

        imageTypes.add("image/tiff");// $NON-NLS-1$

    }

    private void adjustScrollbars(org.eclipse.swt.graphics.Rectangle imgSize) {
        org.eclipse.swt.graphics.Rectangle clientArea = scrolledComposite.getClientArea();
        org.eclipse.swt.widgets.ScrollBar hBar = scrolledComposite.getHorizontalBar();
        hBar.setMinimum(0);
        hBar.setMaximum(imgSize.width - 1);
        hBar.setPageIncrement(clientArea.width);
        hBar.setIncrement(10);
        org.eclipse.swt.widgets.ScrollBar vBar = scrolledComposite.getVerticalBar();
        vBar.setMinimum(0);
        vBar.setMaximum(imgSize.height - 1);
        vBar.setPageIncrement(clientArea.height);
        vBar.setIncrement(10);
    }

    public void createControl(org.eclipse.swt.widgets.Composite parent) {
        final org.eclipse.swt.widgets.Composite composite = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NONE);
        composite.setLayout(new org.eclipse.swt.layout.GridLayout());
        setControl(composite);
        contentComposite = new org.eclipse.swt.widgets.Composite(composite, org.eclipse.swt.SWT.NONE);
        contentComposite.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_BOTH));
        contentComposite.setLayout(new org.eclipse.swt.layout.GridLayout());
        runInBackgroundButton = new org.eclipse.swt.widgets.Button(composite, org.eclipse.swt.SWT.CHECK);
        runInBackgroundButton.setText(Messages.AttachmentPreviewPage_Run_in_background);
        org.eclipse.jface.dialogs.IDialogSettings settings = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getDialogSettings();
        org.eclipse.jface.dialogs.IDialogSettings attachmentsSettings = settings.getSection(org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentPreviewPage.DIALOG_SETTINGS_SECTION_ATTACHMENTS_WIZARD);
        if (attachmentsSettings != null) {
            runInBackgroundButton.setSelection(attachmentsSettings.getBoolean(org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentPreviewPage.DIALOG_SETTING_RUN_IN_BACKGROUND));
        }
        org.eclipse.jface.dialogs.Dialog.applyDialogFont(composite);
    }

    @java.lang.Override
    public void dispose() {
        org.eclipse.jface.dialogs.IDialogSettings settings = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getDialogSettings();
        org.eclipse.jface.dialogs.IDialogSettings attachmentsSettings = settings.getSection(org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentPreviewPage.DIALOG_SETTINGS_SECTION_ATTACHMENTS_WIZARD);
        if (attachmentsSettings == null) {
            attachmentsSettings = settings.addNewSection(org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentPreviewPage.DIALOG_SETTINGS_SECTION_ATTACHMENTS_WIZARD);
        }
        attachmentsSettings.put(org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentPreviewPage.DIALOG_SETTING_RUN_IN_BACKGROUND, runInBackgroundButton.getSelection());
        super.dispose();
    }

    @java.lang.Override
    public void setVisible(boolean visible) {
        if (visible) {
            org.eclipse.swt.widgets.Control[] children = contentComposite.getChildren();
            for (org.eclipse.swt.widgets.Control control : children) {
                control.dispose();
            }
            if (isTextAttachment() || isImageAttachment()) {
                java.lang.Object content = getContent(contentComposite);
                if (content instanceof java.lang.String) {
                    createTextPreview(contentComposite, ((java.lang.String) (content)));
                } else if (content instanceof org.eclipse.swt.graphics.Image) {
                    createImagePreview(contentComposite, ((org.eclipse.swt.graphics.Image) (content)));
                }
            } else {
                createGenericPreview(contentComposite);
            }
            contentComposite.layout(true, true);
        }
        org.eclipse.jface.dialogs.Dialog.applyDialogFont(contentComposite);
        super.setVisible(visible);
    }

    private void createErrorPreview(org.eclipse.swt.widgets.Composite composite, java.lang.String message) {
        org.eclipse.swt.widgets.Label label = new org.eclipse.swt.widgets.Label(composite, org.eclipse.swt.SWT.NONE);
        label.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_BOTH));
        label.setText(message);
    }

    private void createGenericPreview(org.eclipse.swt.widgets.Composite composite) {
        org.eclipse.swt.widgets.Label label = new org.eclipse.swt.widgets.Label(composite, org.eclipse.swt.SWT.NONE);
        label.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_BOTH));
        // TODO 3.5 put filename on model
        java.lang.String name = model.getSource().getName();
        org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper taskAttachment = org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper.createFrom(model.getAttribute());
        if (taskAttachment.getFileName() != null) {
            name = taskAttachment.getFileName();
        }
        label.setText(java.text.MessageFormat.format(Messages.AttachmentPreviewPage_A_preview_the_type_X_is_currently_not_available, name, model.getContentType()));
    }

    private void createImagePreview(org.eclipse.swt.widgets.Composite composite, final org.eclipse.swt.graphics.Image bufferedImage) {
        scrolledComposite = new org.eclipse.swt.custom.ScrolledComposite(composite, (org.eclipse.swt.SWT.H_SCROLL | org.eclipse.swt.SWT.V_SCROLL) | org.eclipse.swt.SWT.BORDER);
        scrolledComposite.setLayoutData(org.eclipse.jface.layout.GridDataFactory.fillDefaults().grab(true, true).create());
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        org.eclipse.swt.widgets.Composite canvasComposite = new org.eclipse.swt.widgets.Composite(scrolledComposite, org.eclipse.swt.SWT.NONE);
        canvasComposite.setLayout(org.eclipse.jface.layout.GridLayoutFactory.fillDefaults().create());
        org.eclipse.swt.widgets.Canvas canvas = new org.eclipse.swt.widgets.Canvas(canvasComposite, org.eclipse.swt.SWT.NO_BACKGROUND);
        final org.eclipse.swt.graphics.Rectangle imgSize = bufferedImage.getBounds();
        canvas.setLayoutData(org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.CENTER, org.eclipse.swt.SWT.CENTER).grab(true, true).hint(imgSize.width, imgSize.height).create());
        canvas.addPaintListener(new org.eclipse.swt.events.PaintListener() {
            public void paintControl(org.eclipse.swt.events.PaintEvent event) {
                event.gc.drawImage(bufferedImage, 0, 0);
            }
        });
        canvas.addDisposeListener(new org.eclipse.swt.events.DisposeListener() {
            public void widgetDisposed(org.eclipse.swt.events.DisposeEvent event) {
                bufferedImage.dispose();
            }
        });
        canvas.setSize(imgSize.width, imgSize.height);
        scrolledComposite.setMinSize(imgSize.width, imgSize.height);
        scrolledComposite.setContent(canvasComposite);
        scrolledComposite.addControlListener(new org.eclipse.swt.events.ControlAdapter() {
            @java.lang.Override
            public void controlResized(org.eclipse.swt.events.ControlEvent event) {
                adjustScrollbars(imgSize);
            }
        });
        adjustScrollbars(imgSize);
    }

    private void createTextPreview(org.eclipse.swt.widgets.Composite composite, java.lang.String contents) {
        org.eclipse.swt.widgets.Text text = new org.eclipse.swt.widgets.Text(composite, (((org.eclipse.swt.SWT.MULTI | org.eclipse.swt.SWT.READ_ONLY) | org.eclipse.swt.SWT.V_SCROLL) | org.eclipse.swt.SWT.H_SCROLL) | org.eclipse.swt.SWT.BORDER);
        org.eclipse.swt.layout.GridData gd = new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_BOTH);
        gd.heightHint = composite.getBounds().y;
        gd.widthHint = composite.getBounds().x;
        text.setLayoutData(gd);
        text.setText(contents);
    }

    private java.lang.Object getContent(final org.eclipse.swt.widgets.Composite composite) {
        final java.lang.Object result[] = new java.lang.Object[1];
        try {
            getContainer().run(true, false, new org.eclipse.jface.operation.IRunnableWithProgress() {
                public void run(org.eclipse.core.runtime.IProgressMonitor monitor) throws java.lang.reflect.InvocationTargetException, java.lang.InterruptedException {
                    try {
                        monitor.beginTask(Messages.AttachmentPreviewPage_Preparing_preview, org.eclipse.core.runtime.IProgressMonitor.UNKNOWN);
                        final java.io.InputStream in = model.getSource().createInputStream(monitor);
                        try {
                            if (isTextAttachment()) {
                                java.lang.StringBuilder content = new java.lang.StringBuilder();
                                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(in));
                                java.lang.String line;
                                while ((((line = reader.readLine()) != null) && (content.length() < org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentPreviewPage.MAX_TEXT_SIZE)) && (!monitor.isCanceled())) {
                                    content.append(line);
                                    content.append("\n");// $NON-NLS-1$

                                } 
                                result[0] = content.toString();
                            } else if (isImageAttachment()) {
                                org.eclipse.swt.widgets.Display.getDefault().syncExec(new java.lang.Runnable() {
                                    public void run() {
                                        // Uses double buffering to paint the image; there was a weird behavior
                                        // with transparent images and flicker with large images
                                        org.eclipse.swt.graphics.Image originalImage = new org.eclipse.swt.graphics.Image(getShell().getDisplay(), in);
                                        final org.eclipse.swt.graphics.Image bufferedImage = new org.eclipse.swt.graphics.Image(getShell().getDisplay(), originalImage.getBounds());
                                        org.eclipse.swt.graphics.GC gc = new org.eclipse.swt.graphics.GC(bufferedImage);
                                        gc.setBackground(composite.getBackground());
                                        gc.fillRectangle(originalImage.getBounds());
                                        gc.drawImage(originalImage, 0, 0);
                                        gc.dispose();
                                        originalImage.dispose();
                                        result[0] = bufferedImage;
                                    }
                                });
                            }
                        } catch (java.io.IOException e) {
                            throw new java.lang.reflect.InvocationTargetException(e);
                        } finally {
                            try {
                                in.close();
                            } catch (java.io.IOException e) {
                                org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Failed to close file", e));// $NON-NLS-1$

                            }
                        }
                    } catch (org.eclipse.core.runtime.CoreException e) {
                        throw new java.lang.reflect.InvocationTargetException(e);
                    } finally {
                        monitor.done();
                    }
                }
            });
        } catch (java.lang.reflect.InvocationTargetException e) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Error generating preview", e));// $NON-NLS-1$

            createErrorPreview(composite, Messages.AttachmentPreviewPage_Could_not_create_preview);
            return null;
        } catch (java.lang.InterruptedException e) {
            return null;
        }
        return result[0];
    }

    private boolean isImageAttachment() {
        return imageTypes.contains(model.getContentType());
    }

    private boolean isTextAttachment() {
        return textTypes.contains(model.getContentType());
    }

    public boolean runInBackground() {
        return runInBackgroundButton.getSelection();
    }
}