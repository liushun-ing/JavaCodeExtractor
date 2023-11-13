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
 *     Frank Becker - fixes for bug 169916
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.views;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.commons.ui.PlatformUiUtil;
import org.eclipse.mylyn.commons.ui.compatibility.CommonFonts;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.core.UnmatchedTaskContainer;
import org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter;
import org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.StateTaskContainer;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITask.SynchronizationState;
import org.eclipse.mylyn.tasks.core.ITaskContainer;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;
/**
 *
 * @author Mik Kersten
 * @author Frank Becker
 */
public class CustomTaskListDecorationDrawer implements org.eclipse.swt.widgets.Listener {
    private final int activationImageOffset;

    private final org.eclipse.swt.graphics.Image taskActive = org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.CONTEXT_ACTIVE);

    private final org.eclipse.swt.graphics.Image taskInactive = org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.CONTEXT_INACTIVE_EMPTY);

    private final org.eclipse.swt.graphics.Image taskInactiveContext = org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.CONTEXT_INACTIVE);

    // see bug 185004
    private final int platformSpecificSquish;

    private boolean useStrikethroughForCompleted;

    private boolean synchronizationOverlaid;

    private boolean focusedMode;

    private final org.eclipse.jface.util.IPropertyChangeListener PROPERTY_LISTENER = new org.eclipse.jface.util.IPropertyChangeListener() {
        public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
            if (event.getProperty().equals(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.USE_STRIKETHROUGH_FOR_COMPLETED)) {
                if (event.getNewValue() instanceof java.lang.Boolean) {
                    useStrikethroughForCompleted = ((java.lang.Boolean) (event.getNewValue()));
                }
            } else if (event.getProperty().equals(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.OVERLAYS_INCOMING_TIGHT)) {
                if (event.getNewValue() instanceof java.lang.Boolean) {
                    synchronizationOverlaid = ((java.lang.Boolean) (event.getNewValue()));
                }
            }
        }
    };

    public CustomTaskListDecorationDrawer(int activationImageOffset, boolean focusedMode) {
        this.activationImageOffset = activationImageOffset;
        this.platformSpecificSquish = org.eclipse.mylyn.commons.ui.PlatformUiUtil.getTreeItemSquish();
        this.synchronizationOverlaid = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.OVERLAYS_INCOMING_TIGHT);
        this.useStrikethroughForCompleted = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.tasks.ui.ITasksUiPreferenceConstants.USE_STRIKETHROUGH_FOR_COMPLETED);
        this.focusedMode = focusedMode;
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(PROPERTY_LISTENER);
    }

    /* NOTE: MeasureItem, PaintItem and EraseItem are called repeatedly.
    Therefore, it is critical for performance that these methods be as
    efficient as possible.
     */
    public void handleEvent(org.eclipse.swt.widgets.Event event) {
        java.lang.Object data = event.item.getData();
        org.eclipse.swt.graphics.Image activationImage = null;
        if (data instanceof org.eclipse.mylyn.tasks.core.ITask) {
            org.eclipse.mylyn.internal.tasks.core.AbstractTask task = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (data));
            if (task.isActive()) {
                activationImage = taskActive;
            } else if (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getContextStore().hasContext(task)) {
                activationImage = taskInactiveContext;
            } else {
                activationImage = taskInactive;
            }
        }
        if (!org.eclipse.mylyn.commons.ui.compatibility.CommonFonts.HAS_STRIKETHROUGH) {
            if ((data instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask) & useStrikethroughForCompleted) {
                org.eclipse.mylyn.internal.tasks.core.AbstractTask task = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (data));
                if (task.isCompleted()) {
                    org.eclipse.swt.graphics.Rectangle bounds;
                    // if (isCOCOA) {
                    bounds = ((org.eclipse.swt.widgets.TreeItem) (event.item)).getTextBounds(0);
                    // } else {
                    // bounds = ((TreeItem) event.item).getBounds();
                    // }
                    int lineY = bounds.y + (bounds.height / 2);
                    java.lang.String itemText = ((org.eclipse.swt.widgets.TreeItem) (event.item)).getText();
                    org.eclipse.swt.graphics.Point extent = event.gc.textExtent(itemText);
                    event.gc.drawLine(bounds.x, lineY, bounds.x + extent.x, lineY);
                }
            }
        }
        if (data instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) {
            switch (event.type) {
                case org.eclipse.swt.SWT.EraseItem :
                    {
                        if ("gtk".equals(org.eclipse.swt.SWT.getPlatform())) {
                            // $NON-NLS-1$
                            // GTK requires drawing on erase event so that images don't disappear when selected.
                            if (activationImage != null) {
                                drawActivationImage(activationImageOffset, event, activationImage);
                            }
                            if (!this.synchronizationOverlaid) {
                                if (data instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) {
                                    drawSyncronizationImage(((org.eclipse.mylyn.tasks.core.ITaskContainer) (data)), event);
                                }
                            }
                        }
                        // TODO: would be nice not to do this on each item's painting
                        // String text = tree.getFilterControl().getText();
                        // if (text != null && !text.equals("") && tree.getViewer().getExpandedElements().length <= 12) {
                        // int offsetY = tree.getViewer().getExpandedElements().length * tree.getViewer().getTree().getItemHeight();
                        // event.gc.drawText("Open search dialog...", 20, offsetY - 10);
                        // }
                        break;
                    }
                case org.eclipse.swt.SWT.PaintItem :
                    {
                        if (activationImage != null) {
                            drawActivationImage(activationImageOffset, event, activationImage);
                        }
                        if (data instanceof org.eclipse.mylyn.tasks.core.ITaskContainer) {
                            drawSyncronizationImage(((org.eclipse.mylyn.tasks.core.ITaskContainer) (data)), event);
                        }
                        break;
                    }
            }
        }
    }

    private void drawSyncronizationImage(org.eclipse.mylyn.tasks.core.ITaskContainer element, org.eclipse.swt.widgets.Event event) {
        org.eclipse.swt.graphics.Image image = null;
        int offsetX = org.eclipse.mylyn.commons.ui.PlatformUiUtil.getIncomingImageOffset();
        int offsetY = (event.height / 2) - 5;
        if (synchronizationOverlaid) {
            offsetX = (event.x + 18) - platformSpecificSquish;
            offsetY += 2;
        }
        if (element != null) {
            if (element instanceof org.eclipse.mylyn.tasks.core.ITask) {
                image = org.eclipse.mylyn.commons.ui.CommonImages.getImage(getSynchronizationImageDescriptor(element, synchronizationOverlaid));
            } else {
                int imageOffset = 0;
                if ((!hideDecorationOnContainer(element, ((org.eclipse.swt.widgets.TreeItem) (event.item)))) && org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter.hasDescendantIncoming(element)) {
                    if (synchronizationOverlaid) {
                        image = org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.OVERLAY_SYNC_OLD_INCOMMING);
                    } else {
                        image = org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.OVERLAY_SYNC_INCOMMING);
                    }
                } else if (element instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery) {
                    org.eclipse.mylyn.internal.tasks.core.RepositoryQuery query = ((org.eclipse.mylyn.internal.tasks.core.RepositoryQuery) (element));
                    if (query.getStatus() != null) {
                        image = org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getIconFromStatusOfQuery(query));
                        if (synchronizationOverlaid) {
                            imageOffset = 11;
                        } else {
                            imageOffset = 3;
                        }
                    }
                }
                int additionalSquish = 0;
                if ((platformSpecificSquish > 0) && synchronizationOverlaid) {
                    additionalSquish = platformSpecificSquish + 3;
                } else if (platformSpecificSquish > 0) {
                    additionalSquish = platformSpecificSquish / 2;
                }
                if (synchronizationOverlaid) {
                    offsetX = (42 - imageOffset) - additionalSquish;
                } else {
                    offsetX = (24 - imageOffset) - additionalSquish;
                }
            }
        }
        if (image != null) {
            event.gc.drawImage(image, offsetX, event.y + offsetY);
        }
    }

    private boolean hideDecorationOnContainer(org.eclipse.mylyn.tasks.core.ITaskContainer element, org.eclipse.swt.widgets.TreeItem treeItem) {
        if (element instanceof org.eclipse.mylyn.internal.tasks.ui.views.TaskScheduleContentProvider.StateTaskContainer) {
            return true;
        } else if (element instanceof org.eclipse.mylyn.internal.tasks.core.UnmatchedTaskContainer) {
            if (!focusedMode) {
                return false;
            } else if (org.eclipse.mylyn.internal.tasks.ui.AbstractTaskListFilter.hasDescendantIncoming(element)) {
                return true;
            }
        } else if (element instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery) {
            org.eclipse.mylyn.internal.tasks.core.RepositoryQuery query = ((org.eclipse.mylyn.internal.tasks.core.RepositoryQuery) (element));
            if (query.getStatus() != null) {
                return true;
            }
        }
        if (focusedMode) {
            return false;
        } else if (!(element instanceof org.eclipse.mylyn.tasks.core.ITask)) {
            return treeItem.getExpanded();
        } else {
            return false;
        }
    }

    protected void drawActivationImage(final int activationImageOffset, org.eclipse.swt.widgets.Event event, org.eclipse.swt.graphics.Image image) {
        org.eclipse.swt.graphics.Rectangle rect = image.getBounds();
        int offset = java.lang.Math.max(0, (event.height - rect.height) / 2);
        event.gc.drawImage(image, activationImageOffset, event.y + offset);
    }

    private org.eclipse.jface.resource.ImageDescriptor getSynchronizationImageDescriptor(java.lang.Object element, boolean synchViewStyle) {
        if (element instanceof org.eclipse.mylyn.tasks.core.ITask) {
            org.eclipse.mylyn.tasks.core.ITask repositoryTask = ((org.eclipse.mylyn.tasks.core.ITask) (element));
            if (repositoryTask.getSynchronizationState() == org.eclipse.mylyn.tasks.core.ITask.SynchronizationState.INCOMING_NEW) {
                if (synchViewStyle) {
                    return org.eclipse.mylyn.commons.ui.CommonImages.OVERLAY_SYNC_OLD_INCOMMING_NEW;
                } else {
                    return org.eclipse.mylyn.commons.ui.CommonImages.OVERLAY_SYNC_INCOMMING_NEW;
                }
            } else if (repositoryTask.getSynchronizationState() == org.eclipse.mylyn.tasks.core.ITask.SynchronizationState.OUTGOING_NEW) {
                if (synchViewStyle) {
                    return org.eclipse.mylyn.commons.ui.CommonImages.OVERLAY_SYNC_OLD_OUTGOING;
                } else {
                    return org.eclipse.mylyn.commons.ui.CommonImages.OVERLAY_SYNC_OUTGOING_NEW;
                }
            }
            org.eclipse.jface.resource.ImageDescriptor imageDescriptor = null;
            if ((repositoryTask.getSynchronizationState() == org.eclipse.mylyn.tasks.core.ITask.SynchronizationState.OUTGOING) || (repositoryTask.getSynchronizationState() == org.eclipse.mylyn.tasks.core.ITask.SynchronizationState.OUTGOING_NEW)) {
                if (synchViewStyle) {
                    imageDescriptor = org.eclipse.mylyn.commons.ui.CommonImages.OVERLAY_SYNC_OLD_OUTGOING;
                } else {
                    imageDescriptor = org.eclipse.mylyn.commons.ui.CommonImages.OVERLAY_SYNC_OUTGOING;
                }
            } else if (repositoryTask.getSynchronizationState() == org.eclipse.mylyn.tasks.core.ITask.SynchronizationState.INCOMING) {
                if (!java.lang.Boolean.parseBoolean(repositoryTask.getAttribute(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.ATTRIBUTE_TASK_SUPPRESS_INCOMING))) {
                    if (synchViewStyle) {
                        imageDescriptor = org.eclipse.mylyn.commons.ui.CommonImages.OVERLAY_SYNC_OLD_INCOMMING;
                    } else {
                        imageDescriptor = org.eclipse.mylyn.commons.ui.CommonImages.OVERLAY_SYNC_INCOMMING;
                    }
                }
            } else if (repositoryTask.getSynchronizationState() == org.eclipse.mylyn.tasks.core.ITask.SynchronizationState.CONFLICT) {
                imageDescriptor = org.eclipse.mylyn.commons.ui.CommonImages.OVERLAY_SYNC_CONFLICT;
            }
            if (((imageDescriptor == null) && (repositoryTask instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask)) && (((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (repositoryTask)).getStatus() != null)) {
                return org.eclipse.mylyn.commons.ui.CommonImages.OVERLAY_SYNC_WARNING;
            } else if (imageDescriptor != null) {
                return imageDescriptor;
            }
        } else if (element instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery) {
            org.eclipse.mylyn.internal.tasks.core.RepositoryQuery query = ((org.eclipse.mylyn.internal.tasks.core.RepositoryQuery) (element));
            return org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getIconFromStatusOfQuery(query);
        }
        // HACK: need a proper blank image
        return org.eclipse.mylyn.commons.ui.CommonImages.OVERLAY_CLEAR;
    }

    public void dispose() {
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(PROPERTY_LISTENER);
    }

    public void setUseStrikethroughForCompleted(boolean useStrikethroughForCompleted) {
        this.useStrikethroughForCompleted = useStrikethroughForCompleted;
    }

    public void setSynchronizationOverlaid(boolean synchronizationOverlaid) {
        this.synchronizationOverlaid = synchronizationOverlaid;
    }

    public boolean isFocusedMode() {
        return focusedMode;
    }

    public void setFocusedMode(boolean focusedMode) {
        this.focusedMode = focusedMode;
    }
}