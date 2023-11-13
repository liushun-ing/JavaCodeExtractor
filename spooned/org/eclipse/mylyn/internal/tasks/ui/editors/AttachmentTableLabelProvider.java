/**
 * *****************************************************************************
 * Copyright (c) 2004, 2015 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Frank Becker - indicate deprecated attachments, bug 215549
 *     Perforce - fixes for bug 318505
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.editors;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.commons.ui.compatibility.CommonThemes;
import org.eclipse.mylyn.commons.workbench.CommonImageManger;
import org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil;
import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.ITaskAttachment;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.editors.AttributeEditorToolkit;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.IThemeManager;
/**
 *
 * @author Mik Kersten
 * @author Steffen Pingel
 * @author Kevin Sawicki
 */
public class AttachmentTableLabelProvider extends org.eclipse.jface.viewers.ColumnLabelProvider {
    private final org.eclipse.mylyn.internal.tasks.ui.editors.AttachmentSizeFormatter sizeFormatter = org.eclipse.mylyn.internal.tasks.ui.editors.AttachmentSizeFormatter.getInstance();

    private final org.eclipse.mylyn.tasks.core.data.TaskDataModel model;

    private final org.eclipse.mylyn.tasks.ui.editors.AttributeEditorToolkit attributeEditorToolkit;

    private final org.eclipse.mylyn.commons.workbench.CommonImageManger imageManager;

    public AttachmentTableLabelProvider(org.eclipse.mylyn.tasks.core.data.TaskDataModel model, org.eclipse.mylyn.tasks.ui.editors.AttributeEditorToolkit attributeEditorToolkit) {
        this.model = model;
        this.attributeEditorToolkit = attributeEditorToolkit;
        this.imageManager = new org.eclipse.mylyn.commons.workbench.CommonImageManger();
    }

