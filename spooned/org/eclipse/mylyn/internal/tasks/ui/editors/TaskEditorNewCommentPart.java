/**
 * *****************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
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
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
/**
 *
 * @author Steffen Pingel
 */
public class TaskEditorNewCommentPart extends org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorRichTextPart {
    public TaskEditorNewCommentPart() {
        setPartName(Messages.TaskEditorNewCommentPart_New_Comment);
        setSectionStyle(org.eclipse.ui.forms.widgets.ExpandableComposite.TITLE_BAR | org.eclipse.ui.forms.widgets.ExpandableComposite.EXPANDED);
        setExpandVertically(true);
    }

    @java.lang.Override
    public void initialize(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage taskEditorPage) {
        super.initialize(taskEditorPage);
        setAttribute(getModel().getTaskData().getRoot().getMappedAttribute(org.eclipse.mylyn.tasks.core.data.TaskAttribute.COMMENT_NEW));
    }
}