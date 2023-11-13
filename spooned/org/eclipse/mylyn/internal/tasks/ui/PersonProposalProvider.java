/**
 * *****************************************************************************
 * Copyright (c) 2004, 2015 Tasktop Technologies and others.
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
package org.eclipse.mylyn.internal.tasks.ui;
import com.google.common.base.Strings;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMetaData;
import org.eclipse.mylyn.tasks.core.data.TaskCommentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.osgi.util.NLS;
/**
 *
 * @author Shawn Minto
 * @author Eugene Kuleshov
 * @author Steffen Pingel
 * @author David Shepherd
 * @author Sam Davis
 * @author Thomas Ehrnhoefer
 * @author Mike Wu
 */
public class PersonProposalProvider implements org.eclipse.jface.fieldassist.IContentProposalProvider {
    private final org.eclipse.mylyn.internal.tasks.core.AbstractTask currentTask;

    private java.lang.String currentUser;

    private java.util.SortedSet<java.lang.String> addressSet;

    private java.lang.String repositoryUrl;

    private java.lang.String connectorKind;

    private org.eclipse.mylyn.tasks.core.data.TaskData currentTaskData;

    private final java.util.Map<java.lang.String, java.lang.String> proposals;

    private java.util.Map<java.lang.String, java.lang.String> errorProposals;

    public PersonProposalProvider(org.eclipse.mylyn.internal.tasks.core.AbstractTask task, org.eclipse.mylyn.tasks.core.data.TaskData taskData) {
        this(task, taskData, new java.util.HashMap<java.lang.String, java.lang.String>(0));
    }

    public PersonProposalProvider(org.eclipse.mylyn.internal.tasks.core.AbstractTask task, org.eclipse.mylyn.tasks.core.data.TaskData taskData, java.util.Map<java.lang.String, java.lang.String> proposals) {
        this.currentTask = task;
        this.currentTaskData = taskData;
        if (task != null) {
            repositoryUrl = task.getRepositoryUrl();
            connectorKind = task.getConnectorKind();
        } else if (taskData != null) {
            repositoryUrl = taskData.getRepositoryUrl();
            connectorKind = taskData.getConnectorKind();
        }
        this.proposals = new java.util.HashMap<java.lang.String, java.lang.String>(proposals);
    }

    public PersonProposalProvider(java.lang.String repositoryUrl, java.lang.String repositoryKind) {
        this(repositoryUrl, repositoryKind, new java.util.HashMap<java.lang.String, java.lang.String>(0));
    }

    public PersonProposalProvider(java.lang.String repositoryUrl, java.lang.String repositoryKind, java.util.Map<java.lang.String, java.lang.String> proposals) {
        this.currentTask = null;
        this.repositoryUrl = repositoryUrl;
        this.connectorKind = repositoryKind;
        this.proposals = new java.util.HashMap<java.lang.String, java.lang.String>(proposals);
    }

    protected java.lang.String getRepositoryUrl() {
        return repositoryUrl;
    }

    protected java.lang.String getConnectorKind() {
        return connectorKind;
    }

    public org.eclipse.jface.fieldassist.IContentProposal[] getProposals(java.lang.String contents, int position) {
        org.eclipse.core.runtime.Assert.isLegal(contents != null);
        org.eclipse.core.runtime.Assert.isLegal(position >= 0);
        int leftSeparator = getIndexOfLeftSeparator(contents, position);
        int rightSeparator = getIndexOfRightSeparator(contents, position);
        assert leftSeparator <= position;
        assert position <= rightSeparator;
        java.lang.String searchText = contents.substring(leftSeparator + 1, position);
        java.lang.String resultPrefix = contents.substring(0, leftSeparator + 1);
        java.lang.String resultPostfix = contents.substring(rightSeparator);
        // retrieve subset of the tree set using key range
        java.util.SortedSet<java.lang.String> addressSet = getAddressSet();
        if ((errorProposals == null) || errorProposals.isEmpty()) {
            if (!searchText.equals("")) {
                // $NON-NLS-1$
                // lower bounds
                searchText = searchText.toLowerCase();
                // compute the upper bound
                char[] nextWord = searchText.toCharArray();
                nextWord[searchText.length() - 1]++;
                // filter matching keys
                addressSet = new java.util.TreeSet<java.lang.String>(addressSet.subSet(searchText, new java.lang.String(nextWord)));
                // add matching keys based on pretty names
                addMatchingProposalsByPrettyName(addressSet, searchText);
            }
        }
        org.eclipse.jface.fieldassist.IContentProposal[] result = new org.eclipse.jface.fieldassist.IContentProposal[addressSet.size()];
        int i = 0;
        for (final java.lang.String address : addressSet) {
            result[i++] = createPersonProposal(address, address.equalsIgnoreCase(currentUser), (resultPrefix + address) + resultPostfix, resultPrefix.length() + address.length());
        }
        java.util.Arrays.sort(result);
        return result;
    }

