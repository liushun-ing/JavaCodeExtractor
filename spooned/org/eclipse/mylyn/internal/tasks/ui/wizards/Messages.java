/**
 * *****************************************************************************
 * Copyright (c) 2004, 2014 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.wizards;
import org.eclipse.osgi.util.NLS;
public class Messages extends org.eclipse.osgi.util.NLS {
    private static final java.lang.String BUNDLE_NAME = "org.eclipse.mylyn.internal.tasks.ui.wizards.messages";// $NON-NLS-1$


    static {
        // load message values from bundle file
        reloadMessages();
    }

    public static void reloadMessages() {
        org.eclipse.osgi.util.NLS.initializeMessages(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.BUNDLE_NAME, org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.class);
    }

    public static java.lang.String EditRepositoryWizard_Failed_to_refactor_repository_urls;

    public static java.lang.String EditRepositoryWizard_Properties_for_Task_Repository;

    public static java.lang.String AttachmentSourcePage__Clipboard_;

    public static java.lang.String AttachmentSourcePage__Screenshot_;

    public static java.lang.String AttachmentSourcePage_Browse_;

    public static java.lang.String AttachmentSourcePage_Cannot_locate_attachment_file;

    public static java.lang.String AttachmentSourcePage_Clipboard;

    public static java.lang.String AttachmentSourcePage_Clipboard_contains_an_unsupported_data;

    public static java.lang.String AttachmentSourcePage_Clipboard_supports_text_and_image_attachments_only;

    public static java.lang.String AttachmentSourcePage_File;

    public static java.lang.String AttachmentSourcePage_No_file_name;

    public static java.lang.String AttachmentSourcePage_Select_attachment_source;

    public static java.lang.String AttachmentSourcePage_Select_File_Dialog_Title;

    public static java.lang.String AttachmentSourcePage_Select_the_location_of_the_attachment;

    public static java.lang.String AttachmentSourcePage_Workspace;

    public static java.lang.String NewQueryWizard_New_Repository_Query;

    public static java.lang.String NewTaskWizard_New_Task;

    public static java.lang.String NewWebTaskPage_Create_via_Web_Browser;

    public static java.lang.String NewWebTaskPage_New_Task;

    public static java.lang.String NewWebTaskPage_Once_submitted_synchronize_queries_or_add_the_task_to_a_category;

    public static java.lang.String NewWebTaskPage_This_will_open_a_web_browser_that_can_be_used_to_create_a_new_task;

    public static java.lang.String AttachmentPreviewPage_A_preview_the_type_X_is_currently_not_available;

    public static java.lang.String AttachmentPreviewPage_Attachment_Preview;

    public static java.lang.String AttachmentPreviewPage_Could_not_create_preview;

    public static java.lang.String AttachmentPreviewPage_Preparing_preview;

    public static java.lang.String AttachmentPreviewPage_Review_the_attachment_before_submitting;

    public static java.lang.String AttachmentPreviewPage_Run_in_background;

    public static java.lang.String SelectRepositoryConnectorPage_discoveryProblemMessage;

    public static java.lang.String SelectRepositoryConnectorPage_discoveryProblemTitle;

    public static java.lang.String SelectRepositoryConnectorPage_activateDiscovery;

    public static java.lang.String SelectRepositoryConnectorPage_Select_a_task_repository_type;

    public static java.lang.String SelectRepositoryConnectorPage_You_can_connect_to_an_existing_account_using_one_of_the_installed_connectors;

    public static java.lang.String SelectRepositoryPage_Add_new_repositories_using_the_X_view;

    public static java.lang.String SelectRepositoryPage_Select_a_repository;

    public static java.lang.String TaskAttachmentWizard_Add_Attachment;

    public static java.lang.String TaskAttachmentWizard_Attach_Screenshot;

    public static java.lang.String TaskAttachmentWizard_Attaching_context;

    public static java.lang.String TaskAttachmentWizard_Attachment_Failed;

    public static java.lang.String TaskAttachmentWizard_Screenshot;

    public static java.lang.String TaskDataExportWizard_Export;

    public static java.lang.String TaskDataExportWizard_export_failed;

    public static java.lang.String TaskDataExportWizardPage_Browse_;

    public static java.lang.String TaskDataExportWizardPage_Export_Mylyn_Task_Data;

    public static java.lang.String TaskDataExportWizardPage_File;

    public static java.lang.String TaskDataExportWizardPage_Folder;

    public static java.lang.String TaskDataExportWizardPage_Folder_Selection;

    public static java.lang.String TaskDataExportWizardPage_Please_choose_an_export_destination;

    public static java.lang.String TaskDataExportWizardPage_Specify_the_destination_folder_for_task_data;

    public static java.lang.String TaskDataImportWizard_confirm_overwrite;

    public static java.lang.String TaskDataImportWizard_could_not_be_found;

    public static java.lang.String TaskDataImportWizard_existing_task_data_about_to_be_erased_proceed;

    public static java.lang.String TaskDataImportWizard_File_not_found;

    public static java.lang.String TaskDataImportWizard_Import;

    public static java.lang.String TaskDataImportWizard_Import_Error;

    public static java.lang.String TaskDataImportWizard_Importing_Data;

    public static java.lang.String TaskDataImportWizard_task_data_import_failed;

    public static java.lang.String TaskDataImportWizardPage_Restore_tasks_from_history;

    public static java.lang.String TaskDataImportWizardPage_Browse_;

    public static java.lang.String TaskDataImportWizardPage_From_snapshot;

    public static java.lang.String TaskDataImportWizardPage_From_zip_file;

    public static java.lang.String TaskDataImportWizardPage_Import_method_backup;

    public static java.lang.String TaskDataImportWizardPage_Import_method_zip;

    public static java.lang.String TaskDataImportWizardPage_Import_Settings_saved;

    public static java.lang.String TaskDataImportWizardPage_Import_Source_zip_file_setting;

    public static java.lang.String TaskDataImportWizardPage_Importing_overwrites_current_tasks_and_repositories;

    public static java.lang.String TaskDataImportWizardPage__unspecified_;

    public static java.lang.String TaskDataImportWizardPage_Zip_File_Selection;

    public static java.lang.String AbstractRepositoryQueryPage_A_category_with_this_name_already_exists;

    public static java.lang.String AbstractRepositoryQueryPage_Enter_query_parameters;

    public static java.lang.String AbstractRepositoryQueryPage_If_attributes_are_blank_or_stale_press_the_Update_button;

    public static java.lang.String AbstractRepositoryQueryPage_Please_specify_a_title_for_the_query;

    public static java.lang.String AbstractRepositoryQueryPage_A_query_with_this_name_already_exists;

    public static java.lang.String AbstractRepositorySettingsPage_A_repository_with_this_name_already_exists;

    public static java.lang.String AbstractRepositorySettingsPage_Additional_Settings;

    public static java.lang.String AbstractRepositorySettingsPage_Anonymous_Access;

    public static java.lang.String AbstractRepositorySettingsPage_Authentication_credentials_are_valid;

    public static java.lang.String AbstractRepositorySettingsPage_Change_account_settings;

    public static java.lang.String AbstractRepositorySettingsPage_Change_Settings;

    public static java.lang.String AbstractRepositorySettingsPage_Character_encoding;

    public static java.lang.String AbstractRepositorySettingsPage_Create_new_account;

    public static java.lang.String AbstractRepositorySettingsPage_Default__;

    public static java.lang.String AbstractRepositorySettingsPage_Disconnected;

    public static java.lang.String AbstractRepositorySettingsPage_Enable_http_authentication;

    public static java.lang.String AbstractRepositorySettingsPage_Enable_proxy_authentication;

    public static java.lang.String AbstractRepositorySettingsPage_Enter_a_user_id_Message0;

    public static java.lang.String AbstractRepositorySettingsPage_Enter_a_valid_server_url;

    public static java.lang.String AbstractRepositorySettingsPage_Http_Authentication;

    public static java.lang.String AbstractRepositorySettingsPage_Internal_error_validating_repository;

    public static java.lang.String AbstractRepositorySettingsPage_Label_;

    public static java.lang.String AbstractRepositorySettingsPage_Other;

    public static java.lang.String AbstractRepositorySettingsPage_Password_;

    public static java.lang.String AbstractRepositorySettingsPage_certificate_settings;

    public static java.lang.String AbstractRepositorySettingsPage_Enable_certificate_authentification;

    public static java.lang.String AbstractRepositorySettingsPage_CertificateFile_;

    public static java.lang.String AbstractRepositorySettingsPage_ChooseCertificateFile_;

    public static java.lang.String AbstractRepositorySettingsPage_CertificatePassword_;

    public static java.lang.String AbstractRepositorySettingsPage_Problems_encountered_determining_available_charsets;

    public static java.lang.String AbstractRepositorySettingsPage_Proxy_host_address_;

    public static java.lang.String AbstractRepositorySettingsPage_Proxy_host_port_;

    public static java.lang.String AbstractRepositorySettingsPage_Proxy_Server_Configuration;

    public static java.lang.String AbstractRepositorySettingsPage_Repository_already_exists;

    public static java.lang.String AbstractRepositorySettingsPage_Repository_is_valid;

    public static java.lang.String AbstractRepositorySettingsPage_Repository_url_is_invalid;

    public static java.lang.String AbstractRepositorySettingsPage_Save_Password;

    public static java.lang.String AbstractRepositorySettingsPage_Server_;

    public static java.lang.String AbstractRepositorySettingsPage_Settings_are_valid_version;

    public static java.lang.String AbstractRepositorySettingsPage_Unable_to_authenticate_with_repository;

    public static java.lang.String AbstractRepositorySettingsPage_Use_global_Network_Connections_preferences;

    public static java.lang.String AbstractRepositorySettingsPage_User_ID_;

    public static java.lang.String AbstractRepositorySettingsPage_Validate_on_Finish;

    public static java.lang.String AbstractRepositorySettingsPage_Validate_Settings;

    public static java.lang.String AbstractRepositorySettingsPage_Validate_Settings_Button_Label;

    public static java.lang.String AbstractRepositorySettingsPage_Validating_server_settings;

    public static java.lang.String NewTaskWizard_Create_Task;

    public static java.lang.String NewTaskWizard_Error_creating_new_task;

    public static java.lang.String NewTaskWizard_Failed_to_create_new_task_;

    public static java.lang.String NewWebTaskWizard_New_Task;

    public static java.lang.String NewWebTaskWizard_This_connector_does_not_provide_a_rich_task_editor_for_creating_tasks;

    public static java.lang.String RepositoryQueryWizard_Edit_Repository_Query;

    public static java.lang.String TaskAttachmentPage_ATTACHE_CONTEXT;

    public static java.lang.String TaskAttachmentPage_Attachment_Details;

    public static java.lang.String TaskAttachmentPage_Comment;

    public static java.lang.String TaskAttachmentPage_Content_Type;

    public static java.lang.String TaskAttachmentPage_Description;

    public static java.lang.String TaskAttachmentPage_Enter_a_description;

    public static java.lang.String TaskAttachmentPage_Enter_a_file_name;

    public static java.lang.String TaskAttachmentPage_File;

    public static java.lang.String TaskAttachmentPage_Patch;

    public static java.lang.String TaskAttachmentPage_Replace_existing_attachment_Label;

    public static java.lang.String TaskAttachmentPage_Verify_the_content_type_of_the_attachment;

    public static java.lang.String AbstractTaskRepositoryPage_Validation_failed;

    public static java.lang.String LocalRepositorySettingsPage_Configure_the_local_repository;

    public static java.lang.String LocalRepositorySettingsPage_Local_Repository_Settings;

    public static java.lang.String QueryWizardDialog_Clear_Fields;

    public static java.lang.String QueryWizardDialog_Update_Attributes_from_Repository;

    public static java.lang.String AbstractRepositoryQueryPage2__Refresh_From_Repository;

    public static java.lang.String AbstractRepositoryQueryPage2__Title_;

    public static java.lang.String AbstractRepositoryQueryPage2_Clear_Fields;

    public static java.lang.String AbstractRepositoryQueryPage2_Create_a_Query_Page_Description;

    public static java.lang.String AbstractRepositoryQueryPage2_Enter_a_title;

    public static java.lang.String AbstractRepositoryQueryPage2_Enter_query_parameters;

    public static java.lang.String AbstractRepositoryQueryPage2_No_repository_available_please_add_one_using_the_Task_Repositories_view;

    public static java.lang.String AbstractRepositoryQueryPage2_Refresh_Configuration_Button_Label;

    public static java.lang.String AbstractRepositoryQueryPage2_Update_Attributes_Failed;
}