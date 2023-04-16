#!/usr/bin/python3

import sys
import time
from datetime import datetime
import socket

class Client:
    def __init__(self, server_host, server_port, client_host, client_port):
        self.server_host = server_host
        self.server_port = server_port
        self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.client_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.client_socket.bind((client_host, client_port))


    def send(self, message):
        try:
            self.client_socket.sendto(message.encode(), (self.server_host, self.server_port))

        except Exception as e:
            print(f"error: {e}")


if (__name__ == "__main__"):
    server_host = sys.argv[1]
    server_port = int(sys.argv[2])
    client_host = sys.argv[3]
    client_port = int(sys.argv[4])

    client = Client(server_host, server_port, client_host, client_port)

    cur_id = 0
    while True:
        cur_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        client.send(f"{cur_id} {cur_time}")

        cur_id += 1
        time.sleep(0.1)
