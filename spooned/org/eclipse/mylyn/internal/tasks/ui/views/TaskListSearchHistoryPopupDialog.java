/**
 * *****************************************************************************
 * Copyright (c) 2010, 2011 Tasktop Technologies and others.
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
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.mylyn.commons.ui.GradientCanvas;
import org.eclipse.mylyn.commons.ui.GradientColors;
import org.eclipse.mylyn.commons.ui.compatibility.CommonColors;
import org.eclipse.mylyn.commons.workbench.search.SearchHistoryPopupDialog;
import org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
public class TaskListSearchHistoryPopupDialog extends org.eclipse.mylyn.commons.workbench.search.SearchHistoryPopupDialog {
    private static org.eclipse.mylyn.commons.ui.GradientColors colors;

    private org.eclipse.jface.resource.LocalResourceManager resourceManager;

    public TaskListSearchHistoryPopupDialog(org.eclipse.swt.widgets.Shell parent, int side) {
        super(parent, side);
    }

    @java.lang.Override
    protected void createAdditionalSearchRegion(org.eclipse.swt.widgets.Composite composite) {
        if (!org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil.supportsTaskSearch()) {
            return;
        }
        resourceManager = new org.eclipse.jface.resource.LocalResourceManager(org.eclipse.jface.resource.JFaceResources.getResources());
        org.eclipse.mylyn.internal.tasks.ui.views.TaskListSearchHistoryPopupDialog.colors = new org.eclipse.mylyn.commons.ui.GradientColors(composite.getDisplay(), resourceManager);
        org.eclipse.mylyn.commons.ui.GradientCanvas gradient = new org.eclipse.mylyn.commons.ui.GradientCanvas(composite, org.eclipse.swt.SWT.NONE);
        gradient.setBackgroundGradient(new org.eclipse.swt.graphics.Color[]{ org.eclipse.mylyn.internal.tasks.ui.views.TaskListSearchHistoryPopupDialog.colors.getGradientBegin(), org.eclipse.mylyn.internal.tasks.ui.views.TaskListSearchHistoryPopupDialog.colors.getGradientEnd() }, new int[]{ 100 }, true);
        org.eclipse.swt.layout.GridLayout headLayout = new org.eclipse.swt.layout.GridLayout();
        headLayout.marginHeight = 5;
        headLayout.marginWidth = 5;
        headLayout.horizontalSpacing = 0;
        headLayout.verticalSpacing = 0;
        gradient.setLayout(headLayout);
        gradient.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.HORIZONTAL_ALIGN_FILL | org.eclipse.swt.layout.GridData.GRAB_HORIZONTAL));
        gradient.setSeparatorVisible(true);
        gradient.setSeparatorAlignment(org.eclipse.swt.SWT.TOP);
        org.eclipse.swt.widgets.Composite editContainer = new org.eclipse.swt.widgets.Composite(gradient, org.eclipse.swt.SWT.NONE);
        org.eclipse.swt.layout.GridLayout editLayout = new org.eclipse.swt.layout.GridLayout();
        editLayout.marginHeight = 0;
        editLayout.marginWidth = 0;
        editContainer.setLayout(editLayout);
        editContainer.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.CENTER, org.eclipse.swt.layout.GridData.CENTER, true, true));
        org.eclipse.ui.forms.widgets.ImageHyperlink advancedSearchButton = new org.eclipse.ui.forms.widgets.ImageHyperlink(editContainer, org.eclipse.swt.SWT.NONE);
        advancedSearchButton.setUnderlined(true);
        advancedSearchButton.setForeground(org.eclipse.mylyn.commons.ui.compatibility.CommonColors.HYPERLINK_WIDGET);
        advancedSearchButton.setText(TaskListFilteredTree.LABEL_SEARCH);
        advancedSearchButton.addHyperlinkListener(new org.eclipse.ui.forms.events.HyperlinkAdapter() {
            @java.lang.Override
            public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil.openSearchDialog(org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow());
            }
        });
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.CENTER, org.eclipse.swt.SWT.BEGINNING).applyTo(advancedSearchButton);
    }
}