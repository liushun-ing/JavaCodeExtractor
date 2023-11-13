/**
 * *****************************************************************************
 * Copyright (c) 2004, 2011 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.tasks.ui;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.commons.ui.compatibility.CommonFonts;
import org.eclipse.mylyn.commons.ui.compatibility.CommonThemes;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.DayDateRange;
import org.eclipse.mylyn.internal.tasks.core.Person;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.TaskCategory;
import org.eclipse.mylyn.internal.tasks.core.TaskGroup;
import org.eclipse.mylyn.internal.tasks.core.UncategorizedTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.UnmatchedTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.UnsubmittedTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.WeekDateRange;
import org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants;
import org.eclipse.mylyn.internal.tasks.ui.Messages;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskContainer;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.IThemeManager;
/**
 *
 * @author Mik Kersten
 * @since 3.0
 */
public class TaskElementLabelProvider extends org.eclipse.jface.viewers.LabelProvider implements org.eclipse.jface.viewers.IColorProvider , org.eclipse.jface.viewers.IFontProvider {
    private final org.eclipse.ui.themes.IThemeManager themeManager = org.eclipse.ui.PlatformUI.getWorkbench().getThemeManager();

    private boolean wideImages = false;

    private class CompositeImageDescriptor {
        org.eclipse.jface.resource.ImageDescriptor icon;

        org.eclipse.jface.resource.ImageDescriptor overlayKind;
    }

    public TaskElementLabelProvider() {
        this(false);
    }

    public TaskElementLabelProvider(boolean wideImages) {
        super();
        this.wideImages = wideImages;
    }

    @java.lang.Override
    public org.eclipse.swt.graphics.Image getImage(java.lang.Object element) {
        org.eclipse.mylyn.tasks.ui.TaskElementLabelProvider.CompositeImageDescriptor compositeDescriptor = getImageDescriptor(element);
        if (element instanceof org.eclipse.mylyn.tasks.core.ITask) {
            if (compositeDescriptor.overlayKind == null) {
                compositeDescriptor.overlayKind = org.eclipse.mylyn.commons.ui.CommonImages.OVERLAY_CLEAR;
            }
            return org.eclipse.mylyn.commons.ui.CommonImages.getCompositeTaskImage(compositeDescriptor.icon, compositeDescriptor.overlayKind, wideImages);
        } else if (element instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) {
            return org.eclipse.mylyn.commons.ui.CommonImages.getCompositeTaskImage(compositeDescriptor.icon, org.eclipse.mylyn.commons.ui.CommonImages.OVERLAY_CLEAR, wideImages);
        } else {
            return org.eclipse.mylyn.commons.ui.CommonImages.getCompositeTaskImage(compositeDescriptor.icon, null, wideImages);
        }
    }

