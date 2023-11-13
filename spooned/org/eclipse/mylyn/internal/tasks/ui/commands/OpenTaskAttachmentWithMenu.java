/**
 * *****************************************************************************
 * Copyright (c) 2010, 2013 Peter Stibrany and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Peter Stibrany - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.commands;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.mylyn.internal.tasks.ui.ITaskAttachmentViewer;
import org.eclipse.mylyn.internal.tasks.ui.TaskAttachmentViewerManager;
import org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.ITaskAttachment;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
/**
 *
 * @author Peter Stibrany
 */
public class OpenTaskAttachmentWithMenu extends org.eclipse.jface.action.ContributionItem {
    private final org.eclipse.mylyn.internal.tasks.ui.TaskAttachmentViewerManager manager = new org.eclipse.mylyn.internal.tasks.ui.TaskAttachmentViewerManager();

    public OpenTaskAttachmentWithMenu() {
    }

    public OpenTaskAttachmentWithMenu(java.lang.String id) {
        super(id);
    }

    @java.lang.Override
    public void fill(org.eclipse.swt.widgets.Menu menu, int index) {
        java.util.List<org.eclipse.mylyn.tasks.core.ITaskAttachment> attachments = org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.getSelectedAttachments(null);
        if (attachments.isEmpty() || (attachments.size() > 1)) {
            return;
        }
        // find all interesting editors, and add them into menu
        org.eclipse.mylyn.tasks.core.ITaskAttachment attachment = attachments.get(0);
        java.lang.String viewerId = manager.getPreferredViewerID(attachment);
        int itemsAdded = 0;
        org.eclipse.mylyn.internal.tasks.ui.ITaskAttachmentViewer viewer = manager.getBrowserViewer(attachment);
        if (viewer != null) {
            itemsAdded = addItems(menu, index, java.util.Collections.singletonList(viewer), attachments, viewerId);
            index += itemsAdded;
        }
        int defaultViewerIndex = -1;
        java.util.List<org.eclipse.mylyn.internal.tasks.ui.ITaskAttachmentViewer> viewers = manager.getWorkbenchViewers(attachment);
        if (viewers.size() > 0) {
            itemsAdded = addSeparator(menu, index, itemsAdded);
            index += itemsAdded;
            for (int i = 0; i < viewers.size(); i++) {
                if (viewers.get(i).isWorkbenchDefault()) {
                    defaultViewerIndex = index + i;
                }
            }
            itemsAdded = addItems(menu, index, viewers, attachments, viewerId);
            index += itemsAdded;
        }
        viewers = manager.getSystemViewers(attachment);
        if (viewers.size() > 0) {
            itemsAdded = addSeparator(menu, index, itemsAdded);
            index += itemsAdded;
            if ((defaultViewerIndex == (-1)) && org.eclipse.core.runtime.Platform.getOS().equals(org.eclipse.core.runtime.Platform.OS_WIN32)) {
                for (int i = 0; i < viewers.size(); i++) {
                    if (org.eclipse.ui.IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID.equals(viewers.get(i).getId())) {
                        defaultViewerIndex = index + i;
                    }
                }
            }
            itemsAdded = addItems(menu, index, viewers, attachments, viewerId);
            index += itemsAdded;
        }
        if (defaultViewerIndex != (-1)) {
            boolean selectedPreferredViewer = false;
            for (org.eclipse.swt.widgets.MenuItem item : menu.getItems()) {
                if (item.getSelection()) {
                    selectedPreferredViewer = true;
                    break;
                }
            }
            if (!selectedPreferredViewer) {
                menu.getItem(defaultViewerIndex).setSelection(true);
            }
        }
    }

    protected int addSeparator(org.eclipse.swt.widgets.Menu menu, int index, int itemsAdded) {
        if (itemsAdded > 0) {
            new org.eclipse.swt.widgets.MenuItem(menu, org.eclipse.swt.SWT.SEPARATOR, index);
            return 1;
        }
        return 0;
    }

    protected int addItems(org.eclipse.swt.widgets.Menu menu, int index, java.util.List<org.eclipse.mylyn.internal.tasks.ui.ITaskAttachmentViewer> viewers, java.util.List<org.eclipse.mylyn.tasks.core.ITaskAttachment> attachments, java.lang.String viewerId) {
        int i = 0;
        for (org.eclipse.mylyn.internal.tasks.ui.ITaskAttachmentViewer viewer : viewers) {
            org.eclipse.swt.widgets.MenuItem item = new org.eclipse.swt.widgets.MenuItem(menu, org.eclipse.swt.SWT.RADIO, index + i);
            item.setText(viewer.getLabel());
            item.addSelectionListener(new org.eclipse.mylyn.internal.tasks.ui.commands.OpenTaskAttachmentWithMenu.RunAssociatedViewer(viewer, attachments));
            if ((viewerId != null) && viewerId.equals(viewer.getId())) {
                item.setSelection(true);
            }
            i++;
        }
        return i;
    }

    private class RunAssociatedViewer extends org.eclipse.swt.events.SelectionAdapter {
        private final org.eclipse.mylyn.internal.tasks.ui.ITaskAttachmentViewer viewer;

        private final java.util.List<org.eclipse.mylyn.tasks.core.ITaskAttachment> attachments;

        RunAssociatedViewer(org.eclipse.mylyn.internal.tasks.ui.ITaskAttachmentViewer handler, java.util.List<org.eclipse.mylyn.tasks.core.ITaskAttachment> attachments) {
            this.attachments = attachments;
            this.viewer = handler;
        }

        @java.lang.Override
        public void widgetSelected(org.eclipse.swt.events.SelectionEvent event) {
            if (!((org.eclipse.swt.widgets.MenuItem) (event.widget)).getSelection()) {
                // if the default viewer changes ignore the event for the unselected menu item
                return;
            }
            org.eclipse.ui.IWorkbenchWindow window = org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if (window != null) {
                org.eclipse.ui.IWorkbenchPage page = window.getActivePage();
                if (page != null) {
                    // this event is fired also for item which gets unselected (i.e. previous 'preferred' viewer)
                    try {
                        for (org.eclipse.mylyn.tasks.core.ITaskAttachment attachment : attachments) {
                            manager.savePreferredViewerID(attachment, viewer.getId());
                            try {
                                viewer.openAttachment(page, attachment);
                            } catch (org.eclipse.core.runtime.CoreException e) {
                                org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.logAndDisplayStatus(Messages.OpenTaskAttachmentHandler_failedToOpenViewer, e.getStatus());
                            }
                        }
                    } catch (org.eclipse.core.runtime.OperationCanceledException e) {
                        // canceled
                    }
                }
            }
        }
    }

    @java.lang.Override
    public boolean isDynamic() {
        // menu depends on selected attachment(s)
        return true;
    }
}