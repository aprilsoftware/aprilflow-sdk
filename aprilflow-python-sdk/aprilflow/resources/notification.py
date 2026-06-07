# Copyright 2026 April Software
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.

from __future__ import annotations

import json
import threading
import time
from typing import Callable, Iterable, Any
from urllib.parse import urlencode

import httpx

from ..exceptions import AprilFlowError, AprilFlowHTTPError
from ..http import APIClient
from ..models.notification import Notification

NotificationListener = Callable[[Notification], None]
NotificationErrorListener = Callable[[Exception], None]


class NotificationSubscription:
    """Handle returned by an active Server-Sent Events notification stream."""

    def __init__(self, stop_event: threading.Event, thread: threading.Thread):
        self._stop_event = stop_event
        self._thread = thread

    def is_open(self) -> bool:
        """Return whether the background listener is still running."""
        return self._thread.is_alive() and not self._stop_event.is_set()

    def close(self, timeout: float = 5.0) -> None:
        """Stop the background SSE listener and wait briefly for it to terminate."""
        self._stop_event.set()
        if self._thread.is_alive():
            self._thread.join(timeout=timeout)

    def __enter__(self) -> "NotificationSubscription":
        return self

    def __exit__(self, exc_type: Any, exc: Any, tb: Any) -> None:
        self.close()


class NotificationResource:
    """AprilFlow SSE notification API.

    Equivalent to the Java SDK `NotificationApi`. It opens
    `/message/v1/pub/notifications` with `Accept: text/event-stream`, filters events
    named `notification`, deserializes their JSON payloads, and invokes the supplied
    callback on a daemon background thread.
    """

    NOTIFICATION_EVENT_NAME = "notification"

    def __init__(self, http: APIClient):
        self._http = http

    def path(self, object_types: Iterable[str] | None = None) -> str:
        path = "/message/v1/pub/notifications"
        values = [("objectType", t) for t in (object_types or []) if t]
        return path if not values else f"{path}?{urlencode(values)}"

    def listen(
        self,
        object_types: Iterable[str] | NotificationListener | None = None,
        listener: NotificationListener | None = None,
        error_listener: NotificationErrorListener | None = None,
        *,
        reconnect: bool = True,
        reconnect_delay: float = 1.0,
    ) -> NotificationSubscription:
        """Listen to AprilFlow notifications over SSE.

        Parameters mirror the Java SDK overloads while remaining Python-friendly:
        `client.notification.listen(listener)` and
        `client.notification.listen(["prompt"], listener)` are both supported.
        """
        if callable(object_types) and listener is None:
            listener = object_types  # type: ignore[assignment]
            object_types = None

        if listener is None:
            raise ValueError("listener is required")

        path = self.path(object_types if not callable(object_types) else None)
        stop_event = threading.Event()
        thread = threading.Thread(
            target=self._listen_loop,
            args=(path, listener, error_listener, stop_event, reconnect, reconnect_delay),
            name="aprilflow-notifications",
            daemon=True,
        )
        thread.start()
        return NotificationSubscription(stop_event, thread)

    def _listen_loop(
        self,
        path: str,
        listener: NotificationListener,
        error_listener: NotificationErrorListener | None,
        stop_event: threading.Event,
        reconnect: bool,
        reconnect_delay: float,
    ) -> None:
        while not stop_event.is_set():
            try:
                with self._http.stream(
                    "GET",
                    path,
                    headers={"Accept": "text/event-stream"},
                ) as response:
                    self._consume_events(response, listener, error_listener, stop_event)
            except Exception as exc:  # pragma: no cover - exact httpx errors depend on transport
                if stop_event.is_set():
                    return
                if error_listener is not None:
                    try:
                        error_listener(exc if isinstance(exc, Exception) else Exception(str(exc)))
                    except Exception:
                        pass
                if not reconnect:
                    return
                stop_event.wait(reconnect_delay)
            else:
                if not reconnect:
                    return
                stop_event.wait(reconnect_delay)

    def _consume_events(
        self,
        response: httpx.Response,
        listener: NotificationListener,
        error_listener: NotificationErrorListener | None,
        stop_event: threading.Event,
    ) -> None:
        event_name: str | None = None
        data_lines: list[str] = []

        for line in response.iter_lines():
            if stop_event.is_set():
                return

            if line == "":
                self._dispatch_event(event_name, data_lines, listener, error_listener)
                event_name = None
                data_lines = []
                continue

            if line.startswith(":"):
                continue

            field, _, value = line.partition(":")
            if value.startswith(" "):
                value = value[1:]

            if field == "event":
                event_name = value
            elif field == "data":
                data_lines.append(value)

        self._dispatch_event(event_name, data_lines, listener, error_listener)

    def _dispatch_event(
        self,
        event_name: str | None,
        data_lines: list[str],
        listener: NotificationListener,
        error_listener: NotificationErrorListener | None,
    ) -> None:
        if not data_lines:
            return

        if event_name is not None and event_name != self.NOTIFICATION_EVENT_NAME:
            return

        raw_data = "\n".join(data_lines)
        try:
            notification = Notification.model_validate(json.loads(raw_data))
            listener(notification)
        except Exception as exc:
            if error_listener is not None:
                error_listener(exc if isinstance(exc, Exception) else Exception(str(exc)))
            else:
                raise
