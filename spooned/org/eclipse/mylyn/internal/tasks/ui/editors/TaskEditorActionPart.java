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
 *     David Green - fix for bug 254806, bug 267135
 *     Benjamin Muskalla - fix for bug 310798
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.editors;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory;
import org.eclipse.mylyn.internal.tasks.core.ITaskList;
import org.eclipse.mylyn.internal.tasks.ui.util.TaskContainerComparator;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskOperation;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.TextChangeListener;
import org.eclipse.swt.custom.TextChangedEvent;
import org.eclipse.swt.custom.TextChangingEvent;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
/**
 *
 * @author Steffen Pingel
 */
public class TaskEditorActionPart extends org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart {
    private static final java.lang.String KEY_OPERATION = "operation";// $NON-NLS-1$


    public class SelectButtonListener implements org.eclipse.swt.events.ModifyListener , org.eclipse.swt.events.VerifyListener , org.eclipse.swt.events.SelectionListener , org.eclipse.swt.events.FocusListener , org.eclipse.swt.custom.TextChangeListener {
        private final org.eclipse.swt.widgets.Button button;

        public SelectButtonListener(org.eclipse.swt.widgets.Button button) {
            this.button = button;
        }

        public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
            selected();
        }

        public void verifyText(org.eclipse.swt.events.VerifyEvent e) {
            selected();
        }

