/**
 * *****************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Eugene Kuleshov - improvements
 * *****************************************************************************
 */
package org.eclipse.mylyn.tasks.ui;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
/**
 * Base class for hyperlink detectors that provides methods for extracting text from an {@link ITextViewer}.
 *
 * @author Rob Elves
 * @author Steffen Pingel
 * @author Eugene Kuleshov
 * @author Terry Hon
 * @since 3.1
 */
public abstract class AbstractTaskHyperlinkDetector extends org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector {
    /**
     *
     * @since 3.1
     */
    public AbstractTaskHyperlinkDetector() {
    }

    /**
     *
     * @since 3.1
     */
    public org.eclipse.jface.text.hyperlink.IHyperlink[] detectHyperlinks(org.eclipse.jface.text.ITextViewer textViewer, final org.eclipse.jface.text.IRegion region, boolean canShowMultipleHyperlinks) {
        org.eclipse.jface.text.IDocument document = textViewer.getDocument();
        if ((document == null) || (document.getLength() == 0)) {
            return null;
        }
        java.lang.String content;
        int contentOffset;
        int index;
        try {
            if (region.getLength() == 0) {
                // expand the region to include the whole line
                org.eclipse.jface.text.IRegion lineInfo = document.getLineInformationOfOffset(region.getOffset());
                int lineLength = lineInfo.getLength();
                int lineOffset = lineInfo.getOffset();
                int lineEnd = lineOffset + lineLength;
                int regionEnd = region.getOffset() + region.getLength();
                if (lineOffset < region.getOffset()) {
                    int regionLength = java.lang.Math.max(regionEnd, lineEnd) - lineOffset;
                    contentOffset = lineOffset;
                    content = document.get(lineOffset, regionLength);
                    index = region.getOffset() - lineOffset;
                } else {
                    // the line starts after region, may never happen
                    int regionLength = java.lang.Math.max(regionEnd, lineEnd) - region.getOffset();
                    contentOffset = region.getOffset();
                    content = document.get(contentOffset, regionLength);
                    index = 0;
                }
            } else {
                content = document.get(region.getOffset(), region.getLength());
                contentOffset = region.getOffset();
                index = -1;
            }
        } catch (org.eclipse.jface.text.BadLocationException ex) {
            return null;
        }
        java.util.List<org.eclipse.jface.text.hyperlink.IHyperlink> hyperlinks = detectHyperlinks(textViewer, content, index, contentOffset);
        if (hyperlinks == null) {
            return null;
        }
        // filter hyperlinks that do not match original region
        if (region.getLength() == 0) {
            for (java.util.Iterator<org.eclipse.jface.text.hyperlink.IHyperlink> it = hyperlinks.iterator(); it.hasNext();) {
                org.eclipse.jface.text.hyperlink.IHyperlink hyperlink = it.next();
                org.eclipse.jface.text.IRegion hyperlinkRegion = hyperlink.getHyperlinkRegion();
                if (!isInRegion(region, hyperlinkRegion)) {
                    it.remove();
                }
            }
        }
        if (hyperlinks.isEmpty()) {
            return null;
        }
        return hyperlinks.toArray(new org.eclipse.jface.text.hyperlink.IHyperlink[hyperlinks.size()]);
    }

    /**
     *
     * @since 3.1
     */
    protected abstract java.util.List<org.eclipse.jface.text.hyperlink.IHyperlink> detectHyperlinks(org.eclipse.jface.text.ITextViewer textViewer, java.lang.String content, int index, int contentOffset);

    private boolean isInRegion(org.eclipse.jface.text.IRegion detectInRegion, org.eclipse.jface.text.IRegion hyperlinkRegion) {
        return (detectInRegion.getOffset() >= hyperlinkRegion.getOffset()) && (detectInRegion.getOffset() <= (hyperlinkRegion.getOffset() + hyperlinkRegion.getLength()));
    }

    /**
     *
     * @since 3.1
     */
    protected java.util.List<org.eclipse.mylyn.tasks.core.TaskRepository> getTaskRepositories(org.eclipse.jface.text.ITextViewer textViewer) {
        java.util.List<org.eclipse.mylyn.tasks.core.TaskRepository> repositories = new java.util.ArrayList<org.eclipse.mylyn.tasks.core.TaskRepository>();
        org.eclipse.mylyn.tasks.core.TaskRepository selectedRepository = getTaskRepository(textViewer);
        if (selectedRepository != null) {
            repositories.add(selectedRepository);
        } else {
            repositories.addAll(org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getAllRepositories());
        }
        return repositories;
    }

    /**
     *
     * @since 3.1
     */
    protected org.eclipse.mylyn.tasks.core.TaskRepository getTaskRepository(org.eclipse.jface.text.ITextViewer textViewer) {
        org.eclipse.mylyn.tasks.core.TaskRepository repository = ((org.eclipse.mylyn.tasks.core.TaskRepository) (getAdapter(org.eclipse.mylyn.tasks.core.TaskRepository.class)));
        if (repository != null) {
            return repository;
        }
        org.eclipse.core.resources.IResource resource = ((org.eclipse.core.resources.IResource) (getAdapter(org.eclipse.core.resources.IResource.class)));
        if (resource == null) {
            // use currently active editor (if any)
            org.eclipse.ui.IWorkbenchWindow window = org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if (window != null) {
                org.eclipse.ui.IWorkbenchPage activePage = window.getActivePage();
                if (activePage != null) {
                    org.eclipse.ui.IWorkbenchPart part = activePage.getActivePart();
                    if (part instanceof org.eclipse.ui.IEditorPart) {
                        org.eclipse.ui.IEditorInput input = ((org.eclipse.ui.IEditorPart) (part)).getEditorInput();
                        if (input != null) {
                            resource = ((org.eclipse.core.resources.IResource) (input.getAdapter(org.eclipse.core.resources.IResource.class)));
                        }
                    }
                }
            }
        }
        if (resource != null) {
            return org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getRepositoryForResource(resource);
        }
        return null;
    }
}