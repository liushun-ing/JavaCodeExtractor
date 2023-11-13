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
package org.eclipse.mylyn.internal.tasks.ui.wizards;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
/**
 * A settings page for the local repository properties dialog. Local repositories have no settings, however they may
 * have settings contributed via the taskRepositoryPageContribution.
 *
 * @author David Green
 */
public class LocalRepositorySettingsPage extends org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPage {
    public LocalRepositorySettingsPage(org.eclipse.mylyn.tasks.core.TaskRepository taskRepository) {
        super(Messages.LocalRepositorySettingsPage_Local_Repository_Settings, Messages.LocalRepositorySettingsPage_Configure_the_local_repository, taskRepository);
    }

    @java.lang.Override
    public java.lang.String getConnectorKind() {
        return org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector.CONNECTOR_KIND;
    }

    public java.lang.String getRepositoryUrl() {
        return org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector.REPOSITORY_URL;
    }

    @java.lang.Override
    protected void createSettingControls(org.eclipse.swt.widgets.Composite parent) {
        // nothing to do, since the local repository has no settings
    }

    @java.lang.Override
    protected org.eclipse.core.runtime.IStatus validate() {
        // nothing to do
        return null;
    }

    @java.lang.Override
    protected void createContributionControls(org.eclipse.swt.widgets.Composite parentControl) {
        super.createContributionControls(parentControl);
        // expand the first contribution since we have no other settings
        org.eclipse.swt.widgets.Control[] children = parentControl.getChildren();
        if (children.length > 0) {
            if (children[0] instanceof org.eclipse.ui.forms.widgets.ExpandableComposite) {
                ((org.eclipse.ui.forms.widgets.ExpandableComposite) (children[0])).setExpanded(true);
            }
        }
    }
}