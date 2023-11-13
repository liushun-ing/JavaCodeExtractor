/**
 * *****************************************************************************
 * Copyright (c) 2004, 2012 Tasktop Technologies and others.
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
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory;
import org.eclipse.mylyn.internal.tasks.core.ITaskList;
import org.eclipse.mylyn.internal.tasks.core.TaskCategory;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode;
import org.eclipse.mylyn.internal.tasks.ui.util.AbstractRetrieveTitleFromUrlJob;
import org.eclipse.mylyn.internal.tasks.ui.util.TaskContainerComparator;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.Section;
/**
 *
 * @author Steffen Pingel
 */
public class AttributePart extends org.eclipse.mylyn.internal.tasks.ui.editors.AbstractLocalEditorPart {
    private org.eclipse.ui.forms.widgets.ImageHyperlink fetchUrlLink;

    private org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor urlEditor;

    private org.eclipse.swt.custom.CCombo categoryChooser;

    protected org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory category;

    private org.eclipse.swt.widgets.Label categoryLabel;

    private java.util.List<org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory> categories;

    public AttributePart() {
        super(Messages.TaskPlanningEditor_Attributes);
    }

    @java.lang.Override
    public void commit(boolean onSave) {
        if (category != null) {
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().addTask(getTask(), category);
            category = null;
            clearState(categoryChooser);
        }
        getTask().setUrl(urlEditor.getText());
        clearState(urlEditor.getControl());
        super.commit(onSave);
    }

