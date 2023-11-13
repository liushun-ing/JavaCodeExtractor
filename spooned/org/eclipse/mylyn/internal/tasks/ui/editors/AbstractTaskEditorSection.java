/**
 * *****************************************************************************
 * Copyright (c) 2009, 2013 Tasktop Technologies and others.
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
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
/**
 *
 * @author Steffen Pingel
 */
public abstract class AbstractTaskEditorSection extends org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart {
    private org.eclipse.ui.forms.widgets.Section section;

    private final java.util.List<org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart> subParts = new java.util.ArrayList<org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart>();

    public void addSubPart(java.lang.String path, org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart part) {
        subParts.add(part);
    }

    protected abstract org.eclipse.swt.widgets.Control createContent(org.eclipse.ui.forms.widgets.FormToolkit toolkit, org.eclipse.swt.widgets.Composite parent);

    @java.lang.Override
    public void createControl(org.eclipse.swt.widgets.Composite parent, final org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        boolean expand = shouldExpandOnCreate();
        section = createSection(parent, toolkit, expand);
        if (expand) {
            createSectionClient(toolkit, section);
        } else {
            section.addExpansionListener(new org.eclipse.ui.forms.events.ExpansionAdapter() {
                @java.lang.Override
                public void expansionStateChanged(org.eclipse.ui.forms.events.ExpansionEvent event) {
                    if (section.getClient() == null) {
                        createSectionClient(toolkit, section);
                        getTaskEditorPage().reflow();
                    }
                }
            });
        }
        setSection(toolkit, section);
    }

    /**
     * Clients can implement to provide attribute overlay text
     *
     * @param section
     */
    private void createInfoOverlay(org.eclipse.swt.widgets.Composite composite, org.eclipse.ui.forms.widgets.Section section, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        java.lang.String text = getInfoOverlayText();
        if (text == null) {
            return;
        }
        final org.eclipse.swt.widgets.Label label = toolkit.createLabel(composite, org.eclipse.jface.action.LegacyActionTools.escapeMnemonics(text));
        label.setForeground(toolkit.getColors().getColor(org.eclipse.ui.forms.IFormColors.TITLE));
        label.setBackground(null);
        label.setVisible(!section.isExpanded());
        section.addExpansionListener(new org.eclipse.ui.forms.events.ExpansionAdapter() {
            @java.lang.Override
            public void expansionStateChanging(org.eclipse.ui.forms.events.ExpansionEvent e) {
                label.setVisible(!e.getState());
            }
        });
    }

    private void createSectionClient(final org.eclipse.ui.forms.widgets.FormToolkit toolkit, final org.eclipse.ui.forms.widgets.Section section) {
        if (subParts.size() > 0) {
            final org.eclipse.swt.widgets.Composite sectionClient = toolkit.createComposite(section);
            org.eclipse.swt.layout.GridLayout layout = org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.createSectionClientLayout();
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            sectionClient.setLayout(layout);
            org.eclipse.swt.widgets.Control content = createContent(toolkit, sectionClient);
            org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.TOP).grab(true, false).applyTo(content);
            section.setClient(sectionClient);
            for (final org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart part : subParts) {
                org.eclipse.core.runtime.SafeRunner.run(new org.eclipse.core.runtime.ISafeRunnable() {
                    public void handleException(java.lang.Throwable e) {
                        org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, ("Error creating task editor part: \"" + part.getPartId()) + "\"", e));// $NON-NLS-1$ //$NON-NLS-2$

                    }

                    public void run() throws java.lang.Exception {
                        part.createControl(sectionClient, toolkit);
                        if (part.getControl() != null) {
                            org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.TOP).grab(true, false).applyTo(part.getControl());
                        }
                    }
                });
            }
        } else {
            org.eclipse.swt.widgets.Control content = createContent(toolkit, section);
            section.setClient(content);
        }
    }

    /**
     * Clients can override to show summary information for the part.
     */
    protected java.lang.String getInfoOverlayText() {
        return null;
    }

    public org.eclipse.ui.forms.widgets.Section getSection() {
        return section;
    }

    public void removeSubPart(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart part) {
        subParts.remove(part);
    }

    @java.lang.Override
    protected void setSection(org.eclipse.ui.forms.widgets.FormToolkit toolkit, org.eclipse.ui.forms.widgets.Section section) {
        if (section.getTextClient() == null) {
            org.eclipse.jface.action.ToolBarManager toolBarManager = new org.eclipse.jface.action.ToolBarManager(org.eclipse.swt.SWT.FLAT);
            fillToolBar(toolBarManager);
            // TODO toolBarManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
            if (toolBarManager.getSize() > 0) {
                org.eclipse.swt.widgets.Composite toolbarComposite = toolkit.createComposite(section);
                toolbarComposite.setBackground(null);
                org.eclipse.swt.layout.RowLayout rowLayout = new org.eclipse.swt.layout.RowLayout();
                rowLayout.marginLeft = 0;
                rowLayout.marginRight = 0;
                rowLayout.marginTop = 0;
                rowLayout.marginBottom = 0;
                rowLayout.center = true;
                toolbarComposite.setLayout(rowLayout);
                createInfoOverlay(toolbarComposite, section, toolkit);
                toolBarManager.createControl(toolbarComposite);
                section.clientVerticalSpacing = 0;
                section.descriptionVerticalSpacing = 0;
                section.setTextClient(toolbarComposite);
            }
        }
        setControl(section);
    }

    protected abstract boolean shouldExpandOnCreate();
}