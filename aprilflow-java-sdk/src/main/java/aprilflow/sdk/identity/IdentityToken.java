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

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class IdentityToken
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final String encodedToken;
    private final Map<String, Object> tokenMap;
    
    public IdentityToken(String encodedToken, Map<String, Object> tokenMap)
    {
        this.encodedToken = encodedToken;

        this.tokenMap = tokenMap;
    }

    public String getEncodedToken()
    {
        return encodedToken;
    }

    public Map<String, Object> getTokenMap()
    {
        return tokenMap;
    }

    public String getUserId()
    {
        return (String) tokenMap.get("user-id");
    }

    public String getTenantId()
    {
        return (String) tokenMap.get("tenant-id");
    }

    public String getIssuer()
    {
        return (String) tokenMap.get("iss");
    }

    public Instant getExpirationTime()
    {
        long expSeconds;
        Object exp;

        exp = tokenMap.get("exp");

        if (exp == null)
        {
            throw new IllegalStateException("Token does not contain exp claim.");
        }

        if (exp instanceof Number number)
        {
            expSeconds = number.longValue();
        }
        else
        {
            expSeconds = Long.parseLong(exp.toString());
        }

        return Instant.ofEpochSecond(expSeconds);
    }

    public boolean isExpired()
    {
        Instant nowWithBuffer;
        
        nowWithBuffer = Instant.now().plusSeconds(30);

        return !nowWithBuffer.isBefore(getExpirationTime());
    }

    public boolean isRoleMember(TenantRole role)
    {
        Object groups = tokenMap.get("groups");

        if (groups instanceof List<?> list)
        {
            return list.contains(role.getName());
        }
        else
        {
            return false;
        }
    }

    public static IdentityToken createToken(String encodedToken)
    {
        return new IdentityToken(encodedToken, decode(encodedToken));
    }

    private static Map<String, Object> decode(String token)
    {
        String payload;
        String[] parts;

        parts = token.split("\\.");

        if (parts.length != 3)
        {
            throw new IllegalArgumentException("Invalid token.");
        }

        payload = decodeBase64(parts[1]);

        try
        {
            return OBJECT_MAPPER.readValue(payload, new TypeReference<Map<String, Object>>() {});
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Invalid payload.", e);
        }
    }

    private static String decodeBase64(String tokenPart)
    {
        byte[] decodedBytes;

        try
        {
            decodedBytes = Base64.getUrlDecoder().decode(tokenPart);

            return new String(decodedBytes, StandardCharsets.UTF_8);
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException("Invalid token.", e);
        }
    }
}
