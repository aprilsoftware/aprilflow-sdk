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

public final class UserListRequest
{
    private String search;
    private Integer firstResult;
    private Integer maxResult;

    private UserListRequest()
    {
    }

    public static UserListRequest create()
    {
        return new UserListRequest();
    }

    public UserListRequest search(String search)
    {
        if (search == null || search.isBlank())
        {
            this.search = null;
        }
        else
        {
            this.search = search;
        }

        return this;
    }

    public UserListRequest firstResult(Integer firstResult)
    {
        this.firstResult = firstResult;

        return this;
    }

    public UserListRequest maxResult(Integer maxResult)
    {
        this.maxResult = maxResult;

        return this;
    }

    public String search()
    {
        return search;
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
