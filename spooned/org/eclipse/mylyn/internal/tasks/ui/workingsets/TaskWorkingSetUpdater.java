/**
 * *****************************************************************************
 * Copyright (c) 2004, 2012 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugene Kuleshov - initial API and implementation
 *     Tasktop Technologies - initial API and implementation
 *     Yatta Solutions - fix for bug 327262
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.workingsets;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.ITaskListChangeListener;
import org.eclipse.mylyn.internal.tasks.core.TaskCategory;
import org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.monitor.ui.MonitorUi;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetUpdater;
import org.eclipse.ui.PlatformUI;
/**
 *
 * @author Eugene Kuleshov
 * @author Mik Kersten
 * @author Steffen Pingel
 * @author Carsten Reckord
 */
public class TaskWorkingSetUpdater implements org.eclipse.ui.IWorkingSetUpdater , org.eclipse.mylyn.internal.tasks.core.ITaskListChangeListener , org.eclipse.core.resources.IResourceChangeListener {
    public static java.lang.String ID_TASK_WORKING_SET = "org.eclipse.mylyn.tasks.ui.workingSet";// $NON-NLS-1$


    private final java.util.List<org.eclipse.ui.IWorkingSet> workingSets = new java.util.concurrent.CopyOnWriteArrayList<org.eclipse.ui.IWorkingSet>();

    private static class TaskWorkingSetDelta {
        private final org.eclipse.ui.IWorkingSet workingSet;

        private final java.util.List<java.lang.Object> elements;

        private boolean changed;

        public TaskWorkingSetDelta(org.eclipse.ui.IWorkingSet workingSet) {
            this.workingSet = workingSet;
            this.elements = new java.util.ArrayList<java.lang.Object>(java.util.Arrays.asList(workingSet.getElements()));
        }

        public int indexOf(java.lang.Object element) {
            return elements.indexOf(element);
        }

        public void set(int index, java.lang.Object element) {
            elements.set(index, element);
            changed = true;
        }

        public void remove(int index) {
            if (elements.remove(index) != null) {
                changed = true;
            }
        }

        public void process() {
            if (changed) {
                workingSet.setElements(elements.toArray(new org.eclipse.core.runtime.IAdaptable[elements.size()]));
            }
        }
    }

    private static boolean enabled = true;

    public TaskWorkingSetUpdater() {
        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().addChangeListener(this);
        org.eclipse.core.resources.ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
    }

    /**
     * Set <code>enabled</code> to false to disable processing of task list changes, e.g. during import operations.
     *
     * @param enabled
     */
    public static void setEnabled(boolean enabled) {
        org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater.enabled = enabled;
    }

    public static boolean isEnabled() {
        return org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater.enabled;
    }

    public void dispose() {
        org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().removeChangeListener(this);
    }

    public void add(org.eclipse.ui.IWorkingSet workingSet) {
        checkElementExistence(workingSet);
        synchronized(workingSets) {
            workingSets.add(workingSet);
        }
    }

    private void checkElementExistence(org.eclipse.ui.IWorkingSet workingSet) {
        java.util.ArrayList<org.eclipse.core.runtime.IAdaptable> list = new java.util.ArrayList<org.eclipse.core.runtime.IAdaptable>(java.util.Arrays.asList(workingSet.getElements()));
        boolean changed = false;
        for (java.util.Iterator<org.eclipse.core.runtime.IAdaptable> iter = list.iterator(); iter.hasNext();) {
            org.eclipse.core.runtime.IAdaptable adaptable = iter.next();
            boolean remove = false;
            if (adaptable instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer) {
                java.lang.String handle = ((org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer) (adaptable)).getHandleIdentifier();
                remove = true;
                for (org.eclipse.mylyn.tasks.core.IRepositoryElement element : org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().getRootElements()) {
                    if ((element != null) && element.getHandleIdentifier().equals(handle)) {
                        remove = false;
                        break;
                    }
                }
            } else if (adaptable instanceof org.eclipse.core.resources.IProject) {
                org.eclipse.core.resources.IProject project = org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot().getProject(((org.eclipse.core.resources.IProject) (adaptable)).getName());
                if ((project == null) || (!project.exists())) {
                    remove = true;
                }
            }
            if (remove) {
                iter.remove();
                changed = true;
            }
        }
        if (changed) {
            workingSet.setElements(list.toArray(new org.eclipse.core.runtime.IAdaptable[list.size()]));
        }
    }

    public boolean contains(org.eclipse.ui.IWorkingSet workingSet) {
        synchronized(workingSets) {
            return workingSets.contains(workingSet);
        }
    }

    public boolean remove(org.eclipse.ui.IWorkingSet workingSet) {
        synchronized(workingSets) {
            return workingSets.remove(workingSet);
        }
    }

