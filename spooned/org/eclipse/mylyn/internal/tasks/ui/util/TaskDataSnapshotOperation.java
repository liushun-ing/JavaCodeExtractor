/**
 * *****************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
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
/**
 *
 * @author Robert Elves
 */
public class TaskDataSnapshotOperation extends org.eclipse.mylyn.internal.tasks.ui.util.TaskDataExportOperation {
    public TaskDataSnapshotOperation(java.lang.String destinationDirectory, java.lang.String destinationFilename) {
        super(destinationDirectory, destinationFilename);
    }

    @java.lang.Override
    protected void selectFiles(java.util.Set<java.io.File> filesToExport) {
        filesToExport.add(new java.io.File(getSourceFolder(), "tasks.xml.zip"));// $NON-NLS-1$

        filesToExport.add(new java.io.File(getSourceFolder(), "repositories.xml.zip"));// $NON-NLS-1$

        filesToExport.add(new java.io.File(getSourceFolder(), "contexts/activity.xml.zip"));// $NON-NLS-1$

    }
}