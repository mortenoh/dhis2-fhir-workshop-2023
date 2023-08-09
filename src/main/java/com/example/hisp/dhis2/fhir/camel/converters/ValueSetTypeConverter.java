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
import org.hisp.dhis.api.model.v2_39_1.OptionSet;
import org.hisp.dhis.api.model.v2_39_1.Translation;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ValueSetTypeConverter implements TypeConverters {
  private final MainProperties properties;

  @Converter
  public ValueSet toValueSet(OptionSet optionSet, Exchange exchange) {
    String namespace = properties.getDhis2().getBaseUrl() + "/optionSets";

    ValueSet valueSet = new ValueSet();
    valueSet.setId(optionSet.getId().get());
    valueSet.getMeta().setLastUpdated(optionSet.getLastUpdated().get());
    valueSet.setUrl(namespace + "/" + optionSet.getId().get() + "/ValueSet");
    valueSet.setName("OptionSet_" + optionSet.getId().get());
    valueSet.setTitle(optionSet.getName().get());
    // valueSet.setDescription( optionSet.getDescription() );
    valueSet.setStatus(Enumerations.PublicationStatus.ACTIVE);

    if (optionSet.getVersion().isPresent()) {
      valueSet.setVersion(String.valueOf(optionSet.getVersion().get()));
    }

    valueSet.setExperimental(false);
    valueSet.setImmutable(true);

    valueSet
        .getIdentifier()
        .add(
            new Identifier()
                .setSystem("http://dhis2.org/optionSet/idVS")
                .setValue(optionSet.getId().get()));

    if (optionSet.getCode().isPresent()) {
      valueSet
          .getIdentifier()
          .add(
              new Identifier()
                  .setSystem("http://dhis2.org/optionSet/codeVS")
                  .setValue(optionSet.getCode().get()));
    }

    valueSet.setCompose(
        new ValueSet.ValueSetComposeComponent()
            .addInclude(
                new ValueSet.ConceptSetComponent()
                    .setSystem(namespace + "/" + optionSet.getId().get() + "/CodeSystem")));

    // Title translations
    optionSet
        .getTranslations()
        .ifPresent(
            translations -> {
              for (Translation translation : translations) {
                if (!translation.getProperty().get().equals("NAME")) {
                  continue;
                }

                Extension extension =
                    valueSet
                        .addExtension()
                        .setUrl("http://hl7.org/fhir/StructureDefinition/translation");

                extension
                    .addExtension()
                    .setUrl("lang")
                    .setValue(
                        new Coding(
                            "urn:iso:std:iso:3166",
                            translation.getLocale().get(),
                            translation.getLocale().get()));

                extension
                    .addExtension()
                    .setUrl("content")
                    .setValue(new StringType(translation.getValue().get()));
              }
            });

    return valueSet;
  }
}
