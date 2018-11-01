ALTER TABLE WLI_QS_REPORT_ATTRIBUTE  
MODIFY (LOCALHOST_TIMESTAMP TIMESTAMP(3) );

create view V_WLI_QS_REPORT as 
    select a.msg_guid, localhost_timestamp, host_name, state, node, 
          pipeline_name, stage_name, inbound_service_name, inbound_service_uri, inbound_operation, 
          outbound_service_name, outbound_service_uri, outbound_operation, 
          regexp_substr(regexp_substr(msg_labels, '[^;]+', 1, 1), '[^=]+', 1, 2) as  sessionID, 
          regexp_substr(regexp_substr(msg_labels, '[^;]+', 1, 2), '[^=]+', 1, 2) as transactionID,
          regexp_substr(regexp_substr(msg_labels, '[^;]+', 1, 3), '[^=]+', 1, 2) as userID, 
          regexp_substr(regexp_substr(msg_labels, '[^;]+', 1, 4), '[^=]+', 1, 2) as operationID, 
          error_code, error_reason, error_details, data_type, encoding, data_value 
        from wli_qs_report_attribute A, wli_qs_report_data B 
            where A.msg_guid = B.msg_guid;