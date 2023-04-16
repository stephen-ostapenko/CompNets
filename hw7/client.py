#!/usr/bin/python3

import sys
import time
from datetime import datetime
import socket
import numpy as np

BUFFER_SIZE = 1024
PACKETS_CNT = 10
TIMEOUT = 1

class Client:
    def __init__(self, server_host, server_port, client_host, client_port):
        self.server_host = server_host
        self.server_port = server_port
        self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.client_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.client_socket.bind((client_host, client_port))

        self.rtts = []


    def send(self, message):
        try:
            self.client_socket.settimeout(TIMEOUT)
            send_time = time.time()
            self.client_socket.sendto(message.encode(), (self.server_host, self.server_port))

            while True:
                received = self.client_socket.recv(BUFFER_SIZE)
                if (len(received) == 0):
                    continue
                
                recv_time = time.time()
                print(received.decode())
                
                self.rtts.append(1000 * (recv_time - send_time))
                print(f"rtt cur/min/avg/max = {self.rtts[-1]:.3f}/{np.min(self.rtts):.3f}/{np.mean(self.rtts):.3f}/{np.max(self.rtts):.3f} ms")

                break

        except socket.timeout:
            print("Request timed out")

        except Exception as e:
            print(f"error: {e}")

        finally:
            print()


if (__name__ == "__main__"):
    server_host = sys.argv[1]
    server_port = int(sys.argv[2])
    client_host = sys.argv[3]
    client_port = int(sys.argv[4])

    client = Client(server_host, server_port, client_host, client_port)

    for i in range(PACKETS_CNT):
        cur_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        client.send(f"Ping {i + 1} {cur_time}")

    print(f"--- {server_host} ping statistics ---")
    print(f"{PACKETS_CNT} packets transmitted, {len(client.rtts)} received, {100 * (1 - len(client.rtts) / PACKETS_CNT):.3f}% packet loss")
    print(f"rtt min/avg/max/mdev = {np.min(client.rtts):.3f}/{np.mean(client.rtts):.3f}/{np.max(client.rtts):.3f}/{np.abs(np.array(client.rtts) - np.array(client.rtts).mean()).mean():.3f} ms")
