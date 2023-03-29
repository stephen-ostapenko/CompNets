#!/usr/bin/python3

import sys
import socket

BUF_SIZE = 1024

host = sys.argv[1]
port = int(sys.argv[2])
cmd = sys.argv[3]

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
    s.connect((host, port))
    
    try:
        s.sendall(bytearray(cmd, "utf-8"))
        
        data = s.recv(BUF_SIZE)
        while (data):
            print(data.decode(), end = "", flush = True)
            data = s.recv(BUF_SIZE)

    finally:
        s.close()
