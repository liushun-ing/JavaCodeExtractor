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
 *     Kevin Barnes, IBM Corporation - fix for bug 277974
 *     Robert Munteanu - fix for bug 350771
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.views;
import com.google.common.base.Strings;
import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.commons.core.DateUtil;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.commons.ui.GradientToolTip;
import org.eclipse.mylyn.commons.ui.PlatformUiUtil;
import org.eclipse.mylyn.commons.ui.compatibility.CommonFonts;
import org.eclipse.mylyn.commons.workbench.forms.ScalingHyperlink;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.DateRange;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.internal.tasks.core.LocalTask;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.TaskCategory;
import org.eclipse.mylyn.internal.tasks.core.UncategorizedTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.UnmatchedTaskContainer;
import org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter;
import org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants;
import org.eclipse.mylyn.internal.tasks.ui.TaskScalingHyperlink;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotifier;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.StateTaskContainer;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITask.SynchronizationState;
import org.eclipse.mylyn.tasks.core.ITaskContainer;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.TaskElementLabelProvider;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.forms.IFormColors;
/**
 *
 * @author Mik Kersten
 * @author Eric Booth
 * @author Leo Dos Santos
 * @author Steffen Pingel
 */
public class TaskListToolTip extends org.eclipse.mylyn.commons.ui.GradientToolTip {
    public static interface TaskListToolTipListener {
        void toolTipHidden(org.eclipse.swt.widgets.Event event);
    }

    private static final int MAX_WIDTH = 600;

    private static final int X_SHIFT = org.eclipse.mylyn.commons.ui.PlatformUiUtil.getToolTipXShift();

    private static final int Y_SHIFT = 1;

    private org.eclipse.mylyn.tasks.core.IRepositoryElement currentTipElement;

    private final java.util.List<org.eclipse.mylyn.internal.tasks.ui.views.TaskListToolTip.TaskListToolTipListener> listeners = new java.util.ArrayList<org.eclipse.mylyn.internal.tasks.ui.views.TaskListToolTip.TaskListToolTipListener>();

    private boolean visible;

    private boolean triggeredByMouse = true;

    private final org.eclipse.swt.widgets.Control control;

    private final org.eclipse.swt.graphics.Color titleColor;

    private boolean enabled;

    public TaskListToolTip(org.eclipse.swt.widgets.Control control) {
        super(control);
        this.control = control;
        setEnabled(true);
        setShift(new org.eclipse.swt.graphics.Point(1, 1));
        titleColor = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getFormColors(control.getDisplay()).getColor(org.eclipse.ui.forms.IFormColors.TITLE);
    }

    public void dispose() {
        hide();
    }

    @java.lang.Override
    protected void afterHideToolTip(org.eclipse.swt.widgets.Event event) {
        triggeredByMouse = true;
        visible = false;
        for (org.eclipse.mylyn.internal.tasks.ui.views.TaskListToolTip.TaskListToolTipListener listener : listeners.toArray(new org.eclipse.mylyn.internal.tasks.ui.views.TaskListToolTip.TaskListToolTipListener[0])) {
            listener.toolTipHidden(event);
        }
    }

    public void addTaskListToolTipListener(org.eclipse.mylyn.internal.tasks.ui.views.TaskListToolTip.TaskListToolTipListener listener) {
        listeners.add(listener);
    }

    public void removeTaskListToolTipListener(org.eclipse.mylyn.internal.tasks.ui.views.TaskListToolTip.TaskListToolTipListener listener) {
        listeners.remove(listener);
    }

    private org.eclipse.mylyn.tasks.core.IRepositoryElement getTaskListElement(java.lang.Object hoverObject) {
        if (hoverObject instanceof org.eclipse.mylyn.commons.workbench.forms.ScalingHyperlink) {
            org.eclipse.mylyn.internal.tasks.ui.TaskScalingHyperlink hyperlink = ((org.eclipse.mylyn.internal.tasks.ui.TaskScalingHyperlink) (hoverObject));
            return hyperlink.getTask();
        } else if (hoverObject instanceof org.eclipse.swt.widgets.Widget) {
            java.lang.Object data = ((org.eclipse.swt.widgets.Widget) (hoverObject)).getData();
            if (data != null) {
                if (data instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) {
                    return ((org.eclipse.mylyn.tasks.core.IRepositoryElement) (data));
                } else if (data instanceof org.eclipse.core.runtime.IAdaptable) {
                    return ((org.eclipse.mylyn.tasks.core.IRepositoryElement) (((org.eclipse.core.runtime.IAdaptable) (data)).getAdapter(org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer.class)));
                }
            }
        }
        return null;
    }