    public void containersChanged(java.util.Set<org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta> delta) {
        if (!org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater.isEnabled()) {
            return;
        }
        for (org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta taskContainerDelta : delta) {
            if ((taskContainerDelta.getElement() instanceof org.eclipse.mylyn.internal.tasks.core.TaskCategory) || (taskContainerDelta.getElement() instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery)) {
                synchronized(workingSets) {
                    switch (taskContainerDelta.getKind()) {
                        case REMOVED :
                            // Remove from all
                            for (org.eclipse.ui.IWorkingSet workingSet : workingSets) {
                                java.util.ArrayList<org.eclipse.core.runtime.IAdaptable> elements = new java.util.ArrayList<org.eclipse.core.runtime.IAdaptable>(java.util.Arrays.asList(workingSet.getElements()));
                                elements.remove(taskContainerDelta.getElement());
                                workingSet.setElements(elements.toArray(new org.eclipse.core.runtime.IAdaptable[elements.size()]));
                            }
                            break;
                        case ADDED :
                            // Add to the active working set
                            for (org.eclipse.ui.IWorkingSet workingSet : org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater.getEnabledSets()) {
                                java.util.ArrayList<org.eclipse.core.runtime.IAdaptable> elements = new java.util.ArrayList<org.eclipse.core.runtime.IAdaptable>(java.util.Arrays.asList(workingSet.getElements()));
                                elements.add(taskContainerDelta.getElement());
                                workingSet.setElements(elements.toArray(new org.eclipse.core.runtime.IAdaptable[elements.size()]));
                            }
                            break;
                    }
                }
            }
        }
    }

    // TODO: consider putting back, but evaluate policy and note bug 197257
    // public void taskActivated(AbstractTask task) {
    // Set<AbstractTaskContainer> taskContainers = new HashSet<AbstractTaskContainer>(
    // TasksUiPlugin.getTaskList().getQueriesForHandle(task.getHandleIdentifier()));
    // taskContainers.addAll(task.getParentContainers());
    // 
    // Set<AbstractTaskContainer> allActiveWorkingSetContainers = new HashSet<AbstractTaskContainer>();
    // for (IWorkingSet workingSet : PlatformUI.getWorkbench()
    // .getActiveWorkbenchWindow()
    // .getActivePage()
    // .getWorkingSets()) {
    // ArrayList<IAdaptable> elements = new ArrayList<IAdaptable>(Arrays.asList(workingSet.getElements()));
    // for (IAdaptable adaptable : elements) {
    // if (adaptable instanceof AbstractTaskContainer) {
    // allActiveWorkingSetContainers.add((AbstractTaskContainer) adaptable);
    // }
    // }
    // }
    // boolean isContained = false;
    // for (AbstractTaskContainer taskContainer : allActiveWorkingSetContainers) {
    // if (taskContainers.contains(taskContainer)) {
    // isContained = true;
    // break;
    // }
    // }
    // 
    // ;
    // if (!isContained) {
    // IWorkingSet matchingWorkingSet = null;
    // for (IWorkingSet workingSet : PlatformUI.getWorkbench().getWorkingSetManager().getAllWorkingSets()) {
    // ArrayList<IAdaptable> elements = new ArrayList<IAdaptable>(Arrays.asList(workingSet.getElements()));
    // for (IAdaptable adaptable : elements) {
    // if (adaptable instanceof AbstractTaskContainer) {
    // if (((AbstractTaskContainer)adaptable).contains(task.getHandleIdentifier())) {
    // matchingWorkingSet = workingSet;
    // }
    // }
    // }
    // }
    // 
    // if (matchingWorkingSet != null) {
    // new ToggleWorkingSetAction(matchingWorkingSet).run();
    // } else {
    // new ToggleAllWorkingSetsAction(PlatformUI.getWorkbench().getActiveWorkbenchWindow()).run();
    // }
    // }
    // }
    public static org.eclipse.ui.IWorkingSet[] getEnabledSets() {
        java.util.Set<org.eclipse.ui.IWorkingSet> workingSets = new java.util.HashSet<org.eclipse.ui.IWorkingSet>();
        java.util.Set<org.eclipse.ui.IWorkbenchWindow> windows = org.eclipse.mylyn.monitor.ui.MonitorUi.getMonitoredWindows();
        for (org.eclipse.ui.IWorkbenchWindow iWorkbenchWindow : windows) {
            org.eclipse.ui.IWorkbenchPage page = iWorkbenchWindow.getActivePage();
            if (page != null) {
                workingSets.addAll(java.util.Arrays.asList(page.getWorkingSets()));
            }
        }
        return workingSets.toArray(new org.eclipse.ui.IWorkingSet[workingSets.size()]);
    }

