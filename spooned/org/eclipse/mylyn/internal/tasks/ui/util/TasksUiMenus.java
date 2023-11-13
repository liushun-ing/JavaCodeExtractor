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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.mylyn.internal.tasks.ui.actions.SaveAttachmentsAction;
import org.eclipse.mylyn.tasks.core.ITaskAttachment;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
/**
 *
 * @author Steffen Pingel
 */
public class TasksUiMenus {
    public static void fillTaskAttachmentMenu(org.eclipse.jface.action.IMenuManager manager) {
        final org.eclipse.jface.action.Action saveAction = new org.eclipse.mylyn.internal.tasks.ui.actions.SaveAttachmentsAction(Messages.TasksUiMenus_Save_);
        final org.eclipse.jface.action.Action copyURLToClipAction = new org.eclipse.jface.action.Action(Messages.TasksUiMenus_Copy_URL) {
            @java.lang.Override
            public void run() {
                org.eclipse.mylyn.tasks.core.ITaskAttachment attachment = org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.getSelectedAttachment();
                if (attachment != null) {
                    org.eclipse.swt.dnd.Clipboard clip = new org.eclipse.swt.dnd.Clipboard(org.eclipse.ui.PlatformUI.getWorkbench().getDisplay());
                    clip.setContents(new java.lang.Object[]{ attachment.getUrl() }, new org.eclipse.swt.dnd.Transfer[]{ org.eclipse.swt.dnd.TextTransfer.getInstance() });
                    clip.dispose();
                }
            }

            @java.lang.Override
            public boolean isEnabled() {
                org.eclipse.mylyn.tasks.core.ITaskAttachment attachment = org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.getSelectedAttachment();
                if (attachment != null) {
                    return attachment.getUrl() != null;
                }
                return super.isEnabled();
            }
        };
        final org.eclipse.jface.action.Action copyToClipAction = new org.eclipse.jface.action.Action(Messages.TasksUiMenus_Copy_Contents) {
            @java.lang.Override
            public void run() {
                org.eclipse.mylyn.tasks.core.ITaskAttachment attachment = org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.getSelectedAttachment();
                if (attachment != null) {
                    org.eclipse.mylyn.internal.tasks.ui.util.CopyAttachmentToClipboardJob job = new org.eclipse.mylyn.internal.tasks.ui.util.CopyAttachmentToClipboardJob(attachment);
                    job.setUser(true);
                    job.schedule();
                }
            }
        };
        manager.add(new org.eclipse.jface.action.Separator("group.open"));// $NON-NLS-1$

        manager.add(new org.eclipse.jface.action.Separator("group.save"));// $NON-NLS-1$

        manager.add(saveAction);
        manager.add(copyURLToClipAction);
        manager.add(copyToClipAction);
        manager.add(new org.eclipse.jface.action.Separator(org.eclipse.ui.IWorkbenchActionConstants.MB_ADDITIONS));
    }
}