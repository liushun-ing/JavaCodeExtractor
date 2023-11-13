/**
 * *****************************************************************************
 * Copyright (c) 2012 Tasktop Technologies and others.
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
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
/**
 *
 * @author Sam Davis
 */
public class MultipleTaskHyperlinkDetector extends org.eclipse.mylyn.internal.tasks.ui.editors.TaskHyperlinkDetector {
    @java.lang.Override
    protected java.util.List<org.eclipse.jface.text.hyperlink.IHyperlink> detectHyperlinks(org.eclipse.jface.text.ITextViewer textViewer, java.lang.String content, int index, int contentOffset) {
        java.util.List<org.eclipse.jface.text.hyperlink.IHyperlink> result = new java.util.ArrayList<org.eclipse.jface.text.hyperlink.IHyperlink>();
        org.eclipse.mylyn.tasks.core.TaskRepository currentRepository = getTaskRepository(textViewer);
        final org.eclipse.jface.text.hyperlink.IHyperlink[] currentRepositoryLinks = detectHyperlinks(currentRepository, content, index, contentOffset);
        if ((currentRepositoryLinks != null) && (currentRepositoryLinks.length > 0)) {
            result.addAll(java.util.Arrays.asList(currentRepositoryLinks));
            java.util.Set<org.eclipse.jface.text.Region> currentRepositoryRegions = new java.util.HashSet<org.eclipse.jface.text.Region>();
            for (org.eclipse.jface.text.hyperlink.IHyperlink link : currentRepositoryLinks) {
                currentRepositoryRegions.add(getRegion(link));
            }
            java.util.List<org.eclipse.mylyn.tasks.core.TaskRepository> otherRepositories = getTaskRepositories(textViewer);
            otherRepositories.remove(currentRepository);
            for (final org.eclipse.mylyn.tasks.core.TaskRepository repository : otherRepositories) {
                final org.eclipse.jface.text.hyperlink.IHyperlink[] links = detectHyperlinks(repository, content, index, contentOffset);
                if (links != null) {
                    for (org.eclipse.jface.text.hyperlink.IHyperlink link : links) {
                        // prevent highlighting text that is not already a link for the current repository
                        if (currentRepositoryRegions.contains(getRegion(link))) {
                            result.add(link);
                        }
                    }
                }
            }
        }
        if (result.isEmpty()) {
            return null;
        }
        return result;
    }

    protected org.eclipse.jface.text.Region getRegion(org.eclipse.jface.text.hyperlink.IHyperlink link) {
        if (link.getHyperlinkRegion() instanceof org.eclipse.jface.text.Region) {
            return ((org.eclipse.jface.text.Region) (link.getHyperlinkRegion()));
        } else {
            return new org.eclipse.jface.text.Region(link.getHyperlinkRegion().getOffset(), link.getHyperlinkRegion().getLength());
        }
    }

    @java.lang.Override
    protected java.util.List<org.eclipse.mylyn.tasks.core.TaskRepository> getTaskRepositories(org.eclipse.jface.text.ITextViewer textViewer) {
        return org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getAllRepositories();
    }
}