/**
 * *****************************************************************************
 * Copyright (c) 2004, 2015 Tasktop Technologies and others.
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
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.mylyn.internal.tasks.ui.editors.Messages;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.core.data.TaskDataModelEvent;
import org.eclipse.mylyn.tasks.core.data.TaskDataModelListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
/**
 *
 * @author Steffen Pingel
 * @author Sam Davis
 * @since 3.0
 */
public abstract class AbstractAttributeEditor {
    /**
     * The key used to associate the editor control with the corresponding task attribute. This enables lookup of the
     * model element from the widget hierarchy.
     *
     * @since 3.5
     * @see Control#getData(String)
     * @see #getControl()
     * @see #getTaskAttribute()
     */
    public static final java.lang.String KEY_TASK_ATTRIBUTE = "org.eclipse.mylyn.tasks.ui.editors.TaskAttribute";// $NON-NLS-1$


    private org.eclipse.swt.widgets.Control control;

    private boolean decorationEnabled;

    private org.eclipse.swt.widgets.Label labelControl;

    private org.eclipse.mylyn.tasks.ui.editors.LayoutHint layoutHint;

    private final org.eclipse.mylyn.tasks.core.data.TaskDataModel dataModel;

    private final org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute;

    private boolean readOnly;

    private java.lang.String description;

    private boolean refreshInProgress;

    private final org.eclipse.mylyn.tasks.core.data.TaskDataModelListener modelListener = new org.eclipse.mylyn.tasks.core.data.TaskDataModelListener() {
        @java.lang.Override
        public void attributeChanged(org.eclipse.mylyn.tasks.core.data.TaskDataModelEvent event) {
            if (getTaskAttribute().equals(event.getTaskAttribute())) {
                try {
                    if (shouldAutoRefresh()) {
                        refreshInProgress = true;
                        refresh();
                    }
                    updateRequiredDecoration();
                } catch (java.lang.UnsupportedOperationException e) {
                } finally {
                    refreshInProgress = false;
                }
                java.lang.String changedAttribute = event.getTaskAttribute().getId();
                for (org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute : event.getTaskAttribute().getTaskData().getRoot().getAttributes().values()) {
                    if (changedAttribute.equals(taskAttribute.getMetaData().getDependsOn())) {
                        event.getModel().attributeChanged(taskAttribute);
                    }
                }
            }
        }
    };

    private final org.eclipse.swt.events.DisposeListener disposeListener = new org.eclipse.swt.events.DisposeListener() {
        public void widgetDisposed(org.eclipse.swt.events.DisposeEvent e) {
            getModel().removeModelListener(modelListener);
        }
    };

    private final org.eclipse.swt.events.DisposeListener disposeDecorationListener = new org.eclipse.swt.events.DisposeListener() {
        @java.lang.Override
        public void widgetDisposed(org.eclipse.swt.events.DisposeEvent e) {
            if (decoration != null) {
                decoration.dispose();
                decoration = null;
            }
        }
    };

    private org.eclipse.jface.fieldassist.ControlDecoration decoration;

    /**
     *
     * @since 3.0
     */
    public AbstractAttributeEditor(@org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.core.data.TaskDataModel dataModel, @org.eclipse.jdt.annotation.NonNull
    org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute) {
        org.eclipse.core.runtime.Assert.isNotNull(dataModel);
        org.eclipse.core.runtime.Assert.isNotNull(taskAttribute);
        this.dataModel = dataModel;
        this.taskAttribute = taskAttribute;
        setDecorationEnabled(true);
        setReadOnly(taskAttribute.getMetaData().isReadOnly());
        setDescription(taskAttribute.getMetaData().getValue(org.eclipse.mylyn.tasks.core.data.TaskAttribute.META_DESCRIPTION));
    }

    /**
     *
     * @since 3.0
     */
    protected void attributeChanged() {
        if (!refreshInProgress) {
            getModel().attributeChanged(getTaskAttribute());
            if (!getTaskAttribute().getTaskData().getRoot().equals(getTaskAttribute().getParentAttribute())) {
                getModel().attributeChanged(getTaskAttribute().getParentAttribute());
            }
        }
    }

