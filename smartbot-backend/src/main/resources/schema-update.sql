-- Schema update for Conversation entity
-- This script creates the necessary tables for conversation logging

CREATE TABLE IF NOT EXISTS conversations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(36),
    user_message TEXT,
    bot_response TEXT,
    sources TEXT,
    response_time_ms BIGINT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_rating INTEGER,
    feedback TEXT,
    intent VARCHAR(100),
    category VARCHAR(100),
    INDEX idx_session_id (session_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_intent (intent),
    INDEX idx_category (category)
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_conversations_date_range ON conversations(timestamp);
CREATE INDEX IF NOT EXISTS idx_conversations_intent ON conversations(intent);
CREATE INDEX IF NOT EXISTS idx_conversations_category ON conversations(category);