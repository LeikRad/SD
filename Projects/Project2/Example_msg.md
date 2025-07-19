# Connect & Disconnect Message 

```json
{
    type: "connect",
    id: "123",
    client_type: "Voter/Clerk/Pollster"
}
```
```json
{
    type: "disconnect",
    id: "123",
    client_type: "Voter/Clerk/Pollster"
}
```

# Voter to ID Check (client / server)

Connection & Disconnection Message only

# ID Check to Voter (server / client)

Voter can vote response
```json
{
    type: "can_vote_response",
    response: "true"
}
```

# Clerk to ID Check (server / client)

Connection Message

ID Request:

```json
{
    type: "id_request",
    id: "123"
}
```

Check ID:
```json
{
    type: "check_id_request",
    id: "123",
    voter_id: "456"
}
```

Respond to Voter:
```json
{
    type: "respond_voter_request",
    id: "123"
    voter_id: "456"
    status: "true"
}
```

# ID Check to Clerk (server / client)

ID request response:

```json
{
    type: "id_request_response",
    voter_id: "456"
}
```

Check ID response:
```json
{
    type: "check_id_response",
    voter_id: "456",
    voted: "true"
}
```

# Voter to Polling Station (client / server):

Vote request:
```json
{
    type: "vote_request",
    vote: "string"
}
```

# Allow Entrance Message

This message is used to signal that entrance is allowed.
```json
{
    "type": "allow_entrance"
}
```

# Voter to Pollster Station (client / server):

Connection message

Poll request response:
```json
{
    type: "poll_response",
    vote: "string"
}
```

# Pollster Station to voter (server / client):

Poll request message:
```json
{
    type: "poll_request",
    accept: "true"
}
```

# Pollster to Pollster Station (client / server):

Connection Message

Accept/decline interview:
```json
{
    type: "interview_response",
    accept: "true"
}
```

# Pollster Station to Pollster (server / clinet):

Interview request:
```json
{
    type: "interview_request"
}
```