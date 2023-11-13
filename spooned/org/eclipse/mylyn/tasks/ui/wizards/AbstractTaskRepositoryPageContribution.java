/**
 * *****************************************************************************
 * Copyright (c) 2004, 2011 David Green and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.tasks.ui.wizards;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
/**
 * A contribution to a {@link ITaskRepositoryPage}, which enables plug-ins to contribute UI to the task repository
 * settings. subclasses must have a default public constructor.
 *
 * @author David Green
 * @since 3.1
 */
public abstract class AbstractTaskRepositoryPageContribution {
    /**
     * A listener interface that should be implemented by classes wishing to be notified of changes that occur within
     * the contribution.
     *
     * @since 3.1
     */
    public interface Listener {
        /**
         * Called when the state of the contribution changes such that validation should be performed
         *
         * @param contribution
         * 		the contribution that changed
         * @see ITaskRepositoryPageContribution#validate(IProgressMonitor)
         */
        public void validationRequired(org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPageContribution contribution);
    }

    private final java.util.List<org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPageContribution.Listener> listeners = new java.util.concurrent.CopyOnWriteArrayList<org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPageContribution.Listener>();

    private final java.lang.String title;

    private final java.lang.String description;

    private org.eclipse.mylyn.tasks.core.TaskRepository repository;

    private java.lang.String connectorKind;

    private java.lang.String id;

    /**
     *
     * @param title
     * 		the title of the contribution, as displayed to the user, usually used as a section heading
     * @param description
     * 		the description of the contribution, as displayed to the user, typically as a tool-tip
     * @since 3.1
     */
    protected AbstractTaskRepositoryPageContribution(java.lang.String title, java.lang.String description) {
        this.title = title;
        this.description = description;
        this.id = "";// $NON-NLS-1$

    }

    /**
     * Initialize the contribution
     *
     * @param connectorKind
     * 		the kind of connector for which this is a contribution
     * @param repository
     * 		the repository for which this contribution was created, or null if the repository is not yet available
     * @since 3.1
     */
    public void init(java.lang.String connectorKind, org.eclipse.mylyn.tasks.core.TaskRepository repository) {
        this.connectorKind = connectorKind;
        this.repository = repository;
    }

    /**
     * Add a listener to this contribution. The contribution must notify the listener at the appropriate times, for
     * example when a setting has changed in the UI.
     *
     * @see #removeListener(Listener)
     * @since 3.1
     */
    public void addListener(org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPageContribution.Listener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a listener from this contribution.
     *
     * @see #addListener(Listener)
     * @since 3.1
     */
    public void removeListener(org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPageContribution.Listener listener) {
        listeners.remove(listener);
    }

    /**
     *
     * @see IDialogPage#createControl(Composite)
     * @since 3.1
     */
    public abstract org.eclipse.swt.widgets.Control createControl(org.eclipse.swt.widgets.Composite parent);

    /**
     *
     * @see IDialogPage#getTitle()
     * @since 3.1
     */
    public java.lang.String getTitle() {
        return title;
    }

    /**
     *
     * @see IDialogPage#getDescription()
     * @since 3.1
     */
    public java.lang.String getDescription() {
        return description;
    }

    /**
     *
     * @see IWizardPage#isPageComplete()
     * @since 3.1
     */
    public abstract boolean isPageComplete();

    /**
     *
     * @see IWizardPage#canFlipToNextPage()
     * @since 3.1
     */
    public abstract boolean canFlipToNextPage();

    /**
     * Validate the settings of the contribution. Contributions should expect this method to be called often and should
     * thus return quickly. Always called on the UI thread.
     *
     * @return the status (errors) on the contribution, or null if there are none. A MultiStatus should be used to
    return multiple error messages or warnings.
     * @since 3.1
     */
    public abstract org.eclipse.core.runtime.IStatus validate();

    /**
     * Apply the settings in the contribution to the given repository.
     *
     * @param repository
     * 		the repository to which settings should be applied
     * @see ITaskRepositoryPage#applyTo(TaskRepository)
     * @since 3.1
     */
    public abstract void applyTo(org.eclipse.mylyn.tasks.core.TaskRepository repository);

    /**
     * Requests a validation.
     *
     * @see #validate()
     * @since 3.1
     */
    protected void fireValidationRequired() {
        for (org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPageContribution.Listener l : listeners) {
            l.validationRequired(this);
        }
    }

    /**
     * Returns the repository for which this contribution was created, or null if it was created for a new repository.
     *
     * @since 3.1
     */
    protected final org.eclipse.mylyn.tasks.core.TaskRepository getRepository() {
        return repository;
    }

    /**
     * Returns the kind of connector for which this contribution was created.
     *
     * @since 3.1
     */
    protected final java.lang.String getConnectorKind() {
        return connectorKind;
    }

    void setId(java.lang.String id) {
        org.eclipse.core.runtime.Assert.isNotNull(id);
        this.id = id;
    }

    /**
     * Returns the id as it is declared in the contribution extension point.
     */
    public final java.lang.String getId() {
        return id;
    }

    /**
     * Indicate if this contribution is enabled. Subclasses may override to programatically disable the contribution
     * when needed. This method is called early in the contributor's lifecycle, before any controls are created. the
     * default implementation returns true.
     */
    public boolean isEnabled() {
        return true;
    }
}