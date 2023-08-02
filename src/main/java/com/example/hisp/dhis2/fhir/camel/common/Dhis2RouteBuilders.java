/*
 * Copyright (c) 2004-2023, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.example.hisp.dhis2.fhir.camel.common;

import java.util.Map;
import org.apache.camel.model.RouteDefinition;

public class Dhis2RouteBuilders {
  private static final String OU_FIELDS =
      "id,code,created,lastUpdated,name,shortName,description,openingDate,parent[id]";

  private static final String OS_FIELDS =
      "id,code,created,lastUpdated,name,description,version,options[id,code,name]";

  private static final String OU_ITEM_TYPE = "org.hisp.dhis.api.model.v2_39_1.OrganisationUnit";

  private static final String OS_ITEM_TYPE = "org.hisp.dhis.api.model.v2_39_1.OptionSet";

  public static RouteDefinition getOrganisationUnits(RouteDefinition routeDefinition) {
    Map<String, String> queryParams =
        Map.of(
            "fields", OU_FIELDS,
            "order", "level",
            "filter", "level:le:2",
            "paging", "true");

    routeDefinition
        .setHeader("CamelDhis2.queryParams", () -> queryParams)
        .to(
            "dhis2://get/collection?path=organisationUnits&itemType=%s&client=#dhis2Client"
                .formatted(OU_ITEM_TYPE));

    return routeDefinition;
  }

  public static RouteDefinition getOptionSets(RouteDefinition routeDefinition) {
    Map<String, String> queryParams = Map.of("fields", OS_FIELDS, "paging", "true");

    routeDefinition
        .setHeader("CamelDhis2.queryParams", () -> queryParams)
        .to(
            "dhis2://get/collection?path=optionSets&itemType=%s&client=#dhis2Client"
                .formatted(OS_ITEM_TYPE));

    return routeDefinition;
  }
}
