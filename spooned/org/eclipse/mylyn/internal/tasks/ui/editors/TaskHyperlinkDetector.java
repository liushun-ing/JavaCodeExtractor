/**
 * *****************************************************************************
 * Copyright (c) 2004, 2012 Tasktop Technologies and others.
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
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.AbstractTaskHyperlinkDetector;
/**
 * Delegates to {@link AbstractRepositoryConnectorUi} for detecting hyperlinks.
 *
 * @author Steffen Pingel
 */
public class TaskHyperlinkDetector extends org.eclipse.mylyn.tasks.ui.AbstractTaskHyperlinkDetector {
    @java.lang.Override
    protected java.util.List<org.eclipse.jface.text.hyperlink.IHyperlink> detectHyperlinks(org.eclipse.jface.text.ITextViewer textViewer, final java.lang.String content, final int index, final int contentOffset) {
        java.util.List<org.eclipse.jface.text.hyperlink.IHyperlink> result = new java.util.ArrayList<org.eclipse.jface.text.hyperlink.IHyperlink>();
        for (final org.eclipse.mylyn.tasks.core.TaskRepository repository : getTaskRepositories(textViewer)) {
            final org.eclipse.jface.text.hyperlink.IHyperlink[] links = detectHyperlinks(repository, content, index, contentOffset);
            if ((links != null) && (links.length > 0)) {
                result.addAll(java.util.Arrays.asList(links));
            }
        }
        if (result.isEmpty()) {
            return null;
        }
        return result;
    }

    protected org.eclipse.jface.text.hyperlink.IHyperlink[] detectHyperlinks(final org.eclipse.mylyn.tasks.core.TaskRepository repository, final java.lang.String content, final int index, final int contentOffset) {
        final org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi connectorUi = getConnectorUi(repository);
        if (connectorUi == null) {
            return null;
        }
        final org.eclipse.jface.text.hyperlink.IHyperlink[][] links = new org.eclipse.jface.text.hyperlink.IHyperlink[1][];
        org.eclipse.jface.util.SafeRunnable.run(new org.eclipse.core.runtime.ISafeRunnable() {
            public void handleException(java.lang.Throwable exception) {
            }

            public void run() throws java.lang.Exception {
                final org.eclipse.mylyn.tasks.core.ITask task = ((org.eclipse.mylyn.tasks.core.ITask) (getAdapter(org.eclipse.mylyn.tasks.core.ITask.class)));
                links[0] = connectorUi.findHyperlinks(repository, task, content, index, contentOffset);
            }
        });
        return links[0];
    }

    protected org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi getConnectorUi(org.eclipse.mylyn.tasks.core.TaskRepository repository) {
        return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getConnectorUi(repository.getConnectorKind());
    }
}