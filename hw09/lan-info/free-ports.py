#!/bin/python3

import sys
import socket

host = sys.argv[1]
first_port = int(sys.argv[2])
last_port = int(sys.argv[3])

def check_port(port):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        return s.connect_ex((host, port)) != 0

print(f"free ports list: {list(filter(check_port, range(first_port, last_port + 1)))}")