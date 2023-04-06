# Лабораторная работа #6
*Степан Остапенко, гр 20.Б09-мкн*

## Задание 2

### 2. FTP клиент

Интерфейс FTP клиента находится в папке [`FtpClientCore`](./ftp-client/FtpClientCore). Консольная реализация находится в папке [`ConsoleClient`](./ftp-client/ConsoleClient).

Запуск с помощью gradle:

* получить список папок и файлов
```shell
./gradlew ConsoleClient:run --args="list -h <host> [-P <port>] -u <username> -p <password> -f <path to file or folder on server>"
```

* вывести содержимое файла в консоль
```shell
./gradlew ConsoleClient:run --args="read -h <host> [-P <port>] -u <username> -p <password> -f <path to file>"
```

* скачать файл с сервера
```shell
./gradlew ConsoleClient:run --args="download -h <host> [-P <port>] -u <username> -p <password> -f <path to file on server>:<path to file on local machine>"
```

* загрузить файл на сервер
```shell
./gradlew ConsoleClient:run --args="upload -h <host> [-P <port>] -u <username> -p <password> -f <path to file on server>:<path to file on local machine>"
```

### 3. GUI FTP клиент

Клиент с графическим интерфейсом находится в текущей [папке](./).

Запуск с помощью gradle:
```shell
./gradlew :run
```