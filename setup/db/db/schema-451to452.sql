-- Licensed to the Apache Software Foundation (ASF) under one
-- or more contributor license agreements.  See the NOTICE file
-- distributed with this work for additional information
-- regarding copyright ownership.  The ASF licenses this file
-- to you under the Apache License, Version 2.0 (the
-- "License"); you may not use this file except in compliance
-- with the License.  You may obtain a copy of the License at
--
--   http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
-- KIND, either express or implied.  See the License for the
-- specific language governing permissions and limitations
-- under the License.

--;
-- Schema upgrade from 4.5.1 to 4.5.2;
--;

DELETE FROM `cloud`.`configuration` WHERE name like 'saml%';

ALTER TABLE `cloud`.`user` ADD COLUMN `external_entity` text DEFAULT NULL COMMENT "reference to external federation entity";

DROP TABLE IF EXISTS `cloud`.`saml_token`;
CREATE TABLE `cloud`.`saml_token` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `uuid` varchar(255) UNIQUE NOT NULL COMMENT 'The Authn Unique Id',
  `domain_id` bigint unsigned DEFAULT NULL,
  `entity` text NOT NULL COMMENT 'Identity Provider Entity Id',
  `created` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_saml_token__domain_id` FOREIGN KEY(`domain_id`) REFERENCES `domain`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

SET foreign_key_checks = 0;
ALTER TABLE `cloud`.`region` MODIFY `id` int unsigned UNIQUE NOT NULL;
SET foreign_key_checks = 1;

DROP VIEW IF EXISTS `cloud`.`affinity_group_view`;
CREATE VIEW `cloud`.`affinity_group_view` AS
    select 
        affinity_group.id id,
        affinity_group.name name,
        affinity_group.type type,
        affinity_group.description description,
        affinity_group.uuid uuid,
		affinity_group.acl_type acl_type,
        account.id account_id,
        account.uuid account_uuid,
        account.account_name account_name,
        account.type account_type,
        domain.id domain_id,
        domain.uuid domain_uuid,
        domain.name domain_name,
        domain.path domain_path,
        projects.id project_id,
        projects.uuid project_uuid,
        projects.name project_name,
        vm_instance.id vm_id,
        vm_instance.uuid vm_uuid,
        vm_instance.name vm_name,
        vm_instance.state vm_state,
        user_vm.display_name vm_display_name
    from
        `cloud`.`affinity_group`
            inner join
        `cloud`.`account` ON affinity_group.account_id = account.id
            inner join
        `cloud`.`domain` ON affinity_group.domain_id = domain.id
            left join
        `cloud`.`projects` ON projects.project_account_id = account.id
            left join
        `cloud`.`affinity_group_vm_map` ON affinity_group.id = affinity_group_vm_map.affinity_group_id
            left join
        `cloud`.`vm_instance` ON vm_instance.id = affinity_group_vm_map.instance_id
            left join
        `cloud`.`user_vm` ON user_vm.id = vm_instance.id;
