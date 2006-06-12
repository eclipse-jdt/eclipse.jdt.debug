/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.jdi.internal.connect;

import org.eclipse.osgi.util.NLS;

public class ConnectMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.jdi.internal.connect.ConnectMessages";//$NON-NLS-1$

	public static String PacketReceiveManager_Got_IOException_from_Virtual_Machine_1;
	public static String PacketReceiveManager_Got_IOException_from_Virtual_Machine_2;
	public static String PacketSendManager_Got_IOException_from_Virtual_Machine_1;
	public static String SocketAttachingConnectorImpl_Machine_name_to_which_to_attach_for_VM_connections_1;
	public static String SocketAttachingConnectorImpl_Host_2;
	public static String SocketAttachingConnectorImpl_Port_number_to_which_to_attach_for_VM_connections_3;
	public static String SocketAttachingConnectorImpl_Port_4;
	public static String SocketAttachingConnectorImpl_1;
	public static String SocketAttachingConnectorImpl_2;
	public static String SocketAttachingConnectorImpl_Attaches_by_socket_to_other_VMs_5;
	public static String SocketAttachingConnectorImpl_Connection_argument_is_not_of_the_right_type_6;
	public static String SocketAttachingConnectorImpl_Necessary_connection_argument_is_null_7;
	public static String SocketAttachingConnectorImpl_Connection_argument_is_not_a_number_8;
	public static String SocketLaunchingConnectorImpl_Home_directory_of_the_SDK_or_runtime_environment_used_to_launch_the_application_1;
	public static String SocketLaunchingConnectorImpl_Home_2;
	public static String SocketLaunchingConnectorImpl_Launched_VM_options_3;
	public static String SocketLaunchingConnectorImpl_Options_4;
	public static String SocketLaunchingConnectorImpl_Main_class_and_arguments__or_if__jar_is_an_option__the_main_jar_file_and_arguments_5;
	public static String SocketLaunchingConnectorImpl_Main_6;
	public static String SocketLaunchingConnectorImpl_All_threads_will_be_suspended_before_execution_of_main_7;
	public static String SocketLaunchingConnectorImpl_Suspend_8;
	public static String SocketLaunchingConnectorImpl_Character_used_to_combine_space_delimited_text_into_a_single_command_line_argument_9;
	public static String SocketLaunchingConnectorImpl_Quote_10;
	public static String SocketLaunchingConnectorImpl_Name_of_the_Java_VM_launcher_11;
	public static String SocketLaunchingConnectorImpl_Launcher_12;
	public static String SocketLaunchingConnectorImpl_Launches_target_using_Sun_Java_VM_command_line_and_attaches_to_it_13;
	public static String SocketLaunchingConnectorImpl_Connection_argument_is_not_of_the_right_type_14;
	public static String SocketLaunchingConnectorImpl_Necessary_connection_argument_is_null_15;
	public static String SocketLaunchingConnectorImpl_Connection_argument_is_not_a_number_16;
	public static String SocketListeningConnectorImpl_Port_number_at_which_to_listen_for_VM_connections_1;
	public static String SocketListeningConnectorImpl_Port_2;
	public static String SocketListeningConnectorImpl_Timeout_before_accept_returns_3;
	public static String SocketListeningConnectorImpl_Timeout_4;
	public static String SocketListeningConnectorImpl_Accepts_socket_connections_initiated_by_other_VMs_5;
	public static String SocketListeningConnectorImpl_Connection_argument_is_not_of_the_right_type_6;
	public static String SocketListeningConnectorImpl_Necessary_connection_argument_is_null_7;
	public static String SocketListeningConnectorImpl_Connection_argument_is_not_a_number_8;
	public static String SocketListeningConnectorImpl_ListeningConnector_Socket_Port;
	public static String SocketRawLaunchingConnectorImpl_Raw_command_to_start_the_debugged_application_VM_1;
	public static String SocketRawLaunchingConnectorImpl_Command_2;
	public static String SocketRawLaunchingConnectorImpl_Address_from_which_to_listen_for_a_connection_after_the_raw_command_is_run_3;
	public static String SocketRawLaunchingConnectorImpl_Address_4;
	public static String SocketRawLaunchingConnectorImpl_Character_used_to_combine_space_delimited_text_into_a_single_command_line_argument_5;
	public static String SocketRawLaunchingConnectorImpl_Quote_6;
	public static String SocketRawLaunchingConnectorImpl_Launches_target_using_user_specified_command_line_and_attaches_to_it_7;
	public static String SocketRawLaunchingConnectorImpl_Connection_argument_is_not_of_the_right_type_8;
	public static String SocketRawLaunchingConnectorImpl_Necessary_connection_argument_is_null_9;
	public static String SocketRawLaunchingConnectorImpl_Connection_argument_is_not_a_number_10;
	public static String SocketLaunchingConnectorImpl_VM_did_not_connect_within_given_time___0__ms_1;
	public static String PacketSendManager_Got__0__from_Virtual_Machine_1;
	public static String PacketSendManager_Got__0__from_Virtual_Machine___1__1;
	public static String PacketReceiveManager_Got__0__from_Virtual_Machine_1;
	public static String PacketReceiveManager_Got__0__from_Virtual_Machine___1__1;
	public static String PacketReceiveManager_0;
	public static String SocketTransportService_0;
	public static String SocketTransportService_1;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, ConnectMessages.class);
	}
}
