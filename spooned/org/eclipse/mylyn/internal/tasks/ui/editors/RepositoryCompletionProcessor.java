/**
 * *****************************************************************************
 * Copyright (c) 2004, 2011 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *     Tasktop Technologies - improvements
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.editors;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.LocalTask;
import org.eclipse.mylyn.internal.tasks.core.TaskActivationHistory;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.actions.CopyTaskDetailsAction;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TaskElementLabelProvider;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
/**
 *
 * @author Frank Becker
 * @author Steffen Pingel
 * @since 3.0
 */
public class RepositoryCompletionProcessor implements org.eclipse.jface.text.contentassist.IContentAssistProcessor {
    public static class TaskCompletionProposal implements org.eclipse.jface.text.contentassist.ICompletionProposal {
        private final org.eclipse.jface.viewers.LabelProvider labelProvider;

        private final org.eclipse.mylyn.tasks.core.TaskRepository repository;

        private final org.eclipse.mylyn.tasks.core.ITask task;

        private final int replacementOffset;

        private final int replacementLength;

        private java.lang.String replacement;

        private final java.lang.String defaultReplacement;

        private final boolean includePrefix;

        public TaskCompletionProposal(org.eclipse.mylyn.tasks.core.TaskRepository repository, org.eclipse.mylyn.tasks.core.ITask task, org.eclipse.jface.viewers.LabelProvider labelProvider, java.lang.String defaultReplacement, boolean includePrefix, int replacementOffset, int replacementLength) {
            this.labelProvider = labelProvider;
            this.repository = repository;
            this.task = task;
            this.defaultReplacement = defaultReplacement;
            this.includePrefix = includePrefix;
            this.replacementOffset = replacementOffset;
            this.replacementLength = replacementLength;
        }

        public void apply(org.eclipse.jface.text.IDocument document) {
            try {
                document.replace(replacementOffset, replacementLength, getReplacement());
            } catch (org.eclipse.jface.text.BadLocationException x) {
                // ignore
            }
        }

        public java.lang.String getReplacement() {
            if (replacement == null) {
                // add an absolute reference to the task if the viewer does not have a repository
                if (((defaultReplacement == null) || (repository == null)) || (!repository.getRepositoryUrl().equals(task.getRepositoryUrl()))) {
                    replacement = org.eclipse.mylyn.internal.tasks.ui.actions.CopyTaskDetailsAction.getTextForTask(task);
                } else if (includePrefix) {
                    replacement = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskPrefix(task.getConnectorKind()) + defaultReplacement;
                } else {
                    replacement = defaultReplacement;
                }
            }
            return replacement;
        }

        public java.lang.String getAdditionalProposalInfo() {
            return null;
        }

        public org.eclipse.jface.text.contentassist.IContextInformation getContextInformation() {
            return null;
        }

        public java.lang.String getDisplayString() {
            return labelProvider.getText(task);
        }

        public org.eclipse.swt.graphics.Image getImage() {
            return labelProvider.getImage(task);
        }

        public org.eclipse.swt.graphics.Point getSelection(org.eclipse.jface.text.IDocument document) {
            return new org.eclipse.swt.graphics.Point(replacementOffset + getReplacement().length(), 0);
        }

        public org.eclipse.mylyn.tasks.core.TaskRepository getRepository() {
            return repository;
        }

        public org.eclipse.mylyn.tasks.core.ITask getTask() {
            return task;
        }
    }

    private class ProposalComputer {
        public static final java.lang.String LABEL_SEPARATOR = " -------------------------------------------- ";// $NON-NLS-1$


        private final java.util.Set<org.eclipse.mylyn.tasks.core.ITask> addedTasks = new java.util.HashSet<org.eclipse.mylyn.tasks.core.ITask>();

        private boolean addSeparator;

        private final int offset;

        private final java.lang.String prefix;

        private final java.util.List<org.eclipse.jface.text.contentassist.ICompletionProposal> resultList = new java.util.ArrayList<org.eclipse.jface.text.contentassist.ICompletionProposal>();

        public ProposalComputer(org.eclipse.jface.text.ITextViewer viewer, int offset) {
            this.offset = offset;
            this.prefix = extractPrefix(viewer, offset).toLowerCase();
        }

        private void addProposal(org.eclipse.mylyn.tasks.core.ITask task, java.lang.String replacement, boolean includeTaskPrefix) {
            if (addSeparator) {
                if (!addedTasks.isEmpty()) {
                    resultList.add(createSeparator());
                }
                addSeparator = false;
            }
            resultList.add(new org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryCompletionProcessor.TaskCompletionProposal(repository, task, labelProvider, replacement, includeTaskPrefix, offset - prefix.length(), prefix.length()));
            addedTasks.add(task);
        }

        public void addSeparator() {
            addSeparator = true;
        }

