// Copyright (c) YugaByte, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.  You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software distributed under the License
// is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
// or implied.  See the License for the specific language governing permissions and limitations
// under the License.
//

package org.yb.client;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import org.jboss.netty.buffer.ChannelBuffer;
import org.yb.cdc.CdcService;
import org.yb.util.Pair;

public class GetCheckpointRequest extends YRpc<GetCheckpointResponse>{
  private String streamId;
  private String tabletId;

  public GetCheckpointRequest(YBTable table, String streamId, String tabletId) {
    super(table);
    this.streamId = streamId;
    this.tabletId = tabletId;
  }

  @Override
  ChannelBuffer serialize(Message header) {
    assert header.isInitialized();
    final CdcService.GetCheckpointRequestPB.Builder builder = CdcService
    .GetCheckpointRequestPB.newBuilder();
    builder.setStreamId(ByteString.copyFromUtf8(this.streamId));
    builder.setTabletId(ByteString.copyFromUtf8(this.tabletId));

    return toChannelBuffer(header, builder.build());
  }

  @Override
  String serviceName() { return CDC_SERVICE_NAME; }

  @Override
  String method() {
    return "GetCheckpoint";
  }

  @Override
  Pair<GetCheckpointResponse, Object> deserialize(CallResponse callResponse,
                                                  String uuid) throws Exception {
    final CdcService.GetCheckpointResponsePB.Builder respBuilder =
      CdcService.GetCheckpointResponsePB.newBuilder();
    readProtobuf(callResponse.getPBMessage(), respBuilder);

    GetCheckpointResponse response =
      new GetCheckpointResponse(deadlineTracker.getElapsedMillis(), uuid,
        respBuilder.getCheckpoint().getOpId().getIndex(),
    respBuilder.getCheckpoint().getOpId().getTerm());
    return new Pair<GetCheckpointResponse, Object>(
      response, respBuilder.hasError() ? respBuilder.getError() : null);
  }
}
