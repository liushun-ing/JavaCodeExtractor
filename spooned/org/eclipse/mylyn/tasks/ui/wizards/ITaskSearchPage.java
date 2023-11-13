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
package org.eclipse.mylyn.tasks.ui.wizards;
import org.eclipse.jface.dialogs.IDialogPage;
/**
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 * @author Steffen Pingel
 * @since 3.0
 */
public interface ITaskSearchPage extends org.eclipse.jface.dialogs.IDialogPage {
    public abstract void setContainer(org.eclipse.mylyn.tasks.ui.wizards.ITaskSearchPageContainer container);

    public abstract boolean performSearch();
}