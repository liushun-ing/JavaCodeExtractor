/**
 * *****************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
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
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
/**
 * Abstract part that can be used on the local task editor
 *
 * @author Shawn Minto
 */
public abstract class AbstractLocalEditorPart extends org.eclipse.ui.forms.AbstractFormPart {
    private static final java.lang.String FLAG_DIRTY = "LocalEditor.dirty";// $NON-NLS-1$


    private org.eclipse.swt.widgets.Control control;

    private org.eclipse.mylyn.tasks.core.TaskRepository repository;

    private org.eclipse.ui.forms.widgets.Section section;

    private final java.lang.String sectionName;

    private final int sectionStyle;

    private org.eclipse.mylyn.internal.tasks.core.AbstractTask task;

    public AbstractLocalEditorPart(int sectionStyle, java.lang.String sectionName) {
        this.sectionStyle = sectionStyle;
        this.sectionName = sectionName;
    }

    public AbstractLocalEditorPart(java.lang.String sectionName) {
        this.sectionStyle = org.eclipse.ui.forms.widgets.ExpandableComposite.TWISTIE;
        this.sectionName = sectionName;
    }

    public abstract org.eclipse.swt.widgets.Control createControl(org.eclipse.swt.widgets.Composite parent, org.eclipse.ui.forms.widgets.FormToolkit toolkit);

    protected org.eclipse.ui.forms.widgets.Section createSection(org.eclipse.swt.widgets.Composite parent, org.eclipse.ui.forms.widgets.FormToolkit toolkit, boolean expandedState) {
        int style = org.eclipse.ui.forms.widgets.ExpandableComposite.TITLE_BAR | getSectionStyle();
        if (expandedState && isTwistie(style)) {
            style |= org.eclipse.ui.forms.widgets.ExpandableComposite.EXPANDED;
        }
        return createSection(parent, toolkit, style);
    }

    protected org.eclipse.ui.forms.widgets.Section createSection(org.eclipse.swt.widgets.Composite parent, org.eclipse.ui.forms.widgets.FormToolkit toolkit, int style) {
        org.eclipse.ui.forms.widgets.Section section = toolkit.createSection(parent, style);
        section.setText(getSectionName());
        section.clientVerticalSpacing = 0;
        section.descriptionVerticalSpacing = 0;
        section.marginHeight = 0;
        return section;
    }

    public org.eclipse.swt.widgets.Control getControl() {
        return control;
    }

    public org.eclipse.mylyn.tasks.core.TaskRepository getRepository() {
        return repository;
    }

    public org.eclipse.ui.forms.widgets.Section getSection() {
        return section;
    }

    public java.lang.String getSectionName() {
        return sectionName;
    }

    private int getSectionStyle() {
        return sectionStyle;
    }

    public org.eclipse.mylyn.internal.tasks.core.AbstractTask getTask() {
        return task;
    }

    public void initialize(org.eclipse.ui.forms.IManagedForm managedForm, org.eclipse.mylyn.tasks.core.TaskRepository repository, org.eclipse.mylyn.internal.tasks.core.AbstractTask task) {
        super.initialize(managedForm);
        this.task = task;
        this.repository = repository;
    }

    private boolean isTwistie(int style) {
        return (style & org.eclipse.ui.forms.widgets.ExpandableComposite.TWISTIE) != 0;
    }

    public void setControl(org.eclipse.swt.widgets.Control control) {
        this.control = control;
    }

    protected void setSection(org.eclipse.ui.forms.widgets.FormToolkit toolkit, org.eclipse.ui.forms.widgets.Section section) {
        this.section = section;
        setControl(section);
    }

    @java.lang.Override
    public void refresh() {
        refresh(true);
        super.refresh();
    }

    protected void refresh(boolean discardChanges) {
    }

    protected void markDirty(org.eclipse.swt.widgets.Control control) {
        control.setData(org.eclipse.mylyn.internal.tasks.ui.editors.AbstractLocalEditorPart.FLAG_DIRTY, java.lang.Boolean.TRUE);
        markDirty();
    }

    @java.lang.Override
    public void markDirty() {
        if (!isDirty()) {
            super.markDirty();
        }
    }

    protected boolean shouldRefresh(org.eclipse.swt.widgets.Control control, boolean discardChanges) {
        if (discardChanges) {
            clearState(control);
            return true;
        }
        return control.getData(org.eclipse.mylyn.internal.tasks.ui.editors.AbstractLocalEditorPart.FLAG_DIRTY) == null;
    }

    protected void clearState(org.eclipse.swt.widgets.Control control) {
        control.setData(org.eclipse.mylyn.internal.tasks.ui.editors.AbstractLocalEditorPart.FLAG_DIRTY, null);
    }
}