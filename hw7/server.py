#!/usr/bin/python3

import sys
from datetime import datetime
import random
import socket

BUFFER_SIZE = 1024

class Server:
    def __init__(self, host, port, timeout):
        self.host = host
        self.port = port
        self.timeout = timeout


    def run(self):
        server_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        print(f"server running on {self.host}:{self.port}")

        try:
            server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            server_socket.bind((self.host, self.port))
            server_socket.setblocking(False)
            connections = dict()

            while True:
                try:
                    message, address = server_socket.recvfrom(BUFFER_SIZE)
                    if (address not in connections):
                        print(f"new connection with {address}")

                    print(f"received message '{message.decode()}' from {address}")
                    connections[address] = datetime.now()

                    if (random.randint(0, 10) < 2):
                        print(f"message to {address} lost")
                        continue
                    
                    response = message.upper()
                    
                    print(f"sending response '{response.decode()}' to {address}")
                    server_socket.sendto(response, address)

                except BlockingIOError:
                    closed_connections = []
                    for address, t in connections.items():
                        cur_time = datetime.now()
                        if ((cur_time - t).total_seconds() > self.timeout):
                            closed_connections.append(address)
                    
                    for address in closed_connections:
                        print(f"connection with {address} closed")
                        connections.pop(address)

        except Exception as e:
            print(f"error: {e}")
        
        finally:
            server_socket.close()


if (__name__ == "__main__"):
    host = sys.argv[1]
    port = int(sys.argv[2])
    timeout = int(sys.argv[3])

    server = Server(host, port, timeout)
    
    try:
        server.run()
    
    except KeyboardInterrupt:
        pass
