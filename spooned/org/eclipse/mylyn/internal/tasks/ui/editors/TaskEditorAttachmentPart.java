/**
 * *****************************************************************************
 * Copyright (c) 2004, 2014 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Jeff Pound - attachment support
 *     Frank Becker - improvements for bug 204051
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.editors;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.mylyn.commons.core.CoreUtil;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.commons.ui.TableSorter;
import org.eclipse.mylyn.commons.ui.TableViewerSupport;
import org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil;
import org.eclipse.mylyn.internal.tasks.core.TaskAttachment;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.commands.OpenTaskAttachmentHandler;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiMenus;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskKeyComparator;
import org.eclipse.mylyn.internal.tasks.ui.wizards.TaskAttachmentWizard.Mode;
import org.eclipse.mylyn.tasks.core.ITaskAttachment;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
/**
 *
 * @author Mik Kersten
 * @author Rob Elves
 * @author Steffen Pingel
 */
public class TaskEditorAttachmentPart extends org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart {
    private class AttachmentTableSorter extends org.eclipse.mylyn.commons.ui.TableSorter {
        org.eclipse.mylyn.internal.tasks.ui.views.TaskKeyComparator keyComparator = new org.eclipse.mylyn.internal.tasks.ui.views.TaskKeyComparator();

        @java.lang.Override
        public int compare(org.eclipse.jface.viewers.TableViewer viewer, java.lang.Object e1, java.lang.Object e2, int columnIndex) {
            org.eclipse.mylyn.tasks.core.ITaskAttachment attachment1 = ((org.eclipse.mylyn.tasks.core.ITaskAttachment) (e1));
            org.eclipse.mylyn.tasks.core.ITaskAttachment attachment2 = ((org.eclipse.mylyn.tasks.core.ITaskAttachment) (e2));
            switch (columnIndex) {
                case 0 :
                    return org.eclipse.mylyn.commons.core.CoreUtil.compare(attachment1.getFileName(), attachment2.getFileName());
                case 1 :
                    java.lang.String description1 = attachment1.getDescription();
                    java.lang.String description2 = attachment2.getDescription();
                    return org.eclipse.mylyn.commons.core.CoreUtil.compare(description1, description2);
                case 2 :
                    return org.eclipse.mylyn.commons.core.CoreUtil.compare(attachment1.getLength(), attachment2.getLength());
                case 3 :
                    java.lang.String author1 = (attachment1.getAuthor() != null) ? attachment1.getAuthor().toString() : null;
                    java.lang.String author2 = (attachment2.getAuthor() != null) ? attachment2.getAuthor().toString() : null;
                    return org.eclipse.mylyn.commons.core.CoreUtil.compare(author1, author2);
                case 4 :
                    return org.eclipse.mylyn.commons.core.CoreUtil.compare(attachment1.getCreationDate(), attachment2.getCreationDate());
                case 5 :
                    java.lang.String key1 = org.eclipse.mylyn.internal.tasks.ui.editors.AttachmentTableLabelProvider.getAttachmentId(attachment1);
                    java.lang.String key2 = org.eclipse.mylyn.internal.tasks.ui.editors.AttachmentTableLabelProvider.getAttachmentId(attachment2);
                    return keyComparator.compare2(key1, key2);
            }
            return super.compare(viewer, e1, e2, columnIndex);
        }
    }

    private class AttachmentTableFilter extends org.eclipse.jface.viewers.ViewerFilter {
        private boolean filterDeprecatedEnabled;

        public boolean isFilterDeprecatedEnabled() {
            return filterDeprecatedEnabled;
        }

        public void setFilterDeprecatedEnabled(boolean filterDeprecatedEnabled) {
            this.filterDeprecatedEnabled = filterDeprecatedEnabled;
        }

        @java.lang.Override
        public boolean select(org.eclipse.jface.viewers.Viewer viewer, java.lang.Object parentElement, java.lang.Object element) {
            if (filterDeprecatedEnabled) {
                if (element instanceof org.eclipse.mylyn.tasks.core.ITaskAttachment) {
                    return !((org.eclipse.mylyn.tasks.core.ITaskAttachment) (element)).isDeprecated();
                }
            }
            return true;
        }
    }

