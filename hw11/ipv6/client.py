#!/usr/bin/python3

import sys
import socket

BUF_SIZE = 1024

host = sys.argv[1]
port = int(sys.argv[2])

with socket.socket(socket.AF_INET6, socket.SOCK_STREAM) as s:
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.setsockopt(socket.IPPROTO_IPV6, socket.IPV6_V6ONLY, 1)
    s.connect((host, port))
    
    try:
        while True:
            message = input("<- ")
            s.sendall(bytearray(message, "utf-8"))
            if (message == "."):
                break
            
            data = s.recv(BUF_SIZE)
            print(f"-> {data.decode()}", flush = True)

    finally:
        s.close()
