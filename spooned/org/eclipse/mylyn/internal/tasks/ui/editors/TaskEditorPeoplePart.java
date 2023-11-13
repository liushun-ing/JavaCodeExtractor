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
 *     Robert Munteanu - fix for bug #377081
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.editors;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMetaData;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
/**
 *
 * @author Steffen Pingel
 */
public class TaskEditorPeoplePart extends org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart {
    private static final int COLUMN_MARGIN = 5;

    public TaskEditorPeoplePart() {
        setPartName(Messages.TaskEditorPeoplePart_People);
    }

    protected void addAttribute(org.eclipse.swt.widgets.Composite composite, org.eclipse.ui.forms.widgets.FormToolkit toolkit, org.eclipse.mylyn.tasks.core.data.TaskAttribute attribute) {
        org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor editor = createAttributeEditor(attribute);
        if (editor != null) {
            editor.createLabelControl(composite, toolkit);
            org.eclipse.jface.layout.GridDataFactory.defaultsFor(editor.getLabelControl()).indent(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorPeoplePart.COLUMN_MARGIN, 0).applyTo(editor.getLabelControl());
            editor.createControl(composite, toolkit);
            getTaskEditorPage().getAttributeEditorToolkit().adapt(editor);
            org.eclipse.jface.layout.GridDataFactory dataFactory = createLayoutData(editor);
            dataFactory.applyTo(editor.getControl());
        }
    }

    protected org.eclipse.jface.layout.GridDataFactory createLayoutData(org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor editor) {
        org.eclipse.jface.layout.GridDataFactory gridDataFactory = org.eclipse.jface.layout.GridDataFactory.fillDefaults().grab(true, false).align(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.TOP).indent(3, 0);// prevent clipping of decorators on Mac

        if (editor instanceof org.eclipse.mylyn.internal.tasks.ui.editors.MultiSelectionAttributeEditor) {
            gridDataFactory.hint(org.eclipse.swt.SWT.DEFAULT, 95);
        }
        return gridDataFactory;
    }

    @java.lang.Override
    public void createControl(org.eclipse.swt.widgets.Composite parent, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        org.eclipse.ui.forms.widgets.Section section = createSection(parent, toolkit, true);
        org.eclipse.swt.widgets.Composite peopleComposite = toolkit.createComposite(section);
        org.eclipse.swt.layout.GridLayout layout = org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.createSectionClientLayout();
        layout.numColumns = 2;
        peopleComposite.setLayout(layout);
        createAttributeEditors(toolkit, peopleComposite);
        toolkit.paintBordersFor(peopleComposite);
        section.setClient(peopleComposite);
        setSection(toolkit, section);
    }

    protected void createAttributeEditors(org.eclipse.ui.forms.widgets.FormToolkit toolkit, org.eclipse.swt.widgets.Composite peopleComposite) {
        java.util.Collection<org.eclipse.mylyn.tasks.core.data.TaskAttribute> attributes = getAttributes();
        for (org.eclipse.mylyn.tasks.core.data.TaskAttribute attribute : attributes) {
            addAttribute(peopleComposite, toolkit, attribute);
        }
    }

    protected java.util.Collection<org.eclipse.mylyn.tasks.core.data.TaskAttribute> getAttributes() {
        java.util.Map<java.lang.String, org.eclipse.mylyn.tasks.core.data.TaskAttribute> allAttributes = getTaskData().getRoot().getAttributes();
        java.util.List<org.eclipse.mylyn.tasks.core.data.TaskAttribute> attributes = new java.util.ArrayList<org.eclipse.mylyn.tasks.core.data.TaskAttribute>(allAttributes.size());
        attributes.add(getTaskData().getRoot().getMappedAttribute(org.eclipse.mylyn.tasks.core.data.TaskAttribute.USER_ASSIGNED));
        attributes.add(getTaskData().getRoot().getMappedAttribute(org.eclipse.mylyn.tasks.core.data.TaskAttribute.USER_REPORTER));
        attributes.add(getTaskData().getRoot().getMappedAttribute(org.eclipse.mylyn.tasks.core.data.TaskAttribute.ADD_SELF_CC));
        attributes.add(getTaskData().getRoot().getMappedAttribute(org.eclipse.mylyn.tasks.core.data.TaskAttribute.USER_CC));
        for (org.eclipse.mylyn.tasks.core.data.TaskAttribute attribute : allAttributes.values()) {
            org.eclipse.mylyn.tasks.core.data.TaskAttributeMetaData properties = attribute.getMetaData();
            if (org.eclipse.mylyn.tasks.core.data.TaskAttribute.KIND_PEOPLE.equals(properties.getKind())) {
                if (!attributes.contains(attribute)) {
                    attributes.add(attribute);
                }
            }
        }
        return attributes;
    }
}