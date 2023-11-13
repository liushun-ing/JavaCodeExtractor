/**
 * *****************************************************************************
 * Copyright (c) 2004, 2010 Tasktop Technologies and others.
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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.AbstractDuplicateDetector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
/**
 *
 * @author Steffen Pingel
 */
public class TaskEditorDescriptionPart extends org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorRichTextPart {
    public TaskEditorDescriptionPart() {
        setPartName(Messages.TaskEditorDescriptionPart_Description);
    }

    private void addDuplicateDetection(org.eclipse.swt.widgets.Composite composite, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        java.util.List<org.eclipse.mylyn.tasks.core.AbstractDuplicateDetector> allCollectors = new java.util.ArrayList<org.eclipse.mylyn.tasks.core.AbstractDuplicateDetector>();
        if (getDuplicateSearchCollectorsList() != null) {
            allCollectors.addAll(getDuplicateSearchCollectorsList());
        }
        if (!allCollectors.isEmpty()) {
            int style = org.eclipse.ui.forms.widgets.ExpandableComposite.TWISTIE | org.eclipse.ui.forms.widgets.ExpandableComposite.SHORT_TITLE_BAR;
            if (getTaskData().isNew()) {
                style |= org.eclipse.ui.forms.widgets.ExpandableComposite.EXPANDED;
            }
            org.eclipse.ui.forms.widgets.Section duplicatesSection = toolkit.createSection(composite, style);
            duplicatesSection.setText(Messages.TaskEditorDescriptionPart_Duplicate_Detection);
            duplicatesSection.setLayout(new org.eclipse.swt.layout.GridLayout());
            org.eclipse.jface.layout.GridDataFactory.fillDefaults().indent(org.eclipse.swt.SWT.DEFAULT, 15).applyTo(duplicatesSection);
            org.eclipse.swt.widgets.Composite relatedBugsComposite = toolkit.createComposite(duplicatesSection);
            relatedBugsComposite.setLayout(new org.eclipse.swt.layout.GridLayout(4, false));
            relatedBugsComposite.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.HORIZONTAL_ALIGN_FILL));
            duplicatesSection.setClient(relatedBugsComposite);
            org.eclipse.swt.widgets.Label duplicateDetectorLabel = new org.eclipse.swt.widgets.Label(relatedBugsComposite, org.eclipse.swt.SWT.LEFT);
            duplicateDetectorLabel.setText(Messages.TaskEditorDescriptionPart_Detector);
            final org.eclipse.swt.custom.CCombo duplicateDetectorChooser = new org.eclipse.swt.custom.CCombo(relatedBugsComposite, org.eclipse.swt.SWT.FLAT | org.eclipse.swt.SWT.READ_ONLY);
            toolkit.adapt(duplicateDetectorChooser, false, false);
            duplicateDetectorChooser.setData(org.eclipse.ui.forms.widgets.FormToolkit.KEY_DRAW_BORDER, org.eclipse.ui.forms.widgets.FormToolkit.TREE_BORDER);
            duplicateDetectorChooser.setFont(org.eclipse.mylyn.internal.tasks.ui.editors.TEXT_FONT);
            duplicateDetectorChooser.setLayoutData(org.eclipse.jface.layout.GridDataFactory.swtDefaults().hint(150, org.eclipse.swt.SWT.DEFAULT).create());
            java.util.Collections.sort(allCollectors, new java.util.Comparator<org.eclipse.mylyn.tasks.core.AbstractDuplicateDetector>() {
                public int compare(org.eclipse.mylyn.tasks.core.AbstractDuplicateDetector c1, org.eclipse.mylyn.tasks.core.AbstractDuplicateDetector c2) {
                    return c1.getName().compareToIgnoreCase(c2.getName());
                }
            });
            for (org.eclipse.mylyn.tasks.core.AbstractDuplicateDetector detector : allCollectors) {
                duplicateDetectorChooser.add(detector.getName());
            }
            duplicateDetectorChooser.select(0);
            duplicateDetectorChooser.setEnabled(true);
            duplicateDetectorChooser.setData(allCollectors);
            if (allCollectors.size() > 0) {
                org.eclipse.swt.widgets.Button searchForDuplicates = toolkit.createButton(relatedBugsComposite, Messages.TaskEditorDescriptionPart_Search, org.eclipse.swt.SWT.NONE);
                org.eclipse.swt.layout.GridData searchDuplicatesButtonData = new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.HORIZONTAL_ALIGN_BEGINNING);
                searchForDuplicates.setLayoutData(searchDuplicatesButtonData);
                searchForDuplicates.addListener(org.eclipse.swt.SWT.Selection, new org.eclipse.swt.widgets.Listener() {
                    public void handleEvent(org.eclipse.swt.widgets.Event e) {
                        java.lang.String selectedDetector = duplicateDetectorChooser.getItem(duplicateDetectorChooser.getSelectionIndex());
                        searchForDuplicates(selectedDetector);
                    }
                });
            }
            toolkit.paintBordersFor(relatedBugsComposite);
        }
    }

    @java.lang.Override
    public void createControl(org.eclipse.swt.widgets.Composite parent, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        if (getAttribute() == null) {
            return;
        }
        super.createControl(parent, toolkit);
        if (org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil.supportsTaskSearch()) {
            addDuplicateDetection(getComposite(), toolkit);
        }
        getEditor().enableAutoTogglePreview();
        if (!getTaskData().isNew()) {
            getEditor().showPreview();
        }
    }

    @java.lang.Override
    protected void fillToolBar(org.eclipse.jface.action.ToolBarManager toolBar) {
        if (!getTaskData().isNew()) {
            org.eclipse.mylyn.internal.tasks.ui.editors.AbstractReplyToCommentAction replyAction = new org.eclipse.mylyn.internal.tasks.ui.editors.AbstractReplyToCommentAction(getTaskEditorPage(), null) {
                @java.lang.Override
                protected java.lang.String getReplyText() {
                    return getEditor().getValue();
                }
            };
            replyAction.setImageDescriptor(org.eclipse.mylyn.tasks.ui.TasksUiImages.COMMENT_REPLY_SMALL);
            toolBar.add(replyAction);
        }
        super.fillToolBar(toolBar);
    }

    protected org.eclipse.mylyn.tasks.core.IRepositoryQuery getDuplicateQuery(java.lang.String name) throws org.eclipse.core.runtime.CoreException {
        java.lang.String duplicateDetectorName = (name.equals("default")) ? "Stack Trace" : name;// $NON-NLS-1$ //$NON-NLS-2$

        for (org.eclipse.mylyn.tasks.core.AbstractDuplicateDetector detector : getDuplicateSearchCollectorsList()) {
            if (detector.getName().equals(duplicateDetectorName)) {
                return detector.getDuplicatesQuery(getTaskEditorPage().getTaskRepository(), getTaskData());
            }
        }
        return null;
    }

    protected java.util.Set<org.eclipse.mylyn.tasks.core.AbstractDuplicateDetector> getDuplicateSearchCollectorsList() {
        java.util.Set<org.eclipse.mylyn.tasks.core.AbstractDuplicateDetector> duplicateDetectors = new java.util.HashSet<org.eclipse.mylyn.tasks.core.AbstractDuplicateDetector>();
        for (org.eclipse.mylyn.tasks.core.AbstractDuplicateDetector detector : org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getDuplicateSearchCollectorsList()) {
            if (isValidDuplicateDetector(detector)) {
                duplicateDetectors.add(detector);
            }
        }
        return duplicateDetectors;
    }

    @java.lang.Override
    public void initialize(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage taskEditorPage) {
        super.initialize(taskEditorPage);
        setAttribute(getModel().getTaskData().getRoot().getMappedAttribute(org.eclipse.mylyn.tasks.core.data.TaskAttribute.DESCRIPTION));
    }

    private boolean isValidDuplicateDetector(org.eclipse.mylyn.tasks.core.AbstractDuplicateDetector detector) {
        return ((detector.getConnectorKind() == null) || detector.getConnectorKind().equals(getTaskEditorPage().getConnectorKind()))// 
         && detector.canQuery(getTaskData());
    }

    public void searchForDuplicates(java.lang.String duplicateDetectorName) {
        try {
            org.eclipse.mylyn.tasks.core.IRepositoryQuery duplicatesQuery = getDuplicateQuery(duplicateDetectorName);
            if (duplicatesQuery != null) {
                org.eclipse.mylyn.internal.tasks.ui.search.SearchUtil.runSearchQuery(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList(), getTaskEditorPage().getTaskRepository(), duplicatesQuery);
            } else {
                org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.displayStatus(Messages.TaskEditorDescriptionPart_Duplicate_Detection_Failed, new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, Messages.TaskEditorDescriptionPart_The_duplicate_detector_did_not_return_a_valid_query));
            }
        } catch (org.eclipse.core.runtime.CoreException e) {
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.displayStatus(Messages.TaskEditorDescriptionPart_Duplicate_Detection_Failed, e.getStatus());
        }
    }
}