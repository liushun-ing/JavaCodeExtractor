/**
 * *****************************************************************************
 * Copyright (c) 2015 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.provisional.tasks.ui.wizards;
import org.eclipse.core.runtime.Assert;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMetaData;
import org.eclipse.mylyn.tasks.core.data.TaskData;
@java.lang.SuppressWarnings("nls")
public class AbstractQueryPageSchema {
    public static class Field {
        private java.util.EnumSet<org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Flag> flags;

        private final java.lang.String key;

        private final java.lang.String label;

        private final java.lang.String type;

        private final java.lang.String indexKey;

        private final java.lang.String dependsOn;

        private final java.lang.Integer layoutPriority;

        protected Field(java.lang.String key, java.lang.String label, java.lang.String type) {
            this(key, label, type, null, ((org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Flag[]) (null)));
        }

        protected Field(java.lang.String key, java.lang.String label, java.lang.String type, org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Flag... flags) {
            this(key, label, type, null, flags);
        }

        /**
         *
         * @param key
         * 		the task attribute key, which may be a common task attribute key defined in defined in
         * 		{@link TaskAttribute}
         * @param label
         * 		the user-visible label that is used by the user to identify this field
         * @param type
         * 		the type of the field, should be one of the constants defined in TaskAttribute (
         * 		<code>TaskAttribute.TYPE_*</code>)
         * @param indexKey
         * 		the index key, or null if this should not be indexed
         * @param flags
         * 		the flags, or null
         */
        public Field(java.lang.String key, java.lang.String label, java.lang.String type, java.lang.String indexKey, org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Flag... flags) {
            this(key, label, type, indexKey, null, null, flags);
        }

        /**
         *
         * @param key
         * 		the task attribute key, which may be a common task attribute key defined in defined in
         * 		{@link TaskAttribute}
         * @param label
         * 		the user-visible label that is used by the user to identify this field
         * @param type
         * 		the type of the field, should be one of the constants defined in TaskAttribute (
         * 		<code>TaskAttribute.TYPE_*</code>)
         * @param indexKey
         * 		the index key, or null if this should not be indexed
         * @param layoutPriority
         * 		the layout priority, or null if this should not be used
         * @param flags
         * 		the flags, or null
         */
        public Field(java.lang.String key, java.lang.String label, java.lang.String type, java.lang.String indexKey, java.lang.Integer layoutPriority, java.lang.String dependsOn, org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Flag... flags) {
            org.eclipse.core.runtime.Assert.isNotNull(key);
            org.eclipse.core.runtime.Assert.isNotNull(label);
            org.eclipse.core.runtime.Assert.isNotNull(type);
            this.key = key;
            this.label = label;
            this.type = type;
            this.indexKey = indexKey;
            this.layoutPriority = layoutPriority;
            this.dependsOn = dependsOn;
            if ((flags == null) || (flags.length == 0)) {
                this.flags = java.util.EnumSet.noneOf(org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Flag.class);
            } else {
                this.flags = java.util.EnumSet.copyOf(java.util.Arrays.asList(flags));
            }
        }

        public org.eclipse.mylyn.tasks.core.data.TaskAttribute createAttribute(org.eclipse.mylyn.tasks.core.data.TaskAttribute parent) {
            org.eclipse.mylyn.tasks.core.data.TaskAttribute attribute = parent.createMappedAttribute(getKey());
            // meta data
            org.eclipse.mylyn.tasks.core.data.TaskAttributeMetaData metaData = attribute.getMetaData();
            metaData.setLabel(getLabel());
            metaData.setType(getType());
            metaData.setReadOnly(isReadOnly());
            metaData.setKind(getKind());
            metaData.setRequired(isRequired());
            if (getLayoutPriority() != null) {
                metaData.putValue("LayoutPriority", getLayoutPriority().toString());
            }
            if (getDependsOn() != null) {
                metaData.setDependsOn(getDependsOn());
            }
            // options
            java.util.Map<java.lang.String, java.lang.String> options = getDefaultOptions();
            if (options != null) {
                for (java.util.Map.Entry<java.lang.String, java.lang.String> option : options.entrySet()) {
                    attribute.putOption(option.getKey(), option.getValue());
                }
            }
            return attribute;
        }

        public java.util.Map<java.lang.String, java.lang.String> getDefaultOptions() {
            return java.util.Collections.emptyMap();
        }

        public java.lang.String getKey() {
            return key;
        }

        /**
         * the key to use when indexing this field
         *
         * @return the index key, or null if this should not be indexed
         */
        public java.lang.String getIndexKey() {
            return indexKey;
        }

        public java.lang.String getKind() {
            if (flags.contains(org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Flag.ATTRIBUTE)) {
                return org.eclipse.mylyn.tasks.core.data.TaskAttribute.KIND_DEFAULT;
            } else if (flags.contains(org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Flag.PEOPLE)) {
                return org.eclipse.mylyn.tasks.core.data.TaskAttribute.KIND_PEOPLE;
            } else if (flags.contains(org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Flag.OPERATION)) {
                return org.eclipse.mylyn.tasks.core.data.TaskAttribute.KIND_OPERATION;
            } else if (flags.contains(org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Flag.DESCRIPTION)) {
                return org.eclipse.mylyn.tasks.core.data.TaskAttribute.KIND_DESCRIPTION;
            }
            return null;
        }

        public java.lang.String getLabel() {
            return label;
        }

        public java.lang.String getType() {
            return type;
        }

        public boolean isReadOnly() {
            return flags.contains(org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Flag.READ_ONLY);
        }

        @java.lang.Override
        public java.lang.String toString() {
            return getLabel();
        }

        @java.lang.Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + (indexKey == null ? 0 : indexKey.hashCode());
            result = (prime * result) + (key == null ? 0 : key.hashCode());
            return result;
        }

        @java.lang.Override
        public boolean equals(java.lang.Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Field other = ((org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Field) (obj));
            if (indexKey == null) {
                if (other.indexKey != null) {
                    return false;
                }
            } else if (!indexKey.equals(other.indexKey)) {
                return false;
            }
            if (key == null) {
                if (other.key != null) {
                    return false;
                }
            } else if (!key.equals(other.key)) {
                return false;
            }
            return true;
        }

        public boolean isRequired() {
            return flags.contains(org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Flag.REQUIRED);
        }

        java.lang.Integer getLayoutPriority() {
            return layoutPriority;
        }

        public boolean isQueryRequired() {
            return flags.contains(org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Flag.QUERY_REQUIRED);
        }

        public java.lang.String getDependsOn() {
            return dependsOn;
        }
    }

    public enum Flag {

        ATTRIBUTE,
        OPERATION,
        PEOPLE,
        READ_ONLY,
        /**
         * A flag used to indicate that the field is related to a description.
         *
         * @see TaskAttribute#KIND_DESCRIPTION
         */
        DESCRIPTION,
        /**
         * A flag used to indicate that the field is required.
         *
         * @see TaskAttribute#META_REQUIRED
         */
        REQUIRED,
        /**
         * A flag used to indicate that the field is required in the Query Page.
         *
         * @see TaskAttribute#META_QUERY_REQUIRED
         */
        QUERY_REQUIRED;}

    protected class FieldFactory {
        private java.util.EnumSet<org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Flag> flags;

        private java.lang.String key;

        private java.lang.String label;

        private java.lang.String type;

        private java.lang.Integer layoutPriority;

        private java.lang.String dependsOn;

        public FieldFactory(org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Field source) {
            this.flags = java.util.EnumSet.copyOf(source.flags);
            this.key = source.key;
            this.label = source.label;
            this.type = source.type;
            this.dependsOn = source.dependsOn;
        }

        public FieldFactory(org.eclipse.mylyn.tasks.core.data.AbstractTaskSchema.Field source) {
            this.flags = java.util.EnumSet.noneOf(org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Flag.class);
            this.key = source.getKey();
            this.label = source.getLabel();
            this.type = source.getType();
            this.dependsOn = source.getDependsOn();
        }

        public org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.FieldFactory addFlags(org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Flag... flags) {
            this.flags.addAll(java.util.Arrays.asList(flags));
            return this;
        }

        public org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Field create() {
            return createField(key, label, type, null, layoutPriority, dependsOn, !flags.isEmpty() ? flags.toArray(new org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Flag[0]) : null);
        }

        public org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.FieldFactory flags(org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Flag... flags) {
            this.flags = java.util.EnumSet.copyOf(java.util.Arrays.asList(flags));
            return this;
        }

        public org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.FieldFactory key(java.lang.String key) {
            this.key = key;
            return this;
        }

        public org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.FieldFactory label(java.lang.String label) {
            this.label = label;
            return this;
        }

        public org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.FieldFactory removeFlags(org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Flag... flags) {
            this.flags.removeAll(java.util.Arrays.asList(flags));
            return this;
        }

        public org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.FieldFactory type(java.lang.String type) {
            this.type = type;
            return this;
        }

        public org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.FieldFactory layoutPriority(java.lang.Integer layoutPriority) {
            this.layoutPriority = layoutPriority;
            return this;
        }

        public org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.FieldFactory dependsOn(java.lang.String dependsOn) {
            this.dependsOn = dependsOn;
            return this;
        }
    }

    private final java.util.Map<java.lang.String, org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Field> fieldByKey = new java.util.LinkedHashMap<java.lang.String, org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Field>();

    /**
     * Returns the specified field for the given key.
     */
    public org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Field getFieldByKey(java.lang.String taskKey) {
        return fieldByKey.get(taskKey);
    }

    /**
     * Creates no-value attributes with default options for the supplied task for each schema field.
     */
    public void initialize(org.eclipse.mylyn.tasks.core.data.TaskData taskData) {
        for (org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Field field : fieldByKey.values()) {
            field.createAttribute(taskData.getRoot());
        }
    }

    /**
     * Provides an iterator for all fields within the schema. Subsequent modifications to the returned collection are
     * not reflected to schema.
     *
     * @return all fields within the schema
     */
    public java.util.Collection<org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Field> getFields() {
        return new java.util.ArrayList<org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Field>(fieldByKey.values());
    }

    protected org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Field createField(java.lang.String key, java.lang.String label, java.lang.String type) {
        return createField(key, label, type, null, ((org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Flag[]) (null)));
    }

    protected org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Field createField(java.lang.String key, java.lang.String label, java.lang.String type, org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Flag... flags) {
        return createField(key, label, type, null, flags);
    }

    /**
     *
     * @see Field#Field(String, String, String, String, Flag...)
     */
    protected org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Field createField(java.lang.String key, java.lang.String label, java.lang.String type, java.lang.String indexKey, org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Flag... flags) {
        return createField(key, label, type, indexKey, null, flags);
    }

    /**
     *
     * @see Field#Field(String, String, String, String, Integer, Flag...)
     */
    protected org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Field createField(java.lang.String key, java.lang.String label, java.lang.String type, java.lang.String indexKey, java.lang.Integer layoutPriority, org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Flag... flags) {
        org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Field field = new org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Field(key, label, type, indexKey, layoutPriority, null, flags);
        fieldByKey.put(key, field);
        return field;
    }

    /**
     *
     * @see Field#Field(String, String, String, String, Integer, Flag...)
     */
    protected org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Field createField(java.lang.String key, java.lang.String label, java.lang.String type, java.lang.String indexKey, java.lang.Integer layoutPriority, java.lang.String dependsOn, org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Flag... flags) {
        org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Field field = new org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Field(key, label, type, indexKey, layoutPriority, dependsOn, flags);
        fieldByKey.put(key, field);
        return field;
    }

    protected org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.FieldFactory inheritFrom(org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.Field source) {
        return new org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.FieldFactory(source);
    }

    protected org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.FieldFactory copyFrom(org.eclipse.mylyn.tasks.core.data.AbstractTaskSchema.Field source) {
        return new org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema.FieldFactory(source);
    }
}