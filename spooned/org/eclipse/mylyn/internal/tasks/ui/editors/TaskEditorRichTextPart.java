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
package org.eclipse.mylyn.internal.tasks.ui.editors;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.commons.ui.FillWidthLayout;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor.State;
import org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor.StateChangedEvent;
import org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor.StateChangedListener;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
/**
 *
 * @author Steffen Pingel
 */
public class TaskEditorRichTextPart extends org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart {
    private org.eclipse.mylyn.internal.tasks.ui.editors.RichTextAttributeEditor editor;

    private org.eclipse.mylyn.tasks.core.data.TaskAttribute attribute;

    private org.eclipse.swt.widgets.Composite composite;

    private int sectionStyle;

    private org.eclipse.jface.action.Action toggleEditAction;

    private org.eclipse.jface.action.Action toggleBrowserAction;

    private boolean ignoreToggleEvents;

    public TaskEditorRichTextPart() {
        setSectionStyle((org.eclipse.ui.forms.widgets.ExpandableComposite.TITLE_BAR | org.eclipse.ui.forms.widgets.ExpandableComposite.TWISTIE) | org.eclipse.ui.forms.widgets.ExpandableComposite.EXPANDED);
    }

    public void appendText(java.lang.String text) {
        if (editor == null) {
            return;
        }
        editor.showEditor();
        if (toggleEditAction != null) {
            toggleEditAction.setChecked(false);
        }
        java.lang.StringBuilder strBuilder = new java.lang.StringBuilder();
        java.lang.String oldText = editor.getViewer().getDocument().get();
        if (strBuilder.length() != 0) {
            strBuilder.append("\n");// $NON-NLS-1$

        }
        strBuilder.append(oldText);
        strBuilder.append(text);
        editor.getViewer().getDocument().set(strBuilder.toString());
        org.eclipse.mylyn.tasks.core.data.TaskAttribute attribute = getTaskData().getRoot().getMappedAttribute(org.eclipse.mylyn.tasks.core.data.TaskAttribute.COMMENT_NEW);
        if (attribute != null) {
            attribute.setValue(strBuilder.toString());
            getTaskEditorPage().getModel().attributeChanged(attribute);
        }
        editor.getViewer().getTextWidget().setCaretOffset(strBuilder.length());
        editor.getViewer().getTextWidget().showSelection();
    }

    public int getSectionStyle() {
        return sectionStyle;
    }

    public void setSectionStyle(int sectionStyle) {
        this.sectionStyle = sectionStyle;
    }

