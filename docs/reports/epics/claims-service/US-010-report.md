# Execution Report – US-010: Upload and List Documents

## 1. User Story Summary
- **ID:** US-010
- **Title:** Upload and List Documents
- **Description:** Users can upload PDF documents to their claims and view the list of uploaded documents.

## 2. Functional Overview
- `POST /claims/{id}/documents` accepts multipart PDF uploads (max 10MB)
- Only `application/pdf` MIME type accepted — returns 400 for others
- File stored to `uploads/claims/{claimId}/` on the local filesystem
- Document metadata persisted to DB (filename, path, size, MIME type)
- `GET /claims/{id}/documents` lists all documents for a user-owned claim
- Ownership check enforced — 403 if claim belongs to another user

## 3. API Details

### POST /api/claims/v1/claims/{id}/documents
- **URL:** `http://localhost:8082/api/claims/v1/claims/{claimId}/documents`
- **Headers:** `Authorization: Bearer <JWT>`, `Content-Type: multipart/form-data`
- **Form field:** `file` (PDF, max 10MB)

**Success Response (201):**
```json
{
  "timestamp": "2026-07-10T12:20:00",
  "status": 201,
  "message": "Document uploaded successfully",
  "data": {
    "documentId": "d1e2f3a4-b5c6-7890-abcd-ef1234567890",
    "originalFilename": "claim-evidence.pdf",
    "filePath": "uploads/claims/c1d2e3f4.../uuid.pdf",
    "mimeType": "application/pdf",
    "fileSizeBytes": 204800,
    "uploadedAt": "2026-07-10T12:20:00"
  }
}
```

### GET /api/claims/v1/claims/{id}/documents
- **URL:** `http://localhost:8082/api/claims/v1/claims/{claimId}/documents`
- **Headers:** `Authorization: Bearer <JWT>`

**Response (200):**
```json
{
  "timestamp": "2026-07-10T12:25:00",
  "status": 200,
  "message": "Documents retrieved",
  "data": [
    {
      "documentId": "d1e2f3a4-b5c6-7890-abcd-ef1234567890",
      "originalFilename": "claim-evidence.pdf",
      "filePath": "uploads/claims/c1d2e3f4.../uuid.pdf",
      "mimeType": "application/pdf",
      "fileSizeBytes": 204800,
      "uploadedAt": "2026-07-10T12:20:00"
    }
  ]
}
```

## 4. Database Changes
- **Table:** `claims.documents`
- **Columns:** id (UUID PK), claim_id, original_filename, stored_filename, file_path, mime_type, file_size_bytes, uploaded_at

## 5. Data Inserted
- No seed data. Documents created via file upload.

## 6. Postman Testing Guide

### Test 1: Upload PDF Document
- Method: POST
- URL: `http://localhost:8082/api/claims/v1/claims/{{claimId}}/documents`
- Headers: `Authorization: Bearer {{token}}`
- Body: form-data with key `file`, value: select a PDF file
- Expected: 201 Created

### Test 2: Upload Non-PDF File (400)
- Method: POST
- URL: `http://localhost:8082/api/claims/v1/claims/{{claimId}}/documents`
- Headers: `Authorization: Bearer {{token}}`
- Body: form-data with key `file`, value: select a .txt file
- Expected: 400 Bad Request ("Only PDF files are accepted")

### Test 3: List Documents
```json
{
  "method": "GET",
  "url": "http://localhost:8082/api/claims/v1/claims/{{claimId}}/documents",
  "headers": {
    "Authorization": "Bearer {{token}}"
  },
  "expectedStatus": 200
}
```

## 7. Test Coverage
- File upload and ownership validation covered by integration test patterns
- Unit tests verify ownership checks via ClaimServiceImpl tests

## 8. Notes / Assumptions
- Max file size configured via `spring.servlet.multipart.max-file-size=10MB`
- Files stored locally; in production, use S3 or similar object storage
