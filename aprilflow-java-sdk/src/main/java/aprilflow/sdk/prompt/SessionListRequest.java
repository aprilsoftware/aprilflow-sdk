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
package aprilflow.sdk.prompt;

public final class SessionListRequest
{
    private Integer firstResult;
    private Integer maxResult;

    private SessionListRequest()
    {
    }

    public static SessionListRequest create()
    {
        return new SessionListRequest();
    }

    public SessionListRequest firstResult(Integer firstResult)
    {
        this.firstResult = firstResult;

        return this;
    }

    public SessionListRequest maxResult(Integer maxResult)
    {
        this.maxResult = maxResult;

        return this;
    }

    public Integer firstResult()
    {
        return firstResult;
    }

    public Integer maxResult()
    {
        return maxResult;
    }
}