    @java.lang.Override
    public void createControl(org.eclipse.swt.widgets.Composite parent, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        if (attribute == null) {
            return;
        }
        org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor attributEditor = createAttributeEditor(attribute);
        if (!(attributEditor instanceof org.eclipse.mylyn.internal.tasks.ui.editors.RichTextAttributeEditor)) {
            java.lang.String clazz;
            if (attributEditor != null) {
                clazz = attributEditor.getClass().getName();
            } else {
                clazz = "<null>";// $NON-NLS-1$

            }
            org.eclipse.mylyn.commons.core.StatusHandler.log(// $NON-NLS-1$
            new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.WARNING, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, org.eclipse.osgi.util.NLS.bind("Expected an instance of RichTextAttributeEditor, got \"{0}\"", clazz), new java.lang.IllegalArgumentException()));
            return;
        }
        org.eclipse.ui.forms.widgets.Section section = createSection(parent, toolkit, sectionStyle);
        composite = toolkit.createComposite(section);
        composite.setLayout(org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.createSectionClientLayout());
        editor = ((org.eclipse.mylyn.internal.tasks.ui.editors.RichTextAttributeEditor) (attributEditor));
        editor.createControl(composite, toolkit);
        if (editor.isReadOnly()) {
            composite.setLayout(new org.eclipse.mylyn.commons.ui.FillWidthLayout(org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.getLayoutAdvisor(getTaskEditorPage()), 0, 0, 0, 3));
        } else {
            org.eclipse.swt.custom.StyledText textWidget = editor.getViewer().getTextWidget();
            editor.getControl().setLayoutData(org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.getTextControlLayoutData(getTaskEditorPage(), textWidget, getExpandVertically()));
            editor.getControl().setData(org.eclipse.ui.forms.widgets.FormToolkit.KEY_DRAW_BORDER, org.eclipse.ui.forms.widgets.FormToolkit.TREE_BORDER);
        }
        getEditor().getControl().setData(EditorUtil.KEY_TOGGLE_TO_MAXIMIZE_ACTION, getMaximizePartAction());
        if (getEditor().getControl() instanceof org.eclipse.swt.widgets.Composite) {
            for (org.eclipse.swt.widgets.Control control : ((org.eclipse.swt.widgets.Composite) (getEditor().getControl())).getChildren()) {
                control.setData(EditorUtil.KEY_TOGGLE_TO_MAXIMIZE_ACTION, getMaximizePartAction());
            }
        }
        getTaskEditorPage().getAttributeEditorToolkit().adapt(editor);
        toolkit.paintBordersFor(composite);
        section.setClient(composite);
        setSection(toolkit, section);
    }

    public org.eclipse.mylyn.tasks.core.data.TaskAttribute getAttribute() {
        return attribute;
    }

    protected org.eclipse.swt.widgets.Composite getComposite() {
        return composite;
    }

    protected org.eclipse.mylyn.internal.tasks.ui.editors.RichTextAttributeEditor getEditor() {
        return editor;
    }

    public void setAttribute(org.eclipse.mylyn.tasks.core.data.TaskAttribute attribute) {
        this.attribute = attribute;
    }

    @java.lang.Override
    public void setFocus() {
        if (editor != null) {
            editor.getControl().setFocus();
        }
    }

    @java.lang.Override
    protected void fillToolBar(org.eclipse.jface.action.ToolBarManager manager) {
        if (getEditor().hasPreview()) {
            toggleEditAction = new org.eclipse.jface.action.Action("", org.eclipse.swt.SWT.TOGGLE) {
                // $NON-NLS-1$
                @java.lang.Override
                public void run() {
                    if (isChecked()) {
                        editor.showEditor();
                    } else {
                        editor.showPreview();
                    }
                    if (toggleBrowserAction != null) {
                        toggleBrowserAction.setChecked(false);
                    }
                }
            };
            toggleEditAction.setImageDescriptor(org.eclipse.mylyn.commons.ui.CommonImages.EDIT_SMALL);
            toggleEditAction.setToolTipText(Messages.TaskEditorRichTextPart_Edit_Tooltip);
            toggleEditAction.setChecked(true);
            getEditor().getEditor().addStateChangedListener(new org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor.StateChangedListener() {
                public void stateChanged(org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor.StateChangedEvent event) {
                    try {
                        ignoreToggleEvents = true;
                        toggleEditAction.setChecked((event.state == org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor.State.EDITOR) || (event.state == org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor.State.DEFAULT));
                    } finally {
                        ignoreToggleEvents = false;
                    }
                }
            });
            manager.add(toggleEditAction);
        }
        if ((toggleEditAction == null) && getEditor().hasBrowser()) {
            toggleBrowserAction = new org.eclipse.jface.action.Action("", org.eclipse.swt.SWT.TOGGLE) {
                // $NON-NLS-1$
                @java.lang.Override
                public void run() {
                    if (ignoreToggleEvents) {
                        return;
                    }
                    if (isChecked()) {
                        editor.showBrowser();
                    } else {
                        editor.showEditor();
                    }
                    if (toggleEditAction != null) {
                        toggleEditAction.setChecked(false);
                    }
                }
            };
            toggleBrowserAction.setImageDescriptor(org.eclipse.mylyn.commons.ui.CommonImages.PREVIEW_WEB);
            toggleBrowserAction.setToolTipText(Messages.TaskEditorRichTextPart_Browser_Preview);
            toggleBrowserAction.setChecked(false);
            getEditor().getEditor().addStateChangedListener(new org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor.StateChangedListener() {
                public void stateChanged(org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor.StateChangedEvent event) {
                    try {
                        ignoreToggleEvents = true;
                        toggleBrowserAction.setChecked(event.state == org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor.State.BROWSER);
                    } finally {
                        ignoreToggleEvents = false;
                    }
                }
            });
            manager.add(toggleBrowserAction);
        }
        if (!getEditor().isReadOnly()) {
            manager.add(getMaximizePartAction());
        }
        super.fillToolBar(manager);
    }

    @java.lang.Override
    protected org.eclipse.swt.widgets.Control getLayoutControl() {
        return getEditor() != null ? getEditor().getControl() : null;
    }

    @java.lang.Override
    public boolean setFormInput(java.lang.Object input) {
        if ((input instanceof java.lang.String) && (getAttribute() != null)) {
            if (input.equals(getAttribute().getId())) {
                org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.focusOn(getTaskEditorPage().getManagedForm().getForm(), getControl());
                return true;
            }
        }
        return false;
    }
}