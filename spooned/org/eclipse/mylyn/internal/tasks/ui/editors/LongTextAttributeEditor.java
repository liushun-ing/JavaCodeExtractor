/**
 * *****************************************************************************
 * Copyright (c) 2004, 2011 Tasktop Technologies and others.
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
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport;
import org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil;
import org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
/**
 *
 * @author Steffen Pingel
 */
public class LongTextAttributeEditor extends org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor {
    private org.eclipse.jface.text.source.SourceViewer viewer;

    boolean ignoreNotification;

    boolean suppressRefresh;

    public LongTextAttributeEditor(org.eclipse.mylyn.tasks.core.data.TaskDataModel manager, org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute) {
        super(manager, taskAttribute);
        setLayoutHint(new org.eclipse.mylyn.tasks.ui.editors.LayoutHint(org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan.MULTIPLE, org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan.MULTIPLE));
    }

    @java.lang.Override
    public void createControl(org.eclipse.swt.widgets.Composite parent, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        int style = (org.eclipse.swt.SWT.FLAT | org.eclipse.swt.SWT.MULTI) | org.eclipse.swt.SWT.WRAP;
        if (!isReadOnly()) {
            style |= org.eclipse.swt.SWT.V_SCROLL;
        }
        viewer = new org.eclipse.jface.text.source.SourceViewer(parent, null, style);
        org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration configuration = org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor.installHyperlinkPresenter(viewer, getModel().getTaskRepository(), getModel().getTask(), org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode.DEFAULT);
        viewer.configure(configuration);
        viewer.setDocument(new org.eclipse.jface.text.Document(getValue()));
        final org.eclipse.swt.custom.StyledText text = viewer.getTextWidget();
        text.setToolTipText(getDescription());
        toolkit.adapt(text, false, false);
        // enable cut/copy/paste
        org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport.setTextViewer(text, viewer);
        if (isReadOnly()) {
            viewer.setEditable(false);
        } else {
            viewer.setEditable(true);
            text.setData(org.eclipse.ui.forms.widgets.FormToolkit.KEY_DRAW_BORDER, org.eclipse.ui.forms.widgets.FormToolkit.TREE_BORDER);
            text.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
                public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
                    try {
                        suppressRefresh = true;
                        setValue(text.getText());
                        org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil.ensureVisible(text);
                    } finally {
                        suppressRefresh = false;
                    }
                }
            });
        }
        setControl(text);
    }

    public java.lang.String getValue() {
        return getAttributeMapper().getValue(getTaskAttribute());
    }

    public void setValue(java.lang.String text) {
        getAttributeMapper().setValue(getTaskAttribute(), text);
        attributeChanged();
    }

    @java.lang.Override
    public void refresh() {
        if ((viewer.getTextWidget() != null) && (!viewer.getTextWidget().isDisposed())) {
            try {
                ignoreNotification = true;
                viewer.getDocument().set(getValue());
            } finally {
                ignoreNotification = false;
            }
        }
    }

    @java.lang.Override
    public boolean shouldAutoRefresh() {
        return !suppressRefresh;
    }
}