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
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui;
import org.eclipse.core.net.proxy.IProxyChangeEvent;
import org.eclipse.core.net.proxy.IProxyChangeListener;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.identity.core.IIdentityService;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.eclipse.mylyn.commons.notifications.core.NotificationEnvironment;
import org.eclipse.mylyn.commons.notifications.feed.ServiceMessageManager;
import org.eclipse.mylyn.commons.notifications.ui.AbstractUiNotification;
import org.eclipse.mylyn.commons.ui.compatibility.CommonColors;
import org.eclipse.mylyn.commons.ui.compatibility.CommonFonts;
import org.eclipse.mylyn.commons.workbench.TaskBarManager;
import org.eclipse.mylyn.internal.commons.notifications.feed.ServiceMessage;
import org.eclipse.mylyn.internal.discovery.ui.DiscoveryUi;
import org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin;
import org.eclipse.mylyn.internal.tasks.core.AbstractSearchHandler;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.IRepositoryModelListener;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector;
import org.eclipse.mylyn.internal.tasks.core.RepositoryExternalizationParticipant;
import org.eclipse.mylyn.internal.tasks.core.RepositoryModel;
import org.eclipse.mylyn.internal.tasks.core.RepositoryTemplateManager;
import org.eclipse.mylyn.internal.tasks.core.TaskActivityManager;
import org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryDelta;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryDelta.Type;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.internal.tasks.core.data.SynchronizationManger;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataManager;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataStore;
import org.eclipse.mylyn.internal.tasks.core.externalization.ExternalizationManager;
import org.eclipse.mylyn.internal.tasks.core.externalization.IExternalizationParticipant;
import org.eclipse.mylyn.internal.tasks.core.externalization.TaskActivationExternalizationParticipant;
import org.eclipse.mylyn.internal.tasks.core.externalization.TaskListExternalizationParticipant;
import org.eclipse.mylyn.internal.tasks.core.externalization.TaskListExternalizer;
import org.eclipse.mylyn.internal.tasks.core.util.RepositoryConnectorLoader;
import org.eclipse.mylyn.internal.tasks.core.util.TaskRepositoryKeyringMigrator;
import org.eclipse.mylyn.internal.tasks.core.util.TaskRepositorySecureStoreMigrator;
import org.eclipse.mylyn.internal.tasks.core.util.TasksCoreExtensionReader;
import org.eclipse.mylyn.internal.tasks.ui.actions.ActivateTaskDialogAction;
import org.eclipse.mylyn.internal.tasks.ui.actions.NewTaskAction;
import org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotificationReminder;
import org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotifier;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiExtensionReader;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoriesView;
import org.eclipse.mylyn.tasks.core.AbstractDuplicateDetector;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITask.PriorityLevel;
import org.eclipse.mylyn.tasks.core.ITaskContainer;
import org.eclipse.mylyn.tasks.core.RepositoryTemplate;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.activity.AbstractTaskActivityMonitor;
import org.eclipse.mylyn.tasks.core.context.AbstractTaskContextStore;
import org.eclipse.mylyn.tasks.core.sync.SynchronizationJob;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.AbstractTaskRepositoryLinkProvider;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
/**
 * Main entry point for the Tasks UI.
 *
 * @author Mik Kersten
 * @since 3.0
 */
public class TasksUiPlugin extends org.eclipse.ui.plugin.AbstractUIPlugin {
    private static final int DELAY_QUERY_REFRESH_ON_STARTUP = 20 * 1000;

    private static final int DEFAULT_LINK_PROVIDER_TIMEOUT = 5 * 1000;

    public static final java.lang.String ID_PLUGIN = "org.eclipse.mylyn.tasks.ui";// $NON-NLS-1$


    private static final java.lang.String DIRECTORY_METADATA = ".metadata";// $NON-NLS-1$


    private static final java.lang.String NAME_DATA_DIR = ".mylyn";// $NON-NLS-1$


    private static final char DEFAULT_PATH_SEPARATOR = '/';

    private static final int NOTIFICATION_DELAY = 5000;

    private static final java.lang.String PREF_MIGRATED_TASK_REPOSITORIES_FROM_SECURE_STORE = "migrated.task.repositories.secure.store";// $NON-NLS-1$


    private static final java.lang.String PREF_MIGRATED_TASK_REPOSITORIES_FROM_KEYRING = "migrated.task.repositories.keyring";// $NON-NLS-1$


    private static final java.lang.String PROP_FORCE_CREDENTIALS_MIGRATION = "org.eclipse.mylyn.tasks.force.credentials.migration";// $NON-NLS-1$


    private static org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin INSTANCE;

    private static org.eclipse.mylyn.internal.tasks.core.externalization.ExternalizationManager externalizationManager;

    private static org.eclipse.mylyn.internal.tasks.core.TaskActivityManager taskActivityManager;

    private static org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager repositoryManager;

    private static org.eclipse.mylyn.internal.tasks.ui.TaskListSynchronizationScheduler synchronizationScheduler;

    private static org.eclipse.mylyn.internal.tasks.core.data.TaskDataManager taskDataManager;

    private static java.util.Map<java.lang.String, org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi> repositoryConnectorUiMap = new java.util.HashMap<java.lang.String, org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi>();

    private org.eclipse.mylyn.internal.tasks.ui.TaskListNotificationManager taskListNotificationManager;

    private org.eclipse.mylyn.internal.tasks.ui.TaskListBackupManager taskListBackupManager;

    private org.eclipse.mylyn.internal.tasks.core.RepositoryTemplateManager repositoryTemplateManager;

    private org.eclipse.mylyn.commons.notifications.feed.ServiceMessageManager serviceMessageManager;

    private final java.util.Set<org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory> taskEditorPageFactories = new java.util.HashSet<org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory>();

    private final java.util.TreeSet<org.eclipse.mylyn.tasks.ui.AbstractTaskRepositoryLinkProvider> repositoryLinkProviders = new java.util.TreeSet<org.eclipse.mylyn.tasks.ui.AbstractTaskRepositoryLinkProvider>(new org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.OrderComparator());

    private org.eclipse.mylyn.internal.tasks.core.externalization.TaskListExternalizer taskListExternalizer;

    private final org.eclipse.mylyn.internal.tasks.ui.BrandManager brandManager = new org.eclipse.mylyn.internal.tasks.ui.BrandManager();

    private final java.util.Set<org.eclipse.mylyn.tasks.core.AbstractDuplicateDetector> duplicateDetectors = new java.util.HashSet<org.eclipse.mylyn.tasks.core.AbstractDuplicateDetector>();

    private org.eclipse.core.resources.ISaveParticipant saveParticipant;

    private org.eclipse.mylyn.internal.tasks.ui.TaskJobFactory taskJobFactory;

    // shared colors for all forms
    private org.eclipse.ui.forms.FormColors formColors;

    private static org.eclipse.mylyn.tasks.core.context.AbstractTaskContextStore contextStore;

    private final java.util.List<org.eclipse.mylyn.internal.tasks.core.AbstractSearchHandler> searchHandlers = new java.util.ArrayList<org.eclipse.mylyn.internal.tasks.core.AbstractSearchHandler>();

    private static final boolean DEBUG_HTTPCLIENT = // $NON-NLS-1$
    "true".equalsIgnoreCase(org.eclipse.core.runtime.Platform.getDebugOption("org.eclipse.mylyn.tasks.ui/debug/httpclient"));// $NON-NLS-1$


    // XXX reconsider if this is necessary
    public static class TasksUiStartup implements org.eclipse.ui.IStartup {
        public void earlyStartup() {
            // ignore
        }
    }

    private static final class OrderComparator implements java.util.Comparator<org.eclipse.mylyn.tasks.ui.AbstractTaskRepositoryLinkProvider> {
        public int compare(org.eclipse.mylyn.tasks.ui.AbstractTaskRepositoryLinkProvider p1, org.eclipse.mylyn.tasks.ui.AbstractTaskRepositoryLinkProvider p2) {
            return p1.getOrder() - p2.getOrder();
        }
    }

    public enum TaskListSaveMode {

        ONE_HOUR,
        THREE_HOURS,
        DAY;
        @java.lang.Override
        public java.lang.String toString() {
            switch (this) {
                case ONE_HOUR :
                    return "1 hour";// $NON-NLS-1$

                case THREE_HOURS :
                    return "3 hours";// $NON-NLS-1$

                case DAY :
                    return "1 day";// $NON-NLS-1$

                default :
                    return "3 hours";// $NON-NLS-1$

            }
        }

