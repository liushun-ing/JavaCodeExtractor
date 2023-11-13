/**
 * *****************************************************************************
 * Copyright (c) 2004, 2010 Jeff Pound and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jeff Pound - initial API and implementation
 *     Tasktop Technologies - improvements
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.editors;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.tasks.core.ITaskAttachment;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
/**
 *
 * @author Jeff Pound
 * @author Steffen Pingel
 * @deprecated use TaskAttachmentViewersManager instead
 */
@java.lang.Deprecated
public class TaskAttachmentEditorInput extends org.eclipse.core.runtime.PlatformObject implements org.eclipse.ui.IStorageEditorInput {
    private final org.eclipse.mylyn.tasks.core.ITaskAttachment attachment;

    public TaskAttachmentEditorInput(org.eclipse.mylyn.tasks.core.ITaskAttachment attachment) {
        this.attachment = attachment;
    }

    public boolean exists() {
        return true;
    }

    public org.eclipse.jface.resource.ImageDescriptor getImageDescriptor() {
        // ignore
        return null;
    }

    public java.lang.String getName() {
        return attachment.getFileName();
    }

    public org.eclipse.ui.IPersistableElement getPersistable() {
        return null;
    }

    public java.lang.String getToolTipText() {
        if (attachment.getUrl() != null) {
            return attachment.getUrl();
        } else {
            return getName();
        }
    }

    public org.eclipse.core.resources.IStorage getStorage() throws org.eclipse.core.runtime.CoreException {
        return org.eclipse.mylyn.internal.tasks.ui.editors.TaskAttachmentStorage.create(attachment);
    }
}