    private java.lang.String getTitleText(org.eclipse.mylyn.tasks.core.IRepositoryElement element) {
        if (element instanceof org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer) {
            java.lang.StringBuilder sb = new java.lang.StringBuilder();
            sb.append(((org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer) (element)).getShortSummary());
            if (!(element instanceof org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.StateTaskContainer)) {
                java.util.Calendar start = ((org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer) (element)).getDateRange().getStartDate();
                sb.append(" - ");// $NON-NLS-1$

                sb.append(com.ibm.icu.text.DateFormat.getDateInstance(com.ibm.icu.text.DateFormat.LONG).format(start.getTime()));
            }
            return sb.toString();
        } else if (element instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery) {
            org.eclipse.mylyn.tasks.core.IRepositoryQuery query = ((org.eclipse.mylyn.tasks.core.IRepositoryQuery) (element));
            java.lang.StringBuilder sb = new java.lang.StringBuilder();
            sb.append(element.getSummary());
            sb.append("  [");// $NON-NLS-1$

            sb.append(getRepositoryLabel(query.getConnectorKind(), query.getRepositoryUrl()));
            sb.append("]");// $NON-NLS-1$

            return sb.toString();
        } else if (element instanceof org.eclipse.mylyn.tasks.core.ITask) {
            return ((org.eclipse.mylyn.tasks.core.ITask) (element)).getSummary();
        } else {
            return new org.eclipse.mylyn.tasks.ui.TaskElementLabelProvider(false).getText(element);
        }
    }