        public static org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.TaskListSaveMode fromString(java.lang.String string) {
            if (string == null) {
                return null;
            }
            if (string.equals("1 hour")) {
                // $NON-NLS-1$
                return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.TaskListSaveMode.ONE_HOUR;
            }
            if (string.equals("3 hours")) {
                // $NON-NLS-1$
                return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.TaskListSaveMode.THREE_HOURS;
            }
            if (string.equals("1 day")) {
                // $NON-NLS-1$
                return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.TaskListSaveMode.DAY;
            }
            return null;
        }

        public static long fromStringToLong(java.lang.String string) {
            long hour = 3600 * 1000;
            switch (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.TaskListSaveMode.fromString(string)) {
                case ONE_HOUR :
                    return hour;
                case THREE_HOURS :
                    return hour * 3;
                case DAY :
                    return hour * 24;
                default :
                    return hour * 3;
            }
        }
    }

    public enum ReportOpenMode {

        EDITOR,
        INTERNAL_BROWSER,
        EXTERNAL_BROWSER;}

    private static org.eclipse.mylyn.internal.tasks.ui.ITaskListNotificationProvider REMINDER_NOTIFICATION_PROVIDER = new org.eclipse.mylyn.internal.tasks.ui.ITaskListNotificationProvider() {
        public java.util.Set<org.eclipse.mylyn.commons.notifications.ui.AbstractUiNotification> getNotifications() {
            java.util.Collection<org.eclipse.mylyn.internal.tasks.core.AbstractTask> allTasks = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().getAllTasks();
            java.util.Set<org.eclipse.mylyn.commons.notifications.ui.AbstractUiNotification> reminders = new java.util.HashSet<org.eclipse.mylyn.commons.notifications.ui.AbstractUiNotification>();
            for (org.eclipse.mylyn.internal.tasks.core.AbstractTask task : allTasks) {
                if (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().isPastReminder(task) && (!task.isReminded())) {
                    reminders.add(new org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotificationReminder(task));
                    task.setReminded(true);
                }
            }
            return reminders;
        }
    };

