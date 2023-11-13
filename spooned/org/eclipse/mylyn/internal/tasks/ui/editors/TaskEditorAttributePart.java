/**
 * *****************************************************************************
 * Copyright (c) 2011 Tasktop Technologies and others.
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
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMetaData;
/**
 *
 * @author Steffen Pingel
 */
public class TaskEditorAttributePart extends org.eclipse.mylyn.internal.tasks.ui.editors.AbstractTaskEditorAttributeSection {
    public TaskEditorAttributePart() {
        setPartName(Messages.TaskEditorAttributePart_Attributes);
        setNeedsRefresh(true);
    }

    @java.lang.Override
    protected java.util.Collection<org.eclipse.mylyn.tasks.core.data.TaskAttribute> getAttributes() {
        java.util.Map<java.lang.String, org.eclipse.mylyn.tasks.core.data.TaskAttribute> allAttributes = getTaskData().getRoot().getAttributes();
        java.util.List<org.eclipse.mylyn.tasks.core.data.TaskAttribute> attributes = new java.util.ArrayList<org.eclipse.mylyn.tasks.core.data.TaskAttribute>(allAttributes.size());
        for (org.eclipse.mylyn.tasks.core.data.TaskAttribute attribute : allAttributes.values()) {
            org.eclipse.mylyn.tasks.core.data.TaskAttributeMetaData properties = attribute.getMetaData();
            if (org.eclipse.mylyn.tasks.core.data.TaskAttribute.KIND_DEFAULT.equals(properties.getKind())) {
                attributes.add(attribute);
            }
        }
        return attributes;
    }

    @java.lang.Override
    protected java.util.List<org.eclipse.mylyn.tasks.core.data.TaskAttribute> getOverlayAttributes() {
        org.eclipse.mylyn.tasks.core.data.TaskAttribute product = getModel().getTaskData().getRoot().getMappedAttribute(org.eclipse.mylyn.tasks.core.data.TaskAttribute.PRODUCT);
        java.util.List<org.eclipse.mylyn.tasks.core.data.TaskAttribute> attributes = new java.util.ArrayList<org.eclipse.mylyn.tasks.core.data.TaskAttribute>(2);
        if (product != null) {
            attributes.add(product);
        }
        org.eclipse.mylyn.tasks.core.data.TaskAttribute component = getModel().getTaskData().getRoot().getMappedAttribute(org.eclipse.mylyn.tasks.core.data.TaskAttribute.COMPONENT);
        if (component != null) {
            attributes.add(component);
        }
        return attributes;
    }
}