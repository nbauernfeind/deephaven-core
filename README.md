deephaven-cpp
=============

To run on osx:
```
# install dependencies
$ brew install openssl
$ brew install apache-arrow

$ mkdir build
$ cd build

# you likely need to export the path to the ssl root dir
$ cmake -DOPENSSL_ROOT_DIR=/usr/local/opt/openssl -DOPENSSL_LIBRARIES=/usr/local/opt/openssl/lib ..
$ make

# finally run the generated example:
$ bin/example
```
