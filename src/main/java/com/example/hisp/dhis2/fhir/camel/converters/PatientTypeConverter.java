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
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.apache.camel.TypeConverters;
import org.hisp.dhis.api.model.v2_39_1.Attribute__1;
import org.hisp.dhis.api.model.v2_39_1.DataValue__2;
import org.hisp.dhis.api.model.v2_39_1.Enrollment;
import org.hisp.dhis.api.model.v2_39_1.Event;
import org.hisp.dhis.api.model.v2_39_1.TrackedEntityInstance;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Address.AddressType;
import org.hl7.fhir.r4.model.Address.AddressUse;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PatientTypeConverter implements TypeConverters {
  private final MainProperties properties;

  @Converter
  public Patient toPatient(TrackedEntityInstance te, Exchange exchange) {
    Map<String, String> teData = getPatientData(te);

    String namespace = properties.getDhis2().getBaseUrl() + "/trackedEntityInstances";

    Patient patient = new Patient();
    patient.setId(te.getTrackedEntityInstance());

    patient
        .getIdentifier()
        .add(new Identifier().setSystem(namespace).setValue(te.getTrackedEntityInstance()));

    // which TZ?
    patient
        .getMeta()
        .setLastUpdated(
            Date.from(LocalDateTime.parse(te.getLastUpdated()).toInstant(ZoneOffset.UTC)))
        .addProfile("http://example.com/fhir/example/StructureDefinition/DHIS2BasePatient");

    patient.setManagingOrganization(new Reference("Organization?identifier=" + te.getOrgUnit()));

    patient.addExtension(
        "http://hl7.org/fhir/StructureDefinition/patient-birthPlace",
        new Address().setCountry(teData.get("gWTETHreVph")));

    patient.addExtension(
        "http://example.com/fhir/example/StructureDefinition/ConsentToBeContacted",
        new BooleanType(teData.get("YsxExAltfIE")));

    CodeableConcept codeableConcept = new CodeableConcept();
    codeableConcept
        .addCoding()
        .setSystem("http://dhis2.org/identifiertypes")
        .setCode("nationalidentifier");

    if (teData.containsKey("Ewi7FUfcHAD")) {
      patient.addIdentifier(
          new Identifier()
              .setType(codeableConcept)
              .setSystem("http://whatever.country/nationalidnamespace")
              .setValue(teData.get("Ewi7FUfcHAD")));
    }

    patient.addName().setFamily(teData.get("ENRjVGxVL6l")).addGiven(teData.get("sB1IHYu2xQT"));

    patient.setGender(getGender(teData.get("Jt68iauILtD")));

    if (teData.containsKey("NI0QRzJvQ0k")) {
      patient.setBirthDate(
          Date.from(
              LocalDateTime.parse(teData.get("NI0QRzJvQ0k") + "T00:00:00")
                  .toInstant(ZoneOffset.UTC)));
    }

    if (teData.containsKey("Z1rLc1rVHK8")) {
      patient
          .getBirthDateElement()
          .addExtension(
              "http://example.com/fhir/example/StructureDefinition/DateOfBirthIsEstimated",
              new BooleanType(teData.get("Z1rLc1rVHK8")));
    }

    if (teData.containsKey("Xhdn49gUd52")) {
      patient
          .addAddress()
          .setUse(AddressUse.HOME)
          .setType(AddressType.PHYSICAL)
          .setText(teData.get("Xhdn49gUd52"));
    }

    if (teData.containsKey("fctSQp5nAYl")) {
      patient
          .addContact()
          .addTelecom()
          .setSystem(ContactPointSystem.PHONE)
          .setValue(teData.get("fctSQp5nAYl"));
    }

    return patient;
  }

  private Enumerations.AdministrativeGender getGender(String gender) {
    if (gender == null) {
      return Enumerations.AdministrativeGender.UNKNOWN;
    }

    return switch (gender) {
      case "FEMALE" -> Enumerations.AdministrativeGender.FEMALE;
      case "MALE" -> Enumerations.AdministrativeGender.MALE;
      case "TG", "OTHER" -> AdministrativeGender.OTHER;
      default -> Enumerations.AdministrativeGender.UNKNOWN;
    };
  }

  private Map<String, String> getPatientData(TrackedEntityInstance te) {
    Map<String, String> data = new HashMap<>();

    te.getAttributes()
        .ifPresent(
            attributes -> {
              for (Attribute__1 attribute : attributes) {
                data.put(attribute.getAttribute().get(), attribute.getValue().get());
              }
            });

    te.getEnrollments()
        .ifPresent(
            en -> {
              for (Enrollment enrollment : en) {
                enrollment
                    .getEvents()
                    .ifPresent(
                        e -> {
                          for (Event event : e) {
                            event
                                .getDataValues()
                                .ifPresent(
                                    dv -> {
                                      for (DataValue__2 v : dv) {
                                        data.put(v.getDataElement().get(), v.getValue().get());
                                      }
                                    });
                          }
                        });
              }
            });

    return data;
  }
}