    private static final java.lang.String PREF_FILTER_DEPRECATED = "org.eclipse.mylyn.tasks.ui.editor.attachments.filter.deprecated";// $NON-NLS-1$


    private static final java.lang.String ID_POPUP_MENU = "org.eclipse.mylyn.tasks.ui.editor.menu.attachments";// $NON-NLS-1$


    private final java.lang.String[] attachmentsColumns = new java.lang.String[]{ Messages.TaskEditorAttachmentPart_Name, Messages.TaskEditorAttachmentPart_Description, Messages.TaskEditorAttachmentPart_Size, Messages.TaskEditorAttachmentPart_Creator, Messages.TaskEditorAttachmentPart_Created, Messages.TaskEditorAttachmentPart_ID };

    private final int[] attachmentsColumnWidths = new int[]{ 130, 150, 70, 100, 100, 0 };

    private java.util.List<org.eclipse.mylyn.tasks.core.data.TaskAttribute> attachmentAttributes;

    private boolean hasIncoming;

    private org.eclipse.jface.action.MenuManager menuManager;

    private org.eclipse.swt.widgets.Composite attachmentsComposite;

    private org.eclipse.swt.widgets.Table attachmentsTable;

    private org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorAttachmentPart.AttachmentTableFilter tableFilter;

    private org.eclipse.jface.viewers.TableViewer attachmentsViewer;

    private java.util.List<org.eclipse.mylyn.tasks.core.ITaskAttachment> attachmentList;

    private org.eclipse.ui.forms.widgets.Section section;

    private int nonDeprecatedCount;

    private org.eclipse.jface.action.Action filterDeprecatedAttachmentsAction;

    public TaskEditorAttachmentPart() {
        setPartName(Messages.TaskEditorAttachmentPart_Attachments);
    }