    private org.eclipse.mylyn.tasks.ui.TaskElementLabelProvider.CompositeImageDescriptor getImageDescriptor(java.lang.Object object) {
        org.eclipse.mylyn.tasks.ui.TaskElementLabelProvider.CompositeImageDescriptor compositeDescriptor = new org.eclipse.mylyn.tasks.ui.TaskElementLabelProvider.CompositeImageDescriptor();
        if (object instanceof org.eclipse.mylyn.internal.tasks.core.UncategorizedTaskContainer) {
            compositeDescriptor.icon = TasksUiImages.CATEGORY_UNCATEGORIZED;
            return compositeDescriptor;
        } else if (object instanceof org.eclipse.mylyn.internal.tasks.core.UnsubmittedTaskContainer) {
            compositeDescriptor.icon = TasksUiImages.CATEGORY_UNCATEGORIZED;
            return compositeDescriptor;
        } else if (object instanceof org.eclipse.mylyn.internal.tasks.core.TaskCategory) {
            compositeDescriptor.icon = TasksUiImages.CATEGORY;
        } else if (object instanceof org.eclipse.mylyn.internal.tasks.core.TaskGroup) {
            compositeDescriptor.icon = org.eclipse.mylyn.commons.ui.CommonImages.GROUPING;
        }
        if (object instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) {
            org.eclipse.mylyn.tasks.core.IRepositoryElement element = ((org.eclipse.mylyn.tasks.core.IRepositoryElement) (object));
            org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi = null;
            if (element instanceof org.eclipse.mylyn.tasks.core.ITask) {
                org.eclipse.mylyn.tasks.core.ITask repositoryTask = ((org.eclipse.mylyn.tasks.core.ITask) (element));
                connectorUi = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getConnectorUi(((org.eclipse.mylyn.tasks.core.ITask) (element)).getConnectorKind());
                if (connectorUi != null) {
                    compositeDescriptor.overlayKind = connectorUi.getTaskKindOverlay(repositoryTask);
                }
            } else if (element instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery) {
                connectorUi = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getConnectorUi(((org.eclipse.mylyn.tasks.core.IRepositoryQuery) (element)).getConnectorKind());
            }
            if (connectorUi != null) {
                compositeDescriptor.icon = connectorUi.getImageDescriptor(element);
                return compositeDescriptor;
            } else {
                if (element instanceof org.eclipse.mylyn.internal.tasks.core.UnmatchedTaskContainer) {
                    compositeDescriptor.icon = TasksUiImages.QUERY_UNMATCHED;
                } else if (element instanceof org.eclipse.mylyn.internal.tasks.core.RepositoryQuery) {
                    compositeDescriptor.icon = (((org.eclipse.mylyn.internal.tasks.core.RepositoryQuery) (element)).getAutoUpdate()) ? TasksUiImages.QUERY : TasksUiImages.QUERY_OFFLINE;
                } else if (element instanceof org.eclipse.mylyn.tasks.core.ITask) {
                    compositeDescriptor.icon = TasksUiImages.TASK;
                } else if (element instanceof org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer) {
                    org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer scheduledTaskContainer = ((org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer) (element));
                    if (scheduledTaskContainer.getDateRange() instanceof org.eclipse.mylyn.internal.tasks.core.DayDateRange) {
                        if (scheduledTaskContainer.isPresent()) {
                            compositeDescriptor.icon = org.eclipse.mylyn.commons.ui.CommonImages.SCHEDULE_DAY;
                        } else {
                            compositeDescriptor.icon = org.eclipse.mylyn.commons.ui.CommonImages.SCHEDULE;
                        }
                    } else if (scheduledTaskContainer.getDateRange() instanceof org.eclipse.mylyn.internal.tasks.core.WeekDateRange) {
                        compositeDescriptor.icon = org.eclipse.mylyn.commons.ui.CommonImages.SCHEDULE_WEEK;
                    } else {
                        compositeDescriptor.icon = TasksUiImages.QUERY_UNMATCHED;
                    }
                } else if (element instanceof org.eclipse.mylyn.internal.tasks.core.Person) {
                    compositeDescriptor.icon = org.eclipse.mylyn.commons.ui.CommonImages.PERSON;
                    org.eclipse.mylyn.internal.tasks.core.Person person = ((org.eclipse.mylyn.internal.tasks.core.Person) (element));
                    org.eclipse.mylyn.tasks.core.TaskRepository repository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(person.getConnectorKind(), person.getRepositoryUrl());
                    if (((repository != null) && (!repository.isAnonymous())) && ((repository.getUserName() != null) && repository.getUserName().equalsIgnoreCase(element.getHandleIdentifier()))) {
                        compositeDescriptor.icon = org.eclipse.mylyn.commons.ui.CommonImages.PERSON_ME;
                    }
                }
                return compositeDescriptor;
            }
        }
        return compositeDescriptor;
    }

    @java.lang.Override
    public java.lang.String getText(java.lang.Object object) {
        if (object instanceof org.eclipse.mylyn.tasks.core.ITask) {
            org.eclipse.mylyn.tasks.core.ITask task = ((org.eclipse.mylyn.tasks.core.ITask) (object));
            java.lang.String summary = task.getSummary();
            if (summary == null) {
                summary = org.eclipse.mylyn.internal.tasks.ui.Messages.TaskElementLabelProvider__no_summary_available_;
            }
            java.lang.String taskKey = task.getTaskKey();
            if (taskKey != null) {
                return (taskKey + ": ") + summary;// $NON-NLS-1$

            } else {
                return summary;
            }
        } else if (object instanceof org.eclipse.mylyn.tasks.core.IRepositoryElement) {
            org.eclipse.mylyn.tasks.core.IRepositoryElement element = ((org.eclipse.mylyn.tasks.core.IRepositoryElement) (object));
            return element.getSummary();
        } else {
            return super.getText(object);
        }
    }