        public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
            selected();
        }

        public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
            selected();
        }

        public void focusGained(org.eclipse.swt.events.FocusEvent event) {
            selected();
        }

        public void focusLost(org.eclipse.swt.events.FocusEvent e) {
        }

        public void textChanged(org.eclipse.swt.custom.TextChangedEvent event) {
            selected();
        }

        public void textSet(org.eclipse.swt.custom.TextChangedEvent event) {
            selected();
        }

        public void textChanging(org.eclipse.swt.custom.TextChangingEvent event) {
        }

        private void selected() {
            setSelectedRadionButton(button, true);
        }
    }

    private static final int DEFAULT_FIELD_WIDTH = 150;

    private static final int RADIO_OPTION_WIDTH = 120;

    private static final java.lang.String KEY_ASSOCIATED_EDITOR = "associatedEditor";// $NON-NLS-1$


    private java.util.List<org.eclipse.swt.widgets.Button> operationButtons;

    private org.eclipse.swt.widgets.Button submitButton;

    private org.eclipse.swt.widgets.Button attachContextButton;

    private org.eclipse.swt.widgets.Button addToCategory;

    private org.eclipse.swt.custom.CCombo categoryChooser;

    private org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory category;

    private org.eclipse.mylyn.tasks.core.data.TaskAttribute selectedOperationAttribute;

    public TaskEditorActionPart() {
        setPartName(Messages.TaskEditorActionPart_Actions);
    }

    protected void addAttachContextButton(org.eclipse.swt.widgets.Composite buttonComposite, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        attachContextButton = toolkit.createButton(buttonComposite, Messages.TaskEditorActionPart_Attach_Context, org.eclipse.swt.SWT.CHECK);
        attachContextButton.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.CONTEXT_ATTACH));
    }

    /**
     * Adds buttons to this composite. Subclasses can override this method to provide different/additional buttons.
     *
     * @param buttonComposite
     * 		Composite to add the buttons to.
     * @param toolkit
     */
    private void createActionButtons(org.eclipse.swt.widgets.Composite buttonComposite, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        // if (!getTaskEditorPage().needsSubmitButton()) {
        submitButton = toolkit.createButton(buttonComposite, Messages.TaskEditorActionPart_Submit, org.eclipse.swt.SWT.NONE);
        submitButton.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.tasks.ui.TasksUiImages.REPOSITORY_SUBMIT));
        submitButton.addListener(org.eclipse.swt.SWT.Selection, new org.eclipse.swt.widgets.Listener() {
            public void handleEvent(org.eclipse.swt.widgets.Event e) {
                getTaskEditorPage().doSubmit();
            }
        });
        org.eclipse.swt.graphics.Point minSize = submitButton.computeSize(org.eclipse.swt.SWT.DEFAULT, org.eclipse.swt.SWT.DEFAULT, true);
        org.eclipse.swt.layout.GridData submitButtonData = new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.HORIZONTAL_ALIGN_BEGINNING);
        submitButtonData.widthHint = java.lang.Math.max(100, minSize.x);
        submitButton.setLayoutData(submitButtonData);
        setSubmitEnabled(true);
        toolkit.createLabel(buttonComposite, "    ");// $NON-NLS-1$

        // }
        createAttachContextButton(buttonComposite, toolkit);
    }

    private void createAttachContextButton(org.eclipse.swt.widgets.Composite buttonComposite, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage taskEditorPage = getTaskEditorPage();
        org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = taskEditorPage.getConnector();
        org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentHandler taskAttachmentHandler = connector.getTaskAttachmentHandler();
        boolean canPostContent = false;
        if (taskAttachmentHandler != null) {
            org.eclipse.mylyn.tasks.core.TaskRepository taskRepository = taskEditorPage.getTaskRepository();
            org.eclipse.mylyn.tasks.core.ITask task = taskEditorPage.getTask();
            canPostContent = taskAttachmentHandler.canPostContent(taskRepository, task);
        }
        if ((!getTaskData().isNew()) && canPostContent) {
            addAttachContextButton(buttonComposite, toolkit);
        }
    }

    /**
     * Creates the button layout. This displays options and buttons at the bottom of the editor to allow actions to be
     * performed on the bug.
     *
     * @param toolkit
     */
    private void createCategoryChooser(org.eclipse.swt.widgets.Composite buttonComposite, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        addToCategory = getManagedForm().getToolkit().createButton(buttonComposite, Messages.TaskEditorActionPart_Add_to_Category, org.eclipse.swt.SWT.CHECK);
        categoryChooser = new org.eclipse.swt.custom.CCombo(buttonComposite, org.eclipse.swt.SWT.FLAT | org.eclipse.swt.SWT.READ_ONLY);
        categoryChooser.setData(org.eclipse.ui.forms.widgets.FormToolkit.KEY_DRAW_BORDER, org.eclipse.ui.forms.widgets.FormToolkit.TREE_BORDER);
        categoryChooser.setLayoutData(org.eclipse.jface.layout.GridDataFactory.swtDefaults().hint(150, org.eclipse.swt.SWT.DEFAULT).create());
        toolkit.adapt(categoryChooser, false, false);
        categoryChooser.setFont(org.eclipse.mylyn.internal.tasks.ui.editors.TEXT_FONT);
        org.eclipse.mylyn.internal.tasks.core.ITaskList taskList = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList();
        final java.util.List<org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory> categories = new java.util.ArrayList<org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory>(taskList.getCategories());
        java.util.Collections.sort(categories, new org.eclipse.mylyn.internal.tasks.ui.util.TaskContainerComparator());
        org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory selectedCategory = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getSelectedCategory(org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.getFromActivePerspective());
        int i = 0;
        int selectedIndex = 0;
        for (org.eclipse.mylyn.tasks.core.IRepositoryElement category : categories) {
            categoryChooser.add(category.getSummary());
            if (category.equals(selectedCategory)) {
                selectedIndex = i;
            }
            i++;
        }
        categoryChooser.select(selectedIndex);
        categoryChooser.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent event) {
                if (categoryChooser.getSelectionIndex() != (-1)) {
                    org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionPart.this.category = categories.get(categoryChooser.getSelectionIndex());
                }
            }
        });
        categoryChooser.setEnabled(false);
        addToCategory.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                markDirty();
                if (!addToCategory.getSelection()) {
                    org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionPart.this.category = null;
                } else if (categoryChooser.getSelectionIndex() != (-1)) {
                    org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionPart.this.category = categories.get(categoryChooser.getSelectionIndex());
                }
                categoryChooser.setEnabled(addToCategory.getSelection());
            }
        });
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().hint(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionPart.DEFAULT_FIELD_WIDTH, org.eclipse.swt.SWT.DEFAULT).span(3, org.eclipse.swt.SWT.DEFAULT).applyTo(categoryChooser);
    }

    public org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory getCategory() {
        return category;
    }

    @java.lang.Override
    public void createControl(org.eclipse.swt.widgets.Composite parent, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        org.eclipse.ui.forms.widgets.Section section = createSection(parent, toolkit, true);
        org.eclipse.swt.widgets.Composite buttonComposite = toolkit.createComposite(section);
        org.eclipse.swt.layout.GridLayout buttonLayout = org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.createSectionClientLayout();
        buttonLayout.numColumns = 4;
        buttonComposite.setLayout(buttonLayout);
        if (getTaskEditorPage().needsAddToCategory()) {
            createCategoryChooser(buttonComposite, toolkit);
        }
        selectedOperationAttribute = getTaskData().getRoot().getMappedAttribute(org.eclipse.mylyn.tasks.core.data.TaskAttribute.OPERATION);
        if ((selectedOperationAttribute != null) && org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_OPERATION.equals(selectedOperationAttribute.getMetaData().getType())) {
            org.eclipse.mylyn.tasks.core.data.TaskOperation selectedOperation = getTaskData().getAttributeMapper().getTaskOperation(selectedOperationAttribute);
            createRadioButtons(buttonComposite, toolkit, selectedOperation);
        }
        createOperationAttributes(buttonComposite, toolkit);
        createActionButtons(buttonComposite, toolkit);
        toolkit.paintBordersFor(buttonComposite);
        section.setClient(buttonComposite);
        setSection(toolkit, section);
    }

    private void createOperationAttributes(org.eclipse.swt.widgets.Composite buttonComposite, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        org.eclipse.swt.widgets.Composite parent = null;
        for (org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute : getTaskData().getRoot().getAttributes().values()) {
            if (org.eclipse.mylyn.tasks.core.data.TaskAttribute.KIND_OPERATION.equals(taskAttribute.getMetaData().getKind())) {
                if (parent == null) {
                    parent = toolkit.createComposite(buttonComposite);
                    parent.setLayout(new org.eclipse.swt.layout.GridLayout(2, false));
                    org.eclipse.jface.layout.GridDataFactory.fillDefaults().span(4, 1).grab(true, false).applyTo(parent);
                    toolkit.paintBordersFor(parent);
                }
                addAttribute(parent, toolkit, taskAttribute);
            }
        }
    }

    private void addAttribute(org.eclipse.swt.widgets.Composite attributesComposite, org.eclipse.ui.forms.widgets.FormToolkit toolkit, org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute) {
        org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor attributeEditor = createAttributeEditor(taskAttribute);
        if (attributeEditor.hasLabel() && (attributeEditor.getLabel().length() != 0)) {
            attributeEditor.createLabelControl(attributesComposite, toolkit);
            org.eclipse.swt.widgets.Label label = attributeEditor.getLabelControl();
            java.lang.String text = label.getText();
            label.setText(text);
            org.eclipse.swt.layout.GridData gd = org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.LEFT, org.eclipse.swt.SWT.CENTER).hint(org.eclipse.swt.SWT.DEFAULT, org.eclipse.swt.SWT.DEFAULT).create();
            label.setLayoutData(gd);
        }
        attributeEditor.createControl(attributesComposite, toolkit);
        getTaskEditorPage().getAttributeEditorToolkit().adapt(attributeEditor);
        org.eclipse.swt.layout.GridData gd = new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL, org.eclipse.swt.layout.GridData.CENTER, true, false);
        gd.minimumWidth = org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionPart.DEFAULT_FIELD_WIDTH;
        if (attributeEditor.getLabelControl() == null) {
            gd.horizontalSpan = 2;
        }
        attributeEditor.getControl().setLayoutData(gd);
    }

    private void addAttribute(org.eclipse.swt.widgets.Composite composite, org.eclipse.ui.forms.widgets.FormToolkit toolkit, org.eclipse.mylyn.tasks.core.data.TaskAttribute attribute, org.eclipse.swt.widgets.Button button) {
        org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor editor = createAttributeEditor(attribute);
        if (editor != null) {
            editor.createControl(composite, toolkit);
            org.eclipse.swt.layout.GridData gd = new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.HORIZONTAL_ALIGN_BEGINNING);
            gd.horizontalSpan = 3;
            org.eclipse.swt.widgets.Control editorControl = editor.getControl();
            if (editorControl instanceof org.eclipse.swt.custom.CCombo) {
                if (!org.eclipse.core.runtime.Platform.OS_MACOSX.equals(org.eclipse.core.runtime.Platform.getOS())) {
                    // XXX on some platforms combo boxes are too tall by default and wider than other controls
                    // bug 267135 only do this for non-mac platforms, since the default CCombo height on Carbon and Cocoa is perfect
                    gd.heightHint = 20;
                }
                gd.widthHint = org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionPart.RADIO_OPTION_WIDTH;
            } else {
                gd.widthHint = org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionPart.RADIO_OPTION_WIDTH - 5;
            }
            editorControl.setLayoutData(gd);
            if (editor instanceof org.eclipse.mylyn.internal.tasks.ui.editors.PersonAttributeEditor) {
                editorControl = ((org.eclipse.mylyn.internal.tasks.ui.editors.PersonAttributeEditor) (editor)).getText();
            }
            // the following listeners are hooked up so that changes to something in the actions area
            // will cause the corresponding radio button to become selected.  Note that we can't just use
            // a focus listener due to bug 254806
            if (editorControl instanceof org.eclipse.swt.custom.CCombo) {
                ((org.eclipse.swt.custom.CCombo) (editorControl)).addSelectionListener(new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionPart.SelectButtonListener(button));
            } else if (editorControl instanceof org.eclipse.swt.widgets.Text) {
                ((org.eclipse.swt.widgets.Text) (editorControl)).addModifyListener(new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionPart.SelectButtonListener(button));
                ((org.eclipse.swt.widgets.Text) (editorControl)).addVerifyListener(new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionPart.SelectButtonListener(button));
            } else if (editorControl instanceof org.eclipse.swt.custom.StyledText) {
                ((org.eclipse.swt.custom.StyledText) (editorControl)).getContent().addTextChangeListener(new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionPart.SelectButtonListener(button));
            } else {
                // last resort
                editorControl.addFocusListener(new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionPart.SelectButtonListener(button));
            }
            button.setData(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionPart.KEY_ASSOCIATED_EDITOR, editor);
            getTaskEditorPage().getAttributeEditorToolkit().adapt(editor);
        }
    }

    private void createRadioButtons(org.eclipse.swt.widgets.Composite buttonComposite, org.eclipse.ui.forms.widgets.FormToolkit toolkit, org.eclipse.mylyn.tasks.core.data.TaskOperation selectedOperation) {
        java.util.List<org.eclipse.mylyn.tasks.core.data.TaskOperation> operations = getTaskData().getAttributeMapper().getTaskOperations(selectedOperationAttribute);
        if (operations.size() > 0) {
            operationButtons = new java.util.ArrayList<org.eclipse.swt.widgets.Button>();
            org.eclipse.swt.widgets.Button selectedButton = null;
            for (org.eclipse.mylyn.tasks.core.data.TaskOperation operation : operations) {
                org.eclipse.swt.widgets.Button button = toolkit.createButton(buttonComposite, operation.getLabel(), org.eclipse.swt.SWT.RADIO);
                button.setFont(org.eclipse.mylyn.internal.tasks.ui.editors.TEXT_FONT);
                button.setData(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionPart.KEY_OPERATION, operation);
                // button.setEnabled(!operation.getTaskAttribute().getMetaData().isReadOnly());
                button.setEnabled(!operation.getTaskAttribute().getMetaData().isDisabled());
                button.setToolTipText(operation.getTaskAttribute().getMetaData().getValue(org.eclipse.mylyn.tasks.core.data.TaskAttribute.META_DESCRIPTION));
                org.eclipse.swt.layout.GridData radioData = new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.HORIZONTAL_ALIGN_BEGINNING);
                org.eclipse.mylyn.tasks.core.data.TaskAttribute associatedAttribute = getTaskData().getAttributeMapper().getAssoctiatedAttribute(operation);
                if (associatedAttribute != null) {
                    radioData.horizontalSpan = 1;
                    addAttribute(buttonComposite, toolkit, associatedAttribute, button);
                } else {
                    radioData.horizontalSpan = 4;
                }
                button.setLayoutData(radioData);
                button.addSelectionListener(new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionPart.SelectButtonListener(button));
                operationButtons.add(button);
                if (operation.equals(selectedOperation)) {
                    selectedButton = button;
                }
            }
            // do this last to ensure only a single button is selected
            if ((selectedButton == null) && (!operationButtons.isEmpty())) {
                selectedButton = operationButtons.get(0);
            }
            setSelectedRadionButton(selectedButton, false);
        }
    }

    public boolean getAttachContext() {
        if ((attachContextButton == null) || attachContextButton.isDisposed()) {
            return false;
        } else {
            return attachContextButton.getSelection();
        }
    }

    // boolean needsAttachContext() {
    // return needsAttachContext;
    // }
    // 
    // void setNeedsAttachContext(boolean attachContextEnabled) {
    // this.needsAttachContext = attachContextEnabled;
    // }
    public void setSubmitEnabled(boolean enabled) {
        if ((submitButton != null) && (!submitButton.isDisposed())) {
            submitButton.setEnabled(enabled);
            if (enabled) {
                submitButton.setToolTipText(java.text.MessageFormat.format(Messages.TaskEditorActionPart_Submit_to_X, getTaskEditorPage().getTaskRepository().getRepositoryUrl()));
            }
        }
    }

    private void setSelectedRadionButton(org.eclipse.swt.widgets.Button selectedButton, boolean updateModel) {
        // avoid changes to the model if the button is already selected
        if (selectedButton.getSelection()) {
            return;
        }
        selectedButton.setSelection(true);
        for (org.eclipse.swt.widgets.Button button : operationButtons) {
            if (button != selectedButton) {
                button.setSelection(false);
            }
        }
        if (updateModel) {
            org.eclipse.mylyn.tasks.core.data.TaskOperation taskOperation = ((org.eclipse.mylyn.tasks.core.data.TaskOperation) (selectedButton.getData(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionPart.KEY_OPERATION)));
            getTaskData().getAttributeMapper().setTaskOperation(selectedOperationAttribute, taskOperation);
            getModel().attributeChanged(selectedOperationAttribute);
            org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor editor = ((org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor) (selectedButton.getData(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionPart.KEY_ASSOCIATED_EDITOR)));
            if (editor instanceof org.eclipse.mylyn.internal.tasks.ui.editors.SingleSelectionAttributeEditor) {
                ((org.eclipse.mylyn.internal.tasks.ui.editors.SingleSelectionAttributeEditor) (editor)).selectDefaultValue();
            }
        }
    }

    /**
     *
     * @since 3.5
     */
    public void refreshOperations() {
        if (operationButtons != null) {
            for (org.eclipse.swt.widgets.Button button : operationButtons) {
                org.eclipse.mylyn.tasks.core.data.TaskOperation taskOperation = ((org.eclipse.mylyn.tasks.core.data.TaskOperation) (button.getData(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionPart.KEY_OPERATION)));
                button.setEnabled(!taskOperation.getTaskAttribute().getMetaData().isDisabled());
                button.setToolTipText(taskOperation.getTaskAttribute().getMetaData().getValue(org.eclipse.mylyn.tasks.core.data.TaskAttribute.META_DESCRIPTION));
            }
        }
    }
}