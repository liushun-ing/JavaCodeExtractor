/**
 * *****************************************************************************
 * Copyright (c) 2010, 2011 Peter Stibrany and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Peter Stibrany - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
/**
 *
 * @author Peter Stibrany
 */
public class AttachmentFileStorage extends org.eclipse.core.runtime.PlatformObject implements org.eclipse.core.resources.IStorage {
    private final java.io.File file;

    private final java.lang.String name;

    public AttachmentFileStorage(java.io.File file, java.lang.String name) {
        this.file = file;
        this.name = name;
    }

    public java.io.InputStream getContents() throws org.eclipse.core.runtime.CoreException {
        try {
            return new java.io.FileInputStream(file);
        } catch (java.io.FileNotFoundException e) {
            throw new org.eclipse.core.runtime.CoreException(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, TasksUiPlugin.ID_PLUGIN, Messages.FileStorage_unableToReadAttachmentFile, e));
        }
    }

    public org.eclipse.core.runtime.IPath getFullPath() {
        return org.eclipse.core.runtime.Path.fromOSString(file.getAbsolutePath());
    }

    public java.lang.String getName() {
        return name;
    }

    public boolean isReadOnly() {
        return true;
    }
}