#!/bin/python3

import sys
import netifaces

info = netifaces.ifaddresses(sys.argv[1])[netifaces.AF_INET][0]

print(f"address: {info['addr']}")
print(f"mask: {info['netmask']}")