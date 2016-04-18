DROP VIEW IF EXISTS `user_vm_view`;

CREATE VIEW `user_vm_view` AS
SELECT
  `vm_instance`.`id`,
  `vm_instance`.`name`,
  `user_vm`.`display_name`,
  `user_vm`.`user_data`,
  `account`.`id` AS `account_id`,
  `account`.`uuid` AS `account_uuid`,
  `account`.`account_name`,
  `account`.`type` AS `account_type`,
  `domain`.`id` AS `domain_id`,
  `domain`.`uuid` AS `domain_uuid`,
  `domain`.`name` AS `domain_name`,
  `domain`.`path` AS `domain_path`,
  `projects`.`id` AS `project_id`,
  `projects`.`uuid` AS `project_uuid`,
  `projects`.`name` AS `project_name`,
  `instance_group`.`id` AS `instance_group_id`,
  `instance_group`.`uuid` AS `instance_group_uuid`,
  `instance_group`.`name` AS `instance_group_name`,
  `vm_instance`.`uuid`,
  `vm_instance`.`last_host_id`,
  `vm_instance`.`vm_type` AS `type`,
  `vm_instance`.`limit_cpu_use`,
  `vm_instance`.`created`,
  `vm_instance`.`state`,
  `vm_instance`.`removed`,
  `vm_instance`.`ha_enabled`,
  `vm_instance`.`hypervisor_type`,
  `vm_instance`.`instance_name`,
  `vm_instance`.`guest_os_id`,
  `vm_instance`.`display_vm`,
  `guest_os`.`uuid` AS `guest_os_uuid`,
  `vm_instance`.`pod_id`,
  `host_pod_ref`.`uuid` AS `pod_uuid`,
  `vm_instance`.`private_ip_address`,
  `vm_instance`.`private_mac_address`,
  `vm_instance`.`vm_type`,
  `data_center`.`id` AS `data_center_id`,
  `data_center`.`uuid` AS `data_center_uuid`,
  `data_center`.`name` AS `data_center_name`,
  `data_center`.`is_security_group_enabled` AS `security_group_enabled`,
  `data_center`.`networktype` AS `data_center_type`,
  `host`.`id` AS `host_id`,
  `host`.`uuid` AS `host_uuid`,
  `host`.`name` AS `host_name`,
  `host`.`private_ip_address` AS `host_ip_address`,
  `vm_template`.`id` AS `template_id`,
  `vm_template`.`uuid` AS `template_uuid`,
  `vm_template`.`name` AS `template_name`,
  `vm_template`.`display_text` AS `template_display_text`,
  `vm_template`.`enable_password` AS `password_enabled`,
  `iso`.`id` AS `iso_id`,
  `iso`.`uuid` AS `iso_uuid`,
  `iso`.`name` AS `iso_name`,
  `iso`.`display_text` AS `iso_display_text`,
  `service_offering`.`id` AS `service_offering_id`,
  `svc_disk_offering`.`uuid` AS `service_offering_uuid`,
  `disk_offering`.`uuid` AS `disk_offering_uuid`,
  `disk_offering`.`id` AS `disk_offering_id`,
  (CASE
     WHEN Isnull(`service_offering`.`cpu`) THEN `custom_cpu`.`value`
     ELSE `service_offering`.`cpu`
   end) AS `cpu`,
  (CASE
     WHEN Isnull(`service_offering`.`speed`) THEN `custom_speed`.`value`
     ELSE `service_offering`.`speed`
   end) AS `speed`,
  (CASE
     WHEN Isnull(`service_offering`.`ram_size`) THEN `custom_ram_size`.`value`
     ELSE `service_offering`.`ram_size`
   end) AS `ram_size`,
  `svc_disk_offering`.`name` AS `service_offering_name`,
  `disk_offering`.`name` AS `disk_offering_name`,
  `storage_pool`.`id` AS `pool_id`,
  `storage_pool`.`uuid` AS `pool_uuid`,
  `storage_pool`.`pool_type`,
  `volumes`.`id` AS `volume_id`,
  `volumes`.`uuid` AS `volume_uuid`,
  `volumes`.`device_id` AS `volume_device_id`,
  `volumes`.`volume_type`,
  `security_group`.`id` AS `security_group_id`,
  `security_group`.`uuid` AS `security_group_uuid`,
  `security_group`.`name` AS `security_group_name`,
  `security_group`.`description` AS `security_group_description`,
  `nics`.`id` AS `nic_id`,
  `nics`.`uuid` AS `nic_uuid`,
  `nics`.`network_id`,
  `nics`.`ip4_address` AS `ip_address`,
  `nics`.`ip6_address`,
  `nics`.`ip6_gateway`,
  `nics`.`ip6_cidr`,
  `nics`.`default_nic` AS `is_default_nic`,
  `nics`.`gateway`,
  `nics`.`netmask`,
  `nics`.`mac_address`,
  `nics`.`broadcast_uri`,
  `nics`.`isolation_uri`,
  `vpc`.`id` AS `vpc_id`,
  `vpc`.`uuid` AS `vpc_uuid`,
  `networks`.`uuid` AS `network_uuid`,
  `networks`.`name` AS `network_name`,
  `networks`.`traffic_type`,
  `networks`.`guest_type`,
  `user_ip_address`.`id` AS `public_ip_id`,
  `user_ip_address`.`uuid` AS `public_ip_uuid`,
  `user_ip_address`.`public_ip_address`,
  `ssh_keypairs`.`keypair_name`,
  `resource_tags`.`id` AS `tag_id`,
  `resource_tags`.`uuid` AS `tag_uuid`,
  `resource_tags`.`key` AS `tag_key`,
  `resource_tags`.`value` AS `tag_value`,
  `resource_tags`.`domain_id` AS `tag_domain_id`,
  `resource_tags`.`account_id` AS `tag_account_id`,
  `resource_tags`.`resource_id` AS `tag_resource_id`,
  `resource_tags`.`resource_uuid` AS `tag_resource_uuid`,
  `resource_tags`.`resource_type` AS `tag_resource_type`,
  `resource_tags`.`customer` AS `tag_customer`,
  `async_job`.`id` AS `job_id`,
  `async_job`.`uuid` AS `job_uuid`,
  `async_job`.`job_status`,
  `async_job`.`account_id` AS `job_account_id`,
  `affinity_group`.`id` AS `affinity_group_id`,
  `affinity_group`.`uuid` AS `affinity_group_uuid`,
  `affinity_group`.`name` AS `affinity_group_name`,
  `affinity_group`.`description` AS `affinity_group_description`,
  `vm_instance`.`dynamically_scalable`
