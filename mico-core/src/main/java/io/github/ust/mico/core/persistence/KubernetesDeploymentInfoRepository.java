/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.ust.mico.core.persistence;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import io.github.ust.mico.core.model.KubernetesDeploymentInfo;

public interface KubernetesDeploymentInfoRepository extends Neo4jRepository<KubernetesDeploymentInfo, Long> {
	
	/**
	 * Deletes all {@link KubernetesDeploymentInfo} nodes that do <b>not</b> have any relationship to another node.
	 */
	@Query("MATCH (kdi:KubernetesDeploymentInfo) WHERE size((kdi)--()) = 0 DELETE (kdi)")
	void cleanUp();
	
}