    public org.eclipse.swt.graphics.Image getColumnImage(java.lang.Object element, int columnIndex) {
        org.eclipse.mylyn.tasks.core.ITaskAttachment attachment = ((org.eclipse.mylyn.tasks.core.ITaskAttachment) (element));
        if (columnIndex == 0) {
            if (org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.isContext(attachment)) {
                return imageManager.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.CONTEXT_TRANSFER);
            } else if (attachment.isPatch()) {
                return imageManager.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.TASK_ATTACHMENT_PATCH);
            } else {
                return imageManager.getFileImage(attachment.getFileName());
            }
        } else if ((columnIndex == 3) && (attachment.getAuthor() != null)) {
            return getAuthorImage(attachment.getAuthor(), attachment.getTaskRepository());
        }
        return null;
    }

    /**
     * Get author image for a specified repository person and task repository
     *
     * @param person
     * @param repository
     * @return author image
     */
    protected org.eclipse.swt.graphics.Image getAuthorImage(org.eclipse.mylyn.tasks.core.IRepositoryPerson person, org.eclipse.mylyn.tasks.core.TaskRepository repository) {
        if (((repository != null) && (person != null)) && person.matchesUsername(repository.getUserName())) {
            return imageManager.getImage(org.eclipse.mylyn.commons.ui.CommonImages.PERSON_ME);
        } else {
            return imageManager.getImage(org.eclipse.mylyn.commons.ui.CommonImages.PERSON);
        }
    }

    public java.lang.String getColumnText(java.lang.Object element, int columnIndex) {
        org.eclipse.mylyn.tasks.core.ITaskAttachment attachment = ((org.eclipse.mylyn.tasks.core.ITaskAttachment) (element));
        switch (columnIndex) {
            case 0 :
                if (org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.isContext(attachment)) {
                    return Messages.AttachmentTableLabelProvider_Task_Context;
                } else if (attachment.isPatch()) {
                    return Messages.AttachmentTableLabelProvider_Patch;
                } else {
                    return " " + attachment.getFileName();// $NON-NLS-1$

                }
            case 1 :
                return attachment.getDescription();
            case 2 :
                java.lang.Long length = attachment.getLength();
                if (length < 0) {
                    return "-";// $NON-NLS-1$

                }
                return sizeFormatter.format(length);
            case 3 :
                return attachment.getAuthor() != null ? attachment.getAuthor().toString() : "";// $NON-NLS-1$

            case 4 :
                return attachment.getCreationDate() != null ? org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.formatDateTime(attachment.getCreationDate()) : "";// $NON-NLS-1$

            case 5 :
                // FIXME add id to ITaskAttachment
                return org.eclipse.mylyn.internal.tasks.ui.editors.AttachmentTableLabelProvider.getAttachmentId(attachment);
        }
        return "unrecognized column";// $NON-NLS-1$

    }

    static java.lang.String getAttachmentId(org.eclipse.mylyn.tasks.core.ITaskAttachment attachment) {
        java.lang.String a = attachment.getUrl();
        if (a != null) {
            int i = a.indexOf("?id=");// $NON-NLS-1$

            if (i != (-1)) {
                return a.substring(i + 4);
            }
        }
        return "";// $NON-NLS-1$

    }

    @java.lang.Override
    public void addListener(org.eclipse.jface.viewers.ILabelProviderListener listener) {
        // ignore
    }

    @java.lang.Override
    public void dispose() {
        imageManager.dispose();
    }

    @java.lang.Override
    public boolean isLabelProperty(java.lang.Object element, java.lang.String property) {
        // ignore
        return false;
    }

    @java.lang.Override
    public void removeListener(org.eclipse.jface.viewers.ILabelProviderListener listener) {
        // ignore
    }

    @java.lang.Override
    public org.eclipse.swt.graphics.Color getForeground(java.lang.Object element) {
        org.eclipse.mylyn.tasks.core.ITaskAttachment att = ((org.eclipse.mylyn.tasks.core.ITaskAttachment) (element));
        if (att.isDeprecated()) {
            org.eclipse.ui.themes.IThemeManager themeManager = org.eclipse.ui.PlatformUI.getWorkbench().getThemeManager();
            return themeManager.getCurrentTheme().getColorRegistry().get(org.eclipse.mylyn.commons.ui.compatibility.CommonThemes.COLOR_COMPLETED);
        }
        return super.getForeground(element);
    }

    @java.lang.Override
    public java.lang.String getToolTipText(java.lang.Object element) {
        org.eclipse.mylyn.tasks.core.ITaskAttachment attachment = ((org.eclipse.mylyn.tasks.core.ITaskAttachment) (element));
        java.lang.StringBuilder sb = new java.lang.StringBuilder();
        sb.append(Messages.AttachmentTableLabelProvider_File_);
        sb.append(attachment.getFileName());
        if (attachment.getContentType() != null) {
            sb.append("\n");// $NON-NLS-1$

            sb.append(Messages.AttachmentTableLabelProvider_Type_);
            sb.append(attachment.getContentType());
        }
        return sb.toString();
        /* "\nFilename\t\t"  + attachment.getAttributeValue("filename")
        +"ID\t\t\t"        + attachment.getAttributeValue("attachid")
        + "\nDate\t\t\t"    + attachment.getAttributeValue("date")
        + "\nDescription\t" + attachment.getAttributeValue("desc")
        + "\nCreator\t\t"   + attachment.getCreator()
        + "\nType\t\t\t"    + attachment.getAttributeValue("type")
        + "\nURL\t\t\t"     + attachment.getAttributeValue("task.common.attachment.url");
         */
    }

    @java.lang.Override
    public org.eclipse.swt.graphics.Point getToolTipShift(java.lang.Object object) {
        return new org.eclipse.swt.graphics.Point(5, 5);
    }

    @java.lang.Override
    public int getToolTipDisplayDelayTime(java.lang.Object object) {
        return 200;
    }

    @java.lang.Override
    public int getToolTipTimeDisplayed(java.lang.Object object) {
        return 5000;
    }

    @java.lang.Override
    public void update(org.eclipse.jface.viewers.ViewerCell cell) {
        java.lang.Object element = cell.getElement();
        cell.setText(getColumnText(element, cell.getColumnIndex()));
        org.eclipse.swt.graphics.Image image = getColumnImage(element, cell.getColumnIndex());
        cell.setImage(image);
        cell.setBackground(getBackground(element));
        cell.setForeground(getForeground(element));
        cell.setFont(getFont(element));
    }

    @java.lang.Override
    public org.eclipse.swt.graphics.Color getBackground(java.lang.Object element) {
        if ((model != null) && (attributeEditorToolkit != null)) {
            org.eclipse.mylyn.tasks.core.ITaskAttachment attachment = ((org.eclipse.mylyn.tasks.core.ITaskAttachment) (element));
            if (model.hasIncomingChanges(attachment.getTaskAttribute())) {
                return attributeEditorToolkit.getColorIncoming();
            }
        }
        return null;
    }
}