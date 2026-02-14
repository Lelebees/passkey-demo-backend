```mermaid
erDiagram
    User ||--|{ Passkey : has
    User {
        UUID id PK
        String name
    }
    
    Passkey {
        String id PK "base64url encoded"
        UUID user FK
        DateTime createdAt
        String description
        String createdByUserAgent
        String publicKey "Public key format"
    }
```