    /**
     *
     * @since 3.0
     */
    public abstract void createControl(@org.eclipse.jdt.annotation.NonNull
    org.eclipse.swt.widgets.Composite parent, @org.eclipse.jdt.annotation.NonNull
    org.eclipse.ui.forms.widgets.FormToolkit toolkit);

    /**
     *
     * @since 3.0
     */
    public void createLabelControl(@org.eclipse.jdt.annotation.NonNull
    org.eclipse.swt.widgets.Composite composite, @org.eclipse.jdt.annotation.NonNull
    org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        labelControl = toolkit.createLabel(composite, getLabel());
        labelControl.setForeground(toolkit.getColors().getColor(org.eclipse.ui.forms.IFormColors.TITLE));
    }

    /**
     *
     * @since 3.0
     * @deprecated Method is never called
     */
    @java.lang.Deprecated
    public void dispose() {
    }

    /**
     *
     * @since 3.0
     */
    @org.eclipse.jdt.annotation.NonNull
    public org.eclipse.mylyn.tasks.core.data.TaskDataModel getModel() {
        return dataModel;
    }

    /**
     *
     * @since 3.0
     */
    @org.eclipse.jdt.annotation.NonNull
    protected org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper getAttributeMapper() {
        return getModel().getTaskData().getAttributeMapper();
    }

    /**
     *
     * @since 3.0
     */
    @org.eclipse.jdt.annotation.Nullable
    public org.eclipse.swt.widgets.Control getControl() {
        return control;
    }

    /**
     *
     * @since 3.0
     */
    @org.eclipse.jdt.annotation.NonNull
    public java.lang.String getLabel() {
        java.lang.String label = getAttributeMapper().getLabel(getTaskAttribute());
        return label != null ? org.eclipse.jface.action.LegacyActionTools.escapeMnemonics(label) : "";// $NON-NLS-1$

    }

    /**
     *
     * @since 3.0
     */
    @org.eclipse.jdt.annotation.Nullable
    public org.eclipse.swt.widgets.Label getLabelControl() {
        return labelControl;
    }

    /**
     *
     * @since 3.0
     */
    @org.eclipse.jdt.annotation.Nullable
    public org.eclipse.mylyn.tasks.ui.editors.LayoutHint getLayoutHint() {
        return layoutHint;
    }

    /**
     *
     * @since 3.0
     */
    @org.eclipse.jdt.annotation.NonNull
    public org.eclipse.mylyn.tasks.core.data.TaskAttribute getTaskAttribute() {
        return taskAttribute;
    }

    /**
     *
     * @since 3.0
     */
    public boolean hasLabel() {
        // TODO EDITOR
        return true;
    }

    /**
     *
     * @since 3.0
     */
    public boolean isDecorationEnabled() {
        return decorationEnabled;
    }

    /**
     *
     * @since 3.0
     */
    protected void setControl(@org.eclipse.jdt.annotation.Nullable
    org.eclipse.swt.widgets.Control control) {
        if ((this.control != null) && (!this.control.isDisposed())) {
            this.control.removeDisposeListener(disposeListener);
            getModel().removeModelListener(modelListener);
        }
        this.control = control;
        if (control != null) {
            control.setData(org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor.KEY_TASK_ATTRIBUTE, taskAttribute);
            control.addDisposeListener(disposeListener);
            getModel().addModelListener(modelListener);
        }
    }

    /**
     *
     * @since 3.0
     */
    public void setDecorationEnabled(boolean decorationEnabled) {
        this.decorationEnabled = decorationEnabled;
    }

    /**
     *
     * @since 3.1
     */
    public void setLayoutHint(@org.eclipse.jdt.annotation.Nullable
    org.eclipse.mylyn.tasks.ui.editors.LayoutHint layoutHint) {
        this.layoutHint = layoutHint;
    }