    /**
     * TODO: move
     */
    public static boolean areNoTaskWorkingSetsEnabled() {
        org.eclipse.ui.IWorkingSet[] workingSets = org.eclipse.ui.PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSets();
        for (org.eclipse.ui.IWorkingSet workingSet : workingSets) {
            if ((workingSet != null) && workingSet.getId().equalsIgnoreCase(org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater.ID_TASK_WORKING_SET)) {
                if (org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater.isWorkingSetEnabled(workingSet)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isWorkingSetEnabled(org.eclipse.ui.IWorkingSet set) {
        org.eclipse.ui.IWorkingSet[] enabledSets = org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater.getEnabledSets();
        for (org.eclipse.ui.IWorkingSet enabledSet : enabledSets) {
            if (enabledSet.equals(set)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOnlyTaskWorkingSetEnabled(org.eclipse.ui.IWorkingSet set) {
        if (!org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater.isWorkingSetEnabled(set)) {
            return false;
        }
        org.eclipse.ui.IWorkingSet[] enabledSets = org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater.getEnabledSets();
        for (int i = 0; i < enabledSets.length; i++) {
            if ((!enabledSets[i].equals(set)) && enabledSets[i].getId().equalsIgnoreCase(org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater.ID_TASK_WORKING_SET)) {
                return false;
            }
        }
        return true;
    }

    private void processResourceDelta(org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater.TaskWorkingSetDelta result, org.eclipse.core.resources.IResourceDelta delta) {
        org.eclipse.core.resources.IResource resource = delta.getResource();
        int type = resource.getType();
        int index = result.indexOf(resource);
        int kind = delta.getKind();
        int flags = delta.getFlags();
        if (((kind == org.eclipse.core.resources.IResourceDelta.CHANGED) && (type == org.eclipse.core.resources.IResource.PROJECT)) && (index != (-1))) {
            if ((flags & org.eclipse.core.resources.IResourceDelta.OPEN) != 0) {
                result.set(index, resource);
            }
        }
        if ((index != (-1)) && (kind == org.eclipse.core.resources.IResourceDelta.REMOVED)) {
            if ((flags & org.eclipse.core.resources.IResourceDelta.MOVED_TO) != 0) {
                result.set(index, org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot().findMember(delta.getMovedToPath()));
            } else {
                result.remove(index);
            }
        }
        // Don't dive into closed or opened projects
        if (projectGotClosedOrOpened(resource, kind, flags)) {
            return;
        }
        org.eclipse.core.resources.IResourceDelta[] children = delta.getAffectedChildren();
        for (org.eclipse.core.resources.IResourceDelta element : children) {
            processResourceDelta(result, element);
        }
    }

    private boolean projectGotClosedOrOpened(org.eclipse.core.resources.IResource resource, int kind, int flags) {
        return ((resource.getType() == org.eclipse.core.resources.IResource.PROJECT) && (kind == org.eclipse.core.resources.IResourceDelta.CHANGED)) && ((flags & org.eclipse.core.resources.IResourceDelta.OPEN) != 0);
    }

    public void resourceChanged(org.eclipse.core.resources.IResourceChangeEvent event) {
        for (org.eclipse.ui.IWorkingSet workingSet : workingSets) {
            org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater.TaskWorkingSetDelta workingSetDelta = new org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater.TaskWorkingSetDelta(workingSet);
            if (event.getDelta() != null) {
                processResourceDelta(workingSetDelta, event.getDelta());
            }
            workingSetDelta.process();
        }
    }

    /**
     * Must be called from the UI thread
     */
    public static void applyWorkingSetsToAllWindows(java.util.Collection<org.eclipse.ui.IWorkingSet> workingSets) {
        org.eclipse.ui.IWorkingSet[] workingSetArray = workingSets.toArray(new org.eclipse.ui.IWorkingSet[workingSets.size()]);
        for (org.eclipse.ui.IWorkbenchWindow window : org.eclipse.mylyn.monitor.ui.MonitorUi.getMonitoredWindows()) {
            for (org.eclipse.ui.IWorkbenchPage page : window.getPages()) {
                page.setWorkingSets(workingSetArray);
            }
        }
    }

    public static java.util.Set<org.eclipse.ui.IWorkingSet> getActiveWorkingSets(org.eclipse.ui.IWorkbenchWindow window) {
        if ((window != null) && (window.getActivePage() != null)) {
            java.util.Set<org.eclipse.ui.IWorkingSet> allSets = new java.util.HashSet<org.eclipse.ui.IWorkingSet>(java.util.Arrays.asList(window.getActivePage().getWorkingSets()));
            java.util.Set<org.eclipse.ui.IWorkingSet> tasksSets = new java.util.HashSet<org.eclipse.ui.IWorkingSet>(allSets);
            for (org.eclipse.ui.IWorkingSet workingSet : allSets) {
                if ((workingSet.getId() == null) || (!workingSet.getId().equalsIgnoreCase(org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater.ID_TASK_WORKING_SET))) {
                    tasksSets.remove(workingSet);
                }
            }
            return tasksSets;
        } else {
            return java.util.Collections.emptySet();
        }
    }
}