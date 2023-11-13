/**
 * *****************************************************************************
 * Copyright (c) 2004, 2010 Tasktop Technologies and others.
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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.internal.commons.core.ZipFileUtil;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
/**
 * Zips task data up to specified directly and filename.
 *
 * @author Wesley Coelho
 * @author Mik Kersten
 * @author Rob Elves TODO: Move into internal.tasks.core
 */
@java.lang.SuppressWarnings("restriction")
public class TaskDataExportOperation implements org.eclipse.jface.operation.IRunnableWithProgress {
    private static final java.lang.String EXPORT_JOB_LABEL = Messages.TaskDataExportOperation_exporting_task_data;

    private static final java.util.regex.Pattern excludePattern = java.util.regex.Pattern.compile("(?:^\\.|^monitor-log.xml\\z|^tasklist.xml.zip\\z|attachments\\z|backup\\z)");// $NON-NLS-1$


    private final java.lang.String destinationDirectory;

    private final java.lang.String destinationFilename;

    public TaskDataExportOperation(java.lang.String destinationDirectory, java.lang.String destinationFilename) {
        this.destinationFilename = destinationFilename;
        this.destinationDirectory = destinationDirectory;
    }

    public void run(org.eclipse.core.runtime.IProgressMonitor monitor) throws java.lang.reflect.InvocationTargetException {
        monitor = org.eclipse.mylyn.commons.net.Policy.monitorFor(monitor);
        java.util.Set<java.io.File> filesToExport = new java.util.HashSet<java.io.File>();
        selectFiles(filesToExport);
        if ((filesToExport.size() > 0) && org.eclipse.core.runtime.Platform.isRunning()) {
            try {
                monitor.beginTask(org.eclipse.mylyn.internal.tasks.ui.util.TaskDataExportOperation.EXPORT_JOB_LABEL, filesToExport.size() + 1);
                org.eclipse.core.runtime.jobs.Job.getJobManager().beginRule(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.ROOT_SCHEDULING_RULE, new org.eclipse.core.runtime.SubProgressMonitor(monitor, 1));
                org.eclipse.mylyn.internal.commons.core.ZipFileUtil.createZipFile(getDestinationFile(), new java.util.ArrayList<java.io.File>(filesToExport), org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getDataDirectory(), monitor);
            } catch (java.io.IOException e) {
                throw new java.lang.reflect.InvocationTargetException(e);
            } finally {
                org.eclipse.core.runtime.jobs.Job.getJobManager().endRule(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.ROOT_SCHEDULING_RULE);
                monitor.done();
            }
        }
    }

    public java.io.File getDestinationFile() {
        return new java.io.File((destinationDirectory + java.io.File.separator) + destinationFilename);
    }

    protected void selectFiles(java.util.Set<java.io.File> filesToExport) {
        java.util.Set<java.util.regex.Pattern> exclusionPatterns = new java.util.HashSet<java.util.regex.Pattern>();
        exclusionPatterns.add(org.eclipse.mylyn.internal.tasks.ui.util.TaskDataExportOperation.excludePattern);
        java.lang.String dataRoot = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getDataDirectory();
        java.io.File dataFolder = new java.io.File(dataRoot);
        for (java.io.File file : dataFolder.listFiles()) {
            boolean exclude = false;
            for (java.util.regex.Pattern pattern : exclusionPatterns) {
                if (pattern.matcher(file.getName()).find()) {
                    exclude = true;
                    break;
                }
            }
            if (!exclude) {
                filesToExport.add(file);
            }
        }
    }

    protected java.io.File getSourceFolder() {
        return new java.io.File(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getDataDirectory());
    }
}