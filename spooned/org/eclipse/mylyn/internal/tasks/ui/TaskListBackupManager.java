/**
 * *****************************************************************************
 * Copyright (c) 2004, 2013 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui;
import com.ibm.icu.text.SimpleDateFormat;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil;
import org.eclipse.mylyn.internal.tasks.ui.util.TaskDataExportOperation;
import org.eclipse.mylyn.internal.tasks.ui.util.TaskDataSnapshotOperation;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
/**
 *
 * @author Rob Elves
 */
public class TaskListBackupManager implements org.eclipse.jface.util.IPropertyChangeListener {
    private static final java.lang.String OLD_MYLYN_2_BACKUP_FILE_PREFIX = "mylyndata-";// $NON-NLS-1$


    // Mylyn 3.0 Backup file name
    private static final java.lang.String BACKUP_FILE_PREFIX = "mylyn-v3-data-";// $NON-NLS-1$


    private static final java.util.regex.Pattern MYLYN_BACKUP_REGEXP = java.util.regex.Pattern.compile(((("^(" + org.eclipse.mylyn.internal.tasks.ui.TaskListBackupManager.BACKUP_FILE_PREFIX) + ")?(")// $NON-NLS-1$ //$NON-NLS-2$
     + org.eclipse.mylyn.internal.tasks.ui.TaskListBackupManager.OLD_MYLYN_2_BACKUP_FILE_PREFIX) + ")?");// $NON-NLS-1$


    private static final java.util.regex.Pattern DATE_FORMAT_OLD = java.util.regex.Pattern.compile("\\d{4}-\\d{2}-\\d{2}");// $NON-NLS-1$


    private static final java.util.regex.Pattern DATE_FORMAT = java.util.regex.Pattern.compile("\\d{4}-\\d{2}-\\d{2}-\\d{6}");// $NON-NLS-1$


    private static final long SECOND = 1000;

    private static final long MINUTE = 60 * org.eclipse.mylyn.internal.tasks.ui.TaskListBackupManager.SECOND;

    private static final long STANDARD_DELAY = 30 * org.eclipse.mylyn.internal.tasks.ui.TaskListBackupManager.MINUTE;

    private java.lang.String backupFolderPath;

    private org.eclipse.core.runtime.jobs.Job runBackup;

    private static boolean errorDisplayed = false;

    public TaskListBackupManager(java.lang.String backupFolderPath) {
        this.backupFolderPath = backupFolderPath;
        start(org.eclipse.mylyn.internal.tasks.ui.TaskListBackupManager.STANDARD_DELAY);
    }

    public void start(long delay) {
        if (runBackup != null) {
            stop();
        }
        runBackup = new org.eclipse.core.runtime.jobs.Job("Task Data Snapshot") {
            // $NON-NLS-1$
            @java.lang.Override
            protected org.eclipse.core.runtime.IStatus run(org.eclipse.core.runtime.IProgressMonitor monitor) {
                try {
                    if (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().getAllTasks().size() > 0) {
                        backupNow(false, monitor);
                    }
                    return org.eclipse.core.runtime.Status.OK_STATUS;
                } finally {
                    if (org.eclipse.ui.PlatformUI.isWorkbenchRunning()) {
                        schedule(org.eclipse.mylyn.internal.tasks.ui.TaskListBackupManager.STANDARD_DELAY);
                    }
                }
            }
        };
        runBackup.setPriority(org.eclipse.core.runtime.jobs.Job.BUILD);
        runBackup.setSystem(true);
        runBackup.schedule(delay);
    }

    public void stop() {
        if (runBackup != null) {
            if (!runBackup.cancel()) {
                try {
                    runBackup.join();
                } catch (java.lang.InterruptedException e) {
                    // ignore
                }
            }
            runBackup = null;
        }
    }

    public static java.lang.String getBackupFileName() {
        com.ibm.icu.text.SimpleDateFormat format = new com.ibm.icu.text.SimpleDateFormat(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.FILENAME_TIMESTAMP_FORMAT, java.util.Locale.ENGLISH);
        java.lang.String date = format.format(new java.util.Date());
        java.lang.String backupFileName = (org.eclipse.mylyn.internal.tasks.ui.TaskListBackupManager.BACKUP_FILE_PREFIX + date) + ".zip";// $NON-NLS-1$

        return backupFileName;
    }

    public void backupNow(boolean synchronous) {
        backupNow(synchronous, null);
    }

