/**
 * *****************************************************************************
 * Copyright (c) 2004, 2016 Tasktop Technologies and others.
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
import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.fieldassist.ContentAssistCommandAdapter;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
/**
 *
 * @author Steffen Pingel
 */
public class PersonAttributeEditor extends org.eclipse.mylyn.internal.tasks.ui.editors.TextAttributeEditor {
    private org.eclipse.ui.fieldassist.ContentAssistCommandAdapter contentAssistCommandAdapter = null;

    public PersonAttributeEditor(org.eclipse.mylyn.tasks.core.data.TaskDataModel manager, org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute) {
        super(manager, taskAttribute);
    }

    @java.lang.Override
    public org.eclipse.swt.widgets.Text getText() {
        return super.getText();
    }

    @java.lang.Override
    public void createControl(org.eclipse.swt.widgets.Composite parent, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        java.lang.String userName = getModel().getTaskRepository().getUserName();
        if ((isReadOnly() || (userName == null)) || (userName.length() == 0)) {
            super.createControl(parent, toolkit);
        } else {
            final org.eclipse.swt.widgets.Composite composite = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NONE);
            org.eclipse.swt.layout.GridLayout parentLayout = new org.eclipse.swt.layout.GridLayout(2, false);
            parentLayout.marginHeight = 0;
            parentLayout.marginWidth = 0;
            parentLayout.horizontalSpacing = 0;
            composite.setLayout(parentLayout);
            composite.setData(org.eclipse.ui.forms.widgets.FormToolkit.KEY_DRAW_BORDER, org.eclipse.ui.forms.widgets.FormToolkit.TREE_BORDER);
            super.createControl(composite, toolkit);
            getText().setData(org.eclipse.ui.forms.widgets.FormToolkit.KEY_DRAW_BORDER, java.lang.Boolean.FALSE);
            org.eclipse.jface.layout.GridDataFactory.fillDefaults().grab(true, false).applyTo(getText());
            final org.eclipse.ui.forms.widgets.ImageHyperlink selfLink = new org.eclipse.ui.forms.widgets.ImageHyperlink(composite, org.eclipse.swt.SWT.NO_FOCUS);
            selfLink.setToolTipText(Messages.PersonAttributeEditor_Insert_My_User_Id_Tooltip);
            // this is inserted to not get org.eclipse.swt.SWTException: Graphic is disposed on Mac OS Lion
            selfLink.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.PERSON_ME_SMALL));
            selfLink.setActiveImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.PERSON_ME_SMALL));
            selfLink.setHoverImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.PERSON_ME_SMALL));
            selfLink.addHyperlinkListener(new org.eclipse.ui.forms.events.HyperlinkAdapter() {
                @java.lang.Override
                public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                    java.lang.String userName = getModel().getTaskRepository().getUserName();
                    if ((userName != null) && (userName.length() > 0)) {
                        getText().setText(userName);
                        setValue(userName);
                    }
                }
            });
            org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.BEGINNING, org.eclipse.swt.SWT.CENTER).exclude(true).applyTo(selfLink);
            org.eclipse.swt.events.MouseTrackListener mouseListener = new org.eclipse.swt.events.MouseTrackAdapter() {
                int version = 0;

                @java.lang.Override
                public void mouseEnter(org.eclipse.swt.events.MouseEvent e) {
                    ((org.eclipse.swt.layout.GridData) (selfLink.getLayoutData())).exclude = false;
                    composite.layout();
                    selfLink.redraw();
                    version++;
                }

                @java.lang.Override
                public void mouseExit(org.eclipse.swt.events.MouseEvent e) {
                    final int lastVersion = version;
                    org.eclipse.swt.widgets.Display.getDefault().timerExec(100, new java.lang.Runnable() {
                        public void run() {
                            if ((version != lastVersion) || selfLink.isDisposed()) {
                                return;
                            }
                            selfLink.redraw();
                            ((org.eclipse.swt.layout.GridData) (selfLink.getLayoutData())).exclude = true;
                            composite.layout();
                        }
                    });
                }
            };
            composite.addMouseTrackListener(mouseListener);
            getText().addMouseTrackListener(mouseListener);
            selfLink.addMouseTrackListener(mouseListener);
            toolkit.paintBordersFor(composite);
            setControl(composite);
        }
    }

    @java.lang.Override
    public java.lang.String getValue() {
        org.eclipse.mylyn.tasks.core.IRepositoryPerson repositoryPerson = getAttributeMapper().getRepositoryPerson(getTaskAttribute());
        if (repositoryPerson != null) {
            return isReadOnly() ? repositoryPerson.toString() : repositoryPerson.getPersonId();
        }
        return "";// $NON-NLS-1$

    }

    @java.lang.Override
    public void setValue(java.lang.String text) {
        org.eclipse.mylyn.tasks.core.IRepositoryPerson person = getAttributeMapper().getTaskRepository().createPerson(text);
        getAttributeMapper().setRepositoryPerson(getTaskAttribute(), person);
        attributeChanged();
    }

    @java.lang.Override
    protected void decorateIncoming(org.eclipse.swt.graphics.Color color) {
        if (getControl() != null) {
            getControl().setBackground(color);
        }
        if ((getText() != null) && (getText() != getControl())) {
            getText().setBackground(color);
        }
    }

    public org.eclipse.ui.fieldassist.ContentAssistCommandAdapter getContentAssistCommandAdapter() {
        return contentAssistCommandAdapter;
    }

    public void setContentAssistCommandAdapter(org.eclipse.ui.fieldassist.ContentAssistCommandAdapter contentAssistCommandAdapter) {
        this.contentAssistCommandAdapter = contentAssistCommandAdapter;
    }
}