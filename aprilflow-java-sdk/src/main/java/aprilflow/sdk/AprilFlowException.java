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
package aprilflow.sdk;

public class AprilFlowException extends RuntimeException
{
    private final int statusCode;
    private final String responseBody;

    public AprilFlowException(String message)
    {
        this(message, -1, null);
    }

    public AprilFlowException(String message, Throwable cause)
    {
        super(message, cause);
        this.statusCode = -1;
        this.responseBody = null;
    }

    public AprilFlowException(String message, int statusCode, String responseBody)
    {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int statusCode()
    {
        return statusCode;
    }

    public String responseBody()
    {
        return responseBody;
    }
}