    private java.lang.String getDetailsText(org.eclipse.mylyn.tasks.core.IRepositoryElement element) {
        if (element instanceof org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer) {
            org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer container = ((org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer) (element));
            int estimateTotal = 0;
            long activeTotal = 0;
            for (org.eclipse.mylyn.tasks.core.ITask child : container.getChildren()) {
                if (child instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask) {
                    estimateTotal += ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (child)).getEstimatedTimeHours();
                    activeTotal += org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().getElapsedTime(child, container.getDateRange());
                }
            }
            java.lang.StringBuilder sb = new java.lang.StringBuilder();
            appendEstimateAndActive(sb, estimateTotal, activeTotal);
            return sb.toString();
        } else if (element instanceof org.eclipse.mylyn.tasks.core.ITask) {
            org.eclipse.mylyn.tasks.core.ITask task = ((org.eclipse.mylyn.tasks.core.ITask) (element));
            java.lang.StringBuilder sb = new java.lang.StringBuilder();
            org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getConnectorUi(task.getConnectorKind());
            java.lang.String kindLabel = null;
            if (connectorUi != null) {
                kindLabel = connectorUi.getTaskKindLabel(task);
                sb.append(kindLabel);
            }
            java.lang.String key = task.getTaskKey();
            if (key != null) {
                sb.append(" ");// $NON-NLS-1$

                sb.append(key);
            }
            java.lang.String taskKind = task.getTaskKind();
            if (((taskKind != null) && (taskKind.length() > 0)) && (!taskKind.equals(kindLabel))) {
                sb.append(" (");// $NON-NLS-1$

                sb.append(taskKind);
                sb.append(") ");// $NON-NLS-1$

            }
            sb.append(", ");// $NON-NLS-1$

            java.lang.String priorityLabel = task.getAttribute(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.ATTRIBUTE_PRIORITY_LABEL);
            if (com.google.common.base.Strings.isNullOrEmpty(priorityLabel)) {
                priorityLabel = task.getPriority();
            }
            sb.append(priorityLabel);
            sb.append("  [");// $NON-NLS-1$

            sb.append(getRepositoryLabel(task.getConnectorKind(), task.getRepositoryUrl()));
            sb.append("]");// $NON-NLS-1$

            return sb.toString();
        } else {
            return null;
        }
    }

    private void appendEstimateAndActive(java.lang.StringBuilder sb, int estimateTotal, long activeTotal) {
        sb.append(org.eclipse.osgi.util.NLS.bind(Messages.TaskListToolTip_Estimate, estimateTotal));
        sb.append("\n");// $NON-NLS-1$

        if (org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.isActivityTrackingEnabled()) {
            sb.append(org.eclipse.osgi.util.NLS.bind(Messages.TaskListToolTip_Active_X, org.eclipse.mylyn.commons.core.DateUtil.getFormattedDurationShort(activeTotal)));
            sb.append("\n");// $NON-NLS-1$

        }
    }

    private java.lang.String getRepositoryLabel(java.lang.String repositoryKind, java.lang.String repositoryUrl) {
        org.eclipse.mylyn.tasks.core.TaskRepository repository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(repositoryKind, repositoryUrl);
        if (repository != null) {
            java.lang.String label = repository.getRepositoryLabel();
            if (label.indexOf("//") != (-1)) {
                // $NON-NLS-1$
                return label.substring(repository.getRepositoryUrl().indexOf("//") + 2);// $NON-NLS-1$

            }
            return label;
        }
        return "";// $NON-NLS-1$

    }

    private java.lang.String getOwnerText(org.eclipse.mylyn.tasks.core.IRepositoryElement element) {
        if ((element instanceof org.eclipse.mylyn.tasks.core.ITask) && (!(element instanceof org.eclipse.mylyn.internal.tasks.core.LocalTask))) {
            java.lang.String owner = ((org.eclipse.mylyn.tasks.core.ITask) (element)).getOwner();
            if (!com.google.common.base.Strings.isNullOrEmpty(owner)) {
                return org.eclipse.osgi.util.NLS.bind(Messages.TaskListToolTip_Assigned_to_X, owner);
            }
            return org.eclipse.osgi.util.NLS.bind(Messages.TaskListToolTip_Assigned_to_X, Messages.TaskListToolTip_Unassigned);
        }
        return null;
    }

    private org.eclipse.swt.graphics.Image getOwnerImage(org.eclipse.mylyn.tasks.core.IRepositoryElement element) {
        if ((element instanceof org.eclipse.mylyn.tasks.core.ITask) && (!(element instanceof org.eclipse.mylyn.internal.tasks.core.LocalTask))) {
            org.eclipse.mylyn.tasks.core.ITask task = ((org.eclipse.mylyn.tasks.core.ITask) (element));
            org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryConnector(task.getConnectorKind());
            org.eclipse.mylyn.tasks.core.TaskRepository repository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(task.getConnectorKind(), task.getRepositoryUrl());
            if (com.google.common.base.Strings.isNullOrEmpty(task.getOwner())) {
                return null;
            } else if (connector.isOwnedByUser(repository, task)) {
                return org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.PERSON_ME);
            } else {
                return org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.PERSON);
            }
        }
        return null;
    }

    private java.lang.String getExtendedToolTipText(org.eclipse.mylyn.tasks.core.IRepositoryElement element) {
        if (element instanceof org.eclipse.mylyn.tasks.core.ITask) {
            org.eclipse.mylyn.tasks.core.ITask task = ((org.eclipse.mylyn.tasks.core.ITask) (element));
            java.lang.String extendedToolTipInfo = task.getAttribute(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.ATTRIBUTE_TASK_EXTENDED_TOOLTIP);
            if ((extendedToolTipInfo != null) && (extendedToolTipInfo.length() > 0)) {
                return extendedToolTipInfo;
            }
        }
        return null;
    }

    private java.lang.String getActivityText(org.eclipse.mylyn.tasks.core.IRepositoryElement element) {
        if (element instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask) {
            org.eclipse.mylyn.internal.tasks.core.AbstractTask task = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (element));
            java.lang.StringBuilder sb = new java.lang.StringBuilder();
            java.util.Date dueDate = task.getDueDate();
            if (dueDate != null) {
                sb.append(org.eclipse.osgi.util.NLS.bind(Messages.TaskListToolTip_Due, new java.lang.Object[]{ new com.ibm.icu.text.SimpleDateFormat("E").format(dueDate)// $NON-NLS-1$
                , com.ibm.icu.text.DateFormat.getDateInstance(com.ibm.icu.text.DateFormat.LONG).format(dueDate), com.ibm.icu.text.DateFormat.getTimeInstance(com.ibm.icu.text.DateFormat.SHORT).format(dueDate) }));
                sb.append("\n");// $NON-NLS-1$

            }
            org.eclipse.mylyn.internal.tasks.core.DateRange scheduledDate = task.getScheduledForDate();
            if (scheduledDate != null) {
                sb.append(org.eclipse.osgi.util.NLS.bind(Messages.TaskListToolTip_Scheduled, scheduledDate.toString()));
                sb.append("\n");// $NON-NLS-1$

            }
            long elapsed = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().getElapsedTime(task);
            appendEstimateAndActive(sb, task.getEstimatedTimeHours(), elapsed);
            return sb.toString();
        }
        return null;
    }

    private java.lang.String getIncomingText(org.eclipse.mylyn.tasks.core.IRepositoryElement element) {
        java.lang.String text = null;
        if (element instanceof org.eclipse.mylyn.tasks.core.ITask) {
            org.eclipse.mylyn.tasks.core.ITask task = ((org.eclipse.mylyn.tasks.core.ITask) (element));
            if (task.getSynchronizationState().isIncoming()) {
                text = task.getAttribute(org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotifier.KEY_INCOMING_NOTIFICATION_TEXT);
                if (com.google.common.base.Strings.isNullOrEmpty(text)) {
                    org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotifier notifier = new org.eclipse.mylyn.internal.tasks.ui.notifications.TaskListNotifier(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskDataManager(), org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getSynchronizationManger());
                    text = notifier.computeNotificationText(task);
                }
            }
        }
        return text;
    }

    private org.eclipse.jface.resource.ImageDescriptor getIncomingImage() {
        org.eclipse.jface.resource.ImageDescriptor incomingImage = org.eclipse.mylyn.commons.ui.CommonImages.OVERLAY_SYNC_INCOMMING;
        if (currentTipElement instanceof org.eclipse.mylyn.tasks.core.ITask) {
            org.eclipse.mylyn.tasks.core.ITask task = ((org.eclipse.mylyn.tasks.core.ITask) (currentTipElement));
            if (task.getSynchronizationState() == org.eclipse.mylyn.tasks.core.ITask.SynchronizationState.INCOMING_NEW) {
                incomingImage = org.eclipse.mylyn.commons.ui.CommonImages.OVERLAY_SYNC_INCOMMING_NEW;
            }
        }
        return incomingImage;
    }

    private java.lang.String getSynchronizationStateText(org.eclipse.mylyn.tasks.core.IRepositoryElement element) {
        if (element instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) {
            java.util.Collection<org.eclipse.mylyn.tasks.core.ITask> tasks = ((org.eclipse.mylyn.tasks.core.ITaskContainer) (element)).getChildren();
            if (tasks.size() > 0) {
                int incoming = 0;
                int outgoing = 0;
                for (org.eclipse.mylyn.tasks.core.ITask task : tasks) {
                    if (task.getSynchronizationState().isIncoming()) {
                        incoming++;
                    }
                    if (task.getSynchronizationState().isOutgoing()) {
                        outgoing++;
                    }
                }
                return org.eclipse.osgi.util.NLS.bind(Messages.TaskListToolTip_Incoming_Outgoing, new java.lang.Object[]{ incoming, outgoing });
            }
        }
        return null;
    }

    private org.eclipse.swt.graphics.Image getStatusIcon(org.eclipse.mylyn.tasks.core.IRepositoryElement element) {
        if (element instanceof org.eclipse.mylyn.internal.tasks.core.RepositoryQuery) {
            return org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getIconFromStatusOfQuery(((org.eclipse.mylyn.internal.tasks.core.RepositoryQuery) (element))));
        }
        return org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.WARNING);
    }

    private java.lang.String getStatusText(org.eclipse.mylyn.tasks.core.IRepositoryElement element) {
        org.eclipse.core.runtime.IStatus status = null;
        if (element instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask) {
            org.eclipse.mylyn.internal.tasks.core.AbstractTask task = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (element));
            status = task.getStatus();
        } else if (element instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery) {
            org.eclipse.mylyn.internal.tasks.core.RepositoryQuery query = ((org.eclipse.mylyn.internal.tasks.core.RepositoryQuery) (element));
            status = query.getStatus();
        }
        if (status != null) {
            java.lang.StringBuilder sb = new java.lang.StringBuilder();
            sb.append(status.getMessage());
            if ((status instanceof org.eclipse.mylyn.tasks.core.RepositoryStatus) && ((org.eclipse.mylyn.tasks.core.RepositoryStatus) (status)).isHtmlMessage()) {
                sb.append(Messages.TaskListToolTip_Please_synchronize_manually_for_full_error_message);
            }
            return sb.toString();
        }
        return null;
    }

    @java.lang.Override
    public org.eclipse.swt.graphics.Point getLocation(org.eclipse.swt.graphics.Point tipSize, org.eclipse.swt.widgets.Event event) {
        org.eclipse.swt.widgets.Widget widget = getTipWidget(event);
        if (widget != null) {
            org.eclipse.swt.graphics.Rectangle bounds = getBounds(widget);
            if (bounds != null) {
                return control.toDisplay(bounds.x + org.eclipse.mylyn.internal.tasks.ui.views.TaskListToolTip.X_SHIFT, (bounds.y + bounds.height) + org.eclipse.mylyn.internal.tasks.ui.views.TaskListToolTip.Y_SHIFT);
            }
        }
        return super.getLocation(tipSize, event);
    }

    private org.eclipse.mylyn.internal.tasks.ui.views.TaskListToolTip.ProgressData getProgressData(org.eclipse.mylyn.tasks.core.IRepositoryElement element) {
        if (element instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) {
            java.lang.Object[] children = new java.lang.Object[0];
            children = ((org.eclipse.mylyn.tasks.core.ITaskContainer) (element)).getChildren().toArray();
            int total = children.length;
            if (total > 0) {
                int completed = 0;
                for (org.eclipse.mylyn.tasks.core.ITask task : ((org.eclipse.mylyn.tasks.core.ITaskContainer) (element)).getChildren()) {
                    if (task.isCompleted()) {
                        completed++;
                    }
                }
                java.lang.String text = org.eclipse.osgi.util.NLS.bind(Messages.TaskListToolTip_Total_Complete_Incomplete, new java.lang.Object[]{ // 
                total, completed, total - completed });
                return new org.eclipse.mylyn.internal.tasks.ui.views.TaskListToolTip.ProgressData(completed, total, text);
            }
        }
        return null;
    }

    private org.eclipse.swt.graphics.Image getImage(org.eclipse.mylyn.tasks.core.IRepositoryElement element) {
        if (element instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery) {
            org.eclipse.mylyn.tasks.core.IRepositoryQuery query = ((org.eclipse.mylyn.tasks.core.IRepositoryQuery) (element));
            org.eclipse.mylyn.tasks.core.TaskRepository repository = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryManager().getRepository(query.getRepositoryUrl());
            if (repository != null) {
                return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getBrandManager().getBrandingIcon(repository);
            }
            return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getBrandManager().getDefaultBrandingIcon(query.getConnectorKind());
        } else if (element instanceof org.eclipse.mylyn.tasks.core.ITask) {
            org.eclipse.mylyn.tasks.core.ITask task = ((org.eclipse.mylyn.tasks.core.ITask) (element));
            org.eclipse.mylyn.tasks.core.TaskRepository repository = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryManager().getRepository(task.getRepositoryUrl());
            if (repository != null) {
                return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getBrandManager().getBrandingIcon(repository);
            }
            return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getBrandManager().getDefaultBrandingIcon(task.getConnectorKind());
        } else if (element instanceof org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer) {
            return org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.CALENDAR);
        }
        return null;
    }

    protected org.eclipse.swt.widgets.Widget getTipWidget(org.eclipse.swt.widgets.Event event) {
        org.eclipse.swt.graphics.Point widgetPosition = new org.eclipse.swt.graphics.Point(event.x, event.y);
        org.eclipse.swt.widgets.Widget widget = event.widget;
        if (widget instanceof org.eclipse.swt.widgets.ToolBar) {
            org.eclipse.swt.widgets.ToolBar w = ((org.eclipse.swt.widgets.ToolBar) (widget));
            return w.getItem(widgetPosition);
        }
        if (widget instanceof org.eclipse.swt.widgets.Table) {
            org.eclipse.swt.widgets.Table w = ((org.eclipse.swt.widgets.Table) (widget));
            return w.getItem(widgetPosition);
        }
        if (widget instanceof org.eclipse.swt.widgets.Tree) {
            org.eclipse.swt.widgets.Tree w = ((org.eclipse.swt.widgets.Tree) (widget));
            return w.getItem(widgetPosition);
        }
        return widget;
    }

    private org.eclipse.swt.graphics.Rectangle getBounds(org.eclipse.swt.widgets.Widget widget) {
        if (widget instanceof org.eclipse.swt.widgets.ToolItem) {
            org.eclipse.swt.widgets.ToolItem w = ((org.eclipse.swt.widgets.ToolItem) (widget));
            return w.getBounds();
        }
        if (widget instanceof org.eclipse.swt.widgets.TableItem) {
            org.eclipse.swt.widgets.TableItem w = ((org.eclipse.swt.widgets.TableItem) (widget));
            return w.getBounds();
        }
        if (widget instanceof org.eclipse.swt.widgets.TreeItem) {
            org.eclipse.swt.widgets.TreeItem w = ((org.eclipse.swt.widgets.TreeItem) (widget));
            return w.getBounds();
        }
        return null;
    }

    @java.lang.Override
    protected boolean shouldCreateToolTip(org.eclipse.swt.widgets.Event event) {
        currentTipElement = null;
        if (isTriggeredByMouse() && (!enabled)) {
            return false;
        }
        if (super.shouldCreateToolTip(event)) {
            org.eclipse.swt.widgets.Widget tipWidget = getTipWidget(event);
            if (tipWidget != null) {
                org.eclipse.swt.graphics.Rectangle bounds = getBounds(tipWidget);
                if (tipWidget instanceof org.eclipse.mylyn.commons.workbench.forms.ScalingHyperlink) {
                    currentTipElement = getTaskListElement(tipWidget);
                } else if ((bounds != null) && contains(bounds.x, bounds.y)) {
                    currentTipElement = getTaskListElement(tipWidget);
                }
            }
        }
        if (currentTipElement == null) {
            hide();
            return false;
        } else {
            return true;
        }
    }

    private boolean contains(int x, int y) {
        if (control instanceof org.eclipse.swt.widgets.Scrollable) {
            return ((org.eclipse.swt.widgets.Scrollable) (control)).getClientArea().contains(x, y);
        } else {
            return control.getBounds().contains(x, y);
        }
    }

    @java.lang.Override
    protected org.eclipse.swt.widgets.Composite createToolTipArea(org.eclipse.swt.widgets.Event event, org.eclipse.swt.widgets.Composite parent) {
        assert currentTipElement != null;
        org.eclipse.swt.widgets.Composite composite = createToolTipContentAreaComposite(parent);
        addIconAndLabel(composite, getImage(currentTipElement), getTitleText(currentTipElement), true);
        java.lang.String detailsText = getDetailsText(currentTipElement);
        if (detailsText != null) {
            addIconAndLabel(composite, null, detailsText);
        }
        java.lang.String ownerText = getOwnerText(currentTipElement);
        if (ownerText != null) {
            addIconAndLabel(composite, getOwnerImage(currentTipElement), ownerText);
        }
        java.lang.String extendedText = getExtendedToolTipText(currentTipElement);
        if (extendedText != null) {
            addIconAndLabel(composite, null, extendedText);
        }
        java.lang.String synchText = getSynchText(currentTipElement);
        if (synchText != null) {
            addIconAndLabel(composite, org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.REPOSITORY_SYNCHRONIZE), synchText);
        }
        java.lang.String activityText = getActivityText(currentTipElement);
        if (activityText != null) {
            addIconAndLabel(composite, org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.CALENDAR), activityText);
        }
        java.lang.String incomingText = getIncomingText(currentTipElement);
        if (!org.apache.commons.lang.StringUtils.isEmpty(incomingText)) {
            org.eclipse.jface.resource.ImageDescriptor incomingImage = getIncomingImage();
            addIconAndLabel(composite, org.eclipse.mylyn.commons.ui.CommonImages.getImage(incomingImage), incomingText);
        }
        java.lang.String synchronizationStateText = getSynchronizationStateText(currentTipElement);
        if (synchronizationStateText != null) {
            addIconAndLabel(composite, null, synchronizationStateText);
        }
        org.eclipse.mylyn.internal.tasks.ui.views.TaskListToolTip.ProgressData progress = getProgressData(currentTipElement);
        if (progress != null) {
            addIconAndLabel(composite, null, progress.text);
            // label height need to be set to 0 to remove gap below the progress bar
            org.eclipse.swt.widgets.Label label = new org.eclipse.swt.widgets.Label(composite, org.eclipse.swt.SWT.NONE);
            org.eclipse.swt.layout.GridData labelGridData = new org.eclipse.swt.layout.GridData(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.TOP, true, false);
            labelGridData.heightHint = 0;
            label.setLayoutData(labelGridData);
            org.eclipse.swt.widgets.Composite progressComposite = new org.eclipse.swt.widgets.Composite(composite, org.eclipse.swt.SWT.NONE);
            org.eclipse.swt.layout.GridLayout progressLayout = new org.eclipse.swt.layout.GridLayout(1, false);
            progressLayout.marginWidth = 0;
            progressLayout.marginHeight = 0;
            progressComposite.setLayout(progressLayout);
            progressComposite.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.TOP, true, false));
            org.eclipse.mylyn.internal.tasks.ui.views.WorkweekProgressBar taskProgressBar = new org.eclipse.mylyn.internal.tasks.ui.views.WorkweekProgressBar(progressComposite);
            taskProgressBar.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.TOP, true, false));
            taskProgressBar.reset(progress.completed, progress.total);
            // do we really need custom canvas? code below renders the same
            // IThemeManager themeManager = PlatformUI.getWorkbench().getThemeManager();
            // Color color = themeManager.getCurrentTheme().getColorRegistry().get(
            // TaskListColorsAndFonts.THEME_COLOR_TASK_TODAY_COMPLETED);
            // ProgressBar bar = new ProgressBar(tipShell, SWT.SMOOTH);
            // bar.setForeground(color);
            // bar.setSelection((int) (100d * progress.completed / progress.total));
            // GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, false);
            // gridData.heightHint = 5;
            // bar.setLayoutData(gridData);
        }
        java.lang.String statusText = getStatusText(currentTipElement);
        if (statusText != null) {
            addIconAndLabel(composite, getStatusIcon(currentTipElement), statusText);
        }
        java.lang.String helpText = getHelpText(currentTipElement);
        if (helpText != null) {
            addIconAndLabel(composite, org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.QUESTION), helpText);
        }
        visible = true;
        return composite;
    }

    private java.lang.String getHelpText(org.eclipse.mylyn.tasks.core.IRepositoryElement element) {
        if ((element instanceof org.eclipse.mylyn.internal.tasks.core.TaskCategory) || (element instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery)) {
            if (org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter.hasDescendantIncoming(((org.eclipse.mylyn.tasks.core.ITaskContainer) (element)))) {
                org.eclipse.mylyn.internal.tasks.ui.views.TaskListView taskListView = org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.getFromActivePerspective();
                if (taskListView != null) {
                    if ((!taskListView.isFocusedMode()) && org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.FILTER_COMPLETE_MODE)) {
                        java.lang.Object[] children = ((org.eclipse.mylyn.internal.tasks.ui.views.TaskListContentProvider) (taskListView.getViewer().getContentProvider())).getChildren(element);
                        boolean hasIncoming = false;
                        for (java.lang.Object child : children) {
                            if (child instanceof org.eclipse.mylyn.tasks.core.ITask) {
                                if (org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.shouldShowIncoming(((org.eclipse.mylyn.tasks.core.ITask) (child)))) {
                                    hasIncoming = true;
                                    break;
                                }
                            }
                        }
                        if (!hasIncoming) {
                            return Messages.TaskListToolTip_Some_incoming_elements_may_be_filtered;
                        }
                    }
                }
            }
            // if has incoming but no top level children have incoming, suggest incoming tasks may be filtered
        }
        if (element instanceof org.eclipse.mylyn.internal.tasks.core.UncategorizedTaskContainer) {
            return Messages.TaskListToolTip_Automatic_container_for_all_local_tasks;
        } else if (element instanceof org.eclipse.mylyn.internal.tasks.core.UnmatchedTaskContainer) {
            return Messages.TaskListToolTip_Automatic_container_for_repository_tasks;
        }
        return null;
    }

    protected org.eclipse.swt.widgets.Composite createToolTipContentAreaComposite(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.widgets.Composite composite = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NONE);
        org.eclipse.swt.layout.GridLayout gridLayout = new org.eclipse.swt.layout.GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginWidth = 5;
        gridLayout.marginHeight = 2;
        composite.setLayout(gridLayout);
        composite.setBackground(composite.getDisplay().getSystemColor(org.eclipse.swt.SWT.COLOR_INFO_BACKGROUND));
        return composite;
    }

    private java.lang.String getSynchText(org.eclipse.mylyn.tasks.core.IRepositoryElement element) {
        if (element instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery) {
            java.lang.String syncStamp = ((org.eclipse.mylyn.internal.tasks.core.RepositoryQuery) (element)).getLastSynchronizedTimeStamp();
            if (syncStamp != null) {
                return org.eclipse.osgi.util.NLS.bind(Messages.TaskListToolTip_Synchronized, syncStamp);
            }
        }
        return null;
    }

    private java.lang.String removeTrailingNewline(java.lang.String text) {
        if (text.endsWith("\n")) {
            // $NON-NLS-1$
            return text.substring(0, text.length() - 1);
        }
        return text;
    }

    protected void addIconAndLabel(org.eclipse.swt.widgets.Composite parent, org.eclipse.swt.graphics.Image image, java.lang.String text) {
        addIconAndLabel(parent, image, text, false);
    }

    protected void addIconAndLabel(org.eclipse.swt.widgets.Composite parent, org.eclipse.swt.graphics.Image image, java.lang.String text, boolean title) {
        org.eclipse.swt.widgets.Label imageLabel = new org.eclipse.swt.widgets.Label(parent, org.eclipse.swt.SWT.NONE);
        imageLabel.setForeground(parent.getDisplay().getSystemColor(org.eclipse.swt.SWT.COLOR_INFO_FOREGROUND));
        imageLabel.setBackground(parent.getDisplay().getSystemColor(org.eclipse.swt.SWT.COLOR_INFO_BACKGROUND));
        imageLabel.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.HORIZONTAL_ALIGN_BEGINNING | org.eclipse.swt.layout.GridData.VERTICAL_ALIGN_BEGINNING));
        imageLabel.setImage(image);
        org.eclipse.swt.widgets.Label textLabel = new org.eclipse.swt.widgets.Label(parent, org.eclipse.swt.SWT.WRAP);
        if (title) {
            textLabel.setFont(org.eclipse.mylyn.commons.ui.compatibility.CommonFonts.BOLD);
        }
        textLabel.setForeground(titleColor);
        textLabel.setBackground(parent.getDisplay().getSystemColor(org.eclipse.swt.SWT.COLOR_INFO_BACKGROUND));
        textLabel.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_HORIZONTAL | org.eclipse.swt.layout.GridData.VERTICAL_ALIGN_CENTER));
        text = removeTrailingNewline(text);
        textLabel.setText(org.eclipse.jface.action.LegacyActionTools.escapeMnemonics(text));
        int width = java.lang.Math.min(textLabel.computeSize(org.eclipse.swt.SWT.DEFAULT, org.eclipse.swt.SWT.DEFAULT).x, org.eclipse.mylyn.internal.tasks.ui.views.TaskListToolTip.MAX_WIDTH);
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.BEGINNING).hint(width, org.eclipse.swt.SWT.DEFAULT).applyTo(textLabel);
    }

    private static class ProgressData {
        int completed;

        int total;

        java.lang.String text;

        public ProgressData(int completed, int total, java.lang.String text) {
            this.completed = completed;
            this.total = total;
            this.text = text;
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isTriggeredByMouse() {
        return triggeredByMouse;
    }

    @java.lang.Override
    public void show(org.eclipse.swt.graphics.Point location) {
        triggeredByMouse = false;
        super.show(location);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}