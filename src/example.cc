#include <iostream>
#include <memory>
#include <string>

#include <grpcpp/grpcpp.h>

#include <proto/session.pb.h>
#include <proto/session.grpc.pb.h>

#include <arrow/type.h>
#include <arrow/flight/types.h>
#include <arrow/flight/client.h>

using grpc::Channel;
using grpc::ClientContext;
using arrow::Status;
using io::deephaven::proto::backplane::grpc::SessionService;
using io::deephaven::proto::backplane::grpc::HandshakeRequest;
using io::deephaven::proto::backplane::grpc::HandshakeResponse;

namespace {

Status FlightDemo(const char* dest) {
  arrow::flight::Location workerDest;
  ARROW_RETURN_NOT_OK(arrow::flight::Location::Parse(dest, &workerDest));

  std::unique_ptr<arrow::flight::FlightClient> client;
  ARROW_RETURN_NOT_OK(arrow::flight::FlightClient::Connect(workerDest, &client));

  std::unique_ptr<arrow::flight::FlightListing> listing;
  ARROW_RETURN_NOT_OK(client->ListFlights(&listing));

  std::unique_ptr<arrow::flight::FlightInfo> nextInfo;

  std::cout << "Flight List: " << std::endl;
  do {
    ARROW_RETURN_NOT_OK(listing->Next(&nextInfo));
    if (!nextInfo) {
      break;
    }

    arrow::ipc::DictionaryMemo memo;
    std::shared_ptr<arrow::Schema> schema;

    std::cout << "Descriptor: " << nextInfo->descriptor().ToString() << std::endl;
    std::cout << "TotalRecords: " << nextInfo->total_records() << std::endl;
    std::cout << "TotalBytes: " << nextInfo->total_bytes() << std::endl;
    for (const arrow::flight::FlightEndpoint fe : nextInfo->endpoints()) {
      std::cout << "\tEndPoint: " << fe.ticket.ticket << std::endl;
    }
    std::cout << std::endl;

    // This should work; it claims that our flatbuffer is invalid:
    // ARROW_RETURN_NOT_OK(nextInfo->GetSchema(&memo, &schema));
    // std::cout << "Schema: " << schema->ToString() << std::endl;
  } while (true);
  std::cout << "(complete)" << std::endl;

  return Status::OK();
}

class Client {
  public:
    Client(std::shared_ptr<Channel> channel)
      : stub_(SessionService::NewStub(channel))
    {}

    void newSession() {
      HandshakeRequest req;
      req.set_auth_protocol(1);

      HandshakeResponse res;

      ClientContext ctxt;

      grpc::Status status = stub_->newSession(&ctxt, req, &res);

      if (status.ok()) {
        std::cout << "New Session Established: " << res.session_token() << std::endl;
      } else {
        std::cout << status.error_code() << ": " << status.error_message() << std::endl;
      }
    }

  private:
    std::unique_ptr<SessionService::Stub> stub_;
};

void RunMain(const char *dest) {
  Client api(grpc::CreateChannel(dest, grpc::InsecureChannelCredentials()));

  api.newSession();
  return;
}

}  // anonymous namespace

int main(int argc, char** argv) {
  try {
    const char *dest = argc <= 1 ? "grpc://localhost:10000" : argv[1];
    Status st = FlightDemo("grpc://localhost:10000");
    if (!st.ok()) {
        std::cerr << st << std::endl;
        return 1;
    }

    RunMain(dest);
  } catch (...) {
    std::cerr << "Unknown Error" << std::endl;
  }
}
