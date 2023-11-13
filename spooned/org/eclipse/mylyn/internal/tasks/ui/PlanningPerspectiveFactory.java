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
package org.eclipse.mylyn.internal.tasks.ui;
import org.eclipse.mylyn.tasks.ui.ITasksUiConstants;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
/**
 *
 * @author Mik Kersten
 */
public class PlanningPerspectiveFactory implements org.eclipse.ui.IPerspectiveFactory {
    public void createInitialLayout(org.eclipse.ui.IPageLayout layout) {
        defineActions(layout);
        defineLayout(layout);
    }

    public void defineActions(org.eclipse.ui.IPageLayout layout) {
        layout.addShowViewShortcut(org.eclipse.ui.IPageLayout.ID_RES_NAV);
        layout.addShowViewShortcut(org.eclipse.ui.IPageLayout.ID_PROP_SHEET);
        layout.addShowViewShortcut(org.eclipse.mylyn.tasks.ui.ITasksUiConstants.ID_VIEW_TASKS);
        // layout.addShowViewShortcut(TaskActivityView.ID);
        layout.addActionSet(org.eclipse.ui.IPageLayout.ID_NAVIGATE_ACTION_SET);
        org.eclipse.mylyn.internal.tasks.ui.PlanningPerspectiveFactory.removeUninterestingActionSets(layout);
    }

    public void defineLayout(org.eclipse.ui.IPageLayout layout) {
        java.lang.String editorArea = layout.getEditorArea();
        org.eclipse.ui.IFolderLayout topRight = layout.createFolder("topRight", org.eclipse.ui.IPageLayout.RIGHT, ((float) (0.6)), editorArea);// $NON-NLS-1$

        topRight.addView(org.eclipse.mylyn.tasks.ui.ITasksUiConstants.ID_VIEW_TASKS);
        // IFolderLayout bottomLeft = layout.createFolder(
        // "bottomLeft", IPageLayout.BOTTOM, (float) 0.50,//$NON-NLS-1$
        // "topLeft");//$NON-NLS-1$
        // bottomLeft.addView(TaskActivityView.ID);
        topRight.addPlaceholder(org.eclipse.ui.IPageLayout.ID_RES_NAV);
        // IFolderLayout bottomRight = layout.createFolder(
        // "bottomRight", IPageLayout.BOTTOM, (float) 0.66,//$NON-NLS-1$
        // editorArea);
        // 
        // bottomRight.addView(IPageLayout.ID_TASK_LIST);
    }

    // XXX e4.0 hiding action sets is no longer supported
    public static void removeUninterestingActionSets(org.eclipse.ui.IPageLayout layout) {
        try {
            java.lang.Class<?> clazz = java.lang.Class.forName("org.eclipse.ui.internal.PageLayout");// $NON-NLS-1$

            if ((clazz != null) && clazz.isInstance(layout)) {
                java.lang.reflect.Method method = clazz.getDeclaredMethod("getActionSets");// $NON-NLS-1$

                java.util.ArrayList<?> actionSets = ((java.util.ArrayList<?>) (method.invoke(layout)));
                actionSets.remove("org.eclipse.ui.edit.text.actionSet.annotationNavigation");// $NON-NLS-1$

                actionSets.remove("org.eclipse.ui.edit.text.actionSet.convertLineDelimitersTo");// $NON-NLS-1$

                actionSets.remove("org.eclipse.ui.externaltools.ExternalToolsSet");// $NON-NLS-1$

            }
        } catch (java.lang.Exception e) {
            // ignore
        }
    }
}