    private final org.eclipse.jface.util.IPropertyChangeListener PROPERTY_LISTENER = new org.eclipse.jface.util.IPropertyChangeListener() {
        public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
            if (event.getProperty().equals(ITasksUiPreferenceConstants.PLANNING_ENDHOUR) || event.getProperty().equals(ITasksUiPreferenceConstants.WEEK_START_DAY)) {
                updateTaskActivityManager();
            }
            if (event.getProperty().equals(ITasksUiPreferenceConstants.REPOSITORY_SYNCH_SCHEDULE_ENABLED) || event.getProperty().equals(ITasksUiPreferenceConstants.REPOSITORY_SYNCH_SCHEDULE_MILISECONDS)) {
                updateSynchronizationScheduler(false);
            }
            if (event.getProperty().equals(ITasksUiPreferenceConstants.SERVICE_MESSAGES_ENABLED)) {
                if (getPreferenceStore().getBoolean(ITasksUiPreferenceConstants.SERVICE_MESSAGES_ENABLED)) {
                    serviceMessageManager.start();
                } else {
                    serviceMessageManager.stop();
                }
            }
        }
    };

    private org.eclipse.mylyn.tasks.core.activity.AbstractTaskActivityMonitor taskActivityMonitor;

    @java.lang.SuppressWarnings("rawtypes")
    private org.osgi.framework.ServiceReference proxyServiceReference;

    private org.eclipse.core.net.proxy.IProxyChangeListener proxyChangeListener;

    private static org.eclipse.mylyn.internal.tasks.core.externalization.TaskListExternalizationParticipant taskListExternalizationParticipant;

    private final java.util.Set<org.eclipse.mylyn.internal.tasks.core.IRepositoryModelListener> listeners = new java.util.HashSet<org.eclipse.mylyn.internal.tasks.core.IRepositoryModelListener>();

    private java.io.File activationHistoryFile;

    private static org.eclipse.mylyn.internal.tasks.core.TaskList taskList;

    private static org.eclipse.mylyn.internal.tasks.core.RepositoryModel repositoryModel;

    private static org.eclipse.mylyn.internal.tasks.ui.TasksUiFactory uiFactory;

    @java.lang.Deprecated
    public static java.lang.String LABEL_VIEW_REPOSITORIES = Messages.TasksUiPlugin_Task_Repositories;

    private final java.util.concurrent.atomic.AtomicInteger initializationCount = new java.util.concurrent.atomic.AtomicInteger();

    private org.eclipse.mylyn.internal.tasks.core.data.SynchronizationManger synchronizationManger;

    private org.eclipse.mylyn.internal.tasks.core.util.RepositoryConnectorLoader connectorLoader;

    private class TasksUiInitializationJob extends org.eclipse.ui.progress.UIJob {
        public TasksUiInitializationJob() {
            super(Messages.TasksUiPlugin_Initializing_Task_List);
            setSystem(true);
        }

        @java.lang.SuppressWarnings("restriction")
        @java.lang.Override
        public org.eclipse.core.runtime.IStatus runInUIThread(org.eclipse.core.runtime.IProgressMonitor monitor) {
            // NOTE: failure in one part of the initialization should
            // not prevent others
            monitor.beginTask("Initializing Task List", 5);// $NON-NLS-1$

            try {
                // Needs to run after workbench is loaded because it
                // relies on images.
                org.eclipse.mylyn.internal.tasks.ui.util.TasksUiExtensionReader.initWorkbenchUiExtensions(connectorLoader.getBlackList());
                if (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.externalizationManager.getLoadStatus() != null) {
                    // XXX: recovery from task list load failure  (Rendered in task list)
                }
                java.util.List<java.lang.String> commandLineArgs = java.util.Arrays.asList(org.eclipse.core.runtime.Platform.getCommandLineArgs());
                boolean activateTask = !commandLineArgs.contains(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.COMMAND_LINE_NO_ACTIVATE_TASK);
                if (activateTask) {
                    try {
                        java.lang.reflect.Field field = org.eclipse.core.internal.resources.Workspace.class.getDeclaredField("crashed");// $NON-NLS-1$

                        field.setAccessible(true);
                        java.lang.Object value = field.get(org.eclipse.core.resources.ResourcesPlugin.getWorkspace());
                        if (value instanceof java.lang.Boolean) {
                            activateTask = !((java.lang.Boolean) (value));
                        }
                    } catch (java.lang.Throwable t) {
                        t.printStackTrace();
                        // ignore
                    }
                }
                // Needs to happen asynchronously to avoid bug 159706
                for (org.eclipse.mylyn.internal.tasks.core.AbstractTask task : org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.taskList.getAllTasks()) {
                    if (task.isActive()) {
                        // the externalizer might set multiple tasks active
                        task.setActive(false);
                        if (activateTask) {
                            // make sure only one task is activated
                            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.taskActivityManager.activateTask(task);
                            activateTask = false;
                        }
                    }
                }
                // taskActivityMonitor.reloadActivityTime();
            } catch (java.lang.Throwable t) {
                org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Could not initialize task activity", t));// $NON-NLS-1$

            }
            monitor.worked(1);
            initializeNotificationsAndSynchronization();
            addSystemTaskBarActions();
            try {
                getPreferenceStore().addPropertyChangeListener(PROPERTY_LISTENER);
                // TODO: get rid of this, hack to make decorators show
                // up on startup
                org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoriesView repositoriesView = org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoriesView.getFromActivePerspective();
                if (repositoriesView != null) {
                    repositoriesView.getViewer().refresh();
                }
            } catch (java.lang.Throwable t) {
                org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Could not finish Tasks UI initialization", t));// $NON-NLS-1$

            } finally {
                monitor.done();
            }
            hideNonMatchingSubtasks();
            return new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.OK, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, org.eclipse.core.runtime.IStatus.OK, "", null);// $NON-NLS-1$

        }

        /**
         * hide non-matching subtasks, or display a message about it if this is not a fresh installation
         */
        protected void hideNonMatchingSubtasks() {
            final java.lang.String HIDE_SUBTASKS = "hide-subtasks";// $NON-NLS-1$

            if ((!getPreferenceStore().getBoolean(ITasksUiPreferenceConstants.FILTER_NON_MATCHING)) && (!getPreferenceStore().getBoolean(ITasksUiPreferenceConstants.ENCOURAGED_FILTER_NON_MATCHING))) {
                if (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.taskList.getQueries().isEmpty()) {
                    getPreferenceStore().setValue(ITasksUiPreferenceConstants.FILTER_NON_MATCHING, true);
                } else {
                    org.eclipse.mylyn.internal.tasks.ui.views.TaskListView view = org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.getFromActivePerspective();
                    if ((view != null) && (view.getServiceMessageControl() != null)) {
                        org.eclipse.mylyn.internal.commons.notifications.feed.ServiceMessage message = new org.eclipse.mylyn.internal.commons.notifications.feed.ServiceMessage("") {
                            // $NON-NLS-1$
                            @java.lang.Override
                            public boolean openLink(java.lang.String link) {
                                if (HIDE_SUBTASKS.equals(link)) {
                                    getPreferenceStore().setValue(ITasksUiPreferenceConstants.FILTER_NON_MATCHING, true);
                                    savePluginPreferences();
                                    return true;
                                }
                                return false;
                            }
                        };
                        message.setImage(org.eclipse.jface.dialogs.Dialog.DLG_IMG_MESSAGE_INFO);
                        message.setTitle(Messages.TasksUiPlugin_Hide_Irrelevant_Subtasks);
                        message.setDescription(org.eclipse.osgi.util.NLS.bind(Messages.TasksUiPlugin_Hide_Irrelevant_Subtasks_Message, HIDE_SUBTASKS));
                        view.getServiceMessageControl().setMessage(message);
                    }
                }
                // never do this again
                getPreferenceStore().setValue(ITasksUiPreferenceConstants.ENCOURAGED_FILTER_NON_MATCHING, true);
                savePluginPreferences();
            }
        }
    }

    public TasksUiPlugin() {
        super();
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.INSTANCE = this;
    }

    private void addSystemTaskBarActions() {
        org.eclipse.jface.action.MenuManager taskBarMenuManager = org.eclipse.mylyn.commons.workbench.TaskBarManager.getTaskBarMenuManager();
        if (taskBarMenuManager != null) {
            org.eclipse.mylyn.internal.tasks.ui.actions.NewTaskAction newTaskAction = new org.eclipse.mylyn.internal.tasks.ui.actions.NewTaskAction(Messages.TasksUiPlugin_New_Task, true);
            taskBarMenuManager.add(newTaskAction);
            org.eclipse.jface.action.Action activateTaskAction = new org.eclipse.jface.action.Action() {
                @java.lang.Override
                public void run() {
                    org.eclipse.mylyn.internal.tasks.ui.actions.ActivateTaskDialogAction activateTaskDialogAction = new org.eclipse.mylyn.internal.tasks.ui.actions.ActivateTaskDialogAction();
                    org.eclipse.ui.IWorkbenchWindow window = getWorkbench().getActiveWorkbenchWindow();
                    if ((window == null) && (getWorkbench().getWorkbenchWindows().length > 0)) {
                        window = getWorkbench().getWorkbenchWindows()[0];
                    }
                    activateTaskDialogAction.init(window);
                    activateTaskDialogAction.run(null);
                }
            };
            activateTaskAction.setImageDescriptor(org.eclipse.mylyn.tasks.ui.TasksUiImages.CONTEXT_ACTIVE_CENTERED);
            activateTaskAction.setText(Messages.TasksUiPlugin_Activate_Task);
            taskBarMenuManager.add(activateTaskAction);
            taskBarMenuManager.update(true);
        }
    }

    private void updateSynchronizationScheduler(boolean initial) {
        if (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.synchronizationScheduler == null) {
            return;
        }
        boolean enabled = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().getBoolean(ITasksUiPreferenceConstants.REPOSITORY_SYNCH_SCHEDULE_ENABLED);
        if (enabled) {
            long interval = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().getLong(ITasksUiPreferenceConstants.REPOSITORY_SYNCH_SCHEDULE_MILISECONDS);
            if (initial) {
                org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.synchronizationScheduler.setInterval(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.DELAY_QUERY_REFRESH_ON_STARTUP, interval);
            } else {
                org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.synchronizationScheduler.setInterval(interval);
            }
        } else {
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.synchronizationScheduler.setInterval(0);
        }
    }

    @java.lang.SuppressWarnings("restriction")
    @java.lang.Override
    public void start(org.osgi.framework.BundleContext context) throws java.lang.Exception {
        super.start(context);
        // NOTE: startup order is very sensitive
        try {
            // initialize framework and settings
            if (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.DEBUG_HTTPCLIENT) {
                // do this before anything else, once commons logging is initialized and an instance
                // of Log has been created it's too late
                initHttpLogging();
            }
            org.eclipse.mylyn.commons.net.WebUtil.init();
            initializePreferences(getPreferenceStore());
            // initialize CommonFonts from UI thread: bug 240076
            if (org.eclipse.mylyn.commons.ui.compatibility.CommonFonts.BOLD == null) {
                // ignore
            }
            java.io.File dataDir = new java.io.File(getDataDirectory());
            dataDir.mkdirs();
            // create data model
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.externalizationManager = new org.eclipse.mylyn.internal.tasks.core.externalization.ExternalizationManager(getDataDirectory());
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.repositoryManager = new org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager();
            org.eclipse.mylyn.internal.tasks.core.externalization.IExternalizationParticipant repositoryParticipant = new org.eclipse.mylyn.internal.tasks.core.RepositoryExternalizationParticipant(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.externalizationManager, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.repositoryManager);
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.externalizationManager.addParticipant(repositoryParticipant);
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.taskList = new org.eclipse.mylyn.internal.tasks.core.TaskList();
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.repositoryModel = new org.eclipse.mylyn.internal.tasks.core.RepositoryModel(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.taskList, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.repositoryManager);
            taskListExternalizer = new org.eclipse.mylyn.internal.tasks.core.externalization.TaskListExternalizer(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.repositoryModel, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.repositoryManager);
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.taskListExternalizationParticipant = new org.eclipse.mylyn.internal.tasks.core.externalization.TaskListExternalizationParticipant(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.repositoryModel, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.taskList, taskListExternalizer, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.externalizationManager, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.repositoryManager);
            // externalizationManager.load(taskListSaveParticipant);
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.externalizationManager.addParticipant(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.taskListExternalizationParticipant);
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.taskList.addChangeListener(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.taskListExternalizationParticipant);
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.taskActivityManager = new org.eclipse.mylyn.internal.tasks.core.TaskActivityManager(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.repositoryManager, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.taskList);
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.taskActivityManager.addActivationListener(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.taskListExternalizationParticipant);
            // initialize
            updateTaskActivityManager();
            proxyServiceReference = context.getServiceReference(org.eclipse.core.net.proxy.IProxyService.class.getName());
            if (proxyServiceReference != null) {
                @java.lang.SuppressWarnings("unchecked")
                org.eclipse.core.net.proxy.IProxyService proxyService = ((org.eclipse.core.net.proxy.IProxyService) (context.getService(proxyServiceReference)));
                if (proxyService != null) {
                    proxyChangeListener = new org.eclipse.core.net.proxy.IProxyChangeListener() {
                        public void proxyInfoChanged(org.eclipse.core.net.proxy.IProxyChangeEvent event) {
                            java.util.List<org.eclipse.mylyn.tasks.core.TaskRepository> repositories = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.repositoryManager.getAllRepositories();
                            for (org.eclipse.mylyn.tasks.core.TaskRepository repository : repositories) {
                                if (repository.isDefaultProxyEnabled()) {
                                    org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.repositoryManager.notifyRepositorySettingsChanged(repository, new org.eclipse.mylyn.internal.tasks.core.TaskRepositoryDelta(org.eclipse.mylyn.internal.tasks.core.TaskRepositoryDelta.Type.PROYX));
                                }
                            }
                        }
                    };
                    proxyService.addProxyChangeListener(proxyChangeListener);
                }
            }
            repositoryTemplateManager = new org.eclipse.mylyn.internal.tasks.core.RepositoryTemplateManager();
            // NOTE: initializing extensions in start(..) has caused race
            // conditions previously
            connectorLoader = new org.eclipse.mylyn.internal.tasks.core.util.RepositoryConnectorLoader();
            connectorLoader.registerConnectors(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.repositoryManager, taskListExternalizer);
            connectorLoader.registerTemplates(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.repositoryManager, repositoryTemplateManager);
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiExtensionReader.initStartupExtensions(connectorLoader.getBlackList());
            // instantiate taskDataManager
            org.eclipse.mylyn.internal.tasks.core.data.TaskDataStore taskDataStore = new org.eclipse.mylyn.internal.tasks.core.data.TaskDataStore(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.repositoryManager);
            synchronizationManger = new org.eclipse.mylyn.internal.tasks.core.data.SynchronizationManger(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.repositoryModel);
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.taskDataManager = new org.eclipse.mylyn.internal.tasks.core.data.TaskDataManager(taskDataStore, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.repositoryManager, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.taskList, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.taskActivityManager, synchronizationManger);
            taskJobFactory = new org.eclipse.mylyn.internal.tasks.ui.TaskJobFactory(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.taskList, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.taskDataManager, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.repositoryManager, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.repositoryModel);
            taskActivityMonitor = org.eclipse.mylyn.internal.tasks.core.util.TasksCoreExtensionReader.loadTaskActivityMonitor();
            taskActivityMonitor.start(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.taskActivityManager);
            saveParticipant = new org.eclipse.core.resources.ISaveParticipant() {
                public void doneSaving(org.eclipse.core.resources.ISaveContext context) {
                }

                public void prepareToSave(org.eclipse.core.resources.ISaveContext context) throws org.eclipse.core.runtime.CoreException {
                }

                public void rollback(org.eclipse.core.resources.ISaveContext context) {
                }

                public void saving(org.eclipse.core.resources.ISaveContext context) throws org.eclipse.core.runtime.CoreException {
                    if (context.getKind() == org.eclipse.core.resources.ISaveContext.FULL_SAVE) {
                        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.externalizationManager.stop();
                    }
                }
            };
            org.eclipse.core.resources.ResourcesPlugin.getWorkspace().addSaveParticipant(this, saveParticipant);
            // initialize externalization for task activation history
            org.eclipse.core.runtime.IPath stateLocation = org.eclipse.core.runtime.Platform.getStateLocation(getBundle());
            activationHistoryFile = stateLocation.append("TaskActivationHistory.xml").toFile();// $NON-NLS-1$

            org.eclipse.mylyn.internal.tasks.core.externalization.TaskActivationExternalizationParticipant taskActivationExternalizationParticipant = new org.eclipse.mylyn.internal.tasks.core.externalization.TaskActivationExternalizationParticipant(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.externalizationManager, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.taskList, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.taskActivityManager.getTaskActivationHistory(), activationHistoryFile);
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.taskActivityManager.addActivationListener(taskActivationExternalizationParticipant);
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.externalizationManager.addParticipant(taskActivationExternalizationParticipant);
            // initialize managers
            initializeDataSources();
            migrateCredentials(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.repositoryManager.getAllRepositories());
            // make this available early for clients that are not initialized through tasks ui but need access
            taskListNotificationManager = new org.eclipse.mylyn.internal.tasks.ui.TaskListNotificationManager();
            java.lang.String lastMod = getPreferenceStore().getString(ITasksUiPreferenceConstants.LAST_SERVICE_MESSAGE_LAST_MODIFIED);
            java.lang.String etag = getPreferenceStore().getString(ITasksUiPreferenceConstants.LAST_SERVICE_MESSAGE_ETAG);
            java.lang.String serviceMessageUrl = getPreferenceStore().getString(ITasksUiPreferenceConstants.SERVICE_MESSAGE_URL);
            java.lang.Long checktime = getPreferenceStore().getLong(ITasksUiPreferenceConstants.LAST_SERVICE_MESSAGE_CHECKTIME);
            serviceMessageManager = new org.eclipse.mylyn.commons.notifications.feed.ServiceMessageManager(serviceMessageUrl, lastMod, etag, checktime, new org.eclipse.mylyn.commons.notifications.core.NotificationEnvironment() {
                private java.util.Set<java.lang.String> installedFeatures;

                @java.lang.Override
                public java.util.Set<java.lang.String> getInstalledFeatures(org.eclipse.core.runtime.IProgressMonitor monitor) {
                    if (installedFeatures == null) {
                        installedFeatures = org.eclipse.mylyn.internal.discovery.ui.DiscoveryUi.createInstallJob().getInstalledFeatures(monitor);
                    }
                    return installedFeatures;
                }
            });
            // Disabled for initial 3.4 release as per bug#263528
            if (getPreferenceStore().getBoolean(ITasksUiPreferenceConstants.SERVICE_MESSAGES_ENABLED)) {
                serviceMessageManager.start();
            }
            // trigger lazy initialization
            new org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.TasksUiInitializationJob().schedule();
        } catch (java.lang.Exception e) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Task list initialization failed", e));// $NON-NLS-1$

        }
    }

    /**
     * Migrate credentials from the old secure store location, and from the deprecated keyring if the compatibility.auth
     * bundle is present.
     */
    @java.lang.SuppressWarnings("deprecation")
    private void migrateCredentials(final java.util.List<org.eclipse.mylyn.tasks.core.TaskRepository> repositories) {
        final boolean force = java.lang.Boolean.parseBoolean(java.lang.System.getProperty(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.PROP_FORCE_CREDENTIALS_MIGRATION));
        final boolean migrateFromSecureStore = force || (!getPluginPreferences().getBoolean(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.PREF_MIGRATED_TASK_REPOSITORIES_FROM_SECURE_STORE));
        final boolean migrateFromKeyring = (force || (!getPluginPreferences().getBoolean(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.PREF_MIGRATED_TASK_REPOSITORIES_FROM_KEYRING))) && isKeyringInstalled();
        if ((!migrateFromSecureStore) && (!migrateFromKeyring)) {
            return;
        }
        // Use a UI job to ensure the UI has loaded
        new org.eclipse.ui.progress.UIJob("Credential Migration UI Job") {
            // $NON-NLS-1$
            @java.lang.Override
            public org.eclipse.core.runtime.IStatus runInUIThread(org.eclipse.core.runtime.IProgressMonitor monitor) {
                // use a Job to ensure we do not access the secure store on the UI thread
                new org.eclipse.core.runtime.jobs.Job("Credential Migration") {
                    // $NON-NLS-1$
                    @java.lang.Override
                    protected org.eclipse.core.runtime.IStatus run(org.eclipse.core.runtime.IProgressMonitor monitor) {
                        if (force) {
                            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.INFO, org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.ID_PLUGIN, // $NON-NLS-1$
                            org.eclipse.osgi.util.NLS.bind("Forcing task repository credential migration because system property {0} is set.", org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.PROP_FORCE_CREDENTIALS_MIGRATION)));
                        }
                        if (migrateFromSecureStore) {
                            new org.eclipse.mylyn.internal.tasks.core.util.TaskRepositorySecureStoreMigrator().migrateCredentials(repositories);
                        }
                        if (migrateFromKeyring) {
                            new org.eclipse.mylyn.internal.tasks.core.util.TaskRepositoryKeyringMigrator("", "Basic").migrateCredentials(repositories);// $NON-NLS-1$ //$NON-NLS-2$

                        }
                        return org.eclipse.core.runtime.Status.OK_STATUS;
                    }
                }.schedule();
                return org.eclipse.core.runtime.Status.OK_STATUS;
            }
        }.schedule();
    }

    private boolean isKeyringInstalled() {
        return org.eclipse.core.runtime.Platform.getBundle("org.eclipse.core.runtime.compatibility.auth") != null;// $NON-NLS-1$

    }

    private void initHttpLogging() {
        java.lang.System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");// $NON-NLS-1$ //$NON-NLS-2$

        java.lang.System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");// $NON-NLS-1$ //$NON-NLS-2$

        java.lang.System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug");// $NON-NLS-1$ //$NON-NLS-2$

        java.lang.System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire.header", "debug");// $NON-NLS-1$ //$NON-NLS-2$

        java.lang.System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");// $NON-NLS-1$ //$NON-NLS-2$

        // $NON-NLS-1$
        java.lang.System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient.HttpConnection", "trace");// $NON-NLS-1$

        java.lang.System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.axis.message", "debug");// $NON-NLS-1$ //$NON-NLS-2$

    }

    private void updateTaskActivityManager() {
        int endHour = getPreferenceStore().getInt(ITasksUiPreferenceConstants.PLANNING_ENDHOUR);
        // if (taskActivityManager.getEndHour() != endHour) {
        // taskActivityManager.setEndHour(endHour);
        org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil.setEndHour(endHour);
        // }
        int newWeekStartDay = getPreferenceStore().getInt(ITasksUiPreferenceConstants.WEEK_START_DAY);
        int oldWeekStartDay = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.taskActivityManager.getWeekStartDay();
        if (oldWeekStartDay != newWeekStartDay) {
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.taskActivityManager.setWeekStartDay(newWeekStartDay);
            // taskActivityManager.setStartTime(new Date());
        }
        // event.getProperty().equals(TaskListPreferenceConstants.PLANNING_STARTDAY)
        // scheduledStartHour =
        // TasksUiPlugin.getDefault().getPreferenceStore().getInt(
        // TaskListPreferenceConstants.PLANNING_STARTHOUR);
    }

    private void loadTemplateRepositories() {
        // Add standard local task repository
        org.eclipse.mylyn.tasks.core.TaskRepository local = getLocalTaskRepository();
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.repositoryManager.applyMigrators(local);
        // Add the automatically created templates
        for (org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector : org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.repositoryManager.getRepositoryConnectors()) {
            for (org.eclipse.mylyn.tasks.core.RepositoryTemplate template : repositoryTemplateManager.getTemplates(connector.getConnectorKind())) {
                if (template.addAutomatically && (!org.eclipse.mylyn.internal.tasks.ui.TaskRepositoryUtil.isAddAutomaticallyDisabled(template.repositoryUrl))) {
                    try {
                        java.lang.String repositoryUrl = org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager.stripSlashes(template.repositoryUrl);
                        org.eclipse.mylyn.tasks.core.TaskRepository taskRepository = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.repositoryManager.getRepository(connector.getConnectorKind(), repositoryUrl);
                        if (taskRepository == null) {
                            taskRepository = new org.eclipse.mylyn.tasks.core.TaskRepository(connector.getConnectorKind(), repositoryUrl);
                            taskRepository.setVersion(template.version);
                            taskRepository.setRepositoryLabel(template.label);
                            taskRepository.setCharacterEncoding(template.characterEncoding);
                            if (template.anonymous) {
                                // $NON-NLS-1$
                                taskRepository.setProperty("org.eclipse.mylyn.tasklist.repositories.enabled", java.lang.String.valueOf(false));
                                // bug 332747: avoid reseting password in shared keystore
                                // taskRepository.setCredentials(AuthenticationType.REPOSITORY, null, true);
                            }
                            taskRepository.setCreatedFromTemplate(true);
                            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.repositoryManager.addRepository(taskRepository);
                            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.repositoryManager.applyMigrators(taskRepository);
                        }
                    } catch (java.lang.Throwable t) {
                        org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.WARNING, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, org.eclipse.osgi.util.NLS.bind("Could not load repository template for repository {0}", template.repositoryUrl), t));// $NON-NLS-1$

                    }
                }
            }
        }
    }

    /**
     * Returns the local task repository. If the repository does not exist it is created and added to the task
     * repository manager.
     *
     * @return the local task repository; never <code>null</code>
     * @since 3.0
     */
    public org.eclipse.mylyn.tasks.core.TaskRepository getLocalTaskRepository() {
        org.eclipse.mylyn.tasks.core.TaskRepository localRepository = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.repositoryManager.getRepository(org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector.CONNECTOR_KIND, org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector.REPOSITORY_URL);
        if (localRepository == null) {
            localRepository = new org.eclipse.mylyn.tasks.core.TaskRepository(org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector.CONNECTOR_KIND, org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector.REPOSITORY_URL);
            localRepository.setVersion(org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector.REPOSITORY_VERSION);
            localRepository.setRepositoryLabel(org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector.REPOSITORY_LABEL);
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.repositoryManager.addRepository(localRepository);
        }
        return localRepository;
    }

    @java.lang.Override
    public void stop(org.osgi.framework.BundleContext context) throws java.lang.Exception {
        try {
            org.eclipse.core.runtime.jobs.Job.getJobManager().cancel(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.JOB_FAMILY_SYNCHRONIZATION);
            if (formColors != null) {
                formColors.dispose();
                formColors = null;
            }
            if (taskActivityMonitor != null) {
                taskActivityMonitor.stop();
            }
            if (org.eclipse.core.resources.ResourcesPlugin.getWorkspace() != null) {
                org.eclipse.core.resources.ResourcesPlugin.getWorkspace().removeSaveParticipant(this);
            }
            if (proxyServiceReference != null) {
                @java.lang.SuppressWarnings("unchecked")
                org.eclipse.core.net.proxy.IProxyService proxyService = ((org.eclipse.core.net.proxy.IProxyService) (context.getService(proxyServiceReference)));
                if (proxyService != null) {
                    proxyService.removeProxyChangeListener(proxyChangeListener);
                }
                context.ungetService(proxyServiceReference);
            }
            if (identityServiceTracker != null) {
                identityServiceTracker.close();
                identityServiceTracker = null;
            }
            // wait until stop() to set these to reduce chance of crash after setting them but before creds are persisted
            getPluginPreferences().setValue(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.PREF_MIGRATED_TASK_REPOSITORIES_FROM_SECURE_STORE, java.lang.Boolean.toString(true));
            if (isKeyringInstalled()) {
                getPluginPreferences().setValue(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.PREF_MIGRATED_TASK_REPOSITORIES_FROM_KEYRING, java.lang.Boolean.toString(true));
            }
            if (org.eclipse.ui.PlatformUI.isWorkbenchRunning()) {
                if (taskListNotificationManager != null) {
                    getPreferenceStore().removePropertyChangeListener(taskListNotificationManager);
                }
                if (taskListBackupManager != null) {
                    getPreferenceStore().removePropertyChangeListener(taskListBackupManager);
                }
                getPreferenceStore().removePropertyChangeListener(PROPERTY_LISTENER);
                // taskListManager.getTaskList().removeChangeListener(taskListSaveManager);
                org.eclipse.mylyn.commons.ui.compatibility.CommonColors.dispose();
                // if (ContextCorePlugin.getDefault() != null) {
                // ContextCorePlugin.getDefault().getPluginPreferences().removePropertyChangeListener(
                // PREFERENCE_LISTENER);
                // }
                serviceMessageManager.stop();
                org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.INSTANCE = null;
            }
        } catch (java.lang.Exception e) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Task list stop terminated abnormally", e));// $NON-NLS-1$

        } finally {
            super.stop(context);
        }
    }

    public java.lang.String getDefaultDataDirectory() {
        return (((org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + '/') + org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.DIRECTORY_METADATA) + '/') + org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.NAME_DATA_DIR;
    }

    public java.lang.String getDataDirectory() {
        return getPreferenceStore().getString(ITasksUiPreferenceConstants.PREF_DATA_DIR);
    }

    /**
     * Persist <code>path</code> as data directory and loads data from <code>path</code>. This method may block if other
     * jobs are running that modify tasks data. This method will only execute after all conflicting jobs have been
     * completed.
     *
     * @throws CoreException
     * 		in case setting of the data directory did not complete normally
     * @throws OperationCanceledException
     * 		if the operation is cancelled by the user
     */
    public void setDataDirectory(final java.lang.String path) throws org.eclipse.core.runtime.CoreException {
        org.eclipse.core.runtime.Assert.isNotNull(path);
        org.eclipse.jface.operation.IRunnableWithProgress runner = new org.eclipse.jface.operation.IRunnableWithProgress() {
            public void run(org.eclipse.core.runtime.IProgressMonitor monitor) throws java.lang.reflect.InvocationTargetException, java.lang.InterruptedException {
                try {
                    monitor.beginTask(Messages.TasksUiPlugin_Load_Data_Directory, org.eclipse.core.runtime.IProgressMonitor.UNKNOWN);
                    if (monitor.isCanceled()) {
                        throw new java.lang.InterruptedException();
                    }
                    org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().deactivateActiveTask();
                    // set new preference in case of a change
                    if (!path.equals(getDataDirectory())) {
                        getPreferenceStore().setValue(ITasksUiPreferenceConstants.PREF_DATA_DIR, path);
                    }
                    // reload data from new directory
                    initializeDataSources();
                } finally {
                    // FIXME roll back preferences change in case of an error?
                    monitor.done();
                }
            }
        };
        org.eclipse.ui.progress.IProgressService service = org.eclipse.ui.PlatformUI.getWorkbench().getProgressService();
        try {
            service.runInUI(service, runner, org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.ROOT_SCHEDULING_RULE);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw new org.eclipse.core.runtime.CoreException(// $NON-NLS-1$
            new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Failed to set data directory", e.getCause()));
        } catch (java.lang.InterruptedException e) {
            throw new org.eclipse.core.runtime.OperationCanceledException();
        }
    }

    public void reloadDataDirectory() throws org.eclipse.core.runtime.CoreException {
        // no save just load what is there
        setDataDirectory(getDataDirectory());
    }

    /**
     * Invoked on startup and when data is loaded from disk or when the data directory changes.
     * <p>
     * Public for testing.
     */
    @java.lang.SuppressWarnings("restriction")
    public void initializeDataSources() {
        // ensure that context directory exists
        java.io.File storeFile = new java.io.File(getDataDirectory(), org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.CONTEXTS_DIRECTORY);
        if (!storeFile.exists()) {
            storeFile.mkdirs();
        }
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.taskDataManager.setDataPath(getDataDirectory());
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.externalizationManager.setRootFolderPath(getDataDirectory());
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getContextStore().setDirectory(new java.io.File(getDataDirectory(), "tasks"));// $NON-NLS-1$

        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.externalizationManager.load();
        // TODO: Move management of template repositories to TaskRepositoryManager
        loadTemplateRepositories();
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.taskActivityManager.clear();
        taskActivityMonitor.loadActivityTime();
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.taskActivityManager.reloadPlanningData();
        if ((!activationHistoryFile.exists()) && (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.taskActivityManager.getTaskActivationHistory().getSize() == 0)) {
            // fall back to activity history
            java.util.List<org.eclipse.mylyn.tasks.core.ITask> tasks = taskActivityMonitor.getActivationHistory();
            for (org.eclipse.mylyn.tasks.core.ITask task : tasks) {
                org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.taskActivityManager.getTaskActivationHistory().addTask(((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (task)));
            }
        }
        if (!org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin.getDefault().getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin.ACTIVITY_TRACKING_ENABLED + ".checked")) {
            // $NON-NLS-1$
            if (!taskActivityMonitor.getActivationHistory().isEmpty()) {
                // tasks have been active before so fore preference enabled
                org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin.getDefault().getPreferenceStore().setValue(org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin.ACTIVITY_TRACKING_ENABLED, true);
            }
            org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin.getDefault().getPreferenceStore().setValue(org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin.ACTIVITY_TRACKING_ENABLED + ".checked", true);// $NON-NLS-1$

            org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin.getDefault().savePluginPreferences();
        }
        // inform listeners that initialization is complete
        for (final org.eclipse.mylyn.internal.tasks.core.IRepositoryModelListener listener : listeners) {
            org.eclipse.core.runtime.SafeRunner.run(new org.eclipse.core.runtime.ISafeRunnable() {
                public void handleException(java.lang.Throwable exception) {
                    org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.WARNING, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Listener failed: "// $NON-NLS-1$
                     + listener.getClass(), exception));
                }

                public void run() throws java.lang.Exception {
                    listener.loaded();
                }
            });
        }
    }

    @java.lang.SuppressWarnings("deprecation")
    private void initializePreferences(org.eclipse.jface.preference.IPreferenceStore store) {
        store.setDefault(ITasksUiPreferenceConstants.PREF_DATA_DIR, getDefaultDataDirectory());
        store.setDefault(ITasksUiPreferenceConstants.GROUP_SUBTASKS, true);
        store.setDefault(ITasksUiPreferenceConstants.NOTIFICATIONS_ENABLED, true);
        store.setDefault(ITasksUiPreferenceConstants.SERVICE_MESSAGES_ENABLED, false);
        store.setDefault(ITasksUiPreferenceConstants.FILTER_PRIORITY, org.eclipse.mylyn.tasks.core.ITask.PriorityLevel.P5.toString());
        store.setDefault(ITasksUiPreferenceConstants.EDITOR_TASKS_RICH, true);
        store.setDefault(ITasksUiPreferenceConstants.EDITOR_CURRENT_LINE_HIGHLIGHT, false);
        store.setDefault(ITasksUiPreferenceConstants.ACTIVATE_WHEN_OPENED, false);
        store.setDefault(ITasksUiPreferenceConstants.SHOW_TRIM, false);
        // remove preference
        store.setToDefault(ITasksUiPreferenceConstants.LOCAL_SUB_TASKS_ENABLED);
        store.setDefault(ITasksUiPreferenceConstants.USE_STRIKETHROUGH_FOR_COMPLETED, true);
        store.setDefault(ITasksUiPreferenceConstants.REPOSITORY_SYNCH_SCHEDULE_ENABLED, true);
        store.setDefault(ITasksUiPreferenceConstants.REPOSITORY_SYNCH_SCHEDULE_MILISECONDS, "" + ((20 * 60) * 1000));// $NON-NLS-1$

        store.setDefault(ITasksUiPreferenceConstants.RELEVANT_TASKS_SCHEDULE_MILISECONDS, "" + ((5 * 60) * 1000));// $NON-NLS-1$

        store.setDefault(ITasksUiPreferenceConstants.BACKUP_MAXFILES, 20);
        store.setDefault(ITasksUiPreferenceConstants.BACKUP_LAST, 0.0F);
        store.setDefault(ITasksUiPreferenceConstants.FILTER_HIDDEN, true);
        store.setDefault(ITasksUiPreferenceConstants.FILTER_ARCHIVE_MODE, true);
        store.setDefault(ITasksUiPreferenceConstants.ACTIVATE_MULTIPLE, false);
        store.setValue(ITasksUiPreferenceConstants.ACTIVATE_MULTIPLE, false);
        store.setDefault(ITasksUiPreferenceConstants.WEEK_START_DAY, java.util.Calendar.getInstance().getFirstDayOfWeek());
        store.setDefault(ITasksUiPreferenceConstants.SCHEDULE_NEW_TASKS_FOR, ITasksUiPreferenceConstants.SCHEDULE_NEW_TASKS_FOR_THIS_WEEK);
        store.setDefault(ITasksUiPreferenceConstants.PLANNING_ENDHOUR, 18);
        store.setDefault(ITasksUiPreferenceConstants.AUTO_EXPAND_TASK_LIST, true);
        store.setDefault(ITasksUiPreferenceConstants.TASK_LIST_TOOL_TIPS_ENABLED, true);
        store.setDefault(ITasksUiPreferenceConstants.SERVICE_MESSAGE_URL, "http://eclipse.org/mylyn/updates.xml");// $NON-NLS-1$

    }

    public static org.eclipse.mylyn.internal.tasks.core.TaskActivityManager getTaskActivityManager() {
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.taskActivityManager;
    }

    public static org.eclipse.mylyn.internal.tasks.ui.TaskListNotificationManager getTaskListNotificationManager() {
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.INSTANCE.taskListNotificationManager;
    }

    /**
     * Returns the shared instance.
     */
    public static org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin getDefault() {
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.INSTANCE;
    }

    public boolean groupSubtasks(org.eclipse.mylyn.tasks.core.ITaskContainer element) {
        boolean groupSubtasks = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().getBoolean(ITasksUiPreferenceConstants.GROUP_SUBTASKS);
        if (element instanceof org.eclipse.mylyn.tasks.core.ITask) {
            org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getConnectorUi(((org.eclipse.mylyn.tasks.core.ITask) (element)).getConnectorKind());
            if (connectorUi != null) {
                if (connectorUi.hasStrictSubtaskHierarchy()) {
                    groupSubtasks = true;
                }
            }
        }
        if (element instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery) {
            org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getConnectorUi(((org.eclipse.mylyn.tasks.core.IRepositoryQuery) (element)).getConnectorKind());
            if (connectorUi != null) {
                if (connectorUi.hasStrictSubtaskHierarchy()) {
                    groupSubtasks = true;
                }
            }
        }
        return groupSubtasks;
    }

    private final java.util.Map<java.lang.String, java.util.List<org.eclipse.mylyn.internal.tasks.ui.IDynamicSubMenuContributor>> menuContributors = new java.util.HashMap<java.lang.String, java.util.List<org.eclipse.mylyn.internal.tasks.ui.IDynamicSubMenuContributor>>();

    @java.lang.SuppressWarnings("rawtypes")
    private org.osgi.util.tracker.ServiceTracker identityServiceTracker;

    public java.util.Map<java.lang.String, java.util.List<org.eclipse.mylyn.internal.tasks.ui.IDynamicSubMenuContributor>> getDynamicMenuMap() {
        return menuContributors;
    }

    public void addDynamicPopupContributor(java.lang.String menuPath, org.eclipse.mylyn.internal.tasks.ui.IDynamicSubMenuContributor contributor) {
        java.util.List<org.eclipse.mylyn.internal.tasks.ui.IDynamicSubMenuContributor> contributors = menuContributors.get(menuPath);
        if (contributors == null) {
            contributors = new java.util.ArrayList<org.eclipse.mylyn.internal.tasks.ui.IDynamicSubMenuContributor>();
            menuContributors.put(menuPath, contributors);
        }
        contributors.add(contributor);
    }

    public java.lang.String[] getSaveOptions() {
        java.lang.String[] options = new java.lang.String[]{ org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.TaskListSaveMode.ONE_HOUR.toString(), org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.TaskListSaveMode.THREE_HOURS.toString(), org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.TaskListSaveMode.DAY.toString() };
        return options;
    }

    public java.lang.String getBackupFolderPath() {
        return (getDataDirectory() + org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.DEFAULT_PATH_SEPARATOR) + org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.DEFAULT_BACKUP_FOLDER_NAME;
    }

    /**
     *
     * @since 3.0
     */
    public org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory[] getTaskEditorPageFactories() {
        return taskEditorPageFactories.toArray(new org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory[0]);
    }

    /**
     *
     * @since 3.0
     */
    public void addTaskEditorPageFactory(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory factory) {
        org.eclipse.core.runtime.Assert.isNotNull(factory);
        taskEditorPageFactories.add(factory);
    }

    /**
     *
     * @since 3.0
     */
    public void removeTaskEditorPageFactory(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory factory) {
        org.eclipse.core.runtime.Assert.isNotNull(factory);
        taskEditorPageFactories.remove(factory);
    }

    public static org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager getRepositoryManager() {
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.repositoryManager;
    }

    /**
     *
     * @since 3.0
     */
    public static org.eclipse.mylyn.internal.tasks.core.RepositoryTemplateManager getRepositoryTemplateManager() {
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.INSTANCE.repositoryTemplateManager;
    }

    /**
     *
     * @deprecated Use {@link BrandManager#addDefaultBrandingIcon(String, Image)} instead
     * @see #getBrandManager()
     */
    @java.lang.Deprecated
    public void addBrandingIcon(java.lang.String repositoryType, org.eclipse.swt.graphics.Image icon) {
        brandManager.addDefaultBrandingIcon(repositoryType, icon);
    }

    /**
     *
     * @deprecated Use {@link BrandManager#getDefaultBrandingIcon(String)} instead
     * @see #getBrandManager()
     */
    @java.lang.Deprecated
    public org.eclipse.swt.graphics.Image getBrandingIcon(java.lang.String repositoryType) {
        return brandManager.getDefaultBrandingIcon(repositoryType);
    }

    /**
     *
     * @deprecated Use {@link BrandManager#addDefaultOverlayIcon(String, ImageDescriptor)} instead
     * @see #getBrandManager()
     */
    @java.lang.Deprecated
    public void addOverlayIcon(java.lang.String repositoryType, org.eclipse.jface.resource.ImageDescriptor icon) {
        brandManager.addDefaultOverlayIcon(repositoryType, icon);
    }

    /**
     *
     * @deprecated Use {@link BrandManager#getDefaultOverlayIcon(String)} instead
     * @see #getBrandManager()
     */
    @java.lang.Deprecated
    public org.eclipse.jface.resource.ImageDescriptor getOverlayIcon(java.lang.String repositoryType) {
        return brandManager.getDefaultOverlayIcon(repositoryType);
    }

    public org.eclipse.mylyn.internal.tasks.ui.IBrandManager getBrandManager() {
        return brandManager;
    }

    public void addRepositoryLinkProvider(org.eclipse.mylyn.tasks.ui.AbstractTaskRepositoryLinkProvider repositoryLinkProvider) {
        if (repositoryLinkProvider != null) {
            this.repositoryLinkProviders.add(repositoryLinkProvider);
        }
    }

    public static synchronized org.eclipse.mylyn.internal.tasks.ui.TaskListBackupManager getBackupManager() {
        if (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.INSTANCE.taskListBackupManager == null) {
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.INSTANCE.taskListBackupManager = new org.eclipse.mylyn.internal.tasks.ui.TaskListBackupManager(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.INSTANCE.getBackupFolderPath());
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.INSTANCE.getPreferenceStore().addPropertyChangeListener(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.INSTANCE.taskListBackupManager);
        }
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.INSTANCE.taskListBackupManager;
    }

    public void addRepositoryConnectorUi(org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi repositoryConnectorUi) {
        if (!org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.repositoryConnectorUiMap.values().contains(repositoryConnectorUi)) {
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.repositoryConnectorUiMap.put(repositoryConnectorUi.getConnectorKind(), repositoryConnectorUi);
        }
    }

    /**
     *
     * @since 3.0
     */
    public static org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector getConnector(java.lang.String kind) {
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryManager().getRepositoryConnector(kind);
    }

    public static org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi getConnectorUi(java.lang.String kind) {
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.repositoryConnectorUiMap.get(kind);
    }

    @java.lang.Deprecated
    public static org.eclipse.mylyn.internal.tasks.ui.TaskListSynchronizationScheduler getSynchronizationScheduler() {
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.synchronizationScheduler;
    }

    /**
     *
     * @since 3.0
     */
    public static org.eclipse.mylyn.internal.tasks.core.data.TaskDataManager getTaskDataManager() {
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.taskDataManager;
    }

    /**
     *
     * @since 3.0
     */
    public static org.eclipse.mylyn.internal.tasks.ui.TaskJobFactory getTaskJobFactory() {
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.INSTANCE.taskJobFactory;
    }

    public void addDuplicateDetector(org.eclipse.mylyn.tasks.core.AbstractDuplicateDetector duplicateDetector) {
        org.eclipse.core.runtime.Assert.isNotNull(duplicateDetector);
        duplicateDetectors.add(duplicateDetector);
    }

    public java.util.Set<org.eclipse.mylyn.tasks.core.AbstractDuplicateDetector> getDuplicateSearchCollectorsList() {
        return duplicateDetectors;
    }

    public java.lang.String getRepositoriesFilePath() {
        return (getDataDirectory() + java.io.File.separator) + org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager.DEFAULT_REPOSITORIES_FILE;
    }

    public void addModelListener(org.eclipse.mylyn.internal.tasks.core.IRepositoryModelListener listener) {
        listeners.add(listener);
    }

    public void removeModelListener(org.eclipse.mylyn.internal.tasks.core.IRepositoryModelListener listener) {
        listeners.remove(listener);
    }

    public boolean canSetRepositoryForResource(final org.eclipse.core.resources.IResource resource) {
        if (resource == null) {
            return false;
        }
        // if a repository has already been linked only that provider should be queried to ensure that it is the same
        // provider that is used by getRepositoryForResource()
        final boolean result[] = new boolean[1];
        final boolean found[] = new boolean[1];
        for (final org.eclipse.mylyn.tasks.ui.AbstractTaskRepositoryLinkProvider linkProvider : repositoryLinkProviders) {
            org.eclipse.core.runtime.SafeRunner.run(new org.eclipse.core.runtime.ISafeRunnable() {
                public void handleException(java.lang.Throwable e) {
                    org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, ("Task repository link provider failed: \"" + linkProvider.getId()) + "\"", e));// $NON-NLS-1$ //$NON-NLS-2$

                }

                public void run() throws java.lang.Exception {
                    if (linkProvider.getTaskRepository(resource, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryManager()) != null) {
                        found[0] = true;
                        result[0] = linkProvider.canSetTaskRepository(resource);
                    }
                }
            });
            if (found[0]) {
                return result[0];
            }
        }
        // find a provider that can set new repository
        for (final org.eclipse.mylyn.tasks.ui.AbstractTaskRepositoryLinkProvider linkProvider : repositoryLinkProviders) {
            org.eclipse.core.runtime.SafeRunner.run(new org.eclipse.core.runtime.ISafeRunnable() {
                public void handleException(java.lang.Throwable e) {
                    org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, ("Task repository link provider failed: \"" + linkProvider.getId()) + "\"", e));// $NON-NLS-1$ //$NON-NLS-2$

                }

                public void run() throws java.lang.Exception {
                    if (linkProvider.canSetTaskRepository(resource)) {
                        result[0] = true;
                    }
                }
            });
            if (result[0]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Associate a Task Repository with a workbench project
     *
     * @param resource
     * 		project or resource belonging to a project
     * @param repository
     * 		task repository to associate with given project
     * @throws CoreException
     */
    public void setRepositoryForResource(final org.eclipse.core.resources.IResource resource, final org.eclipse.mylyn.tasks.core.TaskRepository repository) {
        org.eclipse.core.runtime.Assert.isNotNull(resource);
        for (final org.eclipse.mylyn.tasks.ui.AbstractTaskRepositoryLinkProvider linkProvider : repositoryLinkProviders) {
            org.eclipse.core.runtime.SafeRunner.run(new org.eclipse.core.runtime.ISafeRunnable() {
                public void handleException(java.lang.Throwable e) {
                    org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, ("Task repository link provider failed: \"" + linkProvider.getId()) + "\"", e));// $NON-NLS-1$ //$NON-NLS-2$

                }

                public void run() throws java.lang.Exception {
                    boolean canSetRepository = linkProvider.canSetTaskRepository(resource);
                    if (canSetRepository) {
                        linkProvider.setTaskRepository(resource, repository);
                    }
                }
            });
        }
    }

    /**
     * Retrieve the task repository that has been associated with the given project (or resource belonging to a project)
     * NOTE: if call does not return in LINK_PROVIDER_TIMEOUT_SECONDS, the provide will be disabled until the next time
     * that the Workbench starts.
     */
    public org.eclipse.mylyn.tasks.core.TaskRepository getRepositoryForResource(final org.eclipse.core.resources.IResource resource) {
        org.eclipse.core.runtime.Assert.isNotNull(resource);
        long timeout;
        try {
            timeout = java.lang.Long.parseLong(java.lang.System.getProperty(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.PROPERTY_LINK_PROVIDER_TIMEOUT, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.DEFAULT_LINK_PROVIDER_TIMEOUT + ""));// $NON-NLS-1$

        } catch (java.lang.NumberFormatException e) {
            timeout = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.DEFAULT_LINK_PROVIDER_TIMEOUT;
        }
        java.util.Set<org.eclipse.mylyn.tasks.ui.AbstractTaskRepositoryLinkProvider> defectiveLinkProviders = new java.util.HashSet<org.eclipse.mylyn.tasks.ui.AbstractTaskRepositoryLinkProvider>();
        for (final org.eclipse.mylyn.tasks.ui.AbstractTaskRepositoryLinkProvider linkProvider : repositoryLinkProviders) {
            long startTime = java.lang.System.currentTimeMillis();
            final org.eclipse.mylyn.tasks.core.TaskRepository[] repository = new org.eclipse.mylyn.tasks.core.TaskRepository[1];
            org.eclipse.jface.util.SafeRunnable.run(new org.eclipse.core.runtime.ISafeRunnable() {
                public void handleException(java.lang.Throwable e) {
                    org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, ("Task repository link provider failed: \""// $NON-NLS-1$
                     + linkProvider.getId()) + "\"", e));// $NON-NLS-1$

                }

                public void run() throws java.lang.Exception {
                    repository[0] = linkProvider.getTaskRepository(resource, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryManager());
                }
            });
            long elapsed = java.lang.System.currentTimeMillis() - startTime;
            if ((timeout >= 0) && (elapsed > timeout)) {
                defectiveLinkProviders.add(linkProvider);
            }
            if (repository[0] != null) {
                return repository[0];
            }
        }
        if (!defectiveLinkProviders.isEmpty()) {
            repositoryLinkProviders.removeAll(defectiveLinkProviders);
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.WARNING, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, ((("Repository link provider took over " + timeout)// $NON-NLS-1$
             + " ms to execute and was timed out: \"") + defectiveLinkProviders) + "\""));// $NON-NLS-1$ //$NON-NLS-2$

        }
        return null;
    }

    public static org.eclipse.mylyn.internal.tasks.core.externalization.ExternalizationManager getExternalizationManager() {
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.externalizationManager;
    }

    public static org.eclipse.mylyn.tasks.core.activity.AbstractTaskActivityMonitor getTaskActivityMonitor() {
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.INSTANCE.taskActivityMonitor;
    }

    public static org.eclipse.mylyn.internal.tasks.core.TaskList getTaskList() {
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.taskList;
    }

    public static org.eclipse.mylyn.internal.tasks.core.RepositoryModel getRepositoryModel() {
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.repositoryModel;
    }

    /**
     * Note: This is provisional API that is used by connectors.
     * <p>
     * DO NOT CHANGE.
     */
    public void addSearchHandler(org.eclipse.mylyn.internal.tasks.core.AbstractSearchHandler searchHandler) {
        searchHandlers.add(searchHandler);
    }

    /**
     * Note: This is provisional API that is used by connectors.
     * <p>
     * DO NOT CHANGE.
     */
    public void removeSearchHandler(org.eclipse.mylyn.internal.tasks.core.AbstractSearchHandler searchHandler) {
        searchHandlers.remove(searchHandler);
    }

    public org.eclipse.mylyn.internal.tasks.core.AbstractSearchHandler getSearchHandler(java.lang.String connectorKind) {
        org.eclipse.core.runtime.Assert.isNotNull(connectorKind);
        for (org.eclipse.mylyn.internal.tasks.core.AbstractSearchHandler searchHandler : searchHandlers) {
            if (searchHandler.getConnectorKind().equals(connectorKind)) {
                return searchHandler;
            }
        }
        return null;
    }

    public org.eclipse.ui.forms.FormColors getFormColors(org.eclipse.swt.widgets.Display display) {
        if (formColors == null) {
            formColors = new org.eclipse.ui.forms.FormColors(display);
            formColors.markShared();
        }
        return formColors;
    }

    public void removeRepositoryLinkProvider(org.eclipse.mylyn.tasks.ui.AbstractTaskRepositoryLinkProvider provider) {
        repositoryLinkProviders.remove(provider);
    }

    public org.eclipse.mylyn.internal.tasks.core.externalization.TaskListExternalizer createTaskListExternalizer() {
        return new org.eclipse.mylyn.internal.tasks.core.externalization.TaskListExternalizer(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.repositoryModel, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.repositoryManager);
    }

    public static org.eclipse.mylyn.internal.tasks.core.externalization.TaskListExternalizationParticipant getTaskListExternalizationParticipant() {
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.taskListExternalizationParticipant;
    }

    public static org.eclipse.mylyn.internal.tasks.ui.TasksUiFactory getUiFactory() {
        if (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.uiFactory == null) {
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.uiFactory = new org.eclipse.mylyn.internal.tasks.ui.TasksUiFactory();
        }
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.uiFactory;
    }

    public void initializeNotificationsAndSynchronization() {
        // this method is invoked by the tasks ui initialization job and the task list view
        // only proceed if both calls have been made
        if (initializationCount.incrementAndGet() != 2) {
            return;
        }
        try {
            taskListNotificationManager.addNotificationProvider(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.REMINDER_NOTIFICATION_PROVIDER);
            // taskListNotificationManager.addNotificationProvider(INCOMING_NOTIFICATION_PROVIDER);
            org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotifier taskListNotifier = new org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotifier(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskDataManager(), getSynchronizationManger());
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskDataManager().addListener(taskListNotifier);
            taskListNotificationManager.addNotificationProvider(taskListNotifier);
            taskListNotificationManager.startNotification(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.NOTIFICATION_DELAY);
            getPreferenceStore().addPropertyChangeListener(taskListNotificationManager);
        } catch (java.lang.Throwable t) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Could not initialize notifications", t));// $NON-NLS-1$

        }
        try {
            // trigger backup scheduler
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getBackupManager();
            org.eclipse.mylyn.tasks.core.sync.SynchronizationJob refreshJob = taskJobFactory.createSynchronizeRepositoriesJob(null);
            refreshJob.setFullSynchronization(true);
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.synchronizationScheduler = new org.eclipse.mylyn.internal.tasks.ui.TaskListSynchronizationScheduler(refreshJob);
            org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin.getDefault().getActivityContextManager().addListener(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.synchronizationScheduler);
            updateSynchronizationScheduler(true);
        } catch (java.lang.Throwable t) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Could not initialize task list backup and synchronization", t));// $NON-NLS-1$

        }
    }

    @java.lang.SuppressWarnings("restriction")
    public org.eclipse.mylyn.commons.notifications.feed.ServiceMessageManager getServiceMessageManager() {
        return serviceMessageManager;
    }

    public org.eclipse.mylyn.internal.tasks.core.data.SynchronizationManger getSynchronizationManger() {
        return synchronizationManger;
    }

    @java.lang.SuppressWarnings({ "rawtypes", "unchecked", "restriction" })
    public org.eclipse.mylyn.commons.identity.core.IIdentityService getIdentityService() {
        if (identityServiceTracker == null) {
            identityServiceTracker = new org.osgi.util.tracker.ServiceTracker(getBundle().getBundleContext(), org.eclipse.mylyn.commons.identity.core.IIdentityService.class.getName(), null);
            identityServiceTracker.open();
        }
        return ((org.eclipse.mylyn.commons.identity.core.IIdentityService) (identityServiceTracker.getService()));
    }

    public static synchronized org.eclipse.mylyn.tasks.core.context.AbstractTaskContextStore getContextStore() {
        if (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.contextStore == null) {
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.contextStore = org.eclipse.mylyn.internal.tasks.core.util.TasksCoreExtensionReader.loadTaskContextStore();
        }
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.contextStore;
    }
}