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

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class PromptWaitOptions
{
    private final Duration timeout;
    private final List<PromptStatus> terminalStatuses;

    public PromptWaitOptions(Duration timeout, List<PromptStatus> terminalStatuses)
    {
        if (terminalStatuses == null || terminalStatuses.isEmpty())
        {
            terminalStatuses = Arrays.asList(
                PromptStatus.Completed,
                PromptStatus.Interrupted,
                PromptStatus.OnError,
                PromptStatus.QuotaExceeded
            );
        }

        this.timeout = timeout;
        this.terminalStatuses = Collections.unmodifiableList(terminalStatuses);
    }

    public static PromptWaitOptions defaultOptions()
    {
        return new PromptWaitOptions(
            Duration.ofMinutes(5),
            null
        );
    }

    public Duration timeout()
    {
        return timeout;
    }

    public List<PromptStatus> terminalStatuses()
    {
        return terminalStatuses;
    }
}