        public void addTasks(java.util.List<org.eclipse.mylyn.internal.tasks.core.AbstractTask> tasks) {
            for (org.eclipse.mylyn.internal.tasks.core.AbstractTask task : tasks) {
                addTask(task);
            }
        }

        public void addTask(org.eclipse.mylyn.tasks.core.ITask task) {
            if (addedTasks.contains(task)) {
                return;
            }
            if (getNeverIncludePrefix() && (!task.getRepositoryUrl().equals(repository.getRepositoryUrl()))) {
                return;
            }
            java.lang.String taskKey = task.getTaskKey();
            if (prefix.length() == 0) {
                addProposal(task, taskKey, !getNeverIncludePrefix());
            } else if ((taskKey != null) && taskKey.startsWith(prefix)) {
                // don't include prefix if completing id since it was most likely already added
                addProposal(task, taskKey, false);
            } else if (containsPrefix(task)) {
                addProposal(task, taskKey, !getNeverIncludePrefix());
            }
        }

        private boolean containsPrefix(org.eclipse.mylyn.tasks.core.ITask task) {
            java.lang.String haystack = (org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskPrefix(task.getConnectorKind()) + " ") + labelProvider.getText(task);// $NON-NLS-1$

            java.lang.String[] haystackTokens = haystack.split("\\s");// $NON-NLS-1$

            java.lang.String[] needles = prefix.trim().split("\\*");// $NON-NLS-1$

            if ((haystackTokens.length == 0) || (needles.length == 0)) {
                return false;
            }
            // check if all needles are contained in haystack
            for (java.lang.String needle : needles) {
                boolean matched = false;
                haystack : for (java.lang.String haystackToken : haystackTokens) {
                    if (haystackToken.toLowerCase().startsWith(needle)) {
                        matched = true;
                        break haystack;
                    }
                }
                if (!matched) {
                    return false;
                }
            }
            return true;
        }

        private org.eclipse.jface.text.contentassist.CompletionProposal createSeparator() {
            return // $NON-NLS-1$
            new org.eclipse.jface.text.contentassist.CompletionProposal("", offset, 0, 0, org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.SEPARATOR_LIST), org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryCompletionProcessor.ProposalComputer.LABEL_SEPARATOR, null, null);
        }

        /**
         * Returns the prefix of the currently completed text. Assumes that any character that is not a line break or
         * white space can be part of a task id.
         */
        private java.lang.String extractPrefix(org.eclipse.jface.text.ITextViewer viewer, int offset) {
            int i = offset;
            org.eclipse.jface.text.IDocument document = viewer.getDocument();
            if (i > document.getLength()) {
                return "";// $NON-NLS-1$

            }
            try {
                while (i > 0) {
                    char c = document.getChar(i - 1);
                    if ((java.lang.Character.isWhitespace(c) || (c == '(')) || (c == ':')) {
                        break;
                    }
                    i--;
                } 
                if ((i == offset) && (repository != null)) {
                    // check if document contains "{prefix} "
                    java.lang.String taskPrefix = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskPrefix(repository.getConnectorKind());
                    if (taskPrefix.length() > 1) {
                        try {
                            if (taskPrefix.equals(document.get(offset - taskPrefix.length(), taskPrefix.length()))) {
                                return taskPrefix;
                            }
                        } catch (org.eclipse.jface.text.BadLocationException e) {
                            // ignore
                        }
                    }
                }
                return document.get(i, offset - i);
            } catch (org.eclipse.jface.text.BadLocationException e) {
                return "";// $NON-NLS-1$

            }
        }

        public void filterTasks(java.util.List<org.eclipse.mylyn.internal.tasks.core.AbstractTask> tasks) {
            for (java.util.Iterator<org.eclipse.mylyn.internal.tasks.core.AbstractTask> it = tasks.iterator(); it.hasNext();) {
                org.eclipse.mylyn.tasks.core.ITask task = it.next();
                if (!select(task)) {
                    it.remove();
                }
            }
        }

        private boolean select(org.eclipse.mylyn.tasks.core.ITask task) {
            return (!(task instanceof org.eclipse.mylyn.internal.tasks.core.LocalTask))// 
             && ((repository == null) || task.getRepositoryUrl().equals(repository.getRepositoryUrl()));
        }

