/**
 * *****************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
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
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor;
/**
 * Create a part descriptor from an extension point
 *
 * @author Shawn Minto
 */
public class TaskEditorExtensionPartDescriptor extends org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor {
    private static final java.lang.String ATTR_CLASS = "class";// $NON-NLS-1$


    private static final java.lang.String ATTR_PATH = "path";// $NON-NLS-1$


    private final org.eclipse.core.runtime.IConfigurationElement element;

    public TaskEditorExtensionPartDescriptor(java.lang.String id, org.eclipse.core.runtime.IConfigurationElement element) {
        super(id);
        this.element = element;
        setPath(element.getAttribute(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensionPartDescriptor.ATTR_PATH));
    }

    @java.lang.Override
    public org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart createPart() {
        try {
            return ((org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart) (element.createExecutableExtension(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensionPartDescriptor.ATTR_CLASS)));
        } catch (java.lang.Exception e) {
            throw new java.lang.RuntimeException(e);
        }
    }
}