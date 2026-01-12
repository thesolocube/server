package org.example.socketproject.server;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "server_logs")
public class ServerLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "log_type", nullable = false, length = 50)
    private String logType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    public ServerLog() {
        this.timestamp = LocalDateTime.now();
    }

    public ServerLog(String logType, String message) {
        this();
        this.logType = logType;
        this.message = message;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLogType() { return logType; }
    public void setLogType(String logType) { this.logType = logType; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
