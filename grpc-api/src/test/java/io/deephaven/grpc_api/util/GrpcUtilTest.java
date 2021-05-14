package io.deephaven.grpc_api.util;

import com.google.protobuf.ByteString;
import io.deephaven.base.verify.Assert;
import org.apache.arrow.flight.impl.Flight;
import org.junit.Test;

import java.util.function.Consumer;

public class GrpcUtilTest {
   @Test
   public void testLongToByteStringConversion() {
      final Consumer<Long> testLong = (id) -> {
         Assert.eq((long) id, "id", GrpcUtil.byteStringToLong(GrpcUtil.longToByteString(id)), "GrpcUtil.byteStringToLong(GrpcUtil.longToByteString(id))");
      };
      testLong.accept(0L);
      testLong.accept(1024L);
      testLong.accept(-1L);
      testLong.accept(7L);
      testLong.accept(Long.MAX_VALUE);
      testLong.accept(Long.MIN_VALUE);
   }

   @Test
   public void testTicketProtobufConversion() {
      final Consumer<Long> testLong = (id) -> {
         final ByteString bsid = GrpcUtil.longToByteString(id);
         Assert.eq(8, "8", bsid.size(), "bsid.size()");
         final Flight.Ticket ticket = Flight.Ticket.newBuilder().setTicket(bsid).build();
         Assert.eq(8, "8", ticket.getTicket().size(), "ticket.getId().size()");
         Assert.eq((long) id, "id", GrpcUtil.byteStringToLong(ticket.getTicket()), "GrpcUtil.byteStringToLong(ticket.getTicket())");
      };
      testLong.accept(0L);
      testLong.accept(1024L);
      testLong.accept(-1L);
      testLong.accept(7L);
      testLong.accept(Long.MAX_VALUE);
      testLong.accept(Long.MIN_VALUE);
   }
}
