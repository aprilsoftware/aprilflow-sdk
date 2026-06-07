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
package aprilflow.sdk.document;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class Chunk
{
    private final String collectionId;
    private final String chunkId;
    private final String text;
    private final int startLine;
    private final int endLine;
    private final double score;

    @JsonCreator
    public Chunk(
        @JsonProperty("collectionId") String collectionId,
        @JsonProperty("chunkId") String chunkId,
        @JsonProperty("text") String text,
        @JsonProperty("startLine") int startLine,
        @JsonProperty("endLine") int endLine,
        @JsonProperty("score") double score
    )
    {
        this.collectionId = collectionId;

        this.chunkId = chunkId;

        this.text = text;

        this.startLine = startLine;

        this.endLine = endLine;

        this.score = score;
    }

    @JsonProperty("collectionId")
    public String getCollectionId()
    {
        return collectionId;
    }

    @JsonProperty("chunkId")
    public String getChunkId()
    {
        return chunkId;
    }

    @JsonProperty("text")
    public String getText()
    {
        return text;
    }

    @JsonProperty("startLine")
    public int getStartLine()
    {
        return startLine;
    }

    @JsonProperty("endLine")
    public int getEndLine()
    {
        return endLine;
    }

    @JsonProperty("score")
    public double getScore()
    {
        return score;
    }
}
