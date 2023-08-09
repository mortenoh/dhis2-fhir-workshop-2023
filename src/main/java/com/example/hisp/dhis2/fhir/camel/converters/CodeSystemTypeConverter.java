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
import org.hisp.dhis.api.model.v2_39_1.Option;
import org.hisp.dhis.api.model.v2_39_1.OptionSet;
import org.hisp.dhis.api.model.v2_39_1.Translation;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeSystem.ConceptDefinitionComponent;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.StringType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CodeSystemTypeConverter implements TypeConverters {
  private final MainProperties properties;

  @Converter
  public CodeSystem toCodeSystem(OptionSet optionSet, Exchange exchange) {
    String namespace = properties.getDhis2().getBaseUrl() + "/" + "optionSets";

    CodeSystem codeSystem = new CodeSystem();
    codeSystem.setId(optionSet.getId().get());
    codeSystem.getMeta().setLastUpdated(optionSet.getLastUpdated().get());
    codeSystem.setUrl(namespace + "/" + optionSet.getId().get() + "/CodeSystem");
    codeSystem.setValueSet(namespace + "/" + optionSet.getId().get() + "/ValueSet");
    codeSystem.setName("OptionSet_" + optionSet.getId().get());
    codeSystem.setTitle(optionSet.getName().get());
    codeSystem.setPublisher(properties.getDhis2().getBaseUrl());
    codeSystem.setStatus(Enumerations.PublicationStatus.ACTIVE);
    codeSystem.setContent(CodeSystem.CodeSystemContentMode.COMPLETE);

    if (optionSet.getVersion().isPresent()) {
      codeSystem.setVersion(String.valueOf(optionSet.getVersion().get()));
    }

    codeSystem.setExperimental(false);
    codeSystem.setCaseSensitive(true);

    codeSystem
        .getIdentifier()
        .add(
            new Identifier()
                .setSystem("http://dhis2.org/optionSet/id")
                .setValue(optionSet.getId().get()));

    if (optionSet.getCode().isPresent()) {
      codeSystem
          .getIdentifier()
          .add(
              new Identifier()
                  .setSystem("http://dhis2.org/optionSet/code")
                  .setValue(optionSet.getCode().get()));
    }

    for (Option option : optionSet.getOptions().get()) {
      ConceptDefinitionComponent conceptDefinitionComponent =
          codeSystem
              .addConcept()
              .setDisplay(option.getName().get())
              .setDefinition(option.getName().get())
              .setCode(option.getCode().get());

      option
          .getTranslations()
          .ifPresent(
              translations -> {
                for (Translation translation : translations) {
                  if (!translation.getProperty().get().equals("NAME")) {
                    continue;
                  }

                  conceptDefinitionComponent
                      .addDesignation()
                      .setLanguage(translation.getLocale().get())
                      .setValue(translation.getValue().get());
                }
              });
    }

    // Title translations
    optionSet
        .getTranslations()
        .ifPresent(
            translations -> {
              for (Translation translation : translations) {
                Extension extension =
                    codeSystem
                        .getTitleElement()
                        .addExtension()
                        .setUrl("http://hl7.org/fhir/StructureDefinition/translation");

                extension
                    .addExtension()
                    .setUrl("lang")
                    .setValue(new CodeType(translation.getLocale().get()));

                extension
                    .addExtension()
                    .setUrl("content")
                    .setValue(new StringType(translation.getValue().get()));
              }
            });

    return codeSystem;
  }
}
