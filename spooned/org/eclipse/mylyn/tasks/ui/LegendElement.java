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
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.swt.graphics.Image;
/**
 *
 * @author Steffen Pingel
 * @since 3.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class LegendElement {
    /**
     *
     * @since 3.0
     */
    public static org.eclipse.mylyn.tasks.ui.LegendElement createTask(java.lang.String label, org.eclipse.jface.resource.ImageDescriptor overlay) {
        return new org.eclipse.mylyn.tasks.ui.LegendElement(label, org.eclipse.mylyn.commons.ui.CommonImages.getCompositeTaskImage(TasksUiImages.TASK, overlay, false));
    }

    private final org.eclipse.swt.graphics.Image image;

    private final java.lang.String label;

    private LegendElement(java.lang.String label, org.eclipse.swt.graphics.Image image) {
        this.label = label;
        this.image = image;
    }

    /**
     *
     * @since 3.0
     */
    public void dispose() {
    }

    /**
     *
     * @since 3.0
     */
    public org.eclipse.swt.graphics.Image getImage() {
        return image;
    }

    /**
     *
     * @since 3.0
     */
    public java.lang.String getLabel() {
        return label;
    }
}