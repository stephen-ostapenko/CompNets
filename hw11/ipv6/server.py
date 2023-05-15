#!/usr/bin/python3

import sys
import socket

BUF_SIZE = 1024

host = "::1"
port = int(sys.argv[1])

with socket.socket(socket.AF_INET6, socket.SOCK_STREAM) as s:
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.setsockopt(socket.IPPROTO_IPV6, socket.IPV6_V6ONLY, 1)
    s.bind((host, port))
    s.listen(1)

    try:
        while True:
            try:
                conn, addr = s.accept()
                print(f"\nconnected by {addr}")

                with conn:
                    while True:
                        message = conn.recv(BUF_SIZE).decode()
                        if (not message or message == "."):
                            break

                        print(f"reveived '{message}'")
                        
                        message = message.upper()

                        print(f"sending '{message}'")
                        conn.sendall(bytearray(message, "utf-8"))

                print("done")

            except BrokenPipeError:
                print("pipe broken")

    except Exception as e:
        print(f"exception:\n {e}")

    finally:
        s.close()
