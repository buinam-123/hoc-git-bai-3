# Máy tính vi dịch vụ — phiên bản Java (Spring Boot + Kafka)

**Mỗi phép tính là một microservice riêng**, giao tiếp qua **Kafka**, đóng gói bằng **Docker**, triển khai bằng **Kubernetes**, có **frontend** web.

| Phép tính | Module | Cổng khi chạy local | Topic yêu cầu |
|---|---|---|---|
| Cộng | `add-service` | 8101 | `calc.add.requests` |
| Trừ | `subtract-service` | 8102 | `calc.subtract.requests` |
| Nhân | `multiply-service` | 8103 | `calc.multiply.requests` |
| Chia | `divide-service` | 8104 | `calc.divide.requests` |
| Căn bậc 2 | `sqrt-service` | 8105 | `calc.sqrt.requests` |
| Lũy thừa | `power-service` | 8106 | `calc.power.requests` |
| Giai thừa | `factorial-service` | 8107 | `calc.factorial.requests` |
| Gateway + giao diện | `gateway` | **3000** | `calc.results`, `calc.heartbeat` |

## Kiến trúc

```
Trình duyệt ─► Gateway (Spring MVC) ── produce {id, op, a, b} ─► Kafka  calc.<op>.requests
                  ▲                                                        │
                  │                                                        ▼
                  │                                              <op>-service (Spring Kafka listener)
                  │                                                        │ produce {id, ok, result|error}
                  └──── ghép theo id ◄── Kafka  calc.results ◄─────────────┘
```

- **Request/Reply qua Kafka**: gateway gửi yêu cầu kèm `id`, giữ HTTP request (không chặn thread, dùng `CompletableFuture`) và chờ kết quả cùng `id` ở `calc.results`. Quá 8 giây → HTTP 504.
- **Mở rộng ngang**: các bản sao của một service dùng chung consumer group `calc-<op>-service`, Kafka chia 3 partition giữa chúng (tối đa 3 bản sao có việc để làm). Mỗi replica của gateway có consumer group riêng để nhận mọi kết quả.
- **Heartbeat**: mỗi service gửi nhịp tim vào `calc.heartbeat` mỗi 5 giây; `GET /api/services` dùng nó để báo trạng thái cho frontend.
- **Chính xác**: cộng, trừ, nhân, chia dùng `BigDecimal` (0,1 + 0,2 = 0,3). Lũy thừa với số nguyên và giai thừa dùng `BigInteger` nên chính xác tuyệt đối (giai thừa tối đa n = 3000).
- **Lỗi nghiệp vụ** (chia cho 0, căn số âm...) trả HTTP 200 với `ok:false` và thông báo tiếng Việt.
- **Health/Readiness**: Spring Actuator (`/actuator/health/readiness`), readiness chỉ "xanh" khi Kafka listener đã chạy.

## Cấu trúc

```
pom.xml            Maven cha (Spring Boot 3.5, Java 17)
common/            Thư viện dùng chung: message, topic, Num (BigDecimal), RequestListener, Heartbeat
*-service/         7 service: mỗi cái chỉ có 1 lớp Operation + 1 lớp Application + application.yml
gateway/           REST ⇄ Kafka, đồng thời phục vụ giao diện web
frontend/          HTML/CSS/JS + nginx (dùng cho Docker/K8s)
k8s/               Manifest Kubernetes (kustomize); optional/ có Ingress và HPA
Dockerfile         Một Dockerfile nhiều giai đoạn dùng chung cho cả 8 ứng dụng
docker-compose.yml
```

---

## Cách A — Chạy trực tiếp trong IntelliJ (không cần Docker)

Yêu cầu: JDK 17 trở lên (IntelliJ có thể tải giúp: *File → Project Structure → SDK → Download JDK*).

### 1. Mở dự án
*File → Open →* chọn file `pom.xml` ở thư mục gốc → **Open as Project**. Đợi IntelliJ import Maven xong (thanh tiến trình dưới cùng). Nếu IntelliJ hỏi, chọn **Trust Project**.

### 2. Chạy Kafka
Cần một Kafka chạy ở `localhost:9092`. Chọn một trong hai:

**Cách 2a — Có Docker chạy được (chỉ để chạy Kafka):**
```powershell
docker run -d --name kafka -p 9092:9092 apache/kafka:3.8.0
```

**Cách 2b — Không dùng Docker (Windows):**
1. Tải `kafka_2.13-3.8.0.tgz` ở https://kafka.apache.org/downloads và giải nén vào thư mục đường dẫn ngắn, ví dụ `C:\kafka` (tránh thư mục có dấu cách).
2. Mở `C:\kafka\config\kraft\server.properties`, sửa dòng `log.dirs` thành `log.dirs=C:/kafka/data`.
3. Mở CMD tại `C:\kafka` và chạy lần lượt:
```bat
bin\windows\kafka-storage.bat random-uuid
bin\windows\kafka-storage.bat format -t <UUID-vừa-in-ra> -c config\kraft\server.properties
bin\windows\kafka-server-start.bat config\kraft\server.properties
```
Giữ cửa sổ này mở. Lần sau chỉ cần chạy lệnh cuối (không format lại).

