/**
 * *****************************************************************************
 * Copyright (c) 2004, 2011 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Frank Becker - improvements
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.editors;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlinkPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.mylyn.commons.ui.compatibility.CommonColors;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TaskHyperlinkPresenter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
/**
 *
 * @author Rob Elves
 * @author Steffen Pingel
 * @since 3.0
 */
public class RepositoryTextViewerConfiguration extends org.eclipse.ui.editors.text.TextSourceViewerConfiguration {
    public enum Mode {

        URL,
        TASK,
        TASK_RELATION,
        DEFAULT;}

    private static final java.lang.String ID_CONTEXT_EDITOR_TASK = "org.eclipse.mylyn.tasks.ui.TaskEditor";// $NON-NLS-1$


    private static final java.lang.String ID_CONTEXT_EDITOR_TEXT = "org.eclipse.ui.DefaultTextEditor";// $NON-NLS-1$


    private org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.RepositoryTextScanner scanner;

    private final boolean spellCheck;

    private final org.eclipse.mylyn.tasks.core.TaskRepository taskRepository;

    private org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode mode;

    private final org.eclipse.mylyn.tasks.core.ITask task;

    /**
     *
     * @deprecated use {@link #RepositoryTextViewerConfiguration(TaskRepository, ITask, boolean)} instead.
     */
    @java.lang.Deprecated
    public RepositoryTextViewerConfiguration(org.eclipse.mylyn.tasks.core.TaskRepository taskRepository, boolean spellCheck) {
        this(taskRepository, null, spellCheck);
    }

    public RepositoryTextViewerConfiguration(org.eclipse.mylyn.tasks.core.TaskRepository taskRepository, org.eclipse.mylyn.tasks.core.ITask task, boolean spellCheck) {
        super(org.eclipse.ui.editors.text.EditorsUI.getPreferenceStore());
        this.taskRepository = taskRepository;
        this.spellCheck = spellCheck;
        this.mode = org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode.DEFAULT;
        this.task = task;
    }

    public org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode getMode() {
        return mode;
    }

    public void setMode(org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode mode) {
        org.eclipse.core.runtime.Assert.isNotNull(mode);
        this.mode = mode;
    }

    @java.lang.Override
    public org.eclipse.jface.text.presentation.IPresentationReconciler getPresentationReconciler(org.eclipse.jface.text.source.ISourceViewer sourceViewer) {
        if (getMode() == org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode.DEFAULT) {
            org.eclipse.jface.text.presentation.PresentationReconciler reconciler = new org.eclipse.jface.text.presentation.PresentationReconciler();
            reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
            org.eclipse.jface.text.rules.DefaultDamagerRepairer dr = new org.eclipse.jface.text.rules.DefaultDamagerRepairer(getDefaultScanner());
            reconciler.setDamager(dr, org.eclipse.jface.text.IDocument.DEFAULT_CONTENT_TYPE);
            reconciler.setRepairer(dr, org.eclipse.jface.text.IDocument.DEFAULT_CONTENT_TYPE);
            return reconciler;
        }
        return super.getPresentationReconciler(sourceViewer);
    }

    private org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.RepositoryTextScanner getDefaultScanner() {
        if (scanner == null) {
            scanner = new org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.RepositoryTextScanner(getMode());
        }
        return scanner;
    }

    @java.lang.Override
    public org.eclipse.jface.text.hyperlink.IHyperlinkDetector[] getHyperlinkDetectors(org.eclipse.jface.text.source.ISourceViewer sourceViewer) {
        if ((mode == org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode.URL) || (mode == org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode.TASK_RELATION)) {
            return getDefaultHyperlinkDetectors(sourceViewer, mode);
        }
        return super.getHyperlinkDetectors(sourceViewer);
    }

