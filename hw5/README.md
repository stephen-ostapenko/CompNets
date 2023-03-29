# Лабораторная работа #5
*Степан Остапенко, гр 20.Б09-мкн*

## Задание 1

### 2. Удаленный запуск команд

Клиент и сервер для удаленного запуска команд, реализованные на python, находятся в папке [`remote-control`](./remote-control).

#### Сервер

Запуск:
```shell
./server.py <host> <port>
```

#### Клиент

Запуск:
```shell
./client.py <host> <port> <command with args>
```

#### Пример работы

![remote-control](./assets/task2.png)

### 3. Широковещательная рассылка через UDP

Клиент и сервер для широковещательной рассылки, реализованные на python, находятся в папке [`broadcast`](./broadcast).

#### Сервер

Запуск:
```shell
./server.py <port>
```

#### Клиент

Запуск:
```shell
./client.py <port>
```

#### Пример работы

![broadcast](./assets/task3.png)
