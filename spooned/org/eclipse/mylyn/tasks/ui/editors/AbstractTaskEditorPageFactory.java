/**
 * *****************************************************************************
 * Copyright (c) 2004, 2013 Tasktop Technologies and others.
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
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.forms.editor.IFormPage;
/**
 *
 * @since 3.0
 * @author Steffen Pingel
 */
public abstract class AbstractTaskEditorPageFactory implements org.eclipse.ui.IPluginContribution {
    public static final int PRIORITY_ADDITIONS = 100;

    public static final int PRIORITY_CONTEXT = 20;

    public static final int PRIORITY_PLANNING = 10;

    public static final int PRIORITY_TASK = 30;

    private java.lang.String id;

    private java.lang.String pluginId;

    public abstract boolean canCreatePageFor(@org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput input);

    @org.eclipse.jdt.annotation.NonNull
    public abstract org.eclipse.ui.forms.editor.IFormPage createPage(@org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.ui.editors.TaskEditor parentEditor);

    @org.eclipse.jdt.annotation.Nullable
    public java.lang.String[] getConflictingIds(@org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput input) {
        return null;
    }

    @org.eclipse.jdt.annotation.Nullable
    public java.lang.String getId() {
        return id;
    }

    // TODO EDITOR life cycle of image?
    @org.eclipse.jdt.annotation.NonNull
    public abstract org.eclipse.swt.graphics.Image getPageImage();

    @org.eclipse.jdt.annotation.NonNull
    public abstract java.lang.String getPageText();

    public int getPriority() {
        return org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory.PRIORITY_ADDITIONS;
    }

    public void setId(java.lang.String id) {
        this.id = id;
    }

    /**
     * {@inheritDoc }
     *
     * @since 3.2
     */
    @org.eclipse.jdt.annotation.Nullable
    public final java.lang.String getLocalId() {
        return getId();
    }

    /**
     * {@inheritDoc }
     *
     * @since 3.2
     * @see #setPluginId(String)
     */
    @org.eclipse.jdt.annotation.Nullable
    public final java.lang.String getPluginId() {
        return pluginId;
    }

    /**
     *
     * @since 3.2
     * @see #getPluginId()
     */
    public final void setPluginId(@org.eclipse.jdt.annotation.Nullable
    java.lang.String pluginId) {
        this.pluginId = pluginId;
    }

    /**
     * Clients should override to provide an image for <code>page</code>. Invokes {@link #getPageImage()} for backwards
     * compatibility.
     *
     * @param editor
     * 		the task editor instance
     * @param page
     * 		the page that uses the image
     * @return an image
     * @since 3.10
     */
    @org.eclipse.jdt.annotation.NonNull
    public org.eclipse.swt.graphics.Image getPageImage(@org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.ui.editors.TaskEditor editor, @org.eclipse.jdt.annotation.NonNull
    org.eclipse.ui.forms.editor.IFormPage page) {
        return getPageImage();
    }

    /**
     * Clients should override to provide a label for <code>page</code>. Invokes {@link #getPageText()} for backwards
     * compatibility.
     *
     * @param editor
     * 		the task editor instance
     * @param page
     * 		the page that uses the label
     * @return a label
     * @since 3.10
     */
    @org.eclipse.jdt.annotation.NonNull
    public java.lang.String getPageText(@org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.ui.editors.TaskEditor editor, @org.eclipse.jdt.annotation.NonNull
    org.eclipse.ui.forms.editor.IFormPage page) {
        return getPageText();
    }
}