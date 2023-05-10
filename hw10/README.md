# Лабораторная работа #10

*Степан Остапенко, гр 20.Б09-мкн*

## Wireshark. IP

Задание выполнялось на linux.

Запуск команды:
```text
stephen@flaaxbook:~$ traceroute akamai.com 56
traceroute to akamai.com (23.55.9.23), 30 hops max, 56 byte packets
 1  _gateway (192.168.0.1)  0.914 ms  0.886 ms  1.680 ms
 2  vlan591.schevchenko.bb.pu.ru (81.89.176.1)  2.959 ms  4.726 ms  4.667 ms
 3  vlan3.kronos.pu.ru (195.70.196.3)  4.302 ms  5.319 ms  5.261 ms
 4  spb-81-211-104-177.sovintel.ru (81.211.104.177)  13.238 ms  13.914 ms  14.475 ms
 5  MX01.Stockholm.gldn.net (79.104.229.53)  26.304 ms * *
 6  s-b5-link.ip.twelve99.net (62.115.44.72)  26.353 ms  20.936 ms  23.587 ms
 7  s-bb2-link.ip.twelve99.net (62.115.141.198)  21.487 ms s-bb2-link.ip.twelve99.net (80.91.253.226)  22.071 ms s-bb2-link.ip.twelve99.net (62.115.112.10)  22.373 ms
 8  kbn-bb6-link.ip.twelve99.net (62.115.139.173)  23.259 ms  24.126 ms  24.290 ms
 9  nyk-bb2-link.ip.twelve99.net (80.91.254.91)  106.967 ms  99.135 ms  105.888 ms
10  chi-b23-link.ip.twelve99.net (62.115.137.59)  114.868 ms  114.615 ms chi-bb2-link.ip.twelve99.net (62.115.132.135)  157.416 ms
11  sea-b1-link.ip.twelve99.net (62.115.132.155)  157.046 ms  157.583 ms  157.698 ms
12  akamai-ic-373659.ip.twelve99-cust.net (62.115.146.177)  159.837 ms  159.715 ms  158.717 ms
13  * * *
14  * * *
...
```

### 1

![1](pic1.png)

Видно, что адрес `192.168.0.101`.

### 2

`17`, код протокола UDP.

### 3

20 байт. На полезную нагрузку остается 36 байт.

### 4

![2](pic2.png)

a. Если пролистать запросы, можно увидеть, что постоянно меняются поля `Identification` и `Header Checksum`.
b. Не меняются поля `Version`, `Header Length`, `Differentiated Services Field`, `Total Length`, `Flags`, `Fragment Offset`, `Protocol`, `Source Address`, `Destination Address`, которые и не должны меняться. Должны меняться поля, указанные в пункте a. и поле `Time to Live`, которое меняется раз в 3 запроса (когда утилита переходит на очередную итерацию).
с. В моем случае значения выглядят случайными, так что никакой закономерности нет.

### 5

В поле `Identification` стоит идентификационный номер пакета (в данном случае `0x7ca2 (31906)`). В поле `Time to Live` стоит оставшееся время жизни пакета (сколько еще раз его можно переслать).

### 6

![3](pic3.png)

Поле `Identification` постоянно меняется, каждый раз увеличиваясь на 1. Поле `TTL` всегда равно 64.

Но, при этом, ICMP-сообщение содержит в себе заголовок IP-пакета, в ответ на который оно пришло, в том числе и исходные значения `Identification` и `TTL`.

### 7

![4](pic4.png)

Если взять произвольную серию, `Identification` снова случайно меняется каждый раз, а `TTL` всегда одинаковый в рамках одной серии (при этом, в разных сериях `TTL` бывает 239, 240, 241, 246, 254, ...).

### 8

Запуск команды:
```
stephen@flaaxbook:~$ traceroute akamai.com 3500
traceroute to akamai.com (23.55.9.23), 30 hops max, 3500 byte packets
 1  _gateway (192.168.0.1)  8.209 ms  13.803 ms  17.774 ms
 2  vlan591.schevchenko.bb.pu.ru (81.89.176.1)  23.245 ms  26.732 ms  29.861 ms
 3  vlan3.kronos.pu.ru (195.70.196.3)  34.770 ms  34.571 ms  37.972 ms
 4  spb-81-211-104-177.sovintel.ru (81.211.104.177)  48.499 ms  50.081 ms  52.357 ms
 5  MX01.Stockholm.gldn.net (79.104.229.55)  66.599 ms * *
 6  s-b5-link.ip.twelve99.net (62.115.44.72)  70.703 ms  22.639 ms  22.375 ms
 7  s-bb2-link.ip.twelve99.net (80.91.249.218)  23.754 ms  24.681 ms  25.290 ms
 8  * * *
 9  nyk-bb2-link.ip.twelve99.net (80.91.254.91)  116.161 ms  116.372 ms  118.975 ms
10  chi-b23-link.ip.twelve99.net (62.115.137.59)  129.732 ms chi-bb2-link.ip.twelve99.net (62.115.132.135)  159.129 ms  159.517 ms
11  sea-b1-link.ip.twelve99.net (62.115.132.155)  163.884 ms chi-b23-link.ip.twelve99.net (62.115.138.54)  127.879 ms sea-b1-link.ip.twelve99.net (62.115.132.155)  163.720 ms
12  akamai-ic-373659.ip.twelve99-cust.net (62.115.146.177)  198.006 ms sea-b1-link.ip.twelve99.net (62.115.132.155)  165.655 ms akamai-ic-373659.ip.twelve99-cust.net (62.115.146.177)  173.306 ms
13  * * *
14  * * *
...
```

![5](pic5.png)

a. Сам ICMP-ответ не фрагментируется. А вот сообщение, уходящее с компьютера фрагментируется на 3 части, как видно на картинке.
b. Меняются поля `Flags` и `Fragment Offset`, отвечающие за фрагментацию. Также меняется поле `Total Length`, т. к. размер последнего фрагмента отличается.
