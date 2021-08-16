package io.deephaven.javascript.proto.dhinternal.io.deephaven.proto.field_pb_service;

import io.deephaven.javascript.proto.dhinternal.browserheaders.BrowserHeaders;
import io.deephaven.javascript.proto.dhinternal.io.deephaven.proto.field_pb.FieldsChangeUpdate;
import io.deephaven.javascript.proto.dhinternal.io.deephaven.proto.field_pb.ListFieldsRequest;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(
    isNative = true,
    name = "dhinternal.io.deephaven.proto.field_pb_service.FieldServiceClient",
    namespace = JsPackage.GLOBAL)
public class FieldServiceClient {
  public String serviceHost;

  public FieldServiceClient(String serviceHost, Object options) {}

  public FieldServiceClient(String serviceHost) {}

  public native ResponseStream<FieldsChangeUpdate> listFields(
      ListFieldsRequest requestMessage, BrowserHeaders metadata);

  public native ResponseStream<FieldsChangeUpdate> listFields(ListFieldsRequest requestMessage);
}
