#!/bin/python3

import sys
import os
import socket
import random

from checksums import calc_checksum, validate_checksum, ChecksumException

BUFFER_SIZE = 16
HEADER_SIZE = 3


def create_chunks(data, size):
    n = len(data)
    offset = 0
    
    res = []
    while (offset < n):
        nxt_offset = min(offset + size, n)
        res.append(data[offset : nxt_offset])
        offset = nxt_offset

    return res


def create_datagram(data, ack_no):
    res = ack_no.to_bytes(1, "big") + data
    res = calc_checksum(res).to_bytes(2, "big") + res

    return res


def parse_data(data):
    if (not validate_checksum(data)):
        raise ChecksumException
    
    data = data[2 :]
    ack_no = int(data[0])

    return ack_no


def send_file(server_host, server_port, client_host, client_port, timeout, path_to_file):
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        s.bind((client_host, client_port))
        s.settimeout(timeout)
        print("socket created")
        
        with open(path_to_file, "rb") as f:
            chunks = create_chunks(f.read(), BUFFER_SIZE - HEADER_SIZE)
            ack_no = 0

            for i, data in enumerate(chunks):
                while True:
                    print(f"sending chunk #{i} with ack #{ack_no}")

                    try:
                        if (random.randint(0, 10) <= 2):
                            print("packet lost")
                            continue

                        cur_ack_no = ack_no if (i < len(chunks) - 1) else 2
                        s.sendto(create_datagram(data, cur_ack_no), (server_host, server_port))
                        
                        rc_data = s.recv(BUFFER_SIZE)
                        rc_ack_no = parse_data(rc_data)
                        print(f"received ack #{rc_ack_no}")
                        
                        if (rc_ack_no != cur_ack_no):
                            print("wrong ack")
                            continue

                        ack_no = 1 - ack_no
                        break

                    except socket.timeout:
                        print("timeout!")
                    
                    except ChecksumException:
                        print("checksum error!")

        print(f"file '{path_to_file}' sent")

    except Exception as e:
        print(e)


if (__name__ == "__main__"):
    server_host = sys.argv[1]
    server_port = int(sys.argv[2])
    client_host = sys.argv[3]
    client_port = int(sys.argv[4])
    timeout = float(sys.argv[5])
    path_to_file = sys.argv[6]

    send_file(server_host, server_port, client_host, client_port, timeout, path_to_file)