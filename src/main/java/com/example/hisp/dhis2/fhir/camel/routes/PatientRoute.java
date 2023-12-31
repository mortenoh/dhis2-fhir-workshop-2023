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
package com.example.hisp.dhis2.fhir.camel.routes;

import static com.example.hisp.dhis2.fhir.camel.common.Dhis2RouteBuilders.getTrackedEntities;

import com.example.hisp.dhis2.fhir.camel.common.BundleAggregationStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.hisp.dhis.api.model.v2_39_1.TrackedEntityInstance;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PatientRoute extends RouteBuilder {
  private static final String URI = "get-fhir-patient";

  private final ObjectMapper objectMapper;

  @Override
  public void configure() throws Exception {
    getTrackedEntities(from("direct:%s".formatted(URI)), "Xh88p1nyefp")
        .routeId(URI)
        .split(jsonpath("$.trackedEntityInstances"), new BundleAggregationStrategy())
        .process(
            x -> {
              TrackedEntityInstance trackedEntityInstance =
                  objectMapper.convertValue(x.getIn().getBody(), TrackedEntityInstance.class);
              x.getMessage().setBody(trackedEntityInstance);
            })
        .convertBodyTo(Patient.class)
        .end()
        .marshal()
        .fhirJson("R4");

    rest("/")
        .get("/baseR4/Patient")
        .produces(MediaType.APPLICATION_JSON_VALUE)
        .to("direct:%s".formatted(URI));
  }
}
