// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: io/grpc/channelz.proto

package io.grpc.channelz.v1;

public interface SocketRefOrBuilder extends
    // @@protoc_insertion_point(interface_extends:grpc.channelz.SocketRef)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>int64 socket_id = 3;</code>
   */
  long getSocketId();

  /**
   * <pre>
   * An optional name associated with the socket.
   * </pre>
   *
   * <code>string name = 4;</code>
   */
  java.lang.String getName();
  /**
   * <pre>
   * An optional name associated with the socket.
   * </pre>
   *
   * <code>string name = 4;</code>
   */
  com.google.protobuf.ByteString
      getNameBytes();
}