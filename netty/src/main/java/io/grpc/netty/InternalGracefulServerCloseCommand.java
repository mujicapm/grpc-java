/*
 * Copyright 2021 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.grpc.netty;

import io.grpc.Internal;
import java.util.concurrent.TimeUnit;

/**
 * Internal accessor for {@link GracefulServerCloseCommand}.
 */
@Internal
public final class InternalGracefulServerCloseCommand {
  private InternalGracefulServerCloseCommand() {}

  public static Object create(String goAwayDebugString) {
    return new GracefulServerCloseCommand(goAwayDebugString);
  }

  public static Object create(String goAwayDebugString, long graceTime, TimeUnit graceTimeUnit) {
    return new GracefulServerCloseCommand(goAwayDebugString, graceTime, graceTimeUnit);
  }
}
