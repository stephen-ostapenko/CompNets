#!/usr/bin/python3

import sys
import time
from datetime import datetime
import socket

port = int(sys.argv[1])

with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as s:
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEPORT, 1)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    s.settimeout(1)
    s.bind(("", port))

    while True:
        message = bytearray(str(datetime.now()), "utf-8")
        s.sendto(message, ("<broadcast>", port))
        time.sleep(1)