    private void addMatchingProposalsByPrettyName(java.util.SortedSet<java.lang.String> addressSet, java.lang.String searchText) {
        if (proposals.size() > 0) {
            for (java.util.Map.Entry<java.lang.String, java.lang.String> entry : proposals.entrySet()) {
                if (matchesSubstring(entry.getValue(), searchText)) {
                    addressSet.add(entry.getKey());
                }
            }
        }
    }

    private boolean matchesSubstring(java.lang.String value, java.lang.String searchText) {
        if (value != null) {
            java.lang.String tokens[] = value.split("\\s");// $NON-NLS-1$

            for (java.lang.String token : tokens) {
                if (token.toLowerCase().startsWith(searchText)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected org.eclipse.mylyn.internal.tasks.ui.PersonContentProposal createPersonProposal(java.lang.String address, boolean isCurrentUser, java.lang.String replacementText, int cursorPosition) {
        return new org.eclipse.mylyn.internal.tasks.ui.PersonContentProposal(getPrettyName(address), isCurrentUser, replacementText, cursorPosition);
    }

    protected java.lang.String getPrettyName(java.lang.String address) {
        java.lang.String value = proposals.get(address);
        if (value != null) {
            return org.eclipse.osgi.util.NLS.bind("{0} <{1}>", value, address);// $NON-NLS-1$

        }
        return address;
    }

    private int getIndexOfLeftSeparator(java.lang.String contents, int position) {
        int i = contents.lastIndexOf(' ', position - 1);
        i = java.lang.Math.max(contents.lastIndexOf(',', position - 1), i);
        return i;
    }

    private int getIndexOfRightSeparator(java.lang.String contents, int position) {
        int index = contents.length();
        int i = contents.indexOf(' ', position);
        if (i != (-1)) {
            index = java.lang.Math.min(i, index);
        }
        i = contents.indexOf(',', position);
        if (i != (-1)) {
            index = java.lang.Math.min(i, index);
        }
        return index;
    }

    private java.util.SortedSet<java.lang.String> getAddressSet() {
        if (addressSet != null) {
            return addressSet;
        }
        addressSet = new java.util.TreeSet<java.lang.String>(java.lang.String.CASE_INSENSITIVE_ORDER);
        if ((errorProposals != null) && (!errorProposals.isEmpty())) {
            for (java.lang.String proposal : errorProposals.keySet()) {
                addAddress(addressSet, proposal);
            }
            return addressSet;
        }
        if (proposals.size() > 0) {
            if ((repositoryUrl != null) && (connectorKind != null)) {
                currentUser = getCurrentUser(repositoryUrl, connectorKind);
            }
            for (java.lang.String proposal : proposals.keySet()) {
                addAddress(addressSet, proposal);
            }
            return addressSet;
        }
        if (currentTask != null) {
            addAddress(addressSet, currentTask.getOwner());
        }
        if (currentTaskData != null) {
            addAddresses(currentTaskData, addressSet);
        }
        if ((repositoryUrl != null) && (connectorKind != null)) {
            java.util.Set<org.eclipse.mylyn.internal.tasks.core.AbstractTask> tasks = new java.util.HashSet<org.eclipse.mylyn.internal.tasks.core.AbstractTask>();
            if (currentTask != null) {
                tasks.add(currentTask);
            }
            currentUser = getCurrentUser(repositoryUrl, connectorKind);
            if (currentUser != null) {
                addressSet.add(currentUser);
            }
            java.util.Collection<org.eclipse.mylyn.internal.tasks.core.AbstractTask> allTasks = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().getAllTasks();
            for (org.eclipse.mylyn.internal.tasks.core.AbstractTask task : allTasks) {
                if (repositoryUrl.equals(task.getRepositoryUrl())) {
                    tasks.add(task);
                }
            }
            for (org.eclipse.mylyn.tasks.core.ITask task : tasks) {
                addAddresses(task, addressSet);
            }
        }
        return addressSet;
    }

    private java.lang.String getCurrentUser(java.lang.String repositoryUrl, java.lang.String connectorKind) {
        org.eclipse.mylyn.tasks.core.TaskRepository repository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(connectorKind, repositoryUrl);
        if (repository != null) {
            org.eclipse.mylyn.commons.net.AuthenticationCredentials credentials = repository.getCredentials(org.eclipse.mylyn.commons.net.AuthenticationType.REPOSITORY);
            if ((credentials != null) && (credentials.getUserName().length() > 0)) {
                return credentials.getUserName();
            }
        }
        return null;
    }

    private void addAddresses(org.eclipse.mylyn.tasks.core.ITask task, java.util.Set<java.lang.String> addressSet) {
        if (com.google.common.base.Strings.isNullOrEmpty(task.getOwnerId())) {
            addAddress(addressSet, task.getOwner());
        } else {
            addAddress(addressSet, task.getOwnerId());
            if (!com.google.common.base.Strings.isNullOrEmpty(task.getOwner())) {
                proposals.put(task.getOwnerId(), task.getOwner());
            }
        }
    }

    private void addAddresses(org.eclipse.mylyn.tasks.core.data.TaskData data, java.util.Set<java.lang.String> addressSet) {
        addPerson(data, addressSet, org.eclipse.mylyn.tasks.core.data.TaskAttribute.USER_REPORTER);
        addPerson(data, addressSet, org.eclipse.mylyn.tasks.core.data.TaskAttribute.USER_ASSIGNED);
        addPerson(data, addressSet, org.eclipse.mylyn.tasks.core.data.TaskAttribute.USER_CC);
        java.util.List<org.eclipse.mylyn.tasks.core.data.TaskAttribute> comments = data.getAttributeMapper().getAttributesByType(data, org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_COMMENT);
        for (org.eclipse.mylyn.tasks.core.data.TaskAttribute commentAttribute : comments) {
            addPerson(data, addressSet, commentAttribute);
        }
        java.util.List<org.eclipse.mylyn.tasks.core.data.TaskAttribute> attachments = data.getAttributeMapper().getAttributesByType(data, org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_ATTACHMENT);
        for (org.eclipse.mylyn.tasks.core.data.TaskAttribute attachmentAttribute : attachments) {
            addPerson(data, addressSet, attachmentAttribute);
        }
    }

    private void addPerson(org.eclipse.mylyn.tasks.core.data.TaskData data, java.util.Set<java.lang.String> addresses, java.lang.String key) {
        org.eclipse.mylyn.tasks.core.data.TaskAttribute attribute = data.getRoot().getMappedAttribute(key);
        // ignore modifiable attributes which may have a value edited by the user which may not be a valid proposal
        if ((attribute != null) && attribute.getMetaData().isReadOnly()) {
            addPerson(data, addresses, attribute);
        }
    }

    private void addPerson(org.eclipse.mylyn.tasks.core.data.TaskData data, java.util.Set<java.lang.String> addresses, org.eclipse.mylyn.tasks.core.data.TaskAttribute attribute) {
        org.eclipse.mylyn.tasks.core.data.TaskAttributeMetaData metaData = attribute.getMetaData();
        if (org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_COMMENT.equals(metaData.getType())) {
            org.eclipse.mylyn.tasks.core.data.TaskCommentMapper mapper = org.eclipse.mylyn.tasks.core.data.TaskCommentMapper.createFrom(attribute);
            addPerson(addresses, mapper.getAuthor());
        } else if (org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_ATTACHMENT.equals(metaData.getType())) {
            org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper mapper = org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper.createFrom(attribute);
            addPerson(addresses, mapper.getAuthor());
        } else if (org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_PERSON.equals(metaData.getType())) {
            addPerson(addresses, data.getAttributeMapper().getRepositoryPerson(attribute));
        } else {
            java.util.List<java.lang.String> values = attribute.getValues();
            for (java.lang.String value : values) {
                addAddress(addresses, value);
            }
        }
    }

    private void addPerson(java.util.Set<java.lang.String> addresses, org.eclipse.mylyn.tasks.core.IRepositoryPerson repositoryPerson) {
        if (repositoryPerson != null) {
            addresses.add(repositoryPerson.getPersonId());
            if (repositoryPerson.getName() != null) {
                proposals.put(repositoryPerson.getPersonId(), repositoryPerson.getName());
            }
        }
    }

    private void addAddress(java.util.Set<java.lang.String> addresses, java.lang.String address) {
        if ((address != null) && (address.trim().length() > 0)) {
            addresses.add(address.trim());
        }
    }

    public java.util.Map<java.lang.String, java.lang.String> getProposals() {
        return proposals;
    }

    public java.util.Map<java.lang.String, java.lang.String> getErrorProposals() {
        return errorProposals;
    }

    public void setErrorProposals(java.util.Map<java.lang.String, java.lang.String> errorProposals) {
        this.errorProposals = errorProposals;
        addressSet = null;
    }
}