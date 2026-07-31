# RelayChat

> Spring Boot와 WebSocket 기반의 실시간 채팅 시스템을 시작으로,
> Redis, Kafka를 활용한 분산 메시징 환경과 메시지 신뢰성, 장애 대응을 검증하는 백엔드 프로젝트입니다.

---

## 📖 Project Overview

RelayChat은 단순히 채팅 기능을 구현하는 프로젝트가 아니고 강의를 기반으로 실시간 채팅 시스템을 구현한 뒤, 

분산 환경에서 발생할 수 있는 다양한 상황을 직접 구현하고 검증하는 것을 목표로 합니다.

주요 관심사는 다음과 같습니다.

- 메시지 신뢰성
- 장애 대응
- 운영 모니터링
- 성능 검증
- 기술 선택과 트레이드오프 분석

---

## ✨ Features

### 현재 구현

- WebSocket 기반 실시간 채팅 서버

### 구현 예정

- 1:1 및 그룹 채팅
- Redis Session
- Redis Cache
- Kafka 기반 메시지 처리
- Docker Compose 기반 다중 서버
- Nginx Load Balancing
- 메시지 멱등성
- 누락 메시지 복구
- 장애 대응 시나리오
- Micrometer · Prometheus · Grafana
- JMeter 성능 테스트

---

## 🛠 Tech Stack

| 구분 | 기술 스택 (Tech Stack) |
| :--- | :--- |
| **Backend** | `Java`, `Spring Boot`, `WebSocket` |
| **Database** | `MySQL` |
| **Infra** | `Docker Compose`, `Nginx` |
| **Message Broker** | `Redis`, `Kafka` |
| **Monitoring** | `Micrometer`, `Prometheus`, `Grafana` |

---

## 🎯 Project Goal

RelayChat은 다음과 같은 내용을 직접 구현하고 검증하는 것을 목표로 합니다.

- 실시간 메시지 처리
- 분산 환경에서 메시지 전달
- 장애 발생 시 복구 과정
- 메시지 중복 및 유실 방지
- 운영 환경 모니터링
- 성능 측정 및 개선

---

## 📚 Documentation

프로젝트가 진행됨에 따라 다음 내용을 지속적으로 문서화할 예정입니다.

- Architecture
- Package Structure
- Message Flow
- Data Flow
- Trouble Shooting
- Performance Test
- Failure Scenario
