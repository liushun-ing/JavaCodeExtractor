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
package org.eclipse.mylyn.internal.tasks.ui.editors;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.forms.editor.IFormPage;
public class PlanningPageFactory extends org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory {
    @java.lang.Override
    public boolean canCreatePageFor(org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput input) {
        return true;
    }

    @java.lang.Override
    public org.eclipse.ui.forms.editor.IFormPage createPage(org.eclipse.mylyn.tasks.ui.editors.TaskEditor parentEditor) {
        return new org.eclipse.mylyn.internal.tasks.ui.editors.TaskPlanningEditor(parentEditor);
    }

    @java.lang.Override
    public org.eclipse.swt.graphics.Image getPageImage() {
        return org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.CALENDAR_SMALL);
    }

    @java.lang.Override
    public java.lang.String getPageText() {
        return Messages.PlanningPageFactory_Private;
    }

    @java.lang.Override
    public int getPriority() {
        return PRIORITY_PLANNING;
    }
}