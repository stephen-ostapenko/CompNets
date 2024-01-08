def create_chunks(data, size):
    n = len(data)
    offset = 0
    
    res = []
    while (offset < n):
        nxt_offset = min(offset + size, n)
        res.append(data[offset : nxt_offset])
        offset = nxt_offset

    return res


SIZE = 16


def calc_checksum(data):
    chunks = create_chunks(data, 2)
    checksum = 0
    
    for chunk in chunks:
        checksum += int.from_bytes(chunk, "big")
        checksum %= (1 << SIZE)

    return (1 << SIZE) - 1 - checksum


def validate_checksum(data):
    return calc_checksum(data) == 0


class ChecksumException(Exception):
    pass