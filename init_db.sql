-- init_db.sql for SmartEventOrganizer
-- Run with: mysql -u root -p < init_db.sql

CREATE DATABASE IF NOT EXISTS smart_events CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smart_events;

CREATE TABLE IF NOT EXISTS events (
  id INT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  date DATE NOT NULL,
  deadline DATE NOT NULL,
  venue VARCHAR(255) NOT NULL,
  category VARCHAR(100) NOT NULL,
  capacity INT NOT NULL,
  created_by VARCHAR(255) NOT NULL,
  language VARCHAR(50) NOT NULL,
  region VARCHAR(100) NOT NULL,
  result TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS participants (
  id INT AUTO_INCREMENT PRIMARY KEY,
  event_id INT NOT NULL,
  participant_name VARCHAR(255) NOT NULL,
  UNIQUE KEY uniq_participant (event_id, participant_name),
  FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
