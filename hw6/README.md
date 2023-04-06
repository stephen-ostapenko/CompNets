# Лабораторная работа #6
*Степан Остапенко, гр 20.Б09-мкн*

## Задание 2

### 1. FileZilla сервер и клиент

#### Подключение к серверу от лица администратора

![1](./assets/task1-1.png)

#### Создание тестового пользователя

![2](./assets/task1-2.png)

#### Клиент Filezilla

![3](./assets/task1-3.png)

#### Вход в учетную запись тестового пользователя

![4](./assets/task1-4.png)

#### Просмотр списка файлов

![5](./assets/task1-5.png)

#### Создание папки

![6](./assets/task1-6.png)

#### Загрузка файла на сервер

![7](./assets/task1-7.png)

#### Скачивание файла с сервера

![8](./assets/task1-8.png)

#### Панель администратора после всех операций

![9](./assets/task1-9.png)

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

Клиент с графическим интерфейсом находится в папке [`ftp-client`](./ftp-client) (код в файле [`Main.kt`](./ftp-client/src/jvmMain/kotlin/Main.kt)).

Запуск с помощью gradle:
```shell
./gradlew :run
```

![task3](./assets/task3.png)
