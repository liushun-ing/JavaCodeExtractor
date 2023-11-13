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
package org.eclipse.mylyn.internal.tasks.ui.search;
import org.eclipse.mylyn.commons.workbench.SubstringPatternFilter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.PatternFilter;
/**
 * The default implementation of a search handler
 *
 * @author David Green
 * @see SubstringPatternFilter
 */
public class DefaultSearchHandler extends org.eclipse.mylyn.internal.tasks.ui.search.AbstractSearchHandler {
    @java.lang.Override
    public org.eclipse.ui.dialogs.PatternFilter createFilter() {
        return new org.eclipse.mylyn.commons.workbench.SubstringPatternFilter();
    }

    @java.lang.Override
    public org.eclipse.swt.widgets.Composite createSearchComposite(org.eclipse.swt.widgets.Composite container) {
        // no additional controls
        return null;
    }

    @java.lang.Override
    public void dispose() {
        // nothing to do
    }
}