    public org.eclipse.jface.text.hyperlink.IHyperlinkDetector[] getDefaultHyperlinkDetectors(org.eclipse.jface.text.source.ISourceViewer sourceViewer, org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode mode) {
        org.eclipse.jface.text.hyperlink.IHyperlinkDetector[] detectors;
        if (mode == org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode.URL) {
            detectors = new org.eclipse.jface.text.hyperlink.IHyperlinkDetector[]{ new org.eclipse.mylyn.internal.tasks.ui.editors.TaskUrlHyperlinkDetector() };
        } else if (mode == org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode.TASK) {
            detectors = new org.eclipse.jface.text.hyperlink.IHyperlinkDetector[]{ new org.eclipse.mylyn.internal.tasks.ui.editors.TaskHyperlinkDetector() };
        } else if (mode == org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode.TASK_RELATION) {
            detectors = new org.eclipse.jface.text.hyperlink.IHyperlinkDetector[]{ new org.eclipse.mylyn.internal.tasks.ui.editors.TaskRelationHyperlinkDetector() };
        } else {
            detectors = super.getHyperlinkDetectors(sourceViewer);
        }
        if (detectors != null) {
            org.eclipse.core.runtime.IAdaptable target = getDefaultHyperlinkTarget();
            for (org.eclipse.jface.text.hyperlink.IHyperlinkDetector hyperlinkDetector : detectors) {
                if (hyperlinkDetector instanceof org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector) {
                    ((org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector) (hyperlinkDetector)).setContext(target);
                }
            }
        }
        return detectors;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("rawtypes")
    protected java.util.Map getHyperlinkDetectorTargets(final org.eclipse.jface.text.source.ISourceViewer sourceViewer) {
        org.eclipse.core.runtime.IAdaptable context = getDefaultHyperlinkTarget();
        java.util.Map<java.lang.String, org.eclipse.core.runtime.IAdaptable> targets = new java.util.HashMap<java.lang.String, org.eclipse.core.runtime.IAdaptable>();
        targets.put(org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.ID_CONTEXT_EDITOR_TEXT, context);
        targets.put(org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.ID_CONTEXT_EDITOR_TASK, context);
        return targets;
    }

    @java.lang.SuppressWarnings("rawtypes")
    private org.eclipse.core.runtime.IAdaptable getDefaultHyperlinkTarget() {
        org.eclipse.core.runtime.IAdaptable context = new org.eclipse.core.runtime.IAdaptable() {
            public java.lang.Object getAdapter(java.lang.Class adapter) {
                if (adapter == org.eclipse.mylyn.tasks.core.TaskRepository.class) {
                    return getTaskRepository();
                } else if (adapter == org.eclipse.mylyn.tasks.core.ITask.class) {
                    return getTask();
                }
                return null;
            }
        };
        return context;
    }

    public org.eclipse.mylyn.tasks.core.TaskRepository getTaskRepository() {
        return taskRepository;
    }

    @java.lang.Override
    public org.eclipse.jface.text.hyperlink.IHyperlinkPresenter getHyperlinkPresenter(final org.eclipse.jface.text.source.ISourceViewer sourceViewer) {
        if (fPreferenceStore == null) {
            return new org.eclipse.mylyn.tasks.ui.TaskHyperlinkPresenter(new org.eclipse.swt.graphics.RGB(0, 0, 255));
        }
        return new org.eclipse.mylyn.tasks.ui.TaskHyperlinkPresenter(fPreferenceStore);
    }

    @java.lang.Override
    public int getHyperlinkStateMask(org.eclipse.jface.text.source.ISourceViewer sourceViewer) {
        return org.eclipse.swt.SWT.NONE;
    }

    @java.lang.Override
    public org.eclipse.jface.text.reconciler.IReconciler getReconciler(org.eclipse.jface.text.source.ISourceViewer sourceViewer) {
        if (spellCheck) {
            return super.getReconciler(sourceViewer);
        } else {
            return null;
        }
    }

    private static class RepositoryTextScanner extends org.eclipse.jface.text.rules.RuleBasedScanner {
        public RepositoryTextScanner(org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode mode) {
            org.eclipse.jface.text.rules.IToken quoteToken = new org.eclipse.jface.text.rules.Token(new org.eclipse.jface.text.TextAttribute(org.eclipse.mylyn.commons.ui.compatibility.CommonColors.TEXT_QUOTED));
            org.eclipse.jface.text.rules.IRule[] rules = new org.eclipse.jface.text.rules.IRule[1];
            org.eclipse.jface.text.rules.SingleLineRule quoteRule = new org.eclipse.jface.text.rules.SingleLineRule(">", null, quoteToken, ((char) (0)), true);// $NON-NLS-1$

            quoteRule.setColumnConstraint(0);
            rules[0] = quoteRule;
            setRules(rules);
        }
    }

    @java.lang.Override
    public org.eclipse.jface.text.contentassist.IContentAssistant getContentAssistant(org.eclipse.jface.text.source.ISourceViewer sourceViewer) {
        if (mode == org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode.URL) {
            return null;
        }
        org.eclipse.jface.text.contentassist.ContentAssistant assistant = new org.eclipse.jface.text.contentassist.ContentAssistant();
        org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryCompletionProcessor processor = new org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryCompletionProcessor(taskRepository);
        if (mode == org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode.TASK_RELATION) {
            processor.setNeverIncludePrefix(true);
        }
        assistant.setContentAssistProcessor(processor, org.eclipse.jface.text.IDocument.DEFAULT_CONTENT_TYPE);
        return assistant;
    }

    public org.eclipse.mylyn.tasks.core.ITask getTask() {
        return task;
    }
}