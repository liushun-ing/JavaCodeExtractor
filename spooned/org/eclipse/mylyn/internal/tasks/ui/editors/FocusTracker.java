/**
 * *****************************************************************************
 * Copyright (c) 2009 Tasktop Technologies and others.
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
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.forms.widgets.Hyperlink;
/**
 * Tracks which control had last focus for all registered controls.
 *
 * @author Steffen Pingel
 */
public class FocusTracker {
    private org.eclipse.swt.widgets.Control lastFocusControl;

    private final org.eclipse.swt.events.FocusListener listener = new org.eclipse.swt.events.FocusAdapter() {
        @java.lang.Override
        public void focusGained(org.eclipse.swt.events.FocusEvent e) {
            lastFocusControl = ((org.eclipse.swt.widgets.Control) (e.widget));
        }
    };

    public void track(org.eclipse.swt.widgets.Composite composite) {
        org.eclipse.swt.widgets.Control[] children = composite.getChildren();
        for (org.eclipse.swt.widgets.Control control : children) {
            if ((((((((((((((control instanceof org.eclipse.swt.widgets.Text) || (control instanceof org.eclipse.swt.widgets.Button)) || (control instanceof org.eclipse.swt.widgets.Combo)) || (control instanceof org.eclipse.swt.custom.CCombo)) || (control instanceof org.eclipse.swt.widgets.Tree)) || (control instanceof org.eclipse.swt.widgets.Table)) || (control instanceof org.eclipse.swt.widgets.Spinner)) || (control instanceof org.eclipse.swt.widgets.Link)) || (control instanceof org.eclipse.swt.widgets.List)) || (control instanceof org.eclipse.swt.widgets.TabFolder)) || (control instanceof org.eclipse.swt.custom.CTabFolder)) || (control instanceof org.eclipse.ui.forms.widgets.Hyperlink)) || (control instanceof org.eclipse.ui.dialogs.FilteredTree)) || (control instanceof org.eclipse.swt.custom.StyledText)) {
                control.addFocusListener(listener);
            }
            if (control instanceof org.eclipse.swt.widgets.Composite) {
                track(((org.eclipse.swt.widgets.Composite) (control)));
            }
        }
    }

    public void reset() {
        lastFocusControl = null;
    }

    public boolean setFocus() {
        if ((lastFocusControl != null) && (!lastFocusControl.isDisposed())) {
            return lastFocusControl.setFocus();
        }
        return false;
    }
}