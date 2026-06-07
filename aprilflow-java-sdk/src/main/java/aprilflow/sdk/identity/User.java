/*
 * Copyright 2026 April Software
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package aprilflow.sdk.identity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class User
{
    private final String id;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final String address;
    private final String postalCode;
    private final String city;
    private final String countryCode;
    private final String phoneNumber;
    private final boolean active;
    private final boolean enabled;
    private final boolean totpEnabled;
    private final boolean totpSetupCompleted;
    private final String tenantId;
    private final List<String> policyIds;

    @JsonCreator
    public User(
        @JsonProperty("id") String id,
        @JsonProperty("email") String email,
        @JsonProperty("firstName") String firstName,
        @JsonProperty("lastName") String lastName,
        @JsonProperty("address") String address,
        @JsonProperty("postalCode") String postalCode,
        @JsonProperty("city") String city,
        @JsonProperty("countryCode") String countryCode,
        @JsonProperty("phoneNumber") String phoneNumber,
        @JsonProperty("active") boolean active,
        @JsonProperty("enabled") boolean enabled,
        @JsonProperty("totpEnabled") boolean totpEnabled,
        @JsonProperty("totpSetupCompleted") boolean totpSetupCompleted,
        @JsonProperty("tenantId") String tenantId,
        @JsonProperty("policyIds") List<String> policyIds
    )
    {
        this.id = id;

        this.email = email;

        this.firstName = firstName;

        this.lastName = lastName;

        this.address = address;

        this.postalCode = postalCode;

        this.city = city;

        this.countryCode = countryCode;

        this.phoneNumber = phoneNumber;

        this.active = active;

        this.enabled = enabled;

        this.totpEnabled = totpEnabled;

        this.totpSetupCompleted = totpSetupCompleted;

        this.tenantId = tenantId;

        this.policyIds = policyIds;
    }

    @JsonProperty("id")
    public String getId()
    {
        return id;
    }

    @JsonProperty("email")
    public String getEmail()
    {
        return email;
    }

    @JsonProperty("firstName")
    public String getFirstName()
    {
        return firstName;
    }

    @JsonProperty("lastName")
    public String getLastName()
    {
        return lastName;
    }

    @JsonProperty("address")
    public String getAddress()
    {
        return address;
    }

    @JsonProperty("postalCode")
    public String getPostalCode()
    {
        return postalCode;
    }

    @JsonProperty("city")
    public String getCity()
    {
        return city;
    }

    @JsonProperty("countryCode")
    public String getCountryCode()
    {
        return countryCode;
    }

    @JsonProperty("phoneNumber")
    public String getPhoneNumber()
    {
        return phoneNumber;
    }

    @JsonProperty("active")
    public boolean getActive()
    {
        return active;
    }

    @JsonProperty("enabled")
    public boolean getEnabled()
    {
        return enabled;
    }

    @JsonProperty("totpEnabled")
    public boolean getTotpEnabled()
    {
        return totpEnabled;
    }

    @JsonProperty("totpSetupCompleted")
    public boolean getTotpSetupCompleted()
    {
        return totpSetupCompleted;
    }

    @JsonProperty("tenantId")
    public String getTenantId()
    {
        return tenantId;
    }

    @JsonProperty("policyIds")
    public List<String> getPolicyIds()
    {
        return policyIds;
    }
}
