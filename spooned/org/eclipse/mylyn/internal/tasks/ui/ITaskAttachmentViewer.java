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
package org.eclipse.mylyn.internal.tasks.ui;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.tasks.core.ITaskAttachment;
import org.eclipse.ui.IWorkbenchPage;
/**
 * Simple interface for attachment 'viewers'. Most viewers are based on existing eclipse editors.
 *
 * @author Peter Stibrany
 */
public interface ITaskAttachmentViewer {
    /**
     *
     * @return arbitrary string, used to remember preferred viewer
     */
    public java.lang.String getId();

    /**
     *
     * @return name of the editor, displayed to user
     */
    public java.lang.String getLabel();

    public void openAttachment(org.eclipse.ui.IWorkbenchPage page, org.eclipse.mylyn.tasks.core.ITaskAttachment attachment) throws org.eclipse.core.runtime.CoreException;

    public boolean isWorkbenchDefault();
}