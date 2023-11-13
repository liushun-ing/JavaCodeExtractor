/**
 * *****************************************************************************
 * Copyright (c) 2004, 2014 David Green and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.editors;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.RegisteredTaskEditorExtension;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPageContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Widget;
/**
 * A contribution that adds a section for 'Editor' on the task repository settings page.
 *
 * @author David Green
 */
public class TaskEditorExtensionSettingsContribution extends org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPageContribution {
    private static final java.lang.String LABEL_NONE = Messages.TaskEditorExtensionSettingsContribution_Plain_Text;

    private static final java.lang.String LABEL_DEFAULT_SUFFIX = Messages.TaskEditorExtensionSettingsContribution__default_;

    private static final java.lang.String DATA_EDITOR_EXTENSION = "editorExtension";// $NON-NLS-1$


    private final org.eclipse.swt.events.SelectionListener listener = new org.eclipse.swt.events.SelectionAdapter() {
        @java.lang.Override
        public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
            selectedExtensionId = ((java.lang.String) (((org.eclipse.swt.widgets.Widget) (e.getSource())).getData(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensionSettingsContribution.DATA_EDITOR_EXTENSION)));
            fireValidationRequired();
        }
    };

    private java.lang.String selectedExtensionId;

    private org.eclipse.swt.widgets.Button avatarSupportButton;

    public TaskEditorExtensionSettingsContribution() {
        super(Messages.TaskEditorExtensionSettingsContribution_Editor, Messages.TaskEditorExtensionSettingsContribution_Select_the_capabilities_of_the_task_editor);
    }

    @java.lang.Override
    public void applyTo(org.eclipse.mylyn.tasks.core.TaskRepository repository) {
        org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.setTaskEditorExtensionId(repository, selectedExtensionId == null ? "none"// $NON-NLS-1$
         : selectedExtensionId);
        repository.setProperty(TaskEditorExtensions.REPOSITORY_PROPERTY_AVATAR_SUPPORT, java.lang.Boolean.toString(avatarSupportButton.getSelection()));
    }

    @java.lang.Override
    public boolean canFlipToNextPage() {
        return true;
    }

    @java.lang.Override
    public boolean isPageComplete() {
        return true;
    }

    @java.lang.Override
    public org.eclipse.swt.widgets.Control createControl(org.eclipse.swt.widgets.Composite parentControl) {
        org.eclipse.swt.widgets.Composite parent = new org.eclipse.swt.widgets.Composite(parentControl, org.eclipse.swt.SWT.NONE);
        org.eclipse.swt.layout.GridLayout layout = new org.eclipse.swt.layout.GridLayout(1, true);
        layout.marginWidth = 0;
        parent.setLayout(layout);
        createGravatarControl(parent);
        org.eclipse.swt.widgets.Group group = new org.eclipse.swt.widgets.Group(parent, org.eclipse.swt.SWT.NONE);
        group.setText(Messages.TaskEditorExtensionSettingsContribution_Rendering_Group_Label);
        group.setLayout(new org.eclipse.swt.layout.GridLayout(1, true));
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
        createTaskEditorExtensionsControl(group);
        return parent;
    }

    private void createGravatarControl(org.eclipse.swt.widgets.Composite parent) {
        avatarSupportButton = new org.eclipse.swt.widgets.Button(parent, org.eclipse.swt.SWT.CHECK);
        avatarSupportButton.setText(Messages.TaskEditorExtensionSettingsContribution_Avatar_Button_Label);
        avatarSupportButton.setSelection((getRepository() != null) && java.lang.Boolean.parseBoolean(getRepository().getProperty(TaskEditorExtensions.REPOSITORY_PROPERTY_AVATAR_SUPPORT)));
    }

    private void createTaskEditorExtensionsControl(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.widgets.Composite infoComposite = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NONE);
        org.eclipse.jface.layout.GridLayoutFactory.fillDefaults().numColumns(2).applyTo(infoComposite);
        org.eclipse.swt.widgets.Label infoImage = new org.eclipse.swt.widgets.Label(infoComposite, org.eclipse.swt.SWT.NONE);
        infoImage.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.INFORMATION));
        org.eclipse.swt.widgets.Label infoLabel = new org.eclipse.swt.widgets.Label(infoComposite, org.eclipse.swt.SWT.NONE);
        infoLabel.setText(Messages.TaskEditorExtensionSettingsContribution_Rendering_Group_Info);
        java.lang.String defaultExtensionId = org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.getDefaultTaskEditorExtensionId(getConnectorKind());
        selectedExtensionId = (getRepository() == null) ? defaultExtensionId : org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.getTaskEditorExtensionId(getRepository());
        // configure a 'Plain Text' (none) button
        org.eclipse.swt.widgets.Button noneButton = new org.eclipse.swt.widgets.Button(parent, org.eclipse.swt.SWT.RADIO);
        java.lang.String noneTitle = org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensionSettingsContribution.LABEL_NONE;
        boolean isDefault = (defaultExtensionId == null) || (defaultExtensionId.length() == 0);
        if (isDefault) {
            noneTitle += org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensionSettingsContribution.LABEL_DEFAULT_SUFFIX;
        }
        noneButton.setText(noneTitle);
        noneButton.addSelectionListener(listener);
        boolean foundSelection = false;
        // now add selection buttons for all registered extensions
        java.util.SortedSet<org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.RegisteredTaskEditorExtension> allEditorExtensions = org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.getTaskEditorExtensions();
        for (org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.RegisteredTaskEditorExtension editorExtension : allEditorExtensions) {
            if (org.eclipse.mylyn.commons.workbench.WorkbenchUtil.allowUseOf(editorExtension)) {
                java.lang.String name = editorExtension.getName();
                isDefault = editorExtension.getId().equals(defaultExtensionId);
                if (isDefault) {
                    name += org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensionSettingsContribution.LABEL_DEFAULT_SUFFIX;
                }
                org.eclipse.swt.widgets.Button button = new org.eclipse.swt.widgets.Button(parent, org.eclipse.swt.SWT.RADIO);
                button.setText(name);
                if (editorExtension.getId().equals(selectedExtensionId)) {
                    foundSelection = true;
                    button.setSelection(true);
                }
                button.setText(name);
                button.setData(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensionSettingsContribution.DATA_EDITOR_EXTENSION, editorExtension.getId());
                button.addSelectionListener(listener);
            }
        }
        if (!foundSelection) {
            noneButton.setSelection(true);
        }
    }

    @java.lang.Override
    public org.eclipse.core.runtime.IStatus validate() {
        // nothing to validate
        return null;
    }

    /**
     * only enabled when there are installed/registered task editor extensions.
     */
    @java.lang.Override
    public boolean isEnabled() {
        return !org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions.getTaskEditorExtensions().isEmpty();
    }
}