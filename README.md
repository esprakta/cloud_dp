# ☁️ Дипломная работа: Облачное хранилище файлов

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.x-4FC08D.svg)](https://vuejs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791.svg)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg)](https://www.docker.com/)

Полнофункциональное облачное хранилище файлов, разработанное в качестве дипломного проекта. Приложение позволяет пользователям авторизовываться и управлять своими файлами (загрузка, скачивание, переименование, удаление) через современный веб-интерфейс.

---

## 📑 Оглавление
1. [Архитектура приложения](#-архитектура-приложения)
2. [Хранение данных и настроек](#-хранение-данных-и-настроек)
3. [Структура базы данных](#-структура-базы-данных)
4. [Быстрый старт](#-быстрый-старт)
5. [Тестирование](#-тестирование)

---

## 🏗 Архитектура приложения

Приложение построено по классической клиент-серверной архитектуре с четким разделением ответственности:


```mermaid
graph TD
    Client[🌐 Клиент: Vue.js 3 + TypeScript]  -->  |HTTP/REST API + Cookies| Backend
    Backend[⚙️ Сервер: Spring Boot 3] -->|JPA/Hibernate| DB[(🗄️ PostgreSQL)]
    Backend -->|File I/O| FS[📁 Файловая система: /upload]
    
    subgraph   Docker Environment
        Backend
        DB
        FS
    end
