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
package com.example.hisp.dhis2.fhir.camel.converters;

import com.example.hisp.dhis2.fhir.configuration.MainProperties;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.apache.camel.TypeConverters;
import org.hisp.dhis.api.model.v2_39_1.OrganisationUnit;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Location;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocationTypeConverter implements TypeConverters {
  private final MainProperties properties;

  @Converter
  public Location toLocation(OrganisationUnit ou, Exchange exchange) {
    Location location = new Location();

    location.setId(ou.getId().get());
    location.setName(ou.getName().get());

    String baseUrl = properties.getDhis2().getBaseUrl().replace("/api", "");

    location
        .getIdentifier()
        .add(
            new Identifier()
                .setSystem(baseUrl + "/api/organisationUnits/id")
                .setValue(ou.getId().get()));

    if (ou.getCode().isPresent()) {
      location
          .getIdentifier()
          .add(
              new Identifier()
                  .setSystem(baseUrl + "/api/organisationUnits/code")
                  .setValue(ou.getCode().get()));
    }

    if (ou.getDescription().isPresent()) {
      location.setDescription(ou.getDescription().get());
    }

    // location.getManagingOrganization().setReference("Organization/" + ou.getId().get());
    location.setMode(Location.LocationMode.INSTANCE);

    if (ou.getParent().isPresent()) {
      location.getPartOf().setReference("Location/" + ou.getId().get());
    }

    location.setStatus(Location.LocationStatus.ACTIVE);
    location.getType().add(new CodeableConcept(new Coding().setCode("OF")));

    return location;
  }
}