    @java.lang.Override
    public org.eclipse.swt.widgets.Control createControl(org.eclipse.swt.widgets.Composite parent, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        int style = org.eclipse.ui.forms.widgets.ExpandableComposite.TITLE_BAR | org.eclipse.ui.forms.widgets.ExpandableComposite.TWISTIE;
        // | ExpandableComposite.LEFT_TEXT_CLIENT_ALIGNMENT;
        if ((getTask().getUrl() != null) && (getTask().getUrl().length() > 0)) {
            style |= org.eclipse.ui.forms.widgets.ExpandableComposite.EXPANDED;
        }
        org.eclipse.ui.forms.widgets.Section section = createSection(parent, toolkit, style);
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().grab(true, false).applyTo(section);
        section.setText(Messages.TaskPlanningEditor_Attributes);
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().grab(true, false).applyTo(section);
        createSectionClient(section, toolkit);
        setSection(toolkit, section);
        org.eclipse.swt.widgets.Composite composite = toolkit.createComposite(section);
        org.eclipse.swt.layout.GridLayout layout = org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.createSectionClientLayout();
        layout.numColumns = 4;
        composite.setLayout(layout);
        org.eclipse.swt.widgets.Label label = toolkit.createLabel(composite, Messages.AttributePart_Category_);
        label.setForeground(toolkit.getColors().getColor(org.eclipse.ui.forms.IFormColors.TITLE));
        createCategoryChooser(composite, toolkit);
        // url
        label = toolkit.createLabel(composite, Messages.TaskPlanningEditor_URL);
        label.setForeground(toolkit.getColors().getColor(org.eclipse.ui.forms.IFormColors.TITLE));
        org.eclipse.jface.layout.GridDataFactory.defaultsFor(label).indent(20, 0).applyTo(label);
        org.eclipse.swt.widgets.Composite urlComposite = toolkit.createComposite(composite);
        org.eclipse.swt.layout.GridLayout urlLayout = new org.eclipse.swt.layout.GridLayout(2, false);
        urlLayout.verticalSpacing = 0;
        urlLayout.marginWidth = 1;
        urlComposite.setLayout(urlLayout);
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().grab(true, false).hint(EditorUtil.MAXIMUM_WIDTH, org.eclipse.swt.SWT.DEFAULT).applyTo(urlComposite);
        urlEditor = new org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor(getRepository(), org.eclipse.swt.SWT.FLAT | org.eclipse.swt.SWT.SINGLE, null, null, getTask()) {
            @java.lang.Override
            protected void valueChanged(java.lang.String value) {
                updateButtons();
                markDirty(urlEditor.getControl());
            }
        };
        urlEditor.setMode(org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode.URL);
        urlEditor.createControl(urlComposite, toolkit);
        urlEditor.getControl().setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_HORIZONTAL));
        urlEditor.getViewer().getControl().setMenu(parent.getMenu());
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().grab(true, false).applyTo(urlEditor.getControl());
        fetchUrlLink = toolkit.createImageHyperlink(urlComposite, org.eclipse.swt.SWT.NONE);
        fetchUrlLink.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.TASK_RETRIEVE));
        fetchUrlLink.setToolTipText(Messages.TaskPlanningEditor_Retrieve_task_description_from_URL);
        fetchUrlLink.addHyperlinkListener(new org.eclipse.ui.forms.events.HyperlinkAdapter() {
            @java.lang.Override
            public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                featchUrl(urlEditor.getText());
            }
        });
        toolkit.paintBordersFor(urlComposite);
        toolkit.paintBordersFor(composite);
        section.setClient(composite);
        return section;
    }

    private void createSectionClient(final org.eclipse.ui.forms.widgets.Section section, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        if (section.getTextClient() == null) {
            final org.eclipse.swt.widgets.Composite textClient = toolkit.createComposite(section);
            textClient.setBackground(null);
            org.eclipse.swt.layout.RowLayout rowLayout = new org.eclipse.swt.layout.RowLayout();
            rowLayout.center = true;
            rowLayout.marginLeft = 20;
            rowLayout.marginTop = 1;
            rowLayout.marginBottom = 1;
            textClient.setLayout(rowLayout);
            org.eclipse.swt.widgets.Label label = toolkit.createLabel(textClient, Messages.AttributePart_Category_);
            label.setForeground(toolkit.getColors().getColor(org.eclipse.ui.forms.IFormColors.TITLE));
            label.setBackground(null);
            categoryLabel = toolkit.createLabel(textClient, "");// $NON-NLS-1$

            categoryLabel.setForeground(toolkit.getColors().getColor(org.eclipse.ui.forms.IFormColors.TITLE));
            categoryLabel.setBackground(null);
            toolkit.paintBordersFor(textClient);
            section.setTextClient(textClient);
            section.addExpansionListener(new org.eclipse.ui.forms.events.ExpansionAdapter() {
                @java.lang.Override
                public void expansionStateChanging(org.eclipse.ui.forms.events.ExpansionEvent e) {
                    textClient.setVisible(!e.getState());
                }
            });
            textClient.setVisible(!section.isExpanded());
        }
    }

    /**
     * Set the task summary to the page title from the specified url.
     */
    private void featchUrl(final java.lang.String url) {
        org.eclipse.mylyn.internal.tasks.ui.util.AbstractRetrieveTitleFromUrlJob job = new org.eclipse.mylyn.internal.tasks.ui.util.AbstractRetrieveTitleFromUrlJob(urlEditor.getText()) {
            @java.lang.Override
            protected void titleRetrieved(java.lang.String pageTitle) {
                org.eclipse.ui.forms.IFormPart[] parts = getManagedForm().getParts();
                for (org.eclipse.ui.forms.IFormPart part : parts) {
                    if (part instanceof org.eclipse.mylyn.internal.tasks.ui.editors.SummaryPart) {
                        ((org.eclipse.mylyn.internal.tasks.ui.editors.SummaryPart) (part)).setSummary(pageTitle);
                    }
                }
            }
        };
        job.schedule();
    }

    private void updateButtons() {
        java.lang.String value = urlEditor.getText();
        fetchUrlLink.setEnabled(value.startsWith("http://") || value.startsWith("https://"));// $NON-NLS-1$ //$NON-NLS-2$

    }

    @java.lang.Override
    public void refresh(boolean discardChanges) {
        if (shouldRefresh(categoryChooser, discardChanges)) {
            org.eclipse.mylyn.internal.tasks.core.ITaskList taskList = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList();
            categories = new java.util.ArrayList<org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory>(taskList.getCategories());
            java.util.Collections.sort(categories, new org.eclipse.mylyn.internal.tasks.ui.util.TaskContainerComparator());
            org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory selectedCategory = category;
            if (selectedCategory == null) {
                selectedCategory = org.eclipse.mylyn.internal.tasks.core.TaskCategory.getParentTaskCategory(getTask());
            }
            categoryChooser.removeAll();
            int selectedIndex = 0;
            for (int i = 0; i < categories.size(); i++) {
                org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory category = categories.get(i);
                categoryChooser.add(category.getSummary());
                if (category.equals(selectedCategory)) {
                    selectedIndex = i;
                }
            }
            categoryChooser.select(selectedIndex);
            updateCategoryLabel();
        }
        if (shouldRefresh(urlEditor.getControl(), discardChanges)) {
            java.lang.String url = getTask().getUrl();
            urlEditor.setText(url != null ? url : "");// $NON-NLS-1$

        }
        updateButtons();
    }

    private void updateCategoryLabel() {
        if (category == null) {
            org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory parentTaskCategory = org.eclipse.mylyn.internal.tasks.core.TaskCategory.getParentTaskCategory(getTask());
            categoryLabel.setText(parentTaskCategory != null ? parentTaskCategory.getSummary() : "");// $NON-NLS-1$

        } else {
            categoryLabel.setText(category.getSummary());
        }
        if (!getSection().isExpanded()) {
            getSection().layout(true, true);
        }
    }

    private void createCategoryChooser(org.eclipse.swt.widgets.Composite buttonComposite, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        categoryChooser = new org.eclipse.swt.custom.CCombo(buttonComposite, org.eclipse.swt.SWT.FLAT | org.eclipse.swt.SWT.READ_ONLY);
        categoryChooser.setData(org.eclipse.ui.forms.widgets.FormToolkit.KEY_DRAW_BORDER, org.eclipse.ui.forms.widgets.FormToolkit.TREE_BORDER);
        toolkit.adapt(categoryChooser, false, false);
        categoryChooser.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent event) {
                if (categoryChooser.getSelectionIndex() != (-1)) {
                    category = categories.get(categoryChooser.getSelectionIndex());
                    updateCategoryLabel();
                    markDirty(categoryChooser);
                }
            }
        });
    }
}