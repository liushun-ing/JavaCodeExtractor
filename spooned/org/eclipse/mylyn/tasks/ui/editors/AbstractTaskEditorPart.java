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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil;
import org.eclipse.mylyn.internal.tasks.ui.editors.Messages;
import org.eclipse.mylyn.internal.tasks.ui.editors.RichTextAttributeEditor;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.core.data.TaskDataModelEvent;
import org.eclipse.mylyn.tasks.core.data.TaskDataModelListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
/**
 *
 * @author Steffen Pingel
 * @since 3.0
 */
public abstract class AbstractTaskEditorPart extends org.eclipse.ui.forms.AbstractFormPart {
    // the default font of some controls, e.g. radio buttons, is too big; set this font explicitly on the control
    protected static final org.eclipse.swt.graphics.Font TEXT_FONT = org.eclipse.jface.resource.JFaceResources.getDefaultFont();

    private org.eclipse.swt.widgets.Control control;

    private java.lang.String partName;

    private java.lang.String partId;

    private org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage taskEditorPage;

    private boolean expandVertically;

    private org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart.MaximizePartAction maximizePartAction;

    private final java.util.Set<java.lang.String> attributeIds = new java.util.HashSet<>();

    public AbstractTaskEditorPart() {
    }

    protected org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor createAttributeEditor(org.eclipse.mylyn.tasks.core.data.TaskAttribute attribute) {
        if (attribute == null) {
            return null;
        }
        java.lang.String type = attribute.getMetaData().getType();
        if (type != null) {
            org.eclipse.mylyn.tasks.ui.editors.AttributeEditorFactory attributeEditorFactory = getTaskEditorPage().getAttributeEditorFactory();
            org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor editor = attributeEditorFactory.createEditor(type, attribute);
            if (editor instanceof org.eclipse.mylyn.internal.tasks.ui.editors.RichTextAttributeEditor) {
                boolean spellChecking = getTaskEditorPage().getAttributeEditorToolkit().hasSpellChecking(attribute);
                ((org.eclipse.mylyn.internal.tasks.ui.editors.RichTextAttributeEditor) (editor)).setSpellCheckingEnabled(spellChecking);
            }
            attributeIds.add(attribute.getId());
            return editor;
        }
        return null;
    }

    public abstract void createControl(org.eclipse.swt.widgets.Composite parent, org.eclipse.ui.forms.widgets.FormToolkit toolkit);

    protected org.eclipse.ui.forms.widgets.Section createSection(org.eclipse.swt.widgets.Composite parent, org.eclipse.ui.forms.widgets.FormToolkit toolkit, int style) {
        org.eclipse.ui.forms.widgets.Section section = toolkit.createSection(parent, style);
        section.setText(org.eclipse.jface.action.LegacyActionTools.escapeMnemonics(getPartName()));
        return section;
    }

    protected org.eclipse.ui.forms.widgets.Section createSection(org.eclipse.swt.widgets.Composite parent, org.eclipse.ui.forms.widgets.FormToolkit toolkit, boolean expandedState) {
        int style = org.eclipse.ui.forms.widgets.ExpandableComposite.TITLE_BAR | org.eclipse.ui.forms.widgets.ExpandableComposite.TWISTIE;
        if (expandedState) {
            style |= org.eclipse.ui.forms.widgets.ExpandableComposite.EXPANDED;
        }
        return createSection(parent, toolkit, style);
    }

    protected void fillToolBar(org.eclipse.jface.action.ToolBarManager toolBarManager) {
    }

    public org.eclipse.swt.widgets.Control getControl() {
        return control;
    }

    public org.eclipse.mylyn.tasks.core.data.TaskDataModel getModel() {
        return getTaskEditorPage().getModel();
    }

    public java.lang.String getPartId() {
        return partId;
    }

    public java.lang.String getPartName() {
        return partName;
    }

    public org.eclipse.mylyn.tasks.core.data.TaskData getTaskData() {
        return getTaskEditorPage().getModel().getTaskData();
    }

    public org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage getTaskEditorPage() {
        return taskEditorPage;
    }

