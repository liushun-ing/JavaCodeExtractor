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
package org.eclipse.mylyn.internal.tasks.ui.actions;
import org.eclipse.jface.action.Action;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.ui.part.DrillDownAdapter;
/**
 *
 * @author Mik Kersten
 */
public class GoUpAction extends org.eclipse.jface.action.Action {
    public static final java.lang.String ID = "org.eclipse.mylyn.tasklist.actions.view.go.up";// $NON-NLS-1$


    public GoUpAction(org.eclipse.ui.part.DrillDownAdapter drillDownAdapter) {
        setText(Messages.GoUpAction_Go_Up_To_Root);
        setToolTipText(Messages.GoUpAction_Go_Up_To_Root);
        setId(org.eclipse.mylyn.internal.tasks.ui.actions.GoUpAction.ID);
        setImageDescriptor(org.eclipse.mylyn.commons.ui.CommonImages.GO_UP);
    }

    @java.lang.Override
    public void run() {
        if (org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.getFromActivePerspective() != null) {
            org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.getFromActivePerspective().goUpToRoot();
        }
    }
}