### 3. Chạy các ứng dụng
Mở từng lớp sau và bấm mũi tên xanh cạnh `main` → **Run**:

- `add-service/.../AddServiceApplication`
- `subtract-service/.../SubtractServiceApplication`
- `multiply-service/.../MultiplyServiceApplication`
- `divide-service/.../DivideServiceApplication`
- `sqrt-service/.../SqrtServiceApplication`
- `power-service/.../PowerServiceApplication`
- `factorial-service/.../FactorialServiceApplication`
- `gateway/.../GatewayApplication`

Mẹo: sau khi chạy lần đầu, IntelliJ gom chúng trong cửa sổ **Services** (`Alt+8`) để bật/tắt cả nhóm.

### 4. Dùng thử
Mở http://localhost:3000 (gateway phục vụ luôn giao diện). Góc phải trên báo "7/7 service đang chạy" khi mọi thứ đã sẵn sàng (đợi khoảng 10–20 giây sau khi bật service cuối).

Gọi API trực tiếp:
```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:3000/api/calculate -ContentType 'application/json' -Body '{"op":"power","a":2,"b":100}'
```

---

## Cách B — Docker Compose

Yêu cầu Docker Desktop đang chạy và máy còn trống khoảng 4 GB RAM (8 tiến trình Java + Kafka).

```powershell
docker compose up --build -d
docker compose ps
```
Lần build đầu mất vài phút vì Maven tải thư viện. Giao diện: http://localhost:8080 (nginx) hoặc http://localhost:3000 (gateway). Kafka UI: `docker compose --profile tools up -d` → http://localhost:8090. Dừng: `docker compose down`.

## Cách C — Kubernetes (Docker Desktop, kind, minikube)

```powershell
# 1. Build image
foreach ($m in "add-service","subtract-service","multiply-service","divide-service","sqrt-service","power-service","factorial-service") {
  docker build -t calc/${m}:1.0.0 --build-arg MODULE=$m .
}
docker build -t calc/gateway:1.0.0 --build-arg MODULE=gateway --build-arg SERVER_PORT=3000 .
docker build -t calc/frontend:1.0.0 frontend

# 2. (kind / minikube) nạp image vào cluster. Docker Desktop Kubernetes thì bỏ qua bước này.
#    kind load docker-image calc/add-service:1.0.0 ...   |   minikube image load calc/add-service:1.0.0 ...

# 3. Triển khai
kubectl apply -k k8s/base
kubectl -n calculator get pods -w
```
Khi mọi pod `1/1 Running` (Kafka lên trước, Java mất ~20–40 giây), mở http://localhost:30080 hoặc:
```powershell
kubectl -n calculator port-forward svc/frontend 8080:80
```
Mở rộng một service: `kubectl -n calculator scale deploy/factorial-service --replicas=3`. Tùy chọn: `kubectl apply -f k8s/optional/hpa.yaml` (cần metrics-server), `ingress.yaml` (cần Ingress controller). Xóa: `kubectl delete -k k8s/base`.

> Kafka trong manifest chỉ có 1 broker để chạy thử. Production: 3 broker, `TOPIC_REPLICATION=3`.

## Kiểm thử

```powershell
mvn test            # hoặc chạy trong IntelliJ: Maven → Lifecycle → test
```
Gồm unit test cho 7 phép tính (không cần Kafka) và test cho `PendingRequests`/`ServiceRegistry` của gateway.

## Thêm một phép tính mới (ví dụ `mod`)

1. Sao chép thư mục `add-service` thành `mod-service`, đổi `artifactId`, package `calc.mod`, lớp `ModOperation`, `calc.operation: mod` và cổng trong `application.yml`.
2. Thêm `<module>mod-service</module>` vào `pom.xml` gốc.
3. Thêm `add("mod", "Chia lấy dư", "%", 2)` vào `OperationCatalog` (gateway) và một mục trong `OPS` của `frontend/public/app.js`.
4. Thêm service vào `docker-compose.yml` và một file `k8s/base/20-mod-service.yaml`.

## Lỗi thường gặp

- **Giao diện báo "Không kết nối được gateway"** → chưa chạy `GatewayApplication`, hoặc gateway chưa kết nối xong Kafka.
- **Service hiện "không phản hồi"** → service đó chưa chạy hoặc không nối được Kafka. Kiểm tra Kafka còn mở ở `localhost:9092`.
- **`Connection to node -1 (localhost/127.0.0.1:9092) could not be established`** → Kafka chưa chạy.
- **Trùng cổng** → đổi biến môi trường `SERVER_PORT` trong Run Configuration của ứng dụng đó.
- **`release version 17 not supported`** → IntelliJ đang dùng JDK cũ hơn 17: *File → Project Structure → Project SDK*.
# hoc-git-bai-3