    public void initialize(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage taskEditorPage) {
        this.taskEditorPage = taskEditorPage;
        getModel().addModelListener(new org.eclipse.mylyn.tasks.core.data.TaskDataModelListener() {
            @java.lang.Override
            public void attributeChanged(org.eclipse.mylyn.tasks.core.data.TaskDataModelEvent event) {
                if (attributeIds.contains(event.getTaskAttribute().getId())) {
                    markDirty();
                }
            }
        });
    }

    @java.lang.Override
    public void markDirty() {
        if (!isDirty()) {
            super.markDirty();
        }
    }

    public void setControl(org.eclipse.swt.widgets.Control control) {
        this.control = control;
    }

    void setPartId(java.lang.String partId) {
        this.partId = partId;
    }

    protected void setPartName(java.lang.String partName) {
        this.partName = partName;
    }

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
                toolBarManager.createControl(toolbarComposite);
                section.clientVerticalSpacing = 0;
                section.descriptionVerticalSpacing = 0;
                section.setTextClient(toolbarComposite);
            }
        }
        setControl(section);
    }

    protected boolean setSelection(org.eclipse.jface.viewers.ISelection selection) {
        return false;
    }

    public boolean getExpandVertically() {
        return expandVertically;
    }

    public void setExpandVertically(boolean expandVertically) {
        this.expandVertically = expandVertically;
    }

    /**
     * Returns an action for maximizing the part.
     *
     * @since 3.5
     */
    protected org.eclipse.jface.action.Action getMaximizePartAction() {
        if (maximizePartAction == null) {
            maximizePartAction = new org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart.MaximizePartAction();
        }
        return maximizePartAction;
    }

    /**
     * Returns the control that determines the size of the part.
     *
     * @see #getMaximizePartAction()
     * @since 3.5
     */
    protected org.eclipse.swt.widgets.Control getLayoutControl() {
        return getControl();
    }

    private class MaximizePartAction extends org.eclipse.jface.action.Action {
        private static final java.lang.String COMMAND_ID = "org.eclipse.mylyn.tasks.ui.command.maximizePart";// $NON-NLS-1$


        private static final int SECTION_HEADER_HEIGHT = 50;

        private int originalHeight = -2;

        public MaximizePartAction() {
            super(org.eclipse.mylyn.internal.tasks.ui.editors.Messages.TaskEditorRichTextPart_Maximize, org.eclipse.swt.SWT.TOGGLE);
            setImageDescriptor(org.eclipse.mylyn.commons.ui.CommonImages.PART_MAXIMIZE);
            setToolTipText(org.eclipse.mylyn.internal.tasks.ui.editors.Messages.TaskEditorRichTextPart_Maximize);
            setActionDefinitionId(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart.MaximizePartAction.COMMAND_ID);
            setChecked(false);
        }

        @java.lang.Override
        public void run() {
            if ((getControl() instanceof org.eclipse.ui.forms.widgets.Section) && (!((org.eclipse.ui.forms.widgets.Section) (getControl())).isExpanded())) {
                org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil.setExpanded(((org.eclipse.ui.forms.widgets.Section) (getControl())), true);
            }
            org.eclipse.swt.widgets.Control control = getLayoutControl();
            if ((control == null) || (!(control.getLayoutData() instanceof org.eclipse.swt.layout.GridData))) {
                return;
            }
            org.eclipse.swt.layout.GridData gd = ((org.eclipse.swt.layout.GridData) (control.getLayoutData()));
            // initialize originalHeight on first invocation
            if (originalHeight == (-2)) {
                originalHeight = gd.heightHint;
            }
            int heightHint;
            if (isChecked()) {
                heightHint = getManagedForm().getForm().getClientArea().height - org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart.MaximizePartAction.SECTION_HEADER_HEIGHT;
            } else {
                heightHint = originalHeight;
            }
            // ignore when not necessary
            if (gd.heightHint == heightHint) {
                return;
            }
            gd.heightHint = heightHint;
            gd.minimumHeight = heightHint;
            if (gd.widthHint == (-1)) {
                gd.widthHint = 300;// needs to be set or else heightHint is ignored

            }
            getTaskEditorPage().reflow();
            org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil.ensureVisible(control);
        }
    }
}