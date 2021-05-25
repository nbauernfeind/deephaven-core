#include <iostream>
#include <memory>
#include <string>

#include <grpcpp/grpcpp.h>

#include <proto/session.pb.h>
#include <proto/session.grpc.pb.h>

using grpc::Channel;
using grpc::ClientContext;
using grpc::Status;
using io::deephaven::proto::backplane::grpc::SessionService;
using io::deephaven::proto::backplane::grpc::HandshakeRequest;
using io::deephaven::proto::backplane::grpc::HandshakeResponse;

namespace {

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

      Status status = stub_->newSession(&ctxt, req, &res);

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
  //std::shared_ptr<Channel> channel = grpc::CreateChannel(dest, grpc::InsecureChannelCredentials());
  Client api(grpc::CreateChannel(dest, grpc::InsecureChannelCredentials()));

  api.newSession();
  //return Status::OK();
  return;
}

}  // anonymous namespace

int main(int argc, char** argv) {
  try {
    const char *dest = argc <= 1 ? "grpc://localhost:10000" : argv[1];
    RunMain(dest);
  } catch (...) {
    std::cerr << "Unknown Error" << std::endl;
  }
}
