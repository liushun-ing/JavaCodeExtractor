/**
 * *****************************************************************************
 * Copyright (c) 2010 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.views;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.mylyn.internal.tasks.core.Category;
/**
 *
 * @author Robert Elves
 */
public class EmptyCategoriesFilter extends org.eclipse.jface.viewers.ViewerFilter {
    private final org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoriesContentProvider provider;

    public EmptyCategoriesFilter(org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoriesContentProvider provider) {
        this.provider = provider;
    }

    @java.lang.Override
    public boolean select(org.eclipse.jface.viewers.Viewer viewer, java.lang.Object parentElement, java.lang.Object element) {
        if (element instanceof org.eclipse.mylyn.internal.tasks.core.Category) {
            return provider.getChildren(element).length > 0;
        }
        return true;
    }
}