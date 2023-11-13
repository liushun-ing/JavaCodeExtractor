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
package org.eclipse.mylyn.tasks.ui.editors;
import org.eclipse.core.runtime.Assert;
/**
 *
 * @author Steffen Pingel
 * @since 3.0
 */
public abstract class TaskEditorPartDescriptor {
    private final java.lang.String id;

    private java.lang.String path;

    // public AbstractTaskEditorPart createPart() {
    // final AbstractTaskEditorPart[] result = new AbstractTaskEditorPart[1];
    // SafeRunnable.run(new ISafeRunnable() {
    // 
    // public void handleException(Throwable exception) {
    // StatusHandler.log(new Status(IStatus.ERROR, TasksUiPlugin.ID_PLUGIN,
    // "Unable to create instance of class \"" + getClassName() + "\" for task editor part \""
    // + getId() + "\""));
    // }
    // 
    // public void run() throws Exception {
    // Class<?> clazz = Class.forName(getClassName());
    // result[0] = (AbstractTaskEditorPart) clazz.newInstance();
    // }
    // 
    // });
    // return result[0];
    // }
    public TaskEditorPartDescriptor(java.lang.String id) {
        org.eclipse.core.runtime.Assert.isNotNull(id);
        this.id = id;
        this.path = AbstractTaskEditorPage.PATH_COMMENTS;
    }

    public abstract org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart createPart();

    @java.lang.Override
    public boolean equals(java.lang.Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor other = ((org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor) (obj));
        return id.equals(other.id);
    }

    public java.lang.String getId() {
        return id;
    }

    public java.lang.String getPath() {
        return path;
    }

    @java.lang.Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + id.hashCode();
        return result;
    }

    public org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor setPath(java.lang.String path) {
        this.path = path;
        return this;
    }
}