        public org.eclipse.jface.text.contentassist.ICompletionProposal[] getResult() {
            return resultList.toArray(new org.eclipse.jface.text.contentassist.ICompletionProposal[resultList.size()]);
        }
    }

    private static final int MAX_OPEN_EDITORS = 10;

    private static final int MAX_ACTIVATED_TASKS = 10;

    private final org.eclipse.mylyn.tasks.ui.TaskElementLabelProvider labelProvider = new org.eclipse.mylyn.tasks.ui.TaskElementLabelProvider(false);

    private final org.eclipse.mylyn.tasks.core.TaskRepository repository;

    private boolean neverIncludePrefix;

    public RepositoryCompletionProcessor(org.eclipse.mylyn.tasks.core.TaskRepository taskRepository) {
        this.repository = taskRepository;
        this.neverIncludePrefix = false;
    }

    public boolean getNeverIncludePrefix() {
        return neverIncludePrefix;
    }

    public void setNeverIncludePrefix(boolean includePrefix) {
        this.neverIncludePrefix = includePrefix;
    }

    public org.eclipse.jface.text.contentassist.ICompletionProposal[] computeCompletionProposals(org.eclipse.jface.text.ITextViewer viewer, int offset) {
        org.eclipse.jface.text.ITextSelection selection = ((org.eclipse.jface.text.ITextSelection) (viewer.getSelectionProvider().getSelection()));
        // adjust offset to end of normalized selection
        if (selection.getOffset() == offset) {
            offset = selection.getOffset() + selection.getLength();
        }
        org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryCompletionProcessor.ProposalComputer proposalComputer = new org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryCompletionProcessor.ProposalComputer(viewer, offset);
        // add tasks from navigation history
        // IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        // if (window != null) {
        // IWorkbenchPage page = window.getActivePage();
        // if (page != null) {
        // INavigationHistory history = page.getNavigationHistory();
        // INavigationLocation[] locations = history.getLocations();
        // if (locations != null) {
        // for (INavigationLocation location : locations) {
        // // location is always null
        // }
        // }
        // }
        // }
        // add open editor
        org.eclipse.ui.IWorkbenchWindow window = org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            org.eclipse.ui.IWorkbenchPage page = window.getActivePage();
            if (page != null) {
                org.eclipse.ui.IEditorReference[] editorReferences = page.getEditorReferences();
                int count = 0;
                for (int i = editorReferences.length - 1; (i >= 0) && (count < org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryCompletionProcessor.MAX_OPEN_EDITORS); i--) {
                    try {
                        if (editorReferences[i].getEditorInput() instanceof org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput) {
                            org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput input = ((org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput) (editorReferences[i].getEditorInput()));
                            org.eclipse.mylyn.tasks.core.ITask task = input.getTask();
                            if ((task != null) && (!(task instanceof org.eclipse.mylyn.internal.tasks.core.LocalTask))) {
                                proposalComputer.addTask(task);
                                count++;
                            }
                        }
                    } catch (org.eclipse.ui.PartInitException e) {
                        // ignore
                    }
                }
            }
        }
        // add tasks from activation history
        org.eclipse.mylyn.internal.tasks.core.TaskActivationHistory taskHistory = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().getTaskActivationHistory();
        java.util.List<org.eclipse.mylyn.internal.tasks.core.AbstractTask> tasks = taskHistory.getPreviousTasks(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getContainersFromWorkingSet(org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater.getActiveWorkingSets(window)));
        int count = 0;
        for (int i = tasks.size() - 1; (i >= 0) && (count < org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryCompletionProcessor.MAX_ACTIVATED_TASKS); i--) {
            org.eclipse.mylyn.internal.tasks.core.AbstractTask task = tasks.get(i);
            if (!(task instanceof org.eclipse.mylyn.internal.tasks.core.LocalTask)) {
                proposalComputer.addTask(task);
            }
        }
        // add all remaining tasks for repository
        if (repository != null) {
            proposalComputer.addSeparator();
            org.eclipse.mylyn.internal.tasks.core.TaskList taskList = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList();
            tasks = new java.util.ArrayList<org.eclipse.mylyn.internal.tasks.core.AbstractTask>(taskList.getAllTasks());
            proposalComputer.filterTasks(tasks);
            java.util.Collections.sort(tasks, new java.util.Comparator<org.eclipse.mylyn.internal.tasks.core.AbstractTask>() {
                public int compare(org.eclipse.mylyn.internal.tasks.core.AbstractTask o1, org.eclipse.mylyn.internal.tasks.core.AbstractTask o2) {
                    return labelProvider.getText(o1).compareTo(labelProvider.getText(o2));
                }
            });
            proposalComputer.addTasks(tasks);
        }
        return proposalComputer.getResult();
    }

    public org.eclipse.jface.text.contentassist.IContextInformation[] computeContextInformation(org.eclipse.jface.text.ITextViewer viewer, int offset) {
        return null;
    }

    public char[] getCompletionProposalAutoActivationCharacters() {
        return null;
    }

    public char[] getContextInformationAutoActivationCharacters() {
        return null;
    }

    public org.eclipse.jface.text.contentassist.IContextInformationValidator getContextInformationValidator() {
        return null;
    }

    public java.lang.String getErrorMessage() {
        return null;
    }
}