    /**
     *
     * @since 3.0
     */
    public void decorate(@org.eclipse.jdt.annotation.Nullable
    org.eclipse.swt.graphics.Color color) {
        if (isDecorationEnabled()) {
            if (dataModel.hasBeenRead() && dataModel.hasIncomingChanges(getTaskAttribute())) {
                decorateIncoming(color);
            }
            if (dataModel.hasOutgoingChanges(getTaskAttribute())) {
                decorateOutgoing(color);
            }
            updateRequiredDecoration();
        }
    }

    private void updateRequiredDecoration() {
        if (getLabelControl() != null) {
            if (needsValue()) {
                decorateRequired();
            } else if (decoration != null) {
                decoration.hide();
                decoration.dispose();
                decoration = null;
                getLabelControl().removeDisposeListener(disposeDecorationListener);
            }
        }
    }

    /**
     *
     * @since 3.11
     */
    protected void decorateRequired() {
        if (decoration == null) {
            decoration = new org.eclipse.jface.fieldassist.ControlDecoration(getLabelControl(), org.eclipse.swt.SWT.BOTTOM | org.eclipse.swt.SWT.RIGHT);
            decoration.setDescriptionText(org.eclipse.mylyn.internal.tasks.ui.editors.Messages.AbstractAttributeEditor_AttributeIsRequired);
            decoration.setMarginWidth(0);
            org.eclipse.swt.graphics.Image image = org.eclipse.jface.fieldassist.FieldDecorationRegistry.getDefault().getFieldDecoration(org.eclipse.jface.fieldassist.FieldDecorationRegistry.DEC_ERROR).getImage();
            decoration.setImage(image);
            getLabelControl().addDisposeListener(disposeDecorationListener);
        }
    }

    /**
     *
     * @since 3.11
     */
    protected boolean needsValue() {
        boolean isRequired = getTaskAttribute().getMetaData().isRequired();
        boolean hasValue = !org.apache.commons.lang.StringUtils.isEmpty(getAttributeMapper().getValue(getTaskAttribute()));
        boolean isReadOnly = getTaskAttribute().getMetaData().isReadOnly();
        // for an empty required read only attribute we do not show the error decorator
        // because the user can not change the value (see bug#492500 ).
        return (isRequired && (!hasValue)) && (!isReadOnly);
    }

    /**
     *
     * @since 3.0
     */
    protected void decorateOutgoing(@org.eclipse.jdt.annotation.Nullable
    org.eclipse.swt.graphics.Color color) {
        if (labelControl != null) {
            labelControl.setText("*" + labelControl.getText());// $NON-NLS-1$

        }
    }

    /**
     *
     * @since 3.0
     */
    protected void decorateIncoming(@org.eclipse.jdt.annotation.Nullable
    org.eclipse.swt.graphics.Color color) {
        if (getControl() != null) {
            getControl().setBackground(color);
        }
    }

    /**
     *
     * @since 3.0
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     *
     * @since 3.0
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Refreshes the state of the widget from the data model. The default implementation throws
     * <code>UnsupportedOperationException</code>.
     * <p>
     * Subclasses should overwrite this method.
     *
     * @since 3.1
     * @throws UnsupportedOperationException
     * 		if this method is not supported by the editor
     */
    public void refresh() {
        throw new java.lang.UnsupportedOperationException();
    }

    /**
     * Subclasses that implement refresh should override this method to return true, so that they will be automatically
     * refreshed when the model changes.
     *
     * @return whether the editor should be automatically refreshed when the model changes
     * @since 3.6
     */
    protected boolean shouldAutoRefresh() {
        return false;
    }

    /**
     *
     * @since 3.5
     */
    @org.eclipse.jdt.annotation.Nullable
    public java.lang.String getDescription() {
        return description;
    }

    /**
     *
     * @since 3.5
     */
    public void setDescription(@org.eclipse.jdt.annotation.Nullable
    java.lang.String description) {
        this.description = description;
    }

    /**
     *
     * @since 3.6
     */
    protected void updateLabel() {
        org.eclipse.swt.widgets.Label labelControl = getLabelControl();
        if ((labelControl != null) && (!labelControl.isDisposed())) {
            labelControl.setText(getLabel());
        }
    }
}