    private void createAttachmentTable(org.eclipse.ui.forms.widgets.FormToolkit toolkit, final org.eclipse.swt.widgets.Composite attachmentsComposite) {
        attachmentsTable = toolkit.createTable(attachmentsComposite, org.eclipse.swt.SWT.MULTI | org.eclipse.swt.SWT.FULL_SELECTION);
        attachmentsTable.setLinesVisible(true);
        attachmentsTable.setHeaderVisible(true);
        attachmentsTable.setLayout(new org.eclipse.swt.layout.GridLayout());
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.FILL).grab(true, false).hint(500, org.eclipse.swt.SWT.DEFAULT).applyTo(attachmentsTable);
        attachmentsTable.setData(org.eclipse.ui.forms.widgets.FormToolkit.KEY_DRAW_BORDER, org.eclipse.ui.forms.widgets.FormToolkit.TREE_BORDER);
        for (int i = 0; i < attachmentsColumns.length; i++) {
            org.eclipse.swt.widgets.TableColumn column = new org.eclipse.swt.widgets.TableColumn(attachmentsTable, org.eclipse.swt.SWT.LEFT, i);
            column.setText(attachmentsColumns[i]);
            column.setWidth(attachmentsColumnWidths[i]);
            column.setMoveable(true);
            if (i == 4) {
                attachmentsTable.setSortColumn(column);
                attachmentsTable.setSortDirection(org.eclipse.swt.SWT.DOWN);
            }
        }
        // size column
        attachmentsTable.getColumn(2).setAlignment(org.eclipse.swt.SWT.RIGHT);
        attachmentsViewer = new org.eclipse.jface.viewers.TableViewer(attachmentsTable);
        attachmentsViewer.setUseHashlookup(true);
        attachmentsViewer.setColumnProperties(attachmentsColumns);
        org.eclipse.jface.viewers.ColumnViewerToolTipSupport.enableFor(attachmentsViewer, org.eclipse.jface.window.ToolTip.NO_RECREATE);
        attachmentsViewer.setSorter(new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorAttachmentPart.AttachmentTableSorter());
        attachmentsViewer.setContentProvider(new org.eclipse.jface.viewers.ArrayContentProvider());
        attachmentsViewer.setLabelProvider(new org.eclipse.mylyn.internal.tasks.ui.editors.AttachmentTableLabelProvider(getModel(), getTaskEditorPage().getAttributeEditorToolkit()));
        attachmentsViewer.addOpenListener(new org.eclipse.jface.viewers.IOpenListener() {
            public void open(org.eclipse.jface.viewers.OpenEvent event) {
                openAttachments(event);
            }
        });
        attachmentsViewer.addSelectionChangedListener(getTaskEditorPage());
        attachmentsViewer.setInput(attachmentList.toArray());
        menuManager = new org.eclipse.jface.action.MenuManager();
        menuManager.setRemoveAllWhenShown(true);
        menuManager.addMenuListener(new org.eclipse.jface.action.IMenuListener() {
            public void menuAboutToShow(org.eclipse.jface.action.IMenuManager manager) {
                org.eclipse.mylyn.internal.tasks.ui.util.TasksUiMenus.fillTaskAttachmentMenu(manager);
            }
        });
        getTaskEditorPage().getEditorSite().registerContextMenu(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorAttachmentPart.ID_POPUP_MENU, menuManager, attachmentsViewer, true);
        org.eclipse.swt.widgets.Menu menu = menuManager.createContextMenu(attachmentsTable);
        attachmentsTable.setMenu(menu);
        attachmentsViewer.addFilter(tableFilter);
        new org.eclipse.mylyn.commons.ui.TableViewerSupport(attachmentsViewer, getStateFile());
    }

    private java.io.File getStateFile() {
        org.eclipse.core.runtime.IPath stateLocation = org.eclipse.core.runtime.Platform.getStateLocation(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getBundle());
        return stateLocation.append("TaskEditorAttachmentPart.xml").toFile();// $NON-NLS-1$

    }

    private void createButtons(org.eclipse.swt.widgets.Composite attachmentsComposite, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        final org.eclipse.swt.widgets.Composite attachmentControlsComposite = toolkit.createComposite(attachmentsComposite);
        attachmentControlsComposite.setLayout(new org.eclipse.swt.layout.GridLayout(2, false));
        attachmentControlsComposite.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.BEGINNING));
        org.eclipse.swt.widgets.Button attachFileButton = toolkit.createButton(attachmentControlsComposite, Messages.TaskEditorAttachmentPart_Attach_, org.eclipse.swt.SWT.PUSH);
        attachFileButton.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.FILE_PLAIN));
        attachFileButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.openNewAttachmentWizard(getTaskEditorPage(), org.eclipse.mylyn.internal.tasks.ui.wizards.TaskAttachmentWizard.Mode.DEFAULT, null);
            }
        });
        getTaskEditorPage().registerDefaultDropListener(attachFileButton);
        org.eclipse.swt.widgets.Button attachScreenshotButton = toolkit.createButton(attachmentControlsComposite, Messages.TaskEditorAttachmentPart_Attach__Screenshot, org.eclipse.swt.SWT.PUSH);
        attachScreenshotButton.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.IMAGE_CAPTURE));
        attachScreenshotButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.openNewAttachmentWizard(getTaskEditorPage(), org.eclipse.mylyn.internal.tasks.ui.wizards.TaskAttachmentWizard.Mode.SCREENSHOT, null);
            }
        });
        getTaskEditorPage().registerDefaultDropListener(attachScreenshotButton);
    }

    @java.lang.Override
    public void createControl(org.eclipse.swt.widgets.Composite parent, final org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        initialize();
        section = createSection(parent, toolkit, hasIncoming);
        updateSectionTitle();
        if (hasIncoming) {
            expandSection(toolkit, section);
        } else {
            section.addExpansionListener(new org.eclipse.ui.forms.events.ExpansionAdapter() {
                @java.lang.Override
                public void expansionStateChanged(org.eclipse.ui.forms.events.ExpansionEvent event) {
                    if (attachmentsComposite == null) {
                        expandSection(toolkit, section);
                        getTaskEditorPage().reflow();
                    }
                }
            });
        }
        setSection(toolkit, section);
    }

    private void expandSection(org.eclipse.ui.forms.widgets.FormToolkit toolkit, org.eclipse.ui.forms.widgets.Section section) {
        attachmentsComposite = toolkit.createComposite(section);
        attachmentsComposite.setLayout(org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.createSectionClientLayout());
        attachmentsComposite.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_BOTH));
        getTaskEditorPage().registerDefaultDropListener(section);
        if (attachmentAttributes.size() > 0) {
            createAttachmentTable(toolkit, attachmentsComposite);
        } else {
            org.eclipse.swt.widgets.Label label = toolkit.createLabel(attachmentsComposite, Messages.TaskEditorAttachmentPart_No_attachments);
            getTaskEditorPage().registerDefaultDropListener(label);
        }
        createButtons(attachmentsComposite, toolkit);
        toolkit.paintBordersFor(attachmentsComposite);
        section.setClient(attachmentsComposite);
    }

    @java.lang.Override
    public void dispose() {
        if (menuManager != null) {
            menuManager.dispose();
        }
        super.dispose();
    }

    private void initialize() {
        attachmentAttributes = getTaskData().getAttributeMapper().getAttributesByType(getTaskData(), org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_ATTACHMENT);
        attachmentList = new java.util.ArrayList<org.eclipse.mylyn.tasks.core.ITaskAttachment>(attachmentAttributes.size());
        for (org.eclipse.mylyn.tasks.core.data.TaskAttribute attribute : attachmentAttributes) {
            if (getModel().hasIncomingChanges(attribute)) {
                hasIncoming = true;
            }
            org.eclipse.mylyn.internal.tasks.core.TaskAttachment taskAttachment = new org.eclipse.mylyn.internal.tasks.core.TaskAttachment(getModel().getTaskRepository(), getModel().getTask(), attribute);
            getTaskData().getAttributeMapper().updateTaskAttachment(taskAttachment, attribute);
            attachmentList.add(taskAttachment);
            if (!taskAttachment.isDeprecated()) {
                nonDeprecatedCount++;
            }
        }
        tableFilter = new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorAttachmentPart.AttachmentTableFilter();
    }

    @java.lang.Override
    protected void fillToolBar(org.eclipse.jface.action.ToolBarManager toolBarManager) {
        filterDeprecatedAttachmentsAction = new org.eclipse.jface.action.Action("", org.eclipse.jface.action.IAction.AS_CHECK_BOX) {
            // $NON-NLS-1$
            @java.lang.Override
            public void run() {
                org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().setValue(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorAttachmentPart.PREF_FILTER_DEPRECATED, isChecked());
                filterDeprecated(isChecked());
            }
        };
        filterDeprecatedAttachmentsAction.setImageDescriptor(org.eclipse.mylyn.tasks.ui.TasksUiImages.FILTER_OBSOLETE_SMALL);
        filterDeprecatedAttachmentsAction.setToolTipText(Messages.TaskEditorAttachmentPart_Hide_Obsolete_Tooltip);
        if ((nonDeprecatedCount > 0) && (nonDeprecatedCount < attachmentAttributes.size())) {
            filterDeprecated(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getPreferenceStore().getBoolean(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorAttachmentPart.PREF_FILTER_DEPRECATED));
        } else {
            // do not allow filtering if it would cause the table to be empty or no change
            filterDeprecatedAttachmentsAction.setEnabled(false);
        }
        toolBarManager.add(filterDeprecatedAttachmentsAction);
        org.eclipse.jface.action.Action attachFileAction = new org.eclipse.jface.action.Action() {
            @java.lang.Override
            public void run() {
                org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.openNewAttachmentWizard(getTaskEditorPage(), org.eclipse.mylyn.internal.tasks.ui.wizards.TaskAttachmentWizard.Mode.DEFAULT, null);
            }
        };
        attachFileAction.setToolTipText(Messages.TaskEditorAttachmentPart_Attach_);
        attachFileAction.setImageDescriptor(org.eclipse.mylyn.tasks.ui.TasksUiImages.FILE_NEW_SMALL);
        toolBarManager.add(attachFileAction);
        org.eclipse.jface.action.Action attachScreenshotAction = new org.eclipse.jface.action.Action() {
            @java.lang.Override
            public void run() {
                org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.openNewAttachmentWizard(getTaskEditorPage(), org.eclipse.mylyn.internal.tasks.ui.wizards.TaskAttachmentWizard.Mode.SCREENSHOT, null);
            }
        };
        attachScreenshotAction.setToolTipText(Messages.TaskEditorAttachmentPart_Attach__Screenshot);
        attachScreenshotAction.setImageDescriptor(org.eclipse.mylyn.tasks.ui.TasksUiImages.IMAGE_CAPTURE_SMALL);
        toolBarManager.add(attachScreenshotAction);
    }

    private void updateSectionTitle() {
        if (tableFilter.isFilterDeprecatedEnabled()) {
            section.setText(org.eclipse.osgi.util.NLS.bind(Messages.TaskEditorAttachmentPart_Attachment_Section_Title_X_of_Y, new java.lang.Object[]{ org.eclipse.jface.action.LegacyActionTools.escapeMnemonics(getPartName()), nonDeprecatedCount, attachmentAttributes.size() }));
        } else {
            section.setText(org.eclipse.osgi.util.NLS.bind(Messages.TaskEditorAttachmentPart_Attachment_Section_Title_X, org.eclipse.jface.action.LegacyActionTools.escapeMnemonics(getPartName()), attachmentAttributes.size()));
        }
    }

    protected void openAttachments(org.eclipse.jface.viewers.OpenEvent event) {
        java.util.List<org.eclipse.mylyn.tasks.core.ITaskAttachment> attachments = new java.util.ArrayList<org.eclipse.mylyn.tasks.core.ITaskAttachment>();
        org.eclipse.jface.viewers.StructuredSelection selection = ((org.eclipse.jface.viewers.StructuredSelection) (event.getSelection()));
        java.util.List<?> items = selection.toList();
        for (java.lang.Object item : items) {
            if (item instanceof org.eclipse.mylyn.tasks.core.ITaskAttachment) {
                attachments.add(((org.eclipse.mylyn.tasks.core.ITaskAttachment) (item)));
            }
        }
        if (attachments.isEmpty()) {
            return;
        }
        org.eclipse.ui.IWorkbenchPage page = getTaskEditorPage().getSite().getWorkbenchWindow().getActivePage();
        try {
            org.eclipse.mylyn.internal.tasks.ui.commands.OpenTaskAttachmentHandler.openAttachments(page, attachments);
        } catch (org.eclipse.core.runtime.OperationCanceledException e) {
            // canceled
        }
    }

    @java.lang.Override
    public boolean setFormInput(java.lang.Object input) {
        if (input instanceof java.lang.String) {
            java.lang.String text = ((java.lang.String) (input));
            if (attachmentAttributes != null) {
                for (org.eclipse.mylyn.tasks.core.ITaskAttachment attachment : attachmentList) {
                    if (text.equals(attachment.getTaskAttribute().getId())) {
                        org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil.setExpanded(((org.eclipse.ui.forms.widgets.ExpandableComposite) (getControl())), true);
                        return selectReveal(attachment);
                    }
                }
            }
        }
        return super.setFormInput(input);
    }

    private boolean selectReveal(org.eclipse.mylyn.tasks.core.ITaskAttachment attachment) {
        if ((attachment == null) || (attachmentsTable == null)) {
            return false;
        }
        if (tableFilter.isFilterDeprecatedEnabled() && attachment.isDeprecated()) {
            filterDeprecated(false);
        }
        attachmentsViewer.setSelection(new org.eclipse.jface.viewers.StructuredSelection(attachment));
        org.eclipse.ui.forms.IManagedForm mform = getManagedForm();
        org.eclipse.ui.forms.widgets.ScrolledForm form = mform.getForm();
        org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.focusOn(form, attachmentsTable);
        return true;
    }

    void filterDeprecated(boolean filter) {
        if (filterDeprecatedAttachmentsAction.isChecked() != filter) {
            filterDeprecatedAttachmentsAction.setChecked(filter);
        }
        tableFilter.setFilterDeprecatedEnabled(filter);
        if (attachmentsViewer != null) {
            attachmentsViewer.refresh();
            getTaskEditorPage().reflow();
        }
        updateSectionTitle();
    }
}