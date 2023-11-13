/**
 * *****************************************************************************
 * Copyright (c) 2013 Tasktop Technologies and others.
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
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
public class LastCommentedAttributeEditor extends org.eclipse.mylyn.internal.tasks.ui.editors.DateAttributeEditor {
    public LastCommentedAttributeEditor(org.eclipse.mylyn.tasks.core.data.TaskDataModel manager, org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute) {
        super(manager, taskAttribute);
        setShowDateRelative(true);
    }

    @java.lang.Override
    public java.lang.String getLabel() {
        return Messages.LastCommentedAttributeEditor_dateLastCommented;
    }
}