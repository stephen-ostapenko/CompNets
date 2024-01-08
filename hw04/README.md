# Лабораторная работа #4
*Степан Остапенко, гр 20.Б09-мкн*

## Задание 1

Прокси-сервер на kotlin находится в папке `hw4`.

Запуск производится с помощью
```shell
./gradlew run --args="<port> <path to log file> <path to blacklist> <path to cache storage>"
```

### Логирование

Все запросы к серверу записываются в лог-файл. Логирование реализовано в файле `Logger.kt`.

Пример лог-файла:
```text
new logger created at 2023-03-22T15:49:13.419577901

2023-03-22T15:49:41.525427188: serving request from /0:0:0:0:0:0:0:1
2023-03-22T15:49:41.570634984: GET http://google.com
2023-03-22T15:49:41.847018757: 301: Moved Permanently

2023-03-22T15:49:50.093425206: serving request from /0:0:0:0:0:0:0:1
2023-03-22T15:49:50.097221538: GET http://www.google.com
2023-03-22T15:49:50.424189314: 200: OK

2023-03-22T15:50:32.938329210: serving request from /0:0:0:0:0:0:0:1
2023-03-22T15:50:32.967403483: GET vk.com
2023-03-22T15:50:32.967645573: 403: Forbidden

2023-03-22T15:50:38.938329210: serving request from /0:0:0:0:0:0:0:1
2023-03-22T15:50:38.967403483: GET www.youtube.com/@mkn-sp-20
2023-03-22T15:50:38.967645573: 403: Forbidden

2023-03-22T15:50:46.284807664: serving request from /0:0:0:0:0:0:0:1
2023-03-22T15:50:46.285847957: GET http://www.youtube.com
2023-03-22T15:50:46.328973310: 301: Moved Permanently

2023-03-22T16:02:55.261389054: serving request from /127.0.0.1
2023-03-22T16:02:55.308552851: GET http://gaia.cs.umass.edu/wireshark-labs/HTTP-wireshark-file2.html
2023-03-22T16:02:55.756435167: 200: OK

2023-03-22T16:03:43.834303076: serving request from /127.0.0.1
2023-03-22T16:03:43.905414652: GET http://gaia.cs.umass.edu/wireshark-labs/HTTP-wireshark-file2.html
2023-03-22T16:03:44.367288731: 304: Not Modified
2023-03-22T16:03:44.367907074: using cached response

2023-03-22T16:05:54.747060212: serving request from /127.0.0.1
2023-03-22T16:05:54.750121292: GET http://gaia.cs.umass.edu/wireshark-labs/HTTP-wireshark-file4.html
2023-03-22T16:05:55.132740267: 200: OK

2023-03-22T16:12:16.166554709: serving request from /127.0.0.1
2023-03-22T16:12:16.170452719: GET http://gaia.cs.umass.edu/wireshark-labs/HTTP-wireshark-file4.html
2023-03-22T16:12:16.576311549: 304: Not Modified
2023-03-22T16:12:16.576543106: using cached response

2023-03-22T16:33:26.761741904: serving request from /0:0:0:0:0:0:0:1
2023-03-22T16:33:26.808450847: POST http://httpbin.org/post
2023-03-22T16:33:27.652770442: 200: OK

2023-03-22T16:44:54.854812704: serving request from /0:0:0:0:0:0:0:1
2023-03-22T16:44:54.983009983: PUT http:///httpbin.org/post
2023-03-22T16:44:54.983258828: 405: Method Not Allowed

2023-03-22T16:50:20.423330180: serving request from /127.0.0.1
2023-03-22T16:50:20.576214202: GET http://gaia.cs.umass.edu/wireshark-labs/HTTP-wireshark-file5.html
2023-03-22T16:50:20.895369943: 200: OK

2023-03-22T16:50:21.256089622: serving request from /127.0.0.1
2023-03-22T16:50:21.261389962: GET http://gaia.cs.umass.edu/wireshark-labs/banner.jpg
2023-03-22T16:50:21.649028731: 200: OK

2023-03-22T16:50:21.656907368: serving request from /127.0.0.1
2023-03-22T16:50:21.682810041: GET http://favicon.ico
2023-03-22T16:50:21.691448252: 404: Not Found

2023-03-22T16:50:46.256681755: serving request from /127.0.0.1
2023-03-22T16:50:46.391695062: GET http://gaia.cs.umass.edu/wireshark-labs/HTTP-wireshark-file5.html
2023-03-22T16:50:46.722882494: 304: Not Modified
2023-03-22T16:50:46.725403046: using cached response

2023-03-22T16:50:47.136776863: serving request from /127.0.0.1
2023-03-22T16:50:47.150231869: GET http://gaia.cs.umass.edu/wireshark-labs/banner.jpg
2023-03-22T16:50:47.446048669: 304: Not Modified
2023-03-22T16:50:47.446646369: using cached response

2023-03-22T16:50:47.451670108: serving request from /127.0.0.1
2023-03-22T16:50:47.460072949: GET http://favicon.ico
2023-03-22T16:50:47.465860059: 404: Not Found
```

### Черный список

Черный список реализован в файле `Filter.kt`.

Ограничения задаются в json-файле с помощью списка:
```json
[
  {
    "address": "vk.com",
    "banHost": true
  },
  {
    "address": "www.youtube.com/@mkn-sp-20"
  }
]
```

Для каждого ограничения надо указать адрес. По умолчанию блокируется в точности та страница, которая была указана в файле. Если надо заблокировать сразу весь ресурс, нужно дополнительно указать `"banHost": true`.

Примеры работы черного списка можно найти в лог-файле, указанном выше.

### Кеширование

Кэш реализован в файле `Cache.kt`. В качестве параметров запуска нужно указать путь к папке, в которой будут сохраняться сохраненные запросы.

В кеш сохраняются только те запросы метода `GET`, которые имеют код возврата 200. Запросы метода `POST` в кеш не сохраняются.

Когда сервер возвращает сохраненный ранее запрос, в лог выводится фраза "using cached response".