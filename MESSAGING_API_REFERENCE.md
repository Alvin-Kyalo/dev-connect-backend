# Messaging API Endpoints Reference

## Base URL
`/api/messages`

## Endpoints

### 1. Get User's Chats/Conversations
**GET** `/api/messages/chats/{userId}`

Returns all conversations for a specific user with metadata.

**Response:**
```json
[
  {
    "id": 1,
    "userId": 2,
    "userName": "Jane Smith",
    "userAvatar": null,
    "userRole": "developer",
    "userStatus": "online",
    "lastMessage": "Hello!",
    "lastMessageTime": "2025-11-17T10:00:00",
    "unreadCount": 3,
    "projectId": null
  }
]
```

---

### 2. Get Messages Between Two Users
**GET** `/api/messages/conversation?userId1={id1}&userId2={id2}`

Returns all messages between two users. Creates conversation if it doesn't exist.

**Query Parameters:**
- `userId1` - First user's ID
- `userId2` - Second user's ID

**Response:**
```json
[
  {
    "id": 1,
    "senderId": 1,
    "receiverId": 2,
    "text": "Hello Jane!",
    "status": "sent",
    "timestamp": "2025-11-17T10:00:00",
    "projectId": null
  },
  {
    "id": 2,
    "senderId": 2,
    "receiverId": 1,
    "text": "Hi John!",
    "status": "read",
    "timestamp": "2025-11-17T10:01:00",
    "projectId": null
  }
]
```

---

### 3. Get Messages in a Conversation
**GET** `/api/messages/conversation/{conversationId}?userId={userId}`

Returns all messages in a specific conversation.

**Path Parameters:**
- `conversationId` - The conversation ID

**Query Parameters:**
- `userId` - The requesting user's ID (for authorization)

**Response:** Same as endpoint #2

---

### 4. Send a Message
**POST** `/api/messages/send`

Sends a message from one user to another.

**Request Body:**
```json
{
  "senderId": 1,
  "receiverId": 2,
  "text": "Hello Jane!",
  "status": "sent"
}
```

**Response:**
```json
{
  "id": 1,
  "senderId": 1,
  "receiverId": 2,
  "text": "Hello Jane!",
  "status": "sent",
  "timestamp": "2025-11-17T10:00:00",
  "projectId": null
}
```

**Note:** This also sends a real-time notification via WebSocket to the receiver.

---

### 5. Mark Messages as Read
**PUT** `/api/messages/read?conversationId={id}&readerId={readerId}`

Marks all unread messages in a conversation as read.

**Query Parameters:**
- `conversationId` - The conversation ID
- `readerId` - The ID of the user marking messages as read

**Response:**
```json
{
  "message": "Messages marked as read"
}
```

**Note:** This also sends read receipts via WebSocket to the original senders.

---

### 6. Update User Status
**PUT** `/api/messages/status/{userId}?status={status}`

Updates a user's online/offline status.

**Path Parameters:**
- `userId` - The user's ID

**Query Parameters:**
- `status` - Either "online" or "offline"

**Response:**
```json
{
  "message": "Status updated successfully"
}
```

---

### 7. Get User Status
**GET** `/api/messages/status/{userId}`

Gets a user's current online/offline status.

**Path Parameters:**
- `userId` - The user's ID

**Response:**
```json
{
  "status": "online"
}
```

---

## WebSocket Integration

### Connection
Connect to: `ws://localhost:8081/ws`

### Subscribe to Channels

**Receive Messages:**
```javascript
stompClient.subscribe('/user/queue/messages', (message) => {
  // Handle incoming message
  const messageData = JSON.parse(message.body);
});
```

**Receive Read Receipts:**
```javascript
stompClient.subscribe('/user/queue/read-receipts', (receipt) => {
  // Handle read receipt
  const receiptData = JSON.parse(receipt.body);
});
```

**Receive Delivery Receipts:**
```javascript
stompClient.subscribe('/user/queue/delivery-receipts', (receipt) => {
  // Handle delivery receipt
  const receiptData = JSON.parse(receipt.body);
});
```

### Send Messages via WebSocket
```javascript
stompClient.send('/app/chat', {}, JSON.stringify({
  senderId: 1,
  receiverId: 2,
  text: "Hello!"
}));
```

---

## Error Responses

All endpoints return `400 Bad Request` on error with no body.

Common error scenarios:
- User not found
- Conversation not found
- User not authorized to access conversation
- Invalid status value
- Missing required parameters

---

## Message Status Values

- `sent` - Message has been sent
- `delivered` - Message has been delivered to recipient
- `read` - Message has been read by recipient

---

## Notes

1. All timestamps are in ISO 8601 format
2. IDs are Long (number) type in JSON
3. Real-time updates are delivered via WebSocket
4. Conversations are automatically created when sending messages
5. User status updates are not automatically broadcast (implement separately if needed)