FROM
  ((((((((((((((((((((((((((((((((`user_vm`
                                  JOIN `vm_instance`
                                    ON(((`vm_instance`.`id` = `user_vm`.`id`)
                                        AND Isnull(`vm_instance`.`removed`))))
                                 JOIN `account`
                                   ON((`vm_instance`.`account_id` = `account`.`id`)))
                                JOIN `domain`
                                  ON((`vm_instance`.`domain_id` = `domain`.`id`)))
                               LEFT JOIN `guest_os`
                                      ON((`vm_instance`.`guest_os_id` = `guest_os`.`id`)))
                              LEFT JOIN `host_pod_ref`
                                     ON((`vm_instance`.`pod_id` = `host_pod_ref`.`id`)))
                             LEFT JOIN `projects`
                                    ON((`projects`.`project_account_id` = `account`.`id`)))
                            LEFT JOIN `instance_group_vm_map`
                                   ON((`vm_instance`.`id` = `instance_group_vm_map`.`instance_id`)))
                           LEFT JOIN `instance_group`
                                  ON((`instance_group_vm_map`.`group_id` = `instance_group`.`id`)))
                          LEFT JOIN `data_center`
                                 ON((`vm_instance`.`data_center_id` = `data_center`.`id`)))
                         LEFT JOIN `host`
                                ON((`vm_instance`.`host_id` = `host`.`id`)))
                        LEFT JOIN `vm_template`
                               ON((`vm_instance`.`vm_template_id` = `vm_template`.`id`)))
                       LEFT JOIN `vm_template` `iso`
                              ON((`iso`.`id` = `user_vm`.`iso_id`)))
                      LEFT JOIN `service_offering`
                             ON((`vm_instance`.`service_offering_id` = `service_offering`.`id`)))
                     LEFT JOIN `disk_offering` `svc_disk_offering`
                            ON((`vm_instance`.`service_offering_id` = `svc_disk_offering`.`id`)))
                    LEFT JOIN `disk_offering`
                           ON((`vm_instance`.`disk_offering_id` = `disk_offering`.`id`)))
                   LEFT JOIN `volumes`
                          ON((`vm_instance`.`id` = `volumes`.`instance_id`)))
                  LEFT JOIN `storage_pool`
                         ON((`volumes`.`pool_id` = `storage_pool`.`id`)))
                 LEFT JOIN `security_group_vm_map`
                        ON((`vm_instance`.`id` = `security_group_vm_map`.`instance_id`)))
                LEFT JOIN `security_group`
                       ON((`security_group_vm_map`.`security_group_id` = `security_group`.`id`)))
               LEFT JOIN `nics`
                      ON(((`vm_instance`.`id` = `nics`.`instance_id`)
                          AND Isnull(`nics`.`removed`))))
              LEFT JOIN `networks`
                     ON((`nics`.`network_id` = `networks`.`id`)))
             LEFT JOIN `vpc`
                    ON(((`networks`.`vpc_id` = `vpc`.`id`)
                        AND Isnull(`vpc`.`removed`))))
            LEFT JOIN `user_ip_address`
                   ON((`user_ip_address`.`vm_id` = `vm_instance`.`id`)))
           LEFT JOIN `user_vm_details` `ssh_details`
                  ON(((`ssh_details`.`vm_id` = `vm_instance`.`id`)
                      AND (`ssh_details`.`name` = 'SSH.PublicKey'))))
          LEFT JOIN `ssh_keypairs`
                 ON((`ssh_keypairs`.`public_key` = `ssh_details`.`value`)))
         LEFT JOIN `resource_tags`
                ON(((`resource_tags`.`resource_id` = `vm_instance`.`id`)
                    AND (`resource_tags`.`resource_type` = 'UserVm'))))
        LEFT JOIN `async_job`
               ON(((`async_job`.`instance_id` = `vm_instance`.`id`)
                   AND (`async_job`.`instance_type` = 'VirtualMachine')
                   AND (`async_job`.`job_status` = 0))))
       LEFT JOIN `affinity_group_vm_map`
              ON((`vm_instance`.`id` = `affinity_group_vm_map`.`instance_id`)))
      LEFT JOIN `affinity_group`
             ON((`affinity_group_vm_map`.`affinity_group_id` = `affinity_group`.`id`)))
     LEFT JOIN `user_vm_details` `custom_cpu`
            ON(((`custom_cpu`.`vm_id` = `vm_instance`.`id`)
                AND (`custom_cpu`.`name` = 'CpuNumber'))))
    LEFT JOIN `user_vm_details` `custom_speed`
           ON(((`custom_speed`.`vm_id` = `vm_instance`.`id`)
               AND (`custom_speed`.`name` = 'CpuSpeed'))))
   LEFT JOIN `user_vm_details` `custom_ram_size`
          ON(((`custom_ram_size`.`vm_id` = `vm_instance`.`id`)
              AND (`custom_ram_size`.`name` = 'memory'))));