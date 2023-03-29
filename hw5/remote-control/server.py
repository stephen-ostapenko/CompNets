#!/usr/bin/python3

import sys
import socket
import subprocess

BUF_SIZE = 1024

host = sys.argv[1]
port = int(sys.argv[2])

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.bind((host, port))
    s.listen()

    try:
        while True:
            try:
                conn, addr = s.accept()
                print(f"\nConnected by {addr}")

                with conn:    
                    cmd = conn.recv(BUF_SIZE).decode()
                    print(f"executing '{cmd}'")
                    
                    proc = subprocess.Popen(cmd, shell = True, stdout = subprocess.PIPE, stderr = subprocess.PIPE)
                    
                    for line in proc.stdout:
                        conn.sendall(line)

                    for line in proc.stderr:
                        conn.sendall(line)

                print("execution finished")

            except BrokenPipeError:
                print("pipe broken")

    except Exception as e:
        print(f"Exception:\n {e}")

    finally:
        s.close()
