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
package com.example.hisp.dhis2.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.example.hisp.dhis2.fhir.configuration.MainProperties;
import javax.servlet.Servlet;
import lombok.RequiredArgsConstructor;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.hisp.dhis.integration.sdk.Dhis2ClientBuilder;
import org.hisp.dhis.integration.sdk.api.Dhis2Client;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@RequiredArgsConstructor
@EnableConfigurationProperties(MainProperties.class)
public class MainApp {
  private final MainProperties properties;

  public static void main(String[] args) {
    SpringApplication.run(MainApp.class, args);
  }

  @Bean
  public ServletRegistrationBean<Servlet> servletRegistrationBean() {
    ServletRegistrationBean<Servlet> registration =
        new ServletRegistrationBean<>(new CamelHttpTransportServlet(), "/fhir/*");
    registration.setName("CamelServlet");
    return registration;
  }

  @Bean
  public Dhis2Client dhis2Client() {
    return Dhis2ClientBuilder.newClient(
            properties.getDhis2().getBaseUrl(),
            properties.getDhis2().getUsername(),
            properties.getDhis2().getPassword())
        .build();
  }

  @Bean
  public FhirContext fhirContext() {
    return FhirVersionEnum.R4.newContext();
  }

  @Bean
  public IGenericClient fhirClient(FhirContext fhirContext) {
    return fhirContext.newRestfulGenericClient(properties.getFhir().getBaseUrl());
  }
}