    public synchronized void backupNow(boolean synchronous, org.eclipse.core.runtime.IProgressMonitor monitor) {
        monitor = org.eclipse.mylyn.commons.net.Policy.monitorFor(monitor);
        java.io.File backupFolder = new java.io.File(backupFolderPath);
        if (!backupFolder.exists()) {
            backupFolder.mkdir();
        }
        final org.eclipse.mylyn.internal.tasks.ui.util.TaskDataExportOperation backupJob = new org.eclipse.mylyn.internal.tasks.ui.util.TaskDataSnapshotOperation(backupFolderPath, org.eclipse.mylyn.internal.tasks.ui.TaskListBackupManager.getBackupFileName());
        try {
            if (!synchronous) {
                backupJob.run(monitor);
                removeOldBackups();
            } else {
                org.eclipse.ui.progress.IProgressService service = org.eclipse.ui.PlatformUI.getWorkbench().getProgressService();
                service.run(false, true, backupJob);
            }
        } catch (java.lang.InterruptedException e) {
        } catch (java.lang.Throwable e) {
            if (!org.eclipse.mylyn.internal.tasks.ui.TaskListBackupManager.errorDisplayed) {
                final org.eclipse.core.runtime.Status status = new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, TasksUiPlugin.ID_PLUGIN, Messages.TaskListBackupManager_Error_occured_during_scheduled_tasklist_backup, e);
                org.eclipse.mylyn.internal.tasks.ui.TaskListBackupManager.errorDisplayed = true;
                if (org.eclipse.swt.widgets.Display.getCurrent() != null) {
                    org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.logAndDisplayStatus(Messages.TaskListBackupManager_Scheduled_task_data_backup, status);
                } else {
                    org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.asyncLogAndDisplayStatus(Messages.TaskListBackupManager_Scheduled_task_data_backup, status);
                }
            }
            // clean up corrupt backup file
            if (backupJob.getDestinationFile() != null) {
                backupJob.getDestinationFile().delete();
            }
        }
    }

    public java.util.SortedMap<java.lang.Long, java.io.File> getBackupFiles() {
        java.util.SortedMap<java.lang.Long, java.io.File> filesMap = new java.util.TreeMap<java.lang.Long, java.io.File>();
        java.lang.String destination = backupFolderPath;
        java.io.File backupFolder = new java.io.File(destination);
        if (!backupFolder.exists()) {
            return filesMap;
        }
        java.io.File[] files = backupFolder.listFiles();
        if (files == null) {
            return filesMap;
        }
        for (java.io.File file : files) {
            java.util.regex.Matcher matcher = org.eclipse.mylyn.internal.tasks.ui.TaskListBackupManager.MYLYN_BACKUP_REGEXP.matcher(file.getName());
            if (matcher.find()) {
                java.util.Date date = null;
                try {
                    com.ibm.icu.text.SimpleDateFormat format = null;
                    java.lang.String dateText = null;
                    java.util.regex.Matcher dateFormatMatcher = org.eclipse.mylyn.internal.tasks.ui.TaskListBackupManager.DATE_FORMAT.matcher(file.getName());
                    if (dateFormatMatcher.find()) {
                        format = new com.ibm.icu.text.SimpleDateFormat(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.FILENAME_TIMESTAMP_FORMAT, java.util.Locale.ENGLISH);
                        dateText = dateFormatMatcher.group();
                    } else {
                        dateFormatMatcher = org.eclipse.mylyn.internal.tasks.ui.TaskListBackupManager.DATE_FORMAT_OLD.matcher(file.getName());
                        if (dateFormatMatcher.find()) {
                            format = new com.ibm.icu.text.SimpleDateFormat(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.OLD_FILENAME_TIMESTAMP_FORMAT, java.util.Locale.ENGLISH);
                            dateText = dateFormatMatcher.group();
                        }
                    }
                    if (((format != null) && (dateText != null)) && (dateText.length() > 0)) {
                        date = format.parse(dateText);
                    } else {
                        continue;
                    }
                } catch (java.lang.IndexOutOfBoundsException e) {
                    continue;
                } catch (java.text.ParseException e) {
                    continue;
                }
                if ((date != null) && (date.getTime() > 0)) {
                    filesMap.put(new java.lang.Long(date.getTime()), file);
                }
            }
        }
        return filesMap;
    }

    /**
     * public for testing purposes
     */
    public synchronized void removeOldBackups() {
        java.util.SortedMap<java.lang.Long, java.io.File> filesMap = getBackupFiles();
        if (filesMap.size() > 0) {
            java.util.Calendar rangeStart = org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil.getCalendar();
            rangeStart.setTimeInMillis(filesMap.lastKey());
            org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil.snapStartOfHour(rangeStart);
            int startHour = rangeStart.get(java.util.Calendar.HOUR_OF_DAY);
            java.util.Calendar rangeEnd = org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil.getCalendar();
            rangeEnd.setTimeInMillis(rangeStart.getTimeInMillis());
            rangeEnd.add(java.util.Calendar.HOUR_OF_DAY, 1);
            // Keep one backup for last 8 hours of today
            for (int x = 0; (x <= startHour) && (x < 9); x++) {
                java.util.SortedMap<java.lang.Long, java.io.File> subMap = filesMap.subMap(rangeStart.getTimeInMillis(), rangeEnd.getTimeInMillis());
                if (subMap.size() > 1) {
                    while (subMap.size() > 1) {
                        java.io.File toDelete = subMap.remove(subMap.firstKey());
                        toDelete.delete();
                    } 
                }
                rangeStart.add(java.util.Calendar.HOUR_OF_DAY, -1);
                rangeEnd.add(java.util.Calendar.HOUR_OF_DAY, -1);
            }
            // Keep one backup a day for the past 12 days
            org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil.snapStartOfDay(rangeEnd);
            rangeStart.add(java.util.Calendar.DAY_OF_YEAR, -1);
            for (int x = 1; x <= 12; x++) {
                java.util.SortedMap<java.lang.Long, java.io.File> subMap = filesMap.subMap(rangeStart.getTimeInMillis(), rangeEnd.getTimeInMillis());
                if (subMap.size() > 1) {
                    while (subMap.size() > 1) {
                        java.io.File toDelete = subMap.remove(subMap.firstKey());
                        toDelete.delete();
                    } 
                }
                rangeStart.add(java.util.Calendar.DAY_OF_YEAR, -1);
                rangeEnd.add(java.util.Calendar.DAY_OF_YEAR, -1);
            }
            // Remove all older backups
            java.util.SortedMap<java.lang.Long, java.io.File> subMap = filesMap.subMap(0L, rangeStart.getTimeInMillis());
            if (subMap.size() > 0) {
                while (subMap.size() > 0) {
                    java.io.File toDelete = subMap.remove(subMap.firstKey());
                    toDelete.delete();
                } 
            }
        }
    }

    public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
        if (event.getProperty().equals(ITasksUiPreferenceConstants.PREF_DATA_DIR)) {
            backupFolderPath = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getBackupFolderPath();
        }
    }
}