/**
 * *****************************************************************************
 * Copyright (c) 2011 Tasktop Technologies and others.
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
import org.eclipse.mylyn.commons.identity.core.Account;
import org.eclipse.mylyn.commons.identity.core.IIdentity;
import org.eclipse.mylyn.commons.identity.core.IIdentityService;
import org.eclipse.mylyn.commons.identity.core.IProfileImage;
import org.eclipse.mylyn.commons.identity.core.spi.ProfileImage;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
/**
 *
 * @author Steffen Pingel
 */
public class UserAttributeEditor extends org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor {
    private static final int IMAGE_SIZE = 48;

    private org.eclipse.swt.widgets.Label label;

    private org.eclipse.mylyn.commons.identity.core.IIdentity identity;

    private final java.beans.PropertyChangeListener imageListener = new java.beans.PropertyChangeListener() {
        public void propertyChange(java.beans.PropertyChangeEvent event) {
            if (event.getPropertyName().equals("image")) {
                // $NON-NLS-1$
                final org.eclipse.mylyn.commons.identity.core.spi.ProfileImage profileImage = ((org.eclipse.mylyn.commons.identity.core.spi.ProfileImage) (event.getNewValue()));
                org.eclipse.swt.widgets.Display.getDefault().asyncExec(new java.lang.Runnable() {
                    public void run() {
                        if (!label.isDisposed()) {
                            updateImage(profileImage);
                        }
                    }
                });
            }
        }
    };

    private org.eclipse.swt.graphics.Image image;

    public UserAttributeEditor(org.eclipse.mylyn.tasks.core.data.TaskDataModel manager, org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute) {
        super(manager, taskAttribute);
        setLayoutHint(new org.eclipse.mylyn.tasks.ui.editors.LayoutHint(org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan.SINGLE, org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan.SINGLE));
    }

    protected org.eclipse.swt.graphics.Image updateImage(org.eclipse.mylyn.commons.identity.core.IProfileImage profileImage) {
        if (image != null) {
            image.dispose();
        }
        org.eclipse.swt.graphics.ImageData data;
        if (profileImage != null) {
            data = new org.eclipse.swt.graphics.ImageData(new java.io.ByteArrayInputStream(profileImage.getData()));
            if ((data.width != org.eclipse.mylyn.internal.tasks.ui.editors.UserAttributeEditor.IMAGE_SIZE) || (data.height != org.eclipse.mylyn.internal.tasks.ui.editors.UserAttributeEditor.IMAGE_SIZE)) {
                data = data.scaledTo(org.eclipse.mylyn.internal.tasks.ui.editors.UserAttributeEditor.IMAGE_SIZE, org.eclipse.mylyn.internal.tasks.ui.editors.UserAttributeEditor.IMAGE_SIZE);
            }
        } else {
            data = org.eclipse.mylyn.commons.ui.CommonImages.PERSON_LARGE.getImageData();
        }
        image = new org.eclipse.swt.graphics.Image(label.getDisplay(), data);
        label.setImage(image);
        return image;
    }

    @java.lang.Override
    public void createControl(org.eclipse.swt.widgets.Composite parent, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        label = new org.eclipse.swt.widgets.Label(parent, org.eclipse.swt.SWT.NONE);
        label.setData(org.eclipse.ui.forms.widgets.FormToolkit.KEY_DRAW_BORDER, org.eclipse.ui.forms.widgets.FormToolkit.TREE_BORDER);
        label.addDisposeListener(new org.eclipse.swt.events.DisposeListener() {
            public void widgetDisposed(org.eclipse.swt.events.DisposeEvent e) {
                if (image != null) {
                    image.dispose();
                }
                if (identity != null) {
                    identity.removePropertyChangeListener(imageListener);
                }
            }
        });
        refresh();
        toolkit.adapt(label, false, false);
        setControl(label);
    }

    public org.eclipse.swt.graphics.Image getValue() {
        return image;
    }

    @java.lang.Override
    protected boolean shouldAutoRefresh() {
        // do not auto refresh to avoid picking up partially entered accounts
        return false;
    }

    @java.lang.Override
    public void refresh() {
        if (label.isDisposed()) {
            return;
        }
        if (identity != null) {
            identity.removePropertyChangeListener(imageListener);
        }
        if (org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_PERSON.equals(getTaskAttribute().getMetaData().getType())) {
            org.eclipse.mylyn.tasks.core.IRepositoryPerson person = getTaskAttribute().getTaskData().getAttributeMapper().getRepositoryPerson(getTaskAttribute());
            label.setToolTipText((getLabel() + " ") + person.toString());// $NON-NLS-1$

        } else {
            label.setToolTipText(getDescription());
        }
        org.eclipse.mylyn.commons.identity.core.Account account = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getAccount(getTaskAttribute());
        org.eclipse.mylyn.commons.identity.core.IIdentityService identityService = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getIdentityService();
        if (identityService != null) {
            identity = identityService.getIdentity(account);
            identity.addPropertyChangeListener(imageListener);
            java.util.concurrent.Future<org.eclipse.mylyn.commons.identity.core.IProfileImage> result = identity.requestImage(org.eclipse.mylyn.internal.tasks.ui.editors.UserAttributeEditor.IMAGE_SIZE, org.eclipse.mylyn.internal.tasks.ui.editors.UserAttributeEditor.IMAGE_SIZE);
            if (result.isDone()) {
                try {
                    updateImage(result.get(0, java.util.concurrent.TimeUnit.SECONDS));
                } catch (java.lang.Exception e) {
                    // the event listener will eventually update the image
                    updateImage(null);
                }
            } else {
                updateImage(null);
            }
        }
    }
}