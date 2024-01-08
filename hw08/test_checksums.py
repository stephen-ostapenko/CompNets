#!/bin/python3

import numpy as np

from checksums import calc_checksum, validate_checksum


def test_correct():
    data = "Hello, World!".encode()
    data = calc_checksum(data).to_bytes(2, "big") + data

    assert(validate_checksum(data))


def test_wrong():
    data = "Hello, World!".encode()
    data = calc_checksum(data).to_bytes(2, "big") + "random text".encode()

    assert(not validate_checksum(data))


def test_bit_distortion():
    data = "Hello, World!".encode()
    chsum = calc_checksum(data).to_bytes(2, "big")
    
    data = list(data.decode())
    data[2] = chr(4)
    data = "".join(data).encode()
    
    data = chsum + data

    assert(not validate_checksum(data))


def test_big():
    np.random.seed(2)
    
    data = bytes(list(np.random.randint(256, size = 2 ** 20)))
    data = calc_checksum(data).to_bytes(2, "big") + data
    assert(validate_checksum(data))


if (__name__ == "__main__"):
    test_correct()
    test_wrong()
    test_bit_distortion()
    test_big()

    print("all tests passed!")