    public org.eclipse.swt.graphics.Color getForeground(java.lang.Object object) {
        if (object instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask) {
            org.eclipse.mylyn.internal.tasks.core.AbstractTask task = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (object));
            if (task != null) {
                if (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().isCompletedToday(task)) {
                    return themeManager.getCurrentTheme().getColorRegistry().get(org.eclipse.mylyn.commons.ui.compatibility.CommonThemes.COLOR_COMPLETED_TODAY);
                } else if (task.isCompleted()) {
                    return themeManager.getCurrentTheme().getColorRegistry().get(org.eclipse.mylyn.commons.ui.compatibility.CommonThemes.COLOR_COMPLETED);
                } else if (org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().isActive(task)) {
                    return themeManager.getCurrentTheme().getColorRegistry().get(org.eclipse.mylyn.commons.ui.compatibility.CommonThemes.COLOR_TASK_ACTIVE);
                } else if (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().isOverdue(task)) {
                    return themeManager.getCurrentTheme().getColorRegistry().get(org.eclipse.mylyn.commons.ui.compatibility.CommonThemes.COLOR_OVERDUE);
                } else if (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().isDueToday(task)) {
                    return themeManager.getCurrentTheme().getColorRegistry().get(org.eclipse.mylyn.commons.ui.compatibility.CommonThemes.COLOR_SCHEDULED_TODAY);
                } else if ((task.getScheduledForDate() != null) && org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().isPastReminder(task)) {
                    return themeManager.getCurrentTheme().getColorRegistry().get(org.eclipse.mylyn.commons.ui.compatibility.CommonThemes.COLOR_SCHEDULED_PAST);
                } else if (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().isOverdueForOther(task)) {
                    return themeManager.getCurrentTheme().getColorRegistry().get(org.eclipse.mylyn.commons.ui.compatibility.CommonThemes.COLOR_OVERDUE_FOR_OTHERS);
                } else if (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().isScheduledForToday(task)) {
                    return themeManager.getCurrentTheme().getColorRegistry().get(org.eclipse.mylyn.commons.ui.compatibility.CommonThemes.COLOR_SCHEDULED_TODAY);
                } else if (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().isScheduledForThisWeek(task)) {
                    return themeManager.getCurrentTheme().getColorRegistry().get(org.eclipse.mylyn.commons.ui.compatibility.CommonThemes.COLOR_SCHEDULED_THIS_WEEK);
                }
            }
        } else if (object instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) {
            if (object instanceof org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer) {
                return null;
            } else {
                for (org.eclipse.mylyn.tasks.core.ITask child : ((org.eclipse.mylyn.tasks.core.ITaskContainer) (object)).getChildren()) {
                    if (child.isActive() || ((child instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) && showHasActiveChild(((org.eclipse.mylyn.tasks.core.ITaskContainer) (child))))) {
                        return themeManager.getCurrentTheme().getColorRegistry().get(org.eclipse.mylyn.commons.ui.compatibility.CommonThemes.COLOR_TASK_ACTIVE);
                    } else if (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().isOverdue(child)) {
                        return themeManager.getCurrentTheme().getColorRegistry().get(org.eclipse.mylyn.commons.ui.compatibility.CommonThemes.COLOR_OVERDUE);
                    }
                }
            }
        }
        return null;
    }

    public org.eclipse.swt.graphics.Color getBackground(java.lang.Object element) {
        return null;
    }

    public org.eclipse.swt.graphics.Font getFont(java.lang.Object element) {
        if (element instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask) {
            if (((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (element)).isSynchronizing()) {
                if (((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (element)).isActive()) {
                    return org.eclipse.mylyn.commons.ui.compatibility.CommonFonts.BOLD_ITALIC;
                } else {
                    return org.eclipse.mylyn.commons.ui.compatibility.CommonFonts.ITALIC;
                }
            }
        }
        if (element instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery) {
            if (((org.eclipse.mylyn.internal.tasks.core.RepositoryQuery) (element)).isSynchronizing()) {
                return org.eclipse.mylyn.commons.ui.compatibility.CommonFonts.ITALIC;
            }
        }
        if (element instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) {
            for (org.eclipse.mylyn.tasks.core.ITask child : ((org.eclipse.mylyn.tasks.core.ITaskContainer) (element)).getChildren()) {
                if (child.isActive() || ((child instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) && showHasActiveChild(((org.eclipse.mylyn.tasks.core.ITaskContainer) (child))))) {
                    return org.eclipse.mylyn.commons.ui.compatibility.CommonFonts.BOLD;
                }
            }
        }
        if (element instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask) {
            if (((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (element)).isActive()) {
                return org.eclipse.mylyn.commons.ui.compatibility.CommonFonts.BOLD;
            } else if (((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (element)).isCompleted()) {
                if (org.eclipse.mylyn.commons.ui.compatibility.CommonFonts.HAS_STRIKETHROUGH && org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPluginPreferences().getBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.USE_STRIKETHROUGH_FOR_COMPLETED)) {
                    return org.eclipse.mylyn.commons.ui.compatibility.CommonFonts.STRIKETHROUGH;
                } else {
                    return null;
                }
            }
            for (org.eclipse.mylyn.tasks.core.ITask child : ((org.eclipse.mylyn.tasks.core.ITaskContainer) (element)).getChildren()) {
                if (child.isActive() || ((child instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) && showHasActiveChild(((org.eclipse.mylyn.tasks.core.ITaskContainer) (child))))) {
                    return org.eclipse.mylyn.commons.ui.compatibility.CommonFonts.BOLD;
                }
            }
        }
        return null;
    }

    private boolean showHasActiveChild(org.eclipse.mylyn.tasks.core.ITaskContainer container) {
        if (!org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().groupSubtasks(container)) {
            return false;
        }
        return showHasActiveChildHelper(container, new java.util.HashSet<org.eclipse.mylyn.tasks.core.IRepositoryElement>());
    }

    private boolean showHasActiveChildHelper(org.eclipse.mylyn.tasks.core.ITaskContainer container, java.util.Set<org.eclipse.mylyn.tasks.core.IRepositoryElement> visitedContainers) {
        for (org.eclipse.mylyn.tasks.core.IRepositoryElement child : container.getChildren()) {
            if (visitedContainers.contains(child)) {
                continue;
            }
            visitedContainers.add(child);
            if ((child instanceof org.eclipse.mylyn.tasks.core.ITask) && ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (child)).isActive()) {
                return true;
            } else if (child instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) {
                if (showHasActiveChildHelper(((org.eclipse.mylyn.tasks.core.ITaskContainer) (child)), visitedContainers)) {
                    return true;
                }
            }
        }
        return false;
    }
}