#!/bin/python3

import sys
import os
import socket
import random

from checksums import calc_checksum, validate_checksum, ChecksumException

BUFFER_SIZE = 16


def create_datagram(ack_no):
    res = ack_no.to_bytes(1, "big")
    res = calc_checksum(res).to_bytes(2, "big") + res
    
    return res


def parse_data(data):
    if (not validate_checksum(data)):
        raise ChecksumException
    
    data = data[2 :]
    ack_no = int(data[0])
    
    return ack_no, data[1 :]


def receive_file(host, port, path_to_file):
    try:
        with open(path_to_file, "w") as f:
            f.write("")

        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        s.bind((host, port))
        print("socket created")

        while True:
            try:
                data, address = s.recvfrom(BUFFER_SIZE)
                ack_no, data = parse_data(data)
                print(f"received data with ack #{ack_no}")

                if (random.randint(0, 10) <= 2):
                    print("packet lost")
                    continue

                with open(path_to_file, "ab") as f:
                    f.write(data)

                s.sendto(create_datagram(ack_no), address)
                print(f"sending ack #{ack_no}")

                if (ack_no == 2):
                    break
                
            except BlockingIOError:
                pass
            
            except ChecksumException:
                print("checksum error!")

        print(f"file '{path_to_file}' written")

    except Exception as e:
        print(e)


if (__name__ == "__main__"):
    host = sys.argv[1]
    port = int(sys.argv[2])
    path_to_file = sys.argv[3]

    receive_file(host, port, path_to_file)
