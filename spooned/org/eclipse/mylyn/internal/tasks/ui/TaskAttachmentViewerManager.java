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
 *     Tasktop Technologies - improvements
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil;
import org.eclipse.mylyn.tasks.core.ITaskAttachment;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
/**
 *
 * @author Peter Stibrany
 */
public class TaskAttachmentViewerManager {
    public org.eclipse.mylyn.internal.tasks.ui.ITaskAttachmentViewer getBrowserViewer(org.eclipse.mylyn.tasks.core.ITaskAttachment attachment) {
        if ((attachment.getUrl() != null) && (attachment.getUrl().trim().length() > 0)) {
            return new org.eclipse.mylyn.internal.tasks.ui.TaskAttachmentBrowserViewer();
        }
        return null;
    }

    public java.util.List<org.eclipse.mylyn.internal.tasks.ui.ITaskAttachmentViewer> getWorkbenchViewers(org.eclipse.mylyn.tasks.core.ITaskAttachment attachment) {
        java.util.List<org.eclipse.mylyn.internal.tasks.ui.ITaskAttachmentViewer> result = new java.util.ArrayList<org.eclipse.mylyn.internal.tasks.ui.ITaskAttachmentViewer>();
        org.eclipse.ui.IEditorRegistry registry = org.eclipse.ui.PlatformUI.getWorkbench().getEditorRegistry();
        org.eclipse.ui.IEditorDescriptor defaultEditor = registry.getDefaultEditor(org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.getAttachmentFilename(attachment));
        if (defaultEditor != null) {
            result.add(new org.eclipse.mylyn.internal.tasks.ui.TaskAttachmentEditorViewer(defaultEditor, true));
        }
        org.eclipse.ui.IEditorDescriptor defaultTextEditor = registry.findEditor(org.eclipse.ui.editors.text.EditorsUI.DEFAULT_TEXT_EDITOR_ID);// may be null

        if ((defaultTextEditor != null) && ((defaultEditor == null) || (!defaultTextEditor.getId().equals(defaultEditor.getId())))) {
            result.add(new org.eclipse.mylyn.internal.tasks.ui.TaskAttachmentEditorViewer(defaultTextEditor));
        }
        org.eclipse.ui.IEditorDescriptor[] descriptors = registry.getEditors(org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.getAttachmentFilename(attachment));
        for (org.eclipse.ui.IEditorDescriptor ied : descriptors) {
            if ((defaultEditor == null) || (!ied.getId().equals(defaultEditor.getId()))) {
                result.add(new org.eclipse.mylyn.internal.tasks.ui.TaskAttachmentEditorViewer(ied));
            }
        }
        return result;
    }

    public java.util.List<org.eclipse.mylyn.internal.tasks.ui.ITaskAttachmentViewer> getSystemViewers(org.eclipse.mylyn.tasks.core.ITaskAttachment attachment) {
        java.util.List<org.eclipse.mylyn.internal.tasks.ui.ITaskAttachmentViewer> result = new java.util.ArrayList<org.eclipse.mylyn.internal.tasks.ui.ITaskAttachmentViewer>();
        org.eclipse.ui.IEditorRegistry registry = org.eclipse.ui.PlatformUI.getWorkbench().getEditorRegistry();
        // Don't check whether system external editor is available (IEditorRegistry.isSystemExternalEditorAvailable) ...
        // At least Windows can handle even unknown files, and offers user to choose correct program to open file with
        org.eclipse.ui.IEditorDescriptor extern = registry.findEditor(org.eclipse.ui.IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
        result.add(new org.eclipse.mylyn.internal.tasks.ui.TaskAttachmentEditorViewer(extern, false, true));
        if (registry.isSystemInPlaceEditorAvailable(org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.getAttachmentFilename(attachment))) {
            org.eclipse.ui.IEditorDescriptor inplace = registry.findEditor(org.eclipse.ui.IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID);
            result.add(new org.eclipse.mylyn.internal.tasks.ui.TaskAttachmentEditorViewer(inplace, false, true));
        }
        return result;
    }

    public java.util.List<org.eclipse.mylyn.internal.tasks.ui.ITaskAttachmentViewer> getTaskAttachmentViewers(org.eclipse.mylyn.tasks.core.ITaskAttachment attachment) {
        java.util.List<org.eclipse.mylyn.internal.tasks.ui.ITaskAttachmentViewer> result = new java.util.ArrayList<org.eclipse.mylyn.internal.tasks.ui.ITaskAttachmentViewer>();
        org.eclipse.mylyn.internal.tasks.ui.ITaskAttachmentViewer browserViewer = getBrowserViewer(attachment);
        if (browserViewer != null) {
            result.add(browserViewer);
        }
        result.addAll(getWorkbenchViewers(attachment));
        result.addAll(getSystemViewers(attachment));
        return result;
    }

    /**
     *
     * @param attachment
     * @return preferred attachment viewers, or null if no suitable viewer can be found
     */
    public org.eclipse.mylyn.internal.tasks.ui.ITaskAttachmentViewer getPreferredViewer(org.eclipse.mylyn.tasks.core.ITaskAttachment attachment) {
        /* Find viewers in order of preference: preferred, workbench default, system editor, first editor in list */
        java.util.List<org.eclipse.mylyn.internal.tasks.ui.ITaskAttachmentViewer> viewers = getTaskAttachmentViewers(attachment);
        org.eclipse.mylyn.internal.tasks.ui.ITaskAttachmentViewer defaultViewer = null;
        java.lang.String preferred = getPreferredViewerID(attachment);
        for (org.eclipse.mylyn.internal.tasks.ui.ITaskAttachmentViewer viewer : viewers) {
            if ((preferred != null) && preferred.equals(viewer.getId())) {
                return viewer;
            } else if (viewer.isWorkbenchDefault()) {
                defaultViewer = viewer;
            } else if (((defaultViewer == null) && org.eclipse.core.runtime.Platform.getOS().equals(org.eclipse.core.runtime.Platform.OS_WIN32)) && org.eclipse.ui.IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID.equals(viewer.getId())) {
                defaultViewer = viewer;
            }
        }
        if ((defaultViewer == null) && (!viewers.isEmpty())) {
            defaultViewer = viewers.get(0);
        }
        return defaultViewer;
    }

    public java.lang.String getPreferredViewerID(org.eclipse.mylyn.tasks.core.ITaskAttachment attachment) {
        java.lang.String ext = getExtension(attachment);
        if (ext == null) {
            return null;
        }
        return getPreferencesStore().getString((ITasksUiPreferenceConstants.PREFERRED_TASK_ATTACHMENT_VIEWER_ID + "_") + ext);// $NON-NLS-1$

    }

    private org.eclipse.jface.preference.IPreferenceStore getPreferencesStore() {
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore();
    }

    public void savePreferredViewerID(org.eclipse.mylyn.tasks.core.ITaskAttachment attachment, java.lang.String handlerID) {
        java.lang.String ext = getExtension(attachment);
        if (ext == null) {
            return;
        }
        getPreferencesStore().putValue((ITasksUiPreferenceConstants.PREFERRED_TASK_ATTACHMENT_VIEWER_ID + "_") + ext, handlerID);// $NON-NLS-1$

    }

    private java.lang.String getExtension(org.eclipse.mylyn.tasks.core.ITaskAttachment attachment) {
        if (attachment == null) {
            return null;
        }
        java.lang.String fname = org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil.getAttachmentFilename(attachment);
        int dot = fname.lastIndexOf('.');
        if (dot < 0) {
            return null;
        }
        return fname.substring(dot + 1);
    }
}