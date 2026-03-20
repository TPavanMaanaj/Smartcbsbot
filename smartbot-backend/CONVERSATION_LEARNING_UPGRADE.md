# Conversation Learning System - Upgrade Documentation

## Overview
This upgrade enables the SmartBot to learn efficiently from chat conversations by automatically capturing, scoring, and training from high-quality user interactions.

## Key Features

### 1. Automatic Conversation Capture
- Every non-greeting chat interaction is automatically stored
- Captures user questions and AI responses with metadata
- Tracks session information and timestamps

### 2. Intelligent Quality Scoring
Conversations are automatically scored on a 0-100 scale based on:

**Length-based scoring (10 points)**
- Longer, more detailed responses receive higher scores
- User message length also considered

**Completeness scoring (25 points)**
- Detects complete answers with structured responses
- Identifies incomplete responses that require more training

**Context usage scoring (20 points)**
- Awards points when RAG context is effectively used
- Detects phrases like "based on", "according to", etc.

**Relevance scoring (20 points)**
- Measures how well the response addresses the question
- Counts matching key terms between question and answer

**Helpfulness scoring (15 points)**
- Identifies helpful structures (numbered lists, steps, examples)
- Awards points for clear instructional content

**User feedback bonus (10 points)**
- Additional points based on user ratings (1-5 stars)

### 3. Auto-Training System
- **Scheduled Training**: Automatically trains from high-quality conversations every 6 hours
- **Quality Threshold**: Only uses conversations with quality score ≥ 70
- **Batch Processing**: Processes conversations in batches of 10 to avoid overload
- **Incremental Learning**: Adds to existing knowledge without retraining everything

### 4. User Feedback Integration
- Users can rate responses (1-5 stars)
- Users can provide textual feedback
- Ratings automatically improve quality scores
- Feedback helps prioritize the best responses for training

### 5. Learning Analytics
- Track total conversations processed
- Monitor learned vs unlearned conversations
- View average quality scores
- Identify high-quality conversation patterns

## New API Endpoints

### Conversation Management
```
GET    /api/conversations/session/{sessionId}     - Get all conversations for a session
GET    /api/conversations/user/{userId}           - Get all conversations for a user
GET    /api/conversations/{id}                    - Get specific conversation
GET    /api/conversations/unlearned?minScore=70   - Get high-quality unlearned conversations
DELETE /api/conversations/{id}                    - Delete a conversation
```

### Feedback System
```
POST   /api/conversations/{id}/feedback
Body: {"rating": 5, "comment": "Very helpful response!"}
```

### Training Control
```
POST   /api/conversations/train?minScore=70&maxConversations=50  - Manual training trigger
GET    /api/conversations/stats                                 - Get learning statistics
POST   /api/conversations/reset                                 - Reset learned status (for re-training)
```

### Quality Filtering
```
GET    /api/conversations/quality?minScore=80&maxScore=100  - Get conversations by quality range
```

## Configuration Options

In `application.properties`:
```properties
# Enable/disable auto-training
learning.auto-train.enabled=true

# Minimum quality score for training (0-100)
learning.auto-train.min-quality-score=70

# Number of conversations to process per batch
learning.auto-train.batch-size=10

# Training schedule (cron expression - every 6 hours by default)
learning.auto-train.schedule.cron=0 0 */6 * * ?
```

## How It Works

### 1. Conversation Flow
```
User Message → ChatService → Store Conversation → Quality Scoring → Ready for Learning
     ↓              ↓              ↓                    ↓              ↓
  Response      Vector Store   Database           Auto-scored      Training Queue
```

### 2. Quality Assessment Process
1. **Real-time Storage**: Conversation saved immediately after response
2. **Automated Analysis**: QualityService analyzes response content
3. **Scoring**: Multi-factor quality score calculated (0-100)
4. **Categorization**: Marked as complete/incomplete, context-used/not-used
5. **Learning Queue**: High-quality conversations queued for training

### 3. Training Process
1. **Scheduled Trigger**: Auto-training runs every 6 hours
2. **Quality Filtering**: Selects conversations with score ≥ 70
3. **Batch Selection**: Takes up to 10 conversations per batch
4. **Content Conversion**: Formats Q&A pairs for training
5. **Vector Embedding**: Adds to vector store knowledge base
6. **Status Update**: Marks conversations as "learned"

## Benefits

### For Model Performance
- **Continuous Improvement**: Model learns from real user interactions
- **Quality Control**: Only high-quality responses used for training
- **Context Awareness**: Learns to better use RAG context
- **Response Completeness**: Improves answer thoroughness over time

### For Users
- **Better Responses**: Model improves based on what users actually ask
- **Feedback Loop**: Users can rate responses to guide learning
- **Personalization**: Model adapts to user interaction patterns
- **Transparency**: Can see which conversations contributed to learning

### For Administrators
- **Automated Process**: No manual intervention required
- **Configurable**: Adjust quality thresholds and schedules
- **Monitoring**: Detailed statistics and analytics
- **Control**: Manual training triggers and resets available

## Example Usage

### 1. Check Learning Statistics
```bash
curl http://localhost:8080/api/conversations/stats
```

Response:
```json
{
  "totalConversations": 150,
  "learnedConversations": 45,
  "unlearnedConversations": 105,
  "highQualityConversations": 87,
  "averageQualityScore": 76.5,
  "autoTrainEnabled": true,
  "minQualityScore": 70,
  "batchSize": 10
}
```

### 2. Add User Feedback
```bash
curl -X POST http://localhost:8080/api/conversations/abc123/feedback \
  -H "Content-Type: application/json" \
  -d '{"rating": 5, "comment": "Perfect answer!"}'
```

### 3. Manual Training Trigger
```bash
curl -X POST "http://localhost:8080/api/conversations/train?minScore=80&maxConversations=20"
```

## Best Practices

### Quality Score Optimization
- Encourage detailed, structured responses
- Use clear instructional language
- Reference specific context when available
- Include examples and step-by-step guidance

### Feedback Collection
- Prompt users to rate helpful responses
- Collect textual feedback for improvement insights
- Focus on responses that fully address the question

### Monitoring
- Regularly check learning statistics
- Review high-quality conversations
- Adjust quality thresholds based on performance
- Monitor training job success rates

## Future Enhancements

### Planned Features
1. **Multi-user Collaboration Filtering**: Prioritize responses validated by multiple users
2. **Domain-specific Quality Models**: Custom scoring for different topics
3. **Active Learning**: Proactively ask users to clarify ambiguous responses
4. **A/B Testing**: Compare different response strategies
5. **Learning from Failed Interactions**: Improve based on "I don't know" responses

This conversation learning system transforms SmartBot from a static knowledge base into a continuously improving AI assistant that gets better with every interaction.