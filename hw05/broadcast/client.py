#!/usr/bin/python3

import sys
import time
from datetime import datetime
import socket

port = int(sys.argv[1])

with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as s:
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEPORT, 1)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    s.bind(("", port))

    while True:
        data, addr = s.recvfrom(1024)
        print(f"received '{data.decode()}' from {addr}")
