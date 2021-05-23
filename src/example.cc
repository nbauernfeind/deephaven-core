#include <iostream>

#include <arrow/type.h>
#include <arrow/flight/types.h>
#include <arrow/flight/client.h>

using arrow::Status;

namespace {

Status RunMain(int argc, char** argv) {
  const char *dest = argc <= 1 ? "grpc://localhost:10000" : argv[1];

  arrow::flight::Location workerDest;
  ARROW_RETURN_NOT_OK(arrow::flight::Location::Parse(dest, &workerDest));

  std::unique_ptr<arrow::flight::FlightClient> client;
  ARROW_RETURN_NOT_OK(arrow::flight::FlightClient::Connect(workerDest, &client));

  std::unique_ptr<arrow::flight::FlightListing> listing;
  ARROW_RETURN_NOT_OK(client->ListFlights(&listing));

  std::unique_ptr<arrow::flight::FlightInfo> nextInfo;
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

  return Status::OK();
}

}  // anonymous namespace

int main(int argc, char** argv) {
  Status st = RunMain(argc, argv);
  if (!st.ok()) {
    std::cerr << st << std::endl;
    return 1;
  }
  return 0;
}