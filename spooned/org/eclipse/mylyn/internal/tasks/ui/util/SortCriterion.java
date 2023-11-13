/**
 * *****************************************************************************
 * Copyright (c) 2009, 2010 Frank Becker and others.
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
package org.eclipse.mylyn.internal.tasks.ui.util;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ui.IMemento;
/**
 *
 * @author Frank Becker
 * @author Steffen Pingel
 */
public class SortCriterion {
    public enum SortKey {

        NONE,
        DATE_CREATED,
        PRIORITY,
        RANK,
        SUMMARY,
        TASK_ID,
        TASK_TYPE,
        DUE_DATE,
        MODIFICATION_DATE,
        SCHEDULED_DATE;
        public static org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion.SortKey valueOfLabel(java.lang.String label) {
            for (org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion.SortKey value : org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion.SortKey.values()) {
                if (value.getLabel().equals(label)) {
                    return value;
                }
            }
            return null;
        }

        public java.lang.String getLabel() {
            switch (this) {
                case NONE :
                    return Messages.SortKindEntry_None;
                case PRIORITY :
                    return Messages.SortKindEntry_Priority;
                case SUMMARY :
                    return Messages.SortKindEntry_Summary;
                case RANK :
                    return Messages.SortKindEntry_Rank;
                case DATE_CREATED :
                    return Messages.SortKindEntry_Date_Created;
                case TASK_ID :
                    return Messages.SortKindEntry_Task_ID;
                case TASK_TYPE :
                    return Messages.SortCriterion_Type;
                case DUE_DATE :
                    return Messages.SortKindEntry_Due_Date;
                case MODIFICATION_DATE :
                    return Messages.SortCriterion_Modification_Date;
                case SCHEDULED_DATE :
                    return Messages.SortCriterion_Scheduled_Date;
                default :
                    return null;
            }
        }
    }

    private static final java.lang.String MEMENTO_KEY_SORT_KEY = "sortKey";// $NON-NLS-1$


    private static final java.lang.String MEMENTO_KEY_SORT_DIRECTION = "sortDirection";// $NON-NLS-1$


    private static final org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion.SortKey DEFAULT_SORT_KIND = org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion.SortKey.NONE;

    public static final int ASCENDING = 1;

    public static final int DESCENDING = -1;

    private static final int DEFAULT_SORT_DIRECTION = org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion.ASCENDING;

    public static final int kindCount = org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion.SortKey.values().length - 1;

    private org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion.SortKey key;

    private int direction;

    public SortCriterion() {
        key = org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion.DEFAULT_SORT_KIND;
        direction = org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion.DEFAULT_SORT_DIRECTION;
    }

    public SortCriterion(org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion.SortKey kind, int sortDirection) {
        this.key = kind;
        this.direction = sortDirection;
    }

    public org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion.SortKey getKey() {
        return key;
    }

    public void setKey(org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion.SortKey kind) {
        org.eclipse.core.runtime.Assert.isNotNull(kind);
        this.key = kind;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int sortDirection) {
        org.eclipse.core.runtime.Assert.isTrue((sortDirection == (-1)) || (sortDirection == 1));
        this.direction = sortDirection;
    }

    private org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion.SortKey getSortKey(org.eclipse.ui.IMemento memento, java.lang.String key, org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion.SortKey defaultValue) {
        java.lang.String value = memento.getString(key);
        if (value != null) {
            try {
                return org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion.SortKey.valueOf(value);
            } catch (java.lang.IllegalArgumentException e) {
                // ignore
            }
        }
        return defaultValue;
    }

    private int getSortDirection(org.eclipse.ui.IMemento memento, java.lang.String key, int defaultValue) {
        java.lang.Integer value = memento.getInteger(key);
        if (value != null) {
            return value >= 0 ? 1 : -1;
        }
        return defaultValue;
    }

    public void restoreState(org.eclipse.ui.IMemento memento) {
        setKey(getSortKey(memento, org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion.MEMENTO_KEY_SORT_KEY, org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion.DEFAULT_SORT_KIND));
        setDirection(getSortDirection(memento, org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion.MEMENTO_KEY_SORT_DIRECTION, org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion.DEFAULT_SORT_DIRECTION));
    }

    public void saveState(org.eclipse.ui.IMemento memento) {
        memento.putString(org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion.MEMENTO_KEY_SORT_KEY, getKey().name());
        memento.putInteger(org.eclipse.mylyn.internal.tasks.ui.util.SortCriterion.MEMENTO_KEY_SORT_DIRECTION, getDirection());
    }
}