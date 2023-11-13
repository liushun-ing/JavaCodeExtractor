/**
 * *****************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xiaoyang Guan - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.tasks.ui.editors;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.tasks.core.TaskRepository;
/**
 * Extend to provide HTML preview for ticket description and comments
 *
 * @author Xiaoyang Guan
 * @since 2.1
 */
// TODO 4.0 move to core?
public abstract class AbstractRenderingEngine {
    /**
     * generate HTML preview page for <code>text</code>
     */
    public abstract java.lang.String renderAsHtml(org.eclipse.mylyn.tasks.core.TaskRepository repository, java.lang.String text, org.eclipse.core.runtime.IProgressMonitor monitor) throws org.eclipse.core.runtime